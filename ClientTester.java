import java.util.Scanner;

public class ClientTester {

    static ClientSidePlayer player;

    public static void main(String[] args) throws Exception {
        InputParser p = new InputParser();
        p.start();
        player = new ClientSidePlayer("192.168.0.24");
        //p.stahp();
    }

    static class InputParser extends Thread {

        Scanner input;
        boolean stopped;
        static int num = 1;

        public InputParser() {
            super("ClientTester$InputParser-" + num);
            num++;
            input = new Scanner(System.in);
            stopped = false;
        }

        @Override
        public void run() {
            while (!stopped) {
                if (input.hasNext()) {
                    player.client().transmit(input.nextLine());
                }
            }
        }

        public void stahp() {
            stopped = true;
        }
    }
}