/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meanimputation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Sin@n K@pl@n
 * This class implements the mean imputation method
 * mean of each attribute is calculated from the observed data and then
 * each missing value is replaced mean of the related attribute.
 * For categorical data mode is used instead of mean
 */
public class MeanImputation {

    private int numofInstances;// number of instances
    private int numofAttributes;// number of attributes
    private double[] means;//means for each variable/feature
    private double[] stdeviations;// standart deviations for each variable/features
    private int[] modes;// modes of nominal attributes
    //private boolean[][] missing;// missing columns
    private double[][] covariances;
    public static double[][] data;
    private int[] totalObservedAttr;// hold total number of observed variable for each  feature
    private int[][] availableAttrIndex;//hold the index of the available attributes
    public static boolean[] isnumeric;
    public static String [] header;

    public MeanImputation() {

    }
    public void RunMean(String filename, String output) throws Exception{
        //String filename = "C:\\Users\\Sinan\\Desktop\\ArticleTest\\WholeData\\Gamma\\Wholesale customers data_30.csv";
        read_data(filename);
        initializeParameters();
        process();
        generateCSVFile(output);
    }

    private void initializeParameters() {
        //numofInstances = data.length;//get num of instances in the data set
        //numofAttributes = data[0].length;// get num of attributes each resord have
        System.out.println("attributes  " + data[0].length);
        means = new double[data[0].length];//initialize means matrix
        modes = new int[data[0].length];
        //isnumeric = new boolean[data[0].length];
        //covariances = new double[numofAttributes][numofAttributes];// initialize covariances matrix
        //stdeviations = new double[numofAttributes];
        totalObservedAttr = new int[data[0].length];
        for (int j = 0; j < data[0].length; j++) {

            means[j] = 0;
            modes[j] = 0;
            totalObservedAttr[j] = 0;

        }

    }

    /**
     * this method applies mean/mode imputation
     */
    public void process() {
        computeMeanAndMode();
        impute();
        System.out.println("After Imputation");
        for (int i = 0; i < data.length; i++) {
            System.out.println(i);
            for (int j = 0; j < data[0].length; j++) {
                System.out.print(data[i][j] + "\t");

            }
            System.out.println();
        }

    }

    /**
     * this method computes the mean of the attribute if it is numeric otherwise
     * computes the mode of the attribute which has categorical value
     */
    private void computeMeanAndMode() {
        for (int j = 0; j < data[0].length; j++) {
            computemean(j);
            /*if (isnumeric[j] == true) {
                computemean(j);
            } else {
                computemode(j);
            }*/
        }
    }

    public void computemean(int column) {
        for (double[] data1 : data) {
            if (data1[column] != 0) {
                means[column] = means[column] + data1[column];
                totalObservedAttr[column]++;
            }
        }
        means[column] = means[column] / totalObservedAttr[column]++;
        System.out.println("mean: " + means[column]);
    }

