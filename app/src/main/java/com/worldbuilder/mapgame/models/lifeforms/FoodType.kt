package com.worldbuilder.mapgame.models.lifeforms

enum class FoodType(private val value: String) {
    Herbivore("Herbivore"),
    Carnivore("Carnivore"),
    Omnivore("Omnivore");

    override fun toString(): String {
        return value
    }
}