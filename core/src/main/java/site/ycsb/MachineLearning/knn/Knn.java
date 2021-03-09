package site.ycsb.knn;

import site.ycsb.*;
import site.ycsb.generator.*;
import java.io.*;
import java.util.*;
/**
 * 
 */
public final class Knn {
  private Knn() {
    
  }
  private static List<Sample> readFile(String file) throws IOException {
    List<Sample> samples = new ArrayList<Sample>();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    try {
      String line = reader.readLine(); // ignore header
      while((line = reader.readLine()) != null) {
        String[] tokens = line.split(",");
        Sample sample = new Sample();
        try {
          sample.setLabel(Integer.parseInt(tokens[0]));
        }catch(NumberFormatException n) {
          n.printStackTrace();
          sample.setLabel(0);
        }
        
        sample.setPixels(new int[tokens.length - 1]);
        for(int i = 1; i < tokens.length; i++) {
          try {
            sample.getPixels()[i-1] = Integer.parseInt(tokens[i]);
          }catch(NumberFormatException n) {
            n.printStackTrace();
            sample.getPixels()[i-1] = 0;
          }
        }
        samples.add(sample);
      } 
    } finally { 
      reader.close(); 
    }
    return samples;
  }
  
  private static int distance(int[] a, int[] b) {
    int sum = 0;
    for(int i = 0; i < a.length; i++) {
      sum += (a[i] - b[i]) * (a[i] - b[i]);
    }
    return (int)Math.sqrt(sum); // euclidian distance would be sqrt(sum)...
  }
  
  public static int classify(List<Sample> trainingSet, int[] pixels) {
    int label = 0, bestDistance = Integer.MAX_VALUE;
    for(Sample sample: trainingSet) {
      int dist = distance(sample.getPixels(), pixels);
      if(dist < bestDistance) {
        bestDistance = dist;
        label = sample.getLabel();
      }
    }
    return label;
  }
  
}