    private void computemode(int column) {
        int[] frequency;
        int max = (int) findmax(column);
        System.out.println(max);
        frequency = new int[max + 1];
        for (double[] data1 : data) {
            if (data1[column] != (-1.0)) {
                frequency[(int) data1[column]]++;
            }
        }
        System.out.println(frequency.length);
        int maxvalue = frequency[0];
        int index = 0;
        for (int i = 1; i < frequency.length; i++) {
            if (frequency[i] > maxvalue) {
                maxvalue = frequency[i];
                index = i;
            }
        }
        modes[column] = index;
        System.out.println("modes: " + index);
        //return value;
    }
    /**
     * fill missing values with mean of the attribute that is missing.
     */
    private void impute() {

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
               if (data[i][j] == 0) {
                        data[i][j] = means[j];

                    }
                /*if (isnumeric[j] == true) {
                    if (data[i][j] == 0) {
                        data[i][j] = means[j];

                    }
                }else{
                    if(data[i][j]==-1){
                      data[i][j] = modes[j];  
                    }
                }*/

            }
        }
    }

    /**
     * this method finds the maximum element of the given column
     *
     * @param column
     * @return max
     */
    private double findmax(int column) {
        double max = data[0][column];
        for (int i = 1; i < data.length; i++) {
            if (max < data[i][column]) {
                max = data[i][column];
            }
        }
        return max;
    }

    /**
     * read the data from the .csv file
     *
     * @param filename
     * @param data
     * @throws Exception
     */
    private void read_data(String filename) throws Exception {
        ArrayList<ArrayList<Double>> arr;
        /* String[] dims = inFile.readLine().split(",");
         if (dims.length != 2) {
         throw new Exception("Error: malformed dimensions line");
         }*/
        try (BufferedReader inFile = new BufferedReader(new FileReader(filename))) {
           header = inFile.readLine().split(",");
            /*if (dims.length != 2) {
             throw new Exception("Error: malformed dimensions line");
             }*/
           // isnumeric = new boolean[header.length];
            // decide if the attibute is numeric or not
            /*for (int i = 0; i < header.length; i++) {
                if (header[i].equals("numeric")) {
                    isnumeric[i] = true;
                } else {
                    isnumeric[i] = false;
                }
                System.out.print(header[i] + "\t");
            }*/

            System.out.println();
            arr = new ArrayList<>();
            //data = new double[Integer.parseInt(dims[0])][Integer.parseInt(dims[1])];

            String str;
            // int j = 0;
            while ((str = inFile.readLine()) != null) {
                String[] ins = str.split(",");
                ArrayList<Double> temp = new ArrayList<>();
                for (int i = 0; i < ins.length; i++) {
                   // if (isnumeric[i] == true) { //if data is numeric 
                        if (!"?".equals(ins[i])) {
                            temp.add(Double.parseDouble(ins[i]));
                            // data[j][i] = Double.parseDouble(ins[i]);
                        } else {
                            //data[j][i] = 0;
                            //0.0 indicates that this numerical value is missing
                            temp.add(0.0);
                        }
                   /* } else { //if data is categoric
                        if (!"?".equals(ins[i])) {
                            temp.add(Double.parseDouble(ins[i]));
                            // data[j][i] = Double.parseDouble(ins[i]);
                        } else {
                            //data[j][i] = 0;
                            //-1 indicates that this categorical value is missing
                            temp.add(-1.0);
                        }
                    }*/

                }
                arr.add(temp);
                // j++;
            }
            //System.out.println(arr.size());
            //System.out.println(data[0].length);
        }
        data = new double[arr.size()][arr.get(0).size()];

        // System.out.println(data.length);
        //System.out.println(data[0].length);
        for (int i = 0; i < data.length; i++) {
            // System.out.println(i);
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = arr.get(i).get(j);
                System.out.print(data[i][j] + "\t");
            }
            System.out.println();
        }
        arr = null;
        //System.out.println(arr.size());

    }

    /**
     * write data to the csv file
     */ 
     public void generateCSVFile(String filename) {
        try {
            try (FileWriter writer = new FileWriter(filename, true)) {
                for(int i=0;i<header.length-1;i++){
                   writer.append(String.valueOf(header[i]));
                        writer.append(','); 
                }
                 writer.append(String.valueOf(header[header.length-1]));
                                    writer.append('\n');

                for(int i=0;i<data.length;i++){
                    for(int j=0;j<data[0].length-1;j++){
                        //for normalization
                        //data[i][j]=((data[i][j])*(max[j]-min[j]))+min[j];
                        writer.append(String.valueOf(data[i][j]));
                        writer.append(',');
                    }
                    writer.append(String.valueOf(data[i][data[0].length-1]));
                    writer.append('\n');
                }
                
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String filename = "C:\\Users\\Sinan\\Desktop\\ArticleTest\\WholeData\\Gamma\\Wholesale customers data_10.csv";
        String output = "C:\\Users\\Sinan\\Desktop\\ArticleTest\\new_test_mymean_10_gamma_newwwww.csv";
        MeanImputation m = new MeanImputation();
        m.RunMean(filename,output);
    }

}
