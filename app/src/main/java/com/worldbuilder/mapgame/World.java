package com.worldbuilder.mapgame;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class World {
    private final static String TAG = World.class.getSimpleName();

    private final Tile[][] map;
    private List<Plant> plants;
    private List<Animal> animals;
    private int darwinPoints = 1000;
    //TODO: remove Context related things from this "controller"
    private final RelativeLayout mapView;

    public World(Tile[][] map, RelativeLayout mapView) {
        this.map = map;
        this.plants = new ArrayList<>();
        this.animals = new ArrayList<>();
        this.mapView = mapView;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public int getDarwinPoints() {
        return darwinPoints;
    }

    public void setDarwinPoints(int darwinPoints) {
        this.darwinPoints = darwinPoints;
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    public List<Animal> getAnimals() {
        return animals;
    }

    public void setAnimals(List<Animal> animals) {
        this.animals = animals;
    }

    public RelativeLayout getMapView() {
        return mapView;
    }

    //TODO: remove context (and mapView) asap!
    public synchronized void update(int steps, Context context) {
        Log.d(TAG, "incrementTime");
        for (int step = 0; step < steps; step++) {
            new ArrayList<>(plants).forEach(plant -> plant.update(map, this, context));
            new ArrayList<>(animals).forEach(animal -> animal.update(map, this, context));
            // Remove dead lifeforms (age >= lifespan) from the lists
            removeDead();
        }
    }

    public void addLifeform(Lifeform lifeform) {
        if (lifeform instanceof Plant) {
            Log.d(TAG, "new plant has been added: " + lifeform);
            plants.add((Plant) lifeform);
        } else if (lifeform instanceof Animal) {
            Log.d(TAG, "new animal has been added: " + lifeform);
            animals.add((Animal) lifeform);
        }
    }

    private synchronized void removeDead() {
        Iterator<Plant> plantIterator = plants.iterator();
        while (plantIterator.hasNext()) {
            Plant plant = plantIterator.next();
            if (plant.getAge() >= plant.getLifespan()) {
                removeViewFromMap(plant.getImageView());
                plantIterator.remove();
                map[plant.getPosition().getX()][plant.getPosition().getY()].setInHabitant(null);
            }
        }

        Iterator<Animal> animalIterator = animals.iterator();
        while (animalIterator.hasNext()) {
            Animal animal = animalIterator.next();
            if (animal.getAge() >= animal.getLifespan()) {
                removeViewFromMap(animal.getImageView());
                animalIterator.remove();
                map[animal.getPosition().getX()][animal.getPosition().getY()].setInHabitant(null);
            }
        }
    }

    public void resetLifeforms() {
        animals = new ArrayList<>();
        plants = new ArrayList<>();
    }

    public List<Plant> getNearbyPlants(Position position, int searchRange) {
        List<Plant> nearbyPlants = new ArrayList<>();
        for (Plant plant : plants) {
            if (MapUtils.arePositionsClose(plant.getPosition(), position, searchRange)) {
                nearbyPlants.add(plant);
            }
        }
        return nearbyPlants;
    }

    public List<Animal> getNearbyAnimals(Position position, int searchRange) {
        List<Animal> nearbyAnimals = new ArrayList<>();
        for (Animal animal : animals) {
            if (MapUtils.arePositionsClose(animal.getPosition(), position, searchRange)) {
                nearbyAnimals.add(animal);
            }
        }
        return nearbyAnimals;
    }

    public void removeLifeform(Lifeform lifeform) {
        if (lifeform instanceof Plant) {
            plants.remove(lifeform);
        } else if (lifeform instanceof Animal) {
            animals.remove(lifeform);
        }

        removeViewFromMap(lifeform.getImageView());
        map[lifeform.getPosition().getX()][lifeform.getPosition().getY()].setInHabitant(null);
    }

    private void removeViewFromMap(ImageView imageView) {
        if (imageView != null) {
            mapView.removeView(imageView);
        }
    }
}