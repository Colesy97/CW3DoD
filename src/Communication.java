
import java.util.ArrayList;



/**
 * Communication is used to keep track of communication from the network, whenever
 * data is added to the ArrayList it increments the unchecked counter, and whenever communication
 * is accessed it increments the checked counter.
 *
 * @author Callum Coles
 * @version 1.0
 * @release 06/04/2016
 */
public class Communication {

    private ArrayList<String> allCommunication = new ArrayList<String>();
    private int checkedLines;
    private int totalLines;

    /**
     * Constructor, sets the total and checked lines to zero.
     */
    Communication(){
        checkedLines = 0;
        totalLines = 0;
    }

    /**
     * Increments the number of checked lines.
     */
    private void incCheckedLines(){
        checkedLines += 1;
    }

    /**
     * Increments the number of total lines.
     * @param toInc - Amount to increment by.
     */
    private void incTotalLine(int toInc){
        totalLines += toInc;
    }

    /**
     * @return - The amount of unchecked lines.
     */
    public int uncheckedLines(){
        return (totalLines - checkedLines);
    }

    /**
     * Adds received string to the array list.
     * @param newCommunication - String to add to the array list.
     */
    public void addString(String newCommunication){
        String[] lines = newCommunication.split(System.getProperty("line.separator"));
        for(int i = 0; i<lines.length; i++){
            allCommunication.add(lines[i]);
        }
        incTotalLine(lines.length);
    }

    /**
     * Increments the number of lines checked and returns a line.
     * @return - returns the next unchecked line.
     */
    public String getString(){
        incCheckedLines();
        return allCommunication.get(checkedLines-1);
    }


}
