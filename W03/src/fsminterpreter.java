import java.io.*;
import java.util.*;

public class fsminterpreter {

    private static final int FROM_COLUMN_POS = 0;
    private static final int INPUT_COLUMN_POS = 1;
    private static final int OUTPUT_COLUMN_POS = 2;
    private static final int TO_COLUMN_POS = 3;

    /**
     * Makes sure the input and file given aren't empty or blank
     * Checks the file exists
     * Validates the FSM isn't malformed
     * Validates the input provided is valid
     * Interpretes the FSM
     * @param args
     */
    
    public static void main(String[] args) {
        
        Scanner sc = new Scanner(System.in);
        String input = "";
        String temp = sc.next();

        while (temp.isBlank() || temp.isEmpty()) { //making sure input isn't empty
            temp = sc.next();
            System.out.println("Please enter an input or exit using 'exit'");
            if (temp.equals("exit")) {
                System.exit(0);
            }
        } 

        input = temp;
        sc.close();

        int tempIndex = 0;
        if (args.length == 0) { //exiting if there are no arguements provided on command line 
            System.err.println("No file provided");
            System.exit(0);
        }

        while (args[tempIndex].isEmpty() || args[tempIndex].isBlank()) { //making sure the arguement isn't empty before utilizing it
            tempIndex++;
        } 

        fsminterpreter fsm = new fsminterpreter(); //new instance of fsminterpreter class

        File fsmFile = new File(args[tempIndex].toString()); //instantiates file

        fsm.fileValidation(args[tempIndex].toString()); //validates the file given
        
        String[][] fsmData = fsm.readFsmFile(fsmFile); //reads the data from the FSM file

        fsm.malformedFSM(fsmData); //checks if FSM is malformed

        fsm.inputValidation(input, fsmData); //validates input from user
        
        fsm.interpretFSM(fsmData, input); //interprets the FSM


    }
    
    /**
     * Used to check that the file provided on the command line exists
     * If file doesn't exist then an error message is shown
     * @param fileName
     */
    public void fileValidation(String fileName) {
        File file = new File(fileName);
        if (!file.isFile()) {
            System.err.println("file provided does not exist");
            System.exit(0);
        }
        
    }

    /**
     * Used to validate that the input provided is valid for the FSM that has been provideds
     * @param input -  provided from user
     * @param fsmData - the FSM data in a 2D String array
     * Each input from FSM is compared with each of the input characters from user to make sure they are valid
     */
    public void inputValidation(String input, String[][] fsmData) {

        Set<String> inputAlphabet = getInputAlphabet(fsmData);
        String[] inputAlphabetArray = inputAlphabet.toArray(new String[inputAlphabet.size()]);
        String[] inputSplit = input.split("");
        boolean found = false;

        for (int i = 0; i < inputSplit.length; i++) {
            found = false;
            for (int x = 0; x < inputAlphabetArray.length; x++) {
                if (inputSplit[i].equals(inputAlphabetArray[x])) {
                    found = true;
                } 
            }
            if (!found) {
                System.err.println("Bad input");
                System.exit(0);
            }
        }   
    }
    
    /**
     * Reads the FSM file into 4 Array Lists for each of the columns
     * Line is split and put in a String array, then placed into the corresponding array list
     * The data from the Array Lists are then placed in a 2D array and returned
     * @param file
     * @return
     */
    public String[][] readFsmFile(File file) {

       ArrayList<String> FROM = new ArrayList<>();
       ArrayList<String> INPUT = new ArrayList<>();
       ArrayList<String> OUTPUT = new ArrayList<>();
       ArrayList<String> TO = new ArrayList<>();

       int numLines = 0; 
       
        try {

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "";
            
            while ((line = reader.readLine()) != null) {
               
                String[] lineSplit = line.split(" ");

                for (int i = 0; i < 4; i++) {
                    if (lineSplit[i].isBlank()) {
                        System.err.println("Bad description");
                        System.exit(0);
                    }
                }
                
                FROM.add(lineSplit[FROM_COLUMN_POS]);
                INPUT.add(lineSplit[INPUT_COLUMN_POS]);
                OUTPUT.add(lineSplit[OUTPUT_COLUMN_POS]);
                TO.add(lineSplit[TO_COLUMN_POS]);

                numLines++;
            }
            

            reader.close();

            
        } catch (FileNotFoundException fne){
            System.err.println(file.toString() + " not found");
        
        } catch (IOException e) {
            System.err.println("error reading fsm file");
        } 
       

        if (FROM.size() != numLines || INPUT.size() != numLines || OUTPUT.size() != numLines || TO.size() != numLines) {
            System.err.println("Bad description");
            System.exit(0);
        }

        String[][] fsmData = new String[4][numLines];

        for (int i = 0; i < numLines; i++) {
            fsmData[FROM_COLUMN_POS][i] = FROM.get(i);
            fsmData[INPUT_COLUMN_POS][i] = INPUT.get(i);
            fsmData[OUTPUT_COLUMN_POS][i] = OUTPUT.get(i);
            fsmData[TO_COLUMN_POS][i] = TO.get(i);
        }
       
        return fsmData;

        
    }

    /**
     * Different methods to test if the FSM provided is malformed
     * @param fsmData
     */

    public void malformedFSM(String[][] fsmData) {
        
        correctNumberOfInputs(fsmData); //test the number of different inputs
        correctNumberOfStates(fsmData); //test there are the right amount of states
        equalNumberOfEachState(fsmData); //test there are equal amount of each state

    }

    /**
     * Checks there is the right amount of inputs for the FSM provided
     * @param fsmData
     */

