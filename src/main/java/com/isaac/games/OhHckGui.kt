package com.isaac.games

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringExpression
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.Spinner
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.scene.text.Text
import javafx.stage.Screen
import javafx.stage.Stage
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.Callable

class OhHckGui : Application() {

    private var starting: Scene? = null
    private var stage: Stage? = null
    private val counter = 0

    override fun start(stage: Stage) {
        this.stage = stage
        censored = SimpleBooleanProperty(false)
        hword = Bindings.createStringBinding(
                Callable { if (censored.get()) "H*ck" else "Hell" }, censored)
        val title = Text()
        title.textProperty().bind(Bindings.createStringBinding(
                HckFormatter("Oh $!"), hword))
        title.textAlignment = TextAlignment.CENTER
        title.font = Font.font("Futura", SCREEN_WIDTH / 9)
        stage.titleProperty().bind(Bindings.createStringBinding(
                HckFormatter("Oh $"), hword))
        val beginButton = Button("Begin")
        beginButton.prefWidth = SCREEN_WIDTH / 6
        beginButton.setOnAction { e -> openServerBrowser() }
        val instructionsButton = Button("Instructions")
        instructionsButton.prefWidth = SCREEN_WIDTH / 9
        instructionsButton.setOnAction { e -> showInstructions() }
        val censorButton = Button()
        censorButton.textProperty().bind(Bindings.createStringBinding(
                Callable { if (censored.get()) "Un-censor" else "Censor" }, censored))
        censorButton.prefWidth = SCREEN_WIDTH / 9
        censorButton.setOnAction { e -> censored.set(!censored.get()) }
        val quitButton = Button("Quit")
        quitButton.prefWidth = SCREEN_WIDTH / 9
        quitButton.setOnAction { e -> System.exit(0) }
        val smolButtons = HBox()
        smolButtons.alignment = Pos.CENTER
        smolButtons.spacing = SCREEN_WIDTH / 6
        smolButtons.children.addAll(instructionsButton, censorButton,
                quitButton)
        val components = VBox()
        components.alignment = Pos.CENTER
        components.spacing = SCREEN_HEIGHT / 15
        components.children.addAll(title, beginButton, smolButtons)
        starting = Scene(components)
        stage.scene = starting
        stage.isFullScreen = true
        stage.show()
    }

    private fun showInstructions() {
        val text = Text("Instructions")
        val components = VBox()
        val instructions = TextArea()
        instructions.isWrapText = true
        instructions.textProperty().bind(Bindings.createStringBinding(
                HckFormatter("Oh $ is a card game where the objective is to win tricks and satisfy your bids.\nA round starts with the dealer dealing everyone a certain number of cards. Once cards have been dealt, players place their bids. The person to the \"right\" of the dealer bids first. Your bid should be the number of tricks you think you can win. Each person places their bids one-by-one. For each round, the total number of bids cannot equal the number of tricks, so the dealer (who bids last) must bid accordingly. If you are the dealer, the game will tell you what you cannot bid. After everyone has bid, the person who bid first plays a card. All other players then play a card in turn. Each player must play a card that is the same suit as the first card played if he/she has one. Once everyone has played a card, the winner of the trick is determined as follows:\n\t- Aces are always high; that is, greater in rank than a king.\n\t- The highest card of the suit that was led (played at the start of the trick) wins unless any spades were played, in which case the highest spade played wins.\n\t- If multiple decks are being used and multiple equivalent cards satisfy the previous conditions, the card that was played first wins.\n\t- The person who played the winning card takes the trick.\nIf there are more tricks left in the round, whoever took the previous trick plays the first card for the next trick but the order of play is otherwise unchanged. After all of the tricks in a round have been taken, scores are updated. If the number of tricks that you took is the same as your bid, you get (10 plus your bid) points. Otherwise you get zero points. The game starts with a round where everyone is dealt one card. Each subsequent round will have one additional card until the upper limit, which is set when the game starts, is reached; after which the number of cards will decrease in each round. Once all rounds have been played, the highest score wins."),
                hword))
        instructions.isEditable = false
        instructions.setPrefSize(SCREEN_WIDTH * 0.75, SCREEN_HEIGHT * 0.5)
        components.spacing = SCREEN_HEIGHT / 108
        components.children.addAll(text, instructions, backButton())
        components.alignment = Pos.TOP_CENTER
        val instr = Scene(components)
        stage!!.scene = instr
    }

