import java.util.ArrayList;
import java.util.List;

public class OhHckGame {

    public enum State {
        WAITING, STARTED, BIDDING, TRICK
    }

    private State state;
    private List<OhHckServer.ServerThread> clients;
    private int upperLimit;
    private Deck deck;
    private int dealer;
    private int numCards;
    private int bidTotal;
    private int plon;
    private List<TrackingCard> played;
    private int ctr;
    private boolean direction;

    public OhHckGame() {
        this.state = State.WAITING;
        this.clients = new ArrayList<OhHckServer.ServerThread>();
        this.numCards = 1;
        this.played = new ArrayList<TrackingCard>();
        this.direction = true;
    }

    public void addClient(OhHckServer.ServerThread client) {
        clients.add(client);
    }

    // SCRAPPY STATE MACHINE
    public void process(String input, OhHckServer.ServerThread sender) {
        System.out.println("Processing: " + input);
        if (input == null) {
            if (state == State.WAITING) {
                sender.setPlayerName("Player" + sender.getCurrentPlayers());
                sender.transmit("WELCOME " + sender.getPlayerName());
            } else {
                sender.transmit("IN PROGRESS");
            }
        } else if (input.equals("STOP") && sender == clients.get(0)) {
            transmitAll("STOP");
        } else {
            boolean matched = false;
            switch (state) {
                case WAITING:
                    if (input.length() >= 6
                            && input.substring(0, 6).equals("START ")
                            && sender == clients.get(0) /*&& sender.size() >= 3*/) {
                        state = State.STARTED;
                        upperLimit = Integer.parseInt(
                            input.substring(input.lastIndexOf(' ') + 1));
                        deck = new Deck(Integer.parseInt(input.substring(
                            input.indexOf(' ') + 1, input.indexOf(' ') + 2)));
                        matched = true;
                        transmitAll(scores());
                        dealer = (int) (Math.random() * clients.size());
                        clients.get(dealer).transmit("DEALER");
                    }
                    break;
                case STARTED:
                    if (input.equals("BEGDEAL") && sender == clients.get(dealer)) {
                        for (int i = 0; i < numCards; i++) {
                            for (int j = 0; j < clients.size(); j++) {
                                clients.get((dealer + j + 1) % clients.size())
                                    .transmit("DEAL " + deck.pop().toString());
                            }
                        }
                        eraseBids();
                        matched = true;
                        transmitAll(bids());
                        clients.get((dealer + 1) % clients.size())
                            .transmit("PLACEBID " + bidRestriction(dealer + 1));
                        state = State.BIDDING;
                        plon = dealer + 1;
                    }
                    break;
                case BIDDING:
                    if (input.length() >= 4
                            && input.substring(0, 4).equals("BID ")
                            && sender == clients.get(plon % clients.size())) {
                        sender.setBid(Integer.parseInt(input.substring(4)));
                        updateBidTotal();
                        plon++;
                        matched = true;
                        transmitAll(bids());
                        if (plon < dealer + clients.size() + 1) {
                            clients.get(plon % clients.size())
                                .transmit("PLACEBID " + bidRestriction(plon));
                        } else {
                            state = State.TRICK;
                            ctr = 0;
                            clearTricks();
                            played.clear();
                            transmitAll("PLAYED " + played);
                            clients.get(plon % clients.size()).transmit("PLAY");
                        }
                    }
                    break;
                case TRICK:
                    if (input.length() >= 8
                            && input.substring(0, 8).equals("PLAYING ")
                            && sender == clients.get(plon % clients.size())) {
                        played.add(new TrackingCard(
                            Card.fromString(input.substring(8)), sender,
                            System.currentTimeMillis()));
                        plon++;
                        matched = true;
                        transmitAll("PLAYED " + played);
                        if (plon % clients.size() != (dealer + 1) % clients.size()) {
                            clients.get(plon % clients.size()).transmit("PLAY");
                        } else {
                            transmitAll("TRICK " + winner());
                            ctr++;
                            if (ctr < numCards) {
                                played.clear();
                                ctr = 0;
                                transmitAll("PLAYED " + played);
                                clients.get(plon % clients.size()).transmit("PLAY");
                            } else {
                                if (numCards == upperLimit) {
                                    direction = false;
                                }
                                numCards += (direction) ? 1 : -1;
                                assignScores();
                                transmitAll(scores());
                                if (numCards != 0) {
                                    state = State.STARTED;
                                    deck.reset();
                                    dealer++;
                                    dealer %= clients.size();
                                    clients.get(dealer).transmit("DEALER");
                                } else {
                                    transmitAll(results());
                                    transmitAll("STOP");
                                }
                            }
                        }
                    }
                    break;

            }
            if (!matched) {
                sender.transmit("NOT RECOGNIZED");
            }
        }
    }

