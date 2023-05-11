package com.worldbuilder.mapgame.ui.dialogs
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.worldbuilder.mapgame.databinding.DialogMapClickBinding
import com.worldbuilder.mapgame.models.Position

class MapClickDialog(
    private val context: Context,
    private val onAddLifeFormClick: () -> Unit,
    private val onViewLifeFormClick: () -> Unit
) {
    private lateinit var popupWindow: PopupWindow
    private lateinit var binding : DialogMapClickBinding

    fun showPopupWindow(position : Position) {
        // Inflate the popup layout
        binding = DialogMapClickBinding.inflate(LayoutInflater.from(context))


        // Create the PopupWindow and set its attributes
        popupWindow = PopupWindow(binding.root, -2, -2)
        popupWindow.isFocusable = true
        popupWindow.update()


        binding.btnAddLifeform.setOnClickListener {
            //show Add Lifeform dialog
            onAddLifeFormClick()
            closePopupWindow(binding)
        }
        binding.btnViewLifeforms.setOnClickListener {
            //show Recycler with Lifeforms in the area. Stats on the lifeforms
            onViewLifeFormClick()
            popupWindow.dismiss()
            closePopupWindow(binding)
        }



        // Show the PopupWindow at the clicked location
        popupWindow.showAtLocation(binding.root, 0, position.x, position.y)

        // Apply open Animation
        val openAnimation = Animations().openAnimation()
        binding.root.startAnimation(openAnimation)
    }


    private fun closePopupWindow(binding: DialogMapClickBinding) {
        val closeAnimation = Animations().closeAnimation()
        binding.root.startAnimation(closeAnimation)

        closeAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                popupWindow.dismiss()
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })
    }
}