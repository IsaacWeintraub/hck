import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ServerSearcher extends Thread {

    private ObservableList<ServerData> servers;

    public ServerSearcher() {
        super("ServerSearcher");
        servers = FXCollections.observableArrayList();
    }

    @Override
    public void run() {
        try {
            String hostName = InetAddress.getLocalHost().getHostAddress();
            String hnPart = hostName.substring(0, hostName.lastIndexOf('.') + 1);
            for (int i = 2; i < 256; i++) {
                Socket sock = null;
                try {
                    InetAddress test = InetAddress.getByName(
                        hnPart + Integer.toString(i));
                    if(test.isReachable(25)) {
                        sock = new Socket(test, OhHckServer.INFO_PORT);
                        BufferedReader in = new BufferedReader(
                            new InputStreamReader(sock.getInputStream()));
                        String input;
                        while ((input = in.readLine()) != null) {
                            if (!input.equals("")) {
                                servers.add(ServerData.of(input));
                                break;
                            }
                        }
                        in.close();
                        sock.close();
                    }
                } catch (IOException e) {}
            }
        } catch (Exception e) {}
    }

    public ObservableList<ServerData> getServers() {
        return servers;
    }

}