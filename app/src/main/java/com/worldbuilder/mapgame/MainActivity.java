package com.worldbuilder.mapgame;

import static com.worldbuilder.mapgame.Lifeforms.getRandomAnimalDrawable;
import static com.worldbuilder.mapgame.Lifeforms.getRandomPlantDrawable;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.worldbuilder.mapgame.models.ItemCreationParams;
import com.worldbuilder.mapgame.ui.dialogs.CreatePlantOrAnimalDialog;
import com.worldbuilder.mapgame.ui.dialogs.CustomizeWorldDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CreatePlantOrAnimalDialog.CreatePlantOrAnimalDialogListener, CustomizeWorldDialog.CustomizeWorldDialogListener {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Gson gson = new Gson();
    TextView dpointTV;
    ImageView[][] imageViews;
    Tile[][] tilemap;
    MapGenerator mapGenerator = new MapGenerator();
    World world = null;
    private static final int width = 2000;
    private static final int height = 2000;
    private static final int timeSpeed = 1000; // 1 second

    private int lifeFormID = 1;

    private Map<Plant, ImageView> plantImageViews = new HashMap<>();
    private Map<Animal, ImageView> animalImageViews = new HashMap<>();

    private Position lastTouchedPosition = new Position(0, 0);

    private ImageView mapIV;
    private RelativeLayout lifeformContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        CustomizeWorldDialog customizeWorldDialog = new CustomizeWorldDialog();
        customizeWorldDialog.show(getSupportFragmentManager(), "customize_world_dialog");

        dpointTV = findViewById(R.id.dpoints);


        Button ResetButton = findViewById(R.id.resetButton);
        ResetButton.setOnClickListener(view -> {
            world.resetLifeforms();

            CustomizeWorldDialog customizeWorldDialog1 = new CustomizeWorldDialog();
            customizeWorldDialog1.show(getSupportFragmentManager(), "customize_world_dialog");
        });

    }

    private final Handler handler = new Handler();
    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {

            if(world != null) {
                incrementTime(1);
            }

            handler.postDelayed(this, timeSpeed); // Update every 1000 milliseconds (1 second)
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateTask);
        Log.d("Debug", "onResume() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        Log.d("Debug", "onPause() called");
    }

    private void incrementTime(int steps) {
        for (int step = 0; step < steps; step++) {

            List<Plant> plantsCopy = new ArrayList<>(world.getPlants());
            for (Plant plant : plantsCopy) {
                plant.update(tilemap, world, this);
            }

            List<Animal> animalsCopy = new ArrayList<>(world.getAnimals());
            for (Animal animal : animalsCopy) {
                animal.update(tilemap, world, this);
            }
            // Remove dead lifeforms (age >= lifespan) from the lists
            world.removeDead();
            updateDarwinTV(world);
        }
    }

    private void addLifeformImageView(Lifeform lifeform) {
        ImageView lifeformImageView = new ImageView(this);

        lifeformImageView.setImageResource(lifeform.imgID);

        int xPosition = MapUtils.calculateXPosition(lifeform.getPosition().getX());
        int yPosition = MapUtils.calculateYPosition(lifeform.getPosition().getY());
        Log.d("LifeformPosition", "X: " + xPosition + ", Y: " + yPosition);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Lifeform.getImgSize(), Lifeform.getImgSize());
        layoutParams.leftMargin = xPosition;
        layoutParams.topMargin = yPosition;

        lifeformImageView.setLayoutParams(layoutParams);

        lifeformContainer.addView(lifeformImageView);

        lifeform.setImageView(lifeformImageView);

        int[] location = new int[2];
        lifeformImageView.getLocationInWindow(location);
        Log.d("LifeformImageView", "Position within RelativeLayout: X: " + location[0] + ", Y: " + location[1]);

    }

    private void saveGame() {
//        String json = gson.toJson(world);
//        String json1 = gson.toJson(tilemap);
//        editor.putString("world",json);
//        editor.commit();
//        editor.putString("tilemap", json1);
//        editor.commit();
    }

    private void updateDarwinTV(World world) {
        dpointTV.setText("$" + world.getDarwinPoints() + " Darwin");
    }

    @Override
    public void OnLifeformAddSelected(ItemCreationParams params) {
        if (params.cost > world.getDarwinPoints()) return;

        int lifespan = params.lifeSpanProgress;
        int propagationRate = (params.propagationRateProgress / 20) + 1;
        int elevationHabitat = params.elevationProgress;
        int seedingDist = (params.seedingDistanceProgress / 5) + 1;
        String foodType = params.selectedFoodType;
        int speed = (params.seedSpeedProgress / 20) + 1;
        // Get values from other input fields

        List<Position> positions = MapUtils.generateSurroundingPositions(lastTouchedPosition, tilemap, false, 1, 3);

        List<Position> selectedPositions = MapUtils.getRandomPositions(positions, 5);
        lifeFormID++;
        //generate 5 of the lifeforms
        Log.d("Debug", "Positions generated: " + positions.size());

        if (positions.size() > 4) {
            world.setDarwinPoints(world.darwinPoints - params.cost);
            updateDarwinTV(world);

            for (Position position1 : selectedPositions) {
                if (params.isPlantSelected) {
                    int drawableResId = getRandomPlantDrawable();
                    Plant plant1 = new Plant("", .5f, lifespan, position1, propagationRate, seedingDist, drawableResId, elevationHabitat, lifeFormID);
                    addLifeformImageView(plant1);
                    world.addLifeform(plant1);
                } else {
                    int drawableResId = getRandomAnimalDrawable();
                    Animal animal = new Animal("", speed, .5f, lifespan, position1, propagationRate, drawableResId, elevationHabitat, lifeFormID);
                    animal.setFoodType(foodType);
                    addLifeformImageView(animal);
                    world.addLifeform(animal);
                }
            }
        }
        // Update the UI to display the new lifeform
        // ...
    }

    @Override
    public void onCreateWorld(float waterFrequency, float mountainFrequency) {
        tilemap = mapGenerator.generateRandomMap(width, height, waterFrequency, mountainFrequency);

        mapIV = findViewById(R.id.mapIV);
        Bitmap bitmap = mapGenerator.generateRandomMapBitmap(width, height, Tile.getTileSize(), tilemap);
        mapIV.setImageBitmap(bitmap);

        tilemap = MapUtils.reduceTileArray(tilemap, MapUtils.tileMapDivisor);
        lifeformContainer = findViewById(R.id.lifeFormContainer);

        RelativeLayout.LayoutParams containerLayoutParams = new RelativeLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        lifeformContainer.setLayoutParams(containerLayoutParams);

        lifeformContainer.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int x = (int) (event.getX() / Tile.getTileSize()) / MapUtils.tileMapDivisor;
                int y = (int) (event.getY() / Tile.getTileSize()) / MapUtils.tileMapDivisor;
                Log.d("debug", "IN ONTOUCH: x = " + x + "y = " + y);
                lastTouchedPosition.setX(x);
                lastTouchedPosition.setY(y);
            }
            return false;
        });
        lifeformContainer.setOnClickListener(view -> {
            CreatePlantOrAnimalDialog dialog = new CreatePlantOrAnimalDialog(this);
            dialog.show();
        });


        world = new World(tilemap, lifeformContainer);
        dpointTV.setText("Darwin Points: " + world.getDarwinPoints());
    }
}