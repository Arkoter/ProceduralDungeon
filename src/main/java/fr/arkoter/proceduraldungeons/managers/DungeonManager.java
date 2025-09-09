package fr.arkoter.proceduraldungeons.managers;

import fr.arkoter.proceduraldungeons.ProceduralDungeons;
import fr.arkoter.proceduraldungeons.models.Dungeon;
import fr.arkoter.proceduraldungeons.models.DungeonPlayer;
import fr.arkoter.proceduraldungeons.models.DungeonTemplate;
import fr.arkoter.proceduraldungeons.models.DungeonTheme;
import fr.arkoter.proceduraldungeons.generators.MazeGenerator;
import fr.arkoter.proceduraldungeons.generators.RoomGenerator;
import fr.arkoter.proceduraldungeons.generators.TrapGenerator;
import fr.arkoter.proceduraldungeons.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonManager {

    private final ProceduralDungeons plugin;
    private final Map<String, Dungeon> dungeons;
    private final Map<UUID, DungeonPlayer> dungeonPlayers;
    private final Map<String, UUID> dungeonOwners;
    private final Random random;
    private final RoomGenerator roomGenerator;
    private final TrapGenerator trapGenerator;

    public DungeonManager(ProceduralDungeons plugin) {
        this.plugin = plugin;
        this.dungeons = new ConcurrentHashMap<>();
        this.dungeonPlayers = new ConcurrentHashMap<>();
        this.dungeonOwners = new ConcurrentHashMap<>();
        this.random = new Random();
        this.roomGenerator = new RoomGenerator();
        this.trapGenerator = new TrapGenerator();

        // Charger les donjons et joueurs depuis les fichiers
        loadData();
    }

    // ================================
    // CRÉATION DE DONJONS
    // ================================

    public void createDungeon(Player player, String name) {
        createDungeon(player, name, plugin.getConfigManager().getDefaultSize(),
                plugin.getConfigManager().getDefaultDifficulty());
    }

    public void createDungeon(Player player, String name, int size, int difficulty) {
        if (dungeons.containsKey(name)) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.already-exists", "{name}", name));
            return;
        }

        // Vérifications
        if (size < plugin.getConfigManager().getMinDungeonSize()) {
            player.sendMessage(MessageUtils.getMessage("messages.error.dungeon-too-small",
                    "{min}", String.valueOf(plugin.getConfigManager().getMinDungeonSize())));
            return;
        }

        if (size > plugin.getConfigManager().getMaxDungeonSize()) {
            player.sendMessage(MessageUtils.getMessage("messages.error.dungeon-too-large",
                    "{max}", String.valueOf(plugin.getConfigManager().getMaxDungeonSize())));
            return;
        }

        if (getOwnedDungeonsCount(player.getUniqueId()) >= getMaxDungeonsPerPlayer()) {
            player.sendMessage(MessageUtils.getMessage("messages.error.max-dungeons-reached",
                    "{max}", String.valueOf(getMaxDungeonsPerPlayer())));
            return;
        }

        Location location = player.getLocation();
        size = Math.max(plugin.getConfigManager().getMinDungeonSize(),
                Math.min(size, plugin.getConfigManager().getMaxDungeonSize()));
        difficulty = Math.max(1, Math.min(difficulty, plugin.getConfigManager().getMaxDifficulty()));

        player.sendMessage(MessageUtils.getMessage("messages.dungeon.creating", "{name}", name));

        // Générer le donjon de manière asynchrone
        new BukkitRunnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();

                Dungeon dungeon = new Dungeon(name, location, size, difficulty);
                generateDungeonStructure(dungeon);

                // Revenir au thread principal pour finaliser
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        dungeons.put(name, dungeon);
                        dungeonOwners.put(name, player.getUniqueId());
                        plugin.getDungeonData().saveDungeon(dungeon);

                        long duration = System.currentTimeMillis() - startTime;
                        player.sendMessage(MessageUtils.getMessage("messages.dungeon.created", "{name}", name));
                        player.sendMessage(MessageUtils.getMessage("messages.dungeon.generation-complete",
                                "{time}", String.valueOf(duration)));

                        plugin.logPerformance("Dungeon creation: " + name, startTime);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void createCustomDungeon(Player player, DungeonTemplate template) {
        if (dungeons.containsKey(template.getName())) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.already-exists",
                    "{name}", template.getName()));
            return;
        }

        Location location = player.getLocation();
        player.sendMessage(MessageUtils.getMessage("messages.dungeon.creating",
                "{name}", template.getName()));

        new BukkitRunnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();

                Dungeon dungeon = new Dungeon(template.getName(), location,
                        template.getSize(), template.getDifficulty());
                generateCustomDungeonStructure(dungeon, template);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        dungeons.put(template.getName(), dungeon);
                        dungeonOwners.put(template.getName(), player.getUniqueId());
                        plugin.getDungeonData().saveDungeon(dungeon);

                        long duration = System.currentTimeMillis() - startTime;
                        player.sendMessage(MessageUtils.getMessage("messages.wizard.dungeon-created",
                                "{name}", template.getName()));
                        player.sendMessage(MessageUtils.getMessage("messages.dungeon.generation-complete",
                                "{time}", String.valueOf(duration)));

                        plugin.logPerformance("Custom dungeon creation: " + template.getName(), startTime);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    // ================================
    // GÉNÉRATION DE STRUCTURE
    // ================================

    private void generateDungeonStructure(Dungeon dungeon) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();
        int size = dungeon.getSize();

        plugin.debug("Generating maze for dungeon: " + dungeon.getName());

        // Générer le labyrinthe
        long seed = plugin.getConfigManager().getGenerationSeed();
        if (seed == 0) seed = System.currentTimeMillis();

        MazeGenerator mazeGen = new MazeGenerator(size, size, seed);
        boolean[][] maze = mazeGen.generate();

        // Construire les murs et sols du labyrinthe
        buildMazeStructure(world, center, maze, size, DungeonTheme.MEDIEVAL);

        // Générer les salles spéciales
        generateSpecialRooms(dungeon, maze);

        // Placer le boss
        placeBossRoom(dungeon, maze);

        // Ajouter des pièges
        trapGenerator.generateTraps(dungeon, maze);

        // Spawner les monstres initiaux
        spawnInitialMonsters(dungeon, maze);
    }

    private void generateCustomDungeonStructure(Dungeon dungeon, DungeonTemplate template) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();
        int size = dungeon.getSize();

        plugin.debug("Generating custom dungeon: " + dungeon.getName() + " with theme: " + template.getTheme());

        // Générer le labyrinthe
        long seed = plugin.getConfigManager().getGenerationSeed();
        if (seed == 0) seed = System.currentTimeMillis();

        MazeGenerator mazeGen = new MazeGenerator(size, size, seed);
        boolean[][] maze = mazeGen.generateWithRooms(template.getTotalSpecialRooms(),
                plugin.getConfigManager().getMinRoomSize(),
                plugin.getConfigManager().getMaxRoomSize());

        // Construire avec le thème sélectionné
        buildMazeStructure(world, center, maze, size, template.getTheme());

        // Générer les salles selon le template
        generateTemplateRooms(dungeon, template, maze);

        // Placer le boss si demandé
        if (template.hasBossRoom()) {
            placeBossRoom(dungeon, maze);
        }

        // Ajouter des pièges selon la configuration
        trapGenerator.generateTraps(dungeon, maze);

        // Spawner les monstres sélectionnés
        spawnCustomMonsters(dungeon, template, maze);
    }

    private void buildMazeStructure(World world, Location center, boolean[][] maze, int size, DungeonTheme theme) {
        Material floorMaterial = theme.getFloorMaterial();
        Material wallMaterial = theme.getWallMaterial();
        Material decorationMaterial = theme.getDecorationMaterial();

        int wallHeight = plugin.getConfigManager().getWallHeight();

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Location blockLoc = center.clone().add(x - size/2, 0, z - size/2);

                if (maze[x][z]) {
                    // Sol du donjon (passage)
                    world.getBlockAt(blockLoc).setType(floorMaterial);

                    // Nettoyer l'air au-dessus
                    for (int y = 1; y < wallHeight; y++) {
                        world.getBlockAt(blockLoc.clone().add(0, y, 0)).setType(Material.AIR);
                    }

                    // Plafond/toit
                    world.getBlockAt(blockLoc.clone().add(0, wallHeight, 0)).setType(decorationMaterial);

                    // Ajouter de l'éclairage occasionnel
                    if (random.nextInt(20) == 0) {
                        world.getBlockAt(blockLoc.clone().add(0, 2, 0)).setType(Material.TORCH);
                    }
                } else {
                    // Murs du donjon
                    for (int y = 0; y <= wallHeight; y++) {
                        Material material = (y == 0) ? floorMaterial : wallMaterial;
                        world.getBlockAt(blockLoc.clone().add(0, y, 0)).setType(material);
                    }
                }
            }
        }
    }

    private void generateSpecialRooms(Dungeon dungeon, boolean[][] maze) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();
        int roomCount = random.nextInt(plugin.getConfigManager().getMaxSpecialRooms() -
                plugin.getConfigManager().getMinSpecialRooms() + 1) +
                plugin.getConfigManager().getMinSpecialRooms();

        for (int i = 0; i < roomCount; i++) {
            int x, z;
            int attempts = 0;
            do {
                x = random.nextInt(maze.length - 10) + 5;
                z = random.nextInt(maze[0].length - 10) + 5;
                attempts++;
            } while (!maze[x][z] && attempts < 50);

            if (attempts < 50) {
                Location roomCenter = center.clone().add(x - maze.length/2, 1, z - maze[0].length/2);

                // Déterminer le type de salle
                int roomType = random.nextInt(3);
                switch (roomType) {
                    case 0: // Salle au trésor
                        List<Location> chests = roomGenerator.generateTreasureRoom(world, roomCenter, 7);
                        for (Location chestLoc : chests) {
                            dungeon.addTreasureChest(chestLoc);
                        }
                        break;

                    case 1: // Salle de combat
                        generateCombatRoom(world, roomCenter, 6);
                        spawnRoomMonsters(world, roomCenter, dungeon.getDifficulty());
                        break;

                    case 2: // Salle puzzle
                        roomGenerator.generatePuzzleRoom(world, roomCenter, 5);
                        break;
                }
            }
        }
    }

    private void generateTemplateRooms(Dungeon dungeon, DungeonTemplate template, boolean[][] maze) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();

        // Générer les salles de trésor
        for (int i = 0; i < template.getTreasureRooms(); i++) {
            Location roomLoc = findValidRoomLocation(center, maze);
            if (roomLoc != null) {
                List<Location> chests = roomGenerator.generateTreasureRoom(world, roomLoc, 7);
                for (Location chestLoc : chests) {
                    dungeon.addTreasureChest(chestLoc);
                }
            }
        }

        // Générer les salles de combat
        for (int i = 0; i < template.getCombatRooms(); i++) {
            Location roomLoc = findValidRoomLocation(center, maze);
            if (roomLoc != null) {
                generateCombatRoom(world, roomLoc, 6);
                spawnTemplateMonsters(world, roomLoc, template);
            }
        }

        // Générer les salles puzzle
        for (int i = 0; i < template.getPuzzleRooms(); i++) {
            Location roomLoc = findValidRoomLocation(center, maze);
            if (roomLoc != null) {
                roomGenerator.generatePuzzleRoom(world, roomLoc, 5);
            }
        }
    }

    private void generateCombatRoom(World world, Location center, int size) {
        // Générer une salle de combat basique
        for (int x = -size/2; x <= size/2; x++) {
            for (int z = -size/2; z <= size/2; z++) {
                Location loc = center.clone().add(x, 0, z);

                // Sol en pierre
                world.getBlockAt(loc).setType(Material.STONE);

                // Nettoyer l'air au-dessus
                for (int y = 1; y <= 4; y++) {
                    world.getBlockAt(loc.clone().add(0, y, 0)).setType(Material.AIR);
                }

                // Murs extérieurs
                if (x == -size/2 || x == size/2 || z == -size/2 || z == size/2) {
                    for (int y = 1; y <= 3; y++) {
                        world.getBlockAt(loc.clone().add(0, y, 0)).setType(Material.COBBLESTONE);
                    }
                }
            }
        }

        // Ajouter quelques torches
        world.getBlockAt(center.clone().add(-size/2 + 1, 2, -size/2 + 1)).setType(Material.TORCH);
        world.getBlockAt(center.clone().add(size/2 - 1, 2, size/2 - 1)).setType(Material.TORCH);
    }

    private Location findValidRoomLocation(Location center, boolean[][] maze) {
        for (int attempts = 0; attempts < 50; attempts++) {
            int x = random.nextInt(maze.length - 10) + 5;
            int z = random.nextInt(maze[0].length - 10) + 5;

            if (maze[x][z]) {
                return center.clone().add(x - maze.length/2, 1, z - maze[0].length/2);
            }
        }
        return null;
    }

    private void placeBossRoom(Dungeon dungeon, boolean[][] maze) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();

        // Placer la salle du boss au fond du donjon
        int bossX = maze.length - 12;
        int bossZ = maze[0].length - 12;

        Location bossRoomCenter = center.clone().add(bossX - maze.length/2, 1, bossZ - maze[0].length/2);
        Location bossLocation = roomGenerator.generateBossRoom(world, bossRoomCenter,
                plugin.getConfigManager().getBossRoomSize());

        dungeon.setBossLocation(bossLocation);
    }

    // ================================
    // GESTION DES MONSTRES
    // ================================

    private void spawnInitialMonsters(Dungeon dungeon, boolean[][] maze) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();

        int monsterCount = (dungeon.getSize() * dungeon.getDifficulty()) / 10;
        monsterCount = Math.min(monsterCount, plugin.getConfigManager().getMaxMonstersPerDungeon());

        EntityType[] monsterTypes = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER};

        for (int i = 0; i < monsterCount; i++) {
            Location spawnLoc = findValidSpawnLocation(center, maze);
            if (spawnLoc != null) {
                EntityType monsterType = monsterTypes[random.nextInt(monsterTypes.length)];
                LivingEntity monster = (LivingEntity) world.spawnEntity(spawnLoc, monsterType);
                enhanceMonster(monster, dungeon.getDifficulty());
            }
        }
    }

    private void spawnCustomMonsters(Dungeon dungeon, DungeonTemplate template, boolean[][] maze) {
        Location center = dungeon.getLocation();
        World world = center.getWorld();

        int monsterCount = (template.getSize() * template.getDifficulty()) / 8;
        monsterCount = Math.min(monsterCount, plugin.getConfigManager().getMaxMonstersPerDungeon());

        EntityType[] monsterTypes = template.getMonsterTypes();

        for (int i = 0; i < monsterCount; i++) {
            Location spawnLoc = findValidSpawnLocation(center, maze);
            if (spawnLoc != null && monsterTypes.length > 0) {
                EntityType monsterType = monsterTypes[random.nextInt(monsterTypes.length)];
                LivingEntity monster = (LivingEntity) world.spawnEntity(spawnLoc, monsterType);
                enhanceMonster(monster, template.getDifficulty());
            }
        }
    }

    private void spawnRoomMonsters(World world, Location roomCenter, int difficulty) {
        EntityType[] roomMonsters = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER};
        int count = 2 + difficulty;

        for (int i = 0; i < count; i++) {
            Location spawnLoc = roomCenter.clone().add(
                    random.nextGaussian() * 3, 0, random.nextGaussian() * 3);

            if (spawnLoc.getBlock().getType().isAir()) {
                EntityType monsterType = roomMonsters[random.nextInt(roomMonsters.length)];
                LivingEntity monster = (LivingEntity) world.spawnEntity(spawnLoc, monsterType);
                enhanceMonster(monster, difficulty);
            }
        }
    }

    private void spawnTemplateMonsters(World world, Location roomCenter, DungeonTemplate template) {
        EntityType[] monsterTypes = template.getMonsterTypes();
        if (monsterTypes.length == 0) return;

        int count = 2 + template.getDifficulty();

        for (int i = 0; i < count; i++) {
            Location spawnLoc = roomCenter.clone().add(
                    random.nextGaussian() * 3, 0, random.nextGaussian() * 3);

            if (spawnLoc.getBlock().getType().isAir()) {
                EntityType monsterType = monsterTypes[random.nextInt(monsterTypes.length)];
                LivingEntity monster = (LivingEntity) world.spawnEntity(spawnLoc, monsterType);
                enhanceMonster(monster, template.getDifficulty());
            }
        }
    }

    private void enhanceMonster(LivingEntity monster, int difficulty) {
        // Augmenter la vie selon la difficulté
        double healthMultiplier = 1.0 + (difficulty * 0.5);
        monster.setMaxHealth(monster.getMaxHealth() * healthMultiplier);
        monster.setHealth(monster.getMaxHealth());

        // Ajouter des équipements/effets selon le type et la difficulté
        // Cette partie peut être étendue selon les besoins
    }

    private Location findValidSpawnLocation(Location center, boolean[][] maze) {
        for (int attempts = 0; attempts < 30; attempts++) {
            int x = random.nextInt(maze.length - 4) + 2;
            int z = random.nextInt(maze[0].length - 4) + 2;

            if (maze[x][z]) {
                Location spawnLoc = center.clone().add(x - maze.length/2, 1, z - maze[0].length/2);
                if (spawnLoc.getBlock().getType().isAir()) {
                    return spawnLoc;
                }
            }
        }
        return null;
    }

    // ================================
    // GESTION DES JOUEURS
    // ================================

    public void enterDungeon(Player player, String name) {
        Dungeon dungeon = dungeons.get(name);
        if (dungeon == null) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.not-found"));
            return;
        }

        DungeonPlayer dungeonPlayer = getDungeonPlayer(player.getUniqueId());

        if (dungeonPlayer.isInDungeon()) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.already-in-dungeon"));
            return;
        }

        // Vérifier si le donjon a atteint sa capacité maximale
        if (dungeon.getActivePlayerCount() >= getMaxPlayersPerDungeon()) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.full"));
            return;
        }

        // Sauvegarder la position de sortie
        dungeonPlayer.setExitLocation(player.getLocation());
        dungeonPlayer.setCurrentDungeon(name);
        dungeonPlayer.setEnterTime(System.currentTimeMillis());

        // Réinitialiser les statistiques de session
        dungeonPlayer.setMonstersKilled(0);
        dungeonPlayer.setTreasuresFound(0);
        dungeonPlayer.setBossKey(false);

        // Téléporter le joueur à l'entrée du donjon
        Location entrance = dungeon.getLocation().clone().add(0, 2, -(dungeon.getSize()/2));
        player.teleport(entrance);

        // Ajouter le joueur au donjon
        dungeon.addActivePlayer(player.getUniqueId().toString());
        dungeon.incrementTimesEntered();

        // Messages
        player.sendMessage(MessageUtils.getMessage("messages.dungeon.entered", "{name}", name));
        player.sendMessage("§eDifficulté: " + dungeon.getDifficulty() + "/10");
        player.sendMessage("§eJoueurs dans le donjon: " + dungeon.getActivePlayerCount());

        // Spawner le boss si il n'existe pas déjà
        if (!dungeon.isBossAlive()) {
            spawnBoss(dungeon);
        }

        // Notifier les autres joueurs
        notifyDungeonPlayers(dungeon, MessageUtils.getMessage("messages.notifications.player-joined-dungeon",
                "{player}", player.getName()), player.getUniqueId());

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

            // Notifier les autres joueurs
            notifyDungeonPlayers(dungeon, MessageUtils.getMessage("messages.notifications.player-left-dungeon",
                    "{player}", player.getName()), player.getUniqueId());

            if (dungeon.getActivePlayerCount() == 0) {
                notifyDungeonPlayers(dungeon, MessageUtils.getMessage("messages.notifications.dungeon-empty"), null);
            }
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

    private void notifyDungeonPlayers(Dungeon dungeon, String message, UUID excludePlayer) {
        for (String playerIdStr : dungeon.getActivePlayers()) {
            try {
                UUID playerId = UUID.fromString(playerIdStr);
                if (excludePlayer != null && playerId.equals(excludePlayer)) continue;

                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(message);
                }
            } catch (IllegalArgumentException e) {
                plugin.debug("Invalid UUID in active players: " + playerIdStr);
            }
        }
    }

    private void spawnBoss(Dungeon dungeon) {
        Location bossLoc = dungeon.getBossLocation();
        if (bossLoc != null) {
            World world = bossLoc.getWorld();

            EntityType bossType = getBossTypeForDifficulty(dungeon.getDifficulty());
            LivingEntity boss = (LivingEntity) world.spawnEntity(bossLoc, bossType);

            // Améliorer le boss
            double healthMultiplier = plugin.getConfigManager().getBossHealthMultiplier();
            boss.setMaxHealth(boss.getMaxHealth() * healthMultiplier * dungeon.getDifficulty());
            boss.setHealth(boss.getMaxHealth());

            // Nom personnalisé pour le boss
            boss.setCustomName("§4Boss du Donjon " + dungeon.getName());
            boss.setCustomNameVisible(true);

            dungeon.setBossAlive(true);

            // Notifier tous les joueurs du donjon
            notifyDungeonPlayers(dungeon, MessageUtils.getMessage("messages.boss.spawned"), null);
        }
    }

    private EntityType getBossTypeForDifficulty(int difficulty) {
        switch (difficulty) {
            case 1: return EntityType.ZOMBIE;
            case 2: return EntityType.SKELETON;
            case 3: return EntityType.WITHER_SKELETON;
            case 4: return EntityType.BLAZE;
            default: return EntityType.WITHER;
        }
    }

    // ================================
    // FONCTIONS AVANCÉES
    // ================================

    public void shareDungeon(Player player, String dungeonName, String targetPlayerName) {
        if (!dungeonOwners.containsKey(dungeonName) ||
                !dungeonOwners.get(dungeonName).equals(player.getUniqueId())) {
            player.sendMessage("§cVous n'êtes pas le propriétaire de ce donjon !");
            return;
        }

        Player targetPlayer = plugin.getServer().getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage("§cJoueur introuvable ou hors ligne !");
            return;
        }

        // Logique de partage - peut être étendue
        player.sendMessage("§aDonjon partagé avec " + targetPlayerName);
        targetPlayer.sendMessage("§e" + player.getName() + " a partagé le donjon '" + dungeonName + "' avec vous !");
    }

    public void copyDungeon(Player player, String sourceName, String newName) {
        Dungeon source = dungeons.get(sourceName);
        if (source == null) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.not-found"));
            return;
        }

        if (dungeons.containsKey(newName)) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.already-exists", "{name}", newName));
            return;
        }

        // Créer une copie
        Dungeon copy = new Dungeon(newName, player.getLocation(), source.getSize(), source.getDifficulty());

        dungeons.put(newName, copy);
        dungeonOwners.put(newName, player.getUniqueId());
        plugin.getDungeonData().saveDungeon(copy);

        player.sendMessage("§aDonjon '" + sourceName + "' copié vers '" + newName + "'");
    }

    // ================================
    // GESTION DES INFORMATIONS
    // ================================

    public void listDungeons(Player player) {
        if (dungeons.isEmpty()) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.no-dungeons"));
            return;
        }

        player.sendMessage("§e=== Liste des donjons (" + dungeons.size() + ") ===");

        for (Dungeon dungeon : dungeons.values()) {
            String status = dungeon.getActivePlayerCount() > 0 ? "§a[Actif]" : "§7[Vide]";
            String owner = dungeonOwners.containsKey(dungeon.getName()) ?
                    plugin.getServer().getOfflinePlayer(dungeonOwners.get(dungeon.getName())).getName() : "Inconnu";

            player.sendMessage("§a- " + dungeon.getName() + " " + status +
                    " §7(Propriétaire: " + owner + ", " +
                    "Difficulté: " + dungeon.getDifficulty() + ", " +
                    "Joueurs: " + dungeon.getActivePlayerCount() + ")");
        }

        // Statistiques personnelles du joueur
        int ownedCount = getOwnedDungeonsCount(player.getUniqueId());
        player.sendMessage("§7Vous possédez §e" + ownedCount + "§7 donjon(s)");
    }

    public void showDungeonInfo(Player player, String name) {
        Dungeon dungeon = dungeons.get(name);
        if (dungeon == null) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.not-found"));
            return;
        }

        String owner = dungeonOwners.containsKey(name) ?
                plugin.getServer().getOfflinePlayer(dungeonOwners.get(name)).getName() : "Inconnu";

        player.sendMessage("§e=== Informations du donjon " + name + " ===");
        player.sendMessage("§7Propriétaire: §f" + owner);
        player.sendMessage("§7Taille: §f" + dungeon.getSize() + "x" + dungeon.getSize());
        player.sendMessage("§7Difficulté: §f" + dungeon.getDifficulty() + "/10");
        player.sendMessage("§7Joueurs actifs: §f" + dungeon.getActivePlayerCount() + "/" + getMaxPlayersPerDungeon());
        player.sendMessage("§7Fois entré: §f" + dungeon.getTimesEntered());
        player.sendMessage("§7Fois complété: §f" + dungeon.getTimesCompleted());
        player.sendMessage("§7Taux de réussite: §f" + String.format("%.1f%%", dungeon.getCompletionRate()));
        player.sendMessage("§7Coffres au trésor: §f" + dungeon.getTreasureChests().size());
        player.sendMessage("§7Pièges: §f" + dungeon.getTraps().size());
        player.sendMessage("§7Record de vitesse: §f" + dungeon.getFormattedFastestCompletion());
        player.sendMessage("§7Créé le: §f" + dungeon.getFormattedCreationDate());
        player.sendMessage("§7Boss vivant: §f" + (dungeon.isBossAlive() ? "§aOui" : "§cNon"));
    }

    public void deleteDungeon(Player player, String name) {
        if (!dungeons.containsKey(name)) {
            player.sendMessage(MessageUtils.getMessage("messages.dungeon.not-found"));
            return;
        }

        // Vérifier les permissions
        UUID owner = dungeonOwners.get(name);
        if (owner != null && !owner.equals(player.getUniqueId()) &&
                !player.hasPermission("proceduraldungeons.admin")) {
            player.sendMessage("§cVous n'êtes pas le propriétaire de ce donjon !");
            return;
        }

        Dungeon dungeon = dungeons.get(name);

        // Faire sortir tous les joueurs du donjon
        for (String playerId : new HashSet<>(dungeon.getActivePlayers())) {
            try {
                UUID uuid = UUID.fromString(playerId);
                Player dungeonPlayer = plugin.getServer().getPlayer(uuid);
                if (dungeonPlayer != null) {
                    leaveDungeon(dungeonPlayer);
                }
            } catch (IllegalArgumentException e) {
                plugin.debug("Invalid UUID when removing player from dungeon: " + playerId);
            }
        }

        dungeons.remove(name);
        dungeonOwners.remove(name);
        plugin.getDungeonData().deleteDungeon(name);

        player.sendMessage(MessageUtils.getMessage("messages.dungeon.deleted", "{name}", name));
    }

    // ================================
    // MÉTHODES DE DONNÉES
    // ================================

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
        if (!location.getWorld().equals(dungeonCenter.getWorld())) return false;

        double distance = location.distance(dungeonCenter);
        double maxDistance = dungeon.getSize() * Math.sqrt(2) / 2; // Rayon du donjon

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

    public int getOwnedDungeonsCount(UUID playerId) {
        return (int) dungeonOwners.values().stream().filter(owner -> owner.equals(playerId)).count();
    }

    private int getMaxDungeonsPerPlayer() {
        return plugin.getConfigManager().getConfig().getInt("limits.max-dungeons-per-player", 5);
    }

    private int getMaxPlayersPerDungeon() {
        return plugin.getConfigManager().getConfig().getInt("limits.max-players-per-dungeon", 10);
    }

    private void loadData() {
        plugin.debug("Loading dungeons and players data...");

        // Charger les donjons
        Map<String, Dungeon> loadedDungeons = plugin.getDungeonData().loadAllDungeons();
        dungeons.putAll(loadedDungeons);

        // Charger les joueurs
        Map<UUID, DungeonPlayer> loadedPlayers = plugin.getPlayerData().loadAllPlayers();
        dungeonPlayers.putAll(loadedPlayers);

        plugin.getLogger().info("Chargés: " + dungeons.size() + " donjons et " + dungeonPlayers.size() + " joueurs");
    }

    public void saveAllData() {
        plugin.debug("Saving all dungeons and players data...");

        plugin.getDungeonData().saveAllDungeons(dungeons);

        for (DungeonPlayer player : dungeonPlayers.values()) {
            plugin.getPlayerData().savePlayer(player);
        }

        plugin.debug("Data saved successfully");
    }

    public void shutdown() {
        plugin.getLogger().info("Shutting down DungeonManager...");

        // Faire sortir tous les joueurs des donjons
        for (DungeonPlayer player : new ArrayList<>(dungeonPlayers.values())) {
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

        plugin.getLogger().info("DungeonManager shutdown complete");
    }
}