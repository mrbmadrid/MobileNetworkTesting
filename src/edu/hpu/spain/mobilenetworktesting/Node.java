
package edu.hpu.spain.mobilenetworktesting;

import java.util.ArrayList;
import java.util.Random;

/**
 * @description Node intended for mobile network simulation research at
 * HPU. This node acts as a mobile network hub such as a drone or car moving
 * around in a 2D space.
 * @author Brian Spain
 */

public class Node {
    private final int id, buffer, transmitSpeed, range, rangeDecayThreshold,
            domainWidth, domainHeight;
    private double x, y, k, k2, r, d, dp, alphap, alpha;
    private int direction;
    private final int[][] transmitionData;
    private final boolean useFakeDestination;
    private final boolean[] fakeDestination;
    private final ArrayList<ArrayList<Integer>> eligibleNeighbors;
    private final ArrayList<ArrayList<Integer>> neighbors;  
    private final ArrayList<ArrayList<Integer>> uninfectedNeighbors;
    /**
     * @param id each node has an id in the environment, which corresponds to the 
     * order in which it was created.
     * @param domainWidth maximum distance the node can travel in the x direction
     * @param domainHeight maximum distance the node can travel in the y direction
     * @param x starting x position
     * @param y starting y position
     * @param direction starting direction (degrees)
     * @param buffer the time units a message sits at the Node before the Node
     * "processes" the message and takes its first picture.
     * @param transmitSpeed the amount of time it takes to transmit the message.
     * Functionally this is the amount of time used in the isEligible() method to 
     * calculate whether a target node will stay within the range of the sending
     * node long enough to receive an entire message.
     * @param range the transmit range of the node
     * @param rangeDecayThreshold
     * @param useFakeDestination
     * 
     */

       
    public Node(int id, int domainWidth, int domainHeight, double x, double y, 
            int direction, int buffer, int transmitSpeed, int range, 
            int rangeDecayThreshold, boolean useFakeDestination){
        
        this.id = id;                                                           
        this.domainWidth = domainWidth;
        this.domainHeight = domainHeight;
        this.x = x;                                                             
        this.y = y;                                                             
        this.direction = direction;                                             
        this.range = range;
        this.rangeDecayThreshold = rangeDecayThreshold;
        this.buffer = buffer;                                                   
        this.transmitSpeed = transmitSpeed;
        this.useFakeDestination = useFakeDestination;
        transmitionData = new int[4][4]; //see below
        fakeDestination = new boolean[4]; //used for message termination 
        eligibleNeighbors = new ArrayList<>(); //stores neighbors which will stay in range to recieve message from first picture
        neighbors = new ArrayList<>(); //stores first picture
        uninfectedNeighbors = new ArrayList<>(); //stores uninfected neighbors for calculating alpha
        for(int i = 0; i < 4; i++){ //each message type has its own first picture
            eligibleNeighbors.add(new ArrayList<>());
            neighbors.add(new ArrayList<>()); 
            uninfectedNeighbors.add(new ArrayList<>());
        }
        
        /*
            transmitionData[0][X] -> =-1 : not recieved >-1 : recieved;
            transmitionData[1][X] -> =-1 : no message >0 : Generation of message
            transmitionData[2][X] -> =-1 : has not broadcasted 0 : Broadcasted
                                    <transmitspeed : transmitting
                                    =transmitspeed : first picture
                                    >transmitspeed : counting down buffer
            transmitionData[3][X] -> # Children
                                    -1 : Not connected
                                     0 : Leaf
                                     >0 : # of child nodes
        
            transmitionData[][0] -> Flood
            transmitionData[][1] -> Gossip80
            transmitionData[][2] -> Gossip60
            transmitionData[][3] -> Infect
        
            the following sets initial values of transmitData to -1
            (no messages recieved no messages broadcasted)
        */
        
        for (int[] data : transmitionData) {
            for (int j = 0; j < data.length; j++) {
                data[j] = -1;
            }
        }
        
        alpha = -1;
    }
    
