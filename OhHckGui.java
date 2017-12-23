import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
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
        instructionsButton.setOnAction(e -> {
            System.out.println("show instructions");
        });
        Button censorButton = new Button();
        censorButton.textProperty().bind(Bindings.createStringBinding(
            () -> censored.get() ? "Un-censor" : "Censor", censored));
        censorButton.setPrefWidth(SCREEN_WIDTH / 9);
        censorButton.setOnAction(e -> censored.set(!censored.get()));
        Button quitButton = new Button("Quit");
        quitButton.setPrefWidth(SCREEN_WIDTH / 9);
        quitButton.setOnAction(e -> Platform.exit());
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
        connectButton.setOnAction(e -> System.out.println("connecting to server"));
        connectButton.disableProperty().bind(
            Bindings.isNull(table.getSelectionModel().selectedItemProperty()));
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> stage.setScene(starting));
        HBox buttons = new HBox();
        buttons.getChildren().addAll(refreshButton, createButton, connectButton,
            backButton);
        components.setSpacing(SCREEN_HEIGHT / 108);
        components.getChildren().addAll(text, table, buttons);
        Scene serverBrowser = new Scene(components);
        stage.setScene(serverBrowser);
        stage.setFullScreen(true);
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

    public class HckFormatter implements Callable<String> {

        private String argument;

        public HckFormatter(String argument) {
            this.argument = argument;
        }

        @Override
        public String call() {
            return argument.replaceAll("\\$", hword.getValue());
        }

    }
}