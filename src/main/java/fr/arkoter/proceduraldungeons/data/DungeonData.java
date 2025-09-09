package fr.arkoter.proceduraldungeons.data;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.models.Dungeon;
import fr.arkoter.proceduraldungeons.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DungeonData {

    private final ProceduralDungeons plugin;
    private final File dataFile;
    private YamlConfiguration config;

    public DungeonData(ProceduralDungeons plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "dungeons.yml");
        loadData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            plugin.saveResource("dungeons.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder les données des donjons: " + e.getMessage());
        }
    }

    public void saveDungeon(Dungeon dungeon) {
        String path = "dungeons." + dungeon.getName();

        // Informations de base
        config.set(path + ".location", LocationUtils.locationToString(dungeon.getLocation()));
        config.set(path + ".size", dungeon.getSize());
        config.set(path + ".difficulty", dungeon.getDifficulty());
        config.set(path + ".created-at", System.currentTimeMillis());
        config.set(path + ".boss-alive", dungeon.isBossAlive());

        // Position du boss
        if (dungeon.getBossLocation() != null) {
            config.set(path + ".boss-location", LocationUtils.locationToString(dungeon.getBossLocation()));
        }

        // Coffres au trésor
        List<String> chestLocations = new ArrayList<>();
        for (Location chestLoc : dungeon.getTreasureChests()) {
            chestLocations.add(LocationUtils.locationToString(chestLoc));
        }
        config.set(path + ".treasure-chests", chestLocations);

        // Pièges
        List<Map<String, Object>> trapData = new ArrayList<>();
        for (Dungeon.Trap trap : dungeon.getTraps()) {
            Map<String, Object> trapMap = new HashMap<>();
            trapMap.put("location", LocationUtils.locationToString(trap.getLocation()));
            trapMap.put("type", trap.getType());
            trapMap.put("activated", trap.isActivated());
            trapData.add(trapMap);
        }
        config.set(path + ".traps", trapData);

        // Statistiques
        config.set(path + ".stats.times-entered", dungeon.getTimesEntered());
        config.set(path + ".stats.times-completed", dungeon.getTimesCompleted());
        config.set(path + ".stats.total-monsters-killed", dungeon.getTotalMonstersKilled());
        config.set(path + ".stats.fastest-completion", dungeon.getFastestCompletion());

        // Joueurs actifs
        List<String> activePlayers = new ArrayList<>();
        for (String playerId : dungeon.getActivePlayers()) {
            activePlayers.add(playerId);
        }
        config.set(path + ".active-players", activePlayers);

        saveData();
    }

    public Dungeon loadDungeon(String name) {
        String path = "dungeons." + name;
        ConfigurationSection section = config.getConfigurationSection(path);

        if (section == null) {
            return null;
        }

        // Charger les informations de base
        Location location = LocationUtils.stringToLocation(section.getString("location"));
        if (location == null) {
            plugin.getLogger().warning("Location invalide pour le donjon: " + name);
            return null;
        }

        int size = section.getInt("size", 50);
        int difficulty = section.getInt("difficulty", 1);

        Dungeon dungeon = new Dungeon(name, location, size, difficulty);

        // Charger les autres propriétés
        dungeon.setBossAlive(section.getBoolean("boss-alive", false));

        String bossLocationStr = section.getString("boss-location");
        if (bossLocationStr != null) {
            Location bossLoc = LocationUtils.stringToLocation(bossLocationStr);
            if (bossLoc != null) {
                dungeon.setBossLocation(bossLoc);
            }
        }

        // Charger les coffres
        List<String> chestLocations = section.getStringList("treasure-chests");
        for (String chestLocStr : chestLocations) {
            Location chestLoc = LocationUtils.stringToLocation(chestLocStr);
            if (chestLoc != null) {
                dungeon.addTreasureChest(chestLoc);
            }
        }

        // Charger les pièges
        List<Map<?, ?>> trapData = section.getMapList("traps");
        for (Map<?, ?> trapMap : trapData) {
            String trapLocStr = (String) trapMap.get("location");
            int trapType = (Integer) trapMap.get("type");
            boolean activated = (Boolean) trapMap.getOrDefault("activated", false);

            Location trapLoc = LocationUtils.stringToLocation(trapLocStr);
            if (trapLoc != null) {
                Dungeon.Trap trap = new Dungeon.Trap(trapLoc, trapType);
                trap.setActivated(activated);
                dungeon.addTrap(trap);
            }
        }

        // Charger les statistiques
        ConfigurationSection statsSection = section.getConfigurationSection("stats");
        if (statsSection != null) {
            dungeon.setTimesEntered(statsSection.getInt("times-entered", 0));
            dungeon.setTimesCompleted(statsSection.getInt("times-completed", 0));
            dungeon.setTotalMonstersKilled(statsSection.getInt("total-monsters-killed", 0));
            dungeon.setFastestCompletion(statsSection.getLong("fastest-completion", 0));
        }

        // Charger les joueurs actifs
        List<String> activePlayers = section.getStringList("active-players");
        for (String playerId : activePlayers) {
            dungeon.addActivePlayer(playerId);
        }

        return dungeon;
    }

    public void deleteDungeon(String name) {
        config.set("dungeons." + name, null);
        saveData();
    }

    public List<String> getAllDungeonNames() {
        ConfigurationSection dungeonsSection = config.getConfigurationSection("dungeons");
        if (dungeonsSection == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(dungeonsSection.getKeys(false));
    }

    public Map<String, Dungeon> loadAllDungeons() {
        Map<String, Dungeon> dungeons = new HashMap<>();

        for (String dungeonName : getAllDungeonNames()) {
            Dungeon dungeon = loadDungeon(dungeonName);
            if (dungeon != null) {
                dungeons.put(dungeonName, dungeon);
            }
        }

        return dungeons;
    }

    public boolean dungeonExists(String name) {
        return config.contains("dungeons." + name);
    }

    public void saveAllDungeons(Map<String, Dungeon> dungeons) {
        for (Dungeon dungeon : dungeons.values()) {
            saveDungeon(dungeon);
        }
    }

    public void cleanupOldDungeons(long maxAge) {
        ConfigurationSection dungeonsSection = config.getConfigurationSection("dungeons");
        if (dungeonsSection == null) return;

        long currentTime = System.currentTimeMillis();
        List<String> dungeonsToDelete = new ArrayList<>();

        for (String dungeonName : dungeonsSection.getKeys(false)) {
            long createdAt = config.getLong("dungeons." + dungeonName + ".created-at", 0);

            if (currentTime - createdAt > maxAge) {
                dungeonsToDelete.add(dungeonName);
            }
        }

        for (String dungeonName : dungeonsToDelete) {
            deleteDungeon(dungeonName);
            plugin.getLogger().info("Donjon ancien supprimé: " + dungeonName);
        }
    }
}