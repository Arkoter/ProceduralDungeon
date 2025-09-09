package fr.arkoter.proceduraldungeons.listeners;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.models.DungeonPlayer;
import fr.arkoter.proceduraldungeons.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

    private final ProceduralDungeons plugin;

    public PlayerListener(ProceduralDungeons plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Charger les données du joueur
        plugin.getDungeonManager().loadPlayerData(player.getUniqueId());

        // Vérifier si le joueur était dans un donjon lors de sa déconnexion
        DungeonPlayer dungeonPlayer = plugin.getDungeonManager().getDungeonPlayer(player.getUniqueId());

        if (dungeonPlayer.isInDungeon()) {
            // Téléporter le joueur à sa position de sortie si elle existe
            if (dungeonPlayer.getExitLocation() != null) {
                player.teleport(dungeonPlayer.getExitLocation());
                dungeonPlayer.reset();
                player.sendMessage(MessageUtils.getMessage("messages.dungeon.left"));
                plugin.getDungeonManager().savePlayerData(player.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DungeonPlayer dungeonPlayer = plugin.getDungeonManager().getDungeonPlayer(player.getUniqueId());

        if (dungeonPlayer.isInDungeon()) {
            // Sauvegarder la position actuelle comme position de sortie
            dungeonPlayer.setExitLocation(player.getLocation());

            // Retirer le joueur du donjon
            String dungeonName = dungeonPlayer.getCurrentDungeon();
            plugin.getDungeonManager().removePlayerFromDungeon(player.getUniqueId(), dungeonName);
        }

        // Sauvegarder les données du joueur
        plugin.getDungeonManager().savePlayerData(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        DungeonPlayer dungeonPlayer = plugin.getDungeonManager().getDungeonPlayer(player.getUniqueId());

        if (dungeonPlayer.isInDungeon()) {
            // Si le joueur meurt dans un donjon, le faire sortir
            if (dungeonPlayer.getExitLocation() != null) {
                event.setRespawnLocation(dungeonPlayer.getExitLocation());
            }

            // Faire sortir le joueur du donjon
            plugin.getDungeonManager().leaveDungeon(player);

            // Message de mort dans un donjon
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.death-exit"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        DungeonPlayer dungeonPlayer = plugin.getDungeonManager().getDungeonPlayer(player.getUniqueId());

        if (!dungeonPlayer.isInDungeon()) {
            return;
        }

        // Vérifier si le joueur se téléporte hors du donjon
        String dungeonName = dungeonPlayer.getCurrentDungeon();
        if (plugin.getDungeonManager().isLocationInDungeon(event.getTo(), dungeonName)) {
            return; // Toujours dans le donjon
        }

        // Si la téléportation n'est pas causée par le plugin lui-même
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
            // Permettre la téléportation mais faire sortir le joueur du donjon
            plugin.getDungeonManager().removePlayerFromDungeon(player.getUniqueId(), dungeonName);
            dungeonPlayer.reset();
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.left"));
        }
    }
}