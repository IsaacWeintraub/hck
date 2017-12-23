import java.util.Scanner;

public class ClientSidePlayer {

    private String name;
    private OhHckClient client;

    public ClientSidePlayer(String host) throws Exception {
        name = "";
        client = new OhHckClient(host, this);
    }

    public OhHckClient client() {
        return client;
    }

    public void process(String input) {
        if (input.length() >= 8 &&input.substring(0, 8).equals("WELCOME ")) {
            name = input.substring(8);
        }
        System.out.println(input);
    }

}