public class Card {

    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES;
    }

    public static final int JACK = 11;
    public static final int QUEEN = 12;
    public static final int KING = 13;
    public static final int ACE = 14;

    private Suit suit;
    private int rank;

    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
    }

    @Override
    public String toString() {
        String ret = "";
        switch (rank) {
            case JACK:
                ret += "JACK";
                break;
            case QUEEN:
                ret += "QUEEN";
                break;
            case KING:
                ret += "KING";
                break;
            case ACE:
                ret += "ACE";
                break;
            default:
                ret += Integer.toString(rank);
        }
        return ret + " of " + suit.name();
    }
}