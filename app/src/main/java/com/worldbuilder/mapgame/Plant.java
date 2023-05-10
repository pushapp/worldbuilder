package com.worldbuilder.mapgame;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
    public void update(Tile[][] map, World world, Context context) {
        // Implement plant-specific behavior, like growth or spreading
        Log.d("Debug", "update Called");
        if (propCounter == MapUtils.resolution) {
            world.setDarwinPoints(world.getDarwinPoints() + 1);
            incrementAge();
            spread(map, world, context);
            propCounter = 1;
        }
        propCounter++;
    }

    private void spread(Tile[][] map, World world, Context context) {
        Log.d("Debug", "spread() for plant [" + getLifeFormID() + "] called");
        Random random = new Random();
        int rand = random.nextInt(100);
        if (rand < propagationRate) {
            Log.d("Debug", "Propagation successful");
            Position newPos = MapUtils.findPlantSproutingPosition(this, map, seedingDist, false);
            Log.d("Debug", "New position: " + (newPos != null ? newPos.toString() : "null"));
            if (newPos != null) {
                Plant plant = new Plant(name, camouflage, lifespan, newPos, propagationRate, seedingDist, imgID, habitat, getLifeFormID());
                ImageView newPlantImageView = new ImageView(context);
                newPlantImageView.setImageResource(plant.imgID);
                int xPosition = MapUtils.calculateXPosition(newPos.getX());
                int yPosition = MapUtils.calculateYPosition(newPos.getY());

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getImgSize(), getImgSize());
                layoutParams.leftMargin = xPosition;
                layoutParams.topMargin = yPosition;
                newPlantImageView.setLayoutParams(layoutParams);

                plant.setImageView(newPlantImageView);
                world.addLifeform(plant);
                map[newPos.getX()][newPos.getY()].setInHabitant(plant);
                world.getMapView().addView(newPlantImageView); // Add the ImageView to the layout
            }
        }
    }

    // Plant-specific methods, if any
    // ...
}