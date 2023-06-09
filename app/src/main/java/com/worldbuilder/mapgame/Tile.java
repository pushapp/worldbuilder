package com.worldbuilder.mapgame;

import com.worldbuilder.mapgame.models.map.TerrainType;

import java.util.HashMap;
import java.util.Map;

public class Tile {
    private final TerrainType terrainType;
    private final int elevation;
    private int color;
    private Lifeform inHabitant;

    public Tile(TerrainType terrainType, int elevation) {
        this.terrainType = terrainType;
        this.elevation = elevation;
    }

    public Lifeform getInHabitant() {
        return inHabitant;
    }

    public void setInHabitant(Lifeform inHabitant) {
        this.inHabitant = inHabitant;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public int getElevation() {
        return elevation;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public static int getTileSize() {
        return 2;
    }

    // Convert the Tile object to a Map
    public Map<String, Object> toMap() {
        Map<String, Object> tileMap = new HashMap<>();
        tileMap.put("terrainType", terrainType.name());
        tileMap.put("elevation", elevation);
        tileMap.put("color", color);

        return tileMap;
    }

    // Create a Tile object from a Map
    public static Tile fromMap(Map<String, Object> tileMap) {
        TerrainType terrainType = TerrainType.valueOf((String) tileMap.get("terrainType"));
        int elevation = ((Long) tileMap.get("elevation")).intValue();
        Tile tile = new Tile(terrainType, elevation);

        tile.setColor(((Long) tileMap.get("color")).intValue());

        return tile;
    }


}
