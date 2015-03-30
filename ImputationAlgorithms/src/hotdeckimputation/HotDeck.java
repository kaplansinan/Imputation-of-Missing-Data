/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hotdeckimputation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Sin@n  K@pl@n
 * This class implements the hot deck technique to impute missing
 * data the column/feature/variable to be sorted based on chosen attribute if
 * there is no missing variable on this attribute
 */
public class HotDeck {

    private double[] means;//means for each variable/feature
    private double[] stdeviations;// standart deviations for each variable/features
    public static double[][] data;
    private int[] tot_mis_atr;// holds the total missing instance of  the each  atrribute
    public static int selected_atr;// to perform hot deck algorithm
    public static boolean select_atr;// if the hot deck  performs on the selected item
    public int countb, countf;
    public double[][] modes;
    public static String[] header;

    public HotDeck() {

    }

    public void prepareData() {

        //initialize tot_mis_atr array
        tot_mis_atr = new int[data[0].length];
        //initialize means array
        means = new double[data[0].length];
        modes = new double[2][2];
        // count the total missing instances that have missing value on j th attribute
        for (double[] data1 : data) {
            for (int j = 0; j < data[0].length; j++) {
                if (data1[j] == 0) {
                    tot_mis_atr[j]++;
                }
            }
        }

        // just for debugging purpose
        for (int j = 0; j < data[0].length; j++) {
            System.out.println("Total missing attribute " + j + " is: " + tot_mis_atr[j]);
        }
    }

