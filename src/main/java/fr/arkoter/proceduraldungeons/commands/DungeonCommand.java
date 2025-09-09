package fr.arkoter.proceduraldungeons.commands;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DungeonCommand implements CommandExecutor {

    private final ProceduralDungeons plugin;

    public DungeonCommand(ProceduralDungeons plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Debug pour voir les arguments reçus
        plugin.getLogger().info("Command received: " + label + " with args: " + Arrays.toString(args));

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getMessage("messages.general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelpMessage(player);
                break;

            case "wizard":
                handleWizardCommand(player);
                break;

            case "create":
                handleCreateCommand(player, args);
                break;

            case "enter":
                handleEnterCommand(player, args);
                break;

            case "leave":
                handleLeaveCommand(player);
                break;

            case "list":
                handleListCommand(player);
                break;

            case "info":
                handleInfoCommand(player, args);
                break;

            case "delete":
                handleDeleteCommand(player, args);
                break;

            case "share":
                handleShareCommand(player, args);
                break;

            case "copy":
                handleCopyCommand(player, args);
                break;

            case "preview":
                handlePreviewCommand(player, args);
                break;

            case "template":
                handleTemplateCommand(player, args);
                break;

            case "reload":
                handleReloadCommand(player);
                break;

            default:
                player.sendMessage(MessageUtils.getMessage("messages.general.invalid-arguments"));
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§e=== ProceduralDungeons - Aide ===");
        player.sendMessage("§a/dungeon wizard §7- Assistant de création guidée");
        player.sendMessage("§a/dungeon create <nom> [taille] [difficulté] §7- Créer un donjon");
        player.sendMessage("§a/dungeon enter <nom> §7- Entrer dans un donjon");
        player.sendMessage("§a/dungeon leave §7- Quitter le donjon actuel");
        player.sendMessage("§a/dungeon list §7- Liste des donjons");
        player.sendMessage("§a/dungeon info <nom> §7- Informations d'un donjon");
        player.sendMessage("§a/dungeon delete <nom> §7- Supprimer un donjon");
        player.sendMessage("§a/dungeon reload §7- Recharger la config");
    }

    private void handleWizardCommand(Player player) {
        if (!player.hasPermission("proceduraldungeons.wizard")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        plugin.getDungeonCreationWizard().startWizard(player);
    }

    private void handleCreateCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.create")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.create"));
            return;
        }

        String name = args[1];
        int size = args.length > 2 ? parseIntSafe(args[2], 50) : 50;
        int difficulty = args.length > 3 ? parseIntSafe(args[3], 1) : 1;

        plugin.getDungeonManager().createDungeon(player, name, size, difficulty);
    }

    private void handleEnterCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.enter")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.enter"));
            return;
        }

        plugin.getDungeonManager().enterDungeon(player, args[1]);
    }

    private void handleLeaveCommand(Player player) {
        if (!player.hasPermission("proceduraldungeons.leave")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        plugin.getDungeonManager().leaveDungeon(player);
    }

    private void handleListCommand(Player player) {
        if (!player.hasPermission("proceduraldungeons.use")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        plugin.getDungeonManager().listDungeons(player);
    }

    private void handleInfoCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.use")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.info"));
            return;
        }

        plugin.getDungeonManager().showDungeonInfo(player, args[1]);
    }

    private void handleDeleteCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.delete")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.delete"));
            return;
        }

        plugin.getDungeonManager().deleteDungeon(player, args[1]);
    }

    private void handleShareCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.share")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.share"));
            return;
        }

        plugin.getDungeonManager().shareDungeon(player, args[1], args[2]);
    }

    private void handleCopyCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.copy")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.copy"));
            return;
        }

        plugin.getDungeonManager().copyDungeon(player, args[1], args[2]);
    }

    private void handlePreviewCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.preview")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.preview"));
            return;
        }

        player.sendMessage("§eAperçu du donjon: " + args[1] + " (fonctionnalité à venir)");
    }

    private void handleTemplateCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.template")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.template"));
            return;
        }

        player.sendMessage("§eTemplate: " + args[1] + " (fonctionnalité à venir)");
    }

    private void handleReloadCommand(Player player) {
        if (!player.hasPermission("proceduraldungeons.reload")) {
            player.sendMessage(MessageUtils.getMessage("messages.general.no-permission"));
            return;
        }

        plugin.reloadConfigs();
        player.sendMessage(MessageUtils.getMessage("messages.general.config-reloaded"));
    }

    private int parseIntSafe(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}