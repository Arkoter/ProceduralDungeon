package fr.arkoter.proceduraldungeons.utils;

import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtils {

    public static String locationToString(Location location) {
        if (location == null) return null;

        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    public static Location stringToLocation(String locationString) {
        if (locationString == null || locationString.isEmpty()) return null;

        try {
            String[] parts = locationString.split(",");
            if (parts.length != 6) return null;

            World world = org.bukkit.Bukkit.getWorld(parts[0]);
            if (world == null) return null;

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);

            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isLocationSafe(Location location) {
        if (location == null || location.getWorld() == null) return false;

        // Vérifier que la location n'est pas dans le vide
        if (location.getY() < 0 || location.getY() > 256) return false;

        // Vérifier qu'il y a de l'air pour le joueur
        Location checkLoc = location.clone();
        return checkLoc.getBlock().getType().isAir() &&
                checkLoc.add(0, 1, 0).getBlock().getType().isAir();
    }

    public static Location findSafeLocation(Location center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -10; y <= 10; y++) {
                    Location testLoc = center.clone().add(x, y, z);
                    if (isLocationSafe(testLoc)) {
                        return testLoc;
                    }
                }
            }
        }
        return center; // Fallback vers le centre si aucune location sûre trouvée
    }
}