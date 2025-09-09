package fr.arkoter.proceduraldungeons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import java.util.*;

public class DungeonManager {

    private final ProceduralDungeons plugin;
    private final Map<String, Dungeon> dungeons;
    private final Random random;

    public DungeonManager(ProceduralDungeons plugin) {
        this.plugin = plugin;
        this.dungeons = new HashMap<>();
        this.random = new Random();
    }

    public void createDungeon(Player player, String name) {
        if (dungeons.containsKey(name)) {
            player.sendMessage("§cUn donjon avec ce nom existe déjà !");
            return;
        }

        Location location = player.getLocation();
        int size = 50; // Taille du donjon
        int difficulty = 1; // Difficulté de base

        Dungeon dungeon = new Dungeon(name, location, size, difficulty);
        generateDungeonStructure(dungeon);

        dungeons.put(name, dungeon);
        player.sendMessage("§aDonjon '" + name + "' créé avec succès !");
    }

    private void generateDungeonStructure(Dungeon dungeon) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();
        int size = dungeon.getSize();

        // Générer le labyrinthe
        MazeGenerator mazeGen = new MazeGenerator(size, size);
        boolean[][] maze = mazeGen.generate();

        // Construire les murs et sols du labyrinthe
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Location blockLoc = center.clone().add(x - size/2, 0, z - size/2);

                if (maze[x][z]) {
                    // Sol du donjon
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

        // Ajouter des salles spéciales
        generateSpecialRooms(dungeon, maze);

        // Placer le boss
        placeBossRoom(dungeon, maze);

        // Ajouter des pièges
        addTraps(dungeon, maze);
    }

    private void generateSpecialRooms(Dungeon dungeon, boolean[][] maze) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();
        int roomCount = 3 + random.nextInt(3); // 3-5 salles spéciales

