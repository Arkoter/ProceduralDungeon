package fr.arkoter.proceduraldungeons;

import fr.arkoter.proceduraldungeons.commands.DungeonCommand;
import fr.arkoter.proceduraldungeons.commands.DungeonTabCompleter;
import fr.arkoter.proceduraldungeons.data.DungeonData;
import fr.arkoter.proceduraldungeons.data.PlayerData;
import fr.arkoter.proceduraldungeons.listeners.DungeonListener;
import fr.arkoter.proceduraldungeons.listeners.EntityListener;
import fr.arkoter.proceduraldungeons.listeners.PlayerListener;
import fr.arkoter.proceduraldungeons.listeners.WizardListener;
import fr.arkoter.proceduraldungeons.managers.ConfigManager;
import fr.arkoter.proceduraldungeons.managers.DungeonCreationWizard;
import fr.arkoter.proceduraldungeons.managers.DungeonManager;
import fr.arkoter.proceduraldungeons.managers.TemplateManager;
import fr.arkoter.proceduraldungeons.managers.PreviewManager;
import fr.arkoter.proceduraldungeons.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class ProceduralDungeons extends JavaPlugin {

    private DungeonManager dungeonManager;
    private ConfigManager configManager;
    private DungeonData dungeonData;
    private PlayerData playerData;
    private DungeonCreationWizard dungeonCreationWizard;
    private TemplateManager templateManager;
    private PreviewManager previewManager;
    private BukkitTask autoSaveTask;

    @Override
    public void onEnable() {
        getLogger().info("=== ProceduralDungeons v1.0.0 ===");
        getLogger().info("Développé par Arkoter");
        getLogger().info("Chargement du plugin...");

        // Créer le dossier de données
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
            getLogger().info("Dossier de données créé: " + getDataFolder().getPath());
        }

        // IMPORTANT: Sauvegarder la config par défaut AVANT d'initialiser ConfigManager
        getLogger().info("Création des fichiers de configuration...");
        saveDefaultConfig();
        createDefaultMessages();

        // Initialisation du gestionnaire de configuration APRÈS saveDefaultConfig()
        getLogger().info("Chargement de la configuration...");
        configManager = new ConfigManager(this);
        configManager.loadConfig(); // Charger immédiatement la config

        // Initialisation des gestionnaires de données
        getLogger().info("Chargement des données...");
        dungeonData = new DungeonData(this);
        playerData = new PlayerData(this);

        // Charger les messages
        getLogger().info("Chargement des messages...");
        MessageUtils.loadMessages(new File(getDataFolder(), "messages.yml"));

        // Initialisation des gestionnaires principaux
        getLogger().info("Initialisation des gestionnaires...");
        dungeonManager = new DungeonManager(this);
        dungeonCreationWizard = new DungeonCreationWizard(this);
        templateManager = new TemplateManager(this);
        previewManager = new PreviewManager(this);

        // Enregistrement des événements
        getLogger().info("Enregistrement des listeners...");
        getServer().getPluginManager().registerEvents(new DungeonListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WizardListener(this), this);

        // Enregistrement des commandes
        getLogger().info("Enregistrement des commandes...");
        DungeonCommand dungeonCommand = new DungeonCommand(this);
        getCommand("dungeon").setExecutor(dungeonCommand);
        getCommand("dungeon").setTabCompleter(new DungeonTabCompleter(this));

        // Démarrer la sauvegarde automatique
        startAutoSave();

        getLogger().info("ProceduralDungeons activé avec succès !");
        getLogger().info("Commandes disponibles: /dungeon help");
    }

    @Override
    public void onDisable() {
        getLogger().info("Désactivation de ProceduralDungeons...");

        // Arrêter la sauvegarde automatique
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        // Fermer tous les wizards actifs
        if (dungeonCreationWizard != null) {
            dungeonCreationWizard.shutdown();
        }

        // Sauvegarder et fermer tous les donjons
        if (dungeonManager != null) {
            dungeonManager.shutdown();
        }

        getLogger().info("ProceduralDungeons désactivé avec succès !");
    }

    private void createDefaultMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
            getLogger().info("Fichier messages.yml créé");
        }
    }

    public void reloadConfigs() {
        getLogger().info("Rechargement de la configuration...");

        // Recharger les configurations
        reloadConfig();
        if (configManager != null) {
            configManager.loadConfig();
        }

        // Recharger les messages
        MessageUtils.loadMessages(new File(getDataFolder(), "messages.yml"));

        // Redémarrer la sauvegarde automatique
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        startAutoSave();

        getLogger().info("Configuration rechargée avec succès !");
    }

    private void startAutoSave() {
        if (configManager == null) return;

        int interval = configManager.getAutoSaveInterval();
        if (interval > 0) {
            autoSaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                if (dungeonManager != null) {
                    dungeonManager.saveAllData();
                    getLogger().info("Sauvegarde automatique effectuée");
                }
            }, interval * 20L, interval * 20L);

            getLogger().info("Sauvegarde automatique activée (toutes les " + interval + " secondes)");
        }
    }

    // Getters pour tous les gestionnaires

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DungeonData getDungeonData() {
        return dungeonData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public DungeonCreationWizard getDungeonCreationWizard() {
        return dungeonCreationWizard;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public PreviewManager getPreviewManager() {
        return previewManager;
    }

    // Méthodes utilitaires

    public void saveAllData() {
        if (dungeonManager != null) {
            dungeonManager.saveAllData();
        }
    }

    public void debug(String message) {
        if (configManager != null && configManager.isDebugEnabled()) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public void logPerformance(String operation, long startTime) {
        if (configManager != null && configManager.isShowPerformanceStats()) {
            long duration = System.currentTimeMillis() - startTime;
            getLogger().info("[PERF] " + operation + " completed in " + duration + "ms");
        }
    }
}