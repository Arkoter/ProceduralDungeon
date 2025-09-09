package fr.arkoter.proceduraldungeons.models;

import fr.arkoter.proceduraldungeons.managers.DungeonCreationWizard.WizardStep;

import java.util.*;

public class WizardSession {

    private final UUID playerId;
    private WizardStep currentStep;
    private String name;
    private int size = 50;
    private DungeonTheme theme = DungeonTheme.MEDIEVAL;
    private int difficulty = 1;
    private int treasureRooms = 3;
    private int combatRooms = 2;
    private int puzzleRooms = 1;
    private boolean bossRoom = true;
    private int trapDensity = 2;
    private int lootQuality = 2;
    private final Set<String> enabledMonsters;
    private final Set<Integer> enabledTraps;
    private final Set<Integer> enabledRewards;
    private final Map<String, Object> customValues;

    public WizardSession(UUID playerId) {
        this.playerId = playerId;
        this.currentStep = WizardStep.NAME;
        this.enabledMonsters = new HashSet<>(Arrays.asList("ZOMBIE", "SKELETON", "SPIDER"));
        this.enabledTraps = new HashSet<>(Arrays.asList(0, 1, 2)); // Pression, TNT, Poison
        this.enabledRewards = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4)); // Tous activés
        this.customValues = new HashMap<>();
    }

    // Getters et Setters
    public UUID getPlayerId() { return playerId; }

    public WizardStep getCurrentStep() { return currentStep; }
    public void setCurrentStep(WizardStep currentStep) { this.currentStep = currentStep; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public DungeonTheme getTheme() { return theme; }
    public void setTheme(DungeonTheme theme) { this.theme = theme; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public int getTreasureRooms() { return treasureRooms; }
    public void setTreasureRooms(int treasureRooms) { this.treasureRooms = Math.max(0, treasureRooms); }

    public int getCombatRooms() { return combatRooms; }
    public void setCombatRooms(int combatRooms) { this.combatRooms = Math.max(0, combatRooms); }

    public int getPuzzleRooms() { return puzzleRooms; }
    public void setPuzzleRooms(int puzzleRooms) { this.puzzleRooms = Math.max(0, puzzleRooms); }

    public boolean hasBossRoom() { return bossRoom; }
    public void setBossRoom(boolean bossRoom) { this.bossRoom = bossRoom; }

    public int getTrapDensity() { return trapDensity; }
    public void setTrapDensity(int trapDensity) { this.trapDensity = Math.max(1, Math.min(4, trapDensity)); }

    public int getLootQuality() { return lootQuality; }
    public void setLootQuality(int lootQuality) { this.lootQuality = Math.max(1, Math.min(5, lootQuality)); }

    // Gestion des monstres
    public Set<String> getEnabledMonsters() { return new HashSet<>(enabledMonsters); }
    public boolean isMonsterEnabled(String monster) { return enabledMonsters.contains(monster); }
    public void toggleMonster(String monster) {
        if (enabledMonsters.contains(monster)) {
            enabledMonsters.remove(monster);
        } else {
            enabledMonsters.add(monster);
        }
    }

    // Gestion des pièges
    public Set<Integer> getEnabledTraps() { return new HashSet<>(enabledTraps); }
    public boolean isTrapEnabled(int trapIndex) { return enabledTraps.contains(trapIndex); }
    public void toggleTrap(int trapIndex) {
        if (enabledTraps.contains(trapIndex)) {
            enabledTraps.remove(trapIndex);
        } else {
            enabledTraps.add(trapIndex);
        }
    }

    // Gestion des récompenses
    public Set<Integer> getEnabledRewards() { return new HashSet<>(enabledRewards); }
    public boolean isRewardEnabled(int rewardIndex) { return enabledRewards.contains(rewardIndex); }
    public void toggleReward(int rewardIndex) {
        if (enabledRewards.contains(rewardIndex)) {
            enabledRewards.remove(rewardIndex);
        } else {
            enabledRewards.add(rewardIndex);
        }
    }

    // Valeurs personnalisées
    public int getValue(String key, int defaultValue) {
        return (Integer) customValues.getOrDefault(key, defaultValue);
    }

    public void setValue(String key, Object value) {
        customValues.put(key, value);
    }

    // Validation
    public boolean isValid() {
        return name != null && !name.isEmpty() &&
                size >= 20 && size <= 100 &&
                difficulty >= 1 && difficulty <= 5 &&
                !enabledMonsters.isEmpty();
    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();

        if (name == null || name.isEmpty()) {
            errors.add("Le nom du donjon est requis");
        }
        if (size < 20 || size > 100) {
            errors.add("La taille doit être entre 20 et 100");
        }
        if (difficulty < 1 || difficulty > 5) {
            errors.add("La difficulté doit être entre 1 et 5");
        }
        if (enabledMonsters.isEmpty()) {
            errors.add("Au moins un type de monstre doit être activé");
        }

        return errors;
    }
}