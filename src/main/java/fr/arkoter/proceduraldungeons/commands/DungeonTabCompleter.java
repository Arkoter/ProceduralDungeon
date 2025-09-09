package fr.arkoter.proceduraldungeons.commands;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DungeonTabCompleter implements TabCompleter {

    private final ProceduralDungeons plugin;

    public DungeonTabCompleter(ProceduralDungeons plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Première argument : sous-commandes
            List<String> subCommands = Arrays.asList(
                    "help", "wizard", "create", "enter", "leave", "list",
                    "info", "delete", "share", "copy", "preview", "template", "reload", "stats"
            );

            return subCommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "enter":
                case "info":
                case "delete":
                case "preview":
                    // Noms des donjons existants
                    return plugin.getDungeonManager().getDungeonNames().stream()
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "create":
                    // Suggestions pour le nom du donjon
                    completions.add("<nom_du_donjon>");
                    return completions;

                case "share":
                case "copy":
                    // Noms des donjons pour partage/copie
                    return plugin.getDungeonManager().getDungeonNames().stream()
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "template":
                    // Templates disponibles
                    completions.addAll(Arrays.asList("medieval", "nether", "ocean", "desert", "ice", "end", "jungle", "steampunk"));
                    return completions.stream()
                            .filter(template -> template.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "stats":
                    // Noms des joueurs en ligne
                    return plugin.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "create":
                    // Taille du donjon
                    completions.addAll(Arrays.asList("30", "50", "80", "100"));
                    return completions;

                case "share":
                    // Noms des joueurs en ligne
                    return plugin.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());

                case "copy":
                    // Nouveau nom pour la copie
                    completions.add("<nouveau_nom>");
                    return completions;
            }
        }

        if (args.length == 4) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("create")) {
                // Difficulté du donjon
                completions.addAll(Arrays.asList("1", "2", "3", "4", "5"));
                return completions;
            }
        }

        return completions;
    }
}