package cspackage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BlackjackMSThread extends Thread {
    private Socket socket;				//private var
    private String clientName = "";
    private static BlackjackProtocol protocol = new BlackjackProtocol(); // Protocol shared by threads
    private boolean isHost = false;
    private static final int MAX_PLAYERS = 4;
    private static List<BlackjackMSThread> players = new ArrayList<>(MAX_PLAYERS); //List of players
    
    private ObjectOutputStream out; // Output stream for sending data
    private ObjectInputStream in;   // Input stream for receiving data

    public BlackjackMSThread(Socket socket) {
        super("BlackjackMSThread");
        this.socket = socket;
    }

    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());	// Initialize input and output streams
            out.flush();

            synchronized (players) {
            	if(players.size() >= MAX_PLAYERS) {
                    out.writeObject(new Message("Lobby full. Please try again later."));	//kicks player if lobby is full
                    out.flush();
                    socket.close();
                    return;
                }
                players.add(this);  //add this thread to the player list
                updateLeader();
            }

            //send welcome message and request player name
            out.writeObject(new Message("Welcome to Blackjack! Please enter your name: "));
            out.flush();
            
            Message clientMessage = (Message) in.readObject(); //wait for player's name
            clientName = clientMessage.getMsg();
            System.out.println("Client connected: " + clientName);
            
            // Wait for host to start the game
            while (true) {
                clientMessage = (Message) in.readObject();
                String clientText = clientMessage.getMsg();

                if ("START".equalsIgnoreCase(clientText) && isHost) {
                    synchronized (protocol) {
                        protocol.startGame();  // Start the game via BlackjackProtocol
                    }
                    broadcastGameState("Game has started! " + protocol.getGameState());	//initial game state
                    break;
                } else if ("QUIT".equalsIgnoreCase(clientText)) {
                    out.writeObject(new Message("You quit the game."));
                    out.flush(); 	//game is quit, client ends
                    break;
                } else if ("START".equalsIgnoreCase(clientText)) {
                    out.writeObject(new Message("Waiting for the host to start the game..."));
                    out.flush();	//not host, can't start game
                }
                else {
                	out.writeObject(new Message("Unknown command."));
                	out.flush();
                }
            }

            // Main game loop
            while (true) {
                clientMessage = (Message) in.readObject();
                String clientText = clientMessage.getMsg();

                if ("QUIT".equalsIgnoreCase(clientText)) {
                    out.writeObject(new Message("You quit the game."));
                    out.flush();	//quit ends client if not host

                    synchronized (protocol) {
                        broadcastGameState(protocol.quitGame(clientName, isHost).getMsg()); //host can send back to lobby
                    }

                    //Player is removed from game for now, will have to be changed in updated version where there is host and players
                    synchronized (players) {
                        removePlayer();
                    }
                    break;
                }

                
                //reset logic
                if ("RESET".equalsIgnoreCase(clientText) && isHost) {
                    synchronized (protocol) {
                        protocol.resetGame();  //reset the game state via BlackjackProtocol
                    }
                    //broadcast game state after reset
                    broadcastGameState("The game has been reset.\n" + protocol.getGameState());
                    continue;
                }


                //process normal actions (HIT, STAND, etc.) via BlackjackProtocol
                Message response;
                synchronized (protocol) {
                    response = protocol.processMessage(clientMessage, clientName);  //protocol handles messages
                }

                // If the response is valid, send it back to the player
                if (response != null) {
                    out.writeObject(response);
                    out.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**sendGameStateToClient() client receives game state, used in broadcast message
     * Preconditions: state is not null, game in progress
     * @param state of the game
     */
    public void sendGameStateToClient(String state) {
    	synchronized (players) {
    		try {
            	out.writeObject(new Message(state));
            	out.flush();
        	} catch (IOException e) {
            	e.printStackTrace();
        	}
    	}
    }

    //sends out the game state
    public static void broadcastGameState(String state) {
        synchronized (players) {
            for (BlackjackMSThread player : players) {
                player.sendGameStateToClient(state);
            }
        }
    }
    
 // Called when player quits or disconnects
    private void removePlayer() {
        synchronized(players) {
            players.remove(this);  // Remove player immediately
            updateLeader();        // Update the host for remaining players
        }
    }
    
    private static void updateLeader() {
        synchronized(players) {
            if (players.isEmpty()) return;

            // Assign first player as host, everyone else as non-host
            for (int i = 0; i < players.size(); i++) {
                BlackjackMSThread player = players.get(i);
                boolean wasHost = player.isHost;
                player.isHost = (i == 0);

                try {
                    if (player.isHost && !wasHost) {
                        // New host notification
                        player.out.writeObject(new Message("You are now the leader. You can start the game by typing START."));
                    } else if (!player.isHost && wasHost) {
                        // Lost host status notification
                        player.out.writeObject(new Message("You are no longer the leader. Waiting for leader to start the game."));
                    } else if (player.isHost) {
                        // Existing host reminder
                        player.out.writeObject(new Message("You are the leader. Waiting for others to join. To start, type START. To quit, type QUIT."));
                    } else {
                        // Other players
                        player.out.writeObject(new Message("Waiting for the leader to start the game."));
                    }
                    player.out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    // If we can't message this player, assume disconnected and remove
                    players.remove(player);
                    i--; // Adjust index due to removal
                }
            }
        }
    }
}
