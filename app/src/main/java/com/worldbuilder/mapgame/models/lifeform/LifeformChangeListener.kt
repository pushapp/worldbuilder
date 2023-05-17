package com.worldbuilder.mapgame.models.lifeform

import com.worldbuilder.mapgame.Lifeform

interface LifeformChangeListener {
    fun onLifeFormCreated(lifeform: Lifeform?)
    fun onLifeformRemoved(lifeform: Lifeform?)
}