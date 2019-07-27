package com.isaac.games

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

class ServerData(name: String, host: String, players: String, ipAddress: String) {

    private val name = SimpleStringProperty(this, "NA")
    private val host = SimpleStringProperty(this, "NA")
    private val players = SimpleStringProperty(this, "NA")
    private val ipAddress = SimpleStringProperty(this, "NA")

    init {

        this.name.bind(Bindings.createStringBinding(OhHckGui.HckFormatter(
                name.replace("H[Ee][Ll][Ll]".toRegex(), "\\$")
                        .replace("h[Ee][Ll][Ll]".toRegex(), "%")), OhHckGui.censored))
        this.host.bind(Bindings.createStringBinding(OhHckGui.HckFormatter(
                host.replace("H[Ee][Ll][Ll]".toRegex(), "\\$")
                        .replace("h[Ee][Ll][Ll]".toRegex(), "%")), OhHckGui.censored))
        this.players.set(players)
        this.ipAddress.set(ipAddress)
    }

    fun getName(): String {
        return name.get()
    }

    fun nameProperty(): StringProperty {
        return name
    }

    fun getHost(): String {
        return host.get()
    }

    fun hostProperty(): StringProperty {
        return host
    }

    fun getPlayers(): String {
        return players.get()
    }

    fun playersProperty(): StringProperty {
        return players
    }

    fun getIpAddress(): String {
        return ipAddress.get()
    }

    fun ipAddressProperty(): StringProperty {
        return ipAddress
    }

    companion object {

        fun of(input: String): ServerData {
            return ServerData(input.substring(5, input.indexOf("HOST=") - 1),
                    input.substring(input.indexOf("HOST=") + 5,
                            input.indexOf("PLAYERS=") - 1),
                    input.substring(input.indexOf("PLAYERS=") + 8,
                            input.indexOf("IPADDRESS=") - 1),
                    input.substring(input.indexOf("IPADDRESS=") + 10))
        }
    }

}