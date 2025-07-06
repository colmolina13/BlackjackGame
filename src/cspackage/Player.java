package cspackage;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private List<Card> hand = new ArrayList<>();
    private boolean isHost = false;
    private boolean inGame = true;

    public Player(String name) {
        this.name = name;
    }
    
    public boolean getHost() {
    	return isHost;
    }

	public void setHost(boolean isHost) {
		this.isHost = isHost;	
	}

    // getters and setters
}
