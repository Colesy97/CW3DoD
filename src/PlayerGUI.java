
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;



/**
 * PlayerGUI provides the framework for displaying a GUI based on the transfers between
 * the client object that it uses and the server.
 *
 * Swing Worker Concept learnt from: http://www.javaadvent.com/2012/12/multi-threading-in-java-swing-with-swingworker.html
 *
 * @author Callum Coles
 * @version 1.0
 * @release 06/04/2016
 */
public class PlayerGUI extends JFrame {

    //The last look window
    private String[] lookWindow = new String[5];

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

    //Sets up the Image Icons
    private ImageIcon mapIcon = null;
    private ImageIcon wallIcon = null;
    private ImageIcon playerIcon = null;
    private ImageIcon botIcon = null;
    private ImageIcon oPlayerIcon = null;
    private ImageIcon goldIcon = null;
    private ImageIcon exitIcon = null;
    private ImageIcon blankIcon = null;
    private ImageIcon xIcon = null;


    /**
     * Constructor for PlayerGUI, passes the window name to the super class and initialises the socket(IP)
     * address and the port number.
     * @param name  - Name of the window.
     * @param connectAddress - IP Address to connect to.
     * @param portNumber - Port number to connect to at IP address.
     */
    public PlayerGUI(String name, String connectAddress, int portNumber) {
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
        PlayerGUI gui = new PlayerGUI("Dungeon of Doom", IP, Integer.parseInt(port));
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

        JLabel commuTitle = new JLabel("Communication");

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
                    //Send look response to the server.
                    //Send to the server.
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
                //Update the communication window.
                communicate("You: QUIT");
                //Send quit response to the server.
                client.quitGame();
                checkAndCreate(false);
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
        controlPanel.add(quitButton, c);

        c.gridx = 4;
        c.gridy = 5;
        controlPanel.add(lookButton, c);

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

        try{
            connectToServer();
        }catch (IOException e){

        }

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
        client.initialHandshake(false);
        new Thread(new ClientListener(socket, communication)).start();
    }

    /**
     * Keeps checking the communication object until checkCommunication returns false
     * @param fromLook - is this being called from the look method.
     */
    private void checkAndCreate(final boolean fromLook){
        SwingWorker<Void, Void> commCheck = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try{
                    Thread.sleep(50);
                }catch(InterruptedException e){
                    //Non fatal.
                }
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
                        break;
                }
                return toReturn;
            }
        };
        GUIUpdate.execute();
    }

    /**
     * Check communication object, return whether or not text has been received.
     * @param fromLook - has this call come via the look function.
     * @return - whether some text has been processed.
     */
    private boolean checkCommunication(boolean fromLook){
        boolean somePrinted = false;
        synchronized(communication){
            int i = 0;
            while(communication.uncheckedLines() > 0){
                String newLine = communication.getString();
                processString(newLine);
                somePrinted = true;
                if (fromLook){
                    lookWindow[i] = newLine;
                    i++;
                }
            }
        }
        return somePrinted;
    }

    /**
     * Sends the string to be printed.
     * @param toProcess - String to process.
     */
    private void processString(String toProcess) {
        communicate(toProcess);
    }

    /**
     * Checks key presses by the user, so that the user can play via keys rather than button presses.
     */
    private class KeyChecker extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            int keyPressed = e.getKeyCode();
            switch( keyPressed ) {
                case KeyEvent.VK_UP:
                    //Update the communication window.
                    communicate("You: MOVE N");
                    //Send move north response to the server.
                    client.move("MOVE", 'N');
                    checkAndCreate(false);
                    break;
                case KeyEvent.VK_DOWN:
                    //Update the communication window.
                    communicate("You: MOVE S");
                    //Send move south response to the server.
                    client.move("MOVE", 'S');
                    checkAndCreate(false);
                    break;
                case KeyEvent.VK_RIGHT:
                    //Update the communication window.
                    communicate("You: MOVE E");
                    //Send move east response to the server.
                    client.move("MOVE", 'E');
                    checkAndCreate(false);
                    break;
                case KeyEvent.VK_LEFT:
                    //Update the communication window.
                    communicate("You: MOVE W");
                    //Send move west response to the server.
                    client.move("MOVE", 'W');
                    checkAndCreate(false);
                    break;
                case KeyEvent.VK_L:
                    //Update the communication window.
                    communicate("You: LOOK");
                    //Send look command to the server.
                    client.look();
                    checkAndCreate(true);
                    updateGUI();
                    break;
                case KeyEvent.VK_H:
                    //Update the communication window.
                    communicate("You: HELLO");
                    //Send hello command to the server.
                    client.hello();
                    checkAndCreate(false);
                    break;
                case KeyEvent.VK_P:
                    //Update the communication window.
                    communicate("You: PICKUP");
                    //Send pickup command to the server.
                    client.pickup();
                    checkAndCreate(false);
                    break;
                case KeyEvent.VK_Q:
                    //Update the communication window.
                    communicate("You: QUIT");
                    //Send quit command to the server.
                    client.quitGame();
                    checkAndCreate(false);
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