package fr.arkoter.proceduraldungeons.managers;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.models.Dungeon;
import fr.arkoter.proceduraldungeons.models.DungeonPlayer;
import fr.arkoter.proceduraldungeons.generators.MazeGenerator;
import fr.arkoter.proceduraldungeons.generators.RoomGenerator;
import fr.arkoter.proceduraldungeons.generators.TrapGenerator;
import fr.arkoter.proceduraldungeons.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class DungeonManager {

    private final ProceduralDungeons plugin;
    private final Map<String, Dungeon> dungeons;
    private final Map<UUID, DungeonPlayer> dungeonPlayers;
    private final Random random;
    private final RoomGenerator roomGenerator;
    private final TrapGenerator trapGenerator;

    public DungeonManager(ProceduralDungeons plugin) {
        this.plugin = plugin;
        this.dungeons = new HashMap<>();
        this.dungeonPlayers = new HashMap<>();
        this.random = new Random();
        this.roomGenerator = new RoomGenerator();
        this.trapGenerator = new TrapGenerator();

        // Charger les donjons et joueurs depuis les fichiers
        loadData();
    }

    public void createDungeon(Player player, String name) {
        createDungeon(player, name, 50, 1);
    }

    public void createDungeon(Player player, String name, int size, int difficulty) {
        if (dungeons.containsKey(name)) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.already-exists", "{name}", name));
            return;
        }

        Location location = player.getLocation();
        size = Math.max(30, Math.min(size, 100)); // Limiter la taille entre 30 et 100
        difficulty = Math.max(1, Math.min(difficulty, 10)); // Limiter la difficulté entre 1 et 10

        Dungeon dungeon = new Dungeon(name, location, size, difficulty);
        generateDungeonStructure(dungeon);

        dungeons.put(name, dungeon);
        plugin.getDungeonData().saveDungeon(dungeon);

        player.sendMessage(MessageUtils.getMessage("messages.dungeon.created", "{name}", name));
    }

    private void generateDungeonStructure(Dungeon dungeon) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();
        int size = dungeon.getSize();

        // Générer le labyrinthe
        MazeGenerator mazeGen = new MazeGenerator(size, size);
        boolean[][] maze = mazeGen.generate();

        // Construire les murs et sols du labyrinthe
        buildMazeStructure(world, center, maze, size);

        // Générer les salles spéciales
        generateSpecialRooms(dungeon, maze);

        // Placer le boss
        placeBossRoom(dungeon, maze);

        // Ajouter des pièges
        trapGenerator.generateTraps(dungeon, maze);
    }

    private void buildMazeStructure(World world, Location center, boolean[][] maze, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Location blockLoc = center.clone().add(x - size/2, 0, z - size/2);

                if (maze[x][z]) {
                    // Sol du donjon (passage)
                    world.getBlockAt(blockLoc).setType(Material.STONE_BRICKS);
                    world.getBlockAt(blockLoc.clone().add(0, 1, 0)).setType(Material.AIR);
                    world.getBlockAt(blockLoc.clone().add(0, 2, 0)).setType(Material.AIR);
                    world.getBlockAt(blockLoc.clone().add(0, 3, 0)).setType(Material.STONE_BRICKS);
                } else {
                    // Murs du donjon
                    for (int y = 0; y < 4; y++) {
                        world.getBlockAt(blockLoc.clone().add(0, y, 0)).setType(Material.COBBLESTONE);
                    }
                }
            }
        }
    }

    private void generateSpecialRooms(Dungeon dungeon, boolean[][] maze) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();
        int roomCount = 3 + random.nextInt(3); // 3-5 salles spéciales

        for (int i = 0; i < roomCount; i++) {
            // Trouver une position valide pour la salle
            int x, z;
            int attempts = 0;
            do {
                x = random.nextInt(maze.length - 10) + 5;
                z = random.nextInt(maze[0].length - 10) + 5;
                attempts++;
            } while (!maze[x][z] && attempts < 50);

            if (attempts < 50) {
                // Utiliser RoomGenerator pour créer une salle au trésor
                Location roomCenter = center.clone().add(x - maze.length/2, 1, z - maze[0].length/2);
                List<Location> chests = roomGenerator.generateTreasureRoom(world, roomCenter, 7);

                // Ajouter les coffres au donjon
                for (Location chestLoc : chests) {
                    dungeon.addTreasureChest(chestLoc);
                }
            }
        }
    }

    private void placeBossRoom(Dungeon dungeon, boolean[][] maze) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();

        // Placer la salle du boss au fond du donjon
        int bossX = maze.length - 8;
        int bossZ = maze[0].length - 8;

        Location bossRoomCenter = center.clone().add(bossX - maze.length/2, 1, bossZ - maze[0].length/2);
        Location bossLocation = roomGenerator.generateBossRoom(world, bossRoomCenter, 10);

        dungeon.setBossLocation(bossLocation);
    }

    public void enterDungeon(Player player, String name) {
        Dungeon dungeon = dungeons.get(name);
        if (dungeon == null) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.not-found"));
            return;
        }

        DungeonPlayer dungeonPlayer = getDungeonPlayer(player.getUniqueId());

        // Vérifier si le joueur est déjà dans un donjon
        if (dungeonPlayer.isInDungeon()) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.already-in-dungeon"));
            return;
        }

        // Sauvegarder la position de sortie
        dungeonPlayer.setExitLocation(player.getLocation());
        dungeonPlayer.setCurrentDungeon(name);
        dungeonPlayer.setEnterTime(System.currentTimeMillis());
        dungeonPlayer.reset(); // Reset les statistiques de session

        // Téléporter le joueur à l'entrée du donjon
        Location entrance = dungeon.getLocation().clone().add(0, 1, -(dungeon.getSize()/2));
        player.teleport(entrance);

        // Ajouter le joueur au donjon
        dungeon.addActivePlayer(player.getUniqueId().toString());
        dungeon.incrementTimesEntered();

        player.sendMessage(MessageUtils.getMessage("messages.dungeon.entered", "{name}", name));
        player.sendMessage("§eDifficulté: " + dungeon.getDifficulty());

        // Spawner le boss si il n'existe pas déjà
        if (!dungeon.isBossAlive()) {
            spawnBoss(dungeon);
        }

        savePlayerData(player.getUniqueId());
    }

    public void leaveDungeon(Player player) {
        DungeonPlayer dungeonPlayer = getDungeonPlayer(player.getUniqueId());

        if (!dungeonPlayer.isInDungeon()) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.not-in-dungeon"));
            return;
        }

        String dungeonName = dungeonPlayer.getCurrentDungeon();
        Dungeon dungeon = dungeons.get(dungeonName);

        // Téléporter le joueur à sa position de sortie
        if (dungeonPlayer.getExitLocation() != null) {
            player.teleport(dungeonPlayer.getExitLocation());
        }

        // Retirer le joueur du donjon
        if (dungeon != null) {
            dungeon.removeActivePlayer(player.getUniqueId().toString());
        }

        // Sauvegarder le temps passé dans le donjon
        if (dungeonPlayer.getEnterTime() > 0) {
            long timeSpent = System.currentTimeMillis() - dungeonPlayer.getEnterTime();
            plugin.getPlayerData().addTimeInDungeons(player.getUniqueId(), timeSpent);
        }

        dungeonPlayer.reset();
        player.sendMessage(MessageUtils.getMessage("messages.dungeon.left"));

        savePlayerData(player.getUniqueId());
    }

    private void spawnBoss(Dungeon dungeon) {
        Location bossLoc = dungeon.getBossLocation();
        if (bossLoc != null) {
            World world = bossLoc.getWorld();

            // Spawner différents types de boss selon la difficulté
            EntityType bossType;
            switch (dungeon.getDifficulty()) {
                case 1:
                    bossType = EntityType.ZOMBIE;
                    break;
                case 2:
                    bossType = EntityType.SKELETON;
                    break;
                case 3:
                    bossType = EntityType.WITHER_SKELETON;
                    break;
                default:
                    bossType = EntityType.WITHER;
                    break;
            }

            world.spawnEntity(bossLoc, bossType);
            dungeon.setBossAlive(true);
        }
    }

    public void listDungeons(Player player) {
        if (dungeons.isEmpty()) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.no-dungeons"));
            return;
        }

        player.sendMessage("§e=== Liste des donjons ===");
        for (Dungeon dungeon : dungeons.values()) {
            String status = dungeon.getActivePlayerCount() > 0 ? "§a[Actif]" : "§7[Vide]";
            player.sendMessage("§a- " + dungeon.getName() + " " + status +
                    " §7(Difficulté: " + dungeon.getDifficulty() +
                    ", Joueurs: " + dungeon.getActivePlayerCount() + ")");
        }
    }

    public void deleteDungeon(Player player, String name) {
        if (!dungeons.containsKey(name)) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.not-found"));
            return;
        }

        Dungeon dungeon = dungeons.get(name);

        // Faire sortir tous les joueurs du donjon
        for (String playerId : new HashSet<>(dungeon.getActivePlayers())) {
            UUID uuid = UUID.fromString(playerId);
            Player dungeonPlayer = plugin.getServer().getPlayer(uuid);
            if (dungeonPlayer != null) {
                leaveDungeon(dungeonPlayer);
            }
        }

        dungeons.remove(name);
        plugin.getDungeonData().deleteDungeon(name);

        player.sendMessage(MessageUtils.getMessage("messages.dungeon.deleted", "{name}", name));
    }

    public void showDungeonInfo(Player player, String name) {
        Dungeon dungeon = dungeons.get(name);
        if (dungeon == null) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.not-found"));
            return;
        }

        player.sendMessage("§e=== Informations du donjon " + name + " ===");
        player.sendMessage("§7Taille: §f" + dungeon.getSize() + "x" + dungeon.getSize());
        player.sendMessage("§7Difficulté: §f" + dungeon.getDifficulty());
        player.sendMessage("§7Joueurs actifs: §f" + dungeon.getActivePlayerCount());
        player.sendMessage("§7Fois entré: §f" + dungeon.getTimesEntered());
        player.sendMessage("§7Fois complété: §f" + dungeon.getTimesCompleted());
        player.sendMessage("§7Taux de réussite: §f" + String.format("%.1f%%", dungeon.getCompletionRate()));
        player.sendMessage("§7Coffres au trésor: §f" + dungeon.getTreasureChests().size());
        player.sendMessage("§7Pièges: §f" + dungeon.getTraps().size());
        player.sendMessage("§7Record de vitesse: §f" + dungeon.getFormattedFastestCompletion());
        player.sendMessage("§7Créé le: §f" + dungeon.getFormattedCreationDate());
    }

    // Méthodes de gestion des données

    public DungeonPlayer getDungeonPlayer(UUID playerId) {
        return dungeonPlayers.computeIfAbsent(playerId, DungeonPlayer::new);
    }

    public void loadPlayerData(UUID playerId) {
        DungeonPlayer player = plugin.getPlayerData().loadPlayer(playerId);
        dungeonPlayers.put(playerId, player);
    }

    public void savePlayerData(UUID playerId) {
        DungeonPlayer player = dungeonPlayers.get(playerId);
        if (player != null) {
            plugin.getPlayerData().savePlayer(player);
        }
    }

    public void removePlayerFromDungeon(UUID playerId, String dungeonName) {
        Dungeon dungeon = dungeons.get(dungeonName);
        if (dungeon != null) {
            dungeon.removeActivePlayer(playerId.toString());
        }

        DungeonPlayer player = dungeonPlayers.get(playerId);
        if (player != null) {
            player.reset();
        }
    }

    public boolean isLocationInDungeon(Location location, String dungeonName) {
        Dungeon dungeon = dungeons.get(dungeonName);
        if (dungeon == null) return false;

        Location dungeonCenter = dungeon.getLocation();
        double distance = location.distance(dungeonCenter);
        double maxDistance = dungeon.getSize() * Math.sqrt(2); // Distance diagonale

        return distance <= maxDistance;
    }

    public Dungeon getDungeon(String name) {
        return dungeons.get(name);
    }

    public Map<String, Dungeon> getDungeons() {
        return new HashMap<>(dungeons);
    }

    public List<String> getDungeonNames() {
        return new ArrayList<>(dungeons.keySet());
    }

    private void loadData() {
        // Charger les donjons
        Map<String, Dungeon> loadedDungeons = plugin.getDungeonData().loadAllDungeons();
        dungeons.putAll(loadedDungeons);

        // Charger les joueurs
        Map<UUID, DungeonPlayer> loadedPlayers = plugin.getPlayerData().loadAllPlayers();
        dungeonPlayers.putAll(loadedPlayers);

        plugin.getLogger().info("Chargés: " + dungeons.size() + " donjons et " + dungeonPlayers.size() + " joueurs");
    }

    public void saveAllData() {
        plugin.getDungeonData().saveAllDungeons(dungeons);

        for (DungeonPlayer player : dungeonPlayers.values()) {
            plugin.getPlayerData().savePlayer(player);
        }
    }

    public void shutdown() {
        // Faire sortir tous les joueurs des donjons
        for (DungeonPlayer player : dungeonPlayers.values()) {
            if (player.isInDungeon()) {
                UUID playerId = player.getPlayerId();
                Player bukkitPlayer = plugin.getServer().getPlayer(playerId);
                if (bukkitPlayer != null) {
                    leaveDungeon(bukkitPlayer);
                }
            }
        }

        // Sauvegarder toutes les données
        saveAllData();
    }
}