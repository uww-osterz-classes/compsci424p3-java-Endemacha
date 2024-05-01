/* COMPSCI 424 Program 3
 * Name:
 * 
 * This is a template. Program3.java *must* contain the main class
 * for this program. 
 * 
 * You will need to add other classes to complete the program, but
 * there's more than one way to do this. Create a class structure
 * that works for you. Add any classes, methods, and data structures
 * that you need to solve the problem and display your solution in the
 * correct format.
 */

package compsci424.p3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Main class for this program. To help you get started, the major
 * steps for the main program are shown as comments in the main
 * method. Feel free to add more comments to help you understand
 * your code, or for any reason. Also feel free to edit this
 * comment to be more helpful.
 */
public class Program3 {
    // Declare any class/instance variables that you need here.
    private static Semaphore allocationPermit = new Semaphore(1);

    /**
     * @param args Command-line arguments. 
     * 
     * args[0] should be a string, either "manual" or "auto". 
     * 
     * args[1] should be another string: the path to the setup file
     * that will be used to initialize your program's data structures. 
     * To avoid having to use full paths, put your setup files in the
     * top-level directory of this repository.
     * - For Test Case 1, use "424-p3-test1.txt".
     * - For Test Case 2, use "424-p3-test2.txt".
     */
    public static void main(String[] args) {
        // Code to test command-line argument processing.
        // You can keep, modify, or remove this. It's not required.
        if (args.length < 2) {
            System.err.println("Not enough command-line arguments provided, exiting.");
            return;
        }
        System.out.println("Selected mode: " + args[0]);
        System.out.println("Setup file location: " + args[1]);

        // 1. Open the setup file using the path in args[1]
        String currentLine;
        BufferedReader setupFileReader;
        try {
            setupFileReader = new BufferedReader(new FileReader(args[1]));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find setup file at " + args[1] + ", exiting.");
            return;
       }

        // 2. Get the number of resources and processes from the setup
        // file, and use this info to create the Banker's Algorithm
        // data structures
        int numResources;
        int numProcesses;

        int[] availableUnits;
        int[][] maxClaims;
        int[][] currentAllocations;
        int[][] currentRequests;

        // For simplicity's sake, we'll use one try block to handle
        // possible exceptions for all code that reads the setup file.
        try {
            // Get number of resources
            currentLine = setupFileReader.readLine();
            if (currentLine == null) {
                System.err.println("Cannot find number of resources, exiting.");
                setupFileReader.close();
                return;
            }
            else {
                numResources = Integer.parseInt(currentLine.split(" ")[0]);
                System.out.println(numResources + " resources");
            }
 
            // Get number of processes
            currentLine = setupFileReader.readLine();
            if (currentLine == null) {
                System.err.println("Cannot find number of processes, exiting.");
                setupFileReader.close();
                return;
            }
            else {
                numProcesses = Integer.parseInt(currentLine.split(" ")[0]);
                System.out.println(numProcesses + " processes");
            }

            // Create the Banker's Algorithm data structures, in any
            // way you like as long as they have the correct size

            availableUnits = new int[numResources];
            maxClaims = new int[numProcesses][numResources];
            currentAllocations = new int[numProcesses][numResources];
            currentRequests = new int[numProcesses][numResources];

            // 3. Use the rest of the setup file to initialize the
            // data structures

            for(int p = 0; p<numProcesses; p++){
                for(int r = 0; r<numResources; r++){
                    currentRequests[p][r] = 0;
                }
            }

            setupFileReader.readLine(); //skip "Available" line
            currentLine = setupFileReader.readLine();
            if (currentLine == null) {
                System.err.println("Cannot find number of processes, exiting.");
                setupFileReader.close();
                return;
            }else{
                String[] availableAllocations = currentLine.split(" ");
                for(int i = 0; i < availableAllocations.length; i++){
                    availableUnits[i] = Integer.parseInt(availableAllocations[i]);
                }
            }

            setupFileReader.readLine(); //skip "Max" line
            currentLine = setupFileReader.readLine();
            int pseudoProcessID = 0;
            while(!currentLine.equals("Allocation")) {
                if (currentLine == null) {
                    System.err.println("Data incomplete");
                    setupFileReader.close();
                    return;
                } else {

                    String[] maxAllocations = currentLine.split(" ");
                    for (int i = 0; i < maxAllocations.length; i++) {
                        maxClaims[pseudoProcessID][i] = Integer.parseInt(maxAllocations[i]);
                    }

                    pseudoProcessID += 1;
                    if (pseudoProcessID > numProcesses) {
                        System.out.println("Data mismatch: num processes > definition");
                        return;
                    }
                }
                currentLine = setupFileReader.readLine();
            }

            pseudoProcessID = 0;
            while(true) {
                currentLine = setupFileReader.readLine();
                if (currentLine == null) {
                    System.out.println("Setup complete...");
                    System.out.println();
                    setupFileReader.close();
                    break;
                } else {

                    String[] allocations = currentLine.split(" ");
                    for (int i = 0; i < allocations.length; i++) {
                        currentAllocations[pseudoProcessID][i] = Integer.parseInt(allocations[i]);
                    }

                    pseudoProcessID += 1;
                    if (pseudoProcessID > numProcesses) {
                        System.out.println("Data mismatch: num processes > definition");
                        return;
                    }
                }
            }


            setupFileReader.close(); // done reading the file, so close it
        }
        catch (IOException e) {
            System.err.println("Something went wrong while reading setup file "
            + args[1] + ". Stack trace follows. Exiting.");
            e.printStackTrace(System.err);
            System.err.println("Exiting.");
            return;
        }

        // 4. Check initial conditions to ensure that the system is 
        // beginning in a safe state: see "Check initial conditions"
        // in the Program 3 instructions
        int totalUnits[] = new int[numResources];
        for(int r = 0; r<numResources; r++){
            totalUnits[r] += availableUnits[r];
        }
        for(int p = 0; p<numProcesses; p++){
            for(int r = 0; r<numResources; r++){

                //Check one
                if(currentAllocations[p][r] > maxClaims[p][r]){
                    System.err.println("invalid resource claim: allocation over max");
                    return;
                }

                //Check 2
                totalUnits[r] += currentAllocations[p][r];
            }
        }

        if(!isSafe(maxClaims, currentAllocations, availableUnits)){
            System.err.println("Invalid source file: Unsafe graph");
            return;
        }

        // 5. Go into either manual or automatic mode, depending on
        // the value of args[0]; you could implement these two modes
        // as separate methods within this class, as separate classes
        // with their own main methods, or as additional code within
        // this main method.

        if(args[0].equals("manual")){
            System.out.println("You are in manual mode: Opening CLI...\n");

            Scanner input = new Scanner(System.in);

            while(true){
                String[] command = input.nextLine().split(" ");

                if(command[0].equals("end")){
                    return;
                }

                if(command[0].equals("request")){
                    int numRequests = Integer.parseInt(command[1]);
                    int p = Integer.parseInt(command[5]);
                    int r = Integer.parseInt(command[3]);
                    Thread t = new Thread(() -> {
                        while(true){
                            try {
                                if(numRequests <= maxClaims[p][r] - currentAllocations[p][r] && numRequests <= availableUnits[r]){
                                    allocationPermit.acquire();
                                    currentAllocations[p][r] += numRequests;
                                    availableUnits[r] -= numRequests;
                                    if(!isSafe(maxClaims, currentAllocations, availableUnits)){ //If not safe deny and revert
                                        currentAllocations[p][r] -= numRequests;
                                        availableUnits[r] += numRequests;
                                        System.out.println("Process " + p + " requests " + numRequests + " of resource " + r + ": denied(unsafe)");
                                    }else{ //Otherwise is safe
                                        System.out.println("Process " + p + " requests " + numRequests + " of resource " + r + ": granted");
                                    }
                                    allocationPermit.release();
                                }else{
                                    System.out.println("Process " + p + " requests " + numRequests + " of resource " + r + ": denied(Over capacity)");
                                }
                                return;
                            } catch (InterruptedException e) {
                                //Do nothing;
                            }
                        }
                    });
                    t.run();
                }

                if(command[0].equals("release")){
                    int numReleases = Integer.parseInt(command[1]);
                    int p = Integer.parseInt(command[5]);
                    int r = Integer.parseInt(command[3]);

                    Thread t = new Thread(() -> {
                        while(true){
                            try {
                                if(numReleases <= 0){
                                    System.out.println("Process " + p + " releases " + numReleases + " of resource " + r + ": denied(invalid number)");
                                }else if(numReleases > currentAllocations[p][r]){
                                    System.out.println("Process " + p + " releases " + numReleases + " of resource " + r + ": denied(Over capacity)");
                                }else{
                                    allocationPermit.acquire();
                                    currentAllocations[p][r] -= numReleases;
                                    availableUnits[r] += numReleases;
                                    System.out.println("Process " + p + " releases " + numReleases + " of resource " + r + ": granted");
                                    allocationPermit.release();
                                }
                                return;

                            } catch (InterruptedException e) {
                                //Do nothing;
                            }
                        }
                    });
                    t.run();
                }
            }
        }else if(args[0].equals("automatic")){

        }else{
            System.err.println("Invalid command: " + args[0]);
        }

    }


