package com.worldbuilder.mapgame.ui.dialogs
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.worldbuilder.mapgame.R

class SimpleAddLifeform(context: Context) : Dialog(context) {

    companion object{
        const val PLANT : Int = 1
        const val HERBIVORE : Int = 2
        const val CARNIVORE : Int = 3
    }

    interface AddLifeformClickListener {
        fun onLifeformAdded(nodeId: Int)
    }

    private var addLifeformClickListener: AddLifeformClickListener? = null

    fun setLifeformClickListener(listener: AddLifeformClickListener) {
        addLifeformClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.simple_add_lifeform)

        // Set click listeners for tech nodes
        val plantNode = findViewById<AddAnimalNodeView>(R.id.plant_node)
        plantNode.setNodeTitle("Plant")
        plantNode.setNodeDescription("A basic plant that will spread naturally.")
        plantNode.setNodeIcon(R.drawable.yellowcactus)
        plantNode.setOnClickListener { onTechNodeClick(PLANT) }

        val herbivoreNode = findViewById<AddAnimalNodeView>(R.id.herbivore_node)
        herbivoreNode.setNodeIcon(R.drawable.goat)
        herbivoreNode.setNodeTitle("Herbivore")
        herbivoreNode.setNodeDescription("A basic animal that eats plants to survive.")
        herbivoreNode.setOnClickListener { onTechNodeClick(HERBIVORE) }
        // Add click listeners for other tech nodes

        val carnivoreNode = findViewById<AddAnimalNodeView>(R.id.carnivore_node)
        carnivoreNode.setNodeTitle("Carnivore")
        carnivoreNode.setNodeDescription("A basic animal that eats herbivores to survive")
        carnivoreNode.setNodeIcon(R.drawable.greyanimal)
        carnivoreNode.setOnClickListener { onTechNodeClick(CARNIVORE) }
    }

    private fun onTechNodeClick(nodeId: Int) {
        addLifeformClickListener?.onLifeformAdded(nodeId)
        dismiss()
    }
}