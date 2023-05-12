package com.worldbuilder.mapgame.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.worldbuilder.mapgame.databinding.DialogCustomizeWorldBinding;

public class CustomizeWorldDialog extends AppCompatDialogFragment {

    private CustomizeWorldDialogListener listener;
    private DialogCustomizeWorldBinding binding;

    public interface CustomizeWorldDialogListener {
        void onCreateWorld(float waterFrequency, float mountainFrequency);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (CustomizeWorldDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement CustomizeWorldDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        binding = DialogCustomizeWorldBinding.inflate(getLayoutInflater());

        builder.setView(binding.getRoot());

        binding.btnSave.setOnClickListener(v -> {
            float waterFrequency = binding.seekbarWaterFrequency.getProgress() / 100f;
            float mountainFrequency = binding.seekbarMountainFrequency.getProgress() / 100f;
            listener.onCreateWorld(waterFrequency, mountainFrequency);
            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());

        return builder.create();
    }
}
