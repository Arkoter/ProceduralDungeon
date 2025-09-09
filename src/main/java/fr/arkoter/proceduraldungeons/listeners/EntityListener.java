package fr.arkoter.proceduraldungeons.listeners;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.models.Dungeon;
import fr.arkoter.proceduraldungeons.models.DungeonPlayer;
import fr.arkoter.proceduraldungeons.utils.LootUtils;
import fr.arkoter.proceduraldungeons.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EntityListener implements Listener {

    private final ProceduralDungeons plugin;

    public EntityListener(ProceduralDungeons plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        DungeonPlayer dungeonPlayer = plugin.getDungeonManager().getDungeonPlayer(killer.getUniqueId());

        if (!dungeonPlayer.isInDungeon()) {
            return;
        }

        String dungeonName = dungeonPlayer.getCurrentDungeon();
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonName);

        if (dungeon == null) {
            return;
        }

        // Vérifier si l'entité est dans les limites du donjon
        if (!plugin.getDungeonManager().isLocationInDungeon(entity.getLocation(), dungeonName)) {
            return;
        }

        // Incrémenter le compteur de monstres tués
        dungeonPlayer.incrementMonstersKilled();
        dungeon.incrementTotalMonstersKilled();

        // Vérifier si c'est le boss du donjon
        if (isBossEntity(entity, dungeon)) {
            handleBossKill(killer, entity, dungeon, dungeonPlayer, event);
        } else {
            handleRegularMonsterKill(killer, entity, dungeon, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntitySpawn(EntitySpawnEvent event) {
        // Empêcher le spawn naturel de monstres dans les donjons
        Location spawnLocation = event.getLocation();

        for (Dungeon dungeon : plugin.getDungeonManager().getDungeons().values()) {
            if (plugin.getDungeonManager().isLocationInDungeon(spawnLocation, dungeon.getName())) {
                // Permettre seulement certains types d'entités
                if (!isAllowedInDungeon(event.getEntityType())) {
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    private boolean isBossEntity(LivingEntity entity, Dungeon dungeon) {
        if (dungeon.getBossLocation() == null) {
            return false;
        }

        // Vérifier la proximité avec la position du boss
        double distance = entity.getLocation().distance(dungeon.getBossLocation());

        // Le boss doit être dans un rayon de 5 blocs de sa position de spawn
        if (distance > 5.0) {
            return false;
        }

        // Vérifier le type d'entité selon la difficulté
        EntityType expectedBossType = getBossTypeForDifficulty(dungeon.getDifficulty());
        return entity.getType() == expectedBossType;
    }

    private EntityType getBossTypeForDifficulty(int difficulty) {
        switch (difficulty) {
            case 1:
                return EntityType.ZOMBIE;
            case 2:
                return EntityType.SKELETON;
            case 3:
                return EntityType.WITHER_SKELETON;
            default:
                return EntityType.WITHER;
        }
    }

    private void handleBossKill(Player killer, LivingEntity boss, Dungeon dungeon, DungeonPlayer dungeonPlayer, EntityDeathEvent event) {
        // Marquer le boss comme tué
        dungeon.setBossAlive(false);

        // Message de victoire
        killer.sendMessage(MessageUtils.getMessage("messages.boss.defeated", "{name}", dungeon.getName()));

        // Son de victoire
        killer.getWorld().playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

        // Récompenses spéciales du boss
        List<ItemStack> bossLoot = generateBossLoot(dungeon.getDifficulty());

        // Remplacer le loot normal par le loot du boss
        event.getDrops().clear();
        event.getDrops().addAll(bossLoot);

        // Bonus d'expérience
        event.setDroppedExp(event.getDroppedExp() * (2 + dungeon.getDifficulty()));

        // Incrémenter les statistiques
        dungeon.incrementTimesCompleted();
        dungeonPlayer.setCompletionTime(System.currentTimeMillis() - dungeonPlayer.getEnterTime());

        // Mettre à jour le record de vitesse si nécessaire
        long completionTime = dungeonPlayer.getCompletionTime();
        if (dungeon.getFastestCompletion() == 0 || completionTime < dungeon.getFastestCompletion()) {
            dungeon.setFastestCompletion(completionTime);
            killer.sendMessage(MessageUtils.getMessage("messages.dungeon.new-record"));
        }

        // Augmenter la difficulté du donjon
        dungeon.setDifficulty(Math.min(dungeon.getDifficulty() + 1, 10));
        killer.sendMessage(MessageUtils.getMessage("messages.dungeon.difficulty-increased",
                "{level}", String.valueOf(dungeon.getDifficulty())));

        // Particules de célébration
        spawnCelebrationParticles(killer.getLocation());
    }

    private void handleRegularMonsterKill(Player killer, LivingEntity monster, Dungeon dungeon, EntityDeathEvent event) {
        // Bonus de loot basé sur la difficulté
        if (plugin.getConfigManager().getRandom().nextInt(100) < dungeon.getDifficulty() * 5) {
            // Ajouter un objet bonus
            ItemStack bonusLoot = generateBonusLoot(dungeon.getDifficulty());
            if (bonusLoot != null) {
                event.getDrops().add(bonusLoot);
            }
        }

        // Bonus d'expérience basé sur la difficulté
        int bonusExp = dungeon.getDifficulty() + plugin.getConfigManager().getRandom().nextInt(dungeon.getDifficulty() + 1);
        event.setDroppedExp(event.getDroppedExp() + bonusExp);
    }

    private List<ItemStack> generateBossLoot(int difficulty) {
        List<ItemStack> loot = new ArrayList<>();

        // Loot de base
        loot.add(new ItemStack(Material.DIAMOND, 3 + difficulty));
        loot.add(new ItemStack(Material.EMERALD, 2 + difficulty));
        loot.add(new ItemStack(Material.GOLD_INGOT, 5 + difficulty * 2));

        // Loot rare basé sur la difficulté
        if (difficulty >= 3) {
            loot.add(new ItemStack(Material.NETHER_STAR));
        }

        if (difficulty >= 5) {
            loot.add(new ItemStack(Material.ANCIENT_DEBRIS, 1 + plugin.getConfigManager().getRandom().nextInt(2)));
        }

        // Équipement enchanté
        ItemStack enchantedItem = generateEnchantedEquipment(difficulty);
        if (enchantedItem != null) {
            loot.add(enchantedItem);
        }

        return loot;
    }

    private ItemStack generateBonusLoot(int difficulty) {
        Material[] possibleLoot = {
                Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND,
                Material.EMERALD, Material.REDSTONE, Material.LAPIS_LAZULI
        };

        Material selectedMaterial = possibleLoot[plugin.getConfigManager().getRandom().nextInt(possibleLoot.length)];
        int amount = 1 + plugin.getConfigManager().getRandom().nextInt(difficulty);

        return new ItemStack(selectedMaterial, amount);
    }

    private ItemStack generateEnchantedEquipment(int difficulty) {
        Material[] equipment = {
                Material.DIAMOND_SWORD, Material.DIAMOND_AXE, Material.DIAMOND_PICKAXE,
                Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET, Material.BOW
        };

        Material selectedEquipment = equipment[plugin.getConfigManager().getRandom().nextInt(equipment.length)];
        ItemStack item = new ItemStack(selectedEquipment);

        // Utiliser LootUtils pour ajouter des enchantements
        // Cette méthode devrait être implémentée dans LootUtils
        return item;
    }

    private boolean isAllowedInDungeon(EntityType entityType) {
        // Types d'entités autorisés dans les donjons
        switch (entityType) {
            case ZOMBIE:
            case SKELETON:
            case SPIDER:
            case CREEPER:
            case WITHER_SKELETON:
            case WITHER:
            case BLAZE:
            case ENDERMAN:
                return true;
            default:
                return false;
        }
    }

    private void spawnCelebrationParticles(Location location) {
        // Particules de feu d'artifice
        for (int i = 0; i < 20; i++) {
            Location particleLoc = location.clone().add(
                    plugin.getConfigManager().getRandom().nextGaussian() * 2,
                    plugin.getConfigManager().getRandom().nextDouble() * 3,
                    plugin.getConfigManager().getRandom().nextGaussian() * 2
            );

            location.getWorld().spawnParticle(
                    org.bukkit.Particle.FIREWORKS_SPARK,
                    particleLoc, 1
            );
        }

        // Particules dorées
        for (int i = 0; i < 15; i++) {
            Location particleLoc = location.clone().add(
                    plugin.getConfigManager().getRandom().nextGaussian() * 1.5,
                    plugin.getConfigManager().getRandom().nextDouble() * 2,
                    plugin.getConfigManager().getRandom().nextGaussian() * 1.5
            );

            location.getWorld().spawnParticle(
                    org.bukkit.Particle.VILLAGER_HAPPY,
                    particleLoc, 1
            );
        }
    }
}