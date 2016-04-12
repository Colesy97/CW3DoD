//Concept of JNI learnt from http://javapapers.com/core-java/how-to-call-a-c-program-from-java/

import java.io.File;

/**
 * GameLogic Class, which is used to setup the game for each player and position them on
 * the map. It provides a central map which is shared by all the players and a central
 * player map which is used to keep track of the locations of players on the map so that
 * collisions can be avoided. This class is also responsible for co-ordinating the response
 * that relates to the request made by the client.
 *
 * @author Callum Coles
 * @version 1.0
 * @release 10/03/2016
 */
public class GameLogic implements IGameLogic{

	private boolean myWin = false;

	static {
		System.loadLibrary("JNILogicC");
	}

	/**
	 * Constructor. Sets up the user input ready to set up which map is going to be used.
	 * Creates a new general map and a new playerMap for the players to be placed on to enable
	 * multiple players to appear together.
	 */
	public GameLogic(File mapFile){
	}

	/**
	 * Reads a map from a given file with the format:
	 * name <mapName>
	 * win <totalGold>
	 *
	 * @param mapFile A File pointed to a correctly formatted map file
	 */
	public static native void readMap(File mapFile);

	/**
	 * Sets the map up, player map up and initiates the player.
	 * @param file
     */
	public native void setMap(File file);

	/**
	 * Sets the map as passed to.
	 * @param mapName the name of the map to be used.
     */
	public native void selectMap(String mapName);

	/**
	 * MapName Accessor
	 * @return MapName
     */
	public native String getMapName();

	/**
	 * Prints how much gold is still required to win!
	 */
	public native String hello();

	/**
	 * By proving a character direction from the set of {N,S,E,W} the gamelogic 
	 * checks if this location can be visited by the player. 
	 * If it is true, the player is moved to the new location.
	 * @return If the move was executed Success is returned. If the move could not execute Fail is returned.
	 */
	public native String move(String move, char direction);

	/**
	 * Pickup command from the player
	 * @return whether or not the pickup was successful and the amount of Gold that the user now has.
     */
	public native String pickup();

	/**
	 * The method shows the dungeon around the player location
	 * @return the area around the dungeon so that the player can see it.
	 */
	public native String look();

	/**
	 * @return - the width of the map
     */
	public native int mapWidth();

	/**
	 * @return - the height of the map
	 */
	public native int mapHeight();

	/**
	 * @return - the whole map in its char array state.
	 */
	public native char[][] getMapChars();

	/**
	 * getWin
	 * @return the amount of gold required to win and escape.
     */
	public native int getWin();

	/**
	 * Quits the game when called
	 */
	public native String quitGame();

	/**
	 * gameRunning
	 * @return whether the game is active.
     */
	public native boolean gameRunning();

	/**
	 * Look at the player map tile
	 * @param y - y map position
	 * @param x - x map position
     * @return - the character at that position.
     */
	public native char lookAtPlayerTile(int y, int x);

	/**
	 * @return - whether or not the client has been updated of all changes.
     */
	public native boolean upToDate();

	/**
	 * Set the updated changes variable to the current number of changes that have been made.
	 */
	public native void setUpdated();

	/**
	 * @return - the player x position
     */
	public native int getPlayerXPos();

	/**
	 * @return - the player y position
	 */
	public native int getPlayerYPos();

	/**
	 * Remove the player from the map.
	 */
	public native void removePlayer();

	/**
	 * @return - whether or not the player has won.
     */
	public native boolean getMyWin();

}

	protected int changesMade = 0;
	protected char[][] map;
	protected String mapName;
	private int totalGoldOnMap;
	private boolean mapChanged = false;

	
	/**
	 * setWin. Sets up the amount of gold required to win the game in this map.
	 * @param in
	 * @return The amount of gold required to win this map.
	 */
	protected boolean setWin(String in) {
		if (!in.startsWith("win "))
			return true;
		int win = 0;
		try { win = Integer.parseInt(in.split(" ")[1].trim());
		} catch (NumberFormatException n){
			System.err.println("the map does not contain a valid win criteria!");
		}
		if (win < 0)
			return true;
		this.totalGoldOnMap = win;

		return false;
	}

	/**
	 * setName. Sets the name of the map currently being used.
	 * @param in
	 * @return The name of the map to be used.
	 */
	protected boolean setName(String in) {
		if (!in.startsWith("name ") && in.length() < 4)
			return true;
		String name = in.substring(4).trim();

		if (name.length() < 1)
			return true;

		this.mapName = name;

		return false;
	}


	/**
	 * The method replaces a char at a given position of the map with a new char
	 * @param y the vertical position of the tile to replace
	 * @param x the horizontal position of the tile to replace
	 * @param tile the char character of the tile to replace
	 * @return The old character which was replaced will be returned.
	 */
	protected char replaceTile(int y, int x, char tile) {
		char output = map[y][x];
		map[y][x] = tile;
		incChangesMade();
		return output;
	}

	/**
	 * Prints out the map.
	 */
	protected void printMap(){
		for (int y = 0; y < getMapHeight(); y++) {
			for (int x = 0; x < getMapWidth(); x++) {
				System.out.print(map[y][x]);
			}
			System.out.println();
		}
	}

	/**
	 * The method returns the Tile at a given location. The tile is not removed.
	 * @param y the vertical position of the tile to replace
	 * @param x the horizontal position of the tile to replace
	 * @return The old character which was replaced will be returned.
	 */
	protected char lookAtTile(int y, int x) {
		if (y < 0 || x < 0 || y >= map.length || x >= map[0].length)
			return '#';
		char output = map[y][x];

		return output;
	}

	/**
	 * This method is used to retrieve a map view around a certain location.
	 * The method should be used to get the look() around the player location.
	 * @param y Y coordinate of the location
	 * @param x X coordinate of the location
	 * @param radius The radius defining the area which will be returned.
	 * Without the usage of a lamp the standard value is 5 units.
	 * @return
	 */
	protected char[][] lookWindow(int y, int x, int radius) {
		char[][] reply = new char[radius][radius];
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				int posX = x + j - radius/2;
				int posY = y + i - radius/2;
				if (posX >= 0 && posX < getMapWidth() &&
						posY >= 0 && posY < getMapHeight())
					reply[j][i] = map[posY][posX];
				else
					reply[j][i] = '#';
			}
		}
		reply[0][0] = 'X';
		reply[radius-1][0] = 'X';
		reply[0][radius-1] = 'X';
		reply[radius-1][radius-1] = 'X';

		return reply;
	}

	/**
	 * getWin
	 * @return The amount of gold on the map.
	 */
	public int getWin() {
		return totalGoldOnMap;
	}

	/**
	 * Accessory for mapName.
	 * @return mapName
	 */
	public String getMapName() {
		return mapName;
	}

	/**
	 * Accessor
	 * @return width of the map.
	 */
	protected int getMapWidth() {
		return map[0].length;
	}

	/**
	 * Accessor
	 * @return height of map.
	 */
	protected int getMapHeight() {
		return map.length;
	}

	public char[][] getMap(){
		return map;
	}

	/**
	 * Increments the changes made variables by one.
	 */
	protected synchronized void incChangesMade(){
		changesMade += 1;
	}

	/**
	 * Accessor
	 * @return changesMade variable.
	 */
	public int getChangesMade(){
		return changesMade;
	}


}

