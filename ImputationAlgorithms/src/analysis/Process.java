/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis;

import emimputation.EMImputation;
import hotdeckimputation.HotDeck;
import java.util.ArrayList;
import knnimputation.KNNImputation;
import meanimputation.MeanImputation;
import multipleimputation.MImputation;
import regressionimputation.MultipleLinearRegression;

/**
 *
 * @author Sinan this class performs developed algorithms for missing data on
 * specified missing data set
 * filename--> the data set which has missing values 
 * outputfile --> imputed data set which is created by using specific method
 *
 */
public class Process {

    public EMImputation em;
    public HotDeck hd;
    public KNNImputation knn;
    public MeanImputation mean;
    public MImputation multipleimp;
    public MultipleLinearRegression reg;
    public ArrayList<String> methods;
    /* public String enum  algorithms{
     em,hd,knn,mean,multipleimp,reg;
     }*/

    public Process() {

    }

    public void imputedata(String filename, String outputfile) throws Exception {

        // run each algortihm with specified filename  then write the results specified output filename
        for (int i = 1; i < 4; i++) { // missing value ratio varies from 10 t0 30
            em = new EMImputation();
            em.RunEM(filename + i * 10 + "mv.csv", outputfile + "\\" + i * 10 + "\\em" + ".csv");
            hd = new HotDeck();
            hd.RunHotDeck(filename + i * 10 + "mv.csv", outputfile + "\\" + i * 10 + "\\hd" + ".csv");
            knn = new KNNImputation();
            knn.RunKNN(filename + i * 10 + "mv.csv", outputfile + "\\" + i * 10 + "\\knn" + ".csv");
            mean = new MeanImputation();
            mean.RunMean(filename + i * 10 + "mv.csv", outputfile + "\\" + i * 10 + "\\mean" + ".csv");
            multipleimp = new MImputation();
            multipleimp.RunMI(filename + i * 10 + "mv.csv", outputfile + "\\" + i * 10 + "\\multipleimp" + ".csv");
            reg = new MultipleLinearRegression();
            reg.RunRegression(filename + i * 10 + "mv.csv", outputfile + "\\" + i * 10 + "\\reg" + ".csv");
        }

    }

    public void kmeanscluster(String filename) {

    }

    public static void main(String[] args) throws Exception {
        // data with missisng values
        String filename = "C:\\Users\\Sinan\\Desktop\\Tests\\Beta\\DIM128_beta_";
        // the folder for output
        String output = "C:\\Users\\Sinan\\Desktop\\Tests\\Beta\\";

        Process p = new Process();
        p.imputedata(filename, output);
    }

}
