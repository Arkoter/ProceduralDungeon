package fr.arkoter.proceduraldungeons.generators;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoomGenerator {

    private final Random random;

    public RoomGenerator() {
        this.random = new Random();
    }

    public List<Location> generateTreasureRoom(World world, Location center, int size) {
        List<Location> chestLocations = new ArrayList<>();

        // Créer une salle rectangulaire
        for (int x = -size/2; x <= size/2; x++) {
            for (int z = -size/2; z <= size/2; z++) {
                Location loc = center.clone().add(x, 0, z);

                // Murs
                if (x == -size/2 || x == size/2 || z == -size/2 || z == size/2) {
                    world.getBlockAt(loc).setType(Material.STONE_BRICKS);
                    world.getBlockAt(loc.clone().add(0, 1, 0)).setType(Material.STONE_BRICKS);
                    world.getBlockAt(loc.clone().add(0, 2, 0)).setType(Material.STONE_BRICKS);
                    world.getBlockAt(loc.clone().add(0, 3, 0)).setType(Material.STONE_BRICKS);
                } else {
                    // Sol et air
                    world.getBlockAt(loc).setType(Material.POLISHED_ANDESITE);
                    world.getBlockAt(loc.clone().add(0, 1, 0)).setType(Material.AIR);
                    world.getBlockAt(loc.clone().add(0, 2, 0)).setType(Material.AIR);
                }
            }
        }

        // Placer des coffres
        int chestCount = 1 + random.nextInt(3);
        for (int i = 0; i < chestCount; i++) {
            int x, z;
            do {
                x = random.nextInt(size - 2) - (size/2 - 1);
                z = random.nextInt(size - 2) - (size/2 - 1);
            } while (x == 0 && z == 0); // Éviter le centre

            Location chestLoc = center.clone().add(x, 1, z);
            world.getBlockAt(chestLoc).setType(Material.CHEST);
            chestLocations.add(chestLoc);
        }

        // Ajouter de la décoration
        addRoomDecoration(world, center, size);

        return chestLocations;
    }

    public Location generateBossRoom(World world, Location center, int size) {
        // Créer une grande salle circulaire pour le boss
        for (int x = -size/2; x <= size/2; x++) {
            for (int z = -size/2; z <= size/2; z++) {
                double distance = Math.sqrt(x*x + z*z);
                Location loc = center.clone().add(x, 0, z);

                if (distance <= size/2) {
                    if (distance >= size/2 - 1) {
                        // Murs circulaires
                        world.getBlockAt(loc).setType(Material.OBSIDIAN);
                        world.getBlockAt(loc.clone().add(0, 1, 0)).setType(Material.OBSIDIAN);
                        world.getBlockAt(loc.clone().add(0, 2, 0)).setType(Material.OBSIDIAN);
                        world.getBlockAt(loc.clone().add(0, 3, 0)).setType(Material.OBSIDIAN);
                        world.getBlockAt(loc.clone().add(0, 4, 0)).setType(Material.OBSIDIAN);
                    } else {
                        // Sol de la salle
                        world.getBlockAt(loc).setType(Material.BLACKSTONE);
                        for (int y = 1; y <= 4; y++) {
                            world.getBlockAt(loc.clone().add(0, y, 0)).setType(Material.AIR);
                        }
                    }
                }
            }
        }

        // Placer des piliers décoratifs
        placeBossRoomPillars(world, center, size);

        // Retourner la position du boss au centre
        return center.clone().add(0, 1, 0);
    }

    public void generatePuzzleRoom(World world, Location center, int size) {
        // Créer une salle avec un puzzle de pression plates
        for (int x = -size/2; x <= size/2; x++) {
            for (int z = -size/2; z <= size/2; z++) {
                Location loc = center.clone().add(x, 0, z);

                if (x == -size/2 || x == size/2 || z == -size/2 || z == size/2) {
                    // Murs
                    world.getBlockAt(loc).setType(Material.COBBLESTONE);
                    world.getBlockAt(loc.clone().add(0, 1, 0)).setType(Material.COBBLESTONE);
                    world.getBlockAt(loc.clone().add(0, 2, 0)).setType(Material.COBBLESTONE);
                    world.getBlockAt(loc.clone().add(0, 3, 0)).setType(Material.COBBLESTONE);
                } else {
                    // Sol
                    world.getBlockAt(loc).setType(Material.STONE);
                    world.getBlockAt(loc.clone().add(0, 1, 0)).setType(Material.AIR);
                    world.getBlockAt(loc.clone().add(0, 2, 0)).setType(Material.AIR);
                }
            }
        }

        // Ajouter des pressure plates pour le puzzle
        addPuzzleElements(world, center, size);
    }

    private void addRoomDecoration(World world, Location center, int size) {
        // Ajouter des torches
        Location torch1 = center.clone().add(-size/2 + 1, 2, 0);
        Location torch2 = center.clone().add(size/2 - 1, 2, 0);
        Location torch3 = center.clone().add(0, 2, -size/2 + 1);
        Location torch4 = center.clone().add(0, 2, size/2 - 1);

        world.getBlockAt(torch1).setType(Material.TORCH);
        world.getBlockAt(torch2).setType(Material.TORCH);
        world.getBlockAt(torch3).setType(Material.TORCH);
        world.getBlockAt(torch4).setType(Material.TORCH);
    }

    private void placeBossRoomPillars(World world, Location center, int size) {
        int pillarDistance = size/3;

        Location[] pillarPositions = {
                center.clone().add(pillarDistance, 0, pillarDistance),
                center.clone().add(-pillarDistance, 0, pillarDistance),
                center.clone().add(pillarDistance, 0, -pillarDistance),
                center.clone().add(-pillarDistance, 0, -pillarDistance)
        };

        for (Location pillarPos : pillarPositions) {
            for (int y = 0; y <= 3; y++) {
                world.getBlockAt(pillarPos.clone().add(0, y, 0)).setType(Material.BLACKSTONE);
            }
            world.getBlockAt(pillarPos.clone().add(0, 4, 0)).setType(Material.TORCH);
        }
    }

    private void addPuzzleElements(World world, Location center, int size) {
        // Créer un pattern de pressure plates
        List<Location> platePositions = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int x = random.nextInt(size - 2) - (size/2 - 1);
            int z = random.nextInt(size - 2) - (size/2 - 1);

            Location plateLoc = center.clone().add(x, 1, z);
            world.getBlockAt(plateLoc).setType(Material.STONE_PRESSURE_PLATE);
            platePositions.add(plateLoc);
        }
    }
}