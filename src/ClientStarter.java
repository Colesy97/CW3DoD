
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;


/**
 * ClientStarter provides a GUI based that allows the user to set up the relevant
 * data for the socket to be created so that multiple players can compete across the
 * network. It also allow the user to launch a PlayerGUI or WorldGUI.
 *
 * @author Callum Coles
 * @version 1.0
 * @release 06/04/2016
 */
public class ClientStarter extends JFrame {

    //Code for the GUI
    //Used to get the size of the users screen.
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final int screenWidth = (int)screenSize.getWidth();
    private final int screenHeight = (int)screenSize.getHeight();

    //Sets the communication panel to 1/4ths of the screen width and the full screen height.
    private final int COMMU_PANEL_WIDTH = (int)(screenWidth * 0.5);
    private final int COMMU_PANEL_HEIGHT = (int)(screenHeight * 0.25);

    //Set up the variable needed for the communication panel.
    private JPanel commuPanel;
    private GridBagLayout commuPanelLayout;
    private static JTextArea textArea;
    private JButton setIP;
    private JButton setPort;
    private JButton wholeMap;
    private JButton lookMap;

    private String mapStringName = "default.txt";
    private int portNum = 44444;
    private String ipAddress = "127.0.0.1";

    /**
     * Constructor, passes the name for the window to the super class.
     */
    ClientStarter(){
        super("ClientStarter");
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method is invoked from the
     * event dispatch thread.
     */
    private static void startGUI(){
        //Create and set up the server starter and gui.
        ClientStarter cStart = new ClientStarter();
        cStart.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
        cStart.initUI(cStart.getContentPane());
        //Display the window.
        cStart.pack();
        cStart.setVisible(true);
    }

    /**
     * Adds three panels to the container, one to display the dungeon of doom, one for communication
     * and the final one for control buttons.
     * @param frame - In which to put all the panels in.
     */
    private void initUI(final Container frame){
        setTitle("Client Starter");
        setSize(screenWidth/2, (screenHeight/4));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        commuPanel = new javax.swing.JPanel();
        commuPanelLayout = new GridBagLayout();
        commuPanel.setLayout(commuPanelLayout);

        commuPanel.setPreferredSize(new Dimension(COMMU_PANEL_WIDTH, COMMU_PANEL_HEIGHT));
        commuPanel.setBackground(Color.WHITE);

        JLabel screenTitle = new JLabel("Client Starter", SwingConstants.CENTER);
        screenTitle.setFont(new Font("Sans-Serif", Font.BOLD, 50));

        setIP = new JButton("SET IP");
        setPort = new JButton("SET PORT");
        wholeMap = new JButton("GODS EYE GUI");
        lookMap = new JButton("PLAYER GUI");

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
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.1;
        c.weighty = 0.0;
        commuPanel.add(setIP, c);

        c.gridx = 1;
        commuPanel.add(setPort, c);

        c.gridx = 2;
        commuPanel.add(lookMap, c);

        c.gridx = 3;
        commuPanel.add(wholeMap, c);

        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 10, 20);
        frame.setLayout(layout);
        frame.add(commuPanel);

        //Process the event for the botButton to launch a bot.
        wholeMap.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                createWorldGUI();
            }
        });

        //Process the event for the botButton to launch a bot.
        setIP.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                ipAddress = JOptionPane.showInputDialog("Enter an IP Address: ", "127.0.0.1");
            }
        });

        //Process the event for the botButton to launch a bot.
        setPort.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                portNum = Integer.parseInt(JOptionPane.showInputDialog("Enter a port number: ", portNum));
            }
        });

        //Process the event for the botButton to start the server.
        lookMap.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                createPlayerGUI();
            }
        });

    }

    /**
     * Calls the main method of the PlayerGUI to start it running.
     */
    private void createPlayerGUI(){
        System.out.println(ipAddress);
        SwingWorker<Void, Void> cpg = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String[] args = new String[2];
                args[0] = Integer.toString(portNum);
                args[1] = ipAddress;
                PlayerGUI.main(args);
                return null;
            }
        };
        cpg.execute();

    }

    /**
     * Calls the main method of the WorldGUI to start it running.
     */
    private void createWorldGUI(){
        SwingWorker<Void, Void> cwg = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String[] args = new String[2];
                args[0] = Integer.toString(portNum);
                args[1] = ipAddress;
                WorldGUI.main(args);
                return null;
            }
        };
        cwg.execute();
    }

    /**
     * Main Method. Used to start the Socket listening on port 44444.
     * The while loop is used to keep accepting connections, it does this by creating
     * a new Thread every time there is a connection request. Allowing multiple clients.
     *
     * @param args
     * @throws Exception
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



