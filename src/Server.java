import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.File;



/**
 * Threaded server for the Dungeon of Doom game, which communicates with clients
 * based on the protocol of the original release by the University of Bath Computer
 * Science Department. The server uses a GameLogic object in which to carry out the
 * decisions based on the commands received from the client.
 *
 * @author Callum Coles
 * @version 1.1
 * @release 04/06/2016
 */
public class Server implements Runnable, IGameLogic {

    int timeDelay = 100;
    private ActionListener time;

    private boolean threadStarter;
    private boolean isConnected;
    Socket listenAtSocket;
    private GameLogic logic;
    private PrintWriter dataToClient;
    private BufferedReader dataFromClient;

    private Communication serverComm;

    private static boolean anyWin = false;

    /**
     * Constructor. Keeps track of the socket used for clients
     */
    Server(Socket newSocket, File mapFile, Communication comm) {

        this.serverComm = comm;

        logic = new GameLogic(mapFile);
        this.listenAtSocket = newSocket;
        isConnected = true;
        threadStarter = true;

    }

    // start communication with client when new thread and connection are established

    /**
     * Thread - run
     * If it is the first player to join then the map needs to be set up, otherwise the user
     * will join the existing map.
     *
     * Continues to accept response and co-ordinate a response as necessary.
     */
    public void run() {

        if(threadStarter) {
            try {
                // Setup input from client
                dataFromClient = new BufferedReader(new InputStreamReader(listenAtSocket.getInputStream()));
                //Setup output to client
                dataToClient = new PrintWriter(listenAtSocket.getOutputStream(), true);

                initialHandshake();
            } catch (IOException e) {
                isConnected = false;
                System.out.println("User has exited the game");
            }
            threadStarter = false;
        }

        //while connected keep receiving input data from client
        //and co ordinating a response as required.
        while (isConnected) {

            CharSequence map = "MAP";
            try {
                //Get input from client
                String input = dataFromClient.readLine();
                System.out.println(input);

                //Print the clients message on the Server
                serverComm.addString(listenAtSocket.getInetAddress() + ": " + input);

                if(input.contains(map)){
                    sendWholeMap();
                }else {
                    //Get output for the client
                    String output = parseCommand(input);
                    if(isConnected) {
                        if(output != "UM") {
                            // Send response to client
                            dataToClient.println(output);
                            serverComm.addString("Server to " + listenAtSocket.getInetAddress() + ": " + output);
                        }
                    }else{
                        dataToClient.close();
                    }
                }
            } catch (IOException e1) {
                isConnected = false;
                System.out.println("User has exited the game");
            } catch (NullPointerException e2) {
                isConnected = false;
                logic.removePlayer();
            }

        }
    }

