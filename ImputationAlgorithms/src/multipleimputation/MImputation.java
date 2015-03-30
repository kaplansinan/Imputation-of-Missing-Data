   /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multipleimputation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Sin@n K@pl@n
 * This class implements the Multiple imputation procedure to impute missing values.
 * Data is imputed m times(m=5). Multiple Linear regression lines are used to create model and the to impute data.
 * To create different regression coefficients bootstrap resampling is used.
 * For sampling there is two choices, which are case based bootstrap re-sampling and
 * model based bootstrap re-sampling. User can choose one of those under the sampling method.
 * 
 */
public class MImputation {

    BootstrapModelSampling b;
    BootstrapCaseSampling c;
    public static double[][] data;
    public double[][][] modelcoefficients;
    public static double[][][] multipledataset;
    public static double[][] completedata;
    private boolean[] ismissing;// to check the instance has any missing values or not
    public static double[][] means;
    public static double[][] variances;
    public static double[][] finalcoef;
    public static String [] header;

    public MImputation() {

    }

    /**
     * this method initialize required parameters and builds the complete data
     * matrix
     */
    private void preparedata(int m) {

        ArrayList<Integer> comp_rec_index = new ArrayList<>();// hold the index of complete record
        int tot_comp_rec = 0;//hold the total number of complete records in data matrix
        int k;
        //initialize missing array
        ismissing = new boolean[data.length];
        System.out.println(data.length);
        System.out.println(data[0].length);

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
        //initialize means and variance matrix
        means = new double[m][data[0].length];
        variances = new double[m][data[0].length];

        System.out.print(completedata[0].length);
        System.out.println("completedata");

        /**
         * copy the instance that has no missing value to the completedata
         * matrix. This matrix will be used for creating regression model
         * coefficients using bootstrap sampling methods
         */
        for (int i = 0; i < completedata.length; i++) {
            System.arraycopy(data[comp_rec_index.get(i)], 0, completedata[i], 0, completedata[0].length);
        }

        for (double[] completedata1 : completedata) {
            for (int j = 0; j < completedata[0].length; j++) {
                System.out.print(completedata1[j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * this method creates model coefficients for regression by using
     * bootstrapping there are two choices for bootstrap sampling. First one is
     * model sampling which uses residual to create samplings, and second one is
     * case sampling that uses instances for sampling.
     * User can choose model or case based resampling under this method
     * @param data this data matrix consists of observed samples
     *
     */
    public void sampling(double[][] data) {
        b = new BootstrapModelSampling();
        //c = new BootstrapCaseSampling();
        modelcoefficients = new double[5][data[0].length][data[0].length];
        //initialize final coefficients
        finalcoef = new double[data[0].length][data[0].length];
        //perform selected resampling
        modelcoefficients = b.modelbasedresampling(data);
    }

    /**
     * this method simply creates multiple (m) copy of original data set
     *
     * @param m indicates the number of data sets going to be created.
     */
    public void createmultipledatasets(int m) {
        //initialize size of the multipledata set array
        multipledataset = new double[m][data.length][data[0].length];
        //copy the data 
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < data.length; j++) {
                System.arraycopy(data[j], 0, multipledataset[i][j], 0, data[0].length);
            }
        }

        for (int i = 0; i < m; i++) {
            System.out.println(i + " set of data");
            for (int k = 0; k < data.length; k++) {
                for (int j = 0; j < data[0].length; j++) {
                    System.out.print(multipledataset[i][k][j] + "\t");
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    /**
     * this method imputes each copy of data set based on the coefficients for
     * each regression model created at
     *
     * @param m
     * @throws java.lang.InterruptedException
     */
    public void process(int m) throws InterruptedException {
        // create multiple thread for parallel programming
        //ExecutorService executorService = Executors.newFixedThreadPool(m);
        Thread[] threads = new Thread[m];
        for (int i = 0; i < m; i++) {
            final int k = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(k + " Thread Start");
                    //impute data set
                    impute(k);
                    //compute mean of the each attribute in the given data set(k)
                    computemean(k);
                    //compute variance of each  attribute in the give data set(k)
                    computevariance(k);
                }
            });
            // start thread
            threads[i].start();
            // executorService.shutdown();
        }
        // join all threads to shutdown after each of them finished its job
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * this method pools the general result from each imputed data set such as
     * coefficients
     *
     * @param m
     */
    public void poolresults(int m) {

        //compute average of coefficients from each imputed data set
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < data[0].length; j++) {
                for (int k = 0; k < data[0].length; k++) {
                    finalcoef[j][k] = finalcoef[j][k] + modelcoefficients[i][j][k];
                }
            }
        }
        // final setting for coefficients
        for (int j = 0; j < data[0].length; j++) {
            for (int k = 0; k < data[0].length; k++) {
                finalcoef[j][k] = finalcoef[j][k] / m;
                //System.out.println(finalcoef[j][k]);
            }
        }

        for (int i = 0; i < 5; i++) {
            System.out.println(i + " Set");
            for (int j = 0; j < data[0].length; j++) {
                System.out.println(j + " Attribute");
                for (int k = 0; k < data[0].length; k++) {
                    System.out.print(modelcoefficients[i][j][k] + "\t ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    /**
     * this method imputes missing value based on given data set(m) and its
     * coefficients
     *
     * @param m specify the data set number
     */
    public void impute(int m) {
        int p;
        double value = 0.0d;
        //System.out.println(modelcoefficients[m][2][0]);
        // System.out.println(modelcoefficients[m][1][0]+"\t");
        //start imputing each  missing attribute of each record
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                p = 1;
                // decide if the attirbute j of the record i is misssing or not
                // if it is then start imputing by using regression model for jth attribute
                //y =bo+b1*x1+b2*x2....+bn*xn
                if (multipledataset[m][i][j] == 0) {
                    value = modelcoefficients[m][j][0]; // value = b0
                   /* System.out.println("model " + j + "coefficinets");
                    System.out.print(modelcoefficients[m][j][0] + "\t");
                    System.out.print(modelcoefficients[m][j][1] + "\t");
                    System.out.println(modelcoefficients[m][j][2]);*/
                    for (int k = 0; k < data[0].length; k++) {
                        if (k != j) {
                            value = value + multipledataset[m][i][k] * modelcoefficients[m][j][p];
                            p++;
                        }
                    }
                    System.out.println("Value of " + j + " th attribute of " + i + "  th record is: " + value);
                    multipledataset[m][i][j] = value;
                }
            }
        }


        /*
         System.out.println(m + " set of data after imputation");
         for (int k = 0; k < data.length; k++) {
         for (int j = 0; j < data[0].length; j++) {
         System.out.print(multipledataset[m][k][j]+"\t");
         }
         System.out.println();
         }
         System.out.println();*/
    }

    public void finalimpute(double[][] finalcoef) {
        int p;
        double value = 0.0d;
        //start imputing each  missing attribute of each record
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                p = 1;
                // decide if the attirbute j of the record i is misssing or not
                // if it is then start imputing by using regression model for jth attribute
                //y =bo+b1*x1+b2*x2....+bn*xn
                if (data[i][j] == 0) {
                    /*value=finalcoef[j][0]; // value = b0
                     System.out.println("model "+j+"coefficinets" );
                     System.out.print(finalcoef[j][0]+"\t");
                     System.out.print(finalcoef[j][1]+"\t");
                     System.out.println(finalcoef[j][2]);
                     for(int k=0;k<data[0].length;k++){
                     if(k!=j){
                     value=value+data[i][k]*finalcoef[j][p];
                     p++;
                     }
                     }*/

                    for (int t = 0; t < 5; t++) {
                        data[i][j] = data[i][j] + multipledataset[t][i][j];
                    }
                    data[i][j] = data[i][j] / 5;
                    //System.out.println("Value of "+j+" th attribute of "+i+"  th record is: " +data[i][j]);
                    //data[i][j]=value;
                }
            }
        }

    }

    /**
     * compute mean of each attribute in the given data set
     *
     * @param m specify the data set number
     */
    public void computemean(int m) {
        //compute new mean
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                means[m][j] = means[m][j] + multipledataset[m][i][j];
            }
        }
        // final settings for means
        for (int j = 0; j < data[0].length; j++) {
            means[m][j] = means[m][j] / data.length;
            // System.out.println(means[i]);
        }
    }

    /**
     * compute variance of each attribute in the given data set
     *
     * @param m specify the data set number
     */
    public void computevariance(int m) {

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                variances[m][j] = variances[m][j]
                        + (multipledataset[m][i][j] - means[m][j]) * (multipledataset[m][i][j] - means[m][j]);
            }
        }
        // final settings for variances
        for (int j = 0; j < data[0].length; j++) {
            variances[m][j] = (variances[m][j]) / (data.length - 1);
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
    
    public void RunMI(String filename, String output) throws Exception{
        read_data(filename);
        preparedata(5);
        sampling(completedata);
        createmultipledatasets(5);
        process(5);
        poolresults(5);
        finalimpute(finalcoef);
        generateCSVFile(output);
        
        
         System.out.println("Stop");
        System.out.println("Imputed Data Sets");
        for (int i = 0; i < 5; i++) {
            System.out.println(i + " set of data");
            for (int k = 0; k < data.length; k++) {
                for (int j = 0; j < data[0].length; j++) {
                    System.out.print(multipledataset[i][k][j] + "\t");
                }
                System.out.println();
            }
            System.out.println();
        }
        System.out.println("\nMeans of Imputed Data Sets\n");
        for (int i = 0; i < 5; i++) {
            System.out.println(i + " data set");
            for (int j = 0; j < data[0].length; j++) {
                System.out.print(means[i][j] + "\t");
            }
            System.out.println("\n");
        }

        for (int j = 0; j < data[0].length; j++) {
            System.out.println(j + " Attribute coefficients");
            for (int k = 0; k < data[0].length; k++) {
                System.out.print(finalcoef[j][k] + "\t");
            }
            System.out.println("\n");
        }
        System.out.println("Final Data set");
        for (int j = 0; j < data.length; j++) {

            for (int k = 0; k < data[0].length; k++) {
                System.out.print(data[j][k] + "\t");
            }
            System.out.println("\n");
        }
    }

    public static void main(String[] args) throws InterruptedException, Exception {

        // ExecutorService executorService=Executors.newFixedThreadPool(5);
        
       String filename = "C:\\Users\\Sinan\\Desktop\\Tests\\Gamma\\DIM128_gamma_30mv.csv";
        String output = "C:\\Users\\Sinan\\Desktop\\test_em_10_gamma_newwwww.csv";   
        MImputation m = new MImputation();
        m.RunMI(filename, output);
       
    }
}