    private fun openServerBrowser() {
        val text = Text("Server Browser")
        val components = VBox()
        val servers = findServers()
        val table = createTable(servers)
        val refreshButton = Button("Refresh")
        refreshButton.setOnAction { e -> table.setItems(findServers()) }
        val createButton = Button("Create Server")
        createButton.setOnAction { e ->
            val ownerConts = HBox()
            val snlabel = Text("Server name: ")
            val snfield = TextField()
            val hnlabel = Text("Host name: ")
            val hnfield = TextField()
            val mplabel = Text("Maximum players: ")
            val npSelector = Spinner(
                    SpinnerValueFactory.IntegerSpinnerValueFactory(3, 16))
            val startButton = Button("Start server")
            startButton.disableProperty().bind(Bindings.or(Bindings.isEmpty(
                    snfield.textProperty()), Bindings.isEmpty(hnfield.textProperty())))
            startButton.setOnAction { me ->
                beginServer(
                        snfield.text, npSelector.value, hnfield.text)
            }
            ownerConts.children.addAll(snlabel, snfield, hnlabel, hnfield, mplabel, npSelector, startButton)
            components.children.add(2, ownerConts)
        }
        val connectButton = Button("Connect to Server")
        connectButton.setOnAction { e ->
            beginGame(table.selectionModel
                    .selectedItem.getIpAddress(), false)
        }
        connectButton.disableProperty().bind(
                Bindings.createBooleanBinding( Callable {
                    val sd = table.selectionModel.selectedItem
                    if (sd != null) {
                        val strs = sd.getPlayers().split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        strs [0] == strs[1]
                    } else {
                        true
                    }
                }, table.selectionModel.selectedItemProperty()))
        val buttons = HBox()
        buttons.children.addAll(refreshButton, createButton, connectButton,
                backButton())
        components.spacing = SCREEN_HEIGHT / 108
        components.children.addAll(text, table, buttons)
        components.alignment = Pos.TOP_CENTER
        val serverBrowser = Scene(components)
        stage!!.scene = serverBrowser
    }

    private fun backButton(): Button {
        val b = Button("Back")
        b.setOnAction { e -> stage!!.scene = starting }
        return b
    }

    private fun beginServer(name: String, maxPlayers: Int, host: String) {
        val serv = OhHckServer(name, maxPlayers, host)
        serv.start()
        beginGame("127.0.0.1", true)
    }

