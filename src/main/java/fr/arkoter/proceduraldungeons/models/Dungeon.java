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

    public void addActivePlayer(String playerId) {
        activePlayers.add(playerId);
    }

    public void removeActivePlayer(String playerId) {
        activePlayers.remove(playerId);
    }

    public boolean hasActivePlayer(String playerId) {
        return activePlayers.contains(playerId);
    }

    public int getActivePlayerCount() {
        return activePlayers.size();
    }

    // Statistiques

    public int getTimesEntered() {
        return timesEntered;
    }

    public void setTimesEntered(int timesEntered) {
        this.timesEntered = timesEntered;
    }

    public void incrementTimesEntered() {
        this.timesEntered++;
    }

    public int getTimesCompleted() {
        return timesCompleted;
    }

    public void setTimesCompleted(int timesCompleted) {
        this.timesCompleted = timesCompleted;
    }

    public void incrementTimesCompleted() {
        this.timesCompleted++;
    }

    public int getTotalMonstersKilled() {
        return totalMonstersKilled;
    }

    public void setTotalMonstersKilled(int totalMonstersKilled) {
        this.totalMonstersKilled = totalMonstersKilled;
    }

    public void incrementTotalMonstersKilled() {
        this.totalMonstersKilled++;
    }

    public long getFastestCompletion() {
        return fastestCompletion;
    }

    public void setFastestCompletion(long fastestCompletion) {
        this.fastestCompletion = fastestCompletion;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // Méthodes utilitaires

    public double getCompletionRate() {
        if (timesEntered == 0) return 0.0;
        return (double) timesCompleted / timesEntered * 100.0;
    }

    public boolean isEmpty() {
        return activePlayers.isEmpty();
    }

    public String getFormattedCreationDate() {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(createdAt));
    }

    public String getFormattedFastestCompletion() {
        if (fastestCompletion == 0) return "Aucun";

        long seconds = fastestCompletion / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    // Classe interne Trap

    public static class Trap {
        private final Location location;
        private final int type;
        private boolean activated;

        public Trap(Location location, int type) {
            this.location = location.clone();
            this.type = type;
            this.activated = false;
        }

        public Location getLocation() {
            return location.clone();
        }

        public int getType() {
            return type;
        }

        public boolean isActivated() {
            return activated;
        }

        public void setActivated(boolean activated) {
            this.activated = activated;
        }

        public String getTypeName() {
            switch (type) {
                case 0:
                    return "Pression";
                case 1:
                    return "TNT";
                case 2:
                    return "Fosse de lave";
                case 3:
                    return "Flèches";
                case 4:
                    return "Poison";
                case 5:
                    return "Téléportation";
                default:
                    return "Inconnu";
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            Trap trap = (Trap) obj;
            return type == trap.type && location.equals(trap.location);
        }

        @Override
        public int hashCode() {
            return location.hashCode() * 31 + type;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Dungeon dungeon = (Dungeon) obj;
        return name.equals(dungeon.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Dungeon{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", difficulty=" + difficulty +
                ", timesEntered=" + timesEntered +
                ", timesCompleted=" + timesCompleted +
                ", activePlayers=" + activePlayers.size() +
                '}';
    }
}