package fr.arkoter.proceduraldungeons;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.Random;

public class ConfigManager {

    private final ProceduralDungeons plugin;
    private FileConfiguration config;
    private final Random random;

    public ConfigManager(ProceduralDungeons plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void loadConfig() {
        config = plugin.getConfig();

        // Valeurs par défaut
        config.addDefault("dungeon.max-size", 100);
        config.addDefault("dungeon.min-size", 30);
        config.addDefault("dungeon.max-difficulty", 10);
        config.addDefault("loot.diamond-chance", 20);
        config.addDefault("loot.rare-item-chance", 15);
        config.addDefault("boss.health-multiplier", 1.5);
        config.addDefault("traps.damage", 2.0);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public int getMaxDungeonSize() {
        return config.getInt("dungeon.max-size", 100);
    }

    public int getMinDungeonSize() {
        return config.getInt("dungeon.min-size", 30);
    }

    public int getMaxDifficulty() {
        return config.getInt("dungeon.max-difficulty", 10);
    }

    public int getDiamondChance() {
        return config.getInt("loot.diamond-chance", 20);
    }

    public int getRareItemChance() {
        return config.getInt("loot.rare-item-chance", 15);
    }

    public double getBossHealthMultiplier() {
        return config.getDouble("boss.health-multiplier", 1.5);
    }

    public double getTrapDamage() {
        return config.getDouble("traps.damage", 2.0);
    }

    public Random getRandom() {
        return random;
    }
}