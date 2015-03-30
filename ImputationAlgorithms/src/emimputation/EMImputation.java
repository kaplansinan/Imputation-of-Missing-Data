/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emimputation;

import Jama.Matrix;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 *
 * @author Sin@n K@pl@n
 * This class implements the Expectation Maximization Algorithm to
 * impute missing data. It consists of two main step: E and M. E Step: This step
 * estimates the parameters,mean and variance, and fill the missing values. M
 * Step: This step re-compute the estimated parameters,mean and variance
 */
public class EMImputation {

    private double loglikelihood;//Threshold for convergence of EM
    private int numofInstances;// number of instances
    private int numofAttributes;// number of attributes
    private double[] means;//means for each variable/feature
    private double[] stdeviations;// standart deviations for each variable/features
    private int numberofIteration;//number of maximum iterations
    private boolean[][] missing;// missing columns
    private double[][] covariances;
    public int[] miss_ins;
    public static double[][] data;
    private int[] totalMissingAttr;// hold total number of missing attributes for each recordle
    private int[][] availableAttrIndex;//hold the index of the available attributes
    private static final int MAX_ITERATIONS = 100; //maximum number of EM iterations
    public double[] max;
    public double[] min;
    public static String[] header;

    public EMImputation() {

    }

    /**
     * Sets the maximum number of EM iterations
     *
     * @param newIterations the maximum number of EM iterations
     */
    public void setNumIterations(int newIterations) {
        numberofIteration = newIterations;
    }

    /**
     * Gets the maximum number of EM iterations
     *
     * @return the maximum number of EM iterations
     */
    public int getNumIterations() {
        return numberofIteration;
    }

    /**
     * Sets the EM log-likelihood convergence threshold
     *
     * @param newThreshold the EM log-likelihood convergence threshold
     */
    public void setLogLikelihoodThreshold(double newThreshold) {
        loglikelihood = newThreshold;
    }

    /**
     * Gets the EM log-likelihood convergence threshold
     *
     * @return the EM log-likelihood convergence threshold
     */
    public double getLogLikelihoodThreshold() {
        return loglikelihood;
    }

    /**
     * This method initialize required parameters and set the starting values of
     * the parameters(mean and variance)
     *
     */
    public void prepareData() {

        Random r = new Random(10);//random double number generator
        //System.out.println(data.length);
        numofInstances = data.length;//get num of instances in the data set
        numofAttributes = data[0].length;// get num of attributes each resord have
        System.out.println("attributes  " + data[0].length);
        means = new double[numofAttributes];//initialize means matrix
        missing = new boolean[numofInstances][numofAttributes];//initialize missing matrix
        covariances = new double[numofAttributes][numofAttributes];// initialize covariances matrix
        stdeviations = new double[numofAttributes];
        totalMissingAttr = new int[numofInstances];
        availableAttrIndex = new int[numofInstances][numofAttributes];
        miss_ins = new int[data[0].length];
        max = new double[data[0].length];
        min = new double[data[0].length];
        // normalize();
        for (int i = 0; i < numofAttributes; i++) {
            totalMissingAttr[i] = 0;
        }
        // setting up for missing values and means matrix
        for (int i = 0; i < numofInstances; i++) {
            int k = 0;
            //int temp=0;
            for (int j = 0; j < numofAttributes; j++) {

                if (data[i][j] != 0) {
                    //System.out.println(data[i][j]);
                    //means[j] = means[j] + data[i][j];
                    //missing[i][j] = false;// if attribute j of instance i not missing
                    availableAttrIndex[i][k] = j;
                    // System.out.println("available index of " + i + "is" + availableAttrIndex[i][k]);
                    k++;
                } else {

                    missing[i][j] = true;//if attribute j of instance i is missing
                    miss_ins[j]++;
                    totalMissingAttr[i]++;
                    //System.out.println("instance " + i + "  has  " + j + " atribute is missing");
                }
            }
            //System.out.println("total missing value in "+i+" is" +totalMissingAttr[i]);
        }

        //compute mean of each attribute for a given data 
        int tmp = 0;// hold the number of attributes that have no missing data
        for (int i = 0; i < data.length; i++) {
            // if (totalMissingAttr[i] == 0) {
            for (int j = 0; j < data[0].length; j++) {
                //System.out.println(data[i][j]);
                means[j] = means[j] + data[i][j];

            }
            // tmp++;
            //}
        }

        // final settings for mean array
        for (int i = 0; i < numofAttributes; i++) {
            means[i] = means[i] / (data.length - miss_ins[i]);
            //System.out.println("Mean of attribute "+i+" is: "+means[i]);
        }

        //compute covariances b/w each attibute
        for (int i = 0; i < data.length; i++) {

            if (totalMissingAttr[i] == 0) {
                for (int j = 0; j < data[0].length; j++) {

                    for (int k = 0; k < data[0].length; k++) {
                        covariances[j][k] = covariances[j][k] + data[i][j] * data[i][k];
                    }
                }
            }
        }

        for (int j = 0; j < data[0].length; j++) {
            for (int k = 0; k < data[0].length; k++) {
                covariances[j][k] = (covariances[j][k] / data.length) - (means[j] * means[k]);
                //System.out.print(covariances[j][k]+"\t");
            }
            //System.out.println();
        }

        /* for (int j = 0; j < data[0].length; j++) {
         System.out.print(means[j]+",");
         }*/
        //System.out.println(tmp);
        computeStandartDeviation();
        /* for (int j = 0; j < data[0].length; j++) {
         System.out.print(stdeviations[j]+"\t");
         }*/

        //System.out.println(stdeviations[0]+"\t"+stdeviations[1]+"\t"+stdeviations[2]);
    }

