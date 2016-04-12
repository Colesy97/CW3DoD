
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JOptionPane;



/**
 * WorldGUI provides the framework for displaying a GUI based on the transfers between
 * the client object that it uses and the server. This class displayed a full gods eye
 * view of the dungeon of doom map as received from the server.
 *
 * Swing Worker Concept learnt from: http://www.javaadvent.com/2012/12/multi-threading-in-java-swing-with-swingworker.html
 *
 * @author Callum Coles
 * @version 1.0
 * @release 06/04/2016
 */
public class WorldGUI extends JFrame {

    private int timeDelay = 200;
    private ActionListener time;

    //The last look window
    private String[] lookWindow = new String[5];

    //Gods Eye Map data
    private ArrayList<String> gameData = new ArrayList<>();
    private int mapWidth;
    private int mapHeight;
    private char[][] godMap;

    //Set up the Socket for the client side of things.
    private Socket socket;
    private String socketAddress;
    private int portNumber;
    private Client client;
    private Communication communication = new Communication();

    //Used to get the size of the users screen.
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final int screenWidth = (int)screenSize.getWidth();
    private final int screenHeight = (int)screenSize.getHeight();

    //Sets the game panel to 3/4ths of the screen width and 3/5ths of the screen height.
    private final int GAME_PANEL_WIDTH = (int)(screenWidth * 0.75 - 1);
    private final int GAME_PANEL_HEIGHT = (int)(screenHeight * 0.6 - 1);

    //Sets the communication panel to 1/4ths of the screen width and the full screen height.
    private final int COMMU_PANEL_WIDTH = (int)(screenWidth * 0.25 - 1);
    private final int COMMU_PANEL_HEIGHT = (int)(screenHeight - 1);

    //Sets the control panel to 3/4ths of the screen width and 2/5ths of the screen height.
    private final int CONTROL_PANEL_WIDTH = (int)(screenWidth * 0.75 - 1);
    private final int CONTROL_PANEL_HEIGHT = (int)(screenHeight * 0.4 - 1);

    //Sets up the variables needed for the game panel.
    private JPanel gamePanel;
    private GridLayout gamePanelLayout;
    private static final int MAP_WIDTH = 21;
    private static final int MAP_HEIGHT = 11;


    //Sets up the variables needed for the control panel.
    private JPanel controlPanel;
    private GridBagLayout controlPanelLayout;

    //Set up the variable needed for the communication panel.
    private JPanel commuPanel;
    private GridBagLayout commuPanelLayout;
    public JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;


    //Loads the sprites needed for the GUI game screen.
    private BufferedImage emptyMap = null;
    private BufferedImage wall = null;
    private BufferedImage player = null;
    private BufferedImage bot = null;
    private BufferedImage anotherPlayer = null;
    private BufferedImage gold = null;
    private BufferedImage exit = null;
    private BufferedImage blank = null;
    private BufferedImage xImage = null;

    //Set up the Image Icons
    private ImageIcon mapIcon = null;
    private ImageIcon wallIcon = null;
    private ImageIcon playerIcon = null;
    private ImageIcon botIcon = null;
    private ImageIcon oPlayerIcon = null;
    private ImageIcon goldIcon = null;
    private ImageIcon exitIcon = null;
    private ImageIcon blankIcon = null;
    private ImageIcon xIcon = null;

    private boolean win = false;
    private boolean lose = false;

