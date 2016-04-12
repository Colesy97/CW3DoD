
import java.io.File;

/**
 * PlayerMap class. Extends the abstract Map class providing the additional framework for creating
 * a map which ignores everything except for empty and wall tiles, so that it can only be used for player
 * collision.
 *
 * @author Callum Coles
 * @version 1.1
 * @release 04/06/2016
 */
public class PlayerMap extends Map {

    protected char[][] playerMap;

    /**
     * Constructor. Calls the super class constructor - Map.
     */
    public PlayerMap(){
        super();
    }

    /**
     * Sets up the map ignoring everything but walls and empty spaces.
     */
    private void setupPlayerMap(){
        for (int i = 0; i < playerMap.length; i++) {
            for (int j = 0; j < playerMap[0].length; j++) {
                if(map[i][j] != '#'){
                    playerMap[i][j] = '.';
                } else {
                    playerMap[i][j] = '#';
                }
            }
        }
    }

    /**
     * Reads the map.
     * @param mapFile A File pointed to a correctly formatted map file
     */
    @Override
    public void readMap(File mapFile){
        super.readMap(mapFile);
        printMap();
        playerMap = new char[map.length][map[0].length];
        setupPlayerMap();
        printMap();
    }

    /**
     * The method returns the Tile at a given location. The tile is not removed.
     * @param y the vertical position of the tile to replace
     * @param x the horizontal position of the tile to replace
     * @return The old character which was replaced will be returned.
     */
    protected char lookAtPlayerTile(int y, int x) {
        if (y < 0 || x < 0 || y >= map.length || x >= map[0].length)
            return '#';
        char output = playerMap[y][x];

        return output;
    }

    /**
     * The method replaces a char at a given position of the map with a new char
     * @param y the vertical position of the tile to replace
     * @param x the horizontal position of the tile to replace
     * @param tile the char character of the tile to replace
     * @return The old character which was replaced will be returned.
     */
    protected char replacePlayerTile(int y, int x, char tile) {
        char output = playerMap[y][x];
        playerMap[y][x] = tile;
        incChangesMade();
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
    protected char[][] lookPlayerWindow(int y, int x, int radius) {
        char[][] reply = new char[radius][radius];
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                int posX = x + j - radius/2;
                int posY = y + i - radius/2;
                if (posX >= 0 && posX < getMapWidth() &&
                        posY >= 0 && posY < getMapHeight())
                    reply[j][i] = playerMap[posY][posX];
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

}

