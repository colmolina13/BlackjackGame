package cspackage;

public class BlackjackProtocol { //Protocol for blackjack game, handles messages
    private BlackjackGame game = new BlackjackGame();

    public void startGame(boolean isHost) { //starts the game
    	while(true) { 
    		if (isHost) {
    			game.startGame();
    			break;
    		}
    		else {
    			System.out.println(" Only the host can start the game.");
    		}
    	}
    }

    public String getGameState() { //getter for game state
        return game.getGameState();
    }

    public Message playerHit() { //player hit
        return new Message(game.playerHit());
    }

    public Message dealerPlay() {
        //only after stand, dealer plays
        return new Message(game.dealerPlay());
    }

    public boolean gameOver() {
        return game.gameOver();
    }

    //Reset the game state
    public void resetGame(boolean isHost) {
    	while(true) { 
    		if (isHost) {
    			startGame(isHost);
    			break;
    		}
    		else {
    			System.out.println(" Only the host can reset the game.");
    		}
    	}
    }
    
    public Message quitGame(String playerName, boolean isHost) {
        if (isHost) {
            //host can stop the game and send everyone back to the lobby
            return new Message(playerName + " (Host) has stopped the game and returned everyone to the lobby.");
        } else {
            //players can only quit the game, but can't send others to the lobby
            return new Message(playerName + " has quit the game.");
        }
    }    

    public Message processMessage(Message message, String playerName, boolean isHost) { //processes important messages, main feature of protocol
        String msgContent = message.getMsg();
        switch (msgContent.toUpperCase()) { //switch case, returns different msg/function depending on the word typed
            case "START":
                if (isHost) {
                    startGame(isHost);
                    return new Message("Game has started!\n" + getGameState());
                }
                return new Message("Waiting for the host to start the game.");
            case "HIT":
                return playerHit();
            case "STAND":
                return dealerPlay();
            case "STATE":
                return new Message(getGameState());
            case "QUIT":
            	return quitGame(playerName, isHost);
            case "RESET":
                resetGame(isHost);
                return new Message("The game has been reset. Please make your move (HIT, STAND, QUIT):");
            default:
                return new Message("Unknown command.");
        }
    }
}

