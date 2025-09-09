package fr.arkoter.proceduraldungeons.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;

public class LootUtils {

    private static final Random random = new Random();

    public static void populateChest(Inventory inventory, int difficulty, int dungeonSize) {
        inventory.clear();

        // Loot de base
        addBasicLoot(inventory, difficulty);

        // Loot rare basé sur la difficulté
        addRareLoot(inventory, difficulty);

        // Loot spécial basé sur la taille
        if (dungeonSize > 60) {
            addSpecialLoot(inventory, difficulty);
        }
    }

    private static void addBasicLoot(Inventory inventory, int difficulty) {
        // Or et fer
        int goldAmount = 2 + random.nextInt(difficulty * 3);
        int ironAmount = 1 + random.nextInt(difficulty * 2);

        inventory.addItem(new ItemStack(Material.GOLD_INGOT, goldAmount));
        inventory.addItem(new ItemStack(Material.IRON_INGOT, ironAmount));

        // Nourriture
        inventory.addItem(new ItemStack(Material.BREAD, 3 + random.nextInt(5)));
        inventory.addItem(new ItemStack(Material.COOKED_BEEF, 2 + random.nextInt(4)));

        // Potions
        if (random.nextInt(100) < 30 + difficulty * 5) {
            inventory.addItem(new ItemStack(Material.POTION, 1 + random.nextInt(2)));
        }
    }

    private static void addRareLoot(Inventory inventory, int difficulty) {
        // Diamants (chance augmente avec difficulté)
        if (random.nextInt(100) < 10 + difficulty * 5) {
            int diamondAmount = random.nextInt(difficulty) + 1;
            inventory.addItem(new ItemStack(Material.DIAMOND, diamondAmount));
        }

        // Émeraudes
        if (random.nextInt(100) < 15 + difficulty * 3) {
            int emeraldAmount = random.nextInt(difficulty) + 1;
            inventory.addItem(new ItemStack(Material.EMERALD, emeraldAmount));
        }

        // Équipement enchanté
        if (random.nextInt(100) < 20 + difficulty * 10) {
            addEnchantedEquipment(inventory, difficulty);
        }
    }

    private static void addSpecialLoot(Inventory inventory, int difficulty) {
        // Objets très rares
        if (random.nextInt(100) < 5 + difficulty * 2) {
            inventory.addItem(new ItemStack(Material.NETHER_STAR));
        }

        if (random.nextInt(100) < 10 + difficulty * 3) {
            inventory.addItem(new ItemStack(Material.ENCHANTED_BOOK));
        }

        // Blocs rares
        if (random.nextInt(100) < 15) {
            inventory.addItem(new ItemStack(Material.ANCIENT_DEBRIS, 1 + random.nextInt(2)));
        }
    }

    private static void addEnchantedEquipment(Inventory inventory, int difficulty) {
        Material[] weapons = {Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.BOW};
        Material[] armor = {Material.DIAMOND_CHESTPLATE, Material.IRON_CHESTPLATE,
                Material.DIAMOND_HELMET, Material.IRON_HELMET};
        Material[] tools = {Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE};

        Material selectedItem;
        int category = random.nextInt(3);

        switch (category) {
            case 0:
                selectedItem = weapons[random.nextInt(weapons.length)];
                break;
            case 1:
                selectedItem = armor[random.nextInt(armor.length)];
                break;
            default:
                selectedItem = tools[random.nextInt(tools.length)];
                break;
        }

        ItemStack enchantedItem = new ItemStack(selectedItem);
        addRandomEnchantments(enchantedItem, difficulty);
        inventory.addItem(enchantedItem);
    }

    private static void addRandomEnchantments(ItemStack item, int difficulty) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Enchantment[] possibleEnchantments = getPossibleEnchantments(item.getType());
        int enchantmentCount = Math.min(random.nextInt(difficulty) + 1, 3);

        for (int i = 0; i < enchantmentCount; i++) {
            if (possibleEnchantments.length > 0) {
                Enchantment enchant = possibleEnchantments[random.nextInt(possibleEnchantments.length)];
                int level = random.nextInt(Math.min(difficulty, enchant.getMaxLevel())) + 1;
                meta.addEnchant(enchant, level, true);
            }
        }

        item.setItemMeta(meta);
    }

    private static Enchantment[] getPossibleEnchantments(Material material) {
        switch (material) {
            case DIAMOND_SWORD:
            case IRON_SWORD:
                return new Enchantment[]{Enchantment.DAMAGE_ALL, Enchantment.FIRE_ASPECT,
                        Enchantment.KNOCKBACK, Enchantment.LOOT_BONUS_MOBS};
            case BOW:
                return new Enchantment[]{Enchantment.ARROW_DAMAGE, Enchantment.ARROW_FIRE,
                        Enchantment.ARROW_KNOCKBACK, Enchantment.ARROW_INFINITE};
            case DIAMOND_CHESTPLATE:
            case IRON_CHESTPLATE:
            case DIAMOND_HELMET:
            case IRON_HELMET:
                return new Enchantment[]{Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE,
                        Enchantment.PROTECTION_PROJECTILE, Enchantment.THORNS};
            case DIAMOND_PICKAXE:
            case IRON_PICKAXE:
                return new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.LOOT_BONUS_BLOCKS,
                        Enchantment.SILK_TOUCH, Enchantment.DURABILITY};
            default:
                return new Enchantment[0];
        }
    }

    public static ItemStack createBossKey(String dungeonName) {
        ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = key.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6Clé du Boss - " + dungeonName);
            meta.setLore(Arrays.asList(
                    "§7Une clé mystérieuse qui permet",
                    "§7d'accéder à la salle du boss",
                    "§7du donjon " + dungeonName
            ));
            key.setItemMeta(meta);
        }

        return key;
    }
}