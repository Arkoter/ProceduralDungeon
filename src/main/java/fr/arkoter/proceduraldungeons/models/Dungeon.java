package fr.arkoter.proceduraldungeons.models;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dungeon {

    private final String name;
    private final Location location;
    private final int size;
    private int difficulty;
    private boolean bossAlive;
    private Location bossLocation;
    private final List<Location> treasureChests;
    private final List<Trap> traps;
    private final Set<String> activePlayers;

    // Statistiques
    private int timesEntered;
    private int timesCompleted;
    private int totalMonstersKilled;
    private long fastestCompletion;
    private final long createdAt;

    public Dungeon(String name, Location location, int size, int difficulty) {
        this.name = name;
        this.location = location.clone();
        this.size = size;
        this.difficulty = difficulty;
        this.bossAlive = false;
        this.treasureChests = new ArrayList<>();
        this.traps = new ArrayList<>();
        this.activePlayers = new HashSet<>();
        this.timesEntered = 0;
        this.timesCompleted = 0;
        this.totalMonstersKilled = 0;
        this.fastestCompletion = 0;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters et Setters de base

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location.clone();
    }

    public int getSize() {
        return size;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(difficulty, 10));
    }

    public boolean isBossAlive() {
        return bossAlive;
    }

    public void setBossAlive(boolean bossAlive) {
        this.bossAlive = bossAlive;
    }

    public Location getBossLocation() {
        return bossLocation != null ? bossLocation.clone() : null;
    }

    public void setBossLocation(Location bossLocation) {
        this.bossLocation = bossLocation != null ? bossLocation.clone() : null;
    }

    // Gestion des coffres au trésor

    public List<Location> getTreasureChests() {
        return new ArrayList<>(treasureChests);
    }

    public void addTreasureChest(Location location) {
        treasureChests.add(location.clone());
    }

    public void removeTreasureChest(Location location) {
        treasureChests.removeIf(chest -> chest.equals(location));
    }

    public boolean hasTreasureChest(Location location) {
        return treasureChests.contains(location);
    }

    // Gestion des pièges

    public List<Trap> getTraps() {
        return new ArrayList<>(traps);
    }

    public void addTrap(Location location, int type) {
        traps.add(new Trap(location, type));
    }

    public void addTrap(Trap trap) {
        traps.add(trap);
    }

    public void removeTrap(Location location) {
        traps.removeIf(trap -> trap.getLocation().equals(location));
    }

    public Trap getTrap(Location location) {
        return traps.stream()
                .filter(trap -> trap.getLocation().equals(location))
                .findFirst()
                .orElse(null);
    }

    // Gestion des joueurs actifs

    public Set<String> getActivePlayers() {
        return new HashSet<>(activePlayers);
    }