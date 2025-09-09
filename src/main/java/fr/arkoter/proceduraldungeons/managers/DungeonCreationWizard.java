package fr.arkoter.proceduraldungeons.managers;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.models.WizardSession;
import fr.arkoter.proceduraldungeons.models.DungeonTemplate;
import fr.arkoter.proceduraldungeons.models.DungeonTheme;
import fr.arkoter.proceduraldungeons.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DungeonCreationWizard {

    private final ProceduralDungeons plugin;
    private final Map<UUID, WizardSession> activeSessions;

    public DungeonCreationWizard(ProceduralDungeons plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
    }

    public void startWizard(Player player) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage("§cVous avez déjà un assistant actif !");
            return;
        }

        WizardSession session = new WizardSession(player.getUniqueId());
        activeSessions.put(player.getUniqueId(), session);

        player.sendMessage("§eBienvenue dans l'assistant de création de donjon !");
        showStep(player, WizardStep.NAME);
    }

    public void showStep(Player player, WizardStep step) {
        WizardSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        session.setCurrentStep(step);

        switch (step) {
            case NAME:
                showNameSelection(player);
                break;
            case SIZE:
                showSizeSelection(player);
                break;
            case THEME:
                showThemeSelection(player);
                break;
            case DIFFICULTY:
                showDifficultySelection(player);
                break;
            case PREVIEW:
                showPreview(player);
                break;
            case CONFIRM:
                showConfirmation(player);
                break;
        }
    }

    private void showNameSelection(Player player) {
        player.sendMessage("§e=== Étape 1/6: Nom du donjon ===");
        player.sendMessage("§7Tapez le nom de votre donjon dans le chat:");
        player.sendMessage("§a• Exemple: MonSuperDonjon");
        player.sendMessage("§c• Pas d'espaces autorisés");
        player.sendMessage("§7Ou tapez §c'cancel' §7pour annuler");
    }

    private void showSizeSelection(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 27, "§6Taille du donjon");

        // Petit donjon
        ItemStack small = createGuiItem(Material.STONE, "§aPetit Donjon",
                "§7Taille: 30x30", "§7Temps de génération: Rapide", "§7Difficulté: Facile");
        gui.setItem(10, small);

        // Donjon moyen
        ItemStack medium = createGuiItem(Material.COBBLESTONE, "§eDonjon Moyen",
                "§7Taille: 50x50", "§7Temps de génération: Moyen", "§7Difficulté: Normale");
        gui.setItem(13, medium);

        // Grand donjon
        ItemStack large = createGuiItem(Material.OBSIDIAN, "§cGrand Donjon",
                "§7Taille: 80x80", "§7Temps de génération: Long", "§7Difficulté: Élevée");
        gui.setItem(16, large);

        addNavigationItems(gui);
        player.openInventory(gui);
    }

    private void showThemeSelection(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 45, "§6Thème du donjon");

        int slot = 10;
        for (DungeonTheme theme : DungeonTheme.values()) {
            ItemStack item = createGuiItem(theme.getIcon(),
                    "§e" + theme.getDisplayName(), theme.getDescription());
            gui.setItem(slot, item);
            slot += 2;
        }

        addNavigationItems(gui);
        player.openInventory(gui);
    }

    private void showDifficultySelection(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 27, "§6Difficulté du donjon");

        for (int i = 1; i <= 5; i++) {
            Material material = getDifficultyMaterial(i);
            String color = getDifficultyColor(i);
            ItemStack item = createGuiItem(material,
                    color + "Difficulté " + i,
                    "§7Monstres: " + getDifficultyDescription(i),
                    "§7Pièges: " + getTrapDescription(i),
                    "§7Récompenses: " + getRewardDescription(i));
            gui.setItem(9 + i * 2, item);
        }

        addNavigationItems(gui);
        player.openInventory(gui);
    }

    private void showPreview(Player player) {
        WizardSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        player.sendMessage("§e=== Aperçu de votre donjon ===");
        player.sendMessage("§6Nom: §f" + session.getName());
        player.sendMessage("§6Taille: §f" + session.getSize() + "x" + session.getSize());
        player.sendMessage("§6Thème: §f" + session.getTheme().getDisplayName());
        player.sendMessage("§6Difficulté: §f" + session.getDifficulty());

        player.sendMessage("");
        player.sendMessage("§aTapez §e'confirm' §apour créer le donjon");
        player.sendMessage("§cTapez §e'cancel' §cpour annuler");
        player.sendMessage("§7Tapez §e'back' §7pour revenir en arrière");
    }

    private void showConfirmation(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 27, "§6Confirmer la création");

        // Confirmer
        ItemStack confirm = createGuiItem(Material.EMERALD_BLOCK, "§a✓ CRÉER LE DONJON",
                "§7Cliquez pour créer votre donjon",
                "§7Cette action est irréversible");
        gui.setItem(11, confirm);

        // Annuler
        ItemStack cancel = createGuiItem(Material.REDSTONE_BLOCK, "§c✗ ANNULER",
                "§7Retourner au jeu sans créer",
                "§7Toute la progression sera perdue");
        gui.setItem(15, cancel);

        player.openInventory(gui);
    }

    public void handleChatInput(Player player, String message) {
        WizardSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        if (message.equalsIgnoreCase("cancel")) {
            cancelWizard(player);
            return;
        }

        switch (session.getCurrentStep()) {
            case NAME:
                if (isValidDungeonName(message)) {
                    session.setName(message);
                    showStep(player, WizardStep.SIZE);
                } else {
                    player.sendMessage("§cNom invalide ! Utilisez uniquement des lettres et chiffres.");
                }
                break;

            case PREVIEW:
                if (message.equalsIgnoreCase("confirm")) {
                    createDungeon(player);
                } else if (message.equalsIgnoreCase("back")) {
                    showStep(player, WizardStep.DIFFICULTY);
                }
                break;
        }
    }

    private void createDungeon(Player player) {
        WizardSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        // Créer le template de donjon personnalisé
        DungeonTemplate template = new DungeonTemplate(session);

        // Utiliser le DungeonManager pour créer le donjon
        plugin.getDungeonManager().createCustomDungeon(player, template);

        activeSessions.remove(player.getUniqueId());
        player.sendMessage("§aDonjon créé avec succès: " + session.getName());
    }

    public void cancelWizard(Player player) {
        activeSessions.remove(player.getUniqueId());
        player.sendMessage("§7Assistant de création annulé.");
    }

    public void shutdown() {
        for (UUID playerId : new HashSet<>(activeSessions.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                cancelWizard(player);
            }
        }
        activeSessions.clear();
    }

    // Méthodes utilitaires
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private void addNavigationItems(Inventory gui) {
        // Bouton précédent
        ItemStack back = createGuiItem(Material.ARROW, "§7← Précédent");
        gui.setItem(gui.getSize() - 9, back);

        // Bouton suivant
        ItemStack next = createGuiItem(Material.ARROW, "§7Suivant →");
        gui.setItem(gui.getSize() - 1, next);

        // Bouton annuler
        ItemStack cancel = createGuiItem(Material.BARRIER, "§cAnnuler");
        gui.setItem(gui.getSize() - 5, cancel);
    }

    private boolean isValidDungeonName(String name) {
        return name.matches("[a-zA-Z0-9_]+") && name.length() >= 3 && name.length() <= 20;
    }

    private Material getDifficultyMaterial(int difficulty) {
        switch (difficulty) {
            case 1: return Material.WOODEN_SWORD;
            case 2: return Material.STONE_SWORD;
            case 3: return Material.IRON_SWORD;
            case 4: return Material.DIAMOND_SWORD;
            case 5: return Material.NETHERITE_SWORD;
            default: return Material.WOODEN_SWORD;
        }
    }

    private String getDifficultyColor(int difficulty) {
        switch (difficulty) {
            case 1: return "§a";
            case 2: return "§e";
            case 3: return "§6";
            case 4: return "§c";
            case 5: return "§4";
            default: return "§7";
        }
    }

    private String getDifficultyDescription(int difficulty) {
        switch (difficulty) {
            case 1: return "Peu nombreux, faibles";
            case 2: return "Quelques monstres normaux";
            case 3: return "Monstres variés, moyens";
            case 4: return "Nombreux et puissants";
            case 5: return "Horde dangereuse";
            default: return "Inconnu";
        }
    }

    private String getTrapDescription(int difficulty) {
        switch (difficulty) {
            case 1: return "Pièges simples, rares";
            case 2: return "Quelques pièges basiques";
            case 3: return "Pièges variés, modérés";
            case 4: return "Pièges nombreux et vicieux";
            case 5: return "Pièges mortels partout";
            default: return "Inconnu";
        }
    }

    private String getRewardDescription(int difficulty) {
        switch (difficulty) {
            case 1: return "Loot basique";
            case 2: return "Quelques objets utiles";
            case 3: return "Bonnes récompenses";
            case 4: return "Loot rare et précieux";
            case 5: return "Trésors légendaires";
            default: return "Inconnu";
        }
    }

    public WizardSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }

    public enum WizardStep {
        NAME, SIZE, THEME, DIFFICULTY, PREVIEW, CONFIRM
    }
}