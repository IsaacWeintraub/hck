package com.isaac.games

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import java.net.UnknownHostException

class OhHckClient @Throws(IOException::class)
constructor(host: String, private val player: ClientSidePlayer) : Thread("OhHckClient") {

    private val out: PrintWriter
    private val socket: Socket
    private val `in`: BufferedReader

    init {
        socket = Socket(host, OhHckServer.PORT)
        out = PrintWriter(socket.getOutputStream(), true)
        `in` = BufferedReader(InputStreamReader(socket.getInputStream()))
    }

    override fun run() {
        println("OhHckClient thread has entered run()")
        try {
            var fromServer: String? = `in`.readLine()
            while (fromServer != null) {
                player.process(fromServer)
                if (fromServer == "STOP") {
                    break
                }
                fromServer = `in`.readLine()
            }
            socket.close()
            out.close()
            `in`.close()
        } catch (e: IOException) {
            player.process("CLIENT ERROR " + e.javaClass.name)
        }

        println("OhHckClient thread is returning from run()")
    }

    fun transmit(message: String) {
        out.println(message)
    }
}