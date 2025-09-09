package fr.arkoter.proceduraldungeons;

import fr.arkoter.proceduraldungeons.commands.DungeonCommand;
import fr.arkoter.proceduraldungeons.commands.DungeonTabCompleter;
import fr.arkoter.proceduraldungeons.data.DungeonData;
import fr.arkoter.proceduraldungeons.data.PlayerData;
import fr.arkoter.proceduraldungeons.listeners.DungeonListener;
import fr.arkoter.proceduraldungeons.listeners.EntityListener;
import fr.arkoter.proceduraldungeons.listeners.PlayerListener;
import fr.arkoter.proceduraldungeons.managers.ConfigManager;
import fr.arkoter.proceduraldungeons.managers.DungeonManager;
import fr.arkoter.proceduraldungeons.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ProceduralDungeons extends JavaPlugin {

    private DungeonManager dungeonManager;
    private ConfigManager configManager;
    private DungeonData dungeonData;
    private PlayerData playerData;

    @Override
    public void onEnable() {
        getLogger().info("ProceduralDungeons plugin activé !");

        // Créer le dossier de données
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialisation des managers et données
        configManager = new ConfigManager(this);
        dungeonData = new DungeonData(this);
        playerData = new PlayerData(this);
        dungeonManager = new DungeonManager(this);

        // Charger les messages
        MessageUtils.loadMessages(new File(getDataFolder(), "messages.yml"));

        // Enregistrement des événements
        getServer().getPluginManager().registerEvents(new DungeonListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Enregistrement des commandes
        DungeonCommand dungeonCommand = new DungeonCommand(this);
        getCommand("dungeon").setExecutor(dungeonCommand);
        getCommand("dungeon").setTabCompleter(new DungeonTabCompleter(this));

        // Création du fichier de configuration
        saveDefaultConfig();
        configManager.loadConfig();

        getLogger().info("ProceduralDungeons chargé avec succès !");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProceduralDungeons plugin désactivé !");

        if (dungeonManager != null) {
            dungeonManager.shutdown();
        }
    }

    public void reloadConfigs() {
        reloadConfig();
        configManager.loadConfig();
        MessageUtils.loadMessages(new File(getDataFolder(), "messages.yml"));
    }

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
}