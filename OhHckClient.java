import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class OhHckClient extends Thread {

    private PrintWriter out;
    private Socket socket;
    private BufferedReader in;
    private ClientSidePlayer player;

    public OhHckClient(String host, ClientSidePlayer player)
            throws IOException {
        socket = new Socket(host, OhHckServer.PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.player = player;
    }

    @Override
    public void run() {
        try {
            String fromServer;
            while ((fromServer = in.readLine()) != null) {
                player.process(fromServer);
                if (fromServer.equals("STOP")) {
                    break;
                }
            }
            socket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            player.process("CLIENT ERROR " + e.getClass().getName());
        }
    }

    protected void transmit(String message) {
        out.println(message);
    }
}