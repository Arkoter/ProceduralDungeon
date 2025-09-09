package fr.arkoter.proceduraldungeons.commands;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;  // <-- AJOUTEZ CETTE LIGNE

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

// Le reste du code reste identique...