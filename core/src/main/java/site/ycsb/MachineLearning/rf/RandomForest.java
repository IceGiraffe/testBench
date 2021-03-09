package site.ycsb.rf;


import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit; 
/**
 * Random Forest.
 */
public class RandomForest {

  /**
   * 可用的线程数量.
   * */
  private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors(); 
  /** 
   *target类别数量.
   * */
  private static int cC; 
  /** 
   * 属性（列）的数量.
   * */
  private static int mM; 
  /** 
   *属性扰动，每次从M个属性中随机选取Ms个属性，mMs = log2(mM).
   */
  private static int mMs; 
  /** the collection of the forest's decision trees. */
  private ArrayList<DTree> trees; 
  /** 
   * 开始时间.
   * */
  private long timeO; 
  /** the number of trees in this random tree. */
  private int numTrees; 
  /** 
   * 为了实时显示进度，每建立一棵树的更新量.
   */
  private double update; 
  /**
   * 为了实时显示进度，初试值.
   */
  private double progress; 
  /** importance Array . */
  private int[] importances; 
  /** key = a record from data matrix.
   * value = RF的分类结果*/
  private HashMap<int[], int[]> estimateOOB; 
  /** all of the predictions from RF. */
  private ArrayList<ArrayList<Integer>> prediction; 
  /** RF的错误率. */
  private double error; 
  /** 控制树生长的进程池. */
  private ExecutorService treePool; 
  /** 原始训练数据. */
  private ArrayList<int[]> trainData; 
  /** 测试数据.*/
  private ArrayList<int[]> testdata; 
  /**
   * Initializes a Random forest.
   * @param numTrees    RF的数量
   * @param trainData  原始训练数据
   * @param tData    测试数据
   */
  public RandomForest(int numTrees,  ArrayList<int[]> trainData,  ArrayList<int[]> tData){
    this.numTrees = numTrees; 
    this.trainData = trainData; 
    this.testdata = tData; 
    trees = new ArrayList<DTree>(numTrees); 
    update = 100 / ((double)numTrees); 
    progress = 0; 
    startTimer(); 
    System.out.println("creating "+numTrees+" trees in a random Forest. . ."); 
    System.out.println("total data size is "+trainData.size()); 
    System.out.println("number of attributes " + (trainData.get(0).length-1)); 
    System.out.println("number of selected attributes "
        + ((int)Math.round(Math.log(trainData.get(0).length-1)/Math.log(2) + 1))); 
    estimateOOB = new HashMap<int[], int[]>(trainData.size()); 
    prediction = new ArrayList<ArrayList<Integer>>(); 
  }
  /**
   * Begins the creation of random forest.
   */
  public void start() {
    System.out.println("Num of threads started : " + NUM_THREADS); 
    System.out.println("Running..."); 
    treePool = Executors.newFixedThreadPool(NUM_THREADS); 
    for (int t=0;  t < numTrees;  t++){
      System.out.println("structing " + t + " Tree"); 
      treePool.execute(new CreateTree(trainData, this, t+1)); 
      //System.out.print("."); 
    }
    treePool.shutdown(); 
    try {
      treePool.awaitTermination(Long.MAX_VALUE,  TimeUnit.SECONDS);  //effectively infinity
    } catch (InterruptedException ignored){
      System.out.println("interrupted exception in Random Forests"); 
    }
    System.out.println(""); 
    System.out.println("Finished tree construction"); 
    testForest(trees,  testdata); 
    calcImportances(); 
    System.out.println("Done in "+timeElapsed(timeO)); 
  }

  /**
   * @param collecTree   the collection of the forest's decision trees.
   * @param testData    测试数据集
   */
  private void testForest(ArrayList<DTree> collecTree,  ArrayList<int[]> testData) {
    int correstness = 0;
    int k = 0; 
    ArrayList<Integer> actualLabel = new ArrayList<Integer>(); 
    for(int[] rec:testData){
      actualLabel.add(rec[rec.length-1]); 
    }
    int treeNumber = 1; 
    for(DTree dt:collecTree){
      dt.calculateClasses(testData,  treeNumber); 
      prediction.add(dt.getPredictions()); 
      treeNumber++; 
    }
    for(int i = 0;  i<testData.size();  i++){
      ArrayList<Integer> vval = new ArrayList<Integer>(); 
      for(int j =0;  j<collecTree.size();  j++){
        vval.add(prediction.get(j).get(i)); //The collection of each Tree's prediction in i-th record
      }
      int pred = labelVote(vval); //Voting algorithm
      if(pred == actualLabel.get(i)){
        correstness++; 
      }
    }
    System.out.println("Accuracy of Forest is : " + (100 * correstness / testData.size()) + "%"); 
  }

