import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class OhHckServer  {

    public static final int PORT = 6969;

    private String name;
    private int maxPlayers;
    private int currentPlayers;
    private OhHckGame game;

    public static void main(String[] args) throws IOException {
        OhHckServer server = new OhHckServer("Grsnt", 6);
    }

    public OhHckServer(String name, int maxPlayers) throws IOException {
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = 1;
        this.game = new OhHckGame();
        /* initialize other fields? */

        ServerSocket serverSocket = null;
        serverSocket = new ServerSocket(PORT);
        while(shouldContinue()) {
            while (currentPlayers < maxPlayers && shouldContinue()) {
                (new ServerThread(serverSocket.accept())).start();
            }
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    private boolean shouldContinue() {
        return true;
    }

    private class ServerThread extends Thread {

        private Socket socket;

        public ServerThread(Socket socket) {
            super("OhHckServer.ServerThread");
            this.socket = socket;
            currentPlayers++;
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                String input, output;
                output = game.process(null);
                out.println(output);
                while ((input = in.readLine()) != null) {
                    output = game.process(input);
                    out.println(output);
                    if (output.equals("stop thread")) {
                        break;
                    }
                }
                socket.close();
                out.close();
                in.close();
            } catch (IOException e) {}
            currentPlayers--;
        }
    }
}