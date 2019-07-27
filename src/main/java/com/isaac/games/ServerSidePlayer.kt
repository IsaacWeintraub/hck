package com.isaac.games

class ServerSidePlayer {

    var score: Int = 0
        private set
    var playerName: String? = null
    var bid: Int = 0
    var tricks: Int = 0
        private set

    init {
        score = 0
        playerName = ""
        bid = 0
        tricks = 0
    }

    fun setTricks(addOrClear: Boolean) {
        if (addOrClear) {
            tricks += 1
        } else {
            tricks = 0
        }
    }

    fun addScore(delta: Int) {
        score += delta
    }

}