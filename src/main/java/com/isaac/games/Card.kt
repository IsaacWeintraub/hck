package com.isaac.games

open class Card(val suit: Suit, val rank: Int) {

    enum class Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES
    }

    init {
        if (rank < 2 || rank > 14) {
            throw IllegalArgumentException("Bad rank: $rank")
        }
    }

    override fun equals(o: Any?): Boolean {
        if (o !is Card) {
            return false
        }
        val that = o as Card?
        return this.suit == that!!.suit && this.rank == that.rank
    }

    override fun toString(): String {
        var ret = ""
        when (rank) {
            JACK -> ret += "JACK"
            QUEEN -> ret += "QUEEN"
            KING -> ret += "KING"
            ACE -> ret += "ACE"
            else -> ret += Integer.toString(rank)
        }
        return ret + "of" + suit.name
    }

    companion object {

        val JACK = 11
        val QUEEN = 12
        val KING = 13
        val ACE = 14

        fun fromString(str: String): Card {
            val rs = str.substring(0, str.indexOf('o'))
            val r: Int
            if (rs == "JACK") {
                r = 11
            } else if (rs == "QUEEN") {
                r = 12
            } else if (rs == "KING") {
                r = 13
            } else if (rs == "ACE") {
                r = 14
            } else {
                r = Integer.parseInt(rs)
            }
            return Card(Suit.valueOf(str.substring(str.indexOf('f') + 1)), r)
        }
    }
}