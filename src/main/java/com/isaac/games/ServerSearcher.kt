package com.isaac.games

import javafx.collections.ObservableList
import javafx.collections.FXCollections
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException
import java.net.InetAddress
import java.net.Socket

class ServerSearcher : Thread("ServerSearcher") {

    val servers: ObservableList<ServerData>

    init {
        servers = FXCollections.observableArrayList()
    }

    override fun run() {
        println("ServerSearcher thread has entered run()")
        try {
            val hostName = InetAddress.getLocalHost().hostAddress
            val hnPart = hostName.substring(0, hostName.lastIndexOf('.') + 1)
            for (i in 2..254) {
                var sock: Socket? = null
                try {
                    val test = InetAddress.getByName(
                            hnPart + Integer.toString(i))
                    if (test.isReachable(25)) {
                        sock = Socket(test, OhHckServer.INFO_PORT)
                        val `in` = BufferedReader(
                                InputStreamReader(sock.getInputStream()))
                        var input: String? = `in`.readLine()
                        while (input != null) {
                            if (input != "") {
                                servers.add(ServerData.of(input))
                                break
                            }
                            input = `in`.readLine()
                        }
                        `in`.close()
                        sock.close()
                    }
                } catch (e: IOException) {
                }

            }
        } catch (e: Exception) {
        }

        println("ServerSearcher thread is returning from run()")
    }

}