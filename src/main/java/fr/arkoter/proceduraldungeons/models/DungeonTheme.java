package fr.arkoter.proceduraldungeons.models;

import org.bukkit.Material;

public enum DungeonTheme {
    MEDIEVAL("Médiéval", Material.STONE_BRICKS, Material.COBBLESTONE, Material.OAK_PLANKS,
            "§7Un donjon classique en pierre", "§7avec des torches et du bois"),

    NETHER("Nether", Material.NETHER_BRICKS, Material.BLACKSTONE, Material.CRIMSON_PLANKS,
            "§cDonjon infernal avec de la lave", "§cet des briques du Nether"),

    OCEAN("Océanique", Material.PRISMARINE, Material.DARK_PRISMARINE, Material.PRISMARINE_BRICKS,
            "§bDonjon sous-marin en prismarine", "§bavec des gardiens marins"),

    DESERT("Désert", Material.SANDSTONE, Material.SMOOTH_SANDSTONE, Material.CUT_SANDSTONE,
            "§eDonjon de grès dans le désert", "§eavec des momies et scorpions"),

    ICE("Glace", Material.PACKED_ICE, Material.BLUE_ICE, Material.SNOW_BLOCK,
            "§bDonjon gelé avec de la glace", "§bet des créatures polaires"),

    END("End", Material.END_STONE, Material.END_STONE_BRICKS, Material.PURPUR_BLOCK,
            "§dDonjon de l'End mystérieux", "§davec des Endermen et Shulkers"),

    JUNGLE("Jungle", Material.MOSSY_STONE_BRICKS, Material.MOSSY_COBBLESTONE, Material.JUNGLE_WOOD,
            "§aDonjon envahi par la végétation", "§aavec des pièges naturels"),

    STEAMPUNK("Steampunk", Material.COPPER_BLOCK, Material.IRON_BLOCK, Material.REDSTONE_BLOCK,
            "§6Donjon mécanique en cuivre", "§6avec des mécanismes complexes");

    private final String displayName;
    private final Material floorMaterial;
    private final Material wallMaterial;
    private final Material decorationMaterial;
    private final String[] description;

    DungeonTheme(String displayName, Material floorMaterial, Material wallMaterial,
                 Material decorationMaterial, String... description) {
        this.displayName = displayName;
        this.floorMaterial = floorMaterial;
        this.wallMaterial = wallMaterial;
        this.decorationMaterial = decorationMaterial;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public Material getFloorMaterial() { return floorMaterial; }
    public Material getWallMaterial() { return wallMaterial; }
    public Material getDecorationMaterial() { return decorationMaterial; }
    public String[] getDescription() { return description; }
    public Material getIcon() { return floorMaterial; }
}