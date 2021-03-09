package site.ycsb.SOMcluster;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import site.ycsb.*;
/**
 *
 * @author Taha Emara 
 * Website: http://www.emaraic.com 
 * Email : taha@emaraic.com
 * Created on: Nov 22, 2017
 */
public final class DatasetReader {
  private DatasetReader(){
  }
  public static List<Iris> readFile(String path) {
    BufferedReader br = null;
    String line = "";
    String cvsSplitBy = ",";
    List<Iris> irises = new ArrayList();
    try {

      br = new BufferedReader(new FileReader(path));
      while ((line = br.readLine()) != null) {
        String[] data = line.split(cvsSplitBy);
        Iris iris = new Iris();
        iris.setSepalLength(new Double(data[0]));
        iris.setSepalWidth(new Double(data[1]));
        iris.setPetalLength(new Double(data[2]));
        iris.setPetalWidth(new Double(data[3]));
        iris.setType(data[4]);
        irises.add(iris);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return irises;
  }
  public static List<Iris> readDB(DB db, String table) {
    List<Iris> irises = new ArrayList();
    for(int i=0;; i++) {
      String keyname=String.valueOf(i);
      HashMap<String, ByteIterator> cells = new HashMap<String, ByteIterator>();
      if(db.read(table, keyname, null, cells)!=Status.OK) {
        break;
      }
      Iris iris = new Iris();
      iris.setSepalLength(new Double(cells.get(String.valueOf(0)).toString()));
      iris.setSepalWidth(new Double(cells.get(String.valueOf(1)).toString()));
      iris.setPetalLength(new Double(cells.get(String.valueOf(2)).toString()));
      iris.setPetalWidth(new Double(cells.get(String.valueOf(3)).toString()));
      iris.setType(cells.get(String.valueOf(4)).toString());
      irises.add(iris);
    }
    return irises;
  }
}
