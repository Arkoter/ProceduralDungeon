package fr.arkoter.proceduraldungeons.managers;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import org.bukkit.entity.Player;

public class PreviewManager {

    private final ProceduralDungeons plugin;

    public PreviewManager(ProceduralDungeons plugin) {
        this.plugin = plugin;
    }

    public void showPreview(Player player, String dungeonName) {
        player.sendMessage("§eAperçu du donjon: " + dungeonName + " (fonctionnalité à implémenter)");
    }
}