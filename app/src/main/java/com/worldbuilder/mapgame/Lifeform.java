package com.worldbuilder.mapgame;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.worldbuilder.mapgame.models.Position;
import com.worldbuilder.mapgame.models.lifeform.LifeformChangeListener;

import java.util.Locale;
import java.io.Serializable;

public abstract class Lifeform implements Serializable {

    private final int lifeFormID;
    private Position position;
    private transient ImageView imageView;

    protected String name;
    protected int age;
    protected float camouflage;
    protected int lifespan;
    protected int imgID;
    protected int habitat;
    protected int propagationRate;

    public Lifeform(String name, float camouflage, int lifespan, Position position, int propagationRate, int imgID, int habitat, int lifeFormID) {
        this.name = name;
        this.position = position;
        this.age = 0;
        this.camouflage = camouflage;
        this.lifespan = lifespan;
        this.propagationRate = propagationRate;
        this.imgID = imgID;
        this.habitat = habitat;
        this.lifeFormID = lifeFormID;
    }

    public abstract void update(Tile[][] map, World world, LifeformChangeListener listener);

    //getters & setters
    public int getLifeFormID() {
        return lifeFormID;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

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

    public int getPropagationRate() {
        return propagationRate;
    }

    public void setPropagationRate(int propagationRate) {
        this.propagationRate = propagationRate;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public int getImgID() {
        return imgID;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void incrementAge() {
        this.age++;
    }

    public static int getImgSize() {
        return 40;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "[%d] name: %s pos: %d x %d",
                lifeFormID, name, position.getX(), position.getY());
    }
}



