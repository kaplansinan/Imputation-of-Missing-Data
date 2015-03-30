/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multipleimputation;

import Jama.Matrix;
import Jama.QRDecomposition;
import java.util.Random;

/**
 *
 * @author Sinan
 */
public class BootstrapCaseSampling {
    private int N;//number of 
    private int P;// number of dependent variable
    private Matrix beta;//regression coefficients
    private double SSE;//sum of squared error of prediction
    private double SST;//total sum of squared
    public double [][] resampleddata;//hold the resampled data
    
    public BootstrapCaseSampling(){
        
    }
    
    public double[][][] casebasedresampling(double [][] completedata){
        //initialize coefficients array to be returned. It has three dimensions  because we are going to create m resamplimg model
        // and for each model we have coefficients for each attribute 
        //m--> resampling model number, n--> number of attributes/features (mxnxn)--> total dimesion
        /*initialize variables and parameters*/
        double[][][] coef = new double[5][completedata[0].length][completedata[0].length];
        resampleddata=new double[completedata.length][completedata[0].length];
        double[][] modelcoefficients = new double[completedata[0].length][completedata[0].length];
        
        for(int i=0;i<5;i++){
            //resample cases randomly
            resamplecases(completedata);
            computecoefficients(resampleddata,modelcoefficients);
            
            //get i th model coefficients
            System.out.println("Coefficients " + i + " th imputation");
            for (int j = 0; j < completedata[0].length; j++) {
                System.out.println("Coefficients for attribute " + j);
                for (int k = 0; k < completedata[0].length; k++) {
                    coef[i][j][k] = modelcoefficients[j][k];
                    System.out.print(coef[i][j][k] + "\t");
                }
                System.out.print("\n");
            }
        }
        return coef;
    }
    private void resamplecases( double [][] completedata){
        
        Random rnd = new Random();// to generate random row number for resampling of residuals
        //Random c=new Random();// and random column number for resampling of residuals
        int c, r;
        for (int i = 0; i < completedata.length; i++) {
            r = 0 + rnd.nextInt(completedata.length - 1);
            for (int j = 0; j < completedata[0].length; j++) {
                // generate random row number
                //generate random column number
                //c = 0 + rnd.nextInt(completedata[0].length - 1);
                resampleddata[i][j]=completedata[r][j];
                //System.out.println(regresseddata[i][j]);
            }
        }
    }
    
     public void computecoefficients(double[][] resampleddata, double[][] modelcoefficients) {
        double[] y = new double[resampleddata.length];//
        double[][] x = new double[resampleddata.length][resampleddata[0].length];
        int deleted_column;// hold the column to be deleted
        int p, k;//indexes

        for (int j = 0; j < resampleddata[0].length; j++) {

            // get y values from complete data
            for (int i = 0; i < resampleddata.length; i++) {
                y[i] = resampleddata[i][j];
                //System.out.print(y[i] + "\t");
                x[i][0] = 1;
            }
            // jth column is going to be deleted for each record
            deleted_column = j;
            // create model with coefficients for each y values(attributes)
            p = 0;//row indicator
            for (int i = 0; i < resampleddata.length; i++) {
                k = 1;//column indicator starts from 1 because first column holds 1s
                for (int t = 0; t < resampleddata[0].length; t++) {
                    //check whether t column is  equal to j column
                    //if it is then skip  it
                    if (t != deleted_column) {
                        x[p][k] = resampleddata[i][t];
                        k++;
                    }
                }
                p++;
            }
            // just for debugging
          /*  for (int i = 0; i < completeData.length; i++) {

             for (int t = 0; t < completeData[0].length; t++) {

             System.out.print(x[i][t] + "\t");

             }
             System.out.print("\n");

             }*/

            //compute coefficients for each variable/attribute
            MultipleLinearRegressionCreate(x, y);

            // System.out.println("model coefficinets for attribute: "+j); 
            for (int i = 0; i < x[0].length; i++) {
                modelcoefficients[j][i] = beta(i);
                // System.out.print(modelcoefficients[j][i] + "\t");
            }
            //sSystem.out.print("\n\n");

        }
    }

    /**
     * create regression model from given y and x and compute coefficients of
     * regression model
     *
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
        //System.out.println("SST value: "+SST);
        //variation not accounted for
        Matrix residuals = X.times(beta).minus(Y);
        SSE = residuals.norm2() * residuals.norm2();//(actual data-estimated data)^2
        // System.out.println("SSE value: "+SSE);

        //  System.out.print("\n\n");
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

    
    
    
}
