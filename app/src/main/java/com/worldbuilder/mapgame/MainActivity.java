package com.worldbuilder.mapgame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Gson gson = new Gson();
    TextView dpointTV;
    ImageView[][] imageViews;
    Tile[][] tilemap;
    MapGenerator mapGenerator = new MapGenerator();
    World world;
    private static int width = 2000;
    private static int height = 2000;
    private static int timeSpeed = 1000; // 1 second

    private int lifeFormID = 1;

    private Map<Plant, ImageView> plantImageViews = new HashMap<>();
    private Map<Animal, ImageView> animalImageViews = new HashMap<>();

    private Position lastTouchedPosition = new Position(0, 0);

    private ImageView mapIV;
    private RelativeLayout lifeformContainer;

    int[] animalDrawables = {
            R.drawable.goat,
            R.drawable.pig,
            R.drawable.cat,
            R.drawable.monkey,
            R.drawable.greyanimal
            // ...
    };

    // Plant drawable resource IDs
    int[] plantDrawables = {
            R.drawable.yellowcactus,
            R.drawable.grass,
            R.drawable.blueflower,
            R.drawable.redflower,
            R.drawable.sprout
            // ...
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tilemap = mapGenerator.generateRandomMap(width, height);

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
        lifeformContainer.setOnClickListener(view -> showAddLifeformDialog(lastTouchedPosition));


        world = new World(tilemap, lifeformContainer);

        dpointTV = findViewById(R.id.dpoints);
        dpointTV.setText("Darwin Points: " + world.getDarwinPoints());

        Button ResetButton = findViewById(R.id.resetButton);
        ResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tilemap = mapGenerator.generateRandomMap(width, height);
                mapIV = findViewById(R.id.mapIV);
                Bitmap bitmap = mapGenerator.generateRandomMapBitmap(width, height, Tile.getTileSize(), tilemap);


                mapIV.setImageBitmap(bitmap);

                tilemap = MapUtils.reduceTileArray(tilemap, MapUtils.tileMapDivisor);
                world.resetLifeforms();
                world = new World(tilemap, lifeformContainer);
            }
        });

    }

    private final Handler handler = new Handler();
    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {

            incrementTime(1);

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
        handler.removeCallbacks(updateTask);
        Log.d("Debug", "onPause() called");
    }


    private void showAddLifeformDialog(Position position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_lifeform, null);
        builder.setView(dialogView);

        TextView costTV = dialogView.findViewById(R.id.cost);


        final int[] seedDistCost = {50};
        final int[] lifespanCost = {50};
        final int[] speedCost = {50};
        final int[] propCost = {100};
        final int[] foodCost = {1000};
        final int[] cost = {300};

        costTV.setText("Cost: " + cost[0]);

        final boolean[] isPlant = {true};
        TextView planttv = dialogView.findViewById(R.id.planttv);
        TextView animaltv = dialogView.findViewById(R.id.animaltv);

        //seedDist
        SeekBar seedingDistSeek = dialogView.findViewById(R.id.seedDistanceseek);
        seedingDistSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seedDistCost[0] = i;

                cost[0] = seedDistCost[0] + propCost[0] + lifespanCost[0] + 100;
                costTV.setText("Cost: " + cost[0]);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        LinearLayout seedDistLayout = dialogView.findViewById(R.id.seedDistancelayout);

        //food
        Spinner foodTypeSpin = dialogView.findViewById(R.id.foodTypeSpinner);
        LinearLayout foodTypeLayout = dialogView.findViewById(R.id.foodTypeLayout);
        foodTypeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = foodTypeSpin.getSelectedItem().toString();

                if (selected.equals("Herbivore")) {
                    foodCost[0] = 1000;
                }
                if (selected.equals("Carnivore")) {
                    foodCost[0] = 10000;
                }
                cost[0] = foodCost[0] + propCost[0] + speedCost[0] + lifespanCost[0];
                costTV.setText("Cost: " + cost[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        SeekBar speedSeek = dialogView.findViewById(R.id.speedSeek);
        LinearLayout speedLayout = dialogView.findViewById(R.id.speedlayout);
        speedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                speedCost[0] = i * 4;
                cost[0] = foodCost[0] + propCost[0] + speedCost[0] + lifespanCost[0];
                costTV.setText("Cost: " + cost[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar elevation = dialogView.findViewById(R.id.elevationHabitatseek);


        SeekBar lifespanSeek = dialogView.findViewById(R.id.lifespanseek);
        lifespanSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                lifespanCost[0] = i;

                if (isPlant[0]) {
                    cost[0] = seedDistCost[0] + propCost[0] + lifespanCost[0] + 150;
                } else {
                    cost[0] = foodCost[0] + propCost[0] + speedCost[0] + lifespanCost[0];
                }
                costTV.setText("Cost: " + cost[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // Get references to other input fields

        SeekBar propagationSeek = dialogView.findViewById(R.id.plantdispersion);
        propagationSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                propCost[0] = i * 2;

                if (isPlant[0]) {
                    cost[0] = seedDistCost[0] + propCost[0] + lifespanCost[0] + 150;
                } else {
                    cost[0] = foodCost[0] + propCost[0] + speedCost[0] + lifespanCost[0];
                }
                costTV.setText("Cost: " + cost[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        builder.setPositiveButton("Add", (dialog, which) -> {

            if (world.getDarwinPoints() > cost[0]) {
                int lifespan = lifespanSeek.getProgress();
                int propagationRate = (propagationSeek.getProgress() / 20) + 1;
                int elevationHabitat = elevation.getProgress();
                int seedingDist = (seedingDistSeek.getProgress() / 5) + 1;
                String foodType = foodTypeSpin.getSelectedItem().toString();
                int speed = (speedSeek.getProgress() / 20) + 1;
                // Get values from other input fields

                Random random = new Random();
                int drawablerand = random.nextInt(5);
                List<Position> positions = MapUtils.generateSurroundingPositions(position, tilemap, false, 1, 3);

                List<Position> selectedPositions = MapUtils.getRandomPositions(positions, 5);
                lifeFormID++;
                //generate 5 of the lifeforms
                Log.d("Debug", "Positions generated: " + positions.size());

                if (positions.size() > 4) {
                    world.setDarwinPoints(world.darwinPoints - cost[0]);
                    updateDarwinTV(world);

                    for (Position position1 : selectedPositions) {
                        if (isPlant[0]) {

                            Plant plant1 = new Plant("", .5f, lifespan, position1, propagationRate, seedingDist, plantDrawables[drawablerand], elevationHabitat, lifeFormID);
                            addLifeformImageView(plant1);
                            world.addLifeform(plant1);
                        } else {
                            Animal animal = new Animal("", speed, .5f, lifespan, position1, propagationRate, animalDrawables[drawablerand], elevationHabitat, lifeFormID);
                            animal.setFoodType(foodType);
                            addLifeformImageView(animal);
                            world.addLifeform(animal);
                        }
                    }
                }
            }
            // Update the UI to display the new lifeform
            // ...
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.show();
        dialogView.setBackgroundColor(Color.TRANSPARENT);

        planttv.setOnClickListener(view -> {
            planttv.setBackgroundColor(Color.WHITE);
            animaltv.setBackgroundColor(Color.GRAY);
            isPlant[0] = true;
            seedDistLayout.setVisibility(View.VISIBLE);
            speedLayout.setVisibility(View.GONE);
            foodTypeLayout.setVisibility(View.GONE);


        });
        animaltv.setOnClickListener(view -> {
            planttv.setBackgroundColor(Color.GRAY);
            animaltv.setBackgroundColor(Color.WHITE);
            isPlant[0] = false;
            seedDistLayout.setVisibility(View.GONE);
            speedLayout.setVisibility(View.VISIBLE);
            foodTypeLayout.setVisibility(View.VISIBLE);
            cost[0] = foodCost[0] + propCost[0] + speedCost[0] + lifespanCost[0];
            costTV.setText("Cost: " + cost[0]);
        });
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

}