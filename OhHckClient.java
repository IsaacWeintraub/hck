import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class OhHckClient {

    private PrintWriter out;

    public OhHckClient(String host) throws UnknownHostException, IOException {
        Socket socket = new Socket(host, OhHckServer.PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        String fromServer;
        while ((fromServer = in.readLine()) != null) {
            handleResponse(fromServer);
            if (fromServer.equals("STOP")) {
                break;
            }
        }
        socket.close();
        out.close();
        in.close();
    }

    public void handleResponse(String response) {
        System.out.println("response: " + response);
    }

    protected void transmit(String message) {
        out.println(message);
    }
}