package com.worldbuilder.mapgame;

import android.util.Log;

import com.worldbuilder.mapgame.models.Position;

import java.util.Random;

public class Plant extends Lifeform {

    private int propCounter = 1;
    private final int seedingDist;

    public Plant(String name, float camouflage, int lifespan, Position position, int propagationRate, int seedingDist, int imgID, int habitat, int lifeFormID) {
        super(name, camouflage, lifespan, position, propagationRate, imgID, habitat, lifeFormID);
        this.seedingDist = seedingDist;
    }

    @Override
    public void update(Tile[][] map, World world, LifeformChangeListener listener) {
        // Implement plant-specific behavior, like growth or spreading
        Log.d("Debug", "update Called");
        if (propCounter == MapUtils.resolution) {
            world.setDarwinPoints(world.getDarwinPoints() + 1);
            incrementAge();
            spread(map, listener);
            propCounter = 1;
        }
        propCounter++;
    }

    private void spread(Tile[][] map, LifeformChangeListener listener) {
        Log.d("Debug", "spread() for plant [" + getLifeFormID() + "] called");
        Random random = new Random();
        int rand = random.nextInt(100);
        if (rand < propagationRate) {
            Log.d("Debug", "Propagation successful");
            Position newPos = MapUtils.findPlantSproutingPosition(this, map, seedingDist, false);
            Log.d("Debug", "New position: " + (newPos != null ? newPos.toString() : "null"));
            if (newPos != null) {
                Plant plant = new Plant(name, camouflage, lifespan, newPos, propagationRate, seedingDist, imgID, habitat, getLifeFormID());
                listener.onLifeFormCreated(plant);
                map[newPos.getX()][newPos.getY()].setInHabitant(plant);
            }
        }
    }

    // Plant-specific methods, if any
    // ...
}