    private fun beginGame(ipAddress: String, privileged: Boolean) {
        val components = VBox()
        val game = VBox()
        components.children.addAll(game, backButton())
        try {
            val gameComponents = FXCollections.observableList(ArrayList<Node>(20))
            for (i in 0..19) {
                gameComponents.add(null)
            }
            val player = ClientSidePlayer(ipAddress)
            if (privileged) {
                val privComponents = VBox()
                val mlSelector = Spinner(
                        SpinnerValueFactory.IntegerSpinnerValueFactory(2, 15))
                val mlabel = Text("Upper limit (maximum number of tricks in a round): ")
                val maxLimit = HBox(mlabel, mlSelector)
                val dnSelector = Spinner(
                        SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9))
                val dlabel = Text("Number of decks in play: ")
                val deckNumber = HBox(dlabel, dnSelector)
                val psButton = Button("Start Game")
                psButton.setOnAction { e ->
                    privComponents.isVisible = false
                    player.sendToServer("START "
                            + dnSelector.value + " " + mlSelector.value)
                }
                privComponents.children.addAll(deckNumber, maxLimit, psButton)
                gameComponents[0] = privComponents
            }
            val scoreView = Text()
            scoreView.textProperty().bind(Bindings.createStringBinding(Callable
                    {
                        if (player.getScore() == -1)
                            "Waiting for game to start."
                        else
                            "Your score is " + player.getScore()
                    }, player.scoreProperty()))
            gameComponents[1] = scoreView
            val dealerConts = HBox()
            val clientConts = HBox()
            val dlabel = Text("You are the dealer for this round.")
            val dealButton = Button("Deal")
            dealButton.setOnAction { e ->
                player.dealtProperty().set(true)
                player.sendToServer("BEGDEAL")
            }
            dealButton.disableProperty().bind(player.dealtProperty())
            dealerConts.visibleProperty().bind(Bindings.and(
                    player.dealingProperty(), player.startedProperty()))
            /*  dealButton.disabledProperty().addListener((c, o, n) -> {
                if (!n) {
                    dealerConts.getChildren().add(1, dealButton);
                } else {
                    dealerConts.getChildren().remove(dealButton);
                }
            });*/
            dealerConts.children.addAll(dlabel, dealButton)
            gameComponents[2] = dealerConts
            val handConts = VBox()
            val hlabel = Text("Your hand:")
            val handView = FlowPane()
            player.hand.addListener { ch: ListChangeListener.Change<out NumberedCard> ->
                while (ch.next()) {
                    if (ch.wasAdded()) {
                        for (card in ch.addedSubList) {
                            Platform.runLater {
                                val cg = CardGraphic(card)
                                cg.setOnMouseClicked { e ->
                                    if (player.isPlaying && player.canPlay(cg.card())) {
                                        player.sendToServer("PLAYING $cg")
                                    }
                                }
                                cg.opacityProperty().bind(Bindings.createDoubleBinding(Callable
                                        {
                                            if (player.isPlaying) {
                                                if (player.canPlay(cg.card())) 1.0 else 0.5
                                            } else {
                                                1.0
                                            }
                                        }, player.played))
                                handView.children.add(cg)
                            }
                        }
                    } else if (ch.wasRemoved()) {
                        Platform.runLater {
                            for (card in ch.removed) {
                                val children = handView.children
                                val graphic = CardGraphic(card)
                                for (i in children.indices) {
                                    if (children[i] == graphic) {
                                        children.removeAt(i)
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }
            handView.maxWidthProperty().bind(stage!!.widthProperty())
            handConts.children.addAll(hlabel, handView)
            handConts.visibleProperty().bind(Bindings.not(Bindings.isEmpty(handView.children)))
            gameComponents[3] = handConts
            val bidtotalabel = Text()
            bidtotalabel.textProperty().bind(Bindings.createStringBinding(Callable
                    { "Total bids placed: " + player.getBidTotal() },
                    player.bidTotalProperty()))
            bidtotalabel.visibleProperty().bind(player.startedProperty())
            val bidlabel = Text()
            bidlabel.textProperty().bind(Bindings.createStringBinding(Callable
                    { if (player.getBid() == -1) "Place your bid: " else "Your bid is " + player.getBid() },
                    player.bidProperty()))
            val bidConts = HBox()
            val svf = SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1)
            svf.maxProperty().bind(Bindings.size(handView.children))
            val bidSelector = Spinner(svf)
            val bidButton = Button()
            bidButton.setOnAction { e -> player.sendToServer("BID " + bidSelector.value) }
            bidButton.disableProperty().bind(Bindings.createBooleanBinding(Callable
                    { bidSelector.value.toInt() == player.getBidRestriction() },
                    bidSelector.valueProperty(), player.bidRestrictionProperty()))
            bidButton.textProperty().bind(Bindings.createStringBinding(Callable
                    {
                        if (bidSelector.value.toInt() == player.getBidRestriction())
                            "You can't bid " + bidSelector.value
                        else
                            "Set bid"
                    },
                    bidSelector.valueProperty()/*, player.bidRestrictionProperty()*/))
            bidConts.children.addAll(bidlabel, bidSelector, bidButton)
            bidConts.visibleProperty().bind(player.startedProperty())
            gameComponents[4] = bidtotalabel
            gameComponents[5] = bidConts
            bidButton.visibleProperty().bind(player.biddingProperty())
            bidSelector.visibleProperty().bind(player.biddingProperty())
            val pprompt = Text("Play a card.")
            pprompt.visibleProperty().bind(player.playingProperty())
            gameComponents[6] = pprompt
            val playedView = FlowPane()
            playedView.maxWidthProperty().bind(stage!!.widthProperty())
            player.played.addListener { ch: ListChangeListener.Change<out NumberedCard> ->
                while (ch.next()) {
                    if (ch.wasAdded()) {
                        for (card in ch.addedSubList) {
                            Platform.runLater { playedView.children.add(CardGraphic(card)) }
                        }
                    } else if (ch.wasRemoved()) {
                        for (card in ch.removed) {
                            Platform.runLater { playedView.children.remove(CardGraphic(card)) }
                        }
                    }
                }
            }
            val plabel = Text("Cards played:")
            plabel.visibleProperty().bind(Bindings.not(Bindings.isEmpty(playedView.children)))
            val tricks = Text()
            tricks.textProperty().bind(Bindings.createStringBinding(
                    Callable { "Tricks taken: " + player.getTricks() },
                    player.tricksProperty()))
            tricks.visibleProperty().bind(player.startedProperty())
            val tlabel = Text("You took this trick")
            tlabel.visibleProperty().bind(Bindings.equal(player.tookTrickProperty(),
                    player.getName()))
            gameComponents[7] = tricks
            gameComponents[8] = tlabel
            gameComponents[9] = plabel
            gameComponents[10] = playedView
            val rlabel = Text()
            rlabel.textProperty().bind(Bindings.createStringBinding(Callable {
                var value = "You "
                val tied = player.getPlace() > 64
                value += if (tied) "tied for" else "got"
                val actualPlace = player.getPlace() % 64
                value += " $actualPlace"
                if (actualPlace == 1) {
                    value += "st"
                } else if (actualPlace == 2) {
                    value += "nd"
                } else if (actualPlace == 3) {
                    value += "rd"
                } else {
                    value += "th"
                }
                value + " place with a score of " + player.getScore() + "."
            }, player.placeProperty()))
            rlabel.visibleProperty().bind(Bindings.and(
                    Bindings.not(player.startedProperty()),
                    Bindings.notEqual(player.scoreProperty(), -1)))
            gameComponents[11] = rlabel
            for (node in gameComponents) {
                node?.visibleProperty()?.addListener { c, o, n ->
                    Platform.runLater {
                        game.children.setAll(
                                gameComponents.filtered { d -> d != null && d.isVisible })
                    }
                }
            }
            game.children.setAll(gameComponents.filtered { d -> d != null && d.isVisible })
        } catch (err: IOException) {
            val etext = Text("An error occurred.")
            val deetsButton = Button("Details")
            deetsButton.setOnAction { e ->
                val etrace = StringBuilder()
                etrace.append("Exception in thread \""
                        + Thread.currentThread().name + "\" " + err.toString())
                for (s in err.stackTrace) {
                    etrace.append("\n     at ")
                    etrace.append(s)
                }
                val error = TextArea(etrace.toString())
                error.font = Font.font("Consolas", 12.0)
                error.minWidth = SCREEN_WIDTH * 0.5
                error.minHeight = SCREEN_HEIGHT * 0.5
                error.isEditable = false
                game.children.add(error)
                deetsButton.isDisable = true
            }
            game.children.clear()
            game.setMinSize(SCREEN_WIDTH * 0.6, SCREEN_HEIGHT * 0.55)
            game.children.addAll(etext, deetsButton)
        }

        game.setMinSize(SCREEN_WIDTH * 0.3, SCREEN_HEIGHT * 0.4)
        val playingGame = Scene(components)
        stage!!.scene = playingGame
    }

    private fun findServers(): ObservableList<ServerData> {
        val searcher = ServerSearcher()
        searcher.start()
        return searcher.servers
    }

    private fun createTable(servers: ObservableList<ServerData>): TableView<ServerData> {
        val table = TableView<ServerData>()
        table.items = servers
        val nameCol = TableColumn<ServerData, String>("Name")
        nameCol.cellValueFactory = PropertyValueFactory("name")
        nameCol.prefWidth = SCREEN_WIDTH * 0.3
        table.columns.add(nameCol)
        val hostCol = TableColumn<ServerData, String>("Host")
        hostCol.cellValueFactory = PropertyValueFactory("host")
        hostCol.prefWidth = SCREEN_WIDTH * 0.3
        table.columns.add(hostCol)
        val playerCol = TableColumn<ServerData, String>("Players")
        playerCol.cellValueFactory = PropertyValueFactory("players")
        playerCol.prefWidth = SCREEN_WIDTH * 0.2
        table.columns.add(playerCol)
        return table
    }

    class HckFormatter(private val argument: String) : Callable<String> {

        override fun call(): String {
            return argument.replace("\\$".toRegex(), hword.value)
                    .replace("%".toRegex(), hword.value.toLowerCase())
        }

    }

    companion object {
        val SCREEN_WIDTH = Screen.getPrimary()
                .visualBounds.width
        val SCREEN_HEIGHT = Screen.getPrimary()
                .visualBounds.height

        lateinit var censored: BooleanProperty
        lateinit var hword: StringExpression

        @JvmStatic
        fun main(args: Array<String>) {
            launch(OhHckGui::class.java)
        }
    }
}