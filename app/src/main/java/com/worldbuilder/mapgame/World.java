package com.worldbuilder.mapgame;

import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class World {
    private final static String TAG = World.class.getSimpleName();
    private Tile[][] map;

    public List<Plant> getPlants() {
        return plants;
    }

    public int getDarwinPoints() {
        return darwinPoints;
    }

    public void setDarwinPoints(int darwinPoints) {
        this.darwinPoints = darwinPoints;
    }

    public int darwinPoints = 1000;

    public int getResCounter() {
        return resCounter;
    }

    public void setResCounter(int resCounter) {
        this.resCounter = resCounter;
    }

    public int resCounter = 0;

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    public List<Animal> getAnimals() {
        return animals;
    }

    public void setAnimals(List<Animal> animals) {
        this.animals = animals;
    }

    private List<Plant> plants;
    private List<Animal> animals;

    public void setMapView(RelativeLayout mapView) {
        this.mapView = mapView;
    }

    public RelativeLayout getMapView() {
        return mapView;
    }

    private RelativeLayout mapView;

    public World(Tile[][] map, RelativeLayout mapView) {
        this.map = map;
        this.plants = new ArrayList<>();
        this.animals = new ArrayList<>();
        this.mapView = mapView;
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

   public void removeDead() {
       Iterator<Plant> plantIterator = plants.iterator();
       while (plantIterator.hasNext()) {
           Plant plant = plantIterator.next();
           if (plant.getAge() >= plant.getLifespan()) {
               ImageView plantImageView = plant.getImageView();
               mapView.removeView(plantImageView);
               plantIterator.remove();
               map[plant.getPosition().getX()][plant.getPosition().getY()].setInHabitant(null);
           }
       }



       Iterator<Animal> animalIterator = animals.iterator();
       while (animalIterator.hasNext()) {
           Animal animal = animalIterator.next();
           if (animal.getAge() >= animal.getLifespan()) {
               ImageView animalImageView = animal.getImageView();
               mapView.removeView(animalImageView);
               animalIterator.remove();
               map[animal.getPosition().getX()][animal.getPosition().getY()].setInHabitant(null);
           }
       }
   }

    public void resetLifeforms(){
        for (Animal animal: animals){
            mapView.removeView(animal.getImageView());
        }
        for(Plant plant: plants){
            mapView.removeView(plant.getImageView());
        }
    }
    public List<Plant> getNearbyPlants(Position position, int searchRange) {
        List<Plant> nearbyPlants = new ArrayList<>();
        for (Plant plant : plants) {
                if (MapUtils.arePositionsClose(plant.getPosition(), position, searchRange)) {
                    nearbyPlants.add( plant);
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
        ImageView lifeformImageView = null;
        if (lifeform instanceof Plant) {
            plants.remove(lifeform);
            lifeformImageView = ((Plant) lifeform).getImageView();
        } else {
            animals.remove(lifeform);
            lifeformImageView = ((Animal) lifeform).getImageView();
        }

        if (lifeformImageView != null) {
            mapView.removeView(lifeformImageView);
        }
        map[lifeform.getPosition().getX()][lifeform.getPosition().getY()].setInHabitant(null);
    }
    // Getters and setters for the properties
    // ...

    // Additional world management methods, if needed
    // ...
}