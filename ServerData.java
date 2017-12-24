import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ServerData {

    private StringProperty name = new SimpleStringProperty(this, "NA");
    private StringProperty host = new SimpleStringProperty(this, "NA");
    private StringProperty players = new SimpleStringProperty(this, "NA");
    private StringProperty ipAddress = new SimpleStringProperty(this, "NA");

    public ServerData(String name, String host, String players, String ipAddress) {

        this.name.bind(Bindings.createStringBinding(new OhHckGui.HckFormatter(
            name.replaceAll("H[Ee][Ll][Ll]", "\\$")
            .replaceAll("h[Ee][Ll][Ll]", "%")), OhHckGui.censored));
        this.host.bind(Bindings.createStringBinding(new OhHckGui.HckFormatter(
            host.replaceAll("H[Ee][Ll][Ll]", "\\$")
            .replaceAll("h[Ee][Ll][Ll]", "%")), OhHckGui.censored));
        this.players.set(players);
        this.ipAddress.set(ipAddress);
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

    public String getIpAddress() {
        return ipAddress.get();
    }

    public static ServerData of(String input) {
        return new ServerData(input.substring(5, input.indexOf("HOST=") - 1),
            input.substring(input.indexOf("HOST=") + 5,
                input.indexOf("PLAYERS=") - 1),
            input.substring(input.indexOf("PLAYERS=") + 8,
                input.indexOf("IPADDRESS=") - 1),
            input.substring(input.indexOf("IPADDRESS=") + 10));
    }

}