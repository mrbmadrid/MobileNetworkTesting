
package mobilenetworktesting;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @description Simulate class intended for mobile network simulation research at
 * HPU. This class will simulate a population of mobile networking nodes under
 * the given parameters.
 * @author Brian Spain
 */

public class Simulate implements Runnable{
    
    private final ArrayList<Node> Nodes;
    private final int maxTime, sender, reciever;
    private final dataCompiler compiler;
    boolean[] success;
    boolean recordRawData;
    ArrayList<String> dataRecord;
    
    /**
     * @description constructor, fills the success boolean array with false
     * @param Nodes all the Node objects populating this simulation
     * @param maxTime maximum runtime allowed for simulation
     * @param sender the Node that will originate the message
     * @param reciever the Node that is the intended target for the message
     * @param compiler an instance of dataCompiler that is used to catalog data
     */

    public Simulate(ArrayList<Node> Nodes, int maxTime, int sender, 
            int reciever, dataCompiler compiler) {
        
        this.Nodes = Nodes;
        this.maxTime = maxTime;
        this.reciever = reciever;
        this.sender = sender;
        this.compiler = compiler;
        this.recordRawData = false;
        
        success = new boolean[4]; //used to track success of each message type
        Arrays.fill(success, Boolean.FALSE);
    }
    
   /**
     * @description constructor for Simulations that will store raw data for 
     * later reconstruction.
     * @param Nodes all the Node objects populating this simulation
     * @param maxTime maximum runtime allowed for simulation
     * @param sender the Node that will originate the message
     * @param reciever the Node that is the intended target for the message
     * @param compiler an instance of dataCompiler that is used to catalog data
     * @param dataRecord will contain each data string for reconstruction
     */
    
    public Simulate(ArrayList<Node> Nodes, int maxTime, int sender, 
            int reciever, dataCompiler compiler, ArrayList<String> dataRecord) {
        
        this(Nodes, maxTime, sender, reciever, compiler);
        
        this.dataRecord = dataRecord;
        this.recordRawData = true;
        this.dataRecord.add((int)Nodes.get(sender).getx() + " " + (int)Nodes.get(sender).gety() +
                " " + (int)Nodes.get(reciever).getx() + " " + (int)Nodes.get(reciever).gety());
    }
    
    /**
     * @description will run until maxTime is reached.
     */
    
    @Override
    public void run() {   
        for(int t = 0; t < maxTime; t++){
            if(!recordRawData) incrementTime();
            else{
                if(t==0){
                    Nodes.forEach((node) -> {
                        node.recordLocation(dataRecord);
                    });
                }
                incrementTime(dataRecord);
            }
        }
        compiler.addData(extractSimData());
    }
    
    /**
     * @description updates the position and action of each Node for one time
     * increment
     * @author Brian Spain
     */
    
    public void incrementTime(){
        
        //Loops throught all the nodes updating their position
        //Each node returns an int[4] [Flood, Gossip80, Gossip60, Infect]
        //0 - Do Nothing
        //1 - Take first Picture
        //2 - Broadcast
        
        int[][] broadcast = new int[Nodes.size()][4];
        for(int i = 0; i < broadcast.length; i++){
            broadcast[i] = Nodes.get(i).updateNode();
        }

        for(int i = 0; i < Nodes.size(); i++){
            for( int messageType = 0; messageType < 4; messageType++){
                if(broadcast[i][messageType]==1){
                    Nodes.get(i).setEligibleNeighbors(messageType, Nodes);
                }
                else if(broadcast[i][messageType]==2 && Nodes.get(i).getTransmitSpeed()==0){ 
                    if(i==reciever)
                            success[messageType] = true;
                    else{
                        Nodes.get(i).setEligibleNeighbors(messageType, Nodes);
                        Nodes.get(i).broadcast(messageType, reciever, Nodes);
                    }
                }
                else if(broadcast[i][messageType]==2){
                    if(i==reciever)
                        success[messageType] = true;
                    else
                    Nodes.get(i).broadcast(messageType, reciever, Nodes);
                }
            }
        }
    }
    
    /**
     * @description same as standard incrementTime except data strings for
     * reconstruction are stored in dataRecord
     * @param dataRecord will contain each data string for reconstruction
     * @author Brian Spain
     */
    