        for (int i = 0; i < roomCount; i++) {
            // Trouver une position valide pour la salle
            int x, z;
            do {
                x = random.nextInt(maze.length - 6) + 3;
                z = random.nextInt(maze[0].length - 6) + 3;
            } while (!maze[x][z]);

            // Créer une salle 5x5
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (x + dx >= 0 && x + dx < maze.length && z + dz >= 0 && z + dz < maze[0].length) {
                        maze[x + dx][z + dz] = true;

                        Location roomLoc = center.clone().add((x + dx) - maze.length/2, 1, (z + dz) - maze[0].length/2);
                        world.getBlockAt(roomLoc).setType(Material.AIR);
                        world.getBlockAt(roomLoc.clone().add(0, 1, 0)).setType(Material.AIR);
                    }
                }
            }

            // Placer du loot au centre de la salle
            Location lootLoc = center.clone().add(x - maze.length/2, 1, z - maze[0].length/2);
            world.getBlockAt(lootLoc).setType(Material.CHEST);

            // Stocker la position du coffre pour le loot
            dungeon.addTreasureChest(lootLoc);
        }
    }

    private void placeBossRoom(Dungeon dungeon, boolean[][] maze) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();

        // Placer la salle du boss au fond du donjon
        int bossX = maze.length - 8;
        int bossZ = maze[0].length - 8;

        // Créer une grande salle pour le boss
        for (int dx = 0; dx < 7; dx++) {
            for (int dz = 0; dz < 7; dz++) {
                if (bossX + dx < maze.length && bossZ + dz < maze[0].length) {
                    maze[bossX + dx][bossZ + dz] = true;

                    Location bossRoomLoc = center.clone().add((bossX + dx) - maze.length/2, 1, (bossZ + dz) - maze[0].length/2);
                    world.getBlockAt(bossRoomLoc).setType(Material.AIR);
                    world.getBlockAt(bossRoomLoc.clone().add(0, 1, 0)).setType(Material.AIR);
                }
            }
        }

        // Position du boss au centre de la salle
        Location bossLoc = center.clone().add((bossX + 3) - maze.length/2, 1, (bossZ + 3) - maze[0].length/2);
        dungeon.setBossLocation(bossLoc);

        // Placer un spawner ou marquer pour spawn manuel
        world.getBlockAt(bossLoc.clone().add(0, -1, 0)).setType(Material.SPAWNER);
    }

    private void addTraps(Dungeon dungeon, boolean[][] maze) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();
        int trapCount = 5 + random.nextInt(10); // 5-14 pièges

        for (int i = 0; i < trapCount; i++) {
            int x, z;
            do {
                x = random.nextInt(maze.length);
                z = random.nextInt(maze[0].length);
            } while (!maze[x][z]);

            Location trapLoc = center.clone().add(x - maze.length/2, 0, z - maze[0].length/2);

            // Types de pièges
            int trapType = random.nextInt(3);
            switch (trapType) {
                case 0: // Piège à pression
                    world.getBlockAt(trapLoc).setType(Material.STONE_PRESSURE_PLATE);
                    break;
                case 1: // Piège avec TNT caché
                    world.getBlockAt(trapLoc.clone().add(0, -1, 0)).setType(Material.TNT);
                    world.getBlockAt(trapLoc).setType(Material.STONE_PRESSURE_PLATE);
                    break;
                case 2: // Fosse
                    world.getBlockAt(trapLoc.clone().add(0, -1, 0)).setType(Material.AIR);
                    world.getBlockAt(trapLoc.clone().add(0, -2, 0)).setType(Material.AIR);
                    world.getBlockAt(trapLoc.clone().add(0, -3, 0)).setType(Material.LAVA);
                    break;
            }

            dungeon.addTrap(trapLoc, trapType);
        }
    }

    public void enterDungeon(Player player, String name) {
        Dungeon dungeon = dungeons.get(name);
        if (dungeon == null) {
            player.sendMessage("§cDonjon introuvable !");
            return;
        }

        // Téléporter le joueur à l'entrée du donjon
        Location entrance = dungeon.getLocation().clone().add(0, 1, -(dungeon.getSize()/2));
        player.teleport(entrance);
        player.sendMessage("§aVous êtes entré dans le donjon '" + name + "' !");
        player.sendMessage("§eDifficulté: " + dungeon.getDifficulty());

        // Spawner le boss si il n'existe pas déjà
        if (!dungeon.isBossAlive()) {
            spawnBoss(dungeon);
        }
    }

    private void spawnBoss(Dungeon dungeon) {
        Location bossLoc = dungeon.getBossLocation();
        if (bossLoc != null) {
            World world = bossLoc.getWorld();

            // Spawner différents types de boss selon la difficulté
            switch (dungeon.getDifficulty()) {
                case 1:
                    world.spawnEntity(bossLoc, org.bukkit.entity.EntityType.ZOMBIE);
                    break;
                case 2:
                    world.spawnEntity(bossLoc, org.bukkit.entity.EntityType.SKELETON);
                    break;
                case 3:
                    world.spawnEntity(bossLoc, org.bukkit.entity.EntityType.WITHER_SKELETON);
                    break;
                default:
                    world.spawnEntity(bossLoc, org.bukkit.entity.EntityType.WITHER);
                    break;
            }
            dungeon.setBossAlive(true);
        }
    }

    public void listDungeons(Player player) {
        if (dungeons.isEmpty()) {
            player.sendMessage("§cAucun donjon créé !");
            return;
        }

        player.sendMessage("§e=== Liste des donjons ===");
        for (Dungeon dungeon : dungeons.values()) {
            player.sendMessage("§a- " + dungeon.getName() + " §7(Difficulté: " + dungeon.getDifficulty() + ")");
        }
    }

    public void deleteDungeon(Player player, String name) {
        if (!dungeons.containsKey(name)) {
            player.sendMessage("§cDonjon introuvable !");
            return;
        }

        dungeons.remove(name);
        player.sendMessage("§aDonjon '" + name + "' supprimé !");
    }

    public Map<String, Dungeon> getDungeons() {
        return dungeons;
    }
}