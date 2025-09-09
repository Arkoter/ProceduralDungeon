package fr.arkoter.proceduraldungeons.managers;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.*;

public class ConfigManager {

    private final ProceduralDungeons plugin;
    private FileConfiguration config;
    private final Random random;

    public ConfigManager(ProceduralDungeons plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void loadConfig() {
        // Recharger la config depuis le fichier
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        // Ajouter les valeurs par défaut si elles n'existent pas
        addDefaults();

        // Sauvegarder la config avec les nouvelles valeurs par défaut
        config.options().copyDefaults(true);
        plugin.saveConfig();

        plugin.getLogger().info("Configuration chargée avec succès");
    }

    private void addDefaults() {
        // ================================
        // PARAMÈTRES DES DONJONS
        // ================================
        config.addDefault("dungeon.max-size", 100);
        config.addDefault("dungeon.min-size", 30);
        config.addDefault("dungeon.max-difficulty", 10);
        config.addDefault("dungeon.default-difficulty", 1);
        config.addDefault("dungeon.default-size", 50);
        config.addDefault("dungeon.wall-height", 4);
        config.addDefault("dungeon.floor-material", "STONE_BRICKS");
        config.addDefault("dungeon.wall-material", "COBBLESTONE");
        config.addDefault("dungeon.special-room-material", "POLISHED_ANDESITE");

        // ================================
        // PARAMÈTRES DU LOOT
        // ================================
        config.addDefault("loot.diamond-chance", 20);
        config.addDefault("loot.rare-item-chance", 15);
        config.addDefault("loot.boss-key-chance", 25);
        config.addDefault("loot.difficulty-multiplier", 1.5);
        config.addDefault("loot.min-chests-per-dungeon", 3);
        config.addDefault("loot.max-chests-per-dungeon", 8);

        // ================================
        // PARAMÈTRES DES BOSS
        // ================================
        config.addDefault("boss.health-multiplier", 1.5);
        config.addDefault("boss.experience-multiplier", 2.0);
        config.addDefault("boss.loot-bonus-multiplier", 3.0);

        // Types de boss par difficulté
        config.addDefault("boss.boss-types.1", "ZOMBIE");
        config.addDefault("boss.boss-types.2", "SKELETON");
        config.addDefault("boss.boss-types.3", "WITHER_SKELETON");
        config.addDefault("boss.boss-types.4", "BLAZE");
        config.addDefault("boss.boss-types.5", "WITHER");

        // ================================
        // PARAMÈTRES DES PIÈGES
        // ================================
        config.addDefault("traps.damage", 2.0);
        config.addDefault("traps.damage-multiplier", 1.2);
        config.addDefault("traps.min-traps-per-dungeon", 5);
        config.addDefault("traps.max-traps-per-dungeon", 20);
        config.addDefault("traps.trap-density", 0.15);
        config.addDefault("traps.poison-duration", 200);
        config.addDefault("traps.poison-level", 1);

        // ================================
        // PARAMÈTRES DE GÉNÉRATION
        // ================================
        config.addDefault("generation.extra-connections", 0.01);
        config.addDefault("generation.special-rooms.min", 3);
        config.addDefault("generation.special-rooms.max", 6);
        config.addDefault("generation.room-size.min", 5);
        config.addDefault("generation.room-size.max", 9);
        config.addDefault("generation.boss-room-size", 12);
        config.addDefault("generation.seed", 0);

        // ================================
        // PARAMÈTRES DE PERFORMANCE
        // ================================
        config.addDefault("performance.generation-delay", 50);
        config.addDefault("performance.max-active-dungeons", 10);
        config.addDefault("performance.cleanup-threshold", 3600000);
        config.addDefault("performance.auto-save-interval", 300);

        // ================================
        // PARAMÈTRES D'EXPÉRIENCE
        // ================================
        config.addDefault("experience.monster-kill-bonus", 1.5);
        config.addDefault("experience.completion-bonus", 100);
        config.addDefault("experience.difficulty-bonus", 50);

        // ================================
        // PARAMÈTRES DE DEBUG
        // ================================
        config.addDefault("debug.enabled", false);
        config.addDefault("debug.show-generation-info", false);
        config.addDefault("debug.show-performance-stats", false);

        // ================================
        // PARAMÈTRES AVANCÉS
        // ================================
        config.addDefault("advanced.async-generation", true);
        config.addDefault("advanced.preload-chunks", true);
        config.addDefault("advanced.max-monsters-per-dungeon", 50);
        config.addDefault("advanced.monster-respawn", true);
        config.addDefault("advanced.monster-respawn-delay", 300);

        // ================================
        // PARAMÈTRES DE LIMITES
        // ================================
        config.addDefault("limits.max-dungeons-per-player", 5);
        config.addDefault("limits.max-players-per-dungeon", 10);
        config.addDefault("limits.creation-cooldown", 60);
        config.addDefault("limits.allowed-worlds", Arrays.asList("world", "world_nether", "world_the_end"));

        // ================================
        // PARAMÈTRES DE THÈMES
        // ================================
        config.addDefault("themes.enabled", true);

        // Thème médiéval
        config.addDefault("themes.medieval.floor", "STONE_BRICKS");
        config.addDefault("themes.medieval.wall", "COBBLESTONE");
        config.addDefault("themes.medieval.decoration", "STONE");

        // Thème nether
        config.addDefault("themes.nether.floor", "NETHER_BRICKS");
        config.addDefault("themes.nether.wall", "BLACKSTONE");
        config.addDefault("themes.nether.decoration", "MAGMA_BLOCK");

        // Thème océan
        config.addDefault("themes.ocean.floor", "PRISMARINE");
        config.addDefault("themes.ocean.wall", "DARK_PRISMARINE");
        config.addDefault("themes.ocean.decoration", "SEA_LANTERN");

        // Thème désert
        config.addDefault("themes.desert.floor", "SANDSTONE");
        config.addDefault("themes.desert.wall", "SMOOTH_SANDSTONE");
        config.addDefault("themes.desert.decoration", "CHISELED_SANDSTONE");

        // Thème glace
        config.addDefault("themes.ice.floor", "PACKED_ICE");
        config.addDefault("themes.ice.wall", "BLUE_ICE");
        config.addDefault("themes.ice.decoration", "ICE");

        // Thème end
        config.addDefault("themes.end.floor", "END_STONE_BRICKS");
        config.addDefault("themes.end.wall", "END_STONE");
        config.addDefault("themes.end.decoration", "PURPUR_BLOCK");

        // Thème jungle
        config.addDefault("themes.jungle.floor", "MOSSY_STONE_BRICKS");
        config.addDefault("themes.jungle.wall", "JUNGLE_LOG");
        config.addDefault("themes.jungle.decoration", "JUNGLE_LEAVES");

        // Thème steampunk
        config.addDefault("themes.steampunk.floor", "IRON_BLOCK");
        config.addDefault("themes.steampunk.wall", "COPPER_BLOCK");
        config.addDefault("themes.steampunk.decoration", "REDSTONE_LAMP");

        // ================================
        // PARAMÈTRES D'ÉCONOMIE
        // ================================
        config.addDefault("economy.enabled", false);
        config.addDefault("economy.creation-cost", 1000);
        config.addDefault("economy.size-cost-per-block", 10);
        config.addDefault("economy.completion-reward", 500);

        // ================================
        // PARAMÈTRES DE SAUVEGARDE
        // ================================
        config.addDefault("backup.enabled", true);
        config.addDefault("backup.interval", 30);
        config.addDefault("backup.keep-backups", 5);
        config.addDefault("backup.compress", true);

        // ================================
        // NOTIFICATIONS ET MESSAGES
        // ================================
        config.addDefault("notifications.enabled", true);
        config.addDefault("notifications.show-progress", true);
        config.addDefault("notifications.show-completion-stats", true);
        config.addDefault("notifications.broadcast-achievements", true);

        // ================================
        // COMPATIBILITÉ
        // ================================
        config.addDefault("compatibility.worldguard", true);
        config.addDefault("compatibility.plotsquared", false);
        config.addDefault("compatibility.mcmmo", true);
        config.addDefault("compatibility.vault", false);

        // ================================
        // PARAMÈTRES EXPÉRIMENTAUX
        // ================================
        config.addDefault("experimental.advanced-generation", false);
        config.addDefault("experimental.smart-boss-ai", false);
        config.addDefault("experimental.multi-level-dungeons", false);
    }

    // ================================
    // GETTERS POUR LES PARAMÈTRES DE DONJON
    // ================================

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

    public Material getFloorMaterial() {
        String materialName = config.getString("dungeon.floor-material", "STONE_BRICKS");
        return getMaterialSafe(materialName, Material.STONE_BRICKS);
    }

    public Material getWallMaterial() {
        String materialName = config.getString("dungeon.wall-material", "COBBLESTONE");
        return getMaterialSafe(materialName, Material.COBBLESTONE);
    }

    public Material getSpecialRoomMaterial() {
        String materialName = config.getString("dungeon.special-room-material", "POLISHED_ANDESITE");
        return getMaterialSafe(materialName, Material.POLISHED_ANDESITE);
    }

    // ================================
    // GETTERS POUR LES PARAMÈTRES DE LOOT
    // ================================

    public int getDiamondChance() {
        return config.getInt("loot.diamond-chance", 20);
    }

    public int getRareItemChance() {
        return config.getInt("loot.rare-item-chance", 15);
    }

    public int getBossKeyChance() {
        return config.getInt("loot.boss-key-chance", 25);
    }

    public double getLootDifficultyMultiplier() {
        return config.getDouble("loot.difficulty-multiplier", 1.5);
    }

    public int getMinChestsPerDungeon() {
        return config.getInt("loot.min-chests-per-dungeon", 3);
    }

    public int getMaxChestsPerDungeon() {
        return config.getInt("loot.max-chests-per-dungeon", 8);
    }

    // ================================
    // GETTERS POUR LES PARAMÈTRES DE BOSS
    // ================================

    public double getBossHealthMultiplier() {
        return config.getDouble("boss.health-multiplier", 1.5);
    }

    public double getBossExperienceMultiplier() {
        return config.getDouble("boss.experience-multiplier", 2.0);
    }

    public double getBossLootBonusMultiplier() {
        return config.getDouble("boss.loot-bonus-multiplier", 3.0);
    }

    public EntityType getBossTypeForDifficulty(int difficulty) {
        String entityName = config.getString("boss.boss-types." + difficulty, "ZOMBIE");
        try {
            return EntityType.valueOf(entityName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Type de boss invalide pour la difficulté " + difficulty + ": " + entityName);
            return EntityType.ZOMBIE;
        }
    }

    // ================================
    // GETTERS POUR LES PARAMÈTRES DE PIÈGES
    // ================================

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

    // ================================
    // GETTERS POUR LES PARAMÈTRES DE GÉNÉRATION
    // ================================

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

    // ================================
    // GETTERS POUR LES PARAMÈTRES DE PERFORMANCE
    // ================================

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

    // ================================
    // GETTERS POUR LES PARAMÈTRES D'EXPÉRIENCE
    // ================================

    public double getMonsterKillBonus() {
        return config.getDouble("experience.monster-kill-bonus", 1.5);
    }

    public int getCompletionBonus() {
        return config.getInt("experience.completion-bonus", 100);
    }

    public int getDifficultyBonus() {
        return config.getInt("experience.difficulty-bonus", 50);
    }

    // ================================
    // GETTERS POUR LES PARAMÈTRES DE DEBUG
    // ================================

    public boolean isDebugEnabled() {
        if (config == null) {
            plugin.getLogger().warning("Config non initialisée, utilisation des valeurs par défaut");
            return false;
        }
        return config.getBoolean("debug.enabled", false);
    }

    public boolean isShowGenerationInfo() {
        return config.getBoolean("debug.show-generation-info", false);
    }

    public boolean isShowPerformanceStats() {
        return config.getBoolean("debug.show-performance-stats", false);
    }

    // ================================
    // GETTERS POUR LES PARAMÈTRES AVANCÉS
    // ================================

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

    // ================================
    // GETTERS POUR LES PARAMÈTRES DE LIMITES
    // ================================

    public int getMaxDungeonsPerPlayer() {
        return config.getInt("limits.max-dungeons-per-player", 5);
    }

    public int getMaxPlayersPerDungeon() {
        return config.getInt("limits.max-players-per-dungeon", 10);
    }

    public int getCreationCooldown() {
        return config.getInt("limits.creation-cooldown", 60);
    }

    public List<String> getAllowedWorlds() {
        return config.getStringList("limits.allowed-worlds");
    }

    public boolean isWorldAllowed(String worldName) {
        List<String> allowedWorlds = getAllowedWorlds();
        return allowedWorlds.isEmpty() || allowedWorlds.contains(worldName);
    }

    // ================================
    // GETTERS POUR LES PARAMÈTRES DE THÈMES
    // ================================

    public boolean isThemesEnabled() {
        return config.getBoolean("themes.enabled", true);
    }

    public Set<String> getAvailableThemes() {
        ConfigurationSection themesSection = config.getConfigurationSection("themes");
        if (themesSection == null) return new HashSet<>();

        Set<String> themes = new HashSet<>();
        for (String key : themesSection.getKeys(false)) {
            if (!key.equals("enabled")) {
                themes.add(key);
            }
        }
        return themes;
    }

    public Material getThemeMaterial(String theme, String materialType) {
        String path = "themes." + theme + "." + materialType;
        String materialName = config.getString(path, "STONE");
        return getMaterialSafe(materialName, Material.STONE);
    }

    // ================================
    // GETTERS POUR L'ÉCONOMIE
    // ================================

    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled", false);
    }

    public double getCreationCost() {
        return config.getDouble("economy.creation-cost", 1000);
    }

    public double getSizeCostPerBlock() {
        return config.getDouble("economy.size-cost-per-block", 10);
    }

    public double getCompletionReward() {
        return config.getDouble("economy.completion-reward", 500);
    }

    // ================================
    // GETTERS POUR LES SAUVEGARDES
    // ================================

    public boolean isBackupEnabled() {
        return config.getBoolean("backup.enabled", true);
    }

    public int getBackupInterval() {
        return config.getInt("backup.interval", 30);
    }

    public int getKeepBackups() {
        return config.getInt("backup.keep-backups", 5);
    }

    public boolean isCompressBackups() {
        return config.getBoolean("backup.compress", true);
    }

    // ================================
    // GETTERS POUR LES NOTIFICATIONS
    // ================================

    public boolean isNotificationsEnabled() {
        return config.getBoolean("notifications.enabled", true);
    }

    public boolean isShowProgress() {
        return config.getBoolean("notifications.show-progress", true);
    }

    public boolean isShowCompletionStats() {
        return config.getBoolean("notifications.show-completion-stats", true);
    }

    public boolean isBroadcastAchievements() {
        return config.getBoolean("notifications.broadcast-achievements", true);
    }

    // ================================
    // GETTERS POUR LA COMPATIBILITÉ
    // ================================

    public boolean isWorldGuardEnabled() {
        return config.getBoolean("compatibility.worldguard", true);
    }

    public boolean isPlotSquaredEnabled() {
        return config.getBoolean("compatibility.plotsquared", false);
    }

    public boolean isMcMMOEnabled() {
        return config.getBoolean("compatibility.mcmmo", true);
    }

    public boolean isVaultEnabled() {
        return config.getBoolean("compatibility.vault", false);
    }

    // ================================
    // GETTERS POUR LES FONCTIONNALITÉS EXPÉRIMENTALES
    // ================================

    public boolean isAdvancedGeneration() {
        return config.getBoolean("experimental.advanced-generation", false);
    }

    public boolean isSmartBossAI() {
        return config.getBoolean("experimental.smart-boss-ai", false);
    }

    public boolean isMultiLevelDungeons() {
        return config.getBoolean("experimental.multi-level-dungeons", false);
    }

    // ================================
    // MÉTHODES UTILITAIRES
    // ================================

    private Material getMaterialSafe(String materialName, Material defaultMaterial) {
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Matériau invalide dans la config: " + materialName +
                    ". Utilisation de " + defaultMaterial.name());
            return defaultMaterial;
        }
    }

    public Random getRandom() {
        return random;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // ================================
    // MÉTHODES DE VALIDATION
    // ================================

    public boolean validateConfig() {
        boolean valid = true;

        // Validation des tailles
        if (getMinDungeonSize() >= getMaxDungeonSize()) {
            plugin.getLogger().severe("Erreur config: min-size doit être inférieur à max-size");
            valid = false;
        }

        // Validation des salles spéciales
        if (getMinSpecialRooms() > getMaxSpecialRooms()) {
            plugin.getLogger().severe("Erreur config: min special rooms > max special rooms");
            valid = false;
        }

        // Validation des limites
        if (getMaxDungeonsPerPlayer() <= 0) {
            plugin.getLogger().severe("Erreur config: max-dungeons-per-player doit être > 0");
            valid = false;
        }

        return valid;
    }

    // ================================
    // MÉTHODES DE MISE À JOUR
    // ================================

    public void updateConfigValue(String path, Object value) {
        config.set(path, value);
        plugin.saveConfig();
    }

    public void resetToDefaults() {
        plugin.getLogger().info("Réinitialisation de la configuration aux valeurs par défaut...");

        // Sauvegarder l'ancienne config
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();

        // Recharger
        loadConfig();
    }
}