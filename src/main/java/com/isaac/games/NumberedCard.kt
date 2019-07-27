package com.isaac.games

class NumberedCard : Card {

    private var id: Long = 0

    constructor(card: Card) : super(card.suit, card.rank) {
        id = counter++
    }

    constructor(card: Card, id: Long) : super(card.suit, card.rank) {
        this.id = id
    }

    override fun toString(): String {
        return super.toString() + "$" + id
    }

    override fun equals(o: Any?): Boolean {
        return super.equals(o) && (o as NumberedCard).id == this.id
    }

    companion object {
        private var counter: Long = 0
    }
}