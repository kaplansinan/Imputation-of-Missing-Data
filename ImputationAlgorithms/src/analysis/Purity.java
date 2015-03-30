/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis;

import Jama.Matrix;
import java.util.Random;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author Sinan
 * This class finds the purity of the imputed data set. 
 * originalfile --> Original data set --> which has no missing values
 * imputedfile --> imputed data set 
 */
public class Purity {

    private static Random m_rr;
    public static Instances instances;
    public static Instances myinstances;
    public SilhouetteIndex silindex;
    public Matrix original;
    public Matrix imputed;

    public Purity() {

    }

    /**
     * 
     * @param k number of clusters
     * @param originalfile original data
     * @param imputedfile imputed data
     * @throws Exception 
     */
    public void findpurity(int k, String originalfile, String imputedfile) throws Exception {
        //get original data
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(originalfile);
        // get imputed data
        ConverterUtils.DataSource mysource = new ConverterUtils.DataSource(imputedfile);
        //get instances for clustering
        this.instances = source.getDataSet();
        this.myinstances = mysource.getDataSet();
        //Simple Kmeans for clustering
        SimpleKMeans globalkmeans = new SimpleKMeans();
        SimpleKMeans mykmeans = new SimpleKMeans();
        //set  number of clusters
        globalkmeans.setNumClusters(k);
        mykmeans.setNumClusters(k);
        // build clusters
        globalkmeans.buildClusterer(instances);
        mykmeans.buildClusterer(myinstances);
        
        // to compare clusters create matrix for original data and imputed data
        // this matrix indicates the  instances in the came clusters 
        original = new Matrix(instances.numInstances(), k);
        imputed = new Matrix(myinstances.numInstances(), k);
        // get cluster numbers for each instance and initialize associated cluster value to 1
        for (int i = 0; i < myinstances.numInstances(); i++) {
            //System.out.println(instances.instance(i));
            original.set(i, globalkmeans.clusterInstance(instances.instance(i)), 1);
            imputed.set(i, mykmeans.clusterInstance(myinstances.instance(i)), 1);
        }
        System.out.println("k is: \t"+original.getColumnDimension());
        //System.out.println(imputed.getRowDimension());
        original = original.times(original.transpose());
        imputed = imputed.times(imputed.transpose());

        int total1 = 0;// to count  instances in the imputed data in the same cluster
        int total2 = 0; // to count  instances in the original data in the same cluster
        //int value = 1;
        for (int i = 0; i < original.getRowDimension(); i++) {
            for (int j = i ; j < original.getColumnDimension(); j++) {

                if ((original.get(i, j) == 1)) {
                    if (imputed.get(i, j) == 1) {
                       
                        total1++; // if i  and j th instance in the same cluster in the imputed data
                    }
                    total2++;// if the i and j th instance in the same cluster in the original data
                    
                }

            }
            //System.out.println();
        }

        // calculate purity
        double purity;
        purity=(double)total1 /(double) total2;
        System.out.println("WCSS --> Original Data: " + mykmeans.getSquaredError());
        System.out.println("WCSS --> Imputed Data: " + globalkmeans.getSquaredError());
       // System.out.println("Total Hit is \t" + total1);
        //System.out.println("Total for  hit is \t" + total2);
        System.out.println("Purity is: " +purity );

    }

    public static void main(String[] args) throws Exception {
        Purity p = new Purity();
        String source = "C:\\Users\\Sinan\\Desktop\\Tests\\DIM128.csv";
        String imputedfile = "C:\\Users\\Sinan\\Desktop\\Tests\\Gaussian\\30\\reg.csv";
        p.findpurity(2, source, imputedfile);
        p.findpurity(4, source, imputedfile);
        p.findpurity(8, source, imputedfile);
        p.findpurity(16, source, imputedfile);
        p.findpurity(32, source, imputedfile);
        p.findpurity(64, source, imputedfile);
        p.findpurity(128, source, imputedfile);
    }
}
