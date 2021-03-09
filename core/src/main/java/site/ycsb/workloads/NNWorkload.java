package site.ycsb.workloads;

import site.ycsb.*;
import site.ycsb.generator.*;

import java.util.*;
import site.ycsb.ANN.*;
import site.ycsb.CNN.*;
import site.ycsb.SOMcluster.*;
import site.ycsb.CNN.CNN.LayerBuilder;
import site.ycsb.CNN.Layer.Size;


import java.io.*;
/**
 * The name of the database table to run queries against.
 */
public class NNWorkload extends CoreWorkload {
  public static final String ALGORITHM = "algorithm";
  public static final String ANN= "ANN";
  public static final String CNN= "CNN";
  public static final String SOM= "SOMcluster";

  public static final String TRAIN="ttrain";
  public static final String TEST="ttest";
  public static final String ANN_TRAIN_PATH="data/NeuralNetwork/ANN/train.txt";
  public static final String ANN_TEST_PATH="data/NeuralNetwork/ANN/test.txt";
  public static final String CNN_TRAIN_PATH="data/NeuralNetwork/CNN/train.format";
  public static final String CNN_TEST_PATH="data/NeuralNetwork/CNN/test.format";
  public static final String SOM_TRAIN_PATH="data/NeuralNetwork/SOMcluster/iris.csv";

  public static final String ANNOUTPUT="ann.txt";
  public static final String CNNOUTPUT="cnn.txt";
  public static final String CNNMODEL="cnnModel.txt";
  
  private String algorithmName;
  private String tableTrain;
  private String tableTest;
  /**
   * Initialize the scenario.
   * Called once, in the main client thread, before any operations are started.
   */
  @Override
  public void init(Properties p) throws WorkloadException {
    algorithmName=p.getProperty(ALGORITHM);
    tableTrain=algorithmName+TRAIN;
    tableTest=algorithmName+TEST;
  }
  
  /**
   * Do one insert operation. Because it will be called concurrently from multiple client threads,
   * this function must be thread safe. However, avoid synchronized, or the threads will block waiting
   * for each other, and it will be difficult to reach the target throughput. Ideally, this function would
   * have no side effects other than DB operations.
   */
  @Override
  public boolean doInsert(DB db, Object threadstate) {
    boolean r=true;
    switch(algorithmName) {
    case ANN:
      r=doInsertFromFile(db, ANN_TRAIN_PATH, tableTrain, ",")&&
          doInsertFromFile(db, ANN_TEST_PATH, tableTest, ",");
      break;
    case CNN:
      r=doInsertFromFile(db, CNN_TRAIN_PATH, tableTrain, ",")&&
          doInsertFromFile(db, CNN_TEST_PATH, tableTest, ",");
      break;
    case SOM:
      r=doInsertFromFile(db, SOM_TRAIN_PATH, tableTrain, ",");
      break;
    default:
      break;
    }
    return r;
  }
  
  public boolean doInsertFromFile(DB db, String path, String table, String sp) {
    FileInputStream fileInputStream = null; 
    Scanner scanner = null;
    // Insert data line by line 
    HashMap<String, ByteIterator> values;
    String[] fieldValues;
    ByteIterator data;
    String record;
    Status status = null;
    int numOfRetries=0;
    int count;
    int keyCount=0;
    try {
      fileInputStream = new FileInputStream(path);
      scanner = new Scanner(fileInputStream);
      while (scanner.hasNextLine()) {
        record = String.valueOf(scanner.nextLine());
        System.out.println(record);
        numOfRetries = 0;
        count =0; //dbkey : fieldValues[0]
        
        fieldValues = record.split(sp);
        values = new HashMap<>();
        for (; count < fieldValues.length; count++) {
          if (!fieldValues[count].isEmpty()) {
            data = new StringByteIterator(fieldValues[count]);
            values.put(String.valueOf(count), data);
          }
        }
       /* int keynum = keysequence.nextValue().intValue();
        String dbkey = buildKeyName(keynum);
        values = buildValues(dbkey);*/
        do{
          status = db.insert(table, String.valueOf(keyCount), values);
          if (null != status && status.isOk()) {
            break;
          }
          // Retry if configured. Without retrying, the load process will fail
          // even if one single insertion fails. User can optionally configure
          // an insertion retry limit (default is 0) to enable retry.
          if (++numOfRetries <= insertionRetryLimit) {
            System.err.println("Retrying insertion, retry count: " + numOfRetries);
            try {
              // Sleep for a random number between [0.8, 1.2)*insertionRetryInterval.
              int sleepTime = (int) (1000 * insertionRetryInterval * (0.8 + 0.4 * Math.random()));
              Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
              break;
            }
          } else {
            System.err.println("Error inserting," + numOfRetries + "Insertion Retry Limit: " + insertionRetryLimit);
            break;
          }
        } while (true);
        keyCount++;
      }
      fileInputStream.close();
      scanner.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Fail to read TPC-DS data fiels:1");
    }
    System.out.println("Data have been deleted.");
    return null != status && status.isOk();
  }
  
