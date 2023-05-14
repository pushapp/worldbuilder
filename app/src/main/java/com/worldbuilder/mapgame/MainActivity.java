package com.worldbuilder.mapgame;

import static com.worldbuilder.mapgame.Lifeforms.getRandomAnimalDrawable;
import static com.worldbuilder.mapgame.Lifeforms.getRandomPlantDrawable;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.worldbuilder.mapgame.databinding.ActivityMainBinding;
import com.worldbuilder.mapgame.models.ItemCreationParams;
import com.worldbuilder.mapgame.models.Position;
import com.worldbuilder.mapgame.ui.dialogs.CreatePlantOrAnimalDialog;
import com.worldbuilder.mapgame.ui.dialogs.CustomizeWorldDialog;
import com.worldbuilder.mapgame.ui.dialogs.MapClickDialog;
import com.worldbuilder.mapgame.utils.LifeformUtils;
import com.worldbuilder.mapgame.viewmodels.GameViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;

public class MainActivity extends AppCompatActivity
        implements CreatePlantOrAnimalDialog.CreatePlantOrAnimalDialogListener,
        CustomizeWorldDialog.CustomizeWorldDialogListener,
        LifeformChangeListener {

    private Tile[][] tilemap = null;
    private final MapGenerator mapGenerator = new MapGenerator();
    private World world = null;
    private static final int width = 2000;
    private static final int height = 2000;

    private int lifeFormID = 1;

    private Map<Plant, ImageView> plantImageViews = new HashMap<>();
    private Map<Animal, ImageView> animalImageViews = new HashMap<>();

    private final Position lastTouchedPosition = new Position(0, 0);
    private final Position mapClickDialogPlacement = new Position(0, 0);

    private ActivityMainBinding binding;
    private GameViewModel viewModel;

    final Observer<Long> updateTickerObserver = step -> incrementTime(1);
    final Observer<Boolean> isLoadingObserver = loading -> {
        binding.loadingAnim.setVisibility(loading ? View.VISIBLE : View.GONE);

        binding.dpoints.setVisibility(loading ? View.GONE : View.VISIBLE);
        binding.resetButton.setVisibility(loading ? View.GONE : View.VISIBLE);
        binding.layoutForClicklistener.setVisibility(loading ? View.GONE : View.VISIBLE);
        binding.lifeFormContainer.setVisibility(loading ? View.GONE : View.VISIBLE);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        viewModel.getTime().observe(this, updateTickerObserver);
        viewModel.getLoading().observe(this, isLoadingObserver);

        //lifeformContainer OnClickListener is in this method
        initobjects();
        tilemap = SaveGame.loadTileArrayFromFile(this);

        if (tilemap == null) {
            //no Saved game... launch dialog to create new game
            CustomizeWorldDialog customizeWorldDialog = new CustomizeWorldDialog();
            customizeWorldDialog.show(getSupportFragmentManager(), "customize_world_dialog");
        } else {
            //There is game saved. load it
            Bitmap bitmap = SaveGame.loadBitmapFromInternalStorage(this, SaveGame.BITMAPFILE);
            loadSavedGame(bitmap, tilemap);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.startTimer();
        Log.d("Debug", "onResume() called");
    }

    @Override
    protected void onStop() {
        viewModel.stopTimer();
        super.onStop();
    }

    @SuppressWarnings("SameParameterValue")
    private void incrementTime(int steps) {
        if (world == null) return;

        world.update(steps);
        updateDarwinTV();
    }

    private void addLifeformImageView(Lifeform lifeform) {
        ImageView imageView = LifeformUtils.INSTANCE.createLifeformImageView(lifeform, this);
        binding.lifeFormContainer.addView(imageView);
    }

    private void updateDarwinTV() {
        int points = world.getDarwinPoints();
        binding.dpoints.setText(String.format(Locale.getDefault(), "$%d Darwin", points));
    }

    @Override
    public void OnLifeformAddSelected(ItemCreationParams params) {
        if (params.cost > world.getDarwinPoints()) {
            showNotEnoughPointsMessage();
            return;
        }

        int lifespan = params.lifeSpanProgress;
        int propagationRate = (params.propagationRateProgress / 20) + 1;
        int elevationHabitat = (params.elevationProgress * MapUtils.getMaxElevation(tilemap))/100; //adjust elevation meter to the elevation of the map
        int seedingDist = (params.seedingDistanceProgress / 5) + 1;
        int speed = (params.seedSpeedProgress / 20) + 1;
        // Get values from other input fields


        Log.d("LifeformStats","elevation: " + elevationHabitat + " propagation: " + propagationRate + " speed: " + speed + " seedingDist: " + seedingDist + " lifespan: " + lifespan);

        List<Position> positions = MapUtils.generateSurroundingPositions(lastTouchedPosition, tilemap, false, 1, 3);

        List<Position> selectedPositions = MapUtils.getRandomPositions(positions, 5);
        lifeFormID++;
        //generate 5 of the lifeforms
        Log.d("NumOfGeneratedPositions", "Positions generated: " + positions.size());

        if (positions.size() > 4) {
            world.setDarwinPoints(world.getDarwinPoints() - params.cost);

            int plantRes = getRandomPlantDrawable();
            int animalRes = getRandomAnimalDrawable();
            for (Position position1 : selectedPositions) {
                if (params.isPlantSelected) {
                    Plant plant1 = new Plant("plant_" + lifeFormID, .5f, lifespan, position1, propagationRate, seedingDist, plantRes, elevationHabitat, lifeFormID);
                    addLifeformImageView(plant1);
                    world.addLifeform(plant1);
                } else {
                    Animal animal = new Animal("animal_" + lifeFormID, speed, .5f, lifespan, position1, propagationRate, animalRes, elevationHabitat, lifeFormID);
                    animal.setFoodType(params.selectedFoodType);
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

    public void onLifeFormCreated(Lifeform lifeform) {
        ImageView newPlantImageView = LifeformUtils.INSTANCE.createLifeformImageView(lifeform, this);
        binding.lifeFormContainer.addView(newPlantImageView);
    }

    @Override
    public void onLifeformRemoved(Lifeform lifeform) {
        binding.lifeFormContainer.removeView(lifeform.getImageView());
    }

    @Override
    public void onCreateWorld(float waterFrequency, float mountainFrequency) {
        if (world != null) {
            //remove lifeform imageviews if starting new game
            world.resetLifeforms();
            binding.lifeFormContainer.removeAllViews();
        }
        viewModel.onCreateWorldStarted();
        LoadWorldAsync loadWorldAsync = new LoadWorldAsync(this, width, height, waterFrequency, mountainFrequency, this, mapGenerator);
        loadWorldAsync.setAsyncTaskCallback((world, tiles, bitmap) -> {
            this.world = world;
            tilemap = tiles;
            setMapBitmap(bitmap);
            viewModel.onCreateWorldFinished();
        });
        loadWorldAsync.execute();
    }

    public void loadSavedGame(Bitmap bitmap, Tile[][] map) {
        setMapBitmap(bitmap);

        world = new World(map, this);
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

        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if (map[x][y].getInHabitant() != null) {

                    Lifeform lf = map[x][y].getInHabitant();
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

    private void showNotEnoughPointsMessage() {
        Snackbar.make(binding.getRoot(),
                        R.string.not_enough_darwin_points,
                        Snackbar.LENGTH_LONG)
                .show();
    }

    private void initobjects() {
        binding.lifeFormContainer.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int x = MapUtils.PixeltoTileX((int) event.getX());
                int y = MapUtils.PixeltoTileY((int) event.getY());
                Log.d("TileClicked", "Tile: x = " + x + "y = " + y);
                lastTouchedPosition.set(x, y);
                Log.d("TileClicked", "Click Elevation: " + tilemap[x][y].getElevation());
            }
            return false;
        });

        binding.layoutForClicklistener.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //needs to grab x and y from layout outside of scrollview to place dialog correctly on the screen
                int x = (int) event.getX();
                int y = (int) event.getY();
                mapClickDialogPlacement.set(x, y);
                Log.d("debug", "Dialog Placement: x = " + x + "y = " + y);
            }
            return false;
        });


        binding.lifeFormContainer.setOnClickListener(view -> {
            MapClickDialog mapClickDialog = new MapClickDialog(
                    this,
                    () -> showAddLifeformDialog(),
                    () -> showLifefomListDialog()
            );
            mapClickDialog.showPopupWindow(mapClickDialogPlacement);

        });

        binding.resetButton.setOnClickListener(view -> {
            CustomizeWorldDialog customizeWorldDialog1 = new CustomizeWorldDialog();
            customizeWorldDialog1.show(getSupportFragmentManager(), "customize_world_dialog");
        });
    }

    private Unit showAddLifeformDialog() {
        CreatePlantOrAnimalDialog addLifeformDialog = new CreatePlantOrAnimalDialog(this);
        addLifeformDialog.show();
        return Unit.INSTANCE;
    }

    private Unit showLifefomListDialog() {
        //not created yet. shows a recyclerview with lifeforms in area to view specific lifeforms stats

        return Unit.INSTANCE;
    }
}