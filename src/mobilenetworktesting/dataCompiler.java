
package mobilenetworktesting;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @description dataCompiler class intended for mobile network simulation 
 * research at HPU. This class is used to maintain, compile, and display the
 * data from a batch of simulations.
 * @author Brian Spain
 */

public class dataCompiler{
    
    private final double[][] failureCases;
    private final double[][] successCases;
    private final double[][] graphData;
    private final long[][] failureCasesSquares;
    private final long[][] successCasesSquares;
    private final long[][] graphDataSquares;
    private final double[][] failureCasesSD;
    private final double[][] successCasesSD;
    private final double[][] graphDataSD;
    private int graphCases;
    
    
    public dataCompiler(){
        
        failureCases = new double[4][];
        failureCases[0] = new double[5]; //flood
        failureCases[1] = new double[5]; //gossip80
        failureCases[2] = new double[5]; //gossip60
        failureCases[3] = new double[7]; //infect model
        
        successCases = new double[4][];
        successCases[0] = new double[10]; //flood
        successCases[1] = new double[10]; //gossip80
        successCases[2] = new double[10]; //gossip60
        successCases[3] = new double[12]; //infect model
        
        graphData = new double[4][5];
        
        failureCasesSquares = new long[4][];
        failureCasesSquares[0] = new long[5]; //flood
        failureCasesSquares[1] = new long[5]; //gossip80
        failureCasesSquares[2] = new long[5]; //gossip60
        failureCasesSquares[3] = new long[7]; //infect model
        
        successCasesSquares = new long[4][];
        successCasesSquares[0] = new long[10]; //flood
        successCasesSquares[1] = new long[10]; //gossip80
        successCasesSquares[2] = new long[10]; //gossip60
        successCasesSquares[3] = new long[12]; //infect model
        
        graphDataSquares = new long[4][5];
        
        failureCasesSD = new double[4][];
        failureCasesSD[0] = new double[5]; //flood
        failureCasesSD[1] = new double[5]; //gossip80
        failureCasesSD[2] = new double[5]; //gossip60
        failureCasesSD[3] = new double[7]; //infect model
        
        successCasesSD = new double[4][];
        successCasesSD[0] = new double[10]; //flood
        successCasesSD[1] = new double[10]; //gossip80
        successCasesSD[2] = new double[10]; //gossip60
        successCasesSD[3] = new double[12]; //infect model
        
        graphDataSD = new double[4][5];
    }
    
    /**
     * @description adds the data points form a single simulation to the 
     * data set
     * @param simData  data points from a single simulation
     */
    
    public void addData(ArrayList<double[][]> simData){
        for(int messageType = 0; messageType < 4; messageType++){
            for(int i = 0; i < simData.get(0)[messageType].length; i++){
                successCases[messageType][i] += simData.get(0)[messageType][i];
                successCasesSquares[messageType][i] += (long)Math.pow(simData.get(0)[messageType][i], 2);                  
            } 
        }

        for(int messageType = 0; messageType < 4; messageType++){
            for(int i = 0; i < simData.get(1)[messageType].length; i++){
                failureCases[messageType][i] += simData.get(1)[messageType][i];
                failureCasesSquares[messageType][i] += (long)Math.pow(simData.get(1)[messageType][i], 2);
            }
        }
        if(simData.get(2)[0][0] > 0){
            ++graphCases;
            for(int messageType = 0; messageType < 4; messageType++){
                for(int i = 0; i < simData.get(2)[messageType].length; i++){
                    graphData[messageType][i] += simData.get(2)[messageType][i];
                    graphDataSquares[messageType][i] += (long)Math.pow(simData.get(2)[messageType][i], 2);                  
                } 
            }
        }
    }
    
    /**
     * @desctiption averages totals where necessary and calculates standard 
     * deviation
     */
    
    public void compile(){
        for(int messageType = 0; messageType < 4; messageType++){
            for(int i = 1; i < successCases[messageType].length; i++){
                successCases[messageType][i] /= successCases[messageType][0];
                successCasesSquares[messageType][i] /= successCases[messageType][0];
                if(!Double.isNaN(successCases[messageType][i])){
                    BigDecimal b1 = new BigDecimal(""+successCasesSquares[messageType][i]);
                    BigDecimal b2 = new BigDecimal(""+successCases[messageType][i]);             
                    successCasesSD[messageType][i] = Math.sqrt(b1.subtract(b2.pow(2)).doubleValue());
                }
            }
        }
        
        for(int messageType = 0; messageType < 4; messageType++){
            for(int i = 1; i < failureCases[messageType].length; i++){
                failureCases[messageType][i] /= failureCases[messageType][0];
                failureCasesSquares[messageType][i] /= failureCases[messageType][0];
                if(!Double.isNaN(failureCases[messageType][i])){
                    BigDecimal b1 = new BigDecimal(""+failureCasesSquares[messageType][i]);
                    BigDecimal b2 = new BigDecimal(""+failureCases[messageType][i]);             
                    failureCasesSD[messageType][i] = Math.sqrt(b1.subtract(b2.pow(2)).doubleValue());
                }
            }
        }
        
        for(int messageType = 0; messageType < 4; messageType++){
            for(int i = 0; i < graphData[messageType].length; ++i){
                graphData[messageType][i]/=graphCases;
                graphDataSquares[messageType][i] /= graphCases;
                if(!Double.isNaN(graphData[messageType][i])){
                    BigDecimal b1 = new BigDecimal(""+graphDataSquares[messageType][i]);
                    BigDecimal b2 = new BigDecimal(""+graphData[messageType][i]);             
                    graphDataSD[messageType][i] = Math.sqrt(b1.subtract(b2.pow(2)).doubleValue());
                }
            }
        }       
    }
    