  /**
   * Do one transaction operation. Because it will be called concurrently from multiple client
   * threads, this function must be thread safe. However, avoid synchronized, or the threads will block waiting
   * for each other, and it will be difficult to reach the target throughput. Ideally, this function would
   * have no side effects other than DB operations.
   */
  @Override
  public boolean doTransaction(DB db, Object threadstate) {
    switch(algorithmName) {
    case ANN:
      doTransactionAnn(db);
      break;
    case CNN:
      doTransactionCnn(db);
      break;
    case SOM:
      doTransactionSom(db);
      break;
    default:
      break;
    }
    return true;
  }
  
  public boolean doTransactionAnn(DB db) {
    float eta = 0.02f;
    int nIter = 1000;
    System.out.println("The output file is "+ANNOUTPUT);
    DataUtil util = DataUtil.getInstance();
    List<DataNode> trainList = util.getDataList(db, tableTrain);
    List<DataNode> testList = util.getDataList(db, tableTest);
    //List<DataNode> trainList = util.getDataList(ANN_TRAIN_PATH, ",");
    //List<DataNode> testList = util.getDataList(ANN_TEST_PATH, ",");
    try {
      BufferedWriter output = new BufferedWriter(new FileWriter(new File(ANNOUTPUT)));
      int typeCount = util.getTypeCount();
      AnnClassifier annClassifier = new AnnClassifier(trainList.get(0)
          .getAttribList().size(), trainList.get(0).getAttribList()
          .size() + 8, typeCount);
      annClassifier.setTrainNodes(trainList);
      annClassifier.train(eta, nIter);
      for (int i = 0; i < testList.size(); i++){
        DataNode test = testList.get(i);
        int type = annClassifier.test(test);
        List<Float> attribs = test.getAttribList();
        for (int n = 0; n < attribs.size(); n++){
          output.write(attribs.get(n) + ",");
          output.flush();
        }
        output.write(util.getTypeName(type) + "\n");
        output.flush();
      }
      output.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }
  public boolean doTransactionCnn(DB db) {
    LayerBuilder builder = new LayerBuilder();
    builder.addLayer(Layer.buildInputLayer(new Size(28, 28)));
    builder.addLayer(Layer.buildConvLayer(6, new Size(5, 5)));
    builder.addLayer(Layer.buildSampLayer(new Size(2, 2)));
    builder.addLayer(Layer.buildConvLayer(12, new Size(5, 5)));
    builder.addLayer(Layer.buildSampLayer(new Size(2, 2)));
    builder.addLayer(Layer.buildOutputLayer(10));
    CNN cnn = new CNN(builder, 50);
    
    //导入数据集
    Dataset dataset = Dataset.loadFromDB(db, tableTrain, 784);
    cnn.train(dataset, 3); //
    cnn.saveModel(CNNMODEL);    
    dataset.clear();
    dataset = null;
    
    //预测
    // CNN cnn = CNN.loadModel(modelName);  
    Dataset testset = Dataset.loadFromDB(db, tableTest, -1);
    cnn.predict(testset, CNNOUTPUT);
    return true;
  }
  public boolean doTransactionSom(DB db) {
    List<Iris> train = DatasetReader.readDB(db, tableTrain);
    double[][] datafeartures = new double[train.size()][4];
    int i = 0;
    for (Iris iris : train) {
        double[] features = {iris.getSepalLength(), iris.getSepalWidth(), iris.getPetalLength(), iris.getPetalWidth()};
        datafeartures[i] = features;
        i++;
    }
    int xdim = 2;
    int ydim = 2;
    SOM training = new SOM(datafeartures, xdim, ydim, 10000);
    training.train();;

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
    Set<Integer> unique = new HashSet<>(list);// container of nodes (clusters 0,1,2,3)
    for (Integer key : unique) {
        System.out.println("Cluster " + key + 
                " has number of instances " + Collections.frequency(list, key) 
                + ", Setosa " + nodecontains[key][0]
                + " Versicolor " + nodecontains[key][1]
                + " Virginica " + nodecontains[key][2]);
    }
    return true;
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