    private static boolean isSafe(int[][] maxClaims, int[][] currentAllocations, int[] availableUnits){
        int numProcesses = maxClaims.length;
        int numResources = maxClaims[0].length; //Using 0 but can be any.
        int[] tentativeUnits = availableUnits.clone();

        LinkedList<Integer> processSet = new LinkedList();
        for(int p = 0; p<numProcesses; p++){
            processSet.add(p);
        }
        int attempts = 0; //If this reaches above num processes, graph can not be reduced
        while(!processSet.isEmpty()){
            attempts+=1;
            int p = processSet.getFirst();
            //int[] currentProcessRequests = currentRequests[p];
            int[] potentialProcessRequests = new int[numResources];

            //initialize potential requests array
            for(int r = 0; r < numResources; r++){
                //int requestedUnits = currentProcessRequests[r];
                potentialProcessRequests[r] = maxClaims[p][r] - currentAllocations[p][r];
            }

            //Graph reduction attempt
            boolean isBlocked = false;
            for(int r = 0; r < numResources; r++){

                if(tentativeUnits[r] < potentialProcessRequests[r]){
                    isBlocked = true;

                    //Moving p to end of set
                    processSet.removeFirst();
                    processSet.addLast(p);

                    if(attempts > processSet.size()) {
                        return false; //No process further reducible
                    }
                    break; //Unable to reduce, trying next process;
                }
            }
            if(isBlocked) continue; //Unable to reduce, trying next process

            processSet.removeFirst();
            attempts = 0;
            for(int r = 0; r<numResources; r++) {
                tentativeUnits[r] += currentAllocations[p][r];
            }
        }

        return true; //Only reached if graph is safely reducable
    }
}
