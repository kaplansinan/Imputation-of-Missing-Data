/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multipleimputation;

import Jama.*;
import java.util.Random;

/**
 *
 * @author Sinan
 */
public class BootstrapModelSampling {

    private int N;//number of 
    private int P;// number of dependent variable
    private Matrix beta;//regression coefficients
    private double SSE;//sum of squared error of prediction
    private double SST;//total sum of squared
    //public double[][] modelcoefficients;
    public double[][] residuals;
    public double[][] regresseddata;
    //public double[][] new_coef;

    public BootstrapModelSampling() {

    }

    /**
     * this method implements the bootstrap fixed-x-resampling method it takes
     * complete data as a parameter and performs fixed-x-resampling method on
     * it. As a result it returns coefficients for each model
     *
     * @param completedata
     * @return coef
     */
    public double[][][] modelbasedresampling(double[][] completedata) {
        //initialize coefficients array to be returned. It has three dimensions  because we are going to create m resamplimg model
        // and for each model we have coefficients for each attribute 
        //m--> resampling model number, n--> number of attributes/features (mxnxn)--> total dimesion
        double[][][] coef = new double[5][completedata[0].length][completedata[0].length];
        //initialize residuls array for each attribute
        residuals = new double[completedata.length][completedata[0].length];
        // initialize regressed data to compute residuals y[i]-y*[i]
        regresseddata = new double[completedata.length][completedata[0].length];
        double[][] new_reg_data = new double[completedata.length][completedata[0].length];
        double[][] modelcoefficients = new double[completedata[0].length][completedata[0].length];
        double[][] new_coef = new double[completedata[0].length][completedata[0].length];


        /* start performing  bootstrap fixed resampling*/
        //create regression model for each attribute given complete data
        System.out.print("\n" + "Complete Data" + "\n");
        for (double[] comp1 : completedata) {
            for (int k = 0; k < completedata[0].length; k++) {
                System.out.print(comp1[k] + "\t");
            }
            System.out.print("\n");
        }

        computecoefficients(completedata, completedata, modelcoefficients);
        System.out.println("coefficients for complete data");
        for (int j = 0; j < completedata[0].length; j++) {

            for (int k = 0; k < completedata[0].length; k++) {

                System.out.print(modelcoefficients[j][k] + "\t");
            }
            System.out.print("\n");
        }
        System.out.print("\n" + "regressed  after complete data" + "\n");
        //compute regressed data from complete data and computed coefficients to compute residuals
        computeRegressedData(completedata, regresseddata, modelcoefficients);
        for (double[] regresseddata1 : regresseddata) {
            for (int k = 0; k < regresseddata[0].length; k++) {
                System.out.print(regresseddata1[k] + "\t");
            }
            System.out.print("\n");
        }
        
        computeresiduals(completedata, regresseddata);
        System.out.print("\n" + "Residuals" + "\n");
        for (double[] residuals1 : residuals) {
            for (int k = 0; k < residuals[0].length; k++) {
                System.out.print(residuals1[k] + "\t");
            }
            System.out.print("\n");
        }
        System.out.println("*******************************************************");
        for (int i = 0; i < 5; i++) {
             System.out.print("\n" + "New Regresssed Data" + "\n");
             //compute new Y by adding residuals by randomly re-sampled
            computeRegressedDatawithResiduals(new_reg_data, completedata);
            for (double[] regresseddata1 : new_reg_data) {
                for (int k = 0; k < new_reg_data[0].length; k++) {
                    System.out.print(regresseddata1[k] + "\t");
                }
                System.out.print("\n");
            }
            //Compute 
            computecoefficients(completedata, new_reg_data, new_coef);

            //get i th model coefficients
            System.out.println("Coefficients " + i + " th imputation");
            for (int j = 0; j < completedata[0].length; j++) {
                System.out.println("Coefficients for attribute " + j);
                for (int k = 0; k < completedata[0].length; k++) {
                    coef[i][j][k] = new_coef[j][k];
                    System.out.print(coef[i][j][k] + "\t");
                }
                System.out.print("\n");
            }

            System.out.println("*********************************************");
        }
        
        
        return coef;
    }

    /**
     * *
     * this method creates multiple linear regression model for each attributes
     * from the complete data
     *
     * @param completeData
     * @param regresseddata
     * @param modelcoefficients
     */
    public void computecoefficients(double[][] completeData, double[][] regresseddata, double[][] modelcoefficients) {
        double[] y = new double[completeData.length];//
        double[][] x = new double[completeData.length][completeData[0].length];
        int deleted_column;// hold the column to be deleted
        int p, k;//indexes

        for (int j = 0; j < completeData[0].length; j++) {

            // get y values from complete data
            for (int i = 0; i < completeData.length; i++) {
                y[i] = regresseddata[i][j];
                //System.out.print(y[i] + "\t");
                x[i][0] = 1;
            }
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

    /**
     * E[i]= Y[i]-y[i] Y[i]--> fixed complete data, y[i]--> computed(regressed)
     * new data this method computes residuals for each attibute according to
     * the given complete data set and regressed data set
     *
     * @param completedata
     * @param regresseddata
     */
    private void computeresiduals(double[][] completedata, double[][] regresseddata) {
        for (int i = 0; i < completedata.length; i++) {
            for (int j = 0; j < completedata[0].length; j++) {
                //System.out.println(completedata[i][j]);
                //System.out.println(regresseddata[i][j]);
                residuals[i][j] = completedata[i][j] - regresseddata[i][j];
                //System.out.println(residuals[i][j]);
            }
        }
    }

    /**
     * Y[i]=X[i]*B[i], X[i]--> Predictors, B[i]--> Coefficients this method
     * computes regressed data according to the given complete data and model
     * coefficients this method does not concern about the residuls
     *
     * @param compdata
     * @param regresseddata
     * @param modelcoefficients
     */
    public void computeRegressedData(double[][] compdata, double[][] regresseddata, double[][] modelcoefficients) {
        int p;
        double value;
        //start imputing each  missing attribute of each record
        for (int i = 0; i < compdata.length; i++) {
            for (int j = 0; j < compdata[0].length; j++) {
                p = 1;
                value = modelcoefficients[j][0]; // value = b0--> intercept value
                for (int k = 0; k < compdata[0].length; k++) {
                    if (k != j) {
                        value = value + compdata[i][k] * modelcoefficients[j][p];
                        p++;
                    }
                }
                // System.out.println("Value of "+j+" th attribute of "+i+"  th record is: " +value);
                regresseddata[i][j] = value;
                // System.out.println(value);
            }
        }
    }

    /**
     * Y[i]=X[i]*B[i]+E[i] X--> Predictors, B--> Coefficients, E--> Residuals
     * this method computes new regressed data matrix by adding residuals
     *
     * @param regresseddata
     */
    private void computeRegressedDatawithResiduals(double[][] regresseddata, double[][] completedata) {
        Random rnd = new Random();// to generate random row number for resampling of residuals
        //Random c=new Random();// and random column number for resampling of residuals
        int c, r;
        for (int i = 0; i < regresseddata.length; i++) {
             r = 0 + rnd.nextInt(completedata.length - 1);
            for (int j = 0; j < regresseddata[0].length; j++) {
                // generate random row number
               
                //generate random column number
                ///c = 0 + rnd.nextInt(completedata[0].length - 1);
                regresseddata[i][j] = completedata[i][j] + residuals[r][j];
                //System.out.println(regresseddata[i][j]);
            }
        }
    }

}
