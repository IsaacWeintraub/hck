package com.isaac.games

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import java.io.IOException
import java.util.Arrays

class ClientSidePlayer @Throws(IOException::class)
constructor(host: String) {

    private val name = SimpleStringProperty(this, "NA")
    private val score = SimpleIntegerProperty(this, "NA")
    private val dealing = SimpleBooleanProperty(this, "NA")
    private val dealt = SimpleBooleanProperty(this, "NA")
    val hand = FXCollections.observableArrayList<NumberedCard>()
    private val bidTotal = SimpleIntegerProperty(this, "NA")
    private val bid = SimpleIntegerProperty(this, "NA")
    private val bidRestriction = SimpleIntegerProperty(this, "NA")
    private val bidding = SimpleBooleanProperty(this, "NA")
    private val playing = SimpleBooleanProperty(this, "NA")
    val played = FXCollections.observableArrayList<NumberedCard>()
    private val tricks = SimpleIntegerProperty(this, "NA")
    private val tookTrick = SimpleStringProperty(this, "NA")
    private val started = SimpleBooleanProperty(this, "NA")
    private val place = SimpleIntegerProperty(this, "NA")
    private val client: OhHckClient

    val isDealing: Boolean
        get() = dealing.get()

    val isBidding: Boolean
        get() = bidding.get()

    val isPlaying: Boolean
        get() = playing.get()

    val isStarted: Boolean
        get() = started.get()

    init {
        score.set(-1)
        dealing.set(false)
        dealt.set(false)
        bid.set(-1)
        bidRestriction.set(-1)
        playing.set(false)
        client = OhHckClient(host, this)
        client.start()
    }

    fun nameProperty(): StringProperty {
        return name
    }

    fun getName(): String {
        return name.get()
    }

    fun scoreProperty(): IntegerProperty {
        return score
    }

    fun getScore(): Int {
        return score.get()
    }

    fun dealingProperty(): BooleanProperty {
        return dealing
    }

    fun dealtProperty(): BooleanProperty {
        return dealt
    }

    fun hasDealt(): Boolean {
        return dealt.get()
    }

    fun bidTotalProperty(): IntegerProperty {
        return bidTotal
    }

    fun getBidTotal(): Int {
        return bidTotal.get()
    }

    fun bidProperty(): IntegerProperty {
        return bid
    }

    fun getBid(): Int {
        return bid.get()
    }

    fun bidRestrictionProperty(): IntegerProperty {
        return bidRestriction
    }

    fun getBidRestriction(): Int {
        return bidRestriction.get()
    }

    fun biddingProperty(): BooleanProperty {
        return bidding
    }

    fun playingProperty(): BooleanProperty {
        return playing
    }

    fun tricksProperty(): IntegerProperty {
        return tricks
    }

    fun getTricks(): Int {
        return tricks.get()
    }

    fun tookTrickProperty(): StringProperty {
        return tookTrick
    }

    fun getTookTrick(): String {
        return tookTrick.get()
    }

    fun startedProperty(): BooleanProperty {
        return started
    }

    fun placeProperty(): IntegerProperty {
        return place
    }

    fun getPlace(): Int {
        return place.get()
    }

    fun client(): OhHckClient {
        return client
    }

    fun canPlay(card: Card): Boolean {
        if (played.size == 0) {
            return true
        } else {
            val led = played[0].suit
            if (card.suit === led) {
                return true
            } else {
                var has = false
                for (c in hand) {
                    has = has or (c.suit === led)
                }
                return !has
            }
        }
    }

    fun sendToServer(output: String) {
        var output = output
        val x: Int = output.indexOf(' ')
        val command = output.substring(0, if (x == -1) output.length else x)
        if (command == "PLAYING") {
            hand.remove(NumberedCard(
                    Card.fromString(output.substring(8, output.indexOf('$'))),
                    java.lang.Long.parseLong(output.substring(output.indexOf('$') + 1))))
            output = output.substring(0, output.indexOf('$'))
        } else if (command == "BID") {
            bid.set(Integer.parseInt(output.substring(4)))
        }
        client.transmit(output)
    }

    fun process(input: String) {
        if (input.length >= 8 && input.substring(0, 8) == "WELCOME ") {
            name.set(input.substring(8))
        }
        val x: Int = input.indexOf(' ')
        val command = input.substring(0, if (x == -1) input.length else x)
        if (command == "SCORES") {
            started.set(true)
            dealing.set(false)
            val oset = input.indexOf(getName()) + getName().length + 1
            val i: Int = input.indexOf(',', oset)
            score.set(Integer.parseInt(input.substring(oset,
                    if (i == -1) input.indexOf(']') else i)))
        } else if (command == "DEALER") {
            dealing.set(true)
            dealt.set(false)
        } else if (command == "DEAL") {
            played.clear()
            hand.add(NumberedCard(Card.fromString(input.substring(5))))
            bid.set(-1)
            tricks.set(0)
            tookTrick.set("")
        } else if (command == "BIDS") {
            bidding.set(false)
            bidRestriction.set(-1)
            var bt = 0
            for (str in input.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                val y: Int = str.indexOf(',')
                val end = if (y == -1) str.indexOf(']') else y
                if (end != -1) {
                    val bid = Integer.parseInt(str.substring(0, end))
                    if (bid != -1) {
                        bt += bid
                    }
                }
            }
            bidTotal.set(bt)
        } else if (command == "PLACEBID") {
            bidRestriction.set(Integer.parseInt(input.substring(9)))
            bidding.set(true)
        } else if (command == "PLAYED") {
            played.clear()
            playing.set(false)
            tookTrick.set("")
            val strs = input.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in 1 until strs.size) {
                val y: Int = strs[i].indexOf(',')
                val end = if (y == -1) strs[i].indexOf(']') else y
                played.add(NumberedCard(Card.fromString(strs[i].substring(0, end))))
            }
        } else if (command == "PLAY") {
            playing.set(true)
        } else if (command == "TRICK") {
            tookTrick.set(input.substring(6))
            if (input.substring(6) == getName()) {
                tricks.set(tricks.get() + 1)
            }
        } else if (command == "RESULTS") {
            started.set(false)
            val strs = input.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val results = IntArray(strs.size - 1)
            for (i in 1 until strs.size) {
                val y: Int = strs[i].indexOf(',')
                val end = if (y == -1) strs[i].indexOf(']') else y
                results[i - 1] = Integer.parseInt(strs[i].substring(0, end))
            }
            Arrays.sort(results)
            for (i in 1..results.size) {
                if (results[results.size - i] == getScore()) {
                    if (i < results.size && results[results.size - i] == results[results.size - i - 1]) {
                        place.set(i + 64)
                    } else {
                        place.set(i)
                    }
                    break
                }
            }
        }
    }
}