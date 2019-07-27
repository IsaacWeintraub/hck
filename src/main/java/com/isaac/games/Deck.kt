package com.isaac.games

import java.util.Collections
import java.util.LinkedList

class Deck(private val number: Int) : LinkedList<Card>() {

    init {
        reset()
    }

    fun reset() {
        clear()
        for (d in 0 until number) {
            addDeck()
        }
        for (c in 0 until 7 + number) {
            Collections.shuffle(this)
        }
    }

    private fun addDeck() {
        for (i in 2..14) {
            push(Card(Card.Suit.CLUBS, i))
            push(Card(Card.Suit.DIAMONDS, i))
            push(Card(Card.Suit.HEARTS, i))
            push(Card(Card.Suit.SPADES, i))
        }
    }
}