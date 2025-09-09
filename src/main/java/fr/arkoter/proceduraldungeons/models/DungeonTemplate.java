package fr.arkoter.proceduraldungeons.models;

import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Set;

public class DungeonTemplate {

    private final String name;
    private final int size;
    private final DungeonTheme theme;
    private final int difficulty;
    private final int treasureRooms;
    private final int combatRooms;
    private final int puzzleRooms;
    private final boolean bossRoom;
    private final int trapDensity;
    private final int lootQuality;
    private final Set<String> enabledMonsters;
    private final Set<Integer> enabledTraps;
    private final Set<Integer> enabledRewards;

    public DungeonTemplate(WizardSession session) {
        this.name = session.getName();
        this.size = session.getSize();
        this.theme = session.getTheme();
        this.difficulty = session.getDifficulty();
        this.treasureRooms = session.getTreasureRooms();
        this.combatRooms = session.getCombatRooms();
        this.puzzleRooms = session.getPuzzleRooms();
        this.bossRoom = session.hasBossRoom();
        this.trapDensity = session.getTrapDensity();
        this.lootQuality = session.getLootQuality();
        this.enabledMonsters = session.getEnabledMonsters();
        this.enabledTraps = session.getEnabledTraps();
        this.enabledRewards = session.getEnabledRewards();
    }

    // Getters
    public String getName() { return name; }
    public int getSize() { return size; }
    public DungeonTheme getTheme() { return theme; }
    public int getDifficulty() { return difficulty; }
    public int getTreasureRooms() { return treasureRooms; }
    public int getCombatRooms() { return combatRooms; }
    public int getPuzzleRooms() { return puzzleRooms; }
    public boolean hasBossRoom() { return bossRoom; }
    public int getTrapDensity() { return trapDensity; }
    public int getLootQuality() { return lootQuality; }
    public Set<String> getEnabledMonsters() { return enabledMonsters; }
    public Set<Integer> getEnabledTraps() { return enabledTraps; }
    public Set<Integer> getEnabledRewards() { return enabledRewards; }

    // Méthodes utilitaires
    public EntityType[] getMonsterTypes() {
        return enabledMonsters.stream()
                .map(EntityType::valueOf)
                .toArray(EntityType[]::new);
    }

    public int getTotalSpecialRooms() {
        return treasureRooms + combatRooms + puzzleRooms + (bossRoom ? 1 : 0);
    }

    public double getTrapDensityMultiplier() {
        switch (trapDensity) {
            case 1: return 0.5;
            case 2: return 1.0;
            case 3: return 1.5;
            case 4: return 2.0;
            default: return 1.0;
        }
    }

    public double getLootQualityMultiplier() {
        switch (lootQuality) {
            case 1: return 0.5;
            case 2: return 1.0;
            case 3: return 1.5;
            case 4: return 2.0;
            case 5: return 3.0;
            default: return 1.0;
        }
    }
}