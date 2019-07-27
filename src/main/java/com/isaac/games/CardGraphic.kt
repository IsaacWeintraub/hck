package com.isaac.games

import javafx.scene.image.ImageView

class CardGraphic(private val card: Card) : ImageView("images/" + sanitize(card.toString()) + ".bmp") {

    fun card(): Card {
        return card
    }

    override fun toString(): String {
        return card.toString()
    }

    override fun equals(o: Any?): Boolean {
        if (o !is CardGraphic) {
            return false
        }
        val that = o as CardGraphic?
        return this.card == that!!.card
    }

    companion object {
        private fun sanitize(input: String): String {
            val x = input.indexOf('$')
            return if (x == -1) input else input.substring(0, x)
        }
    }
}