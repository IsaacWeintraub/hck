public class NumberedCard extends Card {

    private long id;
    private static long counter = 0;

    public NumberedCard(Card card) {
        super(card.getSuit(), card.getRank());
        id = counter++;
    }

    public NumberedCard(Card card, long id) {
        super(card.getSuit(), card.getRank());
        this.id = id;
    }

    @Override
    public String toString() {
        return super.toString() + "$" + id;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && ((NumberedCard) o).id == this.id;
    }
}