    /**
     * Estep and Mstep go through the this method
     */
    public void process() {
        double old_loglikelihood = -Double.MAX_VALUE;
        double new_loglikelihood = -Double.MAX_VALUE;
        int iteration = 0;
        double x;
        //a--> available attribute
        //m--> missing attribute
        //go through all instances to impute each missing attribute of each instance
        do {
            // System.out.println("begining");
            //System.out.println("iteration step: " + iteration);
            old_loglikelihood = new_loglikelihood;
            x = means[0];
            // System.out.println("means 0 before:  " + x);
            EStep();
            new_loglikelihood = MStep();
            //System.out.println("means 0 after:  " + means[0]);
            //System.out.println("end of the iteration");
            System.out.println("likelihood is: " + new_loglikelihood);
            iteration++;

        } while (old_loglikelihood < new_loglikelihood && iteration < MAX_ITERATIONS);

        // generateCSVFile();
        System.out.println("iteration step: " + iteration);
        //MStep();
    }

    /**
     * perform E Step
     */
    public void EStep() {
        for (int i = 0; i < data.length; i++) {
            int j = 0;//hold index of attibute for i th instance
            Matrix cov_am = new Matrix((data[i].length) - totalMissingAttr[i], 1);//[ ] axm
            Matrix xama = new Matrix(1, (data[i].length) - totalMissingAttr[i]);//x[a]-means[a]--
            Matrix cov_aa = new Matrix((data[i].length) - totalMissingAttr[i], (data[i].length) - totalMissingAttr[i]);// [ ]axa
            //check whether all attributes of instance "i" are missing or not
            while (totalMissingAttr[i] < data[0].length && j < data[0].length) {
                // if "j"th atribute of instance "i" is missing then do  imputation
                if (missing[i][j]) {

                    for (int k = 0; k < (data[i].length) - totalMissingAttr[i]; k++) {
                        //X[a]-M[a]
                        xama.set(0, k, (data[i][availableAttrIndex[i][k]]) - means[availableAttrIndex[i][k]]);
                        //System.out.print((data[i][availableAttrIndex[i][k]]) - means[availableAttrIndex[i][k]]);
                        //[ ]axm--> Std[a]*Std[m]
                        cov_am.set(k, 0, covariances[availableAttrIndex[i][k]][j]);
                        //[ ]axa--> Std[a]*Std[a]
                        for (int l = 0; l < (data[i].length) - totalMissingAttr[i]; l++) {
                            //Cov axa 
                            cov_aa.set(k, l, (covariances[availableAttrIndex[i][k]][availableAttrIndex[i][l]]));

                        }

                    }
                    /*System.out.println(cov_aa.getColumnDimension());
                     for (int t = 0; t < cov_aa.getRowDimension(); t++) {
                     for (int p = 0; p < cov_aa.getColumnDimension(); p++) {
                     System.out.print(cov_aa.get(t, p) + "***");
                     }
                     System.out.println();
                     }*/
                    //fill the missing attribute   
                    data[i][j] = means[j] + ImputeMissingData(xama, cov_aa, cov_am);

                    System.out.println(i + " instance " + j + "  attribute is:  " + data[i][j]);
                }

                j++;
            }
        }//end mai
    }

    private double ImputeMissingData(Matrix xama, Matrix cov_aa, Matrix cov_am) {
        double value = 0;
        double[] row;
        Matrix invCov_aa, result;
        invCov_aa = cov_aa.inverse();
        result = xama.times(invCov_aa);
        result = result.times(cov_am);
        row = result.getArray()[0];
        // System.out.println("row:"+row.length);
        for (int i = 0; i < row.length; i++) {
            value += row[i];

        }
        return value;

    }