    public void correctNumberOfInputs(String[][] fsmData) {

        int totalLines = fsmData[FROM_COLUMN_POS].length;
        int totalStates = getNumberOfStates(fsmData);
        double correctAmountInputs = (double) totalLines / totalStates;

        double numberOfInputs = getInputAlphabetSize(getInputAlphabet(fsmData));

        if (numberOfInputs != correctAmountInputs) {
            System.err.println("Bad description");
            System.exit(0);
        }
    }

    /**
     * Checks the number of states in the FSM match the expected amount of states
     * @param fsmData
     */
    public void correctNumberOfStates(String[][] fsmData){

        int totalInputs = getInputAlphabetSize(getInputAlphabet(fsmData));
        int totalLines = fsmData[FROM_COLUMN_POS].length;
        double correctAmountStates = (double) totalLines / totalInputs;
        double numberOfStates = getNumberOfStates(fsmData);

        if (numberOfStates != correctAmountStates) {
            System.err.println("Bad description");
            System.exit(0);
        }

    }

    /**
     * Makes sure there are equal amounts of each state in thr FROM and TO columns of FSM provided
     * @param fsmData
     */
    public void equalNumberOfEachState(String[][] fsmData) {

        int totalStates = getNumberOfStates(fsmData);
        int inputAlphabetSize = getInputAlphabetSize(getInputAlphabet(fsmData));

        Set<String> statesSet = getStates(fsmData);
        String[] states = statesSet.toArray(new String[statesSet.size()]);
        
        int correctTotalOfInitialStateInstances = inputAlphabetSize;
        int[] numberFROMInstances = new int[totalStates];
       
        for (int i = 0; i < totalStates; i++) { //each state
            int totalFROM = 0;
            
            for (int x = 0; x < fsmData[0].length; x++) { //each line
                if (states[i].equals(fsmData[FROM_COLUMN_POS][x])) {
                    totalFROM++;
                }
               
            }
            numberFROMInstances[i] = totalFROM;
           
        }

        for (int y = 0; y < totalStates; y++) {
            if (numberFROMInstances[y] != correctTotalOfInitialStateInstances){
                System.err.println("Bad description");
                System.exit(0);
            }
        }
       
    }
    
    /**
     * Method that interprets the FSM provided and produces an output from the input provided
     * @param fsmData
     * @param input
     * The output is produced by:
     *      traversing through the input array
     *      finding the line in the FSM where the current state is, and the input letter is
     *      using this, the output and the next state are able to be determined
     * 
     */
    public void interpretFSM(String[][] fsmData, String input) {

        String[] inputSplit = input.split("");

        String initialStateString = fsmData[FROM_COLUMN_POS][0];
        String currentStateString = initialStateString;
        String nextStateString = "";

        Set<String> inputAlphabet = getInputAlphabet(fsmData);
        int inputAlphabetSize = getInputAlphabetSize(inputAlphabet);
        
        int totalLinesFsmData = fsmData[0].length;

        String outputString = "";


        for (int i = 0; i < inputSplit.length; i++) {

            int[] range = getRange(fsmData, inputAlphabetSize, totalLinesFsmData, currentStateString);
            int lineNumber = findLine(range, inputSplit[i], fsmData);
            outputString += fsmData[OUTPUT_COLUMN_POS][lineNumber];
            nextStateString = fsmData[TO_COLUMN_POS][lineNumber];
            currentStateString = nextStateString;
        }

        System.out.println(outputString);

        
    }

    
    /**
     * gets the input alphabet
     * @param fsmData
     * @return
     */
    public Set<String> getInputAlphabet(String[][] fsmData) {
        Set<String> inputAlphabet = new HashSet<>();
        for (int i = 0; i < fsmData[0].length; i++) {
            inputAlphabet.add(fsmData[INPUT_COLUMN_POS][i]);
        }
        return inputAlphabet;
    }

    /**
     * gets the size of the input alphabet
     * @param inputAlphabet
     * @return
     */
    public int getInputAlphabetSize(Set<String> inputAlphabet) {
        int size = inputAlphabet.size();
        return size;
    }


    /**
     * The range is the points in the FSM file where the state provided has its data within the file
     * @param fsmData
     * @param inputAlphabetSize
     * @param totalLinesFsmData
     * @param stateCharacter
     * @return
     */
    public int[] getRange(String[][] fsmData, int inputAlphabetSize, int totalLinesFsmData, String stateCharacter) {
        
        int[] range = new int[inputAlphabetSize];
        int rangeIndex = 0;

        for (int i = 0; i < fsmData[FROM_COLUMN_POS].length; i++) {
            if (stateCharacter.equals(fsmData[FROM_COLUMN_POS][i])) {
                range[rangeIndex] = i;
                rangeIndex++;
            }
        }

        return range;
    }

    /**
     * Finds the line in the FSM using the range, that the input is the same as the input given
     * @param range
     * @param input
     * @param fsmData
     * @return
     */
    public int findLine(int[] range, String input, String[][] fsmData) {
        int lineNumber = 0;

        for (int i = 0; i < range.length; i++) {
            if (input.equals(fsmData[INPUT_COLUMN_POS][range[i]])) {
                lineNumber = range[i];
            }
        }

        return lineNumber;
    }

    /**
     * gets the states
     * @param fsmData
     * @return
     */
    public Set<String> getStates(String[][] fsmData) {
        Set<String> states = new HashSet<String>();
        for (int i = 0; i < fsmData[FROM_COLUMN_POS].length; i++) {
            states.add(fsmData[FROM_COLUMN_POS][i]);
        }
       return states;
    }

    /**
     * gets the number of states
     * @param fsmData
     * @return
     */
    public int getNumberOfStates(String[][] fsmData) {
        Set<String> states = getStates(fsmData);
        int size = states.size();
        return size;
    }

}
