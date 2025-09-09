package fr.arkoter.proceduraldungeons.generators;

import fr.arkoter.proceduraldungeons.models.Dungeon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class TrapGenerator {

    private final Random random;

    public TrapGenerator() {
        this.random = new Random();
    }

    public void generateTraps(Dungeon dungeon, boolean[][] maze) {
        World world = dungeon.getLocation().getWorld();
        Location center = dungeon.getLocation();
        int trapCount = calculateTrapCount(dungeon);

        for (int i = 0; i < trapCount; i++) {
            Location trapLocation = findValidTrapLocation(maze, center);
            if (trapLocation != null) {
                int trapType = selectTrapType(dungeon.getDifficulty());
                createTrap(world, trapLocation, trapType);
                dungeon.addTrap(trapLocation, trapType);
            }
        }
    }

    private int calculateTrapCount(Dungeon dungeon) {
        int baseTraps = 5;
        int difficultyBonus = dungeon.getDifficulty() * 2;
        int sizeBonus = dungeon.getSize() / 10;

        return baseTraps + difficultyBonus + sizeBonus;
    }

    private Location findValidTrapLocation(boolean[][] maze, Location center) {
        int attempts = 50;

        for (int i = 0; i < attempts; i++) {
            int x = random.nextInt(maze.length);
            int z = random.nextInt(maze[0].length);

            if (maze[x][z]) { // Position valide (couloir)
                return center.clone().add(x - maze.length/2, 0, z - maze[0].length/2);
            }
        }

        return null; // Aucune position valide trouvée
    }

    private int selectTrapType(int difficulty) {
        // Plus la difficulté est élevée, plus les pièges sont dangereux
        int maxTrapType = Math.min(difficulty + 2, 6);
        return random.nextInt(maxTrapType);
    }

    private void createTrap(World world, Location location, int trapType) {
        switch (trapType) {
            case 0: // Piège à pression basique
                createPressureTrap(world, location);
                break;
            case 1: // Piège avec TNT
                createTNTTrap(world, location);
                break;
            case 2: // Fosse de lave
                createLavaPit(world, location);
                break;
            case 3: // Piège à flèches
                createArrowTrap(world, location);
                break;
            case 4: // Piège d'empoisonnement
                createPoisonTrap(world, location);
                break;
            case 5: // Piège de téléportation
                createTeleportTrap(world, location);
                break;
            default:
                createPressureTrap(world, location);
                break;
        }
    }

    private void createPressureTrap(World world, Location location) {
        world.getBlockAt(location.clone().add(0, 1, 0)).setType(Material.STONE_PRESSURE_PLATE);
    }

    private void createTNTTrap(World world, Location location) {
        world.getBlockAt(location.clone().add(0, -1, 0)).setType(Material.TNT);
        world.getBlockAt(location.clone().add(0, 1, 0)).setType(Material.STONE_PRESSURE_PLATE);

        // Redstone pour activer
        world.getBlockAt(location.clone().add(0, -2, 0)).setType(Material.REDSTONE_BLOCK);
    }

    private void createLavaPit(World world, Location location) {
        // Creuser une fosse 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location pitLoc = location.clone().add(x, 0, z);
                world.getBlockAt(pitLoc).setType(Material.AIR);
                world.getBlockAt(pitLoc.clone().add(0, -1, 0)).setType(Material.AIR);
                world.getBlockAt(pitLoc.clone().add(0, -2, 0)).setType(Material.LAVA);
            }
        }
    }

    private void createArrowTrap(World world, Location location) {
        world.getBlockAt(location.clone().add(0, 1, 0)).setType(Material.TRIPWIRE_HOOK);

        // Dispensers dans les murs
        world.getBlockAt(location.clone().add(2, 1, 0)).setType(Material.DISPENSER);
        world.getBlockAt(location.clone().add(-2, 1, 0)).setType(Material.DISPENSER);
    }

    private void createPoisonTrap(World world, Location location) {
        world.getBlockAt(location.clone().add(0, 1, 0)).setType(Material.STONE_PRESSURE_PLATE);

        // Marquer comme piège poison (géré dans les listeners)
        world.getBlockAt(location.clone().add(0, -1, 0)).setType(Material.EMERALD_BLOCK);
    }

    private void createTeleportTrap(World world, Location location) {
        world.getBlockAt(location.clone().add(0, 1, 0)).setType(Material.STONE_PRESSURE_PLATE);

        // Marquer comme piège téléportation
        world.getBlockAt(location.clone().add(0, -1, 0)).setType(Material.DIAMOND_BLOCK);
    }
}