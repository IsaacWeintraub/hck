package com.isaac.games;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class OhHckGui extends Application {

    public static final double SCREEN_WIDTH = Screen.getPrimary()
        .getVisualBounds().getWidth();
    public static final double SCREEN_HEIGHT = Screen.getPrimary()
        .getVisualBounds().getHeight();

    public static BooleanProperty censored;
    public static StringExpression hword;

    private Scene starting;
    private Stage stage;
    private int counter = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        censored = new SimpleBooleanProperty(false);
        hword = Bindings.createStringBinding(
            () -> censored.get() ? "H*ck" : "Hell", censored);
        Text title = new Text();
        title.textProperty().bind(Bindings.createStringBinding(
            new HckFormatter("Oh $!"), hword));
        title.setTextAlignment(TextAlignment.CENTER);
        title.setFont(Font.font("Futura", SCREEN_WIDTH / 9));
        stage.titleProperty().bind(Bindings.createStringBinding(
            new HckFormatter("Oh $"), hword));
        Button beginButton = new Button("Begin");
        beginButton.setPrefWidth(SCREEN_WIDTH / 6);
        beginButton.setOnAction(e -> openServerBrowser());
        Button instructionsButton = new Button("Instructions");
        instructionsButton.setPrefWidth(SCREEN_WIDTH / 9);
        instructionsButton.setOnAction(e -> showInstructions());
        Button censorButton = new Button();
        censorButton.textProperty().bind(Bindings.createStringBinding(
            () -> censored.get() ? "Un-censor" : "Censor", censored));
        censorButton.setPrefWidth(SCREEN_WIDTH / 9);
        censorButton.setOnAction(e -> censored.set(!censored.get()));
        Button quitButton = new Button("Quit");
        quitButton.setPrefWidth(SCREEN_WIDTH / 9);
        quitButton.setOnAction(e -> System.exit(0));
        HBox smolButtons = new HBox();
        smolButtons.setAlignment(Pos.CENTER);
        smolButtons.setSpacing(SCREEN_WIDTH / 6);
        smolButtons.getChildren().addAll(instructionsButton, censorButton,
            quitButton);
        VBox components = new VBox();
        components.setAlignment(Pos.CENTER);
        components.setSpacing(SCREEN_HEIGHT / 15);
        components.getChildren().addAll(title, beginButton, smolButtons);
        starting = new Scene(components);
        stage.setScene(starting);
        stage.setFullScreen(true);
        stage.show();
    }

    private void showInstructions() {
        Text text = new Text("Instructions");
        VBox components = new VBox();
        TextArea instructions = new TextArea();
        instructions.setWrapText(true);
        instructions.textProperty().bind(Bindings.createStringBinding(
            new HckFormatter("Oh $ is a card game where the objective is to win tricks and satisfy your bids.\nA round starts with the dealer dealing everyone a certain number of cards. Once cards have been dealt, players place their bids. The person to the \"right\" of the dealer bids first. Your bid should be the number of tricks you think you can win. Each person places their bids one-by-one. For each round, the total number of bids cannot equal the number of tricks, so the dealer (who bids last) must bid accordingly. If you are the dealer, the game will tell you what you cannot bid. After everyone has bid, the person who bid first plays a card. All other players then play a card in turn. Each player must play a card that is the same suit as the first card played if he/she has one. Once everyone has played a card, the winner of the trick is determined as follows:\n\t- Aces are always high; that is, greater in rank than a king.\n\t- The highest card of the suit that was led (played at the start of the trick) wins unless any spades were played, in which case the highest spade played wins.\n\t- If multiple decks are being used and multiple equivalent cards satisfy the previous conditions, the card that was played first wins.\n\t- The person who played the winning card takes the trick.\nIf there are more tricks left in the round, whoever took the previous trick plays the first card for the next trick but the order of play is otherwise unchanged. After all of the tricks in a round have been taken, scores are updated. If the number of tricks that you took is the same as your bid, you get (10 plus your bid) points. Otherwise you get zero points. The game starts with a round where everyone is dealt one card. Each subsequent round will have one additional card until the upper limit, which is set when the game starts, is reached; after which the number of cards will decrease in each round. Once all rounds have been played, the highest score wins."),
            hword));
        instructions.setEditable(false);
        instructions.setPrefSize(SCREEN_WIDTH * 0.75, SCREEN_HEIGHT * 0.5);
        components.setSpacing(SCREEN_HEIGHT / 108);
        components.getChildren().addAll(text, instructions, backButton());
        components.setAlignment(Pos.TOP_CENTER);
        Scene instr = new Scene(components);
        stage.setScene(instr);
    }

    private void openServerBrowser() {
        Text text = new Text("Server Browser");
        VBox components = new VBox();
        ObservableList<ServerData> servers = findServers();
        TableView<ServerData> table = createTable(servers);
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> table.setItems(findServers()));
        Button createButton = new Button("Create Server");
        createButton.setOnAction(e -> {
            HBox ownerConts = new HBox();
            Text snlabel = new Text("Server name: ");
            TextField snfield = new TextField();
            Text hnlabel = new Text("Host name: ");
            TextField hnfield = new TextField();
            Text mplabel = new Text("Maximum players: ");
            Spinner<Integer> npSelector = new Spinner<>(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 16));
            Button startButton = new Button("Start server");
            startButton.disableProperty().bind(Bindings.or(Bindings.isEmpty(
                snfield.textProperty()), Bindings.isEmpty(hnfield.textProperty())));
            startButton.setOnAction(me -> beginServer(
                snfield.getText(), npSelector.getValue(), hnfield.getText()));
            ownerConts.getChildren().addAll(snlabel, snfield, hnlabel, hnfield, mplabel, npSelector, startButton);
            components.getChildren().add(2, ownerConts);
        });
        Button connectButton = new Button("Connect to Server");
        connectButton.setOnAction(e -> beginGame(table.getSelectionModel()
            .getSelectedItem().getIpAddress(), false));
        connectButton.disableProperty().bind(
            Bindings.createBooleanBinding(() -> {
                ServerData sd = table.getSelectionModel().getSelectedItem();
                if (sd != null) {
                    String[] strs = sd.getPlayers().split("/");
                    return strs[0].equals(strs[1]);
                } else {
                    return true;
                }
            }, table.getSelectionModel().selectedItemProperty()));
        HBox buttons = new HBox();
        buttons.getChildren().addAll(refreshButton, createButton, connectButton,
            backButton());
        components.setSpacing(SCREEN_HEIGHT / 108);
        components.getChildren().addAll(text, table, buttons);
        components.setAlignment(Pos.TOP_CENTER);
        Scene serverBrowser = new Scene(components);
        stage.setScene(serverBrowser);
    }

    private Button backButton() {
        Button b = new Button("Back");
        b.setOnAction(e -> stage.setScene(starting));
        return b;
    }

    private void beginServer(String name, int maxPlayers, String host) {
        OhHckServer serv = new OhHckServer(name, maxPlayers, host);
        serv.start();
        beginGame("127.0.0.1", true);
    }

    private void beginGame(String ipAddress, boolean privileged) {
        VBox components = new VBox();
        VBox game = new VBox();
        components.getChildren().addAll(game, backButton());
        try {
            ObservableList<Node> gameComponents =
                FXCollections.observableList(new ArrayList<>(20));
            for (int i = 0; i < 20; i++) {
                gameComponents.add(null);
            }
            ClientSidePlayer player = new ClientSidePlayer(ipAddress);
            if (privileged) {
                VBox privComponents = new VBox();
                Spinner<Integer> mlSelector = new Spinner<>(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 15));
                Text mlabel = new Text("Upper limit (maximum number of tricks in a round): ");
                HBox maxLimit = new HBox(mlabel, mlSelector);
                Spinner<Integer> dnSelector = new Spinner<>(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9));
                Text dlabel = new Text("Number of decks in play: ");
                HBox deckNumber = new HBox(dlabel, dnSelector);
                Button psButton = new Button("Start Game");
                psButton.setOnAction(e -> {
                    privComponents.setVisible(false);
                    player.sendToServer("START "
                        + dnSelector.getValue() + " " + mlSelector.getValue());
                });
                privComponents.getChildren().addAll(deckNumber, maxLimit, psButton);
                gameComponents.set(0, privComponents);
            }
            Text scoreView = new Text();
            scoreView.textProperty().bind(Bindings.createStringBinding(
                () -> (player.getScore() == -1) ? "Waiting for game to start."
                : "Your score is " + player.getScore(), player.scoreProperty()));
            gameComponents.set(1, scoreView);
            HBox dealerConts = new HBox();
            HBox clientConts = new HBox();
            Text dlabel = new Text("You are the dealer for this round.");
            Button dealButton = new Button("Deal");
            dealButton.setOnAction(e -> {
                player.dealtProperty().set(true);
                player.sendToServer("BEGDEAL");
            });
            dealButton.disableProperty().bind(player.dealtProperty());
            dealerConts.visibleProperty().bind(Bindings.and(
                player.dealingProperty(), player.startedProperty()));
          /*  dealButton.disabledProperty().addListener((c, o, n) -> {
                if (!n) {
                    dealerConts.getChildren().add(1, dealButton);
                } else {
                    dealerConts.getChildren().remove(dealButton);
                }
            });*/
            dealerConts.getChildren().addAll(dlabel, dealButton);
            gameComponents.set(2, dealerConts);
            VBox handConts = new VBox();
            Text hlabel = new Text("Your hand:");
            FlowPane handView = new FlowPane();
            player.getHand().addListener(
                (ListChangeListener.Change<? extends NumberedCard> ch) -> {
                while (ch.next()) {
                    if (ch.wasAdded()) {
                        for (NumberedCard card : ch.getAddedSubList()) {
                            Platform.runLater(() -> {
                                CardGraphic cg = new CardGraphic(card);
                                cg.setOnMouseClicked(e -> {
                                    if (player.isPlaying() && player.canPlay(cg.card())) {
                                        player.sendToServer("PLAYING " + cg);
                                    }
                                });
                                cg.opacityProperty().bind(Bindings.createDoubleBinding(
                                    () -> {
                                        if (player.isPlaying()) {
                                            return player.canPlay(cg.card()) ? 1.0 : 0.5;
                                        } else {
                                            return 1.0;
                                        }
                                    }, player.getPlayed()));
                                handView.getChildren().add(cg);
                            });
                        }
                    } else if (ch.wasRemoved()) {
                        Platform.runLater(() -> {
                            for (NumberedCard card : ch.getRemoved()) {
                                ObservableList<Node> children = handView.getChildren();
                                CardGraphic graphic = new CardGraphic(card);
                                for (int i = 0; i < children.size(); i++) {
                                    if (children.get(i).equals(graphic)) {
                                        children.remove(i);
                                        break;
                                    }
                                }
                            }
                        });
                    }
                }
            });
            handView.maxWidthProperty().bind(stage.widthProperty());
            handConts.getChildren().addAll(hlabel, handView);
            handConts.visibleProperty().bind(Bindings.not(Bindings.isEmpty(handView.getChildren())));
            gameComponents.set(3, handConts);
            Text bidtotalabel = new Text();
            bidtotalabel.textProperty().bind(Bindings.createStringBinding(
                () -> "Total bids placed: " + player.getBidTotal(),
                player.bidTotalProperty()));
            bidtotalabel.visibleProperty().bind(player.startedProperty());
            Text bidlabel = new Text();
            bidlabel.textProperty().bind(Bindings.createStringBinding(
                () -> (player.getBid() == -1) ? "Place your bid: " : "Your bid is " + player.getBid(),
                player.bidProperty()));
            HBox bidConts = new HBox();
            SpinnerValueFactory.IntegerSpinnerValueFactory svf =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1);
            svf.maxProperty().bind(Bindings.size(handView.getChildren()));
            Spinner<Integer> bidSelector = new Spinner<>(svf);
            Button bidButton = new Button();
            bidButton.setOnAction(e -> player.sendToServer("BID " + bidSelector.getValue()));
            bidButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> bidSelector.getValue().intValue() == player.getBidRestriction(),
                bidSelector.valueProperty(), player.bidRestrictionProperty()));
            bidButton.textProperty().bind(Bindings.createStringBinding(
                () -> bidSelector.getValue().intValue() == player.getBidRestriction()
                ? "You can't bid " + bidSelector.getValue() : "Set bid",
                bidSelector.valueProperty()/*, player.bidRestrictionProperty()*/));
            bidConts.getChildren().addAll(bidlabel, bidSelector, bidButton);
            bidConts.visibleProperty().bind(player.startedProperty());
            gameComponents.set(4, bidtotalabel);
            gameComponents.set(5, bidConts);
            bidButton.visibleProperty().bind(player.biddingProperty());
            bidSelector.visibleProperty().bind(player.biddingProperty());
            Text pprompt = new Text("Play a card.");
            pprompt.visibleProperty().bind(player.playingProperty());
            gameComponents.set(6, pprompt);
            FlowPane playedView = new FlowPane();
            playedView.maxWidthProperty().bind(stage.widthProperty());
            player.getPlayed().addListener(
                (ListChangeListener.Change<? extends NumberedCard> ch) -> {
                while (ch.next()) {
                    if (ch.wasAdded()) {
                        for (NumberedCard card : ch.getAddedSubList()) {
                            Platform.runLater(() -> playedView.getChildren().add(new CardGraphic(card)));
                        }
                    } else if (ch.wasRemoved()) {
                        for (NumberedCard card : ch.getRemoved()) {
                            Platform.runLater(() -> playedView.getChildren().remove(new CardGraphic(card)));
                        }
                    }
                }
            });
            Text plabel = new Text("Cards played:");
            plabel.visibleProperty().bind(Bindings.not(Bindings.isEmpty(playedView.getChildren())));
            Text tricks = new Text();
            tricks.textProperty().bind(Bindings.createStringBinding(
                () -> "Tricks taken: " + player.getTricks(),
                player.tricksProperty()));
            tricks.visibleProperty().bind(player.startedProperty());
            Text tlabel = new Text("You took this trick");
            tlabel.visibleProperty().bind(Bindings.equal(player.tookTrickProperty(),
                player.getName()));
            gameComponents.set(7, tricks);
            gameComponents.set(8, tlabel);
            gameComponents.set(9, plabel);
            gameComponents.set(10, playedView);
            Text rlabel = new Text();
            rlabel.textProperty().bind(Bindings.createStringBinding(() -> {
                String value = "You ";
                boolean tied = player.getPlace() > 64;
                value += (tied) ? "tied for" : "got";
                int actualPlace = player.getPlace() % 64;
                value += " " + actualPlace;
                if (actualPlace == 1) {
                    value += "st";
                } else if (actualPlace == 2) {
                    value += "nd";
                } else if (actualPlace == 3) {
                    value += "rd";
                } else {
                    value += "th";
                }
                return value + " place with a score of " + player.getScore() + ".";
            }, player.placeProperty()));
            rlabel.visibleProperty().bind(Bindings.and(
                Bindings.not(player.startedProperty()),
                Bindings.notEqual(player.scoreProperty(), -1)));
            gameComponents.set(11, rlabel);
            for (Node node : gameComponents) {
                if (node != null) {
                    node.visibleProperty().addListener((c, o, n) ->
                        Platform.runLater(() -> game.getChildren().setAll(
                            gameComponents.filtered(
                                d -> d != null && d.isVisible()))));
                }
            }
            game.getChildren().setAll(gameComponents.filtered(
                d -> d != null && d.isVisible()));
        } catch (IOException err) {
            Text etext = new Text("An error occurred.");
            Button deetsButton = new Button("Details");
            deetsButton.setOnAction(e -> {
                StringBuilder etrace = new StringBuilder();
                etrace.append("Exception in thread \""
                    + Thread.currentThread().getName() + "\" " + err.toString());
                for (StackTraceElement s : err.getStackTrace()) {
                    etrace.append("\n     at ");
                    etrace.append(s);
                }
                TextArea error = new TextArea(etrace.toString());
                error.setFont(Font.font("Consolas", 12));
                error.setMinWidth(SCREEN_WIDTH * 0.5);
                error.setMinHeight(SCREEN_HEIGHT * 0.5);
                error.setEditable(false);
                game.getChildren().add(error);
                deetsButton.setDisable(true);
            });
            game.getChildren().clear();
            game.setMinSize(SCREEN_WIDTH * 0.6, SCREEN_HEIGHT * 0.55);
            game.getChildren().addAll(etext, deetsButton);
        }
        game.setMinSize(SCREEN_WIDTH * 0.3, SCREEN_HEIGHT * 0.4);
        Scene playingGame = new Scene(components);
        stage.setScene(playingGame);
    }

    private ObservableList<ServerData> findServers() {
        ServerSearcher searcher = new ServerSearcher();
        searcher.start();
        return searcher.getServers();
    }

    private TableView<ServerData> createTable(ObservableList<ServerData> servers) {
        TableView<ServerData> table = new TableView<>();
        table.setItems(servers);
        TableColumn<ServerData, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(
            new PropertyValueFactory<ServerData, String>("name"));
        nameCol.setPrefWidth(SCREEN_WIDTH * 0.3);
        table.getColumns().add(nameCol);
        TableColumn<ServerData, String> hostCol = new TableColumn<>("Host");
        hostCol.setCellValueFactory(
            new PropertyValueFactory<ServerData, String>("host"));
        hostCol.setPrefWidth(SCREEN_WIDTH * 0.3);
        table.getColumns().add(hostCol);
        TableColumn<ServerData, String> playerCol = new TableColumn<>("Players");
        playerCol.setCellValueFactory(
            new PropertyValueFactory<ServerData, String>("players"));
        playerCol.setPrefWidth(SCREEN_WIDTH * 0.2);
        table.getColumns().add(playerCol);
        return table;
    }

    public static class HckFormatter implements Callable<String> {

        private String argument;

        public HckFormatter(String argument) {
            this.argument = argument;
        }

        @Override
        public String call() {
            return argument.replaceAll("\\$", hword.getValue())
                .replaceAll("%", hword.getValue().toLowerCase());
        }

    }
}