package com.worldbuilder.mapgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class MapGenerator {
    private PerlinNoise perlinNoise;
    float waterThreshold = -.15f;      // 40% of the map will be water
    float beachThreshold = 0f;
    float forestThreshold = .8f;     // 20% of the map will be forests
    float grasslandThreshold = 1.6f;  // 20% of the map will be grasslands
    float scale = .1f;
    private final int maxElevation = 100;
    private final int minElevation = 0;
    float waterFreq = 0;
    float mountainFreq = 0;

    public Bitmap generateRandomMapBitmap(int mapWidth, int mapHeight, int tileSize, Tile[][] map) {

        // Create an empty Bitmap with the size of the map
        Bitmap mapBitmap = Bitmap.createBitmap(mapWidth * tileSize, mapHeight * tileSize, Bitmap.Config.ARGB_8888);

        // Draw the tiles on the Bitmap using a Canvas
        Canvas canvas = new Canvas(mapBitmap);
        Paint paint = new Paint();

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                paint.setColor(map[x][y].getColor());
                canvas.drawRect(x * tileSize, y * tileSize, (x + 1) * tileSize, (y + 1) * tileSize, paint);
            }
        }

        return mapBitmap;
    }

    public Tile[][] generateRandomMap(int mapWidth, int mapHeight, float waterFreq, float mountainFreq) {
        perlinNoise = new PerlinNoise(System.currentTimeMillis());
        Tile[][] map = new Tile[mapWidth][mapHeight];
        float[][] elevations = new float[mapWidth][mapHeight];
        Tile.TerrainType terrainType = Tile.TerrainType.DESERT;
        float noiseValue = 0;
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                float noiseX = x * scale;
                float noiseY = y * scale;
                noiseValue = perlinNoise.noise(noiseX, noiseY, .03f, mountainFreq) - (waterFreq - .3f);
                if (noiseValue < waterThreshold) {
                    terrainType = Tile.TerrainType.WATER;
                } else if (noiseValue < beachThreshold) {
                    terrainType = Tile.TerrainType.BEACH;
                } else if (noiseValue < forestThreshold) {
                    terrainType = Tile.TerrainType.FOREST;
                } else if (noiseValue < grasslandThreshold) {
                    terrainType = Tile.TerrainType.GRASSLAND;
                } else {
                    terrainType = Tile.TerrainType.MOUNTAIN;
                }

                // Calculate elevation
                elevations[x][y] = ((noiseValue - waterThreshold) * (maxElevation - minElevation));

                // Create and store the Tile object
                map[x][y] = new Tile(terrainType, (int) elevations[x][y]);
            }
        }

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                float[] slopeAndAspect = calculateSlopeAndAspect(elevations, x, y);
                float slope = slopeAndAspect[0];
                float aspect = slopeAndAspect[1];

                int baseColor = Color.rgb(244, 164, 96); // RGB values for sandy brown
                if (map[x][y].getTerrainType() != Tile.TerrainType.DESERT) {
                    baseColor = getColorFromElevation(elevations[x][y]);
                }
                int shadedColor = applyHillshading(baseColor, elevations[x][y], slope, aspect);

                map[x][y].setColor(shadedColor);
            }
        }

        return map;
    }

    private float[] calculateSlopeAndAspect(float[][] elevations, int x, int y) {
        float dx = ((x > 0 ? elevations[x - 1][y] : elevations[x][y]) - (x < elevations.length - 1 ? elevations[x + 1][y] : elevations[x][y])) / 2.0f;
        float dy = ((y > 0 ? elevations[x][y - 1] : elevations[x][y]) - (y < elevations[x].length - 1 ? elevations[x][y + 1] : elevations[x][y])) / 2.0f;

        float slope = (float) Math.sqrt(dx * dx + dy * dy);
        float aspect = (float) Math.atan2(dy, dx);

        return new float[]{slope, aspect};
    }

    private int applyHillshading(int baseColor, float normalizedElevation, float slope, float aspect) {

        if (normalizedElevation < waterThreshold * maxElevation) {
            return baseColor;
        }
        float lightAzimuth = 200.0f; // Light source direction in degrees
        float lightElevation = 20.0f; // Light source elevation angle in degrees
        float lightIntensity = 0.8f; // Light intensity, range from 0 to 1

        float lightAzimuthRadians = (float) Math.toRadians(lightAzimuth);
        float lightElevationRadians = (float) Math.toRadians(lightElevation);

        float lightX = (float) (Math.cos(lightAzimuthRadians) * Math.cos(lightElevationRadians));
        float lightY = (float) (Math.sin(lightAzimuthRadians) * Math.cos(lightElevationRadians));
        float lightZ = (float) Math.sin(lightElevationRadians);

        float dotProduct = lightX * slope * (float) Math.cos(aspect) + lightY * slope * (float) Math.sin(aspect) + lightZ * (1 - slope);
        float shade = Math.max(0, dotProduct) * lightIntensity;

        return Color.argb(
                255,
                (int) (Color.red(baseColor) * (1 - shade)),
                (int) (Color.green(baseColor) * (1 - shade)),
                (int) (Color.blue(baseColor) * (1 - shade))
        );
    }

    private int getColorFromElevation(float normalizedElevation) {
        int colorBlend = 20;
        int lightBrown = Color.rgb(210, 180, 140); // RGB values for light brown
        int forestGreen = Color.rgb(34, 139, 34); // RGB values for forest green
        int grasslandGreen = Color.rgb(124, 252, 0); // RGB values for grassland green
        int highElevationColor = Color.rgb(169, 169, 169); // RGB values for high elevation gray
        int[] beachToForestColors = generateColorShades(lightBrown, forestGreen, colorBlend);
        int[] forestToGrasslandColors = generateColorShades(forestGreen, grasslandGreen, colorBlend);
        int[] grasslandToHighElevationColors = generateColorShades(grasslandGreen, highElevationColor, colorBlend);

        if (normalizedElevation < waterThreshold * maxElevation) {
            return Color.BLUE; // Water
        } else {
            float landElevation = normalizedElevation - waterThreshold * maxElevation;
            float landRange = maxElevation - waterThreshold * maxElevation;

            if (landElevation < (beachThreshold - waterThreshold) * maxElevation) {
                int index = (int) ((landElevation / ((beachThreshold - waterThreshold) * maxElevation)) * colorBlend);
                return beachToForestColors[index];
            } else if (landElevation < (forestThreshold - beachThreshold) * maxElevation) {
                int index = (int) (((landElevation - (beachThreshold - waterThreshold) * maxElevation) / ((forestThreshold - beachThreshold) * maxElevation)) * colorBlend);
                return forestToGrasslandColors[index];
            } else if (landElevation < (grasslandThreshold - forestThreshold) * maxElevation) {
                int index = (int) (((landElevation - (forestThreshold - waterThreshold) * maxElevation) / ((grasslandThreshold - forestThreshold) * maxElevation)) * colorBlend);
                if (index < 0) {
                    return highElevationColor;
                }
                return grasslandToHighElevationColors[index];
            } else {
                return highElevationColor; // Mountain
            }
        }
    }

    public int[] generateColorShades(int color1, int color2, int numShades) {
        int[] shades = new int[numShades];

        int startRed = Color.red(color1);
        int startGreen = Color.green(color1);
        int startBlue = Color.blue(color1);

        int endRed = Color.red(color2);
        int endGreen = Color.green(color2);
        int endBlue = Color.blue(color2);

        for (int i = 0; i < numShades; i++) {
            float t = (float) i / (numShades - 1);
            int red = (int) (startRed + t * (endRed - startRed));
            int green = (int) (startGreen + t * (endGreen - startGreen));
            int blue = (int) (startBlue + t * (endBlue - startBlue));

            shades[i] = Color.rgb(red, green, blue);
        }

        return shades;
    }

}

