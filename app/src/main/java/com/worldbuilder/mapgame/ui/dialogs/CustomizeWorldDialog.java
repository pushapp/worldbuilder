package com.worldbuilder.mapgame.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.worldbuilder.mapgame.R;

public class CustomizeWorldDialog extends AppCompatDialogFragment {

    private Button btnSave;
    private SeekBar seekBarWaterFrequency;
    private SeekBar seekBarMountainFrequency;
    private CustomizeWorldDialogListener listener;

    public interface CustomizeWorldDialogListener {
        void onCreateWorld(float waterFrequency, float mountainFrequency);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (CustomizeWorldDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CustomizeWorldDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_customize_world, null);

        builder.setView(view);

        seekBarWaterFrequency = view.findViewById(R.id.seekbar_water_frequency);
        seekBarMountainFrequency = view.findViewById(R.id.seekbar_mountain_frequency);
        btnSave = view.findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float waterFrequency = seekBarWaterFrequency.getProgress() / 100f;
                float mountainFrequency = seekBarMountainFrequency.getProgress() / 100f;
                listener.onCreateWorld(waterFrequency, mountainFrequency);
                dismiss();
            }
        });

        return (Dialog) builder.create();
    }
}
