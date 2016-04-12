import java.io.IOException;
import java.util.Scanner;

/**
 * PlayGame Class. Central class on the client side of the network, which ensures that the users
 * commands are read and sent correctly.
 *
 * @author Callum Coles
 * @version 1.0
 * @release 10/03/2016
 */
public class PlayGame {
	
	protected IGameLogic client;
	protected Scanner userInput;

	/**
	 * Constructor. Sets up a scanner so that the player can send commands. A client is setup
	 * so that these commands from the player can be communicated to the Server.
	 */
	public PlayGame(){
		userInput = new Scanner(System.in);
		try {
			client = new Client();
		} catch (IOException e){
			System.out.println("Server is not on or not responding, please try again.\n" + e);
			System.exit(0);
		}
	}
	
	/**
	 * Reads the user input so that commands can be sent from the user.
	 * @return what the user has typed.
	 */
	public String readUserInput(){
		return userInput.nextLine();
	}

	/**
	 * While the gameRunning, waits for the users commands and makes response.
	 */
	public void update(){
		String answer;
		while (client.gameRunning()){
			answer = parseCommand(readUserInput());
			printAnswer (answer);
		}
	}

	/**
	 * Used to print commands to the user and also initiate the end sequence if necessary.
	 * @param answer to be printed.
     */
	protected void printAnswer(String answer) {
		System.out.println(answer);
		if (answer == "The game will now exit"){
			endSequence();
		}
	}

	/**
	 * Parsing and Evaluating the User Input.
	 * @param readUserInput input the user generates
	 * @return answer of GameLogic
	 */
	protected String parseCommand(String readUserInput) {
		
		String [] command = readUserInput.trim().split(" ");
		String answer;
		
		switch (command[0].toUpperCase()){
		case "HELLO":
			answer = hello();
			break;
		case "MOVE":
			if (command.length == 2 ){
				answer = move(command[0], command[1].charAt(0));
			} else {
				answer = "FAIL";
			}
			break;
		case "PICKUP":
			answer = pickup();
			break;
		case "LOOK":
			answer = look();
			break;
		case "QUIT":
			answer = quitGame();
			break;
		default:
			answer = "FAIL";
		}
		
		return answer;
	}


	/**
	 * hello method called from parseCommand
	 * @return hello method in client
     */
	public String hello() {
		return client.hello();
	}

	/**
	 * move method called from parseCommand
	 * @return move method in client
	 */
	public String move(String move, char direction) {
		return client.move(move, direction);
	}

	/**
	 * pickup method called from parseCommand
	 * @return pickup method in client
	 */
	public String pickup() {
		return client.pickup();
	}

	/**
	 * look method called from parseCommand
	 * @return look method in client
	 */
	public String look() {
		return client.look();
	}

	/**
	 * quitGame method called from parseCommand
	 * @return quitGame method in client
	 */
	public String quitGame() {
		return client.quitGame();
	}

	/**
	 * endSequence is used at the end of the game, to count the user down to the application closing.
	 */
	protected void endSequence(){
		int countDown = 3;
		while (countDown > 0){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}
			System.out.println(countDown);
			countDown --;
		}
		System.exit(0);
	}

	/**
	 * Main Method. Sets up PlayGame which handles the logic, when this is done the user is notified
	 * and then the game update cycle begins to loop through while the game is active.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String [] args) {
		PlayGame game = new PlayGame();

		System.out.println("You may now use MOVE, LOOK, QUIT and any other legal commands");

		game.update();
		
		
	}

}