    public void incrementTime(ArrayList<String> dataRecord){

        //Loops throught all the nodes updating their position
        //Each node returns an int[4] [Flood, Gossip80, Gossip60, Infect]
        //0 - Do Nothing
        //1 - Take first Picture
        //2 - Broadcast
        
        int[][] broadcast = new int[Nodes.size()][4];
        for(int i = 0; i < broadcast.length; i++){
            broadcast[i] = Nodes.get(i).updateNode();
        }

        for(int i = 0; i < Nodes.size(); i++){
            for( int messageType = 0; messageType < 4; messageType++){
                if(broadcast[i][messageType]==1){
                    Nodes.get(i).setEligibleNeighbors(messageType, Nodes);
                }
                else if(broadcast[i][messageType]==2 && Nodes.get(i).getTransmitSpeed()==0){ 
                    if(i==reciever)
                            success[messageType] = true;
                    else{
                        dataRecord.add("b " + Nodes.get(i).getID() + " " + 
                                messageType + " " + (int)Nodes.get(i).getx() + " " + (int)Nodes.get(i).gety());
                        Nodes.get(i).setEligibleNeighbors(messageType, Nodes);
                        Nodes.get(i).broadcast(messageType, reciever, Nodes, dataRecord);
                    }
                }
                else if(broadcast[i][messageType]==2){
                    if(i==reciever)
                        success[messageType] = true;
                    else{
                        dataRecord.add("b " + Nodes.get(i).getID() + " " + 
                                messageType + " " + (int)Nodes.get(i).getx() + " " + (int)Nodes.get(i).gety());
                        Nodes.get(i).broadcast(messageType, reciever, Nodes, dataRecord);
                    }
                }
            }
        }
    }

    
    /**
     * @description pulls relevant data from every node after simulation
     * @return the ArrayList contains two two dimensional double arrays. 
     * ArrayList.get(0): contains success Case data
     * What is stored in the successCase:
     * Flood/Gossip (index 0-2)
     * 0: #successes 1: #infected at success 2: #infected nodes 
     * 3: generation of success 4: highest generation 5: starting neighbors
     * 6: average neighbors
     * Infect Model (index 3)
     * 0: #successes 1: #infected at success 2: #infected nodes 
     * 3: generation of success 4: highest generation 5: starting neighbors
     * 6: average uninfected neighbors 7: active nodes before success
     * 8: active nodes after success 9: average alpha before success
     * 10: average alpha after success
     * ArrayList.get(1): contains failure Case data
     * What is stored in the failureCase:
     * Flood/Gossip (index 0-2)
     * 0: #failures  1: #infected nodes 2: highest generation 3: starting #neighbors 
     * 4: average #neighbors
     * Infect Model (index 3)
     * 0: #failures 1: #infected nodes 2: highest generation 3: starting #neighbors
     * 4: average uninfected #neighbors 5: active nodes 6: average alpha
     * @author Brian Spain
     */
    
