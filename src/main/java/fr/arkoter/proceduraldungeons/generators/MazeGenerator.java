package fr.arkoter.proceduraldungeons.generators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class MazeGenerator {

    private final int width;
    private final int height;
    private final boolean[][] maze;
    private final Random random;
    private final boolean[][] visited;

    // Directions : Nord, Est, Sud, Ouest
    private final int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

    public MazeGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        this.maze = new boolean[height][width];
        this.visited = new boolean[height][width];
        this.random = new Random();
    }

    public MazeGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.maze = new boolean[height][width];
        this.visited = new boolean[height][width];
        this.random = new Random(seed);
    }

    public boolean[][] generate() {
        // Initialiser le labyrinthe avec des murs (false = mur, true = passage)
        initializeMaze();

        // Générer le labyrinthe avec l'algorithme de backtracking récursif
        generateMaze(1, 1);

        // Créer des ouvertures supplémentaires pour plus de connectivité
        addExtraConnections();

        // S'assurer qu'il y a une entrée et une sortie
        createEntranceAndExit();

        return maze;
    }

    private void initializeMaze() {
        // Tout est initialement un mur
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                maze[y][x] = false;
                visited[y][x] = false;
            }
        }
    }

    private void generateMaze(int startX, int startY) {
        Stack<int[]> stack = new Stack<>();
        int[] current = {startX, startY};

        // Marquer la case de départ comme visitée et passage
        visited[current[1]][current[0]] = true;
        maze[current[1]][current[0]] = true;
        stack.push(current);

        while (!stack.isEmpty()) {
            current = stack.peek();
            List<int[]> neighbors = getUnvisitedNeighbors(current[0], current[1]);

            if (!neighbors.isEmpty()) {
                // Choisir un voisin aléatoire
                int[] next = neighbors.get(random.nextInt(neighbors.size()));

                // Supprimer le mur entre la cellule actuelle et le voisin
                removeWallBetween(current[0], current[1], next[0], next[1]);

                // Marquer le voisin comme visité
                visited[next[1]][next[0]] = true;
                maze[next[1]][next[0]] = true;

                stack.push(next);
            } else {
                // Backtrack si aucun voisin non visité
                stack.pop();
            }
        }
    }

    private List<int[]> getUnvisitedNeighbors(int x, int y) {
        List<int[]> neighbors = new ArrayList<>();

        for (int[] dir : directions) {
            int newX = x + dir[1] * 2; // Multiplier par 2 pour sauter le mur
            int newY = y + dir[0] * 2;

            if (isValidCell(newX, newY) && !visited[newY][newX]) {
                neighbors.add(new int[]{newX, newY});
            }
        }

        // Mélanger pour un choix aléatoire
        Collections.shuffle(neighbors, random);
        return neighbors;
    }

    private void removeWallBetween(int x1, int y1, int x2, int y2) {
        int wallX = (x1 + x2) / 2;
        int wallY = (y1 + y2) / 2;

        if (isValidCell(wallX, wallY)) {
            maze[wallY][wallX] = true;
        }
    }

    private boolean isValidCell(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private void addExtraConnections() {
        // Ajouter quelques connexions supplémentaires pour éviter un labyrinthe trop linéaire
        int extraConnections = (width * height) / 100; // 1% de connexions extra

        for (int i = 0; i < extraConnections; i++) {
            int x = random.nextInt(width - 2) + 1;
            int y = random.nextInt(height - 2) + 1;

            // Si c'est un mur et qu'il y a des passages adjacents
            if (!maze[y][x] && hasAdjacentPassages(x, y)) {
                maze[y][x] = true;
            }
        }
    }

    private boolean hasAdjacentPassages(int x, int y) {
        int passageCount = 0;

        for (int[] dir : directions) {
            int newX = x + dir[1];
            int newY = y + dir[0];

            if (isValidCell(newX, newY) && maze[newY][newX]) {
                passageCount++;
            }
        }

        return passageCount >= 2;
    }

    private void createEntranceAndExit() {
        // Créer une entrée en haut
        if (height > 0) {
            maze[0][1] = true; // Entrée
        }

        // Créer une sortie en bas
        if (height > 1) {
            maze[height - 1][width - 2] = true; // Sortie
        }

        // S'assurer qu'il y a un chemin vers l'entrée et la sortie
        if (height > 1) {
            maze[1][1] = true; // Connexion à l'entrée
        }
        if (height > 2) {
            maze[height - 2][width - 2] = true; // Connexion à la sortie
        }
    }

    // Méthodes utilitaires supplémentaires

    public boolean[][] generateWithRooms(int roomCount, int minRoomSize, int maxRoomSize) {
        // Générer le labyrinthe de base
        generate();

        // Ajouter des salles
        for (int i = 0; i < roomCount; i++) {
            addRandomRoom(minRoomSize, maxRoomSize);
        }

        return maze;
    }

    private void addRandomRoom(int minSize, int maxSize) {
        int roomWidth = random.nextInt(maxSize - minSize + 1) + minSize;
        int roomHeight = random.nextInt(maxSize - minSize + 1) + minSize;

        // Trouver une position valide pour la salle
        int attempts = 50;
        for (int i = 0; i < attempts; i++) {
            int roomX = random.nextInt(width - roomWidth - 2) + 1;
            int roomY = random.nextInt(height - roomHeight - 2) + 1;

            if (canPlaceRoom(roomX, roomY, roomWidth, roomHeight)) {
                placeRoom(roomX, roomY, roomWidth, roomHeight);
                break;
            }
        }
    }

    private boolean canPlaceRoom(int x, int y, int roomWidth, int roomHeight) {
        // Vérifier qu'on ne chevauche pas avec des passages existants
        for (int ry = y; ry < y + roomHeight; ry++) {
            for (int rx = x; rx < x + roomWidth; rx++) {
                if (!isValidCell(rx, ry)) return false;
            }
        }
        return true;
    }

    private void placeRoom(int x, int y, int roomWidth, int roomHeight) {
        // Créer la salle
        for (int ry = y; ry < y + roomHeight; ry++) {
            for (int rx = x; rx < x + roomWidth; rx++) {
                maze[ry][rx] = true;
            }
        }

        // Connecter la salle au labyrinthe
        connectRoomToMaze(x, y, roomWidth, roomHeight);
    }

    private void connectRoomToMaze(int roomX, int roomY, int roomWidth, int roomHeight) {
        // Trouver un point de connexion avec le labyrinthe existant
        List<int[]> connectionPoints = new ArrayList<>();

        // Vérifier les bords de la salle
        for (int x = roomX; x < roomX + roomWidth; x++) {
            // Bord haut
            if (roomY > 0 && maze[roomY - 1][x]) {
                connectionPoints.add(new int[]{x, roomY});
            }
            // Bord bas
            if (roomY + roomHeight < height && maze[roomY + roomHeight][x]) {
                connectionPoints.add(new int[]{x, roomY + roomHeight - 1});
            }
        }

        for (int y = roomY; y < roomY + roomHeight; y++) {
            // Bord gauche
            if (roomX > 0 && maze[y][roomX - 1]) {
                connectionPoints.add(new int[]{roomX, y});
            }
            // Bord droit
            if (roomX + roomWidth < width && maze[y][roomX + roomWidth]) {
                connectionPoints.add(new int[]{roomX + roomWidth - 1, y});
            }
        }

        // Faire au moins une connexion
        if (!connectionPoints.isEmpty()) {
            int[] connection = connectionPoints.get(random.nextInt(connectionPoints.size()));
            // La connexion est déjà faite car on a créé la salle
        }
    }

    public void printMaze() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(maze[y][x] ? "  " : "██");
            }
            System.out.println();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isPassage(int x, int y) {
        if (!isValidCell(x, y)) return false;
        return maze[y][x];
    }

    public List<int[]> getDeadEnds() {
        List<int[]> deadEnds = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (maze[y][x] && isDeadEnd(x, y)) {
                    deadEnds.add(new int[]{x, y});
                }
            }
        }

        return deadEnds;
    }

    private boolean isDeadEnd(int x, int y) {
        int openDirections = 0;

        for (int[] dir : directions) {
            int newX = x + dir[1];
            int newY = y + dir[0];

            if (isValidCell(newX, newY) && maze[newY][newX]) {
                openDirections++;
            }
        }

        return openDirections == 1;
    }
}