    /**
     * Makes the initial connection with the client so that the type of client can be established and the
     * necessary information can be provided.
     * @throws IOException
     */
    private void initialHandshake() throws IOException {

        String worldMap = "";
        CharSequence tru = "t";
        CharSequence fal = "f";
        while ( !(worldMap.contains(tru) || worldMap.contains(fal)) ) {
            worldMap = dataFromClient.readLine();
            System.out.println(worldMap);

            //Print the clients message on the Server
            serverComm.addString(listenAtSocket.getInetAddress() + ": " + worldMap);
        }

        if(worldMap.contains(tru)) {
            dataToClient.print(logic.mapWidth() + "\n" + logic.mapHeight() + "\n");
            serverComm.addString("Server to " + listenAtSocket.getInetAddress() + ": " + logic.mapWidth());
            serverComm.addString("Server to " + listenAtSocket.getInetAddress() + ": " + logic.mapHeight());
            sendWholeMap();
            time = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if(!logic.upToDate()) {
                        sendWholeMap();
                    }
                    if (anyWin == true && isConnected){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            //Non fatal
                        }
                        if(logic.getMyWin() == false){
                            dataToClient.println("LOSS");
                            serverComm.addString("Server to " + listenAtSocket.getInetAddress() + ": LOSS");
                        }
                        dataToClient.close();
                        isConnected = false;
                    }
                    if (logic.getMyWin() == true){
                        anyWin = true;
                    }
                }
            };
            new Timer(timeDelay, time).start();
        }
    }

    /**
     * Send the whole dungeon map data to the server.
     */
    private void sendWholeMap() {
        for(int i = 0; i<logic.mapHeight(); i++){
            String theLine = "";
            for(int j = 0; j<logic.mapWidth(); j++){
                System.out.print(logic.getMapChars()[i][j]);
                if (logic.lookAtPlayerTile(i,j) == 'P'){
                    if(j == logic.getPlayerXPos() && i == logic.getPlayerYPos()) {
                        theLine += 'P';
                    } else {
                        theLine += 'p';
                    }
                } else {
                    theLine += logic.getMapChars()[i][j];
                }
            }
            System.out.println();
            dataToClient.println("MAP" + theLine);
            serverComm.addString("Server to " + listenAtSocket.getInetAddress() + ": MAP" + theLine);
        }
        logic.setUpdated();

    }

    /**
     * As in Interface, not needed in Server.
     * @param file
     */
    @Override
    public void setMap(File file) {

    }

    /**
     * Gets response from the logic class, so that client can be sent a message.
     * @return amount of gold needed to escape.
     */
    @Override
    public String hello() {
        return logic.hello();
    }

    /**
     * Gets response from the logic class, so that client can be sent a message.
     * @param move - the command word
     * @param direction - the direction to move in, namely N,E,S or W.
     * @return whether the move was successful.
     */
    @Override
    public String move(String move, char direction) {
        return logic.move(move, direction);
    }

    /**
     * Gets response from the logic class, so that client can be sent a message.
     * @return whether the gold pickup was successful.
     */
    @Override
    public String pickup() {
        return logic.pickup();
    }

    /**
     * Gets response from the logic class, so that client can be sent a message.
     * @return the look screen to the user.
     */
    @Override
    public String look() {
        return logic.look();
    }

    /**
     * Gets response from Logic class.
     * @return whether the game is running.
     */
    @Override
    public boolean gameRunning() {
        return logic.gameRunning();
    }

    /**
     * Closes the socket where the client is communicating.
     * @return quit response
     */
    @Override
    public String quitGame() {
        try {
            listenAtSocket.close();
            return logic.quitGame();
        } catch (IOException e) {
            return logic.quitGame();
        }
    }

    /**
     * Parsing and Evaluating the User Input.
     * @param readUserInput input the user generates
     * @return answer of GameLogic
     */
    protected synchronized String parseCommand(String readUserInput) {

        String[] command;
        try{
            command = readUserInput.trim().split(" ");
        } catch(NullPointerException e){
            quitGame();
            isConnected = false;
            return "";
        }

        String answer = "FAIL";
        System.out.println(command[0]);

        switch (command[0].toUpperCase()){
            case "HELLO":
                answer = hello();
                break;
            case "MOVE":
                if (command.length == 2 )
                    answer = move(command[0], command[1].charAt(0));
                break;
            case "PICKUP":
                answer = pickup();
                break;
            case "LOOK":
                answer = look();
                break;
            case "QUIT":
                answer = quitGame();
                isConnected = false;
                break;
            case "null":
                answer = quitGame();
                isConnected = false;
                break;
            default:
                answer = "UM";
                chatMessage(command);
                break;
        }

        return answer;
    }

    /**
     * Prints the chat message to the server.
     * @param words - the chat message words
     */
    private void chatMessage(String[] words) {
        String chatMes = "";
        for(int i = 1; i < words.length; i++){
            chatMes += words[i] + " ";
        }
        serverComm.addString(listenAtSocket.getInetAddress() + ": " + chatMes);
    }


}