    /**
     * @description sets the transmitData values of the originating node
     * so that every message type will broadcast at time == 1;
     * 
     */
        
    
    public void setSender(){
    
        for(int i = 0; i<4; i++){
            transmitionData[0][i]=0;
            transmitionData[1][i]=1;
            transmitionData[2][i]=transmitSpeed + 1;
            transmitionData[3][i]=0;
        }
        alpha  = 1.0;
    }
    
/**
 * @description Updates node position and recalculates direction if Node reaches
 * domain edge. Decrements broadcast countdown if value is above 0. 
 * @return broadcast returns an array with an integer value for every message
 * type. At countdown = transmitionSpeed, set to 1 -> denotes time
 * to check eligible neighbors (this node has finished receiving message).  
 * At countdown = 0; set to 2 -> denotes time to broadcast. This might seem 
 * counter intuitive, however the simulation has already calculated which
 * nodes will stay within range to receive the message, so the time from
 * countdown == transmitionSpeed to countdown == 0 simulates the time it takes
 * to actually transfer the message. 
 * Otherwise set 0. (countdown = transmitionData[2][i])
 * 
**/
    
    public int[] updateNode(){
        
        int[] broadcast= new int[4];
        
        for(int i = 0; i < 4; i++){
            if(transmitionData[2][i]>=0){
                if(transmitionData[2][i]==transmitSpeed){
                    broadcast[i]=1;
                    if(transmitSpeed == 0)
                        broadcast[i]=2;
                }
                else if(transmitionData[2][i]==0){
                    broadcast[i]=2;
                }     
            }
            else broadcast[i]=0;
            transmitionData[2][i]--;
        }
        
        Random compass = new Random();
        
        if(x>=(domainWidth-1)){
            direction = compass.nextInt(180) + 90;
            --x;
        }
        else if(x<=(1)){
            direction = (compass.nextInt(180)+270)%360;
            ++x;
        }
        else if(y>=(domainHeight-1)){
            direction = compass.nextInt(180) + 180;
            --y;
        }
        else if(y<=(1)){
            direction = compass.nextInt(180);
            ++y;
        }
        x = x + (Math.cos(Math.toRadians(direction)));
        y = y + (Math.sin(Math.toRadians(direction)));
        return broadcast;
    }
    
    /**
     * @description same as updateNode() except this keeps track of the raw data
     * for reconstruction. If a node changes direction a new line is added to
     * dataRecord
     * @param dataRecord where Strings of data are stored for reconstruction
     * @return see updateNode()
     * 
     */
    
    public int[] updateNode(ArrayList<String> dataRecord){
        
        int[] broadcast= new int[4];
        for(int i = 0; i < 4; i++){
            if(transmitionData[2][i]>=0){
                if(transmitionData[2][i]==transmitSpeed){
                    broadcast[i]=1;
                    if(transmitSpeed == 0)
                        broadcast[i]=2;
                }
                else if(transmitionData[2][i]==0){
                    broadcast[i]=2;
                }     
            }
            else broadcast[i]=0;
            transmitionData[2][i]--;
        }
        

        Random compass = new Random();
        
        if(x>=(domainWidth-1)){
            direction = compass.nextInt(180) + 90;
            --x;
            dataRecord.add("t" + id + " " + x + " " + y + " " + direction);
        }
        else if(x<=(1)){
            direction = compass.nextInt(180)+270;
            ++x;
            dataRecord.add("t" + id + " " + x + " " + y + " " + direction);
        }
        else if(y>=(domainHeight-1)){
            direction = compass.nextInt(180) + 180;
            --y;
            dataRecord.add("t" + id + " " + x + " " + y + " " + direction);
        }
        else if(y<=(1)){
            direction = compass.nextInt(180);
            ++y;
            dataRecord.add("t" + id + " " + x + " " + y + " " + direction);
        }
        x = x + (Math.cos(Math.toRadians(direction)));
        y = y + (Math.sin(Math.toRadians(direction)));
        return broadcast;
    }
    