  /**
   * Voting algorithm.
   * @param treePredict   The collection of each Tree's prediction in i-th record
   */
  private int labelVote(ArrayList<Integer> treePredict){
    // TODO Auto-generated method stub
    int max=0,  maxclass=-1; 
    for(int i=0;  i<treePredict.size();  i++){
      int count = 0; 
      for(int j=0;  j<treePredict.size();  j++){
        if(treePredict.get(j) == treePredict.get(i)){
          count++; 
        }
        if(count > max){
          maxclass = treePredict.get(i); 
          max = count; 
        }
      }
    }
    return maxclass; 
  }
  /**
   * 计算RF的分类错误率.
   */
  private void calcErrorRate(){
    double n=0; 
    int correct=0; 
    for (int[] record:estimateOOB.keySet()){
      n++; 
      int[] map=estimateOOB.get(record); 
      int cclass=findMaxIndex(map); 
      if (cclass == DTree.getClass(record)){
        correct++; 
      }
    }
    error=1-correct/n; 
    System.out.println("correctly mapped "+correct); 
    System.out.println("Forest error rate % is: "+(error*100)); 
  }
  /**
   * 更新  OOBEstimate.
   * @param record      a record from data matrix
   * @param cclass  
   */
  public void updateOOBEstimate(int[] record,  int cclass){
    if (estimateOOB.get(record) == null){
      int[] map = new int[getC()]; 
      //System.out.println("class of record : "+cclass); map[cclass-1]++; 
      estimateOOB.put(record, map); 
    }else {
      int[] map = estimateOOB.get(record); 
      map[cclass-1]++; 
    }
  }
  /**
   * calculates the importance levels for all attributes.
   */
  private void calcImportances() {
    importances = new int[getM()]; 
    for (DTree tree:trees){
      for (int i=0;  i<getM();  i++){
        importances[i] += tree.getImportanceLevel(i); 
      }
    }
    for (int i=0; i<getM(); i++){
      importances[i] /= numTrees; 
    }
    System.out.println("The forest-wide importance as follows:"); 
    for (int j=0;  j<importances.length;  j++){
      System.out.println("Attr" + j + ":" + importances[j]); 
    }
  }
  /** 计时开始. */
  private void startTimer(){
    timeO = System.currentTimeMillis(); 
  }
  /**
   * 创建一棵决策树.
   */
  private class CreateTree implements Runnable{ 
    /** 训练数据. */
    private ArrayList<int[]> trainData; 
    /** 随机森林. */
    private RandomForest forest; 
    /** the numb of RF. */
    private int treenum; 
   
    public CreateTree(ArrayList<int[]> trainData,  RandomForest forest,  int num){
      this.trainData = trainData; 
      this.forest = forest; 
      this.treenum = num; 
    }
    /**
     * Create the decision tree.
     */
    public void run() {
      System.out.println("Creating a Dtree num : " + treenum + " "); 
      trees.add(new DTree(trainData,  forest,  treenum)); 
      //System.out.println("tree added in RandomForest.AddTree.run()"); 
      progress += update; 
      System.out.println("---progress:" + progress); 
    }
  }

  /**
   * Evaluates testdata.
   * @param record  a record to be evaluated
   */
  public int evaluate(int[] record){
    int[] counts=new int[getC()]; 
    for (int t=0; t<numTrees; t++){
      int cclass=(trees.get(t)).evaluate(record); 
      counts[cclass]++; 
    }
    return findMaxIndex(counts); 
  }
  
  public static int findMaxIndex(int[] arr){
    int index=0; 
    int max = Integer.MIN_VALUE; 
    for (int i=0; i<arr.length; i++){
      if (arr[i] > max){
        max=arr[i]; 
        index=i; 
      }
    }
    return index; 
  }
  

  /**
   * @param timeinms      开始时间.
   * @return    the hr, min, s 
   */
  private static String timeElapsed(long timeinms){
    int s=(int)(System.currentTimeMillis()-timeinms)/1000; 
    int h=(int)Math.floor(s/((double)3600)); 
    s-=(h*3600); 
    int m=(int)Math.floor(s/((double)60)); 
    s-=(m*60); 
    return ""+h+"hr "+m+"m "+s+"s"; 
  }
  public static int getC() {
    return cC;
  }
  public static void setC(int c) {
    cC = c;
  }
  public static int getM() {
    return mM;
  }
  public static void setM(int m) {
    mM = m;
  }
  public static int getMs() {
    return mMs;
  }
  public static void setMs(int ms) {
    mMs = ms;
  }
}
