import java.util.Collections;
import java.util.LinkedList;

public class Deck extends LinkedList<Card> {

    public Deck(int number) {
        for (int d = 0; d < number; d++) {
            addDeck();
        }
        Collections.shuffle(this);
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