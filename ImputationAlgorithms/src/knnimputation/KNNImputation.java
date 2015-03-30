/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knnimputation;

import Jama.Matrix;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 *
 * @author Sin@n K@pl@n
 * This class implements the KNN method to impute missing
 * values. K value is 11 but user has a chance to change this value at the
 * beginning of the imputation.
 *
 */
public class KNNImputation {

    public int k;
    public double[] min;
    public double[] max;
    private boolean[] ismissing;// to check the instance has any missing values or not
    //private double[][] covariances;
    public static double[][] data; // readed data from the file
    public static double[][] completedata;//partioned data that have all the instances w/o any missing values
    public boolean[] isnumeric;
    public static String[] header;

    /**
     * hold the distances b/w the record with missing values and all complete
     * data instances. First column indicates the distance and second column
     * indicates the index
     *
     */
    public double[][] distances;

    public KNNImputation() {

    }

    //get k neighbors
    public int getk() {
        return k;
    }

    // set k neighbors
    public void setk(int k) {
        this.k = k;
    }

    /**
     * This method initialize required parameters and set the starting values of
     * the parameters
     *
     */
    private void prepareData() {
        ArrayList<Integer> comp_rec_index = new ArrayList<>();// hold the index of complete record
        int tot_comp_rec = 0;//hold the total number of complete records in data matrix
        int k;
        //initialize missing array
        ismissing = new boolean[data.length];
        max = new double[data[0].length];
        min = new double[data[0].length];

        System.out.println(data.length);
        System.out.println(data[0].length);
        //normalize();
        // determine if i th instance has any missing value
        for (int i = 0; i < data.length; i++) {
            int j = 0;
            while (j < data[0].length && data[i][j] != 0) {
                j++;
            }
            //if i th instance has any missing values then
            //mark this instance as a missing
            if (j < data[0].length) {
                ismissing[i] = true;
            }
        }
        // determine size of the complete data array
        for (int i = 0; i < data.length; i++) {
            // if all columns of the record is observed then count it as a complete record
            if (ismissing[i] == false) {
                tot_comp_rec++;
                comp_rec_index.add(i);
                System.out.println("complete index:  " + i);
            }
        }

        //System.out.println(tot_comp_rec);
        //initialize complete data matrix to compute regression models
        completedata = new double[tot_comp_rec][data[0].length];
        distances = new double[tot_comp_rec][2];
        System.out.print(completedata[0].length);
        System.out.println("completedata");

        /*for(int i=0;i<completedata.length;i++){
         for(int j=0;j<completedata[0].length;j++){
         completedata[i][j]=data[comp_rec_index.get(i)][j];
         System.out.print(completedata[i][j]+"\t"); 
         }
         System.out.println(); 
         }*/
        for (int i = 0; i < completedata.length; i++) {
            System.arraycopy(data[comp_rec_index.get(i)], 0, completedata[i], 0, completedata[0].length);
            //System.out.print(completedata[i]+"\n");  
        }

        for (int i = 0; i < completedata.length; i++) {
            for (int j = 0; j < completedata[0].length; j++) {
                System.out.print(completedata[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * this methods firstly calculate distances b/w the instance with missing
     * values and each member of complete data. After that, it sorts the
     * distances matrix to get k-nearest neighbors. Finally it imputes the
     * missing value by using mean of k-nearest neighbors
     *
     * @param k number of neighbors
     */
    public void process(int k) {
        for (int i = 0; i < data.length; i++) {
            //if the instance i has any missing values then perform the k-NN
            if (ismissing[i] == true) {
                //calculate distance matrix
                System.out.println("Distances");
                calculatedistances(i);
                //sort distance matrix
                System.out.println("Sorted Distances");
                sortdistances();
                //impute each missing value of the i th record according to the 
                //mean of interested attribute of k-nearest neighbors
                impute(k, i);
            }
        }
    }

    /**
     * this method calculate distances b/w i th instance and all member of the
     * complete data matrix instances
     *
     * @param i
     */
    public void calculatedistances(int i) {
        double value;
        for (int m = 0; m < completedata.length; m++) {
            value = 0;
            for (int j = 0; j < completedata[0].length; j++) {

                if (data[i][j] != 0) {
                    //compute Euclidean Distance  --> sqrt[(X[i]*X[i]*Y[i]Y[i])]
                    value = value + ((data[i][j] - completedata[m][j]) * (data[i][j] - completedata[m][j]));
                }
                //if j is numeric  
                /*if (isnumeric[j] == true) {
                 //if j th attribute is not missing then start calculation
                 if (data[i][j] != 0) {
                 //compute Euclidean Distance  --> sqrt[(X[i]*X[i]*Y[i]Y[i])]
                 value = value + ((data[i][j] - completedata[m][j]) * (data[i][j] - completedata[m][j]));
                 }
                 } else { //if j is categoric
                 if (data[i][j] != -1) {
                 //compute Euclidean Distance  --> sqrt[(X[i]*X[i]*Y[i]Y[i])]
                 value = value + ((data[i][j] - completedata[m][j]) * (data[i][j] - completedata[m][j]));
                 }
                 }*/

            }
            System.out.println("value is:" + value);
            value = Math.sqrt(value);
            distances[m][0] = value;
            distances[m][1] = m;
            System.out.print(distances[m][1] + "\t");
            System.out.println(distances[m][0]);
        }
    }

    /**
     * this method sorts the distance matrix according to the first column which
     * indicates the distance b/w the record with missing values and the record
     * has no missing data
     */
    public void sortdistances() {
        //sort the array according to the first column
        Arrays.sort(distances, new ColumnComparator(0, SortingOrder.ASCENDING));
        for (int i = 0; i < distances.length; i++) {
            System.out.print(distances[i][1] + "\t");
            System.out.println(distances[i][0]);
        }
    }

    /**
     * this method imputes the missing j attribute of the i th instances by
     * taking mean of j th attribute in the k-nearest complete data matrix
     *
     * @param k number of neighbors
     * @param index index which has missing values
     */
    public void impute(int k, int index) {
        double value = 0;
        for (int j = 0; j < completedata[0].length; j++) {

            if (data[index][j] == 0) {
                value = 0;
                // take k-nearest neighbors
                for (int p = 0; p < k; p++) {
                    System.out.println("Nearest Neighbor " + p + " is: " + (int) distances[p][1]);
                    value = value + completedata[(int) distances[p][1]][j];
                }
                data[index][j] = value / k;
            }
            // tp perform algorithm also fr categorical values just activate this codes
          /*  if (data[index][j] == 0 && isnumeric[j] == true) {
             value = 0;
             // take k-nearest neighbors
             for (int p = 0; p < k; p++) {
             // System.out.println("Nearest Neighbor " + p + " is: " + (int) distances[p][1]);
             value = value + completedata[(int) distances[p][1]][j];
             }
             data[index][j] = value / k;
             } else {
             //categoric variables
             if (data[index][j] == -1) {
             data[index][j] = calculateMode(j);
             }
             }*/

        }
    }

    public int calculateMode(int j) {
        int index = 0;
        double max = completedata[(int) distances[0][1]][j];
        //find max elemnt to initialize frequency array
        for (int p = 1; p < k; p++) {
            //System.out.println("Nearest Neighbor " + p + " is: " + (int) distances[p][1]);
            //completedata[(int) distances[p][1]][j];
            if (completedata[(int) distances[p][1]][j] > max) {
                max = completedata[(int) distances[p][1]][j];
            }
        }
        int[] frequency;
        frequency = new int[(int) max + 1];
        //calculate frequency array for k-nearest neighbor
        for (int p = 0; p < k; p++) {
            frequency[(int) completedata[(int) distances[p][1]][j]]++;
            System.out.println("frequency for : " + (int) completedata[(int) distances[p][1]][j] + " is" + frequency[(int) completedata[(int) distances[p][1]][j]]);
        }

        int maxvalue = frequency[0];
        // find most frequent one in frequency array
        for (int p = 1; p < frequency.length; p++) {
            //System.out.println("Nearest Neighbor " + p + " is: " + (int) distances[p][1]);
            //completedata[(int) distances[p][1]][j];
            if (frequency[p] > maxvalue) {
                maxvalue = frequency[p];
                index = p;
                System.out.println("index is " + index);
            }
        }
        System.out.println("index is " + index);

        return index;
    }

    /**
     * enum for sortingg
     */
    enum SortingOrder {

        ASCENDING, DESCENDING;
    };

    /*
     * Utility Comparator class to sort multi dimensional array in Java
     */
    class ColumnComparator implements Comparator<double[]> {

        private final int iColumn;
        private final SortingOrder order;

        public ColumnComparator(int column, SortingOrder order) {
            this.iColumn = column;
            this.order = order;
        }

        @Override
        public int compare(double[] o1, double[] o2) {
            return Double.valueOf(o1[iColumn]).compareTo(o2[iColumn]);
        }
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

        try (BufferedReader inFile = new BufferedReader(new FileReader(filename))) {
            /* String[] dims = inFile.readLine().split(",");
             if (dims.length != 2) {
             throw new Exception("Error: malformed dimensions line");
             }*/

            header = inFile.readLine().split(",");
            /* isnumeric = new boolean[header.length];
             // decide if the attibute is numeric or not
             for (int i = 0; i < header.length; i++) {
             if (header[i].equals("numeric")) {
             isnumeric[i] = true;
             } else {
             isnumeric[i] = false;
             }
             System.out.print(header[i] + "\t");
             }*/
            arr = new ArrayList<>();
            //data = new double[Integer.parseInt(dims[0])][Integer.parseInt(dims[1])];

            String str;
            // int j = 0;
            while ((str = inFile.readLine()) != null) {
                String[] ins = str.split(",");
                ArrayList<Double> temp = new ArrayList<>();
                for (int i = 0; i < ins.length; i++) {
                    if (!"?".equals(ins[i])) {
                        temp.add(Double.parseDouble(ins[i]));
                        // data[j][i] = Double.parseDouble(ins[i]);
                    } else {
                        //data[j][i] = 0;
                        temp.add(0.0);
                    }

                    /*if (isnumeric[i] == true) { //if data is numeric 
                     if (!"?".equals(ins[i])) {
                     temp.add(Double.parseDouble(ins[i]));
                     // data[j][i] = Double.parseDouble(ins[i]);
                     } else {
                     //data[j][i] = 0;
                     //0.0 indicates that this numerical value is missing
                     temp.add(0.0);
                     }
                     } else { //if data is categoric
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
                // System.out.print(data[i][j]+"\t");
            }
            //System.out.println();
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
                for (int i = 0; i < header.length - 1; i++) {
                    writer.append(String.valueOf(header[i]));
                    writer.append(',');
                }
                writer.append(String.valueOf(header[header.length - 1]));
                writer.append('\n');

                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < data[0].length - 1; j++) {
                        //for normalization
                        //data[i][j]=((data[i][j])*(max[j]-min[j]))+min[j];
                        writer.append(String.valueOf(data[i][j]));
                        writer.append(',');
                    }
                    writer.append(String.valueOf(data[i][data[0].length - 1]));
                    writer.append('\n');
                }

                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void RunKNN(String filename, String output) throws Exception {
        boolean select_k;
        int k = 11;
        Scanner sc = new Scanner(System.in);
        System.out.println("Would you like to enter the k value[true/false]");
        select_k = sc.nextBoolean();
        System.out.println(select_k);
        if (select_k) {
            System.out.println("Enter the k value");

            k = sc.nextInt();
        }
        System.out.println("k value is: " + k);

        read_data(filename);
        prepareData();
        process(k);
        generateCSVFile(output);

    }

    public static void main(String[] args) throws Exception {

        String filename = "C:\\Users\\Sinan\\Desktop\\Tests\\Gamma\\DIM128_gamma_10mv.csv";
        String output = "C:\\Users\\Sinan\\Desktop\\test_em_10_gamma_newwwww.csv";
        KNNImputation knn = new KNNImputation();
        knn.RunKNN(filename, output);
        System.out.println("\nAfter Imputation");

        for (double[] data1 : data) {
            for (int t = 0; t < data[0].length; t++) {
                System.out.print(data1[t] + "\t");
            }
            System.out.print("\n");
        }

    }

    /**
     * perform normalization if necessary
     */
    public void normalize() {

        double[][] data1 = new double[data.length][data[0].length];
        for (int j = 0; j < data.length; j++) {
            for (int p = 0; p < data[0].length; p++) {

                data1[j][p] = data[j][p];
            }

        }
        int k;
        for (int j = 0; j < data[0].length; j++) {
            Arrays.sort(data1, new ColumnComparator(j, SortingOrder.ASCENDING));
            k = 0;
            while (data1[k][j] == 0) {
                k++;
            }
            min[j] = data1[k][j];
            max[j] = data1[data.length - 1][j];
            System.out.println("min: " + min[j]);
            System.out.println("max: " + max[j]);
        }
        // min[0]=0;
        //max[0]=data.length;

        //Arrays.sort(data, new ColumnComparator(0, SortingOrder.ASCENDING));
        for (int j = 0; j < data.length; j++) {
            for (int p = 0; p < data[0].length; p++) {

                System.out.print(data[j][p] + "\t");
            }
            System.out.println();
        }
        for (int j = 0; j < data.length; j++) {
            for (int p = 0; p < data[0].length; p++) {
                if (data[j][p] != 0) {
                    data[j][p] = (data[j][p] - min[p]) / (max[p] - min[p]);
                }
                System.out.print(data[j][p] + "\t");
            }
            System.out.println();
        }
    }
}
