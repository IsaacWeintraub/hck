package com.isaac.games;

import javafx.scene.image.ImageView;

public class CardGraphic extends ImageView {

    private Card card;

    public CardGraphic(Card card) {
        super("images/" + sanitize(card.toString()) + ".bmp");
        this.card = card;
    }

    public Card card() {
        return card;
    }

    @Override
    public String toString() {
        return card.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CardGraphic)) {
            return false;
        }
        CardGraphic that = (CardGraphic) o;
        return this.card.equals(that.card);
    }

    private static String sanitize(String input) {
        int x = input.indexOf('$');
        return (x == -1) ? input : input.substring(0, x);
    }
}