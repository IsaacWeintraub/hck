import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class OhHckClient {

    public OhHckClient(String host) throws UnknownHostException, IOException {
        Socket socket = new Socket(host, OhHckServer.PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        String fromServer, fromUser;
        while ((fromServer = in.readLine()) != null) {
            handleResponse(fromServer);
            if (fromServer.equals("STOP")) {
                break;
            }
            fromUser = obtainData();
            if (fromUser != null) {
                out.println(fromUser);
            }
        }
        socket.close();
        out.close();
        in.close();
    }

    public void handleResponse(String response) {
        System.out.println("response: " + response);
    }

    public String obtainData() {
        String ret = null;
        try {
            ret = (new BufferedReader(new InputStreamReader(System.in))).readLine();
        } catch (Exception e) {}
        return ret;
    }
}