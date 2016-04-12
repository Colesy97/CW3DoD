import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.Timer;



/**
 * ServerStarter provides a GUI based that allows the user to set up the relevant
 * data for the socket to be created so that multiple players can compete across the
 * network. It also allow the user to launch the server, players cannot be accepted until
 * the start button has been pressed as that launches the Server Class.
 *
 * @author Callum Coles
 * @version 1.0
 * @release 06/04/2016
 */
public class ServerStarter extends JFrame {

    private File mapName;

    //Code for the GUI
    //Used to get the size of the users screen.
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final int screenWidth = (int)screenSize.getWidth();
    private final int screenHeight = (int)screenSize.getHeight();

    //Sets the communication panel to 1/4ths of the screen width and the full screen height.
    private final int COMMU_PANEL_WIDTH = (int)(screenWidth * 0.5);
    private final int COMMU_PANEL_HEIGHT = (int)(screenHeight * 0.75);

    //Set up the variable needed for the communication panel.
    private JPanel commuPanel;
    private GridBagLayout commuPanelLayout;
    private static JTextArea textArea;
    private JButton botButton;
    private JButton setMap;
    private JButton setPort;
    private JButton startButton;

    private String mapStringName = "default.txt";
    private int portNum = 44444;

    private Communication communication = new Communication();
    private int timeDelay = 50;
    private ActionListener time;

    /**
     * ServerStarter constructor, passes the name for the GUI window to the super class and sets up
     * the time action listener.
     */
    ServerStarter(){
        super("ServerStarter");

        time = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                while(checkComm()){
                    readAndPrint();
                }

            }
        };
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method is invoked from the
     * event dispatch thread.
     */
    private static void startGUI(){
        //Create and set up the server starter and gui.
        ServerStarter sStart = new ServerStarter();
        sStart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
        sStart.initUI(sStart.getContentPane());
        //Display the window.
        sStart.pack();
        sStart.setVisible(true);
    }

    /**
     * Adds three panels to the container, one to display the dungeon of doom, one for communication
     * and the final one for control buttons.
     * @param frame - In which to put all the panels in.
     */
    private void initUI(final Container frame){
        setTitle("Server Starter");
        setSize(screenWidth/2, (3*screenHeight/4));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        commuPanel = new javax.swing.JPanel();
        commuPanelLayout = new GridBagLayout();
        commuPanel.setLayout(commuPanelLayout);

        commuPanel.setPreferredSize(new Dimension(COMMU_PANEL_WIDTH, COMMU_PANEL_HEIGHT));
        commuPanel.setBackground(Color.WHITE);

        JLabel screenTitle = new JLabel("Server Starter", SwingConstants.CENTER);
        screenTitle.setFont(new Font("Sans-Serif", Font.BOLD, 50));

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textArea.setEditable(false);

        botButton = new JButton("LAUNCH BOT");
        setMap = new JButton("SET MAP");
        setPort = new JButton("SET PORT");
        startButton = new JButton("START");

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10,5,10,5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.weighty = 0.0;
        c.weightx = 0.1;
        c.anchor = GridBagConstraints.CENTER;
        commuPanel.add(screenTitle, c);


        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.weighty = 1.0;
        c.weightx = 0.1;
        commuPanel.add(scrollPane, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.0;
        commuPanel.add(botButton, c);

        c.gridx = 1;
        commuPanel.add(setMap, c);

        c.gridx = 2;
        commuPanel.add(setPort, c);

        c.gridx = 3;
        commuPanel.add(startButton, c);

        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 10, 20);
        frame.setLayout(layout);
        frame.add(commuPanel);

        //Process the event for the botButton to launch a bot.
        botButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               //new Thread(new Bot()).start();
           }
        });

        //Process the event for the botButton to launch a bot.
        setMap.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                mapStringName = JOptionPane.showInputDialog("Enter a map: ", "default.txt");
            }
        });

        //Process the event for the botButton to launch a bot.
        setPort.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                portNum = Integer.parseInt(JOptionPane.showInputDialog("Enter a port number: ", portNum));
            }
        });

        new Timer(timeDelay, time).start();

        //Process the event for the botButton to start the server.
        startButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                createMap();
                startServer();
            }
        });

    }

    /**
     * Turns the string into a file and then uses this file to try read the map.
     */
    private void createMap() {
        mapName = new File("maps", mapStringName);
        GameLogic.readMap(mapName);
    }

    /**
     * Attempts to start the server, uses a SwingWorker for thread safety and GUI response.
     */
    private void startServer() {

        SwingWorker<Boolean, String> startServer = new SwingWorker<Boolean, String>() {
            Socket sock;
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    ServerSocket SerSocket = new ServerSocket(portNum);
                    publish("Listening");
                    while (true) {
                        Socket sock = SerSocket.accept();
                        new Thread(new Server(sock, mapName, communication)).start();
                        publish("Connected to: " + sock.getInetAddress());
                    }
                }catch (Exception e){
                    return false;
                }
            }
            protected void done(){
                boolean connectResult;
                try{
                    connectResult = get();
                    if(connectResult){
                        communicate("Connected to: " + sock.getInetAddress());
                    } else {
                        communicate("Failed to set up connection.");
                    }
                }catch (InterruptedException e1){

                }catch (ExecutionException e2){

                }
            }
            @Override
            protected void process(List<String> chunks){
                String toPublish = chunks.get(chunks.size()-1);
                communicate(toPublish);
            }
        };
        startServer.execute();
    }

    /**
     * Check communication object, return whether or not text has been received.
     * @return - whether some text has been processed.
     */
    private boolean checkComm() {
        if(communication.uncheckedLines() > 0){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Prints out the data from the communication object.
     */
    private void readAndPrint(){
        communicate(communication.getString());
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
     * Thread safe creation of GUI.
     * @param args - none
     */
    public static void main(String[] args) throws Exception {

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {

                startGUI();
            }
        });

    }
}