    public double getx(){
        return x;
    }
    public double gety(){
        return y;
    }
    
    public int getID(){
        return id;
    }
    
    /**
     * Returns the number of children this node has.
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @return -1 not part of graph, 0 leaf, >0 number of children
     */
    
    public int getChildren(int messageType){
        return transmitionData[3][messageType];
    }
    
    /**
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @return generation this node was infected with give message type
     */
    
    public int getGeneration(int messageType){
        return transmitionData[1][messageType];
    }
    
    public int getTransmitSpeed(){
        return transmitSpeed;
    }
    
    /**
     * @return alpha rounded to three decimal places.
     */
    
    public double getAlpha(){
        return (double)Math.abs((int)(1000*alpha))/1000.0;
    }
    
    /**
     * @return total time between message reception and message broadcast
     */
    
    public int getTimeDelay(){
        return (buffer + transmitSpeed);
    }
    
    /**
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @return # of neighbors at first picture.
     */
    
    public int getNeighbors(int messageType){
        return neighbors.get(messageType).size();
    }
    
    /**
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @return # of uninfected neighbors used in alpha calculation.
     */
    
    public int getUninfectedNeighbors(int messageType){
        return uninfectedNeighbors.get(messageType).size();
    }
    
    /**
     * @description records starting position of the node in dataRecord
     * @param dataRecord where Strings of data are stored for reconstruction
     */
    
    public void recordLocation(ArrayList<String> dataRecord){
        dataRecord.add("s " + id + " " + (int)x + " " + (int)y + " " + direction + " " + 
                buffer);
    }
    
    /**
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @return true if calling node is infected with message type
     */
    
    public boolean isInfected(int messageType){
        return transmitionData[0][messageType]>=0;
    }
    
    /**
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @return number of times calling node received the given message type
     */
    
    public int totalMessages(int messageType){
        return (transmitionData[0][messageType]+1);
    }
    
    /**
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @return true if calling node is a fake destination
     */
    
    private boolean isFakeDestination(int messageType){
        if(!useFakeDestination) return false;
        else return fakeDestination[messageType];
    }
    
    /**
     * @param target node check for within range
     * @return true if in range false if out of range
     */
    
    private boolean inRange(Node target){
        double rangeToTarget = getRange(target);
        return range>rangeToTarget;
    }
    
    /** 
     * @param target node to get range to
     * @return range to target node
     */
    
    private double getRange(Node target){
        return Math.sqrt( Math.pow((this.getx() - target.getx()), 2)+
                        Math.pow((this.gety() - target.gety()), 2));
    }
    
    /**
     * To implement remove the if(true) block
     * @param target node to get rangeDecayFactor from
     * @return a value between 0.0 and 1.0 denoting chances of success of
     * message transfer based on a decay in connectivity due to range function
     */
    
    private double rangeDecayFactor(Node target){
        if(true){
            return 1.0;
        }
        double rangeToTarget = getRange(target);
        if(rangeToTarget < rangeDecayThreshold) return 1;
        else {
            //return -(rangeToTarget/(double)range) + 1.0 + (double)rangeDecayThreshold/(double)range;
            double rangeRatio=(double)rangeDecayThreshold/(double)range;
            return -(rangeToTarget/((double)range*(1.0-rangeRatio)))+1.0/(1.0-rangeRatio);
        }    
    }
        
    /**
     * @description checks location of the both calling and target nodes after
     * time = transmitSpeed has elapsed.
     * @param target node to attempt to send message to
     * @return true if the node will be in range long enough to receive the
     * entire message, false otherwise
     * 
     */
    