    /**
     * Constructor for WorldGUI, passes the window name to the super class and initialises the socket(IP)
     * address and the port number.
     * @param name  - Name of the window.
     * @param connectAddress - IP Address to connect to.
     * @param portNumber - Port number to connect to at IP address.
     */
    public WorldGUI(String name, String connectAddress, int portNumber) {
        super(name);
        socketAddress = connectAddress;
        this.portNumber = portNumber;
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method is invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI(String port, String IP) {
        //Create and set up the window.
        WorldGUI gui = new WorldGUI("Dungeon of Doom", IP, Integer.parseInt(port));
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        //Set up the content pane.
        gui.initUI(gui.getContentPane());
        //Display the window.
        gui.pack();
        gui.setVisible(true);
        gui.startTimer();
    }

    /**
     * Adds three panels to the container, one to display the dungeon of doom, one for communication
     * and the final one for control buttons.
     * @param frame - In which to put all the panels in.
     */
    private void initUI(final Container frame) {

        addKeyListener(new KeyChecker());
        setFocusable(true);

        loadSprites();

        gamePanel = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        commuPanel = new javax.swing.JPanel();

        setTitle("Dungeon of Doom");
        setSize(screenWidth, screenHeight);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        gamePanelLayout = new GridLayout(MAP_HEIGHT, MAP_WIDTH);
        gamePanel.setLayout(gamePanelLayout);

        gamePanel.setPreferredSize(new Dimension(GAME_PANEL_WIDTH, GAME_PANEL_HEIGHT));
        gamePanel.setBackground(Color.BLACK);

        for(int i = 0; i<MAP_HEIGHT; i++){
            for(int j = 0; j<MAP_WIDTH; j++){
                JLabel label = new JLabel(blankIcon);
                gamePanel.add(label);
            }
        }


        commuPanelLayout = new GridBagLayout();
        commuPanel.setLayout(commuPanelLayout);

        commuPanel.setPreferredSize(new Dimension(COMMU_PANEL_WIDTH, COMMU_PANEL_HEIGHT));
        commuPanel.setBackground(Color.WHITE);

        final JLabel commuTitle = new JLabel("Communication");

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textArea.setEditable(false);

        textField = new JTextField();

        sendButton = new JButton("Send");

        //Process the event for the sendButton press.
        sendButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Get the text from the box, ready to send to the server.
                String toSend = textField.getText();
                textField.setText("");
                if(!toSend.trim().equals("")) {
                    //Update the communication window.
                    String currentText = textArea.getText();
                    textArea.setText(currentText + "\nYou: " + toSend);
                    //Send to the server.
                    client.sendMessage("MESSAGE: " + toSend);
                }
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weighty = 0.0;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.EAST;
        commuPanel.add(commuTitle, c);


        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.weighty = 1.0;
        c.weightx = 0.1;
        commuPanel.add(scrollPane, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.weighty = 0.0;
        c.weightx = 1.0;
        commuPanel.add(textField, c);

        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        commuPanel.add(sendButton, c);


        controlPanelLayout = new GridBagLayout();
        controlPanel.setLayout(controlPanelLayout);

        controlPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT));
        controlPanel.setBackground(Color.LIGHT_GRAY);

        JButton helloButton = new JButton(("HELLO"));

        //Process the event for the helloButton press.
        helloButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Update the communication window.
                communicate("You: HELLO");
                //Send hello response to the server.
                client.hello();
                checkAndCreate(false);
            }
        });

        JButton lookButton = new JButton("LOOK");

        //Process the event for the lookButton press.
        lookButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Update the communication window.
                communicate("You: LOOK");
                //Send look response to the server.
                client.look();
                checkAndCreate(true);
                updateGUI();

            }
        });

        JButton pickupButton = new JButton("PICK UP");

        //Process the event for the pickupButton press.
        pickupButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Update the communication window.
                communicate("You: PICKUP");
                //Send pickup response to the server.
                client.pickup();
                checkAndCreate(false);
            }
        });


        JButton quitButton = new JButton("QUIT");

        //Process the event for the quitButton press.
        quitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Display popup confirming the game will close.
                int reply = JOptionPane.showConfirmDialog(gamePanel, "Are you sure you want to quit?");
                if (reply == JOptionPane.YES_OPTION)
                {
                    //Update the communication window.
                    communicate("You: QUIT");
                    //Send quit response to the server.
                    client.quitGame();
                    checkAndCreate(false);
                    System.exit(0);
                }
            }
        });

        JButton exitButton = new JButton("EXIT");

        exitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                System.exit(0);
            }
        });

        JButton upButton = new JButton("N");

        //Process the event for the upButton press.
        upButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Update the communication window.
                communicate("You: MOVE N");
                //Send move north response to the server.
                client.move("MOVE", 'N');
                checkAndCreate(false);
            }
        });

        JButton downButton = new JButton("S");

        //Process the event for the southButton press.
        downButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Update the communication window.
                communicate("You: MOVE S");
                //Send move south response to the server.
                client.move("MOVE", 'S');
                checkAndCreate(false);
            }
        });

        JButton leftButton = new JButton("W");

        //Process the event for the leftButton press.
        leftButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Update the communication window.
                communicate("You: MOVE W");
                //Send move west response to the server.
                client.move("MOVE", 'W');
                checkAndCreate(false);
            }
        });

        JButton rightButton = new JButton("E");

        //Process the event for the rightButton press.
        rightButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Update the communication window.
                communicate("You: MOVE E");
                //Send move east response to the server.
                client.move("MOVE", 'E');
                checkAndCreate(false);
            }
        });

        c.weighty = 1.0;

        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 2;
        c.gridwidth = 2;
        controlPanel.add(helloButton, c);

        c.gridx = 4;
        c.gridy = 1;
        controlPanel.add(pickupButton, c);

        c.gridx = 1;
        c.gridy = 5;
        controlPanel.add(exitButton, c);

        c.gridx = 4;
        c.gridy = 5;
        controlPanel.add(quitButton, c);

        c.gridwidth = 1;

        c.gridx = 8;
        c.gridy = 1;
        controlPanel.add(upButton, c);

        c.gridx = 9;
        c.gridy = 3;
        controlPanel.add(rightButton, c);

        c.gridx = 8;
        c.gridy = 5;
        controlPanel.add(downButton, c);

        c.gridx = 7;
        c.gridy = 3;
        controlPanel.add(leftButton, c);



        GroupLayout layout = new GroupLayout(frame);
        frame.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(gamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(commuPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(commuPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(gamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        time = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                gameData = new ArrayList<>();
                if(checkComm()){
                    setupGodMap(0, 3);
                    displayGodMap();
                }
                if(win){
                    JOptionPane.showMessageDialog(gamePanel, "YOU WON!! The game will now exit...");
                    System.exit(0);
                }
                if(lose){
                    JOptionPane.showMessageDialog(gamePanel, "YOU LOST!! The game will now exit...");
                    System.exit(0);
                }

            }
        };
        System.out.println("got to line 453");
        try{
            connectToServer();
        }catch (IOException e){

        }
        System.out.println("got to line 459");
    }

    /**
     * Starts the timer that is used for checking whether any map data has been sent.
     */
    private void startTimer() {
        new Timer(timeDelay, time).start();
    }

    /**
     * Initialises all the sprites icons ready for display.
     */
    private void loadSprites(){
        try {
            emptyMap = ImageIO.read(new File("sprites","emptyMap.jpg"));
            wall = ImageIO.read(new File("sprites","wall.jpg"));
            player = ImageIO.read(new File("sprites","player.jpg"));
            bot = ImageIO.read(new File("sprites","bot.jpg"));
            anotherPlayer = ImageIO.read(new File("sprites","anotherPlayer.jpg"));
            gold = ImageIO.read(new File("sprites","gold.jpg"));
            exit = ImageIO.read(new File("sprites","exit.jpg"));
            blank = ImageIO.read(new File("sprites", "blank.jpg"));
            xImage = ImageIO.read(new File("sprites", "x.jpg"));

            mapIcon = new ImageIcon(emptyMap);
            wallIcon = new ImageIcon(wall);
            playerIcon = new ImageIcon(player);
            botIcon = new ImageIcon(bot);
            oPlayerIcon = new ImageIcon(anotherPlayer);
            goldIcon = new ImageIcon(gold);
            exitIcon = new ImageIcon(exit);
            blankIcon = new ImageIcon(blank);
            xIcon = new ImageIcon(xImage);


        } catch(IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Send text to the text area in a thread safe way.
     * @param toAdd - The string to be added to the text area.
     */
    private void communicate(final String toAdd){
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run() // updates communication text box.
                    {
                        String currentText = textArea.getText();
                        textArea.setText(currentText + "\n" + toAdd);
                    }
                }
        );


    }

    /**
     * Sets up the connection with the server at the specified IP address, then performs the initial
     * handshake to inform the server what type of client this is and sets up a client listener in a
     * new thread.
     * @throws IOException - In the event of unsuccesful communication with the Server.
     */
    private void connectToServer() throws IOException {
        communicate("Attempting connection to client..." );

        // create Socket to make connection to server
        socket = new Socket( InetAddress.getByName( socketAddress ), portNumber );

        // display connection information
        communicate( "Connected to: " + socket.getInetAddress() );

        client = new Client(socket);
        client.initialHandshake(true);
        new Thread(new ClientListener(socket, communication)).start();
        firstCheck();
        setupGame();

    }

    /**
     * Used to setup some of the variables needed for the God Map display.
     */
    private void setupGame() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mapWidth = Integer.parseInt(gameData.get(0).trim());
        mapHeight = Integer.parseInt(gameData.get(1).trim());
        godMap = new char[mapHeight][mapWidth];
        setupGodMap(2,3);
        displayGodMap();
    }

    /**
     * Sets up the god map, used so that maps of different sizes can be handled.
     * @param yDisplacement - how far off in the y direction the map is.
     * @param xDisplacement - how far off in the x direction the map is.
     */
    private void setupGodMap(final int yDisplacement, final int xDisplacement){
        SwingWorker<Void, Void> setupGodMap = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try{
                    Thread.sleep(50);
                }catch(InterruptedException e){
                    //Non fatal.
                }
                for(int i = (yDisplacement); i<(mapHeight+yDisplacement); i++){
                    for(int j = (xDisplacement); j<(mapWidth+xDisplacement); j++){
                        godMap[(i-yDisplacement)][(j-xDisplacement)] = gameData.get(i).charAt(j);
                        System.out.print(gameData.get(i).charAt(j));
                    }
                    System.out.println();
                }
                return null;
            }
        };
        setupGodMap.execute();

    }

    /**
     * Used to call the method to take care of the first communication check.
     */
    private void firstCheck(){
        SwingWorker<Void, Void> commCheck = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try{
                    Thread.sleep(40);
                }catch(InterruptedException e){
                    //Non fatal.
                }
                while(!checkFirstComm()){
                    //Keep repeating.
                }
                return null;
            }
        };
        commCheck.execute();
    }

    /**
     * Checks the communication object for the first time so that the game is set up correctly.
     * @return - whether data has been accessed.
     */
    private boolean checkFirstComm(){
        boolean somePrinted = false;
        synchronized(communication) {
            while (communication.uncheckedLines() > 0) {
                String newLine = communication.getString();
                gameData.add(newLine);
                somePrinted = true;
            }
        }
        return somePrinted;
    }

    /**
     * Check communication object, return whether or not text has been received.
     * @return - whether some text has been processed.
     */
    private boolean checkComm(){
        boolean somePrinted = false;
        synchronized(communication){
            while(communication.uncheckedLines() > 0){
                String newLine = communication.getString();
                System.out.println(newLine + newLine.length());
                if(newLine.charAt(0) == 'L' && newLine.charAt(1) == 'O' && newLine.charAt(2) == 'S') {
                    lose = true;
                }else if(newLine.length() == 22 && newLine.charAt(21) == '!'){
                    win = true;
                }else if(newLine.charAt(0) != 'M' && newLine.charAt(1) != 'A' && newLine.charAt(2) != 'P'){
                    processString(newLine);
                    somePrinted = false;
                }else {
                    gameData.add(newLine);
                    somePrinted = true;
                }
            }
        }
        return somePrinted;
    }

    /**
     * Check communication object, return whether or not text has been received.
     * @param fromLook - has this call come via the look function.
     * @return - whether some text has been processed.
     */
    private boolean checkCommunication(boolean fromLook){
        gameData = new ArrayList<>();
        boolean receivedMapData = false;
        boolean somePrinted = false;
        synchronized(communication){
            int i = 0;
            while(communication.uncheckedLines() > 0){
                String newLine = communication.getString();
                if(newLine.charAt(0) == 'M' && newLine.charAt(1) == 'A' && newLine.charAt(2) == 'P'){
                    gameData.add(newLine);
                    receivedMapData = true;
                } else {
                    processString(newLine);
                    somePrinted = true;
                    if (fromLook) {
                        lookWindow[i] = newLine;
                        i++;
                    }
                }
            }
            if(receivedMapData){
                setupGodMap(0,3);
                displayGodMap();
            }
        }
        return somePrinted;
    }

    /**
     * Uses a SwingWorker so that the GUI doesn't hang, displays the correct icons
     * onto the game panel.
     */
    private void displayGodMap() {
        SwingWorker<Boolean, Void> GUIUpdate = new SwingWorker<Boolean, Void>() {
            int xDif;
            int yDif;
            int yTop;
            int yBottom;
            int xTop;
            int xBottom;

            @Override
            protected Boolean doInBackground() throws Exception {
                try{
                    Thread.sleep(60);
                }catch(InterruptedException e){
                    //Non fatal.
                }

                xDif = MAP_WIDTH - mapWidth;
                yDif = MAP_HEIGHT - mapHeight;
                yTop = getTop(yDif);
                yBottom = getBottom(yDif);
                xTop = getTop(xDif);
                xBottom = getBottom(xDif);

                return true;
            }

            @Override
            protected void done() {
                gamePanel.removeAll();
                gamePanel.revalidate();
                gamePanel.repaint();
                int y = 0;
                for(int i = 0; i<MAP_HEIGHT; i++){
                    int x = 0;
                    for(int j = 0; j<MAP_WIDTH; j++){
                        JLabel label;
                        if(i >= yTop  &&  i < (yBottom+mapHeight)  &&  j >= xTop  && j < (xBottom+mapWidth)){
                            label = new JLabel(getIcon(y,x));
                            x++;
                        } else {
                            label = new JLabel(blankIcon);
                        }
                        gamePanel.add(label);
                    }
                    if(i >= yTop){
                        y++;
                    }
                }
            }

            private ImageIcon getIcon(int y, int x) {
                ImageIcon toReturn;
                switch (godMap[y][x]) {
                    case '.':
                        toReturn = mapIcon;
                        break;
                    case '#':
                        toReturn = wallIcon;
                        break;
                    case 'P':
                        toReturn = playerIcon;
                        break;
                    case 'p':
                        toReturn = oPlayerIcon;
                        break;
                    case 'G':
                        toReturn = goldIcon;
                        break;
                    case 'E':
                        toReturn = exitIcon;
                        break;
                    case 'X':
                        toReturn = xIcon;
                        break;
                    default:
                        toReturn = wallIcon;
                }
                return toReturn;
            }

            private int getTop(int num){
                if(num % 2 == 0){
                    return (num/2);
                } else {
                    return ((num-1)/2);
                }
            }

            private int getBottom(int num){
                if(num % 2 == 0){
                    return (num/2);
                } else {
                    return ((num+1)/2);
                }
            }
        };
        GUIUpdate.execute();
    }

    /**
     * Creates a SwingWorker to check the communication object.
     * @param fromLook - has this come from the look function.
     */
    private void checkAndCreate(final boolean fromLook){
        SwingWorker<Void, Void> commCheck = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                while(!checkCommunication(fromLook)){
                    //Keep repeating.
                }
                return null;
            }
        };
        commCheck.execute();
    }

    /**
     * Updates the GamePanel, used when the dungeon has changed.
     */
    private void updateGUI(){
        SwingWorker<Boolean, Void> GUIUpdate = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try{
                    Thread.sleep(50);
                }catch(InterruptedException e){
                    //Non fatal.
                }

                return true;
            }

            @Override
            protected void done() {
                gamePanel.removeAll();
                gamePanel.revalidate();
                gamePanel.repaint();
                for(int i = 0; i<MAP_HEIGHT; i++){
                    for(int j = 0; j<MAP_WIDTH; j++){
                        JLabel label;
                        if(((3 <= i) && (i <= 7)) && ((8 <= j) && (j <= 12))){
                            if(i == 5 && j == 10){
                                label = new JLabel(playerIcon);
                            } else {
                                label = new JLabel(getIcon(lookWindow[i - 3], j - 8));
                            }
                        } else {
                            label = new JLabel(blankIcon);
                        }
                        gamePanel.add(label);
                    }
                }
            }

            private ImageIcon getIcon(String s, int i) {
                ImageIcon toReturn;
                switch (s.charAt(i)) {
                    case '.':
                        toReturn = mapIcon;
                        break;
                    case '#':
                        toReturn = wallIcon;
                        break;
                    case 'P':
                        toReturn = playerIcon;
                        break;
                    case 'p':
                        toReturn = oPlayerIcon;
                        break;
                    case 'G':
                        toReturn = goldIcon;
                        break;
                    case 'E':
                        toReturn = exitIcon;
                        break;
                    case 'X':
                        toReturn = xIcon;
                        break;
                    default:
                        toReturn = wallIcon;
                }
                return toReturn;
            }
        };
        GUIUpdate.execute();
    }

    /**
     * Sends the string to be printed.
     * @param toProcess - String to process.
     */
    private void processString(String toProcess) {
        communicate(toProcess);
    }

    /**
     * Used to send look command to the server in conjunction with button press.
     */
    private void look(){
        //Update the communication window.
        communicate("You: LOOK");
        //Send look command to the server.
        client.look();
        checkAndCreate(true);
        updateGUI();
    }

    /**
     * Used to send hello command to the server in conjunction with button press.
     */
    private void hello(){
        //Update the communication window.
        communicate("You: HELLO");
        //Send hello command to the server.
        client.hello();
        checkAndCreate(false);
    }

    /**
     * Used to send pickup command to the server in conjunction with button press.
     */
    private void pickup(){
        //Update the communication window.
        communicate("You: PICKUP");
        //Send pickup command to the server.
        client.pickup();
        checkAndCreate(false);
    }

    /**
     * Used to send quit command to the server in conjunction with button press.
     */
    private void quitGame(){
        //Update the communication window.
        communicate("You: QUIT");
        //Send quit command to the server.
        client.quitGame();
        checkAndCreate(false);
    }

    /**
     * Used to send move command to the server in conjunction with button press.
     */
    private void move(char dir){
        //Update the communication window.
        communicate("You: MOVE " + dir);
        //Send move north response to the server.
        client.move("MOVE", dir);
        checkAndCreate(false);
    }

    /**
     * Used to check the players key presses and calls the appropriate funciton to
     * co-ordinate a response.
     */
    private class KeyChecker extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            int keyPressed = e.getKeyCode();
            switch( keyPressed ) {
                case KeyEvent.VK_UP:
                    move('N');
                    break;
                case KeyEvent.VK_DOWN:
                    move('S');
                    break;
                case KeyEvent.VK_RIGHT:
                    move('E');
                    break;
                case KeyEvent.VK_LEFT:
                    move('W');
                    break;
                case KeyEvent.VK_L:
                    look();
                    break;
                case KeyEvent.VK_H:
                    hello();
                    break;
                case KeyEvent.VK_P:
                    pickup();
                    break;
                case KeyEvent.VK_Q:
                    quitGame();
                    break;
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            //Do nothing.
        }
    }

    /**
     * Thread safe creation of GUI.
     * @param args - Port number and IP address
     */
    public static void main(final String[] args) {

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {

                createAndShowGUI(args[0], args[1]);
            }
        });
    }
}