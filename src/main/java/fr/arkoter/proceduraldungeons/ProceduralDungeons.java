package fr.arkoter.proceduraldungeons;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProceduralDungeons extends JavaPlugin {

    private DungeonManager dungeonManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        getLogger().info("ProceduralDungeons plugin activé !");

        // Initialisation des managers
        configManager = new ConfigManager(this);
        dungeonManager = new DungeonManager(this);

        // Enregistrement des événements
        getServer().getPluginManager().registerEvents(new DungeonListener(this), this);

        // Création du fichier de configuration
        saveDefaultConfig();
        configManager.loadConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("ProceduralDungeons plugin désactivé !");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur !");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("dungeon")) {
            if (args.length == 0) {
                player.sendMessage("§e/dungeon create <nom> - Créer un donjon");
                player.sendMessage("§e/dungeon enter <nom> - Entrer dans un donjon");
                player.sendMessage("§e/dungeon list - Lister les donjons");
                player.sendMessage("§e/dungeon delete <nom> - Supprimer un donjon");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length < 2) {
                        player.sendMessage("§cUsage: /dungeon create <nom>");
                        return true;
                    }
                    dungeonManager.createDungeon(player, args[1]);
                    break;

                case "enter":
                    if (args.length < 2) {
                        player.sendMessage("§cUsage: /dungeon enter <nom>");
                        return true;
                    }
                    dungeonManager.enterDungeon(player, args[1]);
                    break;

                case "list":
                    dungeonManager.listDungeons(player);
                    break;

                case "delete":
                    if (args.length < 2) {
                        player.sendMessage("§cUsage: /dungeon delete <nom>");
                        return true;
                    }
                    dungeonManager.deleteDungeon(player, args[1]);
                    break;

                default:
                    player.sendMessage("§cCommande inconnue !");
                    break;
            }
        }
        return true;
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}