package com.worldbuilder.mapgame.models.lifeform

import com.worldbuilder.mapgame.Lifeform
import com.worldbuilder.mapgame.models.Position

interface LifeformChangeListener {
    fun onLifeFormCreated(lifeform: Lifeform)
    fun onLifeformRemoved(lifeform: Lifeform)
    fun onLifeformMoved(lifeform: Lifeform, newPosition: Position)
}