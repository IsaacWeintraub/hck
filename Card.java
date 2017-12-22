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
        return ret + "of" + suit.name();
    }

    public Suit getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public static Card fromString(String str) {
        String rs = str.substring(0, str.indexOf('o'));
        int r;
        if (rs.equals("JACK")) {
            r = 11;
        } else if (rs.equals("QUEEN")) {
            r = 12;
        } else if (rs.equals("KING")) {
            r = 13;
        } else if (rs.equals("ACE")) {
            r = 14;
        } else {
            r = Integer.parseInt(rs);
        }
        return new Card(Suit.valueOf(str.substring(str.indexOf('f') + 1)), r);
    }
}