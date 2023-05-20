package com.worldbuilder.mapgame;

import android.util.Log;

import androidx.annotation.NonNull;

import com.worldbuilder.mapgame.models.Position;
import com.worldbuilder.mapgame.models.lifeform.LifeformChangeListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class World implements LifeformChangeListener {
    private final static String TAG = World.class.getSimpleName();

    private final Tile[][] map;
    private List<Plant> plants;
    private List<Animal> animals;
    private int darwinPoints = 1000;
    private LifeformChangeListener listener;

    public World(Tile[][] map) {
        this.map = map;
        this.plants = new ArrayList<>();
        this.animals = new ArrayList<>();
    }

    public void setLifeformChangeListener(LifeformChangeListener listener) {
        this.listener = listener;
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

    public Tile[][] getMap() {
        return map;
    }

    public synchronized void update(int steps) {
        Log.d(TAG, "incrementTime");
        for (int step = 0; step < steps; step++) {
            new ArrayList<>(plants).forEach(plant -> plant.update(map, this, this));
            new ArrayList<>(animals).forEach(animal -> animal.update(map, this, this));
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
                onLifeformRemoved(plant);
                plantIterator.remove();
                map[plant.getPosition().getX()][plant.getPosition().getY()].setInHabitant(null);
            }
        }

        Iterator<Animal> animalIterator = animals.iterator();
        while (animalIterator.hasNext()) {
            Animal animal = animalIterator.next();
            if (animal.getAge() >= animal.getLifespan()) {
                onLifeformRemoved(animal);
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

        onLifeformRemoved(lifeform);
        map[lifeform.getPosition().getX()][lifeform.getPosition().getY()].setInHabitant(null);
    }

    @Override
    public void onLifeFormCreated(@NonNull Lifeform lifeform) {
        addLifeform(lifeform);
        listener.onLifeFormCreated(lifeform);
    }

    @Override
    public void onLifeformRemoved(@NonNull Lifeform lifeform) {
        listener.onLifeformRemoved(lifeform);
    }

    @Override
    public void onLifeformMoved(@NonNull Lifeform lifeform, @NonNull Position newPosition) {
        listener.onLifeformMoved(lifeform, newPosition);
    }
}