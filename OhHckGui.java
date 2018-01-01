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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.util.concurrent.Callable;

public class OhHckGui extends Application {

    public static final double SCREEN_WIDTH = Screen.getPrimary()
        .getVisualBounds().getWidth();
    public static final double SCREEN_HEIGHT = Screen.getPrimary()
        .getVisualBounds().getHeight();

    public static BooleanProperty censored;
    public static StringExpression hword;

    private Scene starting;
    private Stage stage;
    private StringProperty serverSays = new SimpleStringProperty("Server says...");

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
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> stage.setScene(starting));
        components.setSpacing(SCREEN_HEIGHT / 108);
        components.getChildren().addAll(text, instructions, backButton);
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
        createButton.setOnAction(e -> System.out.println("create server"));
        Button connectButton = new Button("Connect to Server");
        connectButton.setOnAction(e -> beginGame(table.getSelectionModel()
            .selectedItemProperty().getValue().getIpAddress()));
        connectButton.disableProperty().bind(
            Bindings.isNull(table.getSelectionModel().selectedItemProperty()));
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> stage.setScene(starting));
        HBox buttons = new HBox();
        buttons.getChildren().addAll(refreshButton, createButton, connectButton,
            backButton);
        components.setSpacing(SCREEN_HEIGHT / 108);
        components.getChildren().addAll(text, table, buttons);
        components.setAlignment(Pos.TOP_CENTER);
        Scene serverBrowser = new Scene(components);
        stage.setScene(serverBrowser);
    }

    protected void handle(String input) {
        serverSays.set(input);
    }

    private void beginGame(String ipAddress) {
        VBox components = new VBox();
        VBox game = new VBox();
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> stage.setScene(starting));
        components.getChildren().addAll(game, backButton);
        try {
            ClientSidePlayer player = new ClientSidePlayer(ipAddress, this);
            Text scoreView = new Text();
            scoreView.textProperty().bind(Bindings.createStringBinding(
                () -> (player.getScore() == -1) ? "Waiting for game to start."
                : "Your score is " + player.getScore(), player.scoreProperty()));
            TextArea fromServer = new TextArea();
            fromServer.textProperty().bind(serverSays);
            fromServer.setEditable(false);
            HBox dealerConts = new HBox();
            HBox clientConts = new HBox();
            Text dlabel = new Text("You are the dealer for this round.");
            Button dealButton = new Button("Deal");
            dealButton.setOnAction(e -> {
                player.dealtProperty().set(true);
                player.sendToServer("BEGDEAL");
            });
            dealButton.disableProperty().bind(player.dealtProperty());
            dealerConts.visibleProperty().bind(player.dealingProperty());
            dealerConts.visibleProperty().addListener((c, o, n) -> {
                Platform.runLater(() -> {
                    if (n) {
                        game.getChildren().add(game.getChildren().indexOf(scoreView) + 1,
                            dealerConts);
                    } else {
                        game.getChildren().remove(dealerConts);
                    }
                });
            });
            dealerConts.getChildren().addAll(dlabel, dealButton);
            VBox handConts = new VBox();
            Text hlabel = new Text("Your hand:");
            HBox handView = new HBox();
            player.getHand().addListener(
                (ListChangeListener.Change<? extends Card> ch) -> {
                while (ch.next()) {
                    if (ch.wasAdded()) {
                        for (Card card : ch.getAddedSubList()) {
                            Platform.runLater(() -> {
                                CardGraphic cg = new CardGraphic(card);
                                cg.setOnMouseClicked(e -> {
                                    if (player.isPlaying() && player.canPlay(cg.card())) {
                                        player.sendToServer("PLAYING " + cg.card());
                                    }
                                });
                                cg.opacityProperty().bind(Bindings.createDoubleBinding(
                                    () -> player.canPlay(cg.card()) ? 1.0 : 0.5,
                                    player.getPlayed()));
                                handView.getChildren().add(cg);
                            });
                        }
                    } else if (ch.wasRemoved()) {
                        for (Card card : ch.getRemoved()) {
                            Platform.runLater(() -> handView.getChildren().remove(new CardGraphic(card)));
                        }
                    }
                }
            });
            handConts.getChildren().addAll(hlabel, handView);
            handConts.visibleProperty().bind(Bindings.not(Bindings.isEmpty(handView.getChildren())));
            handConts.visibleProperty().addListener((c, o, n) -> {
                if (n) {
                    int x = game.getChildren().indexOf(dealerConts);
                    game.getChildren().add(((x == -1) ? game.getChildren().indexOf(scoreView) : x) + 1, handConts);
                } else {
                    game.getChildren().remove(handConts);
                }
            });
            Text bidtotalabel = new Text();
            bidtotalabel.textProperty().bind(Bindings.createStringBinding(
                () -> "Total bids placed: " + player.getBidTotal(),
                player.bidTotalProperty()));
            Text bidlabel = new Text();
            bidlabel.textProperty().bind(Bindings.createStringBinding(
                () -> (player.getBid() == -1) ? "Place your bid: " : "Your bid is " + player.getBid(),
                player.bidProperty()));
            HBox bidConts = new HBox();
            SpinnerValueFactory.IntegerSpinnerValueFactory svf =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1, 0);
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
            bidButton.visibleProperty().bind(player.biddingProperty());
            bidSelector.visibleProperty().bind(player.biddingProperty());
            Text pprompt = new Text("Play a card.");
            pprompt.visibleProperty().bind(player.playingProperty());
            HBox playedView = new HBox();
            player.getPlayed().addListener(
                (ListChangeListener.Change<? extends Card> ch) -> {
                while (ch.next()) {
                    if (ch.wasAdded()) {
                        for (Card card : ch.getAddedSubList()) {
                            Platform.runLater(() -> playedView.getChildren().add(new CardGraphic(card)));
                        }
                    } else if (ch.wasRemoved()) {
                        for (Card card : ch.getRemoved()) {
                            Platform.runLater(() -> playedView.getChildren().remove(new CardGraphic(card)));
                        }
                    }
                }
            });
            Text plabel = new Text("Cards played:");
            Text tricks = new Text();
            tricks.textProperty().bind(Bindings.createStringBinding(
                () -> "Tricks taken: " + player.getTricks(),
                player.tricksProperty()));
            Text tlabel = new Text("You took this trick");
            tlabel.visibleProperty().bind(Bindings.equal(player.tookTrickProperty(),
                player.getName()));
            TextField fromClient = new TextField("Send to server...");
            Button sendButton = new Button("Send");
            sendButton.disableProperty().bind(Bindings.or(
                Bindings.isEmpty(fromClient.textProperty()),
                Bindings.equal("Send to server...", fromClient.textProperty())));
            sendButton.setOnAction(e -> player.client().transmit(fromClient.getText()));
            clientConts.getChildren().addAll(fromClient, sendButton);
            game.getChildren().addAll(scoreView, bidtotalabel, bidConts, pprompt, tricks, tlabel, plabel, playedView, fromServer, clientConts);
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
        game.setMinHeight(SCREEN_HEIGHT * 0.5);
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