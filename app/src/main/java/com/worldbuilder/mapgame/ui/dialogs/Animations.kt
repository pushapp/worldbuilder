package com.worldbuilder.mapgame.ui.dialogs

import android.view.animation.Animation
import android.view.animation.ScaleAnimation

class Animations {

    fun openAnimation(): Animation {
        return ScaleAnimation(
            0f, 1f,
            0f, 1f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 1f
        ).apply {
            duration = 300
            fillAfter = true
        }
    }

    fun closeAnimation(): Animation {
        return ScaleAnimation(
            1f, 0f,
            1f, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 1f
        ).apply {
            duration = 300
            fillAfter = true
        }
    }
}