    private ArrayList extractSimData(){
        
        double[][] failureCase = new double[4][];
        failureCase[0] = new double[5]; //flood
        failureCase[1] = new double[5]; //gossip80
        failureCase[2] = new double[5]; //gossip60
        failureCase[3] = new double[7]; //infect model
        /*
        What is stored in the failureCase:
        
        Flood/Gossip
        0: #failures  1: #infected nodes 2: highest generation 3: starting #neighbors 
        4: average #neighbors
        
        Infect Model
        0: #failures 1: #infected nodes 2: highest generation 3: starting #neighbors 
        4: average uninfected #neighbors 5: active nodes 6: average alpha
        */
        double[][] successCase = new double[4][];
        successCase[0] = new double[10]; //flood
        successCase[1] = new double[10]; //gossip80
        successCase[2] = new double[10]; //gossip60
        successCase[3] = new double[12]; //infect model
        /*
        What is stored in the successCase:
        
        Flood/Gossip (index 0-2)
        0: #successes
        1: #infected at success 
        2: #infected nodes 
        3: generation of success 
        4: highest generation 
        5: starting neighbors
        6: average neighbors 
        7: active nodes before destination 
        8: active nodes after destination 
        9: Total Messages Sent
        
        Infect Model (index 3)
        0: #successes 
        1: #infected at success 
        2: #infected nodes 
        3: generation of success 
        4: highest generation 
        5: starting neighbors
        6: average uninfected neighbors 
        7: active nodes before destination 
        8: active nodes after destination 
        9: Total Messages Sent 
        10: average alpha before destination 
        11: average alpha after destination 
        */
        
        double[][] graphData = new double[4][5];
        /*
        What is stored in graphData:
        
        The following data points for the cases in which all 4 methods succeeded.
        
        0: # Non-leaf nodes
        1: # Total nodes
        2: # Highest # of children on one parent
        3: # Lowest # of children on one parent
        4: Total messages sent
        */
        
        for(double[] data : graphData){
            data[0] = 0;
            data[1] = 0;
            data[2] = 0;
            data[3] = Integer.MAX_VALUE;
            data[4] = 0;
        }
        
        int[] successGen = new int[4];
        
        boolean allSuccess = true;
        for(int messageType = 0; messageType < success.length; messageType++){
            if(success[messageType]){
                successGen[messageType] = Nodes.get(reciever).getGeneration(messageType);
                successCase[messageType][0]++;
                successCase[messageType][3] = Nodes.get(reciever).getGeneration(messageType);
                successCase[messageType][5] = Nodes.get(sender).getNeighbors(messageType);
            }
            else{
                allSuccess = false;
                successGen[messageType] = -1;
                failureCase[messageType][0]++;
                failureCase[messageType][3] = Nodes.get(sender).getNeighbors(messageType);
            }
        }
        
        for(Node node : Nodes){
            for(int messageType = 0; messageType < 4; messageType++){
                if(node.isInfected(messageType)){
                    if(successGen[messageType] == -1){
                        failureCase[messageType][1]++;
                        if(messageType == 3){
                            if(node.getGeneration(messageType) > failureCase[messageType][2])
                                failureCase[messageType][2] = node.getGeneration(messageType);
                            if(node.getUninfectedNeighbors(messageType) > 0){
                                failureCase[messageType][4] += node.getUninfectedNeighbors(messageType);
                                failureCase[messageType][5]++;
                                failureCase[messageType][6] += node.getAlpha();
                            }
                        }
                        else{
                            failureCase[messageType][1]++;
                            failureCase[messageType][4] += node.getNeighbors(messageType);
                            if(failureCase[messageType][2] < node.getGeneration(messageType))
                                failureCase[messageType][2] = node.getGeneration(messageType);  
                        }
                    }
                    else{
                        successCase[messageType][2]++;
                        if(messageType == 3){
                            if(node.getGeneration(messageType) >= successGen[messageType]){
                                if(node.getChildren(messageType) > 0){ //active node after destination
                                    successCase[messageType][8]++;
                                    successCase[messageType][11] += node.getAlpha();                                    
                                }
                                if(node.getGeneration(messageType) > successCase[messageType][4])
                                    successCase[messageType][4] = node.getGeneration(messageType);
                            }
                            else{
                                successCase[messageType][1]++;
                                if(node.getChildren(messageType) > 0){ //active node before destination
                                    successCase[messageType][7]++;
                                    successCase[messageType][10] += node.getAlpha();                                   
                                }
                            }
                            successCase[messageType][6] += node.getNeighbors(messageType);
                            successCase[messageType][9] += node.totalMessages(messageType);
                        }
                        else {
                            if(node.getGeneration(messageType) >= successGen[messageType]){
                                if(node.getChildren(messageType) > 0){ //active node after destination
                                    successCase[messageType][8]++;                                  
                                }
                                if(node.getGeneration(messageType) > successCase[messageType][4])
                                    successCase[messageType][4] = node.getGeneration(messageType);
                            }
                            else{
                                successCase[messageType][1]++;
                                if(node.getChildren(messageType) > 0){ //active node before destination
                                    successCase[messageType][7]++;                                    
                                }
                            }
                            successCase[messageType][6] += node.getNeighbors(messageType);
                            successCase[messageType][9] += node.totalMessages(messageType);
                        }
                    }//end success data extract loop
                    if(allSuccess){
                        int children = node.getChildren(messageType);
                        if(children > 0){
                            ++graphData[messageType][0];
                            ++graphData[messageType][1];
                            if(children > graphData[messageType][2]){
                                graphData[messageType][2] = children;
                            }
                            if(children < graphData[messageType][3]){
                                graphData[messageType][3] = children;
                            }
                        }else if(children == 0){
                            ++graphData[messageType][1];
                        }
                    }
                }
            }
        }
        
        if(allSuccess){
            for(int messageType = 0; messageType < 4; ++messageType){
                graphData[messageType][4] = successCase[messageType][9];
            }
        }
        
        
        /*
            Calculate average neighbors.
        */
        
        for(int messageType = 0; messageType < 4; messageType++){
            if(success[messageType]){
                if(messageType == 3){
                    successCase[messageType][6] /= (successCase[messageType][7] + 
                            successCase[messageType][8]);
                }
                else
                    successCase[messageType][6] /= successCase[messageType][2];
            }
            else{
                if(messageType == 3){
                    failureCase[messageType][4] /= failureCase[messageType][5];
                }
                else
                failureCase[messageType][4] /= failureCase[messageType][1];
            }
        }
        
        /*
            Calculate average alphas.
        */
        if(success[3]){
            successCase[3][10] /= successCase[3][7];
            successCase[3][10] =((1000*successCase[3][10]))/1000.0;
            successCase[3][11] /= successCase[3][8];
            successCase[3][11] =((1000*successCase[3][11]))/1000.0;
        }
        else{
            failureCase[3][6] /= failureCase[3][5];
            failureCase[3][6] =((int)(1000*failureCase[3][6]))/1000.0;
        }
        
        ArrayList<double[][]> extractedData = new ArrayList<>();

        extractedData.add(successCase);
        extractedData.add(failureCase);
        extractedData.add(graphData);
        
        return extractedData;
    }
}
