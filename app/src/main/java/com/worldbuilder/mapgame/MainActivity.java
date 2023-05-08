package com.worldbuilder.mapgame;

import static com.worldbuilder.mapgame.Lifeforms.getRandomAnimalDrawable;
import static com.worldbuilder.mapgame.Lifeforms.getRandomPlantDrawable;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.worldbuilder.mapgame.databinding.ActivityMainBinding;
import com.worldbuilder.mapgame.models.ItemCreationParams;
import com.worldbuilder.mapgame.ui.dialogs.CreatePlantOrAnimalDialog;
import com.worldbuilder.mapgame.ui.dialogs.CustomizeWorldDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CreatePlantOrAnimalDialog.CreatePlantOrAnimalDialogListener, CustomizeWorldDialog.CustomizeWorldDialogListener {

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

    private final Position lastTouchedPosition = new Position(0, 0);

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //lifeformContainer OnClickListener is in this method
        initobjects();

        Bitmap bitmap = SaveGame.loadBitmapFromInternalStorage(this, SaveGame.BITMAPFILE);
        if (bitmap == null) {
            //no Saved game... launch dialog to create new game
            CustomizeWorldDialog customizeWorldDialog = new CustomizeWorldDialog();
            customizeWorldDialog.show(getSupportFragmentManager(), "customize_world_dialog");
        } else {
            //There is game saved. load it
            tilemap = SaveGame.loadTileArrayFromFile(this);
            loadSavedGame(bitmap, tilemap);
        }
    }

    private final Handler handler = new Handler();
    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {

            if (world != null) {
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

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Lifeform.getImgSize(), Lifeform.getImgSize());
        layoutParams.leftMargin = xPosition;
        layoutParams.topMargin = yPosition;

        lifeformImageView.setLayoutParams(layoutParams);

        binding.lifeFormContainer.addView(lifeformImageView);

        lifeform.setImageView(lifeformImageView);

        int[] location = new int[2];
        lifeformImageView.getLocationInWindow(location);
        Log.d("LifeformImageView", "Position within RelativeLayout: X: " + location[0] + ", Y: " + location[1]);
    }

    private void updateDarwinTV(World world) {
        binding.dpoints.setText("$" + world.getDarwinPoints() + " Darwin");
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

            int plantRes = getRandomPlantDrawable();
            int animalRes = getRandomAnimalDrawable();
            for (Position position1 : selectedPositions) {
                if (params.isPlantSelected) {
                    Plant plant1 = new Plant("", .5f, lifespan, position1, propagationRate, seedingDist, plantRes, elevationHabitat, lifeFormID);
                    addLifeformImageView(plant1);
                    world.addLifeform(plant1);
                } else {
                    Animal animal = new Animal("", speed, .5f, lifespan, position1, propagationRate, animalRes, elevationHabitat, lifeFormID);
                    animal.setFoodType(foodType);
                    addLifeformImageView(animal);
                    world.addLifeform(animal);
                }
            }

            //save Tilemap
            SaveGame.saveTileArrayToFile(this, tilemap);
            SaveGame.saveToSharedPrefs(this, world);
        }
        // Update the UI to display the new lifeform
        // ...
    }

    @Override
    public void onCreateWorld(float waterFrequency, float mountainFrequency) {
        tilemap = mapGenerator.generateRandomMap(width, height, waterFrequency, mountainFrequency);
        Bitmap bitmap = mapGenerator.generateRandomMapBitmap(width, height, Tile.getTileSize(), tilemap);
        SaveGame.saveBitmapToInternalStorage(this, bitmap, SaveGame.BITMAPFILE);
        setMapBitmap(bitmap);

        tilemap = MapUtils.reduceTileArray(tilemap, MapUtils.tileMapDivisor);

        world = new World(tilemap, binding.lifeFormContainer);
        binding.dpoints.setText("Darwin Points: " + world.getDarwinPoints());
    }

    public void loadSavedGame(Bitmap bitmap, Tile[][] map) {
        setMapBitmap(bitmap);

        world = new World(map, binding.lifeFormContainer);
        List<Animal> animals = SaveGame.loadAnimalsFromPrefs(this);
        for (Animal animal : animals) {
            Position pos = animal.getPosition();
            map[pos.getX()][pos.getY()].setInHabitant(animal);
            addLifeformImageView(animal);
        }
        List<Plant> plants = SaveGame.loadPlantsFromPrefs(this);
        for (Plant plant : plants) {
            Position pos = plant.getPosition();
            map[pos.getX()][pos.getY()].setInHabitant(plant);
            addLifeformImageView(plant);
        }
        int darwin = SaveGame.loadDarwinFromPrefs(this);
        world.setAnimals(animals);
        world.setPlants(plants);
        world.setDarwinPoints(darwin);

        binding.dpoints.setText("Darwin Points: " + world.getDarwinPoints());
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                Log.d("debug", "in TM loop");
                if (map[x][y].getInHabitant() != null) {

                    Lifeform lf = map[x][y].getInHabitant();
                    Log.d("debug", "lf at " + lf.getPosition().getX() + " , " + lf.getPosition().getY());
                    world.addLifeform(lf);
                    addLifeformImageView(lf);

                }
            }
        }
    }

    /**
     * apply image of map to view
     */
    private void setMapBitmap(Bitmap bitmap) {
        Drawable map = new BitmapDrawable(getResources(), bitmap);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        binding.lifeFormContainer.setLayoutParams(lp);
        binding.lifeFormContainer.setBackground(map);
    }

    private void initobjects() {
        binding.lifeFormContainer.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int x = (int) (event.getX() / Tile.getTileSize()) / MapUtils.tileMapDivisor;
                int y = (int) (event.getY() / Tile.getTileSize()) / MapUtils.tileMapDivisor;
                Log.d("debug", "IN ONTOUCH: x = " + x + "y = " + y);
                lastTouchedPosition.set(x, y);
            }
            return false;
        });
        binding.lifeFormContainer.setOnClickListener(view -> {
            CreatePlantOrAnimalDialog dialog = new CreatePlantOrAnimalDialog(this);
            dialog.show();
        });

        binding.resetButton.setOnClickListener(view -> {
            world.resetLifeforms();

            CustomizeWorldDialog customizeWorldDialog1 = new CustomizeWorldDialog();
            customizeWorldDialog1.show(getSupportFragmentManager(), "customize_world_dialog");
        });
    }
}