    public void Sort() {

        // Arrays.sort(data, new ColumnComparator(selected_atr, SortingOrder.ASCENDING));
        // randomly generated column
        int hot_deck_column = 0;

        // if the user select the specific atribute to perform hot deck
        if ((select_atr) == true) {
            System.out.println("select atr is true");
            // if all instances of the selected attribute are not missing
            // then perform hot deck on the selected attribute
            if (tot_mis_atr[selected_atr] != data.length) {
                // perform sorting on this column
                Arrays.sort(data, new ColumnComparator(selected_atr, SortingOrder.ASCENDING));

            } //otherwise select a random attribute to perform  hot deck
            else {

                hot_deck_column = SelectRandomAttribute();
                //perform sorting on this column
                Arrays.sort(data, new ColumnComparator(hot_deck_column, SortingOrder.ASCENDING));
            }

        } //otherwise choose a random atribute that has all the instances observed
        else {
            hot_deck_column = SelectRandomAttribute();
            System.out.println("select atr is false");
            //perform sorting on this column
            Arrays.sort(data, new ColumnComparator(hot_deck_column, SortingOrder.ASCENDING));

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

    /**
     *This method selects the random attribute to sort the data
     * when it selects the attribute it checks if there is any missing value on this attribute or not.
     * Simply,the attribute which has no missing value is chosen
     * @return the attribute
     */
    private int SelectRandomAttribute() {
        int sel_atr = 0;
        // random number generator
        Random r = new Random();
        int k = 0;
        int j = 0;
        //check if there is a attribute(s) that has all the instances observed
        for (int i = 0; i < data[0].length; i++) {
            if (tot_mis_atr[i] == 0) {
                k++;
            }
        }
        //if there is a attribute(s) that has all instances observed
        // then perform hot deck  on one of the these attributes randomly choosed
        if (k != 0) {

            while ((j < data[0].length)) {
                System.out.println("data[0] length :" + data[0].length);
                j = 0 + r.nextInt(data[0].length);
                System.out.println("Random j is :" + j);
                // if j th attribute has all instancess observed then
                // perform sorting o this attribute
                if (tot_mis_atr[j] == 0) {
                    // select j to perform hot deck
                    sel_atr = j;
                    j = data[0].length + 1;// to exit the while loop
                }
            }
        } // if there is no attribute that has all instances observed
        // choose randomly to perform hot deck
        else {
            // attribute to perform hot deck
            j = 0 + r.nextInt(data[0].length);
            sel_atr = j;
        }

        return sel_atr;
    }

    /**
     * impute the missing values according to the class that instances belongs
     */
    public void impute() {
        double valuef, valueb;
        // replace missing values of first instance 
        for (int j = 0; j < data[0].length; j++) {
            if (data[0][j] == 0) {
                //go forward to fill it
                valuef = moveforward(0, j);
                data[0][j] = valuef / countf;
            }
        }
        // replace the missing values of i th instance
        for (int i = 1; i < data.length - 1; i++) {
            for (int j = 0; j < data[0].length; j++) {
                // if the attribute j th of instance i is missing then perform imputation
                if (data[i][j] == 0) {
                    //if i th instance b/w same classes according to the selected attribute
                    //the go forward and go back 
                    if (data[i - 1][selected_atr] == data[i + 1][selected_atr]) {
                        valuef = moveforward(i, j);
                        System.out.println("count f: " + countf);

                        valueb = moveback(i, j);
                        System.out.println("count b: " + countb);
                        data[i][j] = (valuef + valueb) / (countf + countb);
                    } else {
                        //if  i th instance belongs to the previous clas then go  back to fill it.
                        if (data[i - 1][selected_atr] == data[i][selected_atr]) {
                            valueb = moveback(i, j);
                            data[i][j] = (valueb) / (countb);
                        } else {
                            //if i th instance belongs to the next class then go forward to fill it.
                            if (data[i + 1][selected_atr] == data[i][selected_atr]) {
                                valuef = moveforward(i, j);
                                data[i][j] = (valuef) / (countf);
                            } else {
                                //if i th instance does not belong to either previous and next classes 
                                // go back and go forward
                                valuef = moveforward(i, j);
                                valueb = moveback(i, j);
                                data[i][j] = (valuef + valueb) / (countf + countb);
                            }
                        }

                    }
                }
            }
        }
        // replace missing values of the last instances
        for (int j = 0; j < data[0].length; j++) {
            if (data[data.length - 1][j] == 0) {
                //go back to fill it
                valueb = moveback(data.length - 1, j);
                data[data.length - 1][j] = valueb / countb;
            }
        }

        Arrays.sort(data, new ColumnComparator(0, SortingOrder.ASCENDING));
    }

    /**
     * this method goes back through same class
     *
     * @param i from which instance goes back
     * @param j the attribute to be filled
     * @return
     */
    private double moveback(int i, int j) {
        double value = 0;
        countb = 1;
        int b = i - 1;
        value = value + data[b][j];
        while (((b - 1) >= 0) && (data[b][selected_atr] == data[b - 1][selected_atr])) {
            b--;
            if (data[b][j] != 0) {
                value = value + data[b][j];
                countb++;
            }

        }
        return value;
    }

    /**
     * this method goes forward through same class
     *
     * @param i from which instance goes forward
     * @param j
     * @return
     */
    private double moveforward(int i, int j) {
        double value = 0;
        countf = 0;
        int f = i + 1;
        //value = value + data[f][j];
        System.out.println("f is: " + f);

        while ((f < data.length - 1) && (data[f][selected_atr] == data[f + 1][selected_atr])) {

            if (data[f][j] != 0) {
                value = value + data[f][j];
                countf++;
            }
            f++;
        }

        if ((f <= data.length - 1) && (data[f][j] != 0)) {
            value = value + data[f][j];
            countf++;
        }

        if (j == selected_atr) {
            // if j==selected attribute and it is missing the perform this special case
            if ((f <= data.length - 1) && (data[f][j] == 0)) {
                f++;
                while ((f < data.length - 1) && (data[f][selected_atr] == data[f + 1][selected_atr])) {
                    if (data[f][j] != 0) {
                        value = value + data[f][j];
                        countf++;
                    }
                    f++;
                }
                if ((f <= data.length - 1) && (data[f][j] != 0)) {
                    value = value + data[f][j];
                    countf++;
                }
            }
        }

        return value;
    }

    private void findmodeofnextclass(int row, int column, int max) {
        int f = row + 1;
        int[] frequency = new int[max + 1];

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

    public void RunHotDeck(String filename, String outputfile) throws Exception {
        char c;
        Scanner sc = new Scanner(System.in);
        read_data(filename);
        prepareData();
        
        System.out.println("Would you like to select the atribute to sort the data?[true/false]");
        select_atr = sc.nextBoolean();
        System.out.println(select_atr);
        if (select_atr) {
            System.out.println("Select attribute");

            selected_atr = sc.nextInt();
        }
        else{
            // if no attribute is selected to sort the data initial is 0
            selected_atr = 0;
        }
        System.out.println("Selected attribute is: " + selected_atr);
        //sort the data
        Sort();
        //impute the missing values according to the choosen attrbiute
        impute();
        //write values to the file
        generateCSVFile(outputfile);
        System.out.println("After Imputation");

        for (double[] data1 : data) {
            for (int t = 0; t < data[0].length; t++) {
                System.out.print(data1[t] + "\t");
            }
            System.out.print("\n");
        }

    }

    public static void main(String[] args) throws Exception {

        String filename = "C:\\Users\\Sinan\\Desktop\\Tests\\Gamma\\DIM128_gamma_30mv.csv";
        String output = "C:\\Users\\Sinan\\Desktop\\test_em_10_gamma_newwwww.csv";

        HotDeck hd = new HotDeck();
        hd.RunHotDeck(filename, output);

    }

}
