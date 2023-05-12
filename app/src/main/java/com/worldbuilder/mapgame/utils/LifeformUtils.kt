package com.worldbuilder.mapgame.utils

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import com.worldbuilder.mapgame.Lifeform
import com.worldbuilder.mapgame.MapUtils

object LifeformUtils {

    /** creates imageView of given lifeform but not adds to parent layout */
    fun createLifeformImageView(lifeform: Lifeform, context: Context): ImageView {
        val xPosition = MapUtils.TiletoPixelX(lifeform.position.x)
        val yPosition = MapUtils.TiletoPixelY(lifeform.position.y)
        Log.d("LifeformPosition", "X: $xPosition, Y: $yPosition")
        val layoutParams = RelativeLayout.LayoutParams(Lifeform.getImgSize(), Lifeform.getImgSize())
        layoutParams.leftMargin = xPosition
        layoutParams.topMargin = yPosition

        return ImageView(context).apply {
            setImageResource(lifeform.imgID)
            setLayoutParams(layoutParams)
            lifeform.imageView = this
        }
    }
}