package com.worldbuilder.mapgame.ui.dialogs

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.worldbuilder.mapgame.R

class AddAnimalNodeView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val nodeIcon: ImageView
    private val nodeTitle: TextView
    private val nodeDescription: TextView

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.tech_tree_node_view, this, true)

        nodeIcon = findViewById(R.id.node_icon)
        nodeTitle = findViewById(R.id.node_title)
        nodeDescription = findViewById(R.id.node_description)
    }

    fun setNodeIcon(@DrawableRes iconRes: Int) {
        nodeIcon.setImageResource(iconRes)
    }

    fun setNodeTitle(title: String) {
        nodeTitle.text = title
    }

    fun setNodeDescription(description: String) {
        nodeDescription.text = description
    }
}