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
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Commandes principales
            List<String> subCommands = Arrays.asList("create", "enter", "leave", "list", "info");

            if (player.hasPermission("proceduraldungeons.admin")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.addAll(Arrays.asList("delete", "reload"));
            }

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
                    // Complétion avec les noms de donjons existants
                    return plugin.getDungeonManager().getDungeonNames().stream()
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "create":
                    // Suggestions de noms pour nouveau donjon
                    return Arrays.asList("mon_donjon", "labyrinthe", "forteresse");
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            // Suggestions de taille
            return Arrays.asList("30", "50", "70", "100");
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            // Suggestions de difficulté
            return Arrays.asList("1", "2", "3", "4", "5");
        }

        return completions;
    }
}