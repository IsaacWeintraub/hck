import javafx.scene.image.ImageView;

public class CardGraphic extends ImageView {

    private Card card;

    public CardGraphic(Card card) {
        super("images/" + card.toString() + ".bmp");
        this.card = card;
    }

    public Card card() {
        return card;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CardGraphic)) {
            return false;
        }
        CardGraphic that = (CardGraphic) o;
        return this.card.equals(that.card);
    }
}