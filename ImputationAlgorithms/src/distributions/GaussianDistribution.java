/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributions;

/**
 *
 * @author Sin@n K@pl@n
 * 1- This class calculates the gaussian distribution of each instance in the specified data set.
 *    Once the density of each instance is found then based on the ratio of the missing value (10%, 20% or 30%)
 *    missing values are created.
 * 2- Missing values are created on the specific attribute but this can be changed to create
 *    mixed missingness in the data set.
 * 
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

public class GaussianDistribution {

    private double mean;//means for each variable/feature
    private double stdeviation;// standart deviations for each variable/features
    private double variance;
    public static double[][] data;
    public static double[] normalizedData;
    private double max;
    private double min;
    private String fileName;
    public double[] Prob;
    public int[] index;
    public static String[] header;//header of csv file
    public double[] distances;
    public ArrayList<Integer> delindex;

    public GaussianDistribution(String filename) {
        this.fileName = filename;
        try {
            read_data(fileName);
            min = 0;
            max = data[0].length;
            Prob = new double[data.length];
            distances = new double[data.length];
            //normalize();
            //process();
            //System.out.println(em.findmax(data));
        } catch (Exception ex) {
            Logger.getLogger("Something wrong");
        }
    }

    public void RunGaussianDistribution(int pickedinstances, int totalinstances, int deletedattribute) {

        process(pickedinstances, totalinstances, deletedattribute);
    }

    //calculate probability
    public void process(int pickedinstances, int totalinstances, int deletedattribute) {
        double value;
        computeMean();
        computevariance();
        for (int row = 0; row < data.length; row++) {
            value = getProbability(row, mean, variance);

            System.out.println(value);
            Prob[row] = value;
            System.out.println(Prob[row]);
        }
        double total = 0;
        for (int row = 0; row < data.length; row++) {
            total = total + Prob[row];

        }
        System.out.println(total);
        findpairs(pickedinstances, totalinstances);
        deletedata(deletedattribute);
        for (int i = 0; i < data.length; i++) {
            System.out.print("\n");
            for (int j = 0; j < data[0].length; j++) {
                System.out.print(data[i][j] + "\t");
            }
            // System.out.print("\n");
        }

        generateCSVFile();
    }

    //Normalize Data
    private void normalize() {
        normalizedData = new double[data.length];
        for (int row = 0; row < data.length; row++) {
            normalizedData[row] = (row - min) / (max - min);
        }

    }

    // gaussian distribution function
    public double getProbability(double x, double mean, double var) {

        double value = 0;
        value = ((x - mean) * (x - mean)) / (var);
        value = value * -0.5d;

//       System.out.println("value: "+value);
        return (1 / ((Math.sqrt(2 * Math.PI)) * Math.sqrt(var))) * Math.exp(value);

    }

    public void computeMean() {
        mean = 0;
        //compute new mean

        for (int row = 0; row < data.length; row++) {
            mean = mean + row;
        }
        mean = mean / data.length;
        // final settings for means
    }

    public void computevariance() {
        variance = 0;
        for (int row = 0; row < data.length; row++) {
            variance = variance + (row - mean) * (row - mean);
        }
        variance = variance / data.length;
    }

    // find max value of given attribute
    public double findmax(int i) {

        double max = Double.MIN_VALUE;
        for (double[] data1 : data) {
            if (max < data1[i]) {
                max = data1[i];
            }
        }
        return max;
    }

    // find min value of given atribute
    public double findmin(int i) {

        double min = Double.MAX_VALUE;
        for (double[] data1 : data) {
            if (min > data1[i]) {
                min = data1[i];
            }
        }
        return min;
    }

    /**
     * this method find the indexes of the instances that are going to have a
     * missing value on the specified attribute
     */
    public void findpairs(int pickedinstances, int totalinstances) {
        double minval = 0;
        int index;
        RandomNumberGenerate rd = new RandomNumberGenerate();
        Set<Integer> picked = new HashSet<>();

        picked = rd.pickRandom(pickedinstances, totalinstances - 1);
        int[] randindex = new int[picked.size()];
        delindex = new ArrayList<>();

        // create an iterator;
        // create an iterator
        Iterator iterator = picked.iterator();

        // check values
        int k = 0;
        while (iterator.hasNext()) {
            //System.out.println("Value: "+iterator.next() + " "); 
            randindex[k] = (int) iterator.next();
            // System.out.println(randindex[k]);
            k++;
        }
        System.out.println("Same indexes are");
        for (int i = 0; i < randindex.length; i++) {
            calculatedistances(randindex[i]);
            minval = distances[0];
            index = 0;
            for (int j = 1; j < data.length; j++) {
                if (j != randindex[i]) {
                    if (distances[j] < minval) {
                        minval = distances[j];
                        index = j;
                    }
                }
            }
            delindex.add(index);
            delindex.add(randindex[i]);
            System.out.print(index + "\t");
            System.out.println(randindex[i]);

        }
        //System.out.println("size is"randindex[i]);
        for (int j = 0; j < delindex.size(); j++) {
            System.out.println(delindex.get(j));
        }

        //System.out.println(Double.parseDouble("NA"));
    }

    /**
     * to find nearest pair of the specified i th instance calculate distance
     *
     * @param i
     */
    public void calculatedistances(int i) {
        double value;

        for (int j = 0; j < data.length; j++) {
            value = 0;

            //compute Euclidean Distance  --> sqrt[(X[i]*X[i]*Y[i]Y[i])]
            value = value + ((Prob[j] - Prob[i]) * (Prob[j] - Prob[i]));
            value = Math.sqrt(value);
            distances[j] = value;

        }
        //System.out.println("value is:" + value);

    }

    /**
     *
     * @param deletedattribute missing values are going to be created on this
     * attribute
     */
    public void deletedata(int deletedattribute) {
        Random rnd = new Random();
        int j;
        for (int i = 0; i < delindex.size(); i++) {
            //j = rnd.nextInt(6) + 3;
            //System.out.println("j is: " + j);
            data[delindex.get(i)][deletedattribute] = 0;
        }
    }

    private void read_data(String filename) throws Exception {
        ArrayList<ArrayList<Double>> arr;

        try (BufferedReader inFile = new BufferedReader(new FileReader(filename))) {

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
     * to check if the distribution is correct or not you can write it to the
     * specified file
     */
    public void generateDistributionCSVFile() {
        try {
            try (FileWriter writer = new FileWriter("C:\\Users\\Sinan\\Desktop\\DIM128.csv", true)) {
                /*  for (int i = 0; i < header.length; i++) {
                 writer.append(String.valueOf(header[i]));
                 writer.append(',');
                 }
                 writer.append('\n');*/

                for (int i = 0; i < data.length; i++) {
                    String s;
                    DecimalFormat df = new DecimalFormat("0.###########");
                    df.setMaximumFractionDigits(12);
                    s = df.format(Prob[i]);
                    //data[i][j]=((data[i][j])*(max[j]-min[j]))+min[j];
                    //writer.append(String.valueOf(i+1));
                    //writer.append(',');
                    writer.append(s);
                    writer.append('\n');
                }

                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * write the data with missing value
     */
    public void generateCSVFile() {
        try {
            try (FileWriter writer = new FileWriter("C:\\Users\\Sinan\\Desktop\\Tests\\Gaussian\\DIM128_gaussian_10mv.csv", true)) {
                for (int i = 0; i < header.length; i++) {
                    writer.append(String.valueOf(header[i]));
                    writer.append(',');
                }
                writer.append('\n');
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < data[0].length; j++) {
                        //data[i][j]=((data[i][j])*(max[j]-min[j]))+min[j];
                        // zero indicates that the attribute is missing and when save it just write "?" character 
                        if (data[i][j] == 0) {
                            writer.append("?");
                        } else {
                            writer.append(String.valueOf(data[i][j]));
                        }

                        writer.append(',');
                    }
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

    public static void main(String[] args) throws Exception {
        // data to delete attributes of some instances
        String filename = "C:\\Users\\Sinan\\Desktop\\Tests\\DIM128.csv";
        GaussianDistribution gd = new GaussianDistribution(filename);
        int pickedinstances = 55;  //%10 luk azaltma için 110 tane instance a ihtiyac duyulmaktadır. 
        //Bundan dolayı 55 verilerek herbirine en yakın olan bir diğer instance bulunup
        //eksiltme işlemi gerçekleştirlmektedir
        int totalinstances = data.length;
        // kayıp veriler bu attribute üzerinde oluşturulacak
        int deletedattribute = 3;
        gd.RunGaussianDistribution(pickedinstances, totalinstances, deletedattribute);
    }
}
