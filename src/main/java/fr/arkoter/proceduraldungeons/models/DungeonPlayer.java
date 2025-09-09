package fr.arkoter.proceduraldungeons.models;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DungeonPlayer {

    private final UUID playerId;
    private String currentDungeon;
    private Location exitLocation;
    private long enterTime;
    private int monstersKilled;
    private int treasuresFound;
    private boolean hasBossKey;
    private long completionTime;

    public DungeonPlayer(UUID playerId) {
        this.playerId = playerId;
        this.currentDungeon = null;
        this.exitLocation = null;
        this.enterTime = 0;
        this.monstersKilled = 0;
        this.treasuresFound = 0;
        this.hasBossKey = false;
        this.completionTime = 0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getCurrentDungeon() {
        return currentDungeon;
    }

    public void setCurrentDungeon(String currentDungeon) {
        this.currentDungeon = currentDungeon;
    }

    public Location getExitLocation() {
        return exitLocation != null ? exitLocation.clone() : null;
    }

    public void setExitLocation(Location exitLocation) {
        this.exitLocation = exitLocation != null ? exitLocation.clone() : null;
    }

    public long getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(long enterTime) {
        this.enterTime = enterTime;
    }

    public int getMonstersKilled() {
        return monstersKilled;
    }

    public void setMonstersKilled(int monstersKilled) {
        this.monstersKilled = monstersKilled;
    }

    public void incrementMonstersKilled() {
        this.monstersKilled++;
    }

    public int getTreasuresFound() {
        return treasuresFound;
    }

    public void setTreasuresFound(int treasuresFound) {
        this.treasuresFound = treasuresFound;
    }

    public void incrementTreasuresFound() {
        this.treasuresFound++;
    }

    public boolean hasBossKey() {
        return hasBossKey;
    }

    public void setBossKey(boolean hasBossKey) {
        this.hasBossKey = hasBossKey;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public boolean isInDungeon() {
        return currentDungeon != null;
    }

    public void reset() {
        this.currentDungeon = null;
        this.exitLocation = null;
        this.enterTime = 0;
        this.monstersKilled = 0;
        this.treasuresFound = 0;
        this.hasBossKey = false;
        this.completionTime = 0;
    }

    public long getTimeInDungeon() {
        if (enterTime == 0) return 0;
        return System.currentTimeMillis() - enterTime;
    }

    public String getFormattedTimeInDungeon() {
        long timeMs = getTimeInDungeon();
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    public String getFormattedCompletionTime() {
        if (completionTime == 0) return "Non terminé";

        long seconds = completionTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DungeonPlayer that = (DungeonPlayer) obj;
        return playerId.equals(that.playerId);
    }

    @Override
    public int hashCode() {
        return playerId.hashCode();
    }

    @Override
    public String toString() {
        return "DungeonPlayer{" +
                "playerId=" + playerId +
                ", currentDungeon='" + currentDungeon + '\'' +
                ", timeInDungeon=" + getFormattedTimeInDungeon() +
                ", monstersKilled=" + monstersKilled +
                ", treasuresFound=" + treasuresFound +
                ", hasBossKey=" + hasBossKey +
                '}';
    }
}