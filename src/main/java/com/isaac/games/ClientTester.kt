package com.isaac.games

import java.util.Scanner

object ClientTester {

    lateinit var player: ClientSidePlayer

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val p = InputParser()
        p.start()
        player = ClientSidePlayer("127.0.0.1")
        //p.stahp();
    }

    internal class InputParser : Thread("ClientTester\$InputParser-$num") {

        var input: Scanner
        var stopped: Boolean = false

        init {
            num++
            input = Scanner(System.`in`)
            stopped = false
        }

        override fun run() {
            while (!stopped) {
                if (input.hasNext()) {
                    player.client().transmit(input.nextLine())
                }
            }
        }

        fun stahp() {
            stopped = true
        }

        companion object {
            var num = 1
        }
    }
}