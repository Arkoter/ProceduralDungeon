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
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getMessage("messages.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
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

            case "delete":
                handleDeleteCommand(player, args);
                break;

            case "info":
                handleInfoCommand(player, args);
                break;

            case "reload":
                handleReloadCommand(player);
                break;

            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(MessageUtils.getMessage("messages.help.header"));
        player.sendMessage(MessageUtils.getMessage("messages.help.create"));
        player.sendMessage(MessageUtils.getMessage("messages.help.enter"));
        player.sendMessage(MessageUtils.getMessage("messages.help.leave"));
        player.sendMessage(MessageUtils.getMessage("messages.help.list"));
        player.sendMessage(MessageUtils.getMessage("messages.help.delete"));
        player.sendMessage(MessageUtils.getMessage("messages.help.info"));
        if (player.hasPermission("proceduraldungeons.admin")) {
            player.sendMessage(MessageUtils.getMessage("messages.help.reload"));
        }
    }

    private void handleCreateCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.create")) {
            player.sendMessage(MessageUtils.getMessage("messages.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.create"));
            return;
        }

        String dungeonName = args[1];
        int size = args.length > 2 ? parseSize(args[2]) : 50;
        int difficulty = args.length > 3 ? parseDifficulty(args[3]) : 1;

        plugin.getDungeonManager().createDungeon(player, dungeonName, size, difficulty);
    }

    private void handleEnterCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.enter")) {
            player.sendMessage(MessageUtils.getMessage("messages.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.enter"));
            return;
        }

        String dungeonName = args[1];
        plugin.getDungeonManager().enterDungeon(player, dungeonName);
    }

    private void handleLeaveCommand(Player player) {
        plugin.getDungeonManager().leaveDungeon(player);
    }

    private void handleListCommand(Player player) {
        plugin.getDungeonManager().listDungeons(player);
    }

    private void handleDeleteCommand(Player player, String[] args) {
        if (!player.hasPermission("proceduraldungeons.admin")) {
            player.sendMessage(MessageUtils.getMessage("messages.no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.delete"));
            return;
        }

        String dungeonName = args[1];
        plugin.getDungeonManager().deleteDungeon(player, dungeonName);
    }

    private void handleInfoCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtils.getMessage("messages.usage.info"));
            return;
        }

        String dungeonName = args[1];
        plugin.getDungeonManager().showDungeonInfo(player, dungeonName);
    }

    private void handleReloadCommand(Player player) {
        if (!player.hasPermission("proceduraldungeons.admin")) {
            player.sendMessage(MessageUtils.getMessage("messages.no-permission"));
            return;
        }

        plugin.reloadConfigs();
        player.sendMessage(MessageUtils.getMessage("messages.config-reloaded"));
    }

    private int parseSize(String sizeStr) {
        try {
            int size = Integer.parseInt(sizeStr);
            return Math.max(20, Math.min(100, size));
        } catch (NumberFormatException e) {
            return 50;
        }
    }

    private int parseDifficulty(String diffStr) {
        try {
            int diff = Integer.parseInt(diffStr);
            return Math.max(1, Math.min(10, diff));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}