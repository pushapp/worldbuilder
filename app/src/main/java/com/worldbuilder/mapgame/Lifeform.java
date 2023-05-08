package com.worldbuilder.mapgame;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.Locale;

public abstract class Lifeform {


    public int getLifeFormID() {
        return lifeFormID;
    }

    public int lifeFormID = 0;

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position position;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getCamouflage() {
        return camouflage;
    }

    public void setCamouflage(float camouflage) {
        this.camouflage = camouflage;
    }


    public String getName() {
        return name;
    }

    protected String name;
    protected int age;
    protected float camouflage;

    public int getPropagationRate() {
        return propagationRate;
    }

    public void setPropagationRate(int propagationRate) {
        this.propagationRate = propagationRate;
    }

    protected int propagationRate;

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    protected int lifespan;

    protected int imgID;

    protected int habitat;

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    protected ImageView imageView;

    public Lifeform(String name, float camouflage, int lifespan, Position position, int propagationRate, int imgID, int habitat, int lifeFormID) {
        this.name = name;
        this.position = position;
        this.age = 0;
        this.camouflage = camouflage;
        this.lifespan = lifespan;
        this.propagationRate = propagationRate;
        this.imgID = imgID;
        this.habitat = habitat;
    }

    public abstract void update(Tile[][] map, World world, Context context);

    public void incrementAge() {
        this.age++;
    }

    public static int getImgSize() {
        return 40;
    }

    // Getters and setters for the properties
    // ...

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "[%d] name: %s pos: %d x %d",
                lifeFormID, name, position.getX(), position.getY());
    }
}