    /**
     * perform M Step
     *
     * @return loglikelihood
     */
    public double MStep() {
        loglikelihood = 0;//initialize loglikelihood
        Matrix x = new Matrix(1, data[0].length);// hold each individual instance for matrix processes
        Matrix m = new Matrix(1, means.length);//hold mean for each attribute
        Matrix cov = new Matrix(covariances.length, covariances.length);//hold covariances for matrix 

        //compute new mean and stdeviations for each atribute
        maximise();
        //Convert covariances[j][j] to Matrix form  and means to matrix form 
        for (int j = 0; j < data[0].length; j++) {
            m.set(0, j, means[j]);
            for (int k = 0; k < data[0].length; k++) {
                cov.set(j, k, covariances[j][k]);
            }
        }
        ///System.out.println(cov.get(0, 0));

        for (double[] data1 : data) {
            for (int j = 0; j < data[0].length; j++) {
                x.set(0, j, data1[j]); //// convert X[i][j] to Matrix form X[i]
            }
            loglikelihood = loglikelihood + getLoglikelihood(x, m, cov);
        }

        return loglikelihood;
    }

    /**
     * compute new mean, standart deviations and covariances
     *
     * @param
     */
    private void maximise() {
        computeMean();
        computeCovariance();
        computeStandartDeviation();
    }

    /**
     * compute loglikelihood
     *
     * @param x the instance
     * @param m the mean matrix
     * @param cov the covariance matrix
     * @return
     */
    private double getLoglikelihood(Matrix x, Matrix m, Matrix cov) {
        double value = 0;
        double[] row;
        double detcov;
        Matrix diff, difftrans, invcov, result;
        detcov = cov.det() / 1000;//|cov|
        diff = x.minus(m);//X[i]-M
        invcov = cov.inverse();//cov^(-1)
        difftrans = x.minus(m);
        difftrans = difftrans.transpose();//(X[i]-M)^T
        result = difftrans.times(diff);//((X[i]-M)^T)*(X[i]-M)
        result = result.times(invcov);
        row = result.getArray()[0];
        // System.out.println("row:"+row.length);
        for (int i = 0; i < row.length; i++) {
            value += row[i];

        }
        //System.out.println("detcov: "+detcov);
        //System.out.println("value: "+value);
        value = value + Math.log(detcov);
        //System.out.println("value log: "+value);
        value *= -0.5d;
        return value;
    }

    /**
     * compute mean of each attribute
     */
    public void computeMean() {

        // to compute the means for each attribute, first old means must be 0///
        for (int j = 0; j < data[0].length; j++) {
            means[j] = 0;
        }
        //compute new mean
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                means[j] = means[j] + data[i][j];
            }
        }

        // final settings for means
        for (int i = 0; i < data[0].length; i++) {
            means[i] = means[i] / data.length;
            // System.out.println(means[i]);
        }

    }

    /**
     * compute std. of each attribute
     */
    public void computeStandartDeviation() {

        // to compute the standart deviation for each attribute, first old standart deviatons must be 0///
        for (int j = 0; j < data[0].length; j++) {
            stdeviations[j] = 0;
        }

        // get standart deviation of each attribute from the covariances matrix
        for (int i = 0; i < data[0].length; i++) {
            stdeviations[i] = Math.sqrt(covariances[i][i]);
        }
    }

    /**
     * compute the variance b/w each attribute
     */
    public void computeCovariance() {

        //first initialize all covariances to 0(zero) to compute new covariances
        for (int j = 0; j < data[0].length; j++) {
            for (int k = 0; k < data[0].length; k++) {
                covariances[j][k] = 0;
            }
        }

        //compute covariances b/w each attribute
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                for (int k = 0; k < data[0].length; k++) {
                    covariances[j][k] = covariances[j][k] + data[i][j] * data[i][k];
                }
            }
        }
        //final settings
        for (int j = 0; j < data[0].length; j++) {
            for (int k = 0; k < data[0].length; k++) {
                covariances[j][k] = (covariances[j][k] / data.length) - (means[j] * means[k]);
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
            /* String[] dims = inFile.readLine().split(",");
             if (dims.length != 2) {
             throw new Exception("Error: malformed dimensions line");
             }*/
            header = inFile.readLine().split(",");
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

    public void RunEM(String filename, String output) throws Exception {
        read_data(filename);
        prepareData();
        process();
        generateCSVFile(output);

    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        EMImputation ei = new EMImputation();
        String filename = "C:\\Users\\Sinan\\Desktop\\Tests\\Gamma\\DIM128_gamma_10mv.csv";
        String output = "C:\\Users\\Sinan\\Desktop\\test_em_10_gamma_newwwww.csv";
        /*ei.read_data(filename);
         ei.prepareData();
         ei.process();*/
        ei.RunEM(filename, output);
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
                /* writer.append(String.valueOf(d1));
                 writer.append(',');
                 writer.append(String.valueOf(d2));
                 writer.append(',');
                 writer.append(String.valueOf(clusterNo));
                 writer.append('\n');*/

                //generate whatever data you want
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

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
         //min[0]=0;
        // max[0]=data.length;

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

    /*
     * Simple Enum to represent sorting order e.g. ascending and descending order
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

}
