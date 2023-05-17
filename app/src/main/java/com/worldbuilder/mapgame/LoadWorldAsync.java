package com.worldbuilder.mapgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.worldbuilder.mapgame.models.lifeform.LifeformChangeListener;

public class LoadWorldAsync extends AsyncTask<Void, Void, Void> {

    private int width;
    private int height;
    private Context context;
    private float waterFreq;
    private float mountainFreq;

    private AsyncTaskCallback asyncTaskCallback;

    private MapGenerator mapGenerator;
    private final LifeformChangeListener listener;
    private World world;
    private Tile[][] map;
    private Bitmap bm;

    public LoadWorldAsync(Context context, int width, int height, float waterFrequency,
                          float mountainFrequency, LifeformChangeListener listener, MapGenerator mapGenerator) {
        this.height = height;
        this.width = width;
        this.context = context;
        this.waterFreq = waterFrequency;
        this.mountainFreq = mountainFrequency;
        this.listener = listener;
        this.mapGenerator = mapGenerator;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        map = mapGenerator.generateRandomMap(width, height, waterFreq, mountainFreq);
        bm = mapGenerator.generateRandomMapBitmap(width, height, Tile.getTileSize(), map);
        SaveGame.saveBitmapToInternalStorage(context, bm, SaveGame.BITMAPFILE);

        map = MapUtils.reduceTileArray(map, MapUtils.tileMapDivisor);

        world = new World(map, listener);


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        asyncTaskCallback.onTaskCompleted(world, map, bm);
    }

    public void setAsyncTaskCallback(AsyncTaskCallback callback) {
        this.asyncTaskCallback = callback;
    }

    public interface AsyncTaskCallback {
        void onTaskCompleted(World world, Tile[][] tilemap, Bitmap bitmap);
    }

}