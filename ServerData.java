import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ServerData {

    private StringProperty name = new SimpleStringProperty(this, "NA");
    private StringProperty host = new SimpleStringProperty(this, "NA");
    private StringProperty players = new SimpleStringProperty(this, "NA");

    public ServerData(String name, String host, String players) {
        this.name.set(name);
        this.host.set(host);
        this.players.set(host);
    }

    public String getName() {
        return name.get();
    }

    public String getHost() {
        return host.get();
    }

    public String getPlayers() {
        return players.get();
    }

    public static ServerData of(String input) {
        return new ServerData(input.substring(5, input.indexOf("HOST=") - 1),
            input.substring(input.indexOf("HOST=") + 5,
                input.indexOf("PLAYERS=") - 1),
            input.substring(input.indexOf("PLAYERS=") + 8));
    }

}