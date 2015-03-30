/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regressionimputation;

/**
 *
 * @author Sin@n K@pl@n
 * This class mainly builds multiple linear regression model for
 * given data and to impute the each missing value according to created
 * regression model. 
 * Regression model is created from the instances that have no missing value.
 */
import Jama.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MultipleLinearRegression {

    //private double[] means;//means for each variable/feature
    //private double[] stdeviations;// standart deviations for each variable/features
    private boolean[][] missing;// nonmissing columns
    //private double[][] covariances;
    public static double[][] data;
    //private int[][] availableAttrIndex;//hold the index of the available attributes

    public static double[][] completedata;
    private int N;//number of 
    private int P;// number of dependent variable
    private Matrix beta;//regression coefficients
    private double SSE;//sum of squared error of prediction
    private double SST;//total sum of squared
    public double[][] modelcoefficients;
    public static String[] header;

    public MultipleLinearRegression() {

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
        missing = new boolean[data.length][data[0].length];
        System.out.println(data.length);
        System.out.println(data[0].length);

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                if (data[i][j] == 0) {
                    missing[i][j] = true;

                }
            }
        }

        for (int i = 0; i < data.length; i++) {
            k = 0;
            // System.out.print(data[i][k]+"\t");

            while ((data[i][k] != 0) && (k < data[0].length - 1)) {

                System.out.print(data[i][k] + "\t");
                //nonmissing[i][k]=true;
                System.out.print(missing[i][k] + "\t");
                k++;
            }
            if (data[i][k] == 0) {
                k--;
            }

            System.out.println("k: " + k);
            // if all columns of the record is observed
            if (k == (data[0].length - 1)) {
                tot_comp_rec++;
                comp_rec_index.add(i);
                System.out.println("nonmissing index:  " + i);
            }
        }
        //System.out.println(tot_comp_rec);
        
        //initialize complete data matrix to compute regression models
        completedata = new double[tot_comp_rec][data[0].length];
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
    * create regression model from given y and x (y=ax+e).
    * This method computes the regression coefficients for y
    * @param x
    * @param y 
    */
    public void MultipleLinearRegressionCreate(double[][] x, double[] y) {
        //cehck the length
        if (x.length != y.length) {
            throw new RuntimeException("dimesions must agree!!!!");
        }
        N = y.length;
        P = x[0].length;
        // modelcoefficients = new double[P][P];

        Matrix X = new Matrix(x);

        // create matrix from vector y
        Matrix Y = new Matrix(y, N);

        //least squares
        QRDecomposition qr = new QRDecomposition(X);
        beta = qr.solve(Y);
        //mean of y[] values
        double sum = 0.0;
        for (int i = 0; i < N; i++) {
            sum = sum + y[i];
        }
        double mean = sum / N;

        //total variation
        for (int i = 0; i < N; i++) {
            double dev = y[i] - mean;
            SST += dev * dev;//(y[i]-mean)^2

        }
        System.out.println("SST value: " + SST);
        //variation not accounted for
        Matrix residuals = X.times(beta).minus(Y);
        SSE = residuals.norm2() * residuals.norm2();//(actual data-estimated data)^2
        System.out.println("SSE value: " + SSE);

        System.out.print("\n\n");
    }

    /**
     *
     * @param j
     * @return coefficients
     */
    public double beta(int j) {
        return beta.get(j, 0);
    }

    /**
     * R^2 is a statistical measures of how close the data are to the fitted
     * regression line. It is also known as the coefficient of determination, or
     * the coefficient of multiple determination of multiple regression
     *
     * @return
     */
    public double R2() {
        return 1.0 - (SSE / SST);
    }

    /**
     * 
     * this method creates multiple linear regression model for each attributes
     * from the complete data
     * @param completeData
     */
    public void createRegressionModel(double[][] completeData) {
        double[] y = new double[completeData.length];//
        double[][] x = new double[completeData.length][completeData[0].length];
        int deleted_column;// hold the column to be deleted
        int p, k;//indexes
        modelcoefficients = new double[data[0].length][data[0].length];
        for (int j = 0; j < completeData[0].length; j++) {

            // get y values from complete data
            for (int i = 0; i < completeData.length; i++) {
                y[i] = completeData[i][j];
                System.out.print(y[i] + "\t");
                x[i][0] = 1;
            }
            System.out.print("\n");
            // jth column is going to be deleted for each record
            deleted_column = j;
            // create model with coefficients for each y values(attributes)
            p = 0;//row indicator
            for (int i = 0; i < completeData.length; i++) {
                k = 1;//column indicator starts from 1 because first column holds 1s
                for (int t = 0; t < completeData[0].length; t++) {
                    //check whether t column is  equal to j column
                    //if it is then skip  it
                    if (t != deleted_column) {
                        x[p][k] = completeData[i][t];
                        k++;
                    }
                }
                p++;
            }

            for (int i = 0; i < completeData.length; i++) {

                for (int t = 0; t < completeData[0].length; t++) {

                    System.out.print(x[i][t] + "\t");

                }
                System.out.print("\n");

            }

            //compute coefficients for each variable/attribute
            MultipleLinearRegressionCreate(x, y);

            System.out.println("model coefficinets for attribute: " + j);
            for (int i = 0; i < x[0].length; i++) {
                modelcoefficients[j][i] = beta(i);
                System.out.print(modelcoefficients[j][i] + "\t");
            }
            System.out.print("\n\n");

        }

    }

    /**
     * this method imputes data according to the given model
     */
    private void impute() {
        int p;
        double value = 0.0d;
        //start imputing each  missing attribute of each record
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                p = 1;
                // decide if the attirbute j of the record i is misssing or not
                // if it is then start imputing by using regression model for jth attribute
                //y =bo+b1*x1+b2*x2....+bn*xn
                if (missing[i][j] == true) {
                    value = modelcoefficients[j][0]; // value = b0
                    /*System.out.println("model "+j+"coefficinets" );
                     System.out.print(modelcoefficients[j][0]+"\t");
                     System.out.print(modelcoefficients[j][1]+"\t");
                     System.out.println(modelcoefficients[j][2]);*/
                    for (int k = 0; k < data[0].length; k++) {
                        if (k != j) {
                            value = value + data[i][k] * modelcoefficients[j][p];
                            p++;
                        }
                    }
                    System.out.println("Value of " + j + " th attribute of " + i + "  th record is: " + value);
                    data[i][j] = value;
                }
            }
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
        /* String[] dims = inFile.readLine().split(",");
         if (dims.length != 2) {
         throw new Exception("Error: malformed dimensions line");
         }*/
        try (BufferedReader inFile = new BufferedReader(new FileReader(filename))) {
            header = inFile.readLine().split(",");
            /* String[] dims = inFile.readLine().split(",");
             if (dims.length != 2) {
             throw new Exception("Error: malformed dimensions line");
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

    public void RunRegression(String filename, String output) throws Exception {
        read_data(filename);
        prepareData();
        System.out.println("Start regression model creation");
        createRegressionModel(completedata);
        System.out.println("Start imputation");
        impute();
        generateCSVFile(output);

        System.out.println("After Imputation");

        for (int i = 0; i < data.length; i++) {

            for (int t = 0; t < data[0].length; t++) {

                System.out.print(data[i][t] + "\t");

            }
            System.out.print("\n");

        }
    }

    public static void main(String[] args) throws Exception {

        String filename = "C:\\Users\\Sinan\\Desktop\\Tests\\Beta\\DIM128_beta_20mv.csv";
        String output = "C:\\Users\\Sinan\\Desktop\\Tests\\Beta\\20\\reg.csv";
        MultipleLinearRegression m = new MultipleLinearRegression();
        m.RunRegression(filename, output);

    }

}
