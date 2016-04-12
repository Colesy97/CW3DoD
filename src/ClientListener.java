import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * ClientListener is used to receive all the data from the Server, it stores it all in a communication
 * object which is past from the client.
 *
 * @author Callum Coles
 * @version 1.0
 * @release 06/04/2016
 */
public class ClientListener implements Runnable{

    private BufferedReader fromServer;
    private boolean active;
    private Communication received;

    /**
     * Constructor
     * @param s - The socket to listen at.
     * @param communication - The object to store the received data.
     */
    ClientListener(Socket s, Communication communication){

        received = communication;
        active = true;
        try {
            fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    /**
     * Start communication with client when new thread and connection are established
     */
    public void run() {

        /**
        while connected keep receiving input data from client
        and displaying, as this is only used for additional messgages
        not associated with the protocol.
        */
        while(active && (!Thread.currentThread().isInterrupted()) ) {

            try {
                // Get input from server

                String input = fromServer.readLine();

                updateCommunication(input);
            }
            catch (IOException e1) {
                if(!(Thread.currentThread().isInterrupted())){
                    active = false;
                    System.out.println("User has exited the game");
                }
            }
            catch (NullPointerException e2) {
                active = false;
                updateCommunication("Lost Connection");
            }

        }

    }

    /**
     * Adds data to the communication object.
     * @param toAdd - The String to add to the communication object.
     */
    private void updateCommunication(String toAdd){
        synchronized(received){
            received.addString(toAdd);
        }
    }
}