    private void assignScores() {
        for (OhHckServer.ServerThread c : clients) {
            if (c.getTricks() == c.getBid()) {
                c.addScore(10 + c.getBid());
            }
        }
    }

    private String winner() {
        Card.Suit led = played.get(0).card.getSuit();
        played.sort((c1, c2) -> {
            Card.Suit s1 = c1.card.getSuit();
            Card.Suit s2 = c2.card.getSuit();
            if (s1 == Card.Suit.SPADES && s2 != Card.Suit.SPADES) {
                return 69;
            } else if (s1 != Card.Suit.SPADES && s2 == Card.Suit.SPADES) {
                return -69;
            } else if (s1 == led && s2 != led) {
                return 24;
            } else if (s1 != led && s2 == led) {
                return -24;
            } else {
                int diff = c1.card.getRank() - c2.card.getRank();
                return (diff == 0)
                    ? (int) ((c2.timePlayed - c1.timePlayed) / 1000) : diff;
            }
        });
        OhHckServer.ServerThread t = played.get(played.size() - 1).playedBy;
        t.setTricks(true);
        return t.getPlayerName();
    }

    private int bidRestriction(int clindex) {
        return (clindex % clients.size() == dealer) ? numCards - bidTotal : -1;
    }

    private String results() {
        return scores(true);
    }

    private String scores() {
        return scores(false);
    }

    private String scores(boolean isFinal) {
        StringBuilder ret = new StringBuilder();
        if (!isFinal) {
            ret.append("SCORES [");
        } else {
            ret.append("RESULTS [");
        }
        for (OhHckServer.ServerThread c : clients) {
            ret.append(c.getPlayerName());
            ret.append('=');
            ret.append(c.getScore());
            ret.append(", ");
        }
        return ret.substring(0, ret.length() - 2) + ']';
    }

    private void updateBidTotal() {
        bidTotal = 0;
        for (OhHckServer.ServerThread c : clients) {
            if (c.getBid() != -1) {
                bidTotal += c.getBid();
            }
        }
    }

    private void eraseBids() {
        for (OhHckServer.ServerThread c : clients) {
            c.setBid(-1);
        }
        updateBidTotal();
    }

    private void clearTricks() {
        for (OhHckServer.ServerThread c : clients) {
            c.setTricks(false);
        }
    }

    private String bids() {
        StringBuilder ret = new StringBuilder();
        ret.append("BIDS [");
        for (OhHckServer.ServerThread c : clients) {
            ret.append(c.getPlayerName());
            ret.append('=');
            ret.append(c.getBid());
            ret.append(", ");
        }
        return ret.substring(0, ret.length() - 2) + ']';
    }

    private void transmitAll(String message) {
        for (OhHckServer.ServerThread c : clients) {
            c.transmit(message);
        }
    }

    public class TrackingCard {

        private Card card;
        private OhHckServer.ServerThread playedBy;
        private long timePlayed;

        public TrackingCard(Card card, OhHckServer.ServerThread playedBy,
                long timePlayed) {
            this.card = card;
            this.playedBy = playedBy;
            this.timePlayed = timePlayed;
        }

        @Override
        public String toString() {
            return playedBy.getPlayerName() + "=" + card.toString();
        }
    }

}