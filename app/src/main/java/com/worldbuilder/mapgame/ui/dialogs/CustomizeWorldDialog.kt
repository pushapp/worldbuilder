package com.worldbuilder.mapgame.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.worldbuilder.mapgame.databinding.DialogCustomizeWorldBinding
import java.lang.NullPointerException

interface CustomizeWorldDialogListener {
    fun onCreateWorld(waterFrequency: Float, mountainFrequency: Float)
}

class CustomizeWorldDialog : AppCompatDialogFragment() {
    private var listener: CustomizeWorldDialogListener? = null
    private lateinit var binding: DialogCustomizeWorldBinding

    fun setOnCreateWorldListener(listener: CustomizeWorldDialogListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCustomizeWorldBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnSave.setOnClickListener {
            val waterFrequency = binding.seekbarWaterFrequency.progress / 100f
            val mountainFrequency = binding.seekbarMountainFrequency.progress / 100f
            listener?.onCreateWorld(waterFrequency, mountainFrequency) ?: run {
                throw NullPointerException(
                    "required listener is null, make sure that you called " +
                            "CustomizeWorldDialog.setOnCreateWorldListener() method"
                )
            }
            dismiss()
        }
        binding.btnCancel.setOnClickListener { dismiss() }
    }
}