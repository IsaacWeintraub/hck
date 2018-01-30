import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class OhHckServer extends Thread {

    public static final int PORT = 6969;
    public static final int INFO_PORT = 6970;

    private String serverName;
    private int maxPlayers;
    private int currentPlayers;
    private OhHckGame game;
    private boolean shouldContinue;
    private String hostName;
    private boolean itOpen;
    private ServerSocket serverSocket;

    public OhHckServer(String name, int maxPlayers, String host) {
        super("OhHckServer");
        this.serverName = name;
        this.hostName = host;
        this.itOpen = true;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = 0;
        this.game = new OhHckGame();
        shouldContinue = true;
    }

    @Override
    public void run() {
        System.out.println("OhHckServer thread has entered run()");
        try {
            serverSocket = new ServerSocket(PORT);
            (new InfoThread()).start();
            while (shouldContinue) {
                while (currentPlayers < maxPlayers && shouldContinue) {
                    Socket sock = serverSocket.accept();
                    ServerThread st = new ServerThread(sock);
                    st.start();
                }
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Exception in OhHckServer " + e.getStackTrace());
        }
        System.out.println("OhHckServer thread is returning from run()");
    }

    public class InfoThread extends Thread {

        public InfoThread() {
            super("OhHckServer$InfoThread");
        }

        @Override
        public void run() {
            System.out.println("OhHckServer$InfoThread thread has entered run()");
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
            System.out.println("OhHckServer$InfoThread thread is returning from run()");
        }
    }

    public class ServerThread extends Thread {

        private Socket socket;
        private PrintWriter out;
        private String playerName;
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
            System.out.println("OhHckServer$ServerThread thread has entered run()");
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
            System.out.println("OhHckServer$ServerThread thread is returning from run()");
        }

        protected void transmit(String message) {
            if (message.contains("STOP")) {
                shouldContinue = false;
                try {
                    serverSocket.close();
                } catch (IOException e) {}
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