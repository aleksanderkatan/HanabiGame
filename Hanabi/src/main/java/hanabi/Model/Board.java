//Aleksander Katan
package hanabi.Model;

import java.util.*;

public class Board {
    HashMap<Color, Integer> result;
    Deck deck;
    DiscardPile discardPile;
    ArrayList<PlayerMove> playerMoveHistory;
    ArrayList<Player> players;
    Integer playerAmount;
    int currentLives;
    int currentHints;
    int currentPlayerIndex;
    int handSize;
    int turnsUntilEnd;



    public HashMap<Color, Integer> getResult() { return result; }
    public Deck getDeck() { return deck; }
    public DiscardPile getDiscardPile() { return discardPile; }
    public ArrayList<PlayerMove> getPlayerMoveHistory() { return playerMoveHistory; }
    public ArrayList<Player> getPlayers() { return players; }
    public Integer getPlayerAmount() { return playerAmount; }
    public int getCurrentLives() { return handSize; }
    public int getCurrentHints() { return currentHints; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public int getHandSize() { return handSize; }
    public int getTurnsUntilEnd() { return (turnsUntilEnd < 0) ? -1 : turnsUntilEnd; }

    public Board(int playerAmount, String... names) {
        result = new HashMap<>();
        for (Color name : Color.values()) {
            result.put(name, 0);
        }

        deck = new Deck();

        discardPile = new DiscardPile();

        playerMoveHistory = new ArrayList<>();

        this.playerAmount = playerAmount;
        currentLives = 3;
        currentHints = 8;
        currentPlayerIndex = 0;

        if (playerAmount<4)
            handSize = 5;
        else
            handSize = 4;

        turnsUntilEnd = -2137;

        players = new ArrayList<>();
        for (String name : names) {
            LinkedList<Card> hand = new LinkedList<>();

            for (int i = 0; i< handSize; i++) {
                try {
                    hand.add(deck.top());
                } catch (EmptyDeckException e) {
                    System.out.println(e);
                }
            }
            players.add(new Player(name, hand));
        }

        if (true) //for future
            Collections.shuffle(players);
    }

    public void action(PlayerMove playerMove) throws GameEndException, NoHintsLeft {
        if (playerMove.getType() == MoveType.HINT) {
            if (currentHints > 0) {
                currentHints--;
                endMove(playerMove);
                return;
            }
            throw new NoHintsLeft();
        }
        if (playerMove.getType() == MoveType.DISCARD) {
            Card cardDiscarded;
            Card topDeck;

            try {
                cardDiscarded = playerMove.getCard();
            } catch (NoCardMoveException e) {
                System.out.println(e);
                throw new RuntimeException("NoCardException");
            }

            try {
                topDeck = deck.pop();
            } catch (EmptyDeckException e) {
                topDeck = null;
                if (turnsUntilEnd < 0)
                    turnsUntilEnd = playerAmount;
            }

            playerMove.getPlayer().playOrDiscard(cardDiscarded, topDeck);

            discardPile.add(cardDiscarded);

            currentHints = (currentHints == 8) ? 8 : currentHints+1;
            endMove(playerMove);
            return;
        }
        if (playerMove.getType() == MoveType.PLAY) {
            Card cardPlayed;
            Card topDeck;

            try {
                cardPlayed = playerMove.getCard();
            } catch (NoCardMoveException e) {
                System.out.println(e);
                throw new RuntimeException("NoCardException");
            }

            try {
                topDeck = deck.pop();
            } catch (EmptyDeckException e) {
                topDeck = null;
                if (turnsUntilEnd < 0)
                    turnsUntilEnd = playerAmount;
            }

            playerMove.getPlayer().playOrDiscard(cardPlayed, topDeck);

            if (result.get(cardPlayed.getColor()) +1 == cardPlayed.getValue()) {
                result.put(cardPlayed.getColor(), cardPlayed.getValue());
                if (cardPlayed.getValue() == 5)
                    currentHints = (currentHints == 8) ? 8 : currentHints+1;
            } else {
                currentLives--;
                discardPile.add(cardPlayed);
            }

            endMove(playerMove);
            return;
        }


        if (hasGameEnded())
            throw new GameEndException();
    }

    private void endMove(PlayerMove playerMove) {
        turnsUntilEnd--;
        currentPlayerIndex = (currentPlayerIndex+1)%playerAmount;
        playerMoveHistory.add(playerMove);
    }

    //Roch Wojtowicz
    private boolean blocked(Card x) {
        int temp=0;
        for(Card i:discardPile.getDiscardPile())
            if(i.getColor()==x.getColor() && i.getValue()==x.getValue())
                ++temp;
        if(x.getValue()==1)
            return temp==3;
        if(x.getValue()==5)
            return temp==1;
        return temp==2;
    }

    private boolean hasGameEnded() {
        if (turnsUntilEnd == 0)
            return true;
        if (currentLives == 0)
            return true;
        for(Color color:Color.values())
            if(blocked(new Card(color,result.get(color)+1)))
                return false;
        return true;
    }
}

class GameEndException extends Exception {}
class NoHintsLeft extends Exception {}