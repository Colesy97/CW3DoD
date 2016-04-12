import java.util.Random;


/**
 * Bot Class. An extension of the PlayGame class which allows a user to play against a bot
 * which takes its own actions in the game to try and escape the dungeon of doom. The bot is
 * delayed by 1.5 seconds in between making movements, so that it is not able to play the game
 * so much faster than a human and therefore is on a fairly level playing field.
 *
 * @author Callum Coles
 * @version 1.0
 * @release 10/03/2016
 */
public class Bot extends PlayGame{
	private Random random;
	private static final char [] DIRECTIONS = {'N','S','E','W'};

	private char[][] positions = new char[50][50];
	private char[][] lookPos = new char[5][5];
	private int[] targetPos = new int[2];
	private char lookFor = 'G';
	private int Gneeded;
	private int xPos = (positions[0].length/2);
	private int yPos = (positions.length/2);
	private int[] lastLooked = new int[2];

	/**
	 * Constructor. Initialises random, for occasional moves.
	 */
	public Bot(){
		super();
		random = new Random();
	}

	/**
	 * Main code loop, the bot looks and fills an array of positions, it then checks if it has seen
	 * the thing it is looking for at the time (G - Gold or E - Exit if enough gold has been collected),
	 * if it has seen it, then it will move to it and pick it up if gold. It will then decide where to move
	 * next based on a spiral drawn around it, to find a position which isnt filled in the unknown positions.
	 */
	@Override
	public void update(){
		setup();

		while (client.gameRunning()){
			lookPos = makeLook();
			printLook();
			fillPositions();
			if(checkFor(lookFor)){
				moveToTarget();
				if(lookFor == 'G'){
					makePickup();
					positions[yPos][xPos] = '.';
				} else  {
					System.out.println("Bot wins - congratulations bot");
					quitGame();
					endSequence();
				}
			}
			whereToMove();
			moveToTarget();
		}

	}

	/**
	 * Decides where the next positions is that it should move to, based on positions that have not been
	 * discovered yet.
	 */
	private void whereToMove(){
		lastLooked[0] = yPos;
		lastLooked[1] = xPos;
		int[] dirCase = {1,1,2,2};
		int widthDefecit = 0;
		int heightDefecit = 0;
		int counter;
		char returnLook;

		while(true){
			//Move Look East
			counter = 0;
			while(counter < (dirCase[0] - widthDefecit)){
				counter++;
				lastLooked[1] ++;
				returnLook = positions[lastLooked[0]][lastLooked[1]];
				if(returnLook == 'O' && (lastLooked[0] != yPos && lastLooked[1] != xPos)){
					targetPos = lastLooked;
					return;
				} else if(returnLook == '#'){
					lastLooked[1] --;
					widthDefecit = dirCase[0] - counter + 1;
				}
			}
			dirCase[0] += 2;
			//Move Look South
			counter = 0;
			while(counter < (dirCase[1] - heightDefecit)){
				counter++;
				lastLooked[0] ++;
				returnLook = positions[lastLooked[0]][lastLooked[1]];
				if(returnLook == 'O' && (lastLooked[0] != yPos && lastLooked[1] != xPos)){
					targetPos = lastLooked;
					return;
				} else if(returnLook == '#'){
					lastLooked[0] --;
					heightDefecit = dirCase[1] - counter + 1;
					break;
				}
			}
			dirCase[1] += 2;
			//Move Look West
			counter = 0;
			while(counter < (dirCase[2] - widthDefecit)){
				lastLooked[1] --;
				counter++;
				returnLook = positions[lastLooked[0]][lastLooked[1]];
				if(returnLook == 'O' && (lastLooked[0] != yPos && lastLooked[1] != xPos)){
					targetPos = lastLooked;
					return;
				} else if(returnLook == '#'){
					lastLooked[1] ++;
					widthDefecit = dirCase[2] - counter + 1;
					break;
				}
			}
			dirCase[2] += 2;
			//Move Look North
			counter = 0;
			while(counter < (dirCase[3] - heightDefecit)){
				counter++;
				lastLooked[0] --;
				returnLook = positions[lastLooked[0]][lastLooked[1]];
				if(returnLook == 'O' && (lastLooked[0] != yPos && lastLooked[1] != xPos)){
					targetPos = lastLooked;
					return;
				} else if(returnLook == '#'){
					lastLooked[0] ++;
					heightDefecit = dirCase[3] - counter + 1;
					break;
				}
			}
			dirCase[3] += 2;
		}
	}

