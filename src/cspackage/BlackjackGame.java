package cspackage;

import java.util.*;

public class BlackjackGame { //Actual blackjack game logic
    private final List<List<Integer>> playerHands = new ArrayList<>();
    private final List<Integer> playerHiddenCards = new ArrayList<>();
    private final List<Integer> dealerHand = new ArrayList<>();
    private Integer dealerHiddenCard;
    private final Random rand = new Random();
    
    private boolean revealHiddenCards = false;
    private boolean gameStart = false;
    
    private int currentTurn = 0; // Track whose turn it is
    private final boolean[] playerConnected = new boolean[4];
    private final int[] playerScores = new int[4];
    private final boolean[] playerDone = new boolean[4];
    
    public BlackjackGame() {
        // Initialize hands and hidden cards for all players
        for (int i = 0; i < 4; i++) {
            playerHands.add(new ArrayList<>());
            playerHiddenCards.add(null);
            playerConnected[i] = false;
            playerScores[i] = 0;
            playerDone[i] = false; 
        }
    }
    
    public boolean advanceTurn() {
        int startTurn = currentTurn;
        do {
            currentTurn = (currentTurn + 1) % playerHands.size();
            // If player is connected and not done, switch to them
            if (playerConnected[currentTurn] && !playerDone[currentTurn]) {
                revealHiddenCards = false; // reset for new player turn
                gameStart = false;
                return true;
            }
        } while (currentTurn != startTurn);

        // If we looped back to the start, no next player found
        return false;
    }

    /** Mark the current player as done with their turn (stand or bust) */
    public void markCurrentPlayerDone() {
        playerDone[currentTurn] = true;
    }

    /** Reset all players' done flags when a new game/round starts */
    public void resetPlayerDone() {
        for (int i = 0; i < playerDone.length; i++) {
            playerDone[i] = false;
        }
    }
    
    /**startGame() - starts game for current player
     * Preconditions: playerHands and dealerHand are non-null and clear, drawCard() returns a card, 4 cards available
     * Postconditions: player/dealerHand contain a card that can be seen, revealHiddenCards is false, 4 cards drawn
     * 
     */    
    public void startGame() {
        // Clear hands for all players and dealer
        for (List<Integer> hand : playerHands) {
            hand.clear();
        }
        dealerHand.clear();
        revealHiddenCards = false;

        // Reset hidden cards for all players
        for (int i = 0; i < playerHiddenCards.size(); i++) {
            playerHiddenCards.set(i, null);
        }
        dealerHiddenCard = null;

        // For current player, deal cards
        playerHiddenCards.set(currentTurn, drawCard());
        dealerHiddenCard = drawCard();

        playerHands.get(currentTurn).add(drawCard());  // visible
        dealerHand.add(drawCard());  // visible
    }

    
    // card drawer
    private int drawCard() {
        return rand.nextInt(10) + 1;
    }

    /**    
     * 
     *  Preconditions: hand, hiddenCard are not null with valid values
     * @return total value of hand
     */
    private int handTotal(List<Integer> hand, Integer hiddenCard) {
        int total = 0;
        for (int card : hand) total += card;
        if (hiddenCard != null) total += hiddenCard;
        return total;
    }

    /** Preconditions: playerHand and dealerHand contains valid values
     * @return string value containing game state (player hand, total, dealers hand)
     */
    public String getGameState() {
        List<Integer> playerHand = playerHands.get(currentTurn);
        Integer playerHiddenCard = playerHiddenCards.get(currentTurn);

        StringBuilder sb = new StringBuilder();

        if (revealHiddenCards) {
            // Show full hands
            List<Integer> fullPlayerHand = new ArrayList<>(playerHand);
            fullPlayerHand.add(playerHiddenCard);
            List<Integer> fullDealerHand = new ArrayList<>(dealerHand);
            fullDealerHand.add(dealerHiddenCard);

            sb.append("Your hand: ").append(fullPlayerHand).append("\n");
            sb.append("Total: ").append(handTotal(playerHand, playerHiddenCard)).append("\n");
            sb.append("Dealer hand: ").append(fullDealerHand).append(" (total: ").append(handTotal(dealerHand, dealerHiddenCard)).append(")\n");
        } else {
            sb.append("Your hand: ").append(playerHand).append(", [Hidden]\n");
            sb.append("Total: ").append(handTotal(playerHand, null)).append(" + ?\n");
            sb.append("Dealer shows: ").append(dealerHand.get(0)).append(", [Hidden]");
        }

        return sb.toString();
    }

