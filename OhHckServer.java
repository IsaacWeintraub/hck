import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class OhHckServer  {

    public static final int PORT = 6969;

    private String name;
    private int maxPlayers;
    private int currentPlayers;
    private OhHckGame game;
    private boolean shouldContinue;

    public static void main(String[] args) throws Exception {
        OhHckServer server = new OhHckServer("Grsnt", 6);
    }

    public OhHckServer(String name, int maxPlayers) throws IOException {
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = 0;
        this.game = new OhHckGame();
        shouldContinue = true;
        /* initialize other fields? */

        ServerSocket serverSocket = null;
        serverSocket = new ServerSocket(PORT);
        while (shouldContinue) {
            while (currentPlayers < maxPlayers && shouldContinue) {
                (new ServerThread(serverSocket.accept())).start();
            }
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
    }


    public class ServerThread extends Thread {

        private Socket socket;
        private PrintWriter out;
        private int score;
        private String playerName;
        private int bid;
        private int tricks;

        public ServerThread(Socket socket) {
            super("OhHckServer.ServerThread$" + currentPlayers);
            this.socket = socket;
            currentPlayers++;
            this.score = 0;
            this.playerName = "";
            this.bid = 0;
            this.tricks = 0;
            game.addClient(this);
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                String input;
                game.process(null, this);
                while ((input = in.readLine()) != null && shouldContinue) {
                    game.process(input, this);
                }
                socket.close();
                out.close();
                in.close();
            } catch (IOException e) {}
            currentPlayers--;
        }

        protected void setTricks(boolean addOrClear) {
            if (addOrClear) {
                tricks += 1;
            } else {
                tricks = 0;
            }
        }

        public int getTricks() {
            return tricks;
        }

        public int getBid() {
            return bid;
        }

        protected void setBid(int value) {
            bid = value;
        }

        public String getPlayerName() {
            return playerName;
        }

        protected void setPlayerName(String value) {
            playerName = value;
        }

        protected void addScore(int delta) {
            score += delta;
        }

        public int getScore() {
            return score;
        }

        protected void transmit(String message) {
            if (message.equals("STOP")) {
                shouldContinue = false;
            }
            out.println(message);
        }

        public int getCurrentPlayers() {
            return currentPlayers;
        }
    }

}