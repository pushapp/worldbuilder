package com.worldbuilder.mapgame;

import static com.worldbuilder.mapgame.Lifeforms.getRandomAnimalDrawable;
import static com.worldbuilder.mapgame.Lifeforms.getRandomPlantDrawable;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.worldbuilder.mapgame.databinding.ActivityMainBinding;
import com.worldbuilder.mapgame.models.ItemCreationParams;
import com.worldbuilder.mapgame.models.Position;
import com.worldbuilder.mapgame.ui.dialogs.CreatePlantOrAnimalDialog;
import com.worldbuilder.mapgame.ui.dialogs.CustomizeWorldDialog;
import com.worldbuilder.mapgame.ui.dialogs.MapClickDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;

public class MainActivity extends AppCompatActivity
        implements CreatePlantOrAnimalDialog.CreatePlantOrAnimalDialogListener,
        CustomizeWorldDialog.CustomizeWorldDialogListener {


    private Tile[][] tilemap = null;
    private MapGenerator mapGenerator = new MapGenerator();
    private World world = null;
    private static final int width = 2000;
    private static final int height = 2000;
    private static final int timeSpeed = 1000; // 1 second

    private int lifeFormID = 1;

    private Map<Plant, ImageView> plantImageViews = new HashMap<>();
    private Map<Animal, ImageView> animalImageViews = new HashMap<>();

    private final Position lastTouchedPosition = new Position(0, 0);
    private final Position mapClickDialogPlacement = new Position(0, 0);

    private ActivityMainBinding binding;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        Disposable d = Observable
                .interval(0, timeSpeed, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(step -> incrementTime(1));
        disposables.add(d);
        Log.d("Debug", "onResume() called");
    }

    @Override
    protected void onStop() {
        disposables.clear();
        super.onStop();
    }

    @SuppressWarnings("SameParameterValue")
    private void incrementTime(int steps) {
        if (world == null) return;

        world.update(steps, this);
        updateDarwinTV(world);
    }

    private void addLifeformImageView(Lifeform lifeform) {
        ImageView lifeformImageView = new ImageView(this);

        lifeformImageView.setImageResource(lifeform.imgID);

        int xPosition = MapUtils.TiletoPixelX(lifeform.getPosition().getX());
        int yPosition = MapUtils.TiletoPixelY(lifeform.getPosition().getY());
        Log.d("LifeformPosition", "X: " + xPosition + ", Y: " + yPosition);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Lifeform.getImgSize(), Lifeform.getImgSize());
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
        binding.dpoints.setText(String.format(Locale.getDefault(), "$%d Darwin", world.getDarwinPoints()));
    }

    @Override
    public void OnLifeformAddSelected(ItemCreationParams params) {
        if (params.cost > world.getDarwinPoints()) {
            showNotEnoughPointsMessage();
            return;
        }

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
            world.setDarwinPoints(world.getDarwinPoints() - params.cost);
            updateDarwinTV(world);

            int plantRes = getRandomPlantDrawable();
            int animalRes = getRandomAnimalDrawable();
            for (Position position1 : selectedPositions) {
                if (params.isPlantSelected) {
                    Plant plant1 = new Plant("plant_" + lifeFormID, .5f, lifespan, position1, propagationRate, seedingDist, plantRes, elevationHabitat, lifeFormID);
                    addLifeformImageView(plant1);
                    world.addLifeform(plant1);
                } else {
                    Animal animal = new Animal("animal_" + lifeFormID, speed, .5f, lifespan, position1, propagationRate, animalRes, elevationHabitat, lifeFormID);
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
        if (world != null) {
            //remove lifeform imageviews if starting new game
            world.resetLifeforms();
            binding.lifeFormContainer.removeAllViews();
        }
        LoadWorldAsync loadWorldAsync = new LoadWorldAsync(binding.loadingAnim, this, width, height, waterFrequency, mountainFrequency, binding, mapGenerator);
        loadWorldAsync.setAsyncTaskCallback((world, tiles, bitmap) -> {
            this.world = world;
            tilemap = tiles;
            setMapBitmap(bitmap);
            updateDarwinTV(this.world);
        });
        loadWorldAsync.execute();
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

        updateDarwinTV(world);
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

    private void showNotEnoughPointsMessage() {
        Snackbar.make(binding.getRoot(),
                        R.string.not_enough_darwin_points,
                        Snackbar.LENGTH_LONG)
                .show();
    }

    private void initobjects() {
        binding.lifeFormContainer.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int x =  MapUtils.PixeltoTileX((int)event.getX());
                int y = MapUtils.PixeltoTileY((int)event.getY());
                Log.d("debug", "IN ONTOUCH: x = " + x + "y = " + y);
                lastTouchedPosition.set(x, y);
            }
            return false;
        });

        binding.layoutForClicklistener.setOnTouchListener((view, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                //needs to grab x and y from layout outside of scrollview to place dialog correctly on the screen
                int x =  (int)event.getX();
                int y = (int) event.getY();
                mapClickDialogPlacement.set(x,y);
                Log.d("debug", "Dialog Placement: x = " + x + "y = " + y);
            }
            return false;
        });



        binding.lifeFormContainer.setOnClickListener(view -> {
            MapClickDialog mapClickDialog = new MapClickDialog(
                    this,
                    ()-> showAddLifeformDialog(),
                    () -> showLifefomListDialog()
            );
            mapClickDialog.showPopupWindow(mapClickDialogPlacement);

        });

        binding.resetButton.setOnClickListener(view -> {
            CustomizeWorldDialog customizeWorldDialog1 = new CustomizeWorldDialog();
            customizeWorldDialog1.show(getSupportFragmentManager(), "customize_world_dialog");
        });
    }

    private Unit showAddLifeformDialog(){
        CreatePlantOrAnimalDialog addLifeformDialog = new CreatePlantOrAnimalDialog(this);
        addLifeformDialog.show();
        return Unit.INSTANCE;
    }
    private Unit showLifefomListDialog(){
        //not created yet. shows a recyclerview with lifeforms in area to view specific lifeforms stats

        return Unit.INSTANCE;
    }
}