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

    public String process(String input, OhHckServer.ServerThread sender) {
        System.out.println("Processing: " + input);
        if (input != null && input.equals("STOP") && sender == clients.get(0)) {
            return "STOP";
        }
        switch (state) {
            case WAITING:
                if (input == null) {
                    return "WELCOME";
                }
                if (input.length() >= 6 && input.substring(0,6).equals("START ")
                    && sender == clients.get(0)) {
                    state = State.STARTED;
                    upperLimit = Integer.parseInt(
                        input.substring(input.lastIndexOf(' ') + 1));
                    return "DECK SHIT";
                }
            case STARTED:
                state = State.WAITING;
                return "IN PROGRESS";
        }
        return "NOT RECOGNIZED";
    }

}