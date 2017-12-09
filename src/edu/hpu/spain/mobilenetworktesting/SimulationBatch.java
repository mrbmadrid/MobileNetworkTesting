
package edu.hpu.spain.mobilenetworktesting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Label;

/**
 * @description Allows running a collection of simulations over multiple threads. 
 * @author Brian Spain
 */

public class SimulationBatch implements Runnable{
    
    SimulationEnvironment environment;
    
    private final int maxTime;
    private final int iterations, simulations;
    private final ArrayList<dataCompiler> data;
    ArrayList<Thread> simGroup;
    private long timer;
    Label Running;
    
    /**
     * @param rangeDecayThreshold
     * @param simulations
     * @param useFakeDestination
     * @param popInc
     * @description constructor which passes parameters to environment object.
     * @param height max y of the domain
     * @param width max x of the domain
     * @param population number of Nodes in the simulation
     * @param maxBuffer maximum buffer value for any node
     * @param transmitTime total time to transmit the message
     * @param maxTime maximum runtime for the simulations
     * @param range maximum transmit range
     * @param iterations number of environments
     */
    
    public SimulationBatch(int height, int width, int population, int popInc, 
            int range, int rangeDecayThreshold, int maxBuffer, int transmitTime,
            int maxTime, int iterations, int simulations, boolean useFakeDestination,
            Label Running) {
        
        this.environment = new SimulationEnvironment(height, width, population,
            popInc, range, rangeDecayThreshold, transmitTime, maxBuffer, 
            iterations, useFakeDestination);
        this.maxTime = maxTime;
        this.iterations = iterations;
        data = new ArrayList<>();
        simGroup = new ArrayList<>();
        this.simulations = simulations;
        this.Running = Running;
    }
    
    /**
     * @description executes the requested number of simulations on six
     * simulation threads.
     */
    
    @Override
    public void run(){
        Running.setText("Running");
        timer = System.currentTimeMillis();
        int simsrun = 0;
        for(int i = 0; i < iterations; i++){
            data.add(new dataCompiler());
        }
        
        for(int n = 0; n < simulations; n++){
            ++simsrun;
           /*
                generate new environments.
            */
           environment.GenerateEnvironments();
           /*
                clear previous threads.
           */
           simGroup.clear();
           /*
                dataRecords will store each of the array lists of strings of
                data for reconstruction, which is generated for every 1000th
                simulation
           */
            ArrayList<ArrayList<String>> dataRecords = new ArrayList<>();
            
            /*
                Add a simulation thread for each environment. 
            */

            for(int i = 0; i < iterations; i++){
                if((n+1) % 10000 == 0){
                    dataRecords.add(new ArrayList<>());
                    simGroup.add(new Thread(new Simulate(
                            environment.getSimEnvironment(i), maxTime,
                            environment.getSender(i), environment.getReciever(i), 
                            data.get(i), dataRecords.get(i))));
                    simGroup.get(i).start(); 
                }
                else{
                    simGroup.add(new Thread(new Simulate(
                            environment.getSimEnvironment(i), maxTime,
                            environment.getSender(i), environment.getReciever(i), 
                            data.get(i))));
                    simGroup.get(i).start();
                }
            }
            simGroup.forEach((sim) -> {
                try {
                    sim.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SimulationBatch.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            
            /*
                Every 1000th simulation has all raw data stored in a text file
                with a file name containing all simulation parameters.
            */
            
            if((n+1) % 10000 == 0){
                for(ArrayList<String> dataRecord : dataRecords)
                    try {
                        dataCompiler.rawDataToFile(environment.getHeight(),
                            environment.getWidth(),
                            environment.getPopulation(), 
                            environment.getBuffer(),
                            environment.getTransmitTime(), n, 2.0, dataRecord);
                    } catch (IOException ex) {
                        Logger.getLogger(SimulationBatch.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
            }
        }
        Running.setText("Done");
        /*
            Compile and output data for each batch of simulations
        */
        
        data.stream().map((compiler) -> {
            compiler.compile();
            return compiler;
        }).forEachOrdered((compiler) -> {
            compiler.print();
        });
        
        timer = System.currentTimeMillis()-timer;
        System.out.println("Time: " + (double)timer/60000.0);
    }
}  