    private boolean iseligible(Node target){
        if(inRange(target)){
            double targetx = target.x + transmitSpeed*Math.cos(target.direction);   
            double targety = target.y + transmitSpeed*Math.sin(target.direction);
            double senderx = x + transmitSpeed*Math.cos(direction);
            double sendery = y + transmitSpeed*Math.sin(direction);

            double rangeToTarget = Math.sqrt( Math.pow((senderx - targetx), 2)+                 
                        Math.pow((sendery - targety), 2));
            return range>=rangeToTarget;
        }
        return false;
    }
    
    /**
      * @description Populates neighbors (first picture) and eligibleNeighbors. 
      * An eligible neighbor is a node which can be seen by the broadcasting 
      * node and will remain in range long enough to successfully receive the 
      * message.
      * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
      * @param nodes all nodes populating the environment.
      * 
    **/
    
    public void setEligibleNeighbors(int messageType, ArrayList<Node> nodes){
        
        for(int i = 0; i < nodes.size(); i++){
            if(inRange(nodes.get(i))){
                if(this != nodes.get(i)){
                    neighbors.get(messageType).add(i);
                }
                if(useFakeDestination){
                    for(int j = 0; j < fakeDestination.length; j++){
                        if(nodes.get(i).isFakeDestination(j)){
                            fakeDestination[j] = true;
                        }
                    }
                }
                if(iseligible(nodes.get(i))){
                    if(this != nodes.get(i))
                        eligibleNeighbors.get(messageType).add(i);
                }
            }
        }
        setUninfectedNeighbors(messageType, nodes);
    }
    
    /**
     * Sets the contents of uninfectedNeighbor ArrayList for the parameter
     * messageType
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @param nodes all nodes populating the environment.
     */
    
    public void setUninfectedNeighbors(int messageType, ArrayList<Node> nodes){
        for(int i = 0; i < neighbors.get(messageType).size(); i++){
            int node = neighbors.get(messageType).get(i);
            if(!nodes.get(node).isInfected(messageType))
                uninfectedNeighbors.get(messageType).add(node);
        }
    }
    
    /**
     * @description rangeSort is a selection sort algorithm that sorts ArrayList
     * of Node ID's based on the range of the referenced Node from the calling
     * node. ascending (farthest neighbor first)
     * @param Nodes all nodes populating the environment.
     * @param neighbors list of node ID's to be sorted
     * @return ArrayList of sorted Node ID's based on range from calling Node
     * 
    **/
    
    private void rangeSort(ArrayList<Node> nodes, ArrayList<Integer> list){
        int index = 0;
        for(int i = 0; i < list.size(); i++){
           int max = list.get(0);
           int maxIndex = 0;
           for(int j = index; j < list.size(); j++){
               if(getRange(nodes.get(list.get(j))) 
                       > getRange(nodes.get(list.get(maxIndex)))){
                   max = list.get(j);
                   maxIndex = j;
               }
           }
           list.remove(maxIndex);
           list.add(index++, max);
        }
    }
    
    /**
     * @description rangeSort is a selection sort algorithm that sorts ArrayList
     * of Node ID's based on the range of the referenced Node from the calling
     * node. descending. (nearest neighbor first)
     * @param Nodes all nodes populating the environment.
     * @param neighbors list of node ID's to be sorted
     * @return ArrayList of sorted Node ID's based on range from calling Node
     * 
    **/
    
    private void reverseRangeSort(ArrayList<Node> nodes, ArrayList<Integer> list){
        int index = 0;
        for(int i = 0; i < list.size(); i++){
           int min = list.get(0);
           int minIndex = 0;
           for(int j = index; j < list.size(); j++){
               if(getRange(nodes.get(list.get(j))) 
                       < getRange(nodes.get(list.get(minIndex)))){
                   min = list.get(j);
                   minIndex = j;
               }
           }
           list.remove(minIndex);
           list.add(index++, min);
        }
    }
    
