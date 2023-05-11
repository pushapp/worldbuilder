package com.worldbuilder.mapgame;

import static com.worldbuilder.mapgame.models.map.TerrainType.WATER;

import androidx.annotation.VisibleForTesting;

import com.worldbuilder.mapgame.models.Position;
import com.worldbuilder.mapgame.models.map.TerrainType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MapUtils {
    public static int tileMapDivisor = 10;
    public static int resolution = 10;

    public static boolean arePositionsClose(Position pos1, Position pos2, int distanceThreshold) {
        int deltaX = pos1.getX() - pos2.getX();
        int deltaY = pos1.getY() - pos2.getY();

        int distanceSquared = deltaX * deltaX + deltaY * deltaY;
        int thresholdSquared = distanceThreshold * distanceThreshold;

        return distanceSquared <= thresholdSquared;
    }

    public static Position findNewOffspringPosition(Tile[][] map, Animal parent) {
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        // Shuffle the directions to try them in a random order
        ArrayList<Integer> indices = createIndices(0, dx.length);
        Collections.shuffle(indices);

        for (int i : indices) {
            int value = indices.get(i);
            int newX = parent.getPosition().getX() + dx[value];
            int newY = parent.getPosition().getY() + dy[value];

            if (newX >= 0 && newX < map.length && newY >= 0 && newY < map[0].length) {
                Tile newTile = map[newX][newY];

                if (Math.abs(parent.habitat - newTile.getElevation()) < 20 &&
                        newTile.getTerrainType() != WATER && newTile.getInHabitant() == null) {
                    // Offspring can swim, so it can be placed on water tiles
                    return new Position(newX, newY);
                }
            }
        }
        return null;
    }

    /**
     * Creates integers in range of startIndex : endIndex
     *
     * @param startIndex start index, inclusive
     * @param endIndex   end index, exclusive
     */
    @SuppressWarnings("SameParameterValue")
    @VisibleForTesting
    static ArrayList<Integer> createIndices(int startIndex, int endIndex) {
        ArrayList<Integer> indices = new ArrayList<>();
        assert startIndex < endIndex;

        for (int index = startIndex; index < endIndex; index++) {
            indices.add(index);
        }
        return indices;
    }

    public static Position findPlantSproutingPosition(Plant plant, Tile[][] map, int threshold, boolean isSwimmer) {
        int parentX = plant.getPosition().getX();
        int parentY = plant.getPosition().getY();

        int attempts = 0;
        int maxAttempts = 100; // Maximum number of attempts to find a suitable position

        while (attempts < maxAttempts) {
            int offsetX = ThreadLocalRandom.current().nextInt(-threshold, threshold + 1);
            int offsetY = ThreadLocalRandom.current().nextInt(-threshold, threshold + 1);

            int newX = parentX + offsetX;
            int newY = parentY + offsetY;

            // Check if the new position is within the map boundaries
            if (newX >= 0 && newX < map.length && newY >= 0 && newY < map[0].length) {
                Tile newTile = map[newX][newY];

                // Check that the tile meets the proper conditions
                if (Math.abs(plant.habitat - newTile.getElevation()) < 20 && newTile.getTerrainType() != WATER && newTile.getInHabitant() == null) {
                    // Plant can grow in water, so it can be placed on water tiles
                    return new Position(newX, newY);
                }
            }
            attempts++;
        }

        // If no suitable position was found, return null
        return null;
    }

    public static Position generateRandomPosition(Random random, Tile[][] tilemap, boolean requireWater) {
        int x, y;
        do {
            x = random.nextInt(tilemap.length);
            y = random.nextInt(tilemap[0].length);
        } while ((requireWater && tilemap[x][y].getElevation() >= 0.5f) || (!requireWater && tilemap[x][y].getElevation() < 0.5f));
        return new Position(x, y);
    }

    public static List<Position> generateSurroundingPositions(Position position, Tile[][] map, boolean isSwimmer, int minDistance, int maxDistance) {
        List<Position> validPositions = new ArrayList<>();

        int x = position.getX();
        int y = position.getY();

        for (int i = -maxDistance; i <= maxDistance; i++) {
            for (int j = -maxDistance; j <= maxDistance; j++) {

                int newX = x + i;
                int newY = y + j;

                // Calculate the Euclidean distance from the original position
                double distance = Math.sqrt(i * i + j * j);

                // Check if the new position is within the world's boundaries and within the min/max distance
                if (newX >= 0 && newX < map.length && newY >= 0 && newY < map[0].length && distance >= minDistance && distance <= maxDistance) {
                    Tile tile = map[newX][newY];
                    if (isSwimmer) {
                        if (tile.getTerrainType() == WATER) {
                            validPositions.add(new Position(newX, newY));
                        }
                    } else {
                        if (tile.getTerrainType() != WATER) {
                            validPositions.add(new Position(newX, newY));
                        }
                    }
                }
            }
        }

        return validPositions;
    }

    public static List<Position> getRandomPositions(List<Position> positions, int numPositions) {
        int positionsToReturn = Math.min(numPositions, positions.size());
        List<Position> selectedPositions = new ArrayList<>(positions);
        Collections.shuffle(selectedPositions, new Random());
        return selectedPositions.subList(0, positionsToReturn);
    }

    public static Tile[][] reduceTileArray(Tile[][] originalTiles, int divisor) {
        int reducedWidth = originalTiles.length / divisor;
        int reducedHeight = originalTiles[0].length / divisor;
        Tile[][] reducedTiles = new Tile[reducedWidth][reducedHeight];

        for (int i = 0; i < reducedWidth; i++) {
            for (int j = 0; j < reducedHeight; j++) {
                int totalElevation = 0;
                int[] terrainTypeCounts = new int[TerrainType.values().length];

                for (int x = i * divisor; x < (i + 1) * divisor; x++) {
                    for (int y = j * divisor; y < (j + 1) * divisor; y++) {
                        Tile tile = originalTiles[x][y];
                        totalElevation += tile.getElevation();
                        terrainTypeCounts[tile.getTerrainType().ordinal()]++;
                    }
                }

                int averageElevation = totalElevation / (divisor * divisor);
                int maxTerrainTypeIndex = 0;
                for (int k = 1; k < terrainTypeCounts.length; k++) {
                    if (terrainTypeCounts[k] > terrainTypeCounts[maxTerrainTypeIndex]) {
                        maxTerrainTypeIndex = k;
                    }
                }
                TerrainType dominantTerrainType = TerrainType.values()[maxTerrainTypeIndex];
                reducedTiles[i][j] = new Tile(dominantTerrainType, averageElevation);
            }
        }
        return reducedTiles;
    }

    public static int TiletoPixelX(int xIndex) {
        return (xIndex * Tile.getTileSize()) * tileMapDivisor;
    }

    public static int TiletoPixelY(int yIndex) {
        return (yIndex * Tile.getTileSize()) * tileMapDivisor;
    }
    public static int PixeltoTileX(int xIndex){
        return (xIndex  / Tile.getTileSize()) / tileMapDivisor;
    }
    public static int PixeltoTileY(int yIndex){
        return (yIndex  / Tile.getTileSize()) / tileMapDivisor;
    }

    public static Position findPositionTowardsTarget(Position start, Position end, int speed) {
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double distance = start.distance(end);

        // If the distance is less than or equal to the speed, return a position that is one tile away from the end position.
        if (distance <= speed) {
            double nx = dx / distance * (distance - 1);
            double ny = dy / distance * (distance - 1);

            int newX = start.getX() + (int) Math.round(nx);
            int newY = start.getY() + (int) Math.round(ny);

            return new Position(newX, newY);
        }

        // Normalize the direction vector and scale it by the speed.
        double nx = dx / distance * speed;
        double ny = dy / distance * speed;

        // Calculate the new position coordinates.
        int newX = start.getX() + (int) Math.round(nx);
        int newY = start.getY() + (int) Math.round(ny);

        return new Position(newX, newY);
    }

    public static Position findPositionAwayFromTarget(Position startPosition, Position targetPosition, int speed) {
        // Calculate the vector from the startPosition to the targetPosition
        int xDifference = targetPosition.getX() - startPosition.getX();
        int yDifference = targetPosition.getY() - startPosition.getY();

        // Calculate the distance between the startPosition and the targetPosition
        double distance = Math.sqrt(xDifference * xDifference + yDifference * yDifference);

        // Normalize the vector (divide by the distance) and multiply by speed to get the movement vector
        double xMove = (xDifference / distance) * speed;
        double yMove = (yDifference / distance) * speed;

        // Invert the movement vector (move away from target) and add it to the startPosition
        int newX = (int) (startPosition.getX() - xMove);
        int newY = (int) (startPosition.getY() - yMove);

        // Create a new Position object and return it
        return new Position(newX, newY);
    }
}
