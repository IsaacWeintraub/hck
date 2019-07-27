package com.isaac.games

class OhHckGame {

    private var state: State? = null
    private val clients: MutableList<OhHckServer.ServerThread>
    private var upperLimit: Int = 0
    private var deck: Deck? = null
    private var dealer: Int = 0
    private var numCards: Int = 0
    private var bidTotal: Int = 0
    private var plon: Int = 0
    private val played: MutableList<TrackingCard>
    private var ctr: Int = 0
    private var direction: Boolean = false
    private var nsp: Int = 0

    enum class State {
        WAITING, STARTED, BIDDING, TRICK
    }

    init {
        this.state = State.WAITING
        this.clients = ArrayList()
        this.numCards = 1
        this.played = ArrayList()
        this.direction = true
    }

    fun addClient(client: OhHckServer.ServerThread) {
        clients.add(client)
    }

    // SCRAPPY STATE MACHINE
    fun process(input: String?, sender: OhHckServer.ServerThread) {
        if (input == null) {
            if (state == State.WAITING) {
                sender.player().playerName = "Player" + sender.currentPlayers
                sender.transmit("WELCOME " + sender.player().playerName)
            } else {
                sender.transmit("IN PROGRESS")
            }
        } else if (input == "STOP" && sender === clients[0]) {
            transmitAll("STOP")
        } else {
            try {
                var matched = false
                when (state) {
                    OhHckGame.State.WAITING -> if (input.length >= 6
                            && input.substring(0, 6) == "START "
                            && sender === clients[0] && clients.size >= 3) {
                        upperLimit = Integer.parseInt(
                                input.substring(input.lastIndexOf(' ') + 1))
                        deck = Deck(Integer.parseInt(input.substring(
                                input.indexOf(' ') + 1, input.indexOf(' ') + 2)))
                        matched = true
                        state = State.STARTED
                        transmitAll(scores())
                        dealer = (Math.random() * clients.size).toInt()
                        clients[dealer].transmit("DEALER")
                    }
                    OhHckGame.State.STARTED -> if (input == "BEGDEAL" && sender === clients[dealer]) {
                        matched = true
                        state = State.BIDDING
                        for (i in 0 until numCards) {
                            for (j in clients.indices) {
                                clients[(dealer + j + 1) % clients.size]
                                        .transmit("DEAL " + deck!!.pop().toString())
                            }
                        }
                        eraseBids()
                        transmitAll(bids())
                        clients[(dealer + 1) % clients.size]
                                .transmit("PLACEBID " + bidRestriction(dealer + 1))
                        plon = dealer + 1
                        nsp = plon % clients.size
                    }
                    OhHckGame.State.BIDDING -> if (input.length >= 4
                            && input.substring(0, 4) == "BID "
                            && sender === clients[plon % clients.size]) {
                        sender.player().bid = Integer.parseInt(input.substring(4))
                        updateBidTotal()
                        plon++
                        matched = true
                        transmitAll(bids())
                        if (plon < dealer + clients.size + 1) {
                            clients[plon % clients.size]
                                    .transmit("PLACEBID " + bidRestriction(plon))
                        } else {
                            state = State.TRICK
                            ctr = 0
                            clearTricks()
                            played.clear()
                            transmitAll("PLAYED $played")
                            clients[plon % clients.size].transmit("PLAY")
                        }
                    }
                    OhHckGame.State.TRICK -> if (input.length >= 8
                            && input.substring(0, 8) == "PLAYING "
                            && sender === clients[plon % clients.size]) {
                        played.add(TrackingCard(
                                Card.fromString(input.substring(8)), sender,
                                System.currentTimeMillis()))
                        plon++
                        matched = true
                        transmitAll("PLAYED $played")
                        if (plon % clients.size != nsp) {
                            clients[plon % clients.size].transmit("PLAY")
                        } else {
                            transmitAll("TRICK " + winner())
                            ctr++
                            if (ctr < numCards) {
                                played.clear()
                                transmitAll("PLAYED $played")
                                clients[plon % clients.size].transmit("PLAY")
                            } else {
                                ctr = 0
                                if (numCards == upperLimit) {
                                    direction = false
                                }
                                numCards += if (direction) 1 else -1
                                assignScores()
                                transmitAll(scores())
                                if (numCards != 0) {
                                    state = State.STARTED
                                    deck!!.reset()
                                    dealer++
                                    dealer %= clients.size
                                    clients[dealer].transmit("DEALER")
                                } else {
                                    transmitAll(results())
                                    transmitAll("STOP")
                                }
                            }
                        }
                    }
                }
                if (!matched) {
                    sender.transmit("NOT RECOGNIZED")
                }
            } catch (t: Throwable) {
                sender.transmit("ERROR " + t.javaClass.name)
            }

        }
    }

    private fun assignScores() {
        for (c in clients) {
            if (c.player().tricks == c.player().bid) {
                c.player().addScore(10 + c.player().bid)
            }
        }
    }

    private fun winner(): String {
        val led = played[0].card.suit
        played.sortWith(Comparator { c1, c2 ->
            val s1 = c1.card.suit
            val s2 = c2.card.suit
            if (s1 === Card.Suit.SPADES && s2 !== Card.Suit.SPADES) {
                69
            } else if (s1 !== Card.Suit.SPADES && s2 === Card.Suit.SPADES) {
                -69
            } else if (s1 === led && s2 !== led) {
                24
            } else if (s1 !== led && s2 === led) {
                -24
            } else {
                val diff = c1.card.rank - c2.card.rank
                if (diff == 0)
                    ((c2.timePlayed - c1.timePlayed) / 1000).toInt()
                else
                    diff
            }
        })
        val t = played[played.size - 1].playedBy
        t.player().setTricks(true)
        plon = clients.indexOf(t)
        nsp = plon
        return t.player().playerName!!
    }

    private fun bidRestriction(clindex: Int): Int {
        return if (clindex % clients.size == dealer) numCards - bidTotal else -1
    }

    private fun results(): String {
        return scores(true)
    }

    private fun scores(isFinal: Boolean = false): String {
        val ret = StringBuilder()
        if (!isFinal) {
            ret.append("SCORES [")
        } else {
            ret.append("RESULTS [")
        }
        for (c in clients) {
            ret.append(c.player().playerName)
            ret.append('=')
            ret.append(c.player().score)
            ret.append(", ")
        }
        return ret.substring(0, ret.length - 2) + ']'
    }

    private fun updateBidTotal() {
        bidTotal = 0
        for (c in clients) {
            if (c.player().bid != -1) {
                bidTotal += c.player().bid
            }
        }
    }

    private fun eraseBids() {
        for (c in clients) {
            c.player().bid = -1
        }
        updateBidTotal()
    }

    private fun clearTricks() {
        for (c in clients) {
            c.player().setTricks(false)
        }
    }

    private fun bids(): String {
        val ret = StringBuilder()
        ret.append("BIDS [")
        for (c in clients) {
            ret.append(c.player().playerName)
            ret.append('=')
            ret.append(c.player().bid)
            ret.append(", ")
        }
        return ret.substring(0, ret.length - 2) + ']'
    }

    private fun transmitAll(message: String) {
        for (c in clients) {
            c.transmit(message)
        }
    }

    inner class TrackingCard(val card: Card, val playedBy: OhHckServer.ServerThread,
                             val timePlayed: Long) {

        override fun toString(): String {
            return playedBy.player().playerName + "=" + card.toString()
        }
    }

}