package com.worldbuilder.mapgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.worldbuilder.mapgame.databinding.ActivityMainBinding;

public class LoadWorldAsync extends AsyncTask<Void, Void, Void> {

    private LottieAnimationView animationView;
    private int width;
    private int height;
    private Context context;
    private float waterFreq;
    private float mountainFreq;

    private AsyncTaskCallback asyncTaskCallback;

    private MapGenerator mapGenerator;
    private ActivityMainBinding binding;
    private World world;
    private Tile[][] map;
    private Bitmap bm;
    public LoadWorldAsync(LottieAnimationView animationView, Context context, int width, int height, float waterFrequency, float mountainFrequency, ActivityMainBinding binding, MapGenerator mapGenerator) {
        this.animationView = animationView;
        this.height = height;
        this.width = width;
        this.context = context;
        this.waterFreq = waterFrequency;
        this.mountainFreq = mountainFrequency;
        this.binding = binding;
        this.mapGenerator = mapGenerator;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        animationView.setVisibility(View.VISIBLE);
        binding.lifeFormContainer.setBackground(null);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        map = mapGenerator.generateRandomMap(width, height, waterFreq, mountainFreq);
        bm = mapGenerator.generateRandomMapBitmap(width, height, Tile.getTileSize(), map);
        SaveGame.saveBitmapToInternalStorage(context, bm, SaveGame.BITMAPFILE);

        map = MapUtils.reduceTileArray(map, MapUtils.tileMapDivisor);

        world = new World(map, binding.lifeFormContainer);


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        asyncTaskCallback.onTaskCompleted(world,map,bm);
        animationView.setVisibility(View.INVISIBLE);
    }

    public void setAsyncTaskCallback(AsyncTaskCallback callback){
        this.asyncTaskCallback = callback;
    }
    public interface AsyncTaskCallback {
        void onTaskCompleted(World world, Tile[][] tilemap, Bitmap bitmap);
    }

}