    /**
     * @description prints data to standard out, truncating decimal values to
     * two decimal places
     */
    
    public void print() {
        DecimalFormat truncator = new DecimalFormat("#.##");
        for(int messageType = 0; messageType < 4; messageType++){
            switch(messageType){
                case 0: System.out.print("Flood---- ");
                break;
                case 1: System.out.print("Gossip80- ");
                break;
                case 2: System.out.print("Gossip60- ");
                break;
                case 3: System.out.print("Infection ");
                break;
            }
            for(int i = 0; i < successCases[0].length; i++){
                if(i == 0) System.out.print(truncator.format(successCases[messageType][i]) + " //  ");
                else
                    System.out.print(truncator.format(successCases[messageType][i]) + " SD: " + 
                            truncator.format(successCasesSD[messageType][i]) + " //  ");
                if(i == successCases[0].length-1)
                    System.out.print("   ");
            }
//            for(int i = 0; i < failureCases[0].length; i++){
//                if(i == 0) System.out.print((int)failureCases[messageType][i] + " ");
//                else
//                    System.out.print((int)failureCases[messageType][i] + " SD: " + 
//                            truncator.format(failureCasesSD[messageType][i]) + "  ");
//            }
            if(messageType == 3){
                System.out.println("\nAlpha Data:  " + 
                        truncator.format(successCases[messageType][7]) + " SD: " +
                        truncator.format(successCasesSD[messageType][7]) + " //  " +
                        truncator.format(successCases[messageType][8]) + " SD: " +
                        truncator.format(successCasesSD[messageType][8]) + " //  " +
                        truncator.format(successCases[messageType][10]) + "  SD:  " +
                        truncator.format(successCasesSD[messageType][10]) + " //  " +
                        truncator.format(successCases[messageType][11]) + "  SD: " +
                        truncator.format(successCasesSD[messageType][11]));
                        //(int)failureCases[messageType][5] + "   " +
                        //truncator.format(failureCases[messageType][6]));
            }
            System.out.println();
        }
        System.out.println("Graph Data:");
        for(int messageType = 0; messageType < 4; messageType++){
            switch(messageType){
                case 0: System.out.print("Flood---- ");
                break;
                case 1: System.out.print("Gossip80- ");
                break;
                case 2: System.out.print("Gossip60- ");
                break;
                case 3: System.out.print("Infection ");
                break;
            }
            System.out.print(truncator.format(graphData[messageType][0]) + " SD: " + truncator.format(graphDataSD[messageType][0])
            + " // " + truncator.format(graphData[messageType][1]) + " SD: " + truncator.format(graphDataSD[messageType][1])
            + " // " + truncator.format(graphData[messageType][2]) + " SD: " + truncator.format(graphDataSD[messageType][2])
            + " // " + truncator.format(graphData[messageType][3]) + " SD: " + truncator.format(graphDataSD[messageType][3])
            + " // " + truncator.format(graphData[messageType][4]) + " SD: " + truncator.format(graphDataSD[messageType][4]));
            System.out.println();
        }
        System.out.println();
    }
    
    /**
     * @description takes in the parameters of the simulation to be used in the
     * name of the file. The file is output the same directory that the method is contained in.
     * @param length max y of the domain
     * @param width max x of the domain
     * @param population number of Nodes in the simulation
     * @param maxBuffer maximum buffer value for any node
     * @param transmitTime total time to transmit the message
     * @param iteration iteration of the simulation
     * @param beta beta value used for the simulation
     * @param recordData contains each data string for reconstruction that will
     * be printed to the file
     * @throws IOException if the file is not created properly and cannot be
     * found by the FileOutputStream
     */
    
    public static void rawDataToFile(int length, int width, int population, int maxBuffer, int transmitTime, 
            int iteration, double beta, ArrayList<String> recordData) throws IOException{
        String fileName = "L_" + length + "_W_" + width + "_P_" + population + 
                "_MB_" + maxBuffer + "_TT_" + transmitTime + "_SIMNUM_" + 
                iteration + "_B_" + beta + ".txt";
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(fileName), "utf-8"))) {
            writer.write("Key: \n" +  "t: node change direction (node id, x, y, new direction)" + "\n"
                                +   "s: node start location (node id, x, y, start direction, buffer)" + "\n"
                                +   "m: message transfer (sender id, target id, messageType, sender x, sender y" + "\n"
                                +   "b: broadcast (node id, message type, x, y)" + "\n");
            for(int i = 0; i < recordData.size(); i++){
                writer.write(recordData.get(i) + "\n");
            }
        }
    }
}

