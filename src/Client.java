import java.io.*;
import java.net.Socket;

/**
 * Client Class. This class is used by PlayGame in order to communicate with the server so that
 * it has means in which to send its messages over the network.
 *
 * @author Callum Coles
 * @version 1.0
 * @release 10/03/2016
 */
public class Client implements IGameLogic {

    private PrintWriter dataToServer;
    private boolean active;

    Client() throws IOException {}

    /**
     * Constructor. Sets up the Socket, a BufferedReader and a PrintWriter so that communication
     * with the Server is possible.
     */
    Client(Socket socket) throws IOException {
        Socket s = socket;

        // Start PrintWriter to manage writing to the Server
        dataToServer = new PrintWriter(s.getOutputStream(), true);

        active = true;
    }


    /**
     * Sends file to Server if it is eventually decided that map should be set from Server.
     * @param file
     */
    @Override
    public void setMap(File file) {
        dataToServer.println(file);
    }

    /**
     * Sends "HELLO" to Server, uses ClientListener to wait for a response.
     * @return Response from the Server.
     */
    @Override
    public String hello() {
        dataToServer.println("HELLO");

        return "";
    }

    /**
     * Sends "MOVE" and the direction to Server, uses ClientListener to wait for a response.
     * @return Response from the Server.
     */
    @Override
    public String move(String move, char direction) {
        dataToServer.println(move + " " + direction);

        return "";
    }

    /**
     * Sends "pickup" to Server, uses ClientListener to wait for a response.
     * @return Response from the Server.
     */
    @Override
    public String pickup() {
        dataToServer.println("pickup");

        return "";
    }

    /**
     * Sends "look" to Server, uses ClientListener to wait for a response.
     * @return Response from the Server.
     */
    @Override
    public String look() {
        dataToServer.println("look");

        return "";
    }

    /**
     * gameRunning
     * @return Whether the game is active.
     */
    @Override
    public boolean gameRunning() {
        return active;
    }

    @Override
    public String quitGame() {
        dataToServer.println("QUIT");
        return "The game will now exit";
    }

    public void initialHandshake(boolean worldMap) {
        char toSend;

        if (worldMap) {
            toSend = 't';
        } else {
            toSend = 'f';
        }
        dataToServer.println(toSend);
        System.out.println("Sent: " + toSend + " to Server.");

    }

    public void sendMapRequest(){
        dataToServer.println("MAP");
    }

    public void sendMessage(String toSend) {
        dataToServer.println(toSend);
    }
}
