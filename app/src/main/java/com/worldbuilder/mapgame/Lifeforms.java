package com.worldbuilder.mapgame;

import java.security.SecureRandom;
import java.util.Random;

public class Lifeforms {
    public static int[] animalDrawables = {
            R.drawable.goat,
            R.drawable.pig,
            R.drawable.cat,
            R.drawable.monkey,
            R.drawable.greyanimal
            // ...
    };

    // Plant drawable resource IDs
    public static int[] plantDrawables = {
            R.drawable.yellowcactus,
            R.drawable.grass,
            R.drawable.blueflower,
            R.drawable.redflower,
            R.drawable.sprout
            // ...
    };

    public static int genericPlantDrawable = R.drawable.grass;
    public static int genericAnimalDrawable = R.drawable.cat;

    public static int getRandomPlantDrawable() {
        Random random = new SecureRandom();
        int drawableResId = random.nextInt(plantDrawables.length);
        return plantDrawables[drawableResId];
    }

    public static int getRandomAnimalDrawable() {
        Random random = new SecureRandom();
        int drawableResId = random.nextInt(animalDrawables.length);
        return animalDrawables[drawableResId];
    }

    private Lifeforms() {
        //prevent more instances of this object
    }
}
