import java.util.ArrayList;
import java.util.List;

public class OhHckGame {

    public enum State {
        WAITING, STARTED
    }

    private State state;
    private List<OhHckServer.ServerThread> clients;
    private int upperLimit;
    private Deck deck;
    private int dealer;

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
            transmitAll("STOP");
        } else {
            boolean matched = false;
            switch (state) {
                case WAITING:
                    if (input == null) {
                        sender.setPlayerName("Player" + sender.getCurrentPlayers());
                        sender.transmit("WELCOME " + sender.getPlayerName());
                        matched = true;
                    } else if (input.length() >= 6
                            && input.substring(0,6).equals("START ")
                            && sender == clients.get(0) /*&& sender.size() >= 3*/) {
                        state = State.STARTED;
                        upperLimit = Integer.parseInt(
                            input.substring(input.lastIndexOf(' ') + 1));
                        deck = new Deck(Integer.parseInt(input.substring(
                            input.indexOf(' ') + 1, input.indexOf(' ') + 2)));
                        matched = true;
                        transmitAll(scores());
                        dealer = (int) (Math.random() * clients.size());
                        clients.get(dealer).transmit("DEALER");
                    }
                    break;
                case STARTED:
                    sender.transmit("IN PROGRESS");
                    matched = true;
                    break;
            }
            if (!matched) {
                sender.transmit("NOT RECOGNIZED");
            }
        }
    }

    private String scores() {
        StringBuilder ret = new StringBuilder();
        ret.append('[');
        for (OhHckServer.ServerThread c : clients) {
            ret.append(c.getName());
            ret.append('=');
            ret.append(c.getScore());
            ret.append(", ");
        }
        ret.deleteCharAt(ret.length() - 1);
        ret.append(']');
        return ret.toString();
    }

    private void transmitAll(String message) {
        for (OhHckServer.ServerThread c : clients) {
            c.transmit(message);
        }
    }

}