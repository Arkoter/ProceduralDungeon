package fr.arkoter.proceduraldungeons.managers;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import org.bukkit.entity.Player;

public class TemplateManager {

    private final ProceduralDungeons plugin;

    public TemplateManager(ProceduralDungeons plugin) {
        this.plugin = plugin;
    }

    public void createFromTemplate(Player player, String templateName) {
        player.sendMessage("§eTemplate: " + templateName + " (fonctionnalité à implémenter)");
    }
}