    /**
     * @description rangeSort is a selection sort algorithm that sorts ArrayList
     * of Node ID's based on the absolute value of the diference between
     * transmition range and expected range from the calling node to paramater
     * node by the end of transmition. Essentially this sorts by the likelyhood
     * a target node will stay in range long enough to recieve the message.
     * @param nodes all nodes populating the environment.
     * @param list list of node ID's to be sorted
    **/
    
    public void sortByExpectedRange(ArrayList<Node> nodes, ArrayList<Integer> list){
        int index = 0;
        for(int i = 0; i < list.size(); i++){
           int min = list.get(0);
           int minIndex = 0;
           for(int j = index; j < list.size(); j++){
               if(Math.abs((double)range-(getRange(nodes.get(list.get(j)))+2*transmitSpeed))
                       < Math.abs((double)range-(getRange(nodes.get(list.get(minIndex)))+2*transmitSpeed))){
                   min = list.get(j);
                   minIndex = j;
               }
           }
           list.remove(minIndex);
           list.add(index++, min);
        }
    }
    
    /**
     * @description If the target node has not received the parameter message
     * type then infect sets broadcast countdown on target node  to 
     * (buffer + timeDelay) and sets generation on target node to calling 
     * node generation + 1. If target node has already received this message
     * type, target.transmitionData[0][messageType]++ increments the number of
     * attempted message transfers for parameter message type
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @param target node to send message to
     * 
     */
    
    private void infect(int messageType, Node target){
        if(Math.random() <= rangeDecayFactor(target)){
            //-1 indicates the node has not recieved the message
            if(target.transmitionData[0][messageType] == -1){
                //Set generation
                target.transmitionData[1][messageType] = 
                        (1+this.transmitionData[1][messageType]);
                //Set broadcast countdown
                target.transmitionData[2][messageType] = target.getTimeDelay();
                target.transmitionData[3][messageType]=0;
            }
            //Increment number of attempted message transfer to target
            target.transmitionData[0][messageType]++;
            ++transmitionData[3][messageType]; //increment children
        }
    }
    
    /**
     * @description adds a line to dataRecord for reconstruction recording the
     * message transfer, then calls the standard infect method
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @param target node to send message to
     * @param dataRecord where Strings of data are stored for reconstruction
     * 
     */
    
    private void infect(int messageType, Node target, ArrayList<String> dataRecord){
        dataRecord.add("m " + this.id + " " + target.id + " " + messageType + " " + (int)x + " " +(int)y);
        infect(messageType, target);
    }
    
    /**
     * @description (For Research Infection Model) If the target node has not 
     * been infected, the alpha, k, k^2 values of the calling node are
     * are transfered to the target node (necessary for target node calculation
     * of alpha). Then infect sets the broadcast countdown on target node to 
     * (buffer + timeDelay) and sets generation on target node to calling node 
     * generation + 1. If target node has already received this message type, 
     * the number of attempted message transfers for parameter message type.
     * @param target node to send message to
     * 
     */
    
    private void infect(Node target){
        if(target.transmitionData[0][3] == -1){                                 
            target.alphap  = alpha;                                             
            target.k = k;
            target.k2 = k2;
            target.transmitionData[1][3] = (1 + this.transmitionData[1][3]);    
            target.transmitionData[2][3] = target.getTimeDelay();
            target.dp = d;
            target.transmitionData[3][3]=0;
        }
        target.transmitionData[0][3]++; //increment to target transfer attempts
        ++transmitionData[3][3]; //increment children
    }
    
    /**
     * @description adds a line to dataRecord for reconstruction recording the 
     * message transfer, then calls the infect method for Research Infection 
     * Model 
     * @param target node to send message to
     * @param dataRecord where Strings of data are stored for reconstruction
     * 
     */
    
    private void infect(Node target, ArrayList<String> dataRecord){
        dataRecord.add("m " + this.id + " " + target.id + " " + 3 + " " + k + 
                " " + k2 + " " + alpha);
        infect(target);
    }

