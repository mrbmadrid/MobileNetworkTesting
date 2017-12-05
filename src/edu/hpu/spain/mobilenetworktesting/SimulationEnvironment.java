
package edu.hpu.spain.mobilenetworktesting;

import java.util.ArrayList;
import java.util.Random;

/**
 * @description SimulationEnvironment class intended for mobile network 
 * simulation research at HPU.  Creates identical starting environments. 
 * @author Brian Spain
 */

public class SimulationEnvironment {
    
    ArrayList<ArrayList<Node>> SimEnvironments;                                 
    
    private final int height, width, population, range, rangeDecayThreshold;
    private final int popInc, maxBuffer, transmitTime;
    private final boolean useFakeDestination;
    private int[][] sendersandreceivers;
    
    /**
     * @description Constructor creates requested number of blank environments
     * @param height max y value of domain
     * @param width max x value of domain
     * @param population number of Nodes to populate each environment
     * @param range transmit range for each Node
     * @param transmitTime time to transfer message
     * @param maxBuffer maximum time to set buffer to for each Node
     * @param iterations number of environments to create
     */
    
    public SimulationEnvironment(int height, int width, int population, int popInc,
            int range, int rangeDecayThreshold, int transmitTime, int maxBuffer, 
            int iterations, boolean useFakeDestination){
        
        this.width=width;
        this.height=height;
        this.population = population;
        this.popInc = popInc;
        this.range=range;
        this.rangeDecayThreshold = rangeDecayThreshold;
        this.transmitTime = transmitTime;
        this.maxBuffer = maxBuffer;
        this.useFakeDestination = useFakeDestination;
        //Create ArrayList for each type of quadrant simulation
        SimEnvironments = new ArrayList<>();
        for(int i = 0; i < iterations; i++){
            SimEnvironments.add(new ArrayList<>());
        }
        sendersandreceivers = new int[iterations][2];
    }
    
    /**
    * @description Clears the environments and generates new environments. All
    * simulations have the same initial node parameters. 
    */
    
    public void GenerateEnvironments(){
        
        SimEnvironments.forEach((SimEnvironment) -> {
            SimEnvironment.clear();
        });
        
        Random random = new Random();
        
        //Choose sender and reciever, ensure they aren't the same node.
        
        
        
        //Populate each simulation with identical starting node conditions
        for(ArrayList<Node> SimEnvironment : SimEnvironments){
            for(int i = 0; i < (population + 
                    SimEnvironments.indexOf(SimEnvironment)*popInc); i++){
                double x = width * Math.abs(random.nextDouble()); //startnig x posit for node
                double y = height * Math.abs(random.nextDouble()); //starting y posit for node
                int theta = random.nextInt(360); //starting direction angle for node
                int buffer = random.nextInt(maxBuffer); //starting buffer for node

                SimEnvironment.add(new Node(i, width, height, x, y, theta, 
                buffer, transmitTime, range, rangeDecayThreshold, useFakeDestination));
            }
            sendersandreceivers[SimEnvironments.indexOf(SimEnvironment)][0] = random.nextInt(population + 
                    SimEnvironments.indexOf(SimEnvironment)*popInc);
            do{
                sendersandreceivers[SimEnvironments.indexOf(SimEnvironment)][1] = random.nextInt(population + 
                    SimEnvironments.indexOf(SimEnvironment)*popInc);
            }while(sendersandreceivers[SimEnvironments.indexOf(SimEnvironment)][0] == 
                    sendersandreceivers[SimEnvironments.indexOf(SimEnvironment)][1]);
            SimEnvironment.get(sendersandreceivers[SimEnvironments.indexOf(SimEnvironment)][0]).setSender();
        }

    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getRange() {
        return range;
    }

    public int getTransmitTime() {
        return transmitTime;
    }
    
    public int getPopulation(){
        return population;
    }
    
    public int getBuffer(){
        return maxBuffer;
    }

    public int getReciever(int index) {
        return sendersandreceivers[index][1];
    }

    public int getSender(int index) {
        return sendersandreceivers[index][0];
    }
    
    /**
     * @param index index of simulation environment
     * @return simulation environment at index
     */
    
    public ArrayList<Node> getSimEnvironment(int index) {
        return SimEnvironments.get(index);
    }

    public ArrayList<ArrayList<Node>> getSimEnvironments() {
        return SimEnvironments;
    }
    
}
