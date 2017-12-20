import java.util.Scanner;

public class ClientTester {

    public static final boolean EVER = true;

    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);
        OhHckClient client = new OhHckClient("192.168.0.24");

        for ( ; EVER; ) {
            if (input.hasNext()) {
                client.transmit(input.nextLine());
            }
        }
    }
}