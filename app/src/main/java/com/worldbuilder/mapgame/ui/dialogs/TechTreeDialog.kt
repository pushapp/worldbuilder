package com.worldbuilder.mapgame.ui.dialogs
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import com.worldbuilder.mapgame.R
import com.worldbuilder.mapgame.ui.dialogs.TechTreeNodeView

class TechTreeDialog(context: Context) : Dialog(context) {

    public val ADAPTATION_1 = 1

    interface TechTreeClickListener {
        fun onTechNodeClick(nodeId: Int)
    }

    private var techTreeClickListener: TechTreeClickListener? = null

    fun setTechTreeClickListener(listener: TechTreeClickListener) {
        techTreeClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.tech_tree)

        // Set click listeners for tech nodes
        val category1Tier1Node = findViewById<TechTreeNodeView>(R.id.category1_tier1_node)
        category1Tier1Node.setOnClickListener { onTechNodeClick(ADAPTATION_1) }

        // Add click listeners for other tech nodes
    }

    private fun onTechNodeClick(nodeId: Int) {
        techTreeClickListener?.onTechNodeClick(nodeId)
        dismiss()
    }
}