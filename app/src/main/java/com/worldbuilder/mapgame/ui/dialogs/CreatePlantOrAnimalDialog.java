package com.worldbuilder.mapgame.ui.dialogs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import com.worldbuilder.mapgame.Lifeforms;
import com.worldbuilder.mapgame.R;
import com.worldbuilder.mapgame.databinding.DialogAddLifeformBinding;
import com.worldbuilder.mapgame.models.ItemCreationParams;
import com.worldbuilder.mapgame.models.lifeforms.FoodType;
import com.worldbuilder.mapgame.ui.OnItemSelectedSimpleListener;
import com.worldbuilder.mapgame.ui.OnSeekBarSimpleChangeListener;

import java.util.Locale;

public class CreatePlantOrAnimalDialog extends AppCompatDialog {

    public interface CreatePlantOrAnimalDialogListener {
        void OnLifeformAddSelected(ItemCreationParams costs);
    }

    private static final float windowWidthRatio = .9f;
    private static final float windowHeightRatio = .9f;

    private DialogAddLifeformBinding binding;

    private ItemCreationParams params;

    private final CreatePlantOrAnimalDialogListener listener;

    public CreatePlantOrAnimalDialog(@NonNull Context context) {
        super(context);
        try {
            listener = (CreatePlantOrAnimalDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("dialog owner must implement CreatePlantOrAnimalDialogListener");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogAddLifeformBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        resizeDialogWindow();

        bindSeekBars();
        bindButtons();

        selectPlantCreation();
    }

    private void resizeDialogWindow() {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        int width = (int) (dm.widthPixels * windowWidthRatio);
        int height = (int) (dm.heightPixels * windowHeightRatio);

        getWindow().setLayout(width, height);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    void bindSeekBars() {
        //seeding distance
        binding.seedDistanceseek.setOnSeekBarChangeListener(new OnSeekBarSimpleChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (!b) return;
                params.seedDistCost = progress;
                params.seedingDistanceProgress = progress;

                params.cost = params.seedDistCost + params.propCost + params.lifespanCost + 100;
                onCostsChanged();
            }
        });

        //speed
        binding.speedSeek.setOnSeekBarChangeListener(new OnSeekBarSimpleChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (!b) return;
                params.speedCost = progress * 4;
                params.seedSpeedProgress = progress;

                params.cost = params.foodCost + params.propCost + params.speedCost + params.lifespanCost;
                onCostsChanged();
            }
        });

        //elevation habitat
        binding.elevationHabitatseek.setOnSeekBarChangeListener(new OnSeekBarSimpleChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (!b) return;
                params.elevationProgress = progress;
                //TODO: complete cost calculation
                onCostsChanged();
            }
        });

        //life span
        binding.lifespanseek.setOnSeekBarChangeListener(new OnSeekBarSimpleChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (!b) return;
                params.lifespanCost = progress;
                params.lifeSpanProgress = progress;

                if (params.isPlantSelected) {
                    params.cost = params.seedDistCost + params.propCost + params.lifespanCost + 150;
                } else {
                    params.cost = params.foodCost + params.propCost + params.speedCost + params.lifespanCost;
                }
                onCostsChanged();
            }
        });

        //propagation rate
        binding.plantdispersion.setOnSeekBarChangeListener(new OnSeekBarSimpleChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (!b) return;
                params.propCost = progress * 2;
                params.propagationRateProgress = progress;

                if (params.isPlantSelected) {
                    params.cost = params.seedDistCost + params.propCost + params.lifespanCost + 150;
                } else {
                    params.cost = params.foodCost + params.propCost + params.speedCost + params.lifespanCost;
                }
                onCostsChanged();
            }
        });

        ArrayAdapter<FoodType> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, FoodType.values());
        binding.foodTypeSpinner.setAdapter(adapter);
        //food type
        binding.foodTypeSpinner.setOnItemSelectedListener(new OnItemSelectedSimpleListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                params.selectedFoodType = (FoodType) binding.foodTypeSpinner.getSelectedItem();

                if (params.selectedFoodType == FoodType.Herbivore) {
                    params.foodCost = 1000;
                }
                if (params.selectedFoodType == FoodType.Carnivore) {
                    params.foodCost = 10000;
                }
                params.cost = params.foodCost + params.propCost + params.speedCost + params.lifespanCost;
                onCostsChanged();
            }
        });
    }

    private void onCostsChanged() {
        binding.cost.setText(String.format(Locale.getDefault(), "Cost: %d", params.cost));
    }

    private void bindButtons() {
        binding.planttv.setOnClickListener(v -> selectPlantCreation());
        binding.animaltv.setOnClickListener(v -> selectAnimalCreation());
        binding.buttonsLL.setVisibility(View.VISIBLE);
        binding.addBtn.setOnClickListener(v -> {
            listener.OnLifeformAddSelected(params);
            dismiss();
        });

        binding.cancelBtn.setOnClickListener(v -> dismiss());
    }

    private void selectPlantCreation() {
        resetCreationParameters();
        binding.planttv.setBackgroundResource(R.color.colorPrimary);
        binding.animaltv.setBackgroundResource(R.color.gray);
        params.isPlantSelected = true;
        binding.seedDistancelayout.setVisibility(View.VISIBLE);
        binding.speedlayout.setVisibility(View.GONE);
        binding.foodTypeLayout.setVisibility(View.GONE);
        binding.itemToCreateIv.setImageResource(Lifeforms.genericPlantDrawable);

        params.cost = params.propCost + params.seedDistCost + params.lifespanCost + 150;
        onCostsChanged();
    }

    private void selectAnimalCreation() {
        resetCreationParameters();
        binding.planttv.setBackgroundResource(R.color.gray);
        binding.animaltv.setBackgroundResource(R.color.colorPrimary);
        params.isPlantSelected = false;
        binding.seedDistancelayout.setVisibility(View.GONE);
        binding.speedlayout.setVisibility(View.VISIBLE);
        binding.foodTypeLayout.setVisibility(View.VISIBLE);
        binding.itemToCreateIv.setImageResource(Lifeforms.genericAnimalDrawable);

        params.cost = params.foodCost + params.propCost + params.speedCost + params.lifespanCost;
        onCostsChanged();
    }

    private void resetCreationParameters() {
        params = new ItemCreationParams();
        binding.seedDistanceseek.setProgress(params.seedingDistanceProgress);
        binding.speedSeek.setProgress(params.seedSpeedProgress);
        binding.elevationHabitatseek.setProgress(params.elevationProgress);
        binding.lifespanseek.setProgress(params.lifeSpanProgress);
        binding.plantdispersion.setProgress(params.propagationRateProgress);
        binding.foodTypeSpinner.setSelection(0);
    }
}
