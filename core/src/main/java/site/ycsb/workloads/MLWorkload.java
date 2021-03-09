package site.ycsb.workloads;

import site.ycsb.*;
import site.ycsb.generator.*;

import site.ycsb.bayes.*;
import site.ycsb.knn.*;
import site.ycsb.svm.*;
import site.ycsb.rf.*;

import java.util.*;


import java.io.*;
/**
 * The name of the database table to run queries against.
 */
public class MLWorkload extends CoreWorkload {
  public static final String ALGORITHM = "algorithm";
  public static final String BAYES= "bayes";
  public static final String KNN= "knn";
  public static final String SVM= "svm";
  public static final String RF="random forest";
  public static final String TRAIN="ttrain";
  public static final String TEST="ttest";
  public static final String BAYES_TRAIN_PATH="data/MachineLearning/bayes/adult.data";
  public static final String BAYES_TEST_PATH="data/MachineLearning/bayes/adult1000.test";
  public static final String KNN_TRAIN_PATH="data/MachineLearning/knn/trainingsample.csv";
  public static final String KNN_TEST_PATH="data/MachineLearning/knn/validationsample.csv";
  public static final String KNN_PROPERTIES_PATH="data/MachineLearning/knn/KnnProperties.txt";
  public static final String SVM_TRAIN_PATH="data/MachineLearning/svm/train_bc";
  public static final String SVM_TEST_PATH="data/MachineLearning/svm/test_bc";
  public static final String RF_TRAIN_PATH="data/MachineLearning/rf/data.txt";
  public static final String RF_TEST_PATH="data/MachineLearning/rf/test.txt";
  
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
    case BAYES:
      r=doInsertFromFile(db, BAYES_TRAIN_PATH, algorithmName+TRAIN, ",")&&
          doInsertFromFile(db, BAYES_TEST_PATH, algorithmName+TEST, ",");
      break;
    case KNN:
      r=doInsertFromFile(db, KNN_TRAIN_PATH, algorithmName+TRAIN, ",")&&
          doInsertFromFile(db, KNN_TEST_PATH, algorithmName+TEST, ",");
      break;
    case SVM:
      r=doInsertFromFile(db, SVM_TRAIN_PATH, algorithmName+TRAIN, " ")&&
          doInsertFromFile(db, SVM_TEST_PATH, algorithmName+TEST, " ");
      break;
    case RF:
      r=doInsertFromFile(db, RF_TRAIN_PATH, algorithmName+TRAIN, " ")&&
          doInsertFromFile(db, RF_TEST_PATH, algorithmName+TEST, " ");
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
    case BAYES:
      doTransactionBayes(db);
      break;
    case KNN:
      doTransactionKnn(db);
      break;
    case SVM:
      doTransactionSvm(db);
      break;
    case RF:
      doTransactionRf(db);
      break;
    default:
      break;
    }
    return true;
  }
  
  public boolean doTransactionBayes(DB db) {
    ArrayList<ArrayList<String>> trainList=new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> testList=new ArrayList<ArrayList<String>>();
    
    for(int i=0;; i++) {
      String keyname=String.valueOf(i);
      HashMap<String, ByteIterator> cells = new HashMap<String, ByteIterator>();
      if(db.read(tableTrain, keyname, null, cells)!=Status.OK) {
        break;
      }
      ArrayList<String> tlist=new ArrayList<String>();
      for(int j=0; j<cells.size(); j++) {
        tlist.add(cells.get(String.valueOf(j)).toString());
      }
      trainList.add(tlist);
    }
    
    for(int i=0;; i++) {
      String keyname=String.valueOf(i);
      HashMap<String, ByteIterator> cells = new HashMap<String, ByteIterator>();
      if(db.read(tableTest, keyname, null, cells)!=Status.OK) {
        break;
      }
      ArrayList<String> tlist=new ArrayList<String>();
      for(int j=0; j<cells.size(); j++) {
        tlist.add(cells.get(String.valueOf(j)).toString());
      }
      testList.add(tlist);
    }
    PreRead convert = new PreRead();
    Bayes bayes = new Bayes();
    ArrayList<ArrayList<String>> pTrainList=convert.process(trainList, true);
    ArrayList<ArrayList<String>> pTestList=convert.process(testList, true);
    double wrongNumber=0;
    for(int i = 0; i < pTestList.size(); i++){
      ArrayList<String> tmp = new ArrayList<String>();
      tmp = pTestList.get(i);
      String label = tmp.get(tmp.size()-1);
      tmp.remove(tmp.size() - 1);
      String r = bayes.predictClass(pTrainList, tmp);
      
      if(!label.equals(r)){
        wrongNumber++;
      }
    }
    System.out.println("total:"+pTestList.size());
    System.out.println("rate:"+100*(1-wrongNumber/pTestList.size())+"%");
    
    return true;
  }

  public boolean doTransactionKnn(DB db) {
    List<Sample> trainList = new ArrayList<Sample>();
    List<Sample> testList = new ArrayList<Sample>();

    for(int i=1;; i++) {
      String keyname=String.valueOf(i);
      HashMap<String, ByteIterator> cells = new HashMap<String, ByteIterator>();
      if(db.read(tableTrain, keyname, null, cells)!=Status.OK) {
        break;
      }
      Sample sample = new Sample();
      try {
        sample.setLabel(Integer.parseInt(cells.get(String.valueOf(0)).toString()));
      }catch(NumberFormatException n) {
        //System.out.println("??"+i);
        //n.printStackTrace();
        sample.setLabel(0);
      }
      sample.setPixels(new int[cells.size() - 1]);
      for(int j = 1; j < cells.size(); j++) {
        try {
          sample.getPixels()[j-1] = Integer.parseInt(cells.get(String.valueOf(j)).toString());
        }catch(NumberFormatException n) {
          //System.out.println("???"+i+" "+j);
          //n.printStackTrace();
          sample.getPixels()[j-1] = 0;
        }
      }
      trainList.add(sample);
    }
    for(int i=1;; i++) {
      String keyname=String.valueOf(i);
      HashMap<String, ByteIterator> cells = new HashMap<String, ByteIterator>();
      if(db.read(tableTest, keyname, null, cells)!=Status.OK) {
        break;
      }
      Sample sample = new Sample();
      try {
        sample.setLabel(Integer.parseInt(cells.get(String.valueOf(0)).toString()));
      }catch(NumberFormatException n) {
        //System.out.println("??a"+i);
        //n.printStackTrace();
        sample.setLabel(0);
      }
      sample.setPixels(new int[cells.size() - 1]);
      for(int j = 1; j < cells.size(); j++) {
        try {
          sample.getPixels()[j-1] = Integer.parseInt(cells.get(String.valueOf(j)).toString());
        }catch(NumberFormatException n) {
          //System.out.println("???a"+i+" "+j);
          //n.printStackTrace();
          sample.getPixels()[j-1] = 0;
        }
      }
      testList.add(sample);
    }
    
    int numCorrect = 0;
    for(Sample sample:testList) {
      if(Knn.classify(trainList, sample.getPixels()) == sample.getLabel()) {
        numCorrect++;
      }
    }
    System.out.println("Accuracy: " + (double)numCorrect / testList.size() * 100 + "%");
    return true;
  }
  
  public boolean doTransactionSvm(DB db) {
    double[] y = new double[400]; 
    double[][] x = new double[400][11]; 
    double[] testy = new double[283]; 
    double[][] testx = new double[283][11]; 
    
    for(int i=0;; i++) {
      String keyname=String.valueOf(i);
      HashMap<String, ByteIterator> cells = new HashMap<String, ByteIterator>();
      if(db.read(tableTrain, keyname, null, cells)!=Status.OK) {
        break;
      }
      y[i]=Double.parseDouble(cells.get(String.valueOf(0)).toString());
      for(int j = 1; j < cells.size(); j++) {
        String [] line=cells.get(String.valueOf(j)).toString().split(":");
        x[i][Integer.parseInt(line[0])]
            =Double.parseDouble(line[1]);
      }
    }
    
    for(int i=0;; i++) {
      String keyname=String.valueOf(i);
      HashMap<String, ByteIterator> cells = new HashMap<String, ByteIterator>();
      if(db.read(tableTest, keyname, null, cells)!=Status.OK) {
        break;
      }
      testy[i]=Double.parseDouble(cells.get(String.valueOf(0)).toString());
      for(int j = 1; j < cells.size(); j++) {
        String [] line=cells.get(String.valueOf(j)).toString().split(":");
        testx[i][Integer.parseInt(line[0])]
            =Double.parseDouble(line[1]);
      }
    }
    
    SimpleSvm svm = new SimpleSvm(0.0001); 
    svm.train(x, y, 7000); 
    svm.test(testx,  testy); 
    return true;
  }
  public boolean doTransactionRf(DB db) {
    int numTrees = 100; 

    ArrayList<int[]> trainList = new ArrayList<int[]>();
    ArrayList<int[]> testList = new ArrayList<int[]>();
    
    for(int i=0;; i++) {
      String keyname=String.valueOf(i);
      HashMap<String, ByteIterator> cells = new HashMap<String, ByteIterator>();
      if(db.read(tableTrain, keyname, null, cells)!=Status.OK) {
        break;
      }
      int [] data=new int [cells.size()];
      for(int j=0; j<cells.size(); j++) {
        data[j]=Integer.parseInt(cells.get(String.valueOf(j)).toString());
      }
      trainList.add(data);
    }
    
    for(int i=0;; i++) {
      String keyname=String.valueOf(i);
      HashMap<String, ByteIterator> cells = new HashMap<String, ByteIterator>();
      if(db.read(tableTest, keyname, null, cells)!=Status.OK) {
        break;
      }
      int [] data=new int [cells.size()];
      for(int j=0; j<cells.size(); j++) {
        data[j]=Integer.parseInt(cells.get(String.valueOf(j)).toString());
      }
      testList.add(data);
    }
    int categ = 0; 
    
    //the num of labels
    int trainLength = trainList.get(0).length; 
    for(int k=0;  k<trainList.size();  k++){
      if(trainList.get(k)[trainLength-1] < categ){
        continue; 
      }else{
        categ = trainList.get(k)[trainLength-1]; 
      }
    }

    RandomForest rf = new RandomForest(numTrees,  trainList,  testList); 
    rf.setC(categ); //the num of labels
    rf.setM(trainList.get(0).length-1); //the num of Attr
    //属性扰动，每次从M个属性中随机选取Ms个属性，Ms = ln(m)/ln(2)
    rf.setMs((int)Math.round(Math.log(rf.getM())/Math.log(2) + 1)); //随机选择的属性数量
    rf.start(); 
    return true;
  }
}
