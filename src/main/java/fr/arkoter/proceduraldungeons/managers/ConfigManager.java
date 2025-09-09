package fr.arkoter.proceduraldungeons.managers;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
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
        config.addDefault("dungeon.default-difficulty", 1);
        config.addDefault("dungeon.default-size", 50);
        config.addDefault("dungeon.wall-height", 4);
        config.addDefault("dungeon.floor-material", "STONE_BRICKS");
        config.addDefault("dungeon.wall-material", "COBBLESTONE");

        config.addDefault("loot.diamond-chance", 20);
        config.addDefault("loot.rare-item-chance", 15);
        config.addDefault("loot.boss-key-chance", 25);
        config.addDefault("loot.difficulty-multiplier", 1.5);
        config.addDefault("loot.min-chests-per-dungeon", 3);
        config.addDefault("loot.max-chests-per-dungeon", 8);

        config.addDefault("boss.health-multiplier", 1.5);
        config.addDefault("boss.experience-multiplier", 2.0);
        config.addDefault("boss.loot-bonus-multiplier", 3.0);

        config.addDefault("traps.damage", 2.0);
        config.addDefault("traps.damage-multiplier", 1.2);
        config.addDefault("traps.min-traps-per-dungeon", 5);
        config.addDefault("traps.max-traps-per-dungeon", 20);
        config.addDefault("traps.trap-density", 0.15);
        config.addDefault("traps.poison-duration", 200);
        config.addDefault("traps.poison-level", 1);

        config.addDefault("generation.extra-connections", 0.01);
        config.addDefault("generation.special-rooms.min", 3);
        config.addDefault("generation.special-rooms.max", 6);
        config.addDefault("generation.room-size.min", 5);
        config.addDefault("generation.room-size.max", 9);
        config.addDefault("generation.boss-room-size", 12);
        config.addDefault("generation.seed", 0);

        config.addDefault("performance.generation-delay", 50);
        config.addDefault("performance.max-active-dungeons", 10);
        config.addDefault("performance.cleanup-threshold", 3600000);
        config.addDefault("performance.auto-save-interval", 300);

        config.addDefault("experience.monster-kill-bonus", 1.5);
        config.addDefault("experience.completion-bonus", 100);
        config.addDefault("experience.difficulty-bonus", 50);

        config.addDefault("debug.enabled", false);
        config.addDefault("debug.show-generation-info", false);
        config.addDefault("debug.show-performance-stats", false);

        config.addDefault("advanced.async-generation", true);
        config.addDefault("advanced.preload-chunks", true);
        config.addDefault("advanced.max-monsters-per-dungeon", 50);
        config.addDefault("advanced.monster-respawn", true);
        config.addDefault("advanced.monster-respawn-delay", 300);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    // Getters pour les paramètres de donjon
    public int getMaxDungeonSize() {
        return config.getInt("dungeon.max-size", 100);
    }

    public int getMinDungeonSize() {
        return config.getInt("dungeon.min-size", 30);
    }

    public int getMaxDifficulty() {
        return config.getInt("dungeon.max-difficulty", 10);
    }

    public int getDefaultDifficulty() {
        return config.getInt("dungeon.default-difficulty", 1);
    }

    public int getDefaultSize() {
        return config.getInt("dungeon.default-size", 50);
    }

    public int getWallHeight() {
        return config.getInt("dungeon.wall-height", 4);
    }

    public String getFloorMaterial() {
        return config.getString("dungeon.floor-material", "STONE_BRICKS");
    }

    public String getWallMaterial() {
        return config.getString("dungeon.wall-material", "COBBLESTONE");
    }

    // Getters pour les paramètres de loot
    public int getDiamondChance() {
        return config.getInt("loot.diamond-chance", 20);
    }

    public int getRareItemChance() {
        return config.getInt("loot.rare-item-chance", 15);
    }

    public int getBossKeyChance() {
        return config.getInt("loot.boss-key-chance", 25);
    }

    public double getDifficultyMultiplier() {
        return config.getDouble("loot.difficulty-multiplier", 1.5);
    }

    public int getMinChestsPerDungeon() {
        return config.getInt("loot.min-chests-per-dungeon", 3);
    }

    public int getMaxChestsPerDungeon() {
        return config.getInt("loot.max-chests-per-dungeon", 8);
    }

    // Getters pour les paramètres de boss
    public double getBossHealthMultiplier() {
        return config.getDouble("boss.health-multiplier", 1.5);
    }

    public double getBossExperienceMultiplier() {
        return config.getDouble("boss.experience-multiplier", 2.0);
    }

    public double getBossLootBonusMultiplier() {
        return config.getDouble("boss.loot-bonus-multiplier", 3.0);
    }

    // Getters pour les paramètres de pièges
    public double getTrapDamage() {
        return config.getDouble("traps.damage", 2.0);
    }

    public double getTrapDamageMultiplier() {
        return config.getDouble("traps.damage-multiplier", 1.2);
    }

    public int getMinTrapsPerDungeon() {
        return config.getInt("traps.min-traps-per-dungeon", 5);
    }

    public int getMaxTrapsPerDungeon() {
        return config.getInt("traps.max-traps-per-dungeon", 20);
    }

    public double getTrapDensity() {
        return config.getDouble("traps.trap-density", 0.15);
    }

    public int getPoisonDuration() {
        return config.getInt("traps.poison-duration", 200);
    }

    public int getPoisonLevel() {
        return config.getInt("traps.poison-level", 1);
    }

    // Getters pour les paramètres de génération
    public double getExtraConnections() {
        return config.getDouble("generation.extra-connections", 0.01);
    }

    public int getMinSpecialRooms() {
        return config.getInt("generation.special-rooms.min", 3);
    }

    public int getMaxSpecialRooms() {
        return config.getInt("generation.special-rooms.max", 6);
    }

    public int getMinRoomSize() {
        return config.getInt("generation.room-size.min", 5);
    }

    public int getMaxRoomSize() {
        return config.getInt("generation.room-size.max", 9);
    }

    public int getBossRoomSize() {
        return config.getInt("generation.boss-room-size", 12);
    }

    public long getGenerationSeed() {
        return config.getLong("generation.seed", 0);
    }

    // Getters pour les paramètres de performance
    public int getGenerationDelay() {
        return config.getInt("performance.generation-delay", 50);
    }

    public int getMaxActiveDungeons() {
        return config.getInt("performance.max-active-dungeons", 10);
    }

    public long getCleanupThreshold() {
        return config.getLong("performance.cleanup-threshold", 3600000);
    }

    public int getAutoSaveInterval() {
        return config.getInt("performance.auto-save-interval", 300);
    }

    // Getters pour les paramètres d'expérience
    public double getMonsterKillBonus() {
        return config.getDouble("experience.monster-kill-bonus", 1.5);
    }

    public int getCompletionBonus() {
        return config.getInt("experience.completion-bonus", 100);
    }

    public int getDifficultyBonus() {
        return config.getInt("experience.difficulty-bonus", 50);
    }

    // Getters pour les paramètres de debug
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }

    public boolean isShowGenerationInfo() {
        return config.getBoolean("debug.show-generation-info", false);
    }

    public boolean isShowPerformanceStats() {
        return config.getBoolean("debug.show-performance-stats", false);
    }

    // Getters pour les paramètres avancés
    public boolean isAsyncGeneration() {
        return config.getBoolean("advanced.async-generation", true);
    }

    public boolean isPreloadChunks() {
        return config.getBoolean("advanced.preload-chunks", true);
    }

    public int getMaxMonstersPerDungeon() {
        return config.getInt("advanced.max-monsters-per-dungeon", 50);
    }

    public boolean isMonsterRespawn() {
        return config.getBoolean("advanced.monster-respawn", true);
    }

    public int getMonsterRespawnDelay() {
        return config.getInt("advanced.monster-respawn-delay", 300);
    }

    public Random getRandom() {
        return random;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}