import java.util.Collections;
import java.util.LinkedList;

public class Deck extends LinkedList<Card> {

    private int number;

    public Deck(int number) {
        this.number = number;
        reset();
    }

    public void reset() {
        clear();
        for (int d = 0; d < number; d++) {
            addDeck();
        }
        for (int c = 0; c < 7 + number; c++ /* haha aren't i funny */) {
            Collections.shuffle(this);
        }
    }

    private void addDeck() {
        for (int i = 2; i < 15; i++) {
            push(new Card(Card.Suit.CLUBS, i));
            push(new Card(Card.Suit.DIAMONDS, i));
            push(new Card(Card.Suit.HEARTS, i));
            push(new Card(Card.Suit.SPADES, i));
        }
    }
}