    /**
     * Preconditions: playerHand is valid, drawCard, getWinner and getGameState exists 
     * @return New card to player's hand
     */
    public String playerHit() {
        List<Integer> playerHand = playerHands.get(currentTurn);
        Integer playerHiddenCard = playerHiddenCards.get(currentTurn);

        playerHand.add(drawCard());
        int total = handTotal(playerHand, playerHiddenCard);
        
        if (total > 21) {
            revealHiddenCards = true;
            return getWinner();
        }

        return getGameState();
    }

    public String playerStand() {
        revealHiddenCards = true;
        return dealerPlay();
    }

    /**
     * dealerPlay() - dealer plays their turn
     * Preconditions: dealerHand and dealerHiddenCard are valid values, getGameState and getWinner exist
     * @return gameState if dealer busts, getWinner if both players are under or equal to 21
     */
    public String dealerPlay() {
        while (handTotal(dealerHand, dealerHiddenCard) <= 17) {
            dealerHand.add(drawCard());
        }

        if (handTotal(dealerHand, dealerHiddenCard) > 21) {
            return getGameState() + "\nDealer busted! You win!";
        }

        return getWinner();
    }

    
    /**getWinner() - retrieves the winner of the game and how they won
     * Preconditions: All player and dealer hands/cards are valid
     * The game is at a state where hidden cards are revealed
     * @return String with player's and dealer's full hands
     * Correct message for the outcome of the game
     */
    private String getWinner() {
        List<Integer> playerHand = playerHands.get(currentTurn);
        Integer playerHiddenCard = playerHiddenCards.get(currentTurn);

        int playerTotal = handTotal(playerHand, playerHiddenCard);
        int dealerTotal = handTotal(dealerHand, dealerHiddenCard);

        List<Integer> fullPlayerHand = new ArrayList<>(playerHand);
        fullPlayerHand.add(playerHiddenCard);

        List<Integer> fullDealerHand = new ArrayList<>(dealerHand);
        fullDealerHand.add(dealerHiddenCard);

        StringBuilder sb = new StringBuilder();
        sb.append("Your hand: ").append(fullPlayerHand).append(" (total: ").append(playerTotal).append(")\n");
        sb.append("Dealer's hand: ").append(fullDealerHand).append(" (total: ").append(dealerTotal).append(")\n");
        							
        if (playerTotal > 21) {
            sb.append("You busted! Dealer wins!");
        } else if (dealerTotal > 21 || playerTotal > dealerTotal) {
            sb.append("You win!");
        } else if (playerTotal == dealerTotal) {
            sb.append("It's a tie!");
        } else {
            sb.append("Dealer wins!");
        }

        return sb.toString();
    }

    public boolean isPlayerBusted() {
        List<Integer> playerHand = playerHands.get(currentTurn);
        Integer playerHiddenCard = playerHiddenCards.get(currentTurn);

        return handTotal(playerHand, playerHiddenCard) > 21;
    }

    public boolean gameOver() {
        return isPlayerBusted() || revealHiddenCards;
    }

    // Optional: method to change current turn
    public void setCurrentTurn(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < playerHands.size()) {
            currentTurn = playerIndex;
            revealHiddenCards = false;
            gameStart = false;
        }
    }
}