    /**
     * @description depending on the message type, this method executes the
     * corresponding message transfer protocol
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @param reciever id of the receiving node
     * @param Nodes all nodes populating the environment.
     * 
     */
    
    public void broadcast(int messageType, int reciever, ArrayList<Node> Nodes){
        if(this != Nodes.get(reciever) && !fakeDestination[messageType]){  
            
            /*
                random object will be used to calculate whether or not a
                gossip node will broadcast.
            */
            
            Random selector = new Random();                               
            switch (messageType){
                case 0:
                    if(iseligible(Nodes.get(reciever))){
                        infect(messageType, Nodes.get(reciever));                    
                        if(useFakeDestination)
                            fakeDestination[messageType] = true;
                    }
                    else{
                        for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                            int node = eligibleNeighbors.get(messageType).get(i);
                            if(uninfectedNeighbors.get(messageType).contains(node)){
                                infect(messageType, Nodes.get(node));                             
                            }
                        }
                    }
                    break;

                case 1:
                    if(transmitionData[1][1] == 1){
                       if(iseligible(Nodes.get(reciever))){
                            infect(messageType, Nodes.get(reciever));                         
                            if(useFakeDestination)
                                fakeDestination[messageType] = true;
                        }
                        else{
                            for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                int node = eligibleNeighbors.get(messageType).get(i);
                                infect(messageType, Nodes.get(node));                             
                            }
                        } 
                    }
                    else if(selector.nextInt(100)>=20){ //Gossip 80%
                        if(iseligible(Nodes.get(reciever))){
                            infect(messageType, Nodes.get(reciever));                         
                            if(useFakeDestination)
                                fakeDestination[messageType] = true;
                        }
                        else{
                            for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                int node = eligibleNeighbors.get(messageType).get(i);
                                if(uninfectedNeighbors.get(messageType).contains(node)){
                                    infect(messageType, Nodes.get(node));                               
                                }
                            }
                        }
                    }
                    break;
                case 2:
                    if(transmitionData[1][2] == 1){
                       if(iseligible(Nodes.get(reciever))){
                            infect(messageType, Nodes.get(reciever));                        
                            if(useFakeDestination)
                                fakeDestination[messageType] = true;
                        }
                        else{
                            for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                int node = eligibleNeighbors.get(messageType).get(i);
                                infect(messageType, Nodes.get(node));                            
                            }
                        } 
                    }
                    else if(selector.nextInt(100)>=40){
                        if(iseligible(Nodes.get(reciever))){
                            infect(messageType, Nodes.get(reciever));
                            if(useFakeDestination)
                                fakeDestination[messageType] = true;
                        }
                        else{
                            for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                int node = eligibleNeighbors.get(messageType).get(i);
                                if(uninfectedNeighbors.get(messageType).contains(node)){
                                    infect(messageType, Nodes.get(node));                                 
                                }
                            }
                        }
                    }
                    break;
                case 3:                                                         
                    if(iseligible(Nodes.get(reciever))){
                        infect(Nodes.get(reciever));                       
                        if(useFakeDestination)
                            fakeDestination[messageType] = true;
                    }

                    else{
                        d = uninfectedNeighbors.get(messageType).size();
                        
                        if(transmitionData[1][3] == 1){
                            k = uninfectedNeighbors.get(messageType).size(); //(k = degree of origin node)
                            k2 = Math.pow((uninfectedNeighbors.get(messageType).size()), 2);
                            //originating node infects all eligible neighbor nodes
                            for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                int node = eligibleNeighbors.get(messageType).get(i);
                                infect(Nodes.get(node));                              
                            }
                        }
                        else{
                            if(uninfectedNeighbors.get(messageType).size() > 0){
                                calculateAlpha();
                                if(alpha == 1){
                                    for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                        int node = eligibleNeighbors.get(messageType).get(i);
                                        if(uninfectedNeighbors.get(messageType).contains(node)){
                                            infect(Nodes.get(node));                                          
                                        }
                                    }
                                }
                                else{
                                    //sortByExpectedRange(Nodes, uninfectedNeighbors.get(messageType));
                                    rangeSort(Nodes, uninfectedNeighbors.get(messageType));
                                    double field = uninfectedNeighbors.get(messageType).size(); 
                                    for(int i = 0; (1.0 - alpha) <= (field/(double)uninfectedNeighbors.get(messageType).size()); i++){
                                        int node = uninfectedNeighbors.get(messageType).get(i);
                                        if(eligibleNeighbors.get(messageType).contains(node)){
                                            infect(Nodes.get(node));                                          
                                        }
                                        field--;
                                    }
                                }
                            }
                        }
                    }
                break;
            }
        }
    }
    
     /**
     * @description Stores reconstruction data for broadcast, otherwise this
     * method is the same as the standard broadcast method
     * @param messageType 0-3 indicating flood, gossip80, gossip60, or infect
     * @param reciever id of the receiving node
     * @param Nodes all nodes populating the environment.
     * @param dataRecord where Strings of data are stored for reconstruction
     * 
     */
    
    public void broadcast(int messageType, int reciever, ArrayList<Node> Nodes, 
            ArrayList<String> dataRecord){
        if(this != Nodes.get(reciever) && !fakeDestination[messageType]){
            Random selector = new Random();                                     //This is used for the Gossip Methods                                                              
            switch (messageType){
                case 0: //Flood infects all nearby eligible nodes
                    if(iseligible(Nodes.get(reciever))){
                        infect(messageType, Nodes.get(reciever), dataRecord);                       
                        if(useFakeDestination)
                            fakeDestination[messageType] = true;
                    }
                    else{
                        for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                            int node = eligibleNeighbors.get(messageType).get(i);
                            if(uninfectedNeighbors.get(messageType).contains(node)){
                                infect(messageType, Nodes.get(node), dataRecord);                             
                            }
                        }
                    }
                    break;

                case 1:
                    if(transmitionData[1][1] == 1){
                       if(iseligible(Nodes.get(reciever))){
                            infect(messageType, Nodes.get(reciever), dataRecord);                         
                            if(useFakeDestination)    
                                fakeDestination[messageType] = true;
                        }
                        else{
                            for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                int node = eligibleNeighbors.get(messageType).get(i);
                                infect(messageType, Nodes.get(node), dataRecord);                              
                            }
                        } 
                    }
                    else if(transmitionData[1][1] != 1 && selector.nextInt(100)>=20){//Gossip 80%
                        if(iseligible(Nodes.get(reciever))){
                            infect(messageType, Nodes.get(reciever), dataRecord);                         
                            if(useFakeDestination)
                                fakeDestination[messageType] = true;
                        }
                        else{
                            for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                int node = eligibleNeighbors.get(messageType).get(i);
                                if(uninfectedNeighbors.get(messageType).contains(node)){
                                    infect(messageType, Nodes.get(node), dataRecord);                                  
                                }
                            }
                        }
                    }
                    break;
                case 2:
                    if(transmitionData[1][2] == 1){
                       if(iseligible(Nodes.get(reciever))){
                            infect(messageType, Nodes.get(reciever), dataRecord);                         
                            if(useFakeDestination)
                                fakeDestination[messageType] = true;
                        }
                        else{
                            for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                int node = eligibleNeighbors.get(messageType).get(i);
                                infect(messageType, Nodes.get(node), dataRecord);                             
                            }
                        } 
                    }
                    else if(selector.nextInt(100)>=40){
                        if(iseligible(Nodes.get(reciever))){
                            infect(messageType, Nodes.get(reciever), dataRecord);                          
                            if(useFakeDestination)
                                fakeDestination[messageType] = true;
                        }
                        else{
                            for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                int node = eligibleNeighbors.get(messageType).get(i);
                                if(uninfectedNeighbors.get(messageType).contains(node)){
                                    infect(messageType, Nodes.get(node), dataRecord);                                  
                                }
                            }
                        }
                    }
                    break;
                case 3:                                                         //Infect
                    if(iseligible(Nodes.get(reciever))){
                        infect(Nodes.get(reciever), dataRecord);                     
                        if(useFakeDestination)
                            fakeDestination[messageType] = true;
                    }

                    else{
                        d = uninfectedNeighbors.get(messageType).size();
                        
                        if(transmitionData[1][3] == 1){
                                k = uninfectedNeighbors.get(messageType).size(); //(k = degree of origin node)
                                k2 = (int)Math.pow((uninfectedNeighbors.get(messageType).size()), 2);

                                //originating node infects all eligible neighbor nodes
                                for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                    int node = eligibleNeighbors.get(messageType).get(i);
                                    infect(Nodes.get(node), dataRecord);                               
                                }
                            }
                            else{
                                if(uninfectedNeighbors.get(messageType).size() > 0){
                                    calculateAlpha();                               
                                    if(alpha == 1){
                                        for(int i = 0; i < eligibleNeighbors.get(messageType).size(); ++i){
                                            int node = eligibleNeighbors.get(messageType).get(i);
                                            if(uninfectedNeighbors.get(messageType).contains(node)){
                                                infect(Nodes.get(node), dataRecord);                                             
                                            }
                                        }
                                    }
                                    else{
                                        //sortByExpectedRange(Nodes, uninfectedNeighbors.get(messageType));
                                        rangeSort(Nodes, uninfectedNeighbors.get(messageType));
                                        double field = uninfectedNeighbors.get(messageType).size(); 
                                        for(int i = 0; (1.0 - alpha) <= (field/(double)uninfectedNeighbors.get(messageType).size()); i++){
                                            int node = uninfectedNeighbors.get(messageType).get(i);
                                            if(eligibleNeighbors.get(messageType).contains(node)){
                                                infect(Nodes.get(node), dataRecord);                                               
                                            }
                                            field--;
                                        }
                                    }
                                }
                            }
                        }
                break;
            }
        }
    }
    
    /**
     * @description calculates alpha for calling node based off alpha and r 
     * values of parent node along with generation and number of uninfected
     * neighbors of calling node, using the model defined by Dr. Hyunsun Lee 
     * at Hawaii Pacific University.
     * @param uninfectedNeighbors neighbors passed to calculate alpha
     * 
     */
    
    private void calculateAlpha(){
        
        /*
            k and k^2 initially have the values from the node that infected
            the calling node. This is so we can have the previous r value
            without passing it as a paremeter.
        */
        
        double rp = (k2-k)/k;                                                   
        double gen = transmitionData[1][3];
        
        /*
            The following formula's calculate k and k^2 based off the model
            designed by Dr. Hyunsun Lee @Hawaii Pacific University
        */

        k = k * gen / (gen + 1) + d / (gen +1);
        k2 = k2 * gen  / (gen + 1) + (d * d) / (gen +1);

        r = (k2-k)/k;

        double epsilon = (dp/d)*(r/rp);
        //System.out.println(epsilon);
        
        /*
            The previous alpha value has been passed to the 
            current node by the node that infected it. Now we
            calculate the current alpha based on epsilon and
            the previous alpha as per Dr. Hyunsun Lee's method.
        
            Beta is passed to the node from the simulation parameters and is a
            value between 1.0 and 2.0, current theoretical optimal value is 
            1.6-1.9.
        */

        if(epsilon <=(2.0-1/alphap )){
            alpha = 1;
        }
        else if(epsilon >=2.0){
            alpha = (1/d);
        }
        else{
            alpha = alphap *(2.0 - epsilon);
        }
       // System.out.println(alpha);
    }
}
