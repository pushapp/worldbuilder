package com.worldbuilder.mapgame.models.lifeform

enum class LifeformType(val id: Int) {
    Plant(1),
    Herbivore(2),
    Carnivore(3),
    Omnivore(4);

    override fun toString(): String {
        return when (id) {
            Plant.id -> "Plant"
            Herbivore.id -> "Herbivore"
            Carnivore.id -> "Carnivore"
            Omnivore.id -> "Omnivore"
            else -> "n/a"
        }
    }

    companion object {
        fun animalLifeforms(): List<LifeformType> {
            return listOf(Herbivore, Carnivore, Omnivore)
        }
    }
}