	/**
	 * Attempts to make a pickup, checks that the server has reported a success before reducing
	 * the amount of gold that is needed.
	 */
	private void makePickup() {
		String serverResponse;
		serverResponse = pickup();
		if(serverResponse.contains("SUCCESS")){
			System.out.println("SUCCESS");
			Gneeded --;
			if(Gneeded == 0){
				lookFor = 'E';
			}
		}
	}

	/**
	 * Looks through the positions to see if the item that the bot is looking for has been
	 * seen on the map yet, if it has then it will be set as the target position.
	 * @param lookFor the char to be search for
	 * @return whether it has been found
     */
	private boolean checkFor(char lookFor) {
		boolean found = false;
		int y = 0;
		while (y<positions.length && !found) {
			int x = 0;
			while (x<positions.length && !found) {
				if(positions[x][y] == lookFor){
					targetPos[0] = x;
					targetPos[1] = y;
					found = true;
				}
				x++;
			}
			y++;
		}
		return found;
	}

	/**
	 * Is delayed by 1.5 seconds to make it more comparable to a human player. Sends look to the Server
	 * and then gets the response.
	 * @return the look back from the Server
     */
	private char[][] makeLook() {
		try{
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		char[][] lookWindow = new char[5][5];
		String look = parseCommand("LOOK");
		String lines[] = look.split("\\n");
		for(int i = 0; i < 5; i++){
			for(int j = 0; j < 5; j++){
				lookWindow[i][j] = lines[i].charAt(j);
			}
		}
		return lookWindow;
	}

	private void printLook(){
		String output = "";
		for (int y = 0; y < 5; y++){
			for (int x = 0; x < 5; x++){
				output += lookPos[y][x];
			}
			output += "\n";
		}
		System.out.println(output);
	}

	/**
	 * Send hello command so that the amount of gold needed can be established.
	 */
	private void setup() {
		String goldNeeded = parseCommand("HELLO");
		goldNeeded = goldNeeded.trim();
		goldNeeded = goldNeeded.substring(5);
		goldNeeded = goldNeeded.trim();

		Gneeded = Integer.parseInt(goldNeeded);

		initialisePositions();
	}

	/**
	 * Sets the whole positions array to 'O' which represents an unexplored area.
	 */
	private void initialisePositions(){
		for(int x = 0; x<positions[0].length; x++){
			for (int y = 0; y<positions.length; y++){
				positions[x][y] = 'O';
			}
		}
	}

	/**
	 * Uses the last look command response to fill empty spaces in the positions array, 'P' and 'X' will
	 * not be added to the positions array and will be left as empty spaces 'O'
	 */
	private void fillPositions(){
		int y = -2;
		while( y < 3) {
			int x = -2;
			while (x < 3) {
				if (positions[yPos + y][xPos + x] == 'O') {
					if (lookPos[equateLook(y)][equateLook(x)] != 'P' && lookPos[equateLook(y)][equateLook(x)] != 'X') {
						positions[yPos + y][xPos + x] = lookPos[equateLook(y)][equateLook(x)];
					}
				}
				x++;
			}
			y++;
		}
		System.out.println(printTable());
	}

	private String printTable(){
		String output = "";
		for (int y = 0; y < 50; y++){
			for (int x = 0; x < 50; x++){
				output += positions[y][x];
			}
			output += "\n";
		}
		return output;
	}

	/**
	 * Used to change the positions relative to the bot occupied position into general positions.
	 * @param from - bot relative position
	 * @return to
     */
	private int equateLook(int from){
		int to;
		switch(from) {
			case -2:
				to = 0;
				break;
			case -1:
				to = 1;
				break;
			case 0:
				to = 2;
				break;
			case 1:
				to = 3;
				break;
			case 2:
				to = 4;
				break;
			default:
				to = 0;
				break;
		}
		return to;
	}

	/**
	 * Uses a series of move commands to move the bot to the target area.
	 */
	public void moveToTarget(){
		String serverResponse = "";
		while(yPos != targetPos[0] || xPos != targetPos[1]){
			if(!checkPossible()){
				return;
			}
			if(positions[targetPos[0]][targetPos[1]] == '#'){
				randomMove();
				return;
			}
			if(xPos < targetPos[1]){
				serverResponse = move("MOVE", 'E');
				if(serverResponse.contains("SUCCESS")){
					xPos ++;
					lookPos = makeLook();
					printLook();
					fillPositions();
				}
			} else if(xPos > targetPos[1]){
				serverResponse = move("MOVE", 'W');
				if(serverResponse.contains("SUCCESS")){
					xPos --;
					lookPos = makeLook();
					printLook();
					fillPositions();
				}
			}

			if(yPos > targetPos[0]){
				serverResponse = move("MOVE", 'N');
				if(serverResponse.contains("SUCCESS")){
					yPos --;
					lookPos = makeLook();
					printLook();
					fillPositions();
				}
			} else if(yPos < targetPos[0]){
				serverResponse = move("MOVE", 'S');
				if(serverResponse.contains("SUCCESS")){
					yPos ++;
					lookPos = makeLook();
					printLook();
					fillPositions();
				}
			}
		}
	}

	/**
	 * Checks that the target position the bot is moving towards is actually accessible and is
	 * not obscured by walls.
	 * @return
     */
	private boolean checkPossible() {
		boolean possible = true;
		switch(targetPos[0] - yPos){
			case -2:
				if(positions[yPos-1][xPos] == '#' && positions[yPos-2][xPos] == '#'){
					//Set top border
					setBorder(0,49,0,yPos-1);
					//Set not possible
					possible = false;
				}
				break;
			case 2:
				if(positions[yPos+1][xPos] == '#' && positions[yPos+2][xPos] == '#'){
					//Set bottom border
					setBorder(0,49,yPos+1,49);
					//Set not possible
					possible = false;
				}
				break;
		}
		switch(targetPos[1] - xPos){
			case -2:
				if(positions[yPos][xPos-1] == '#' && positions[yPos][xPos-2] == '#'){
					//Set left border
					setBorder(0,xPos-1,0,49);
					//Set not possible
					possible = false;
				}
				break;
			case 2:
				if(positions[yPos][xPos+1] == '#' && positions[yPos][xPos+2] == '#'){
					//Set right border
					setBorder(xPos+1,49,0,49);
					//Set not possible
					possible = false;
				}
				break;
		}
		return possible;
	}

	/**
	 * Used to set the borders of the map once determined to stop the bot trying to explore areas that
	 * it is unable to get to.
	 */
	public void setBorder(int xMin, int xMax, int yMin, int yMax){
		for(int i = yMin; i <= yMax; i++){
			for(int j = xMin; j<= xMax; j++){
				positions[i][j] = '#';
			}
		}
	}

	/**
	 * Makes the bot make a random move, when its stuck in a loop.
	 */
	public void randomMove(){
		String response;
		switch(random.nextInt(4)){
			case 1:
				response = move("MOVE", 'N');
				if(response.contains("SUCCESS")) {
					yPos--;
				}
				break;
			case 2:
				response = move("MOVE", 'E');
				if(response.contains("SUCCESS")){
					xPos++;
				}
				break;
			case 3:
				response = move("MOVE", 'S');
				if(response.contains("SUCCESS")){
					yPos++;
				}
				break;
			case 4:
				response = move("MOVE", 'W');
				if(response.contains("SUCCESS")) {
					xPos--;
				}
				break;
			default:
				response = move("MOVE", 'S');
				if(response.contains("SUCCESS")) {
					yPos++;
				}
				break;
		}
		lookPos = makeLook();
		fillPositions();
	}

	/**
	 * Main Method. Sets up the bot and starts it into the loop.
	 * @param args
     */
	public static void main(String [] args) {
		Bot game = new Bot();

		game.update();

	}


}

