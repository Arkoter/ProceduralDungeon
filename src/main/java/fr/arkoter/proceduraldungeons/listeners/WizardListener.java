package fr.arkoter.proceduraldungeons.listeners;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.managers.DungeonCreationWizard;
import fr.arkoter.proceduraldungeons.models.DungeonTheme;
import fr.arkoter.proceduraldungeons.models.WizardSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class WizardListener implements Listener {

    private final ProceduralDungeons plugin;

    public WizardListener(ProceduralDungeons plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        WizardSession session = plugin.getDungeonCreationWizard().getSession(player.getUniqueId());

        if (session == null) return;

        String title = event.getView().getTitle();
        if (!title.contains("donjon") && !title.contains("Taille") && !title.contains("Thème")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

        // Navigation
        if (itemName.contains("Précédent")) {
            handlePreviousStep(player, session);
            return;
        }

        if (itemName.contains("Suivant")) {
            handleNextStep(player, session);
            return;
        }

        if (itemName.contains("Annuler")) {
            plugin.getDungeonCreationWizard().cancelWizard(player);
            player.closeInventory();
            return;
        }

        // Actions spécifiques selon l'étape
        switch (session.getCurrentStep()) {
            case SIZE:
                handleSizeSelection(player, session, itemName);
                break;

            case THEME:
                handleThemeSelection(player, session, event.getSlot());
                break;

            case DIFFICULTY:
                handleDifficultySelection(player, session, itemName);
                break;

            case ROOMS:
                handleRoomSelection(player, session, itemName, event.isRightClick());
                break;

            case MONSTERS:
                handleMonsterSelection(player, session, event.getSlot());
                break;

            case TRAPS:
                handleTrapSelection(player, session, itemName, event.isRightClick());
                break;

            case REWARDS:
                handleRewardSelection(player, session, event.getSlot(), event.isRightClick());
                break;

            case CONFIRM:
                handleConfirmation(player, session, itemName);
                break;
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        WizardSession session = plugin.getDungeonCreationWizard().getSession(player.getUniqueId());

        if (session == null) return;

        // Empêcher le message d'apparaître dans le chat public
        event.setCancelled(true);

        // Traiter l'input dans le thread principal
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getDungeonCreationWizard().handleChatInput(player, event.getMessage());
        });
    }

    private void handleSizeSelection(Player player, WizardSession session, String itemName) {
        if (itemName.contains("Petit")) {
            session.setSize(30);
        } else if (itemName.contains("Moyen")) {
            session.setSize(50);
        } else if (itemName.contains("Grand")) {
            session.setSize(80);
        }

        plugin.getDungeonCreationWizard().showStep(player, DungeonCreationWizard.WizardStep.THEME);
    }

    private void handleThemeSelection(Player player, WizardSession session, int slot) {
        DungeonTheme[] themes = DungeonTheme.values();
        int themeIndex = (slot - 10) / 2;

        if (themeIndex >= 0 && themeIndex < themes.length) {
            session.setTheme(themes[themeIndex]);
            plugin.getDungeonCreationWizard().showStep(player, DungeonCreationWizard.WizardStep.DIFFICULTY);
        }
    }

    private void handleDifficultySelection(Player player, WizardSession session, String itemName) {
        for (int i = 1; i <= 5; i++) {
            if (itemName.contains("Difficulté " + i)) {
                session.setDifficulty(i);
                plugin.getDungeonCreationWizard().showStep(player, DungeonCreationWizard.WizardStep.ROOMS);
                break;
            }
        }
    }

    private void handleRoomSelection(Player player, WizardSession session, String itemName, boolean rightClick) {
        int change = rightClick ? 1 : -1;

        if (itemName.contains("Trésor")) {
            session.setTreasureRooms(session.getTreasureRooms() + change);
        } else if (itemName.contains("Combat")) {
            session.setCombatRooms(session.getCombatRooms() + change);
        } else if (itemName.contains("Puzzle")) {
            session.setPuzzleRooms(session.getPuzzleRooms() + change);
        } else if (itemName.contains("Boss")) {
            session.setBossRoom(!session.hasBossRoom());
        }

        // Rafraîchir l'affichage
        plugin.getDungeonCreationWizard().showStep(player, DungeonCreationWizard.WizardStep.ROOMS);
    }

    private void handleMonsterSelection(Player player, WizardSession session, int slot) {
        String[] monsters = {"ZOMBIE", "SKELETON", "SPIDER", "CREEPER", "WITCH", "VINDICATOR"};
        int monsterIndex = (slot - 10) / 2;

        if (monsterIndex >= 0 && monsterIndex < monsters.length) {
            session.toggleMonster(monsters[monsterIndex]);
            plugin.getDungeonCreationWizard().showStep(player, DungeonCreationWizard.WizardStep.MONSTERS);
        }
    }

    private void handleTrapSelection(Player player, WizardSession session, String itemName, boolean rightClick) {
        if (itemName.contains("Densité")) {
            int change = rightClick ? 1 : -1;
            session.setTrapDensity(session.getTrapDensity() + change);
        } else {
            // Toggle individual traps
            for (int i = 0; i < 5; i++) {
                String[] trapNames = {"Pression", "TNT", "Poison", "Téléportation", "Flèches"};
                if (itemName.contains(trapNames[i])) {
                    session.toggleTrap(i);
                    break;
                }
            }
        }

        plugin.getDungeonCreationWizard().showStep(player, DungeonCreationWizard.WizardStep.TRAPS);
    }

    private void handleRewardSelection(Player player, WizardSession session, int slot, boolean rightClick) {
        if (slot == 20) { // Quality selector
            int change = rightClick ? 1 : -1;
            session.setLootQuality(session.getLootQuality() + change);
        } else if (slot >= 10 && slot <= 14) {
            // Toggle reward types
            session.toggleReward(slot - 10);
        }

        plugin.getDungeonCreationWizard().showStep(player, DungeonCreationWizard.WizardStep.REWARDS);
    }

    private void handleConfirmation(Player player, WizardSession session, String itemName) {
        if (itemName.contains("CRÉER")) {
            plugin.getDungeonCreationWizard().showStep(player, DungeonCreationWizard.WizardStep.CONFIRM);
            player.closeInventory();
            // La création sera gérée par handleChatInput avec "confirm"
            plugin.getDungeonCreationWizard().handleChatInput(player, "confirm");
        } else if (itemName.contains("ANNULER")) {
            plugin.getDungeonCreationWizard().cancelWizard(player);
            player.closeInventory();
        }
    }

    private void handlePreviousStep(Player player, WizardSession session) {
        DungeonCreationWizard.WizardStep current = session.getCurrentStep();
        DungeonCreationWizard.WizardStep previous = getPreviousStep(current);

        if (previous != null) {
            plugin.getDungeonCreationWizard().showStep(player, previous);
        }
    }

    private void handleNextStep(Player player, WizardSession session) {
        DungeonCreationWizard.WizardStep current = session.getCurrentStep();
        DungeonCreationWizard.WizardStep next = getNextStep(current);

        if (next != null) {
            plugin.getDungeonCreationWizard().showStep(player, next);
        }
    }

    private DungeonCreationWizard.WizardStep getPreviousStep(DungeonCreationWizard.WizardStep current) {
        switch (current) {
            case SIZE: return DungeonCreationWizard.WizardStep.NAME;
            case THEME: return DungeonCreationWizard.WizardStep.SIZE;
            case DIFFICULTY: return DungeonCreationWizard.WizardStep.THEME;
            case ROOMS: return DungeonCreationWizard.WizardStep.DIFFICULTY;
            case MONSTERS: return DungeonCreationWizard.WizardStep.ROOMS;
            case TRAPS: return DungeonCreationWizard.WizardStep.MONSTERS;
            case REWARDS: return DungeonCreationWizard.WizardStep.TRAPS;
            case PREVIEW: return DungeonCreationWizard.WizardStep.REWARDS;
            case CONFIRM: return DungeonCreationWizard.WizardStep.PREVIEW;
            default: return null;
        }
    }

    private DungeonCreationWizard.WizardStep getNextStep(DungeonCreationWizard.WizardStep current) {
        switch (current) {
            case NAME: return DungeonCreationWizard.WizardStep.SIZE;
            case SIZE: return DungeonCreationWizard.WizardStep.THEME;
            case THEME: return DungeonCreationWizard.WizardStep.DIFFICULTY;
            case DIFFICULTY: return DungeonCreationWizard.WizardStep.ROOMS;
            case ROOMS: return DungeonCreationWizard.WizardStep.MONSTERS;
            case MONSTERS: return DungeonCreationWizard.WizardStep.TRAPS;
            case TRAPS: return DungeonCreationWizard.WizardStep.REWARDS;
            case REWARDS: return DungeonCreationWizard.WizardStep.PREVIEW;
            case PREVIEW: return DungeonCreationWizard.WizardStep.CONFIRM;
            default: return null;
        }
    }
}