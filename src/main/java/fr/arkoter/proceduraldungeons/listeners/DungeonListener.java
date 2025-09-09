package fr.arkoter.proceduraldungeons.listeners;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.models.Dungeon;
import fr.arkoter.proceduraldungeons.models.DungeonPlayer;
import fr.arkoter.proceduraldungeons.utils.LootUtils;
import fr.arkoter.proceduraldungeons.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class DungeonListener implements Listener {

    private final ProceduralDungeons plugin;
    private final Random random = new Random();

    public DungeonListener(ProceduralDungeons plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null || event.getAction() != Action.PHYSICAL) {
            return;
        }

        // Vérifier si le joueur est dans un donjon
        DungeonPlayer dungeonPlayer = plugin.getDungeonManager().getDungeonPlayer(player.getUniqueId());
        if (!dungeonPlayer.isInDungeon()) {
            return;
        }

        Location blockLocation = clickedBlock.getLocation();

        // Gestion des pièges
        if (clickedBlock.getType() == Material.STONE_PRESSURE_PLATE) {
            handleTrapActivation(player, blockLocation, dungeonPlayer);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        if (event.getClickedBlock().getType() != Material.CHEST) {
            return;
        }

        Player player = event.getPlayer();
        DungeonPlayer dungeonPlayer = plugin.getDungeonManager().getDungeonPlayer(player.getUniqueId());

        if (!dungeonPlayer.isInDungeon()) {
            return;
        }

        // Vérifier si c'est un coffre de donjon
        String dungeonName = dungeonPlayer.getCurrentDungeon();
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonName);

        if (dungeon == null) {
            return;
        }

        Location chestLocation = event.getClickedBlock().getLocation();

        // Vérifier si ce coffre fait partie du donjon
        if (dungeon.getTreasureChests().contains(chestLocation)) {
            handleTreasureChest(player, chestLocation, dungeon, dungeonPlayer);
        }
    }

    private void handleTrapActivation(Player player, Location trapLocation, DungeonPlayer dungeonPlayer) {
        String dungeonName = dungeonPlayer.getCurrentDungeon();
        Dungeon dungeon = plugin.getDungeonManager().getDungeon(dungeonName);

        if (dungeon == null) {
            return;
        }

        // Trouver le piège correspondant
        Dungeon.Trap activatedTrap = null;
        for (Dungeon.Trap trap : dungeon.getTraps()) {
            if (trap.getLocation().equals(trapLocation)) {
                activatedTrap = trap;
                break;
            }
        }

        if (activatedTrap == null || activatedTrap.isActivated()) {
            return; // Piège déjà activé ou non trouvé
        }

        // Marquer le piège comme activé
        activatedTrap.setActivated(true);

        // Appliquer l'effet du piège
        applyTrapEffect(player, activatedTrap, dungeon);

        // Message et son
        player.sendMessage(MessageUtils.getMessage("messages.traps.activated"));
        player.getWorld().playSound(trapLocation, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    private void applyTrapEffect(Player player, Dungeon.Trap trap, Dungeon dungeon) {
        double damage = plugin.getConfigManager().getTrapDamage();

        switch (trap.getType()) {
            case 0: // Piège à pression basique
                player.damage(damage);
                break;

            case 1: // Piège TNT
                player.damage(damage * 2);
                player.getWorld().createExplosion(trap.getLocation(), 2.0f, false, false);
                break;

            case 2: // Fosse de lave (déjà créée physiquement)
                player.damage(damage);
                player.setFireTicks(100); // 5 secondes de feu
                break;

            case 3: // Piège à flèches
                player.damage(damage);
                // Simuler des flèches (effet visuel/sonore)
                player.getWorld().playSound(trap.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
                break;

            case 4: // Piège d'empoisonnement
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1)); // 10 secondes
                player.sendMessage(MessageUtils.getMessage("messages.traps.poisoned"));
                break;

            case 5: // Piège de téléportation
                teleportPlayerRandomly(player, dungeon);
                player.sendMessage(MessageUtils.getMessage("messages.traps.teleported"));
                break;

            default:
                player.damage(damage);
                break;
        }

        // Effet sonore général
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
    }

    private void teleportPlayerRandomly(Player player, Dungeon dungeon) {
        Location dungeonCenter = dungeon.getLocation();
        int attempts = 10;

        for (int i = 0; i < attempts; i++) {
            int randomX = random.nextInt(dungeon.getSize()) - dungeon.getSize() / 2;
            int randomZ = random.nextInt(dungeon.getSize()) - dungeon.getSize() / 2;

            Location teleportLoc = dungeonCenter.clone().add(randomX, 1, randomZ);

            // Vérifier que la location est sûre
            if (teleportLoc.getBlock().getType().isAir() &&
                    teleportLoc.clone().add(0, 1, 0).getBlock().getType().isAir() &&
                    !teleportLoc.clone().add(0, -1, 0).getBlock().getType().isAir()) {

                player.teleport(teleportLoc);
                player.getWorld().playSound(teleportLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                return;
            }
        }

        // Si aucune location sûre trouvée, téléporter au centre du donjon
        player.teleport(dungeonCenter.clone().add(0, 2, 0));
    }

    private void handleTreasureChest(Player player, Location chestLocation, Dungeon dungeon, DungeonPlayer dungeonPlayer) {
        Chest chest = (Chest) chestLocation.getBlock().getState();
        Inventory inventory = chest.getInventory();

        // Vérifier si le coffre a déjà été ouvert
        if (!inventory.isEmpty()) {
            return; // Coffre déjà rempli, laisser le comportement normal
        }

        // Remplir le coffre avec du loot
        LootUtils.populateChest(inventory, dungeon.getDifficulty(), dungeon.getSize());

        // Chance d'obtenir une clé de boss
        if (!dungeonPlayer.hasBossKey() && random.nextInt(100) < getBossKeyChance(dungeon.getDifficulty())) {
            ItemStack bossKey = LootUtils.createBossKey(dungeon.getName());
            inventory.addItem(bossKey);
            dungeonPlayer.setBossKey(true);
            player.sendMessage(MessageUtils.getMessage("messages.treasure.key-obtained"));
        }

        // Incrémenter le compteur de trésors trouvés
        dungeonPlayer.incrementTreasuresFound();

        // Messages et effets
        player.sendMessage(MessageUtils.getMessage("messages.treasure.found"));
        player.getWorld().playSound(chestLocation, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        player.getWorld().playSound(chestLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);

        // Particules d'effet
        for (int i = 0; i < 10; i++) {
            Location particleLoc = chestLocation.clone().add(
                    random.nextGaussian() * 0.5,
                    random.nextDouble() * 2,
                    random.nextGaussian() * 0.5
            );
            player.getWorld().spawnParticle(
                    org.bukkit.Particle.VILLAGER_HAPPY,
                    particleLoc, 1
            );
        }
    }

    private int getBossKeyChance(int difficulty) {
        // Plus la difficulté est élevée, plus la chance d'obtenir une clé est faible
        int baseChance = 30;
        int difficultyReduction = Math.min(difficulty * 2, 20);
        return Math.max(baseChance - difficultyReduction, 5); // Minimum 5% de chance
    }
}