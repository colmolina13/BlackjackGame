package cspackage;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class BlackjackClient { //Client side logic 
	//private variables
    private static ObjectOutputStream out; //input + output streams
    private static ObjectInputStream in;
    private static String playerName;
    private static int WAITING = 0; //lobby and game state
    private static int START = 1;
    private static int state = WAITING;

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
        // command arguments
        if (args.length != 2) {
            System.err.println("Usage: java IMClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];  //host name + port number retrieval
        int portNumber = Integer.parseInt(args[1]); 
        System.out.println("Connecting to server...");
        Socket socket = new Socket(hostName, portNumber);
        System.out.println("Connected to server.");

        out = new ObjectOutputStream(socket.getOutputStream()); // Initialize input and output streams
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());


            // Initialize scanner
            Scanner s = new Scanner(System.in);

            Message serverMessage = (Message) in.readObject();
            System.out.println(serverMessage.getMsg());  // Server asks name


            playerName = s.nextLine();
            out.writeObject(new Message(playerName));
            out.flush();  // Name is sent

            serverMessage = (Message) in.readObject();
            System.out.println(serverMessage.getMsg()); //Lobby messages are sent - either host or player

            while (state == WAITING) {  //Start or quit game logic
                String startquit = s.nextLine();
                if (startquit.equalsIgnoreCase("START")) {
                    out.writeObject(new Message(startquit));
                    out.flush();
                    state = START;
                    break; // starts game loop and ends while loop
                } else if (startquit.equalsIgnoreCase("QUIT")) {
                    out.writeObject(new Message(startquit));
                    out.flush();
                    break; //quits game
                } else {
                    System.out.println("Unknown command.");
                }
                
                
            }
            if (state == START) {
                gameLoop(); //begins game loop
            }
        s.close();
        socket.close();
    }

    private static void gameLoop() throws IOException, ClassNotFoundException { //Blackjack game
        int temp = 0;
        boolean playAgain = true;
        Scanner s = new Scanner(System.in);

        while (playAgain) {
            if (temp == 0) {
                Message stateMsg = (Message) in.readObject();
                System.out.println(stateMsg.getMsg());
            }

            temp = 1; //logic to display initial message

            // Player makes move
            System.out.println("Enter your move (HIT, STAND, QUIT):");
            String action = s.nextLine().toUpperCase();

            // Send to server + receive response
            out.writeObject(new Message(action));
            out.flush();          
            Message response = (Message) in.readObject();
            System.out.println(response.getMsg());

            // End game logic
            if (response.getMsg().contains("You win!") ||
                response.getMsg().contains("It's a tie!") ||
                response.getMsg().contains("Dealer wins!")) {
                System.out.println("Do you want to play again? (yes/no):");
                String replayChoice = s.nextLine().toLowerCase();

                while (!replayChoice.equals("yes") && !replayChoice.equals("no")) {
                    System.out.println("Unknown command. Please enter yes or no:");
                    replayChoice = s.nextLine().toLowerCase();
                }

                if (replayChoice.equals("yes")) { //if yes is chosen, the game resets
                    out.writeObject(new Message("RESET"));
                    out.flush();
                    Message resetState = (Message) in.readObject();
                    System.out.println(resetState.getMsg());

                    temp = 1;
                } else {
                    System.out.println("Thanks for playing!");
                    state = WAITING;
                    playAgain = false; // game ends if not
                }
            }
        }

        s.close();
    }
}



