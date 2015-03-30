/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis;

/**
 *
 * @author Sinan
 */
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

public class SilhouetteIndex {

    public double calculateIndex(SimpleKMeans sk, Instances inst, int c) throws Exception {
        //Map<Integer, Instances> clustermap = sk.clusterInstance;
        sk.setNumClusters(c);
        sk.buildClusterer(inst);
        EuclideanDistance ed = new EuclideanDistance();
        double avgSilhouetteOverAllPoints = 0.d;

        if (sk.getNumClusters() == 1) {
            //Index is not defined for k=1. needs at least 2 clusters
            return Double.NaN;
        }

        for (int i = 0; i < inst.numInstances(); i++) {
            //for the current element get its cluster
            int currentcluster = sk.clusterInstance(inst.instance(i));
            //System.out.println(inst.instance(i).value(2));
            double[] current_attr = new double[inst.numAttributes()];
            double[] other_attr = new double[inst.numAttributes()];
            //get attributes of the current instance
            for (int attr = 0; attr < inst.numAttributes(); attr++) {
                current_attr[attr] = inst.instance(i).value(attr);
            }
            // int counter
            double[] distances = new double[sk.getNumClusters()];
            int[] counters = new int[sk.getNumClusters()];
            //System.out.println("distances: "+distances.length);
            double avgInClusterDist = 0, dist = 0;
            int countsamecluster = 0;
            distances[currentcluster] = Double.MAX_VALUE;
            for (int j = 0; j < inst.numInstances(); j++) {
                for (int attr = 0; attr < inst.numAttributes(); attr++) {
                    other_attr[attr] = inst.instance(j).value(attr);
                }
                //get cluster number of j th element
                int clusternumber = sk.clusterInstance(inst.instance(j));
                //check if j and i in the same cluster
                if (clusternumber == currentcluster) {
                    if (inst.instance(i) != inst.instance(j)) {
                        //calculate average dist to other elements in the cluster
                        //inst.

                        dist = ed.compute(current_attr, other_attr);
                        avgInClusterDist = avgInClusterDist + dist;
                        countsamecluster++;
                    }
                } else {
                    dist = ed.compute(current_attr, other_attr);
                    distances[clusternumber] = distances[clusternumber] + dist;
                    counters[clusternumber]++;
                }
            }
            //calculate value ai
            if (countsamecluster > 0) {
                avgInClusterDist = avgInClusterDist / countsamecluster; //this is value ai
            }
            //find average distances to other clusters
            for (int k = 0; k < distances.length; k++) {
                if (k != currentcluster) {
                    distances[k] = distances[k] / counters[k];
                }
            }
            //Find the min value of average distance to other clusters
            double min = distances[0];
            for (int k = 1; k < distances.length; k++) {
                if (min > distances[k]) {
                    min = distances[k];
                }
            }

            //si for current element:
            double si;
            // if we only have one element in our cluster it makes sense to set
            // si = 0
            if (countsamecluster == 1) {
                si = 0.0d;
            } else {
                si = (min - avgInClusterDist) / Math.max(min, avgInClusterDist);
            }
            avgSilhouetteOverAllPoints = avgSilhouetteOverAllPoints + si;
        }
        //System.out.println(inst.numInstances());
        return avgSilhouetteOverAllPoints / inst.numInstances();

    }
}
