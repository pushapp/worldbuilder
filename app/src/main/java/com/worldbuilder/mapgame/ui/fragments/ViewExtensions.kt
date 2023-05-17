package com.worldbuilder.mapgame.ui.fragments

import android.view.View

fun View.setIsVisible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}