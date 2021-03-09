package site.ycsb.SOMcluster;


import java.util.ArrayList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Taha Emara 
 * Website: http://www.emaraic.com 
 * Email : taha@emaraic.com
 * Created on: Nov 22, 2017
 */
public final class IrisClusterer {
  private IrisClusterer(){
  }
  private static String trainData = "C:\\Users\\69494\\Downl";
  private static int xdim = 2;
  private static int ydim = 2;

  public static void main(String[] args) {
    List<Iris> train = DatasetReader.readFile(trainData);
    double[][] datafeartures = new double[train.size()][4];
    int i = 0;
    for (Iris iris : train) {
      double[] features = {iris.getSepalLength()
          , iris.getSepalWidth(), iris.getPetalLength(), iris.getPetalWidth()};
      datafeartures[i] = features;
      i++;
    }

    SOM training = new SOM(datafeartures, xdim, ydim, 10000);
    training.train();

    int[] nodes = training.getNodes();
    List list = new ArrayList<>();

    System.out.println(nodes.length);
    i = 0;
    for (int node : nodes) {
      list.add(node);
      System.out.println(i + " " + node);
      i++;
    }
    int[][] nodecontains = new int[xdim * ydim][3];
    for (int j = 0; j < xdim * ydim; j++) {
      ArrayList<Integer> indexes = indexOfAll(j, list);
      int setosacounter = 0;
      int versicolorcounter = 0;
      int virginicacounter = 0;
      for (Integer index : indexes) {// setosa 0 to 49 // versicolor 50 to 99 // virginica 100 149
        if (index >= 0 && index <= 49) {
          setosacounter++;
        } else if (index >= 50 && index <= 99) {
          versicolorcounter++;
        } else if (index >= 100 && index <= 149) {
          virginicacounter++;
        }
      }
      int[] contains = {setosacounter, versicolorcounter, virginicacounter};
      nodecontains[j] = contains;

    }
    //calculate the occarence of each cluster 
    Set<Integer> unique = new HashSet<>(list); // container of nodes (clusters 0,1,2,3)
    for (Integer key : unique) {
      System.out.println("Cluster " + key + 
          " has number of instances " + Collections.frequency(list, key) 
          + ", Setosa " + nodecontains[key][0]
          + " Versicolor " + nodecontains[key][1]
          + " Virginica " + nodecontains[key][2]);
    }

  }

  static ArrayList<Integer> indexOfAll(Object obj, List list) {
    ArrayList<Integer> indexList = new ArrayList<Integer>();
    for (int i = 0; i < list.size(); i++) {
      if (obj.equals(list.get(i))) {
        indexList.add(i);
      }
    }
    return indexList;
  }
}
