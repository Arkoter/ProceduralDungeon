package fr.arkoter.proceduraldungeons.data;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.models.DungeonPlayer;
import fr.arkoter.proceduraldungeons.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    private final ProceduralDungeons plugin;
    private final File dataFile;
    private YamlConfiguration config;

    public PlayerData(ProceduralDungeons plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        loadData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossible de créer le fichier players.yml: " + e.getMessage());
            }
        }

        config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder les données des joueurs: " + e.getMessage());
        }
    }

    public void savePlayer(DungeonPlayer player) {
        String path = "players." + player.getPlayerId().toString();

        // Données actuelles
        config.set(path + ".current-dungeon", player.getCurrentDungeon());

        if (player.getExitLocation() != null) {
            config.set(path + ".exit-location", LocationUtils.locationToString(player.getExitLocation()));
        } else {
            config.set(path + ".exit-location", null);
        }

        config.set(path + ".enter-time", player.getEnterTime());
        config.set(path + ".monsters-killed", player.getMonstersKilled());
        config.set(path + ".treasures-found", player.getTreasuresFound());
        config.set(path + ".has-boss-key", player.hasBossKey());

        // Statistiques globales
        config.set(path + ".stats.total-dungeons-entered",
                config.getInt(path + ".stats.total-dungeons-entered", 0) +
                        (player.isInDungeon() ? 1 : 0));
        config.set(path + ".stats.total-dungeons-completed",
                config.getInt(path + ".stats.total-dungeons-completed", 0));
        config.set(path + ".stats.total-monsters-killed",
                config.getInt(path + ".stats.total-monsters-killed", 0) + player.getMonstersKilled());
        config.set(path + ".stats.total-treasures-found",
                config.getInt(path + ".stats.total-treasures-found", 0) + player.getTreasuresFound());
        config.set(path + ".stats.total-time-in-dungeons",
                config.getLong(path + ".stats.total-time-in-dungeons", 0) + player.getTimeInDungeon());
        config.set(path + ".stats.last-activity", System.currentTimeMillis());

        saveData();
    }

    public DungeonPlayer loadPlayer(UUID playerId) {
        String path = "players." + playerId.toString();
        ConfigurationSection section = config.getConfigurationSection(path);

        DungeonPlayer player = new DungeonPlayer(playerId);

        if (section == null) {
            return player; // Nouveau joueur
        }

        // Charger les données actuelles
        player.setCurrentDungeon(section.getString("current-dungeon"));

        String exitLocationStr = section.getString("exit-location");
        if (exitLocationStr != null) {
            Location exitLoc = LocationUtils.stringToLocation(exitLocationStr);
            player.setExitLocation(exitLoc);
        }

        player.setEnterTime(section.getLong("enter-time", 0));
        player.setMonstersKilled(section.getInt("monsters-killed", 0));
        player.setTreasuresFound(section.getInt("treasures-found", 0));
        player.setBossKey(section.getBoolean("has-boss-key", false));

        return player;
    }

    public void deletePlayer(UUID playerId) {
        config.set("players." + playerId.toString(), null);
        saveData();
    }

    public Map<UUID, DungeonPlayer> loadAllPlayers() {
        Map<UUID, DungeonPlayer> players = new HashMap<>();
        ConfigurationSection playersSection = config.getConfigurationSection("players");

        if (playersSection == null) {
            return players;
        }

        for (String playerIdStr : playersSection.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(playerIdStr);
                DungeonPlayer player = loadPlayer(playerId);
                players.put(playerId, player);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("UUID invalide dans players.yml: " + playerIdStr);
            }
        }

        return players;
    }

    public PlayerStats getPlayerStats(UUID playerId) {
        String path = "players." + playerId.toString() + ".stats";
        ConfigurationSection statsSection = config.getConfigurationSection(path);

        if (statsSection == null) {
            return new PlayerStats();
        }

        PlayerStats stats = new PlayerStats();
        stats.totalDungeonsEntered = statsSection.getInt("total-dungeons-entered", 0);
        stats.totalDungeonsCompleted = statsSection.getInt("total-dungeons-completed", 0);
        stats.totalMonstersKilled = statsSection.getInt("total-monsters-killed", 0);
        stats.totalTreasuresFound = statsSection.getInt("total-treasures-found", 0);
        stats.totalTimeInDungeons = statsSection.getLong("total-time-in-dungeons", 0);
        stats.lastActivity = statsSection.getLong("last-activity", 0);

        return stats;
    }

    public void incrementDungeonsCompleted(UUID playerId) {
        String path = "players." + playerId.toString() + ".stats.total-dungeons-completed";
        int current = config.getInt(path, 0);
        config.set(path, current + 1);
        saveData();
    }

    public void addTimeInDungeons(UUID playerId, long additionalTime) {
        String path = "players." + playerId.toString() + ".stats.total-time-in-dungeons";
        long current = config.getLong(path, 0);
        config.set(path, current + additionalTime);
        saveData();
    }

    public void cleanupInactivePlayers(long inactivityThreshold) {
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) return;

        long currentTime = System.currentTimeMillis();

        for (String playerIdStr : playersSection.getKeys(false)) {
            long lastActivity = config.getLong("players." + playerIdStr + ".stats.last-activity", 0);

            if (currentTime - lastActivity > inactivityThreshold) {
                config.set("players." + playerIdStr, null);
                plugin.getLogger().info("Données de joueur inactif supprimées: " + playerIdStr);
            }
        }

        saveData();
    }

    public static class PlayerStats {
        public int totalDungeonsEntered = 0;
        public int totalDungeonsCompleted = 0;
        public int totalMonstersKilled = 0;
        public int totalTreasuresFound = 0;
        public long totalTimeInDungeons = 0;
        public long lastActivity = 0;

        public double getCompletionRate() {
            if (totalDungeonsEntered == 0) return 0.0;
            return (double) totalDungeonsCompleted / totalDungeonsEntered * 100.0;
        }

        public long getAverageTimePerDungeon() {
            if (totalDungeonsCompleted == 0) return 0;
            return totalTimeInDungeons / totalDungeonsCompleted;
        }
    }
}