/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributions;

/**
 *
 * @author Sinan
 * this class generate unique random numbers to perform 
 * developed distribution methods
 */
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class RandomNumberGenerate {

    public RandomNumberGenerate() {

    }

    /**
     * Pick n numbers between 0 (inclusive) and k (inclusive) While there are
     * very deterministic ways to do this, for large k and small n, this could
     * be easier than creating an large array and sorting, i.e. k = 10,000
     */
    public Set<Integer> pickRandom(int n, int k) {
        Random random = new Random(); // if this method is used often, perhaps define random at class level
        Set<Integer> picked = new HashSet<>();
        while (picked.size() < n) {
            picked.add(random.nextInt(k + 1));
        }
        return picked;
    }

    public static void main(String[] args) throws Exception {
        RandomNumberGenerate rd = new RandomNumberGenerate();
        Set<Integer> picked = new HashSet<>();
        picked = rd.pickRandom(10, 440);
        int[] arr = new int[picked.size()];

        // create an iterator
        Iterator iterator = picked.iterator();

        // check values
        int k = 0;
        while (iterator.hasNext()) {
            //System.out.println("Value: "+iterator.next() + " "); 
            arr[k] = (int) iterator.next();
            System.out.println(arr[k]);
            k++;
        }
    }
}

