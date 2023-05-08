package com.worldbuilder.mapgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SaveGame {
    static Type animalListType = new TypeToken<List<Animal>>() {}.getType();
    static Type plantListType = new TypeToken<List<Plant>>() {}.getType();
    public static final String BITMAPFILE = "bmf";

    public static final String ANIMALS_KEY = "animals";
    public static final String PLANTS_KEY = "plants";
    public static final String DARWIN_KEY = "darwin";
    public static final String SHARED_PREFS_KEY = "sharedpreferences";
    public static final String TILE_FILE = "tile_array_data.json";

    public static void saveBitmapToInternalStorage(Context context, Bitmap bitmap, String fileName) {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap loadBitmapFromInternalStorage(Context context, String fileName) {
        try (FileInputStream fis = context.openFileInput(fileName)) {
            return BitmapFactory.decodeStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String tileArrayToJson(Tile[][] tileArray) {
        Gson gson = new Gson();
        return gson.toJson(tileArray);
    }
    public static void saveTileArrayToFile(Context context, Tile[][] tileArray) {
        removeLifeformsFromTiles(tileArray);

        String jsonString = tileArrayToJson(tileArray);

        try (FileOutputStream fos = context.openFileOutput(TILE_FILE, Context.MODE_PRIVATE)) {
            fos.write(jsonString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static Tile[][] jsonToTileArray(String jsonString) {
        Gson gson = new Gson();
        Type tileArrayType = new TypeToken<Tile[][]>() {}.getType();
        return gson.fromJson(jsonString, tileArrayType);
    }
    public static Tile[][] loadTileArrayFromFile(Context context) {

        try (FileInputStream fis = context.openFileInput(TILE_FILE)) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            return jsonToTileArray(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveToSharedPrefs(Context context, World world) {

        Gson gson = new Gson();
            String animalsjson = gson.toJson(world.getAnimals(), animalListType);

            String plantsjson = gson.toJson(world.getPlants(), animalListType);


        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ANIMALS_KEY, animalsjson);
        editor.putString(PLANTS_KEY,plantsjson);
        editor.putInt(DARWIN_KEY,world.darwinPoints);
        editor.apply();
    }

    public static List<Animal> loadAnimalsFromPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(ANIMALS_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        return gson.fromJson(json, animalListType);
    }

    public static List<Plant> loadPlantsFromPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(PLANTS_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        return gson.fromJson(json, plantListType);
    }
    public static int loadDarwinFromPrefs(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(DARWIN_KEY,0);
    }

    private static void removeLifeformsFromTiles(Tile[][] tiles) {
        for (Tile[] tileRow : tiles) {
            for (Tile tile : tileRow) {
                if (tile.getInHabitant() != null) {
                    tile.setInHabitant(null);
                }
            }
        }
    }

}
