package com.worldbuilder.mapgame.models;

public class ItemCreationParams {
    public int seedDistCost = 50;
    public int lifespanCost = 50;
    public int speedCost = 50;
    public int propCost = 100;
    public int foodCost = 1000;
    public int cost = 300;

    public boolean isPlantSelected = true;
    public int lifeSpanProgress = 50;
    public int propagationRateProgress = 50;
    public int seedingDistanceProgress = 50;
    public int seedSpeedProgress = 50;

    public String selectedFoodType = "";

    //TODO: complete this part (not implemented in original code)
    public int elevationProgress = 50;
}
