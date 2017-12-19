import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OhHckGame {

    public String process(String input) {
        System.out.println("Processing: " + input);
        String ret = null;
        try {
            ret = (new BufferedReader(new InputStreamReader(System.in))).readLine();
        } catch (Exception e) {}
        return ret;
    }

}