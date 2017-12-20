import java.util.ArrayList;
import java.util.List;

public class OhHckGame {

    public enum State {
        WAITING, STARTED
    }

    private State state;
    private List<OhHckServer.ServerThread> clients;
    private int upperLimit;

    public OhHckGame() {
        this.state = State.WAITING;
        this.clients = new ArrayList<OhHckServer.ServerThread>();
    }

    public void addClient(OhHckServer.ServerThread client) {
        clients.add(client);
    }

    public void process(String input, OhHckServer.ServerThread sender) {
        System.out.println("Processing: " + input);
        if (input != null && input.equals("STOP") && sender == clients.get(0)) {
            for (OhHckServer.ServerThread c : clients) {
                c.transmit("STOP");
            }
        } else {
            boolean matched = false;
            switch (state) {
                case WAITING:
                    if (input == null) {
                        sender.transmit("WELCOME");
                        matched = true;
                    } else if (input.length() >= 6 && input.substring(0,6).equals("START ")
                        && sender == clients.get(0)) {
                        state = State.STARTED;
                        upperLimit = Integer.parseInt(
                            input.substring(input.lastIndexOf(' ') + 1));
                        sender.transmit("DECK SHIT");
                        matched = true;
                    }
                    break;
                case STARTED:
                    state = State.WAITING;
                    sender.transmit("IN PROGRESS");
                    matched = true;
                    break;
            }
            if (!matched) {
                sender.transmit("NOT RECOGNIZED");
            }
        }
    }

}