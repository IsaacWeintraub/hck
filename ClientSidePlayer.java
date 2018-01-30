import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.util.Arrays;

public class ClientSidePlayer {

    private StringProperty name = new SimpleStringProperty(this, "NA");
    private IntegerProperty score = new SimpleIntegerProperty(this, "NA");
    private BooleanProperty dealing = new SimpleBooleanProperty(this, "NA");
    private BooleanProperty dealt = new SimpleBooleanProperty(this, "NA");
    private ObservableList<NumberedCard> hand = FXCollections.observableArrayList();
    private IntegerProperty bidTotal = new SimpleIntegerProperty(this, "NA");
    private IntegerProperty bid = new SimpleIntegerProperty(this, "NA");
    private IntegerProperty bidRestriction = new SimpleIntegerProperty(this, "NA");
    private BooleanProperty bidding = new SimpleBooleanProperty(this, "NA");
    private BooleanProperty playing = new SimpleBooleanProperty(this, "NA");
    private ObservableList<NumberedCard> played = FXCollections.observableArrayList();
    private IntegerProperty tricks = new SimpleIntegerProperty(this, "NA");
    private StringProperty tookTrick = new SimpleStringProperty(this, "NA");
    private BooleanProperty started = new SimpleBooleanProperty(this, "NA");
    private IntegerProperty place = new SimpleIntegerProperty(this, "NA");
    private OhHckClient client;

    public ClientSidePlayer(String host) throws IOException {
        score.set(-1);
        dealing.set(false);
        dealt.set(false);
        bid.set(-1);
        bidRestriction.set(-1);
        playing.set(false);
        client = new OhHckClient(host, this);
        client.start();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public int getScore() {
        return score.get();
    }

    public BooleanProperty dealingProperty() {
        return dealing;
    }

    public boolean isDealing() {
        return dealing.get();
    }

    public BooleanProperty dealtProperty() {
        return dealt;
    }

    public boolean hasDealt() {
        return dealt.get();
    }

    public ObservableList<NumberedCard> getHand() {
        return hand;
    }

    public IntegerProperty bidTotalProperty() {
        return bidTotal;
    }

    public int getBidTotal() {
        return bidTotal.get();
    }

    public IntegerProperty bidProperty() {
        return bid;
    }

    public int getBid() {
        return bid.get();
    }

    public IntegerProperty bidRestrictionProperty() {
        return bidRestriction;
    }

    public int getBidRestriction() {
        return bidRestriction.get();
    }

    public BooleanProperty biddingProperty() {
        return bidding;
    }

    public boolean isBidding() {
        return bidding.get();
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public ObservableList<NumberedCard> getPlayed() {
        return played;
    }

    public IntegerProperty tricksProperty() {
        return tricks;
    }

    public int getTricks() {
        return tricks.get();
    }

    public StringProperty tookTrickProperty() {
        return tookTrick;
    }

    public String getTookTrick() {
        return tookTrick.get();
    }

    public BooleanProperty startedProperty() {
        return started;
    }

    public boolean isStarted() {
        return started.get();
    }

    public IntegerProperty placeProperty() {
        return place;
    }

    public int getPlace() {
        return place.get();
    }

    public OhHckClient client() {
        return client;
    }

    public boolean canPlay(Card card) {
        if (played.size() == 0) {
            return true;
        } else {
            Card.Suit led = played.get(0).getSuit();
            if (card.getSuit() == led) {
                return true;
            } else {
                boolean has = false;
                for (Card c : hand) {
                    has |= c.getSuit() == led;
                }
                return !has;
            }
        }
    }

    public void sendToServer(String output) {
        int x;
        String command = output.substring(0, (x = output.indexOf(' ')) == -1
            ? output.length() : x);
        if (command.equals("PLAYING")) {
            hand.remove(new NumberedCard(
                Card.fromString(output.substring(8, output.indexOf('$'))),
                Long.parseLong(output.substring(output.indexOf('$') + 1))));
            output = output.substring(0, output.indexOf('$'));
        } else if (command.equals("BID")) {
            bid.set(Integer.parseInt(output.substring(4)));
        }
        client.transmit(output);
    }

    public void process(String input) {
        if (input.length() >= 8 && input.substring(0, 8).equals("WELCOME ")) {
            name.set(input.substring(8));
        }
        int x;
        String command = input.substring(0, (x = input.indexOf(' ')) == -1
            ? input.length() : x);
        if (command.equals("SCORES")) {
            started.set(true);
            dealing.set(false);
            int oset = input.indexOf(getName()) + getName().length() + 1;
            int i;
            score.set(Integer.parseInt(input.substring(oset,
                (i = input.indexOf(',', oset)) == -1 ? input.indexOf(']') : i)));
        } else if (command.equals("DEALER")) {
            dealing.set(true);
            dealt.set(false);
        } else if (command.equals("DEAL")) {
            played.clear();
            hand.add(new NumberedCard(Card.fromString(input.substring(5))));
            bid.set(-1);
            tricks.set(0);
            tookTrick.set("");
        } else if (command.equals("BIDS")) {
            bidding.set(false);
            bidRestriction.set(-1);
            int bt = 0;
            for (String str : input.split("=")) {
                int y;
                int end = ((y = str.indexOf(',')) == -1) ? str.indexOf(']') : y;
                if (end != -1) {
                    int bid = Integer.parseInt(str.substring(0, end));
                    if (bid != -1) {
                        bt += bid;
                    }
                }
            }
            bidTotal.set(bt);
        } else if (command.equals("PLACEBID")) {
            bidRestriction.set(Integer.parseInt(input.substring(9)));
            bidding.set(true);
        } else if (command.equals("PLAYED")) {
            played.clear();
            playing.set(false);
            tookTrick.set("");
            String[] strs = input.split("=");
            for (int i = 1; i < strs.length; i++) {
                int y;
                int end = ((y = strs[i].indexOf(',')) == -1) ? strs[i].indexOf(']') : y;
                played.add(new NumberedCard(Card.fromString(strs[i].substring(0, end))));
            }
        } else if (command.equals("PLAY")) {
            playing.set(true);
        } else if (command.equals("TRICK")) {
            tookTrick.set(input.substring(6));
            if (input.substring(6).equals(getName())) {
                tricks.set(tricks.get() + 1);
            }
        } else if (command.equals("RESULTS")) {
            started.set(false);
            String[] strs = input.split("=");
            int[] results = new int[strs.length - 1];
            for (int i = 1; i < strs.length; i++) {
                int y;
                int end = ((y = strs[i].indexOf(',')) == -1) ? strs[i].indexOf(']') : y;
                results[i - 1] = Integer.parseInt(strs[i].substring(0, end));
            }
            Arrays.sort(results);
            for (int i = 1; i <= results.length; i++) {
                if (results[results.length - i] == getScore()) {
                    if (i < results.length && results[results.length - i]
                            == results[results.length - i - 1]) {
                        place.set(i + 64);
                    } else {
                        place.set(i);
                    }
                    break;
                }
            }
        }
    }

}