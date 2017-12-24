import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class OhHckServer  {

    public static final int PORT = 6969;
    public static final int INFO_PORT = 6970;

    private String serverName;
    private int maxPlayers;
    private int currentPlayers;
    private OhHckGame game;
    private boolean shouldContinue;
    private String hostName;
    private boolean itOpen;

    public static void main(String[] args) throws Exception {
        String sname = "HELL this hElLing Hell to hELl";
        int numPlayers = 8;
        String host = "HelL's hell";
        switch (args.length) {
            case 3:
                host = args[2];
            case 2:
                numPlayers = Integer.parseInt(args[1]);
            case 1:
                sname = args[0];
            default:
                break;
        }
        OhHckServer server = new OhHckServer(sname, numPlayers, host);
    }

    public OhHckServer(String name, int maxPlayers, String host)
            throws IOException {
        if (maxPlayers < 3) {
            throw new IllegalArgumentException("Max players too low: " + maxPlayers);
        }
        this.serverName = name;
        this.hostName = host;
        this.itOpen = true;
        this.maxPlayers = (maxPlayers > 16) ? 16 : maxPlayers;
        this.currentPlayers = 0;
        this.game = new OhHckGame();
        shouldContinue = true;
        /* initialize other fields? */

        ServerSocket serverSocket = null;
        serverSocket = new ServerSocket(PORT);
        (new InfoThread()).start();
        while (shouldContinue) {
            while (currentPlayers < maxPlayers && shouldContinue) {
                (new ServerThread(serverSocket.accept())).start();
            }
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    public class InfoThread extends Thread {

        public InfoThread() {
            super("OhHckServer$InfoThread");
        }

        @Override
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(INFO_PORT);
                while (itOpen) {
                    Socket s = ss.accept();
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.println(String.format(
                        "NAME=%s HOST=%s PLAYERS=%d/%d IPADDRESS=%s",
                        serverName, hostName, currentPlayers, maxPlayers,
                        InetAddress.getLocalHost().getHostAddress()));
                    s.close();
                    out.close();
                }
                ss.close();
            } catch (IOException e) {}
        }
    }

    public class ServerThread extends Thread {

        private Socket socket;
        private PrintWriter out;
        private int score;
        private String playerName;
        private int bid;
        private int tricks;
        private ServerSidePlayer player;

        public ServerThread(Socket socket) {
            super("OhHckServer$ServerThread-" + currentPlayers);
            this.socket = socket;
            currentPlayers++;
            this.player = new ServerSidePlayer();
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

        protected void transmit(String message) {
            if (message.equals("STOP")) {
                shouldContinue = false;
            }
            out.println(message);
        }

        public ServerSidePlayer player() {
            return player;
        }

        public int getCurrentPlayers() {
            return currentPlayers;
        }
    }

}