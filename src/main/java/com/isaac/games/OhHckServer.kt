package com.isaac.games

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class OhHckServer(private val serverName: String, private val maxPlayers: Int, private val hostName: String) : Thread("OhHckServer") {
    private var currentPlayers: Int = 0
    private val game: OhHckGame
    private var shouldContinue: Boolean = false
    private val itOpen: Boolean
    private var serverSocket: ServerSocket? = null

    init {
        this.itOpen = true
        this.currentPlayers = 0
        this.game = OhHckGame()
        shouldContinue = true
    }

    override fun run() {
        println("OhHckServer thread has entered run()")
        try {
            serverSocket = ServerSocket(PORT)
            InfoThread().start()
            while (shouldContinue) {
                while (currentPlayers < maxPlayers && shouldContinue) {
                    val sock = serverSocket!!.accept()
                    val st = ServerThread(sock)
                    st.start()
                }
            }
            if (serverSocket != null) {
                serverSocket!!.close()
            }
        } catch (e: IOException) {
            println("Exception in OhHckServer " + e.stackTrace)
        }

        println("OhHckServer thread is returning from run()")
    }

    inner class InfoThread : Thread("OhHckServer\$InfoThread") {

        override fun run() {
            println("OhHckServer\$InfoThread thread has entered run()")
            try {
                val ss = ServerSocket(INFO_PORT)
                while (itOpen) {
                    val s = ss.accept()
                    val out = PrintWriter(s.getOutputStream(), true)
                    out.println(String.format(
                            "NAME=%s HOST=%s PLAYERS=%d/%d IPADDRESS=%s",
                            serverName, hostName, currentPlayers, maxPlayers,
                            InetAddress.getLocalHost().hostAddress))
                    s.close()
                    out.close()
                }
                ss.close()
            } catch (e: IOException) {
            }

            println("OhHckServer\$InfoThread thread is returning from run()")
        }
    }

    inner class ServerThread(private val socket: Socket) : Thread("OhHckServer\$ServerThread-$currentPlayers") {
        private var out: PrintWriter? = null
        private val playerName: String? = null
        private val player: ServerSidePlayer

        var currentPlayers: Int = 0

        init {
            currentPlayers++
            this.player = ServerSidePlayer()
            game.addClient(this)
        }

        override fun run() {
            println("OhHckServer\$ServerThread thread has entered run()")
            try {
                out = PrintWriter(socket.getOutputStream(), true)
                val `in` = BufferedReader(
                        InputStreamReader(socket.getInputStream()))
                var input: String? = `in`.readLine()
                game.process(null, this)
                while (input != null && shouldContinue) {
                    game.process(input, this)
                    input = `in`.readLine()
                }
                socket.close()
                out!!.close()
                `in`.close()
            } catch (e: IOException) {
            }

            currentPlayers--
            println("OhHckServer\$ServerThread thread is returning from run()")
        }

        fun transmit(message: String) {
            if (message.contains("STOP")) {
                shouldContinue = false
                try {
                    serverSocket!!.close()
                } catch (e: IOException) {
                }

            }
            out!!.println(message)
        }

        fun player(): ServerSidePlayer {
            return player
        }
    }

    companion object {

        val PORT = 6969
        val INFO_PORT = 6970
    }

}