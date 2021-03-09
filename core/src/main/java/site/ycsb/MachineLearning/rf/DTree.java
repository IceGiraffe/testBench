package site.ycsb.rf;



import java.util.*; 

/**
 * Creates a decision tree based on the specifications of random forest trees.
 */
public class DTree {

  /** Instead of checking each index we'll skip every INDEX_SKIP indices 
   * unless there's less than MIN_SIZE_TO_CHECK_EACH.*/
  private static final int INDEX_SKIP = 2; 
  /** If there's less than MIN_SIZE_TO_CHECK_EACH points,   we'll check each one.*/
  private static final int MIN_SIZE_TO_CHECK_EACH = 10; 
  /** If the number of data points is less than MIN_NODE_SIZE,   
   * we won't continue splitting,   we'll take the majority vote. */
  private static final int MIN_NODE_SIZE=5; 
  /** the number of data records. */
  private int nN; 
  /** the number of samples left out of the boostrap of all nN to test error rate.
   * @see <a href="http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#ooberr">OOB error estimate</a>
   */
  private int testN; 
  /** Of the testN,   the number that were correctly identifie. */
  private int correct; 
  /** an estimate of the importance of each attribute in the data record.
   * @see <a href="http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#varimp">Variable Importance</a>
   */
  private int[] importances; 
  /** This is the root of the Decision Tree. */
  private TreeNode root; 
  /** This is a pointer to the Random Forest this decision tree belongs to. */
  private RandomForest forest; 
  /** This keeps track of all the predictions done by this tree. */
  private ArrayList<Integer> predictions; 

  /**
   * This constructs a decision tree from a data matrix.
   * It first creates a bootstrap sample,   the train data matrix,   as well as the left out records,  
   * the test data matrix. Then it creates the tree,   then calculates the variable importances (not essential)
   * and then removes the links to the actual data (to save memory)
   *
   * @param data  The data matrix as a List of int arrays - each array is one record,  
   *          each index in the array is one attribute,   and the last index is the class
   *       (ie [ x1,   x2,   . . .,   xM,   Y ]).
   * @param forest  The random forest this decision tree belongs to
   * @param num    the Tree number
   */
  public DTree(ArrayList<int[]> data,   RandomForest forest,   int num){
    this.forest = forest; 
    nN = data.size(); 
    importances = new int[RandomForest.getM()]; 
    predictions = new ArrayList<Integer>(); 

    //System.out.println("Make a Dtree num : "+num+" with nN:"+nN+" M:"+RandomForest.M+" Ms:"+RandomForest.Ms); 

    ArrayList<int[]> train = new ArrayList<int[]>(nN);  //data becomes the "bootstrap" - that's all it knows
    ArrayList<int[]> val = new ArrayList<int[]>(); 
    //System.out.println("Creating tree No."+num); 
    bootStrapSample(data,   train,   val,   num); //populates train and test using data
    testN = val.size(); 
    correct = 0; 

    root = createTree(train,   num); //creating tree using training data
    calcTreeVariableImportanceAndError(val,   num); 
    flushData(root); 
  }
  /**
   * Responsible for gauging the error rate of this tree and.
   * calculating the importance values of each attribute
   *
   * @param val  The left out data matrix
   * @param nv  The Tree number
   */
  private void calcTreeVariableImportanceAndError(ArrayList<int[]> val,   int nv) {
    //calculate error rate
    correct = calcTreeErrorRate(val,   nv); //the num of correct prediction record
    calculateClasses(val,   nv); 
    //calculate importance of each attribute
    for (int m=0;  m<RandomForest.getM();  m++){
      ArrayList<int[]> testData = randomlyPermuteAttribute(cloneData(val),   m); 
      int correctAfterPermute = 0; 
      for (int[] arr:testData){
        int preLabel = evaluate(arr); 
        if (preLabel == getClass(arr)){
          correctAfterPermute++; 
        } 
      }
      importances[m] += (correct - correctAfterPermute); 
    }
    System.out.println("The importances of tree " + nv + " as follows"); 
//  for(int m=0;  m<importances.length;  m++){
//    System.out.println(" Attr" + m + ":" + importances[m]); 
//  }
  }

  /**
   * Calculates the tree error rate.
   * displays the error rate to console,  
   * and updates the total forest error rate
   *
   * @param val  the left out test data matrix
   * @param nu  The Tree number
   * @return  the number correct
   */
  public int calcTreeErrorRate(ArrayList<int[]> val,   int nu){
    int correctt = 0; 
    for (int[] record:val){
      int preLabel = evaluate(record); 
      forest.updateOOBEstimate(record,   preLabel); 
      int actualLabel = record[record.length-1]; //actualLabel
      if (preLabel == actualLabel){
        correctt++; 
      }
    }

    double err = 1 - correctt/((double)val.size()); 
//  System.out.print("\n"); 
   // System.out.println("Number of correct  = " + correctt + ",   out of :" + val.size()); 
   // System.out.println("Test-Data error rate of tree " + nu + "  is: " + (err * 100) + " %"); 
    return correctt; 
  }
  /**
   * This method will get the classes and will return the updates.
   * @param val  The left out data matrix
   * @param nu  The Tree number
   */
  public ArrayList<Integer> calculateClasses(ArrayList<int[]> val,   int nu){
    ArrayList<Integer> preList = new ArrayList<Integer>(); 
    int korect = 0; 
    for(int[] record : val){
      int preLabel = evaluate(record); 
      preList.add(preLabel); 
      int actualLabel = record[record.length-1]; 
      if (preLabel==actualLabel){
        korect++; 
      }
    }
    predictions = preList; 
    return preList; 

  }
  /**
   * This will classify a new data record by using tree.
   * recursion and testing the relevant variable at each node.
   *
   * This is probably the most-used function in all of <b>GemIdent</b>.
   * It would make sense to inline this in assembly for optimal performance.
   *
   * @param record   the data record to be classified
   * @return    the class the data record was classified into
   */
  public int evaluate(int[] record){
    TreeNode evalNode = root; 

    while (true){
      if (evalNode.isLeaf){
        return evalNode.cclass; 
      }
      if (record[evalNode.splitAttributeM] <= evalNode.splitValue){
        evalNode = evalNode.left; 
      }else{
        evalNode = evalNode.right; 
      }
    }
  }
  /**
   * Takes a list of data records,   and switches the m-th attribute across data records.
   * This is important in order to test the importance of the attribute. If the attribute
   * is randomly permuted and the result of the classification is the same,   the attribute is
   * not important to the classification and vice versa.
   *
   * @see <a href="http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#varimp">Variable Importance</a>
   * @param val  The left out data matrix to be permuted
   * @param m    The attribute index to be permuted
   * @return    The data matrix with the m-th column randomly permuted
   */
  private ArrayList<int[]> randomlyPermuteAttribute(ArrayList<int[]> val,   int m){
    int num = val.size() * 2; 
    for (int i=0;  i<num;  i++){
      int a = (int)Math.floor(Math.random() * val.size()); 
      int b = (int)Math.floor(Math.random() * val.size()); 
      int[] arrA = val.get(a); 
      int[] arrB = val.get(b); 
      int temp = arrA[m]; 
      arrA[m] = arrB[m]; 
      arrB[m] = temp; 
    }
    return val; 
  }
  /**
   * Creates a copy of the data matrix.
   * @param data  the data matrix to be copied
   * @return    the cloned data matrix
   */
  private ArrayList<int[]> cloneData(ArrayList<int[]> data){
    ArrayList<int[]> clone=new ArrayList<int[]>(data.size()); 
    int mM=data.get(0).length; 
    for (int i=0; i<data.size(); i++){
      int[] arr=data.get(i); 
      int[] arrClone=new int[mM]; 
      for (int j=0; j<mM; j++){
        arrClone[j]=arr[j]; 
      }
      clone.add(arrClone); 
    }
    return clone; 
  }
  /**
   * This creates the decision tree according to the specifications of random forest trees.
   *
   * @see <a href="http://www.stat.berkeley.edu/~breiman/
   * RandomForests/cc_home.htm#overview">Overview of random forest decision trees</a>
   * @param train  the training data matrix (a bootstrap sample of the original data)
   * @param ntree   the tree number
   * @return    the TreeNode object that stores information about the parent node of the created tree
   */
  private TreeNode createTree(ArrayList<int[]> train,   int ntree){
    TreeNode roott = new TreeNode(); 
    roott.data = train;  // public List<int[]> data; 
    //System.out.println("creating "); 
    recursiveSplit(roott,   ntree); 
    return roott; 
  }
  /**
   * @author DEGIS
   */
  private class TreeNode implements Cloneable{
    private boolean isLeaf; 
    private TreeNode left; 
    private TreeNode right; 
    private int splitAttributeM; 
    private Integer cclass; 
    private List<int[]> data; 
    private int splitValue; 
    private int generation; 
    private ArrayList<Integer> attrArr; 

    public TreeNode(){
      splitAttributeM=-99; 
      splitValue=-99; 
      generation=1; 
    }
    public TreeNode clone(){ //"data" element always null in clone
      TreeNode treeCopy = new TreeNode(); 
      treeCopy.isLeaf = isLeaf; 
      if (left != null) {
        treeCopy.left = left.clone(); 
      }
      if (right != null) {
        treeCopy.right = right.clone(); 
      }
      treeCopy.splitAttributeM = splitAttributeM; 
      treeCopy.cclass = cclass; 
      treeCopy.splitValue = splitValue; 
      return treeCopy; 
    }
  }
  private class DoubleWrap{
    private double d; 
    public DoubleWrap(double d){
      this.d=d; 
    }
  }
  /**
   * This is the crucial function in tree creation.
   *
   * <ul>
   * <li>Step A
   * Check if this node is a leaf,   if so,   it will mark isLeaf true
   * and mark cclass with the leaf's class. The function will not
   * recurse past this point.
   * </li>
   * <li>Step B
   * Create a left and right node and keep their references in
   * this node's left and right fields. For debugging purposes,  
   * the generation number is also recorded. The {@link RandomForest#Ms Ms} attributes are
   * now chosen by the {@link #getVarsToInclude() getVarsToInclude} function
   * </li>
   * <li>Step C
   * For all Ms variables,   first {@link #sortAtAttribute(List,  int) sort} the data records by that attribute,  
   * then look through the values from lowest to highest.
   * If value i is not equal to value i+1,   record i in the list of "indicesToCheck."
   * This speeds up the splitting. If the number of indices in indicesToCheck >  MIN_SIZE_TO_CHECK_EACH
   * then we will only {@link #checkPosition(int,   int,   int,   DoubleWrap,   TreeNode,   int) check} the
   * entropy at every {@link #INDEX_SKIP INDEX_SKIP} index otherwise,  
   * we {@link #checkPosition(int,   int,   int,   DoubleWrap,   TreeNode,   int) check}
   * the entropy for all. The "E" variable records the entropy and we are trying to find 
   * the minimum in which to split on
   * </li>
   * <li>Step D
   * The newly generated left and right nodes are now checked:
   * If the node has only one record,   we mark it as a leaf and set its class equal to the class of the record.
   * If it has less than {@link #MIN_NODE_SIZE MIN_NODE_SIZE} records,  
   * then we mark it as a leaf and set its class equal to the {@link #getMajorityClass(List) majority class}.
   * If it has more,   then we do a manual check on its data records and if all have the same class,   then it
   * is marked as a leaf. If not,   then we run {@link #recursiveSplit(TreeNode,   int) recursiveSplit} on
   * that node
   * </li>
   * </ul>
   *
   * @param parent  The node of the parent
   * @param nTreenum  the tree number
   */
  private void recursiveSplit(TreeNode parent,   int nTreenum){
    if (!parent.isLeaf){
      //-------------------------------Step A
      //当前结点包含的样本全属于同一类别，无需划分; 
      Integer cclass = checkIfLeaf(parent.data); 
      if (cclass != null){
        parent.isLeaf = true; 
        parent.cclass = cclass; 
        return; 
      }

      //-------------------------------Step B
      int nsub = parent.data.size(); 
      ArrayList<Integer> vars = getVarsToInclude(); //randomly selects Ms' index of attributes from M
      parent.attrArr = vars; 
      parent.left = new TreeNode(); 
      parent.left.generation = parent.generation + 1; 
      parent.right = new TreeNode(); 
      parent.right.generation = parent.generation + 1; 
      DoubleWrap lowestE = new DoubleWrap(Double.MIN_VALUE); 
      //假如当前属性集为空，返回样本数最多的类; 
      if(parent.attrArr.size() == 0){
        parent.cclass = getMajorityClass(parent.data); 
        return; 
      }
      //-------------------------------Step C
      //所有样本在所有属性上取值相同，无法划分，返回样本数最多的类; 
      int sameClass = 0; 
      for (int m:parent.attrArr){
        sortAtAttribute(parent.data,   m); //sorts on a particular column in the row
        ArrayList<Integer> indicesToCheck = new ArrayList<Integer>(); 
        for (int n=1;  n<nsub;  n++){
          int classA = getClass(parent.data.get(n-1)); 
          int classB = getClass(parent.data.get(n)); 
          if (classA != classB){
            indicesToCheck.add(n); 
          }
        }
        //所有样本在所有属性上取值相同，无法划分，返回样本数最多的类; 
        if (indicesToCheck.size() == 0){
          sameClass++; 
        }
      }
      if(sameClass == parent.attrArr.size()){
        parent.isLeaf = true; 
        parent.cclass = getMajorityClass(parent.data); 
        return; 
      }

      for (int m:parent.attrArr){
        sortAtAttribute(parent.data,   m); //sorts on a particular column in the row
        ArrayList<Integer> indicesToCheck = new ArrayList<Integer>(); 
        for (int n=1;  n<nsub;  n++){
          int classA = getClass(parent.data.get(n-1)); 
          int classB = getClass(parent.data.get(n)); 
          if (classA != classB){
            indicesToCheck.add(n); 
          }
        }
        if (indicesToCheck.size() > MIN_SIZE_TO_CHECK_EACH){
          for (int i=0;  i<indicesToCheck.size();  i+=INDEX_SKIP){
            //System.out.println("Checking positions for index : "+i+" and tree :"+nTreenum); 
            checkPosition(m,   indicesToCheck.get(i),   nsub,   lowestE,   parent,   nTreenum); 
            if (lowestE.d == 0){
              break; 
            }
          }
        }else {
          for (int index:indicesToCheck){
            checkPosition(m,   index,   nsub,   lowestE,   parent,   nTreenum); 
            if (lowestE.d == 0){
              break; 
            }
          }
        }
        if (lowestE.d == 0){
          break; 
        }
      }
      //从属性集合删除分裂属性
      Iterator<Integer> it = parent.attrArr.iterator(); 
      while(it.hasNext()){
        int attr = it.next(); 
        if (attr == parent.splitAttributeM){
          it.remove(); 
        }
      }
      parent.left.attrArr = parent.attrArr; 
      parent.right.attrArr = parent.attrArr; 

      //------------Left Child
      if (parent.left.data.size() == 1){//训练集为空
        parent.left.isLeaf = true; 
        parent.left.cclass = getClass(parent.left.data.get(0)); 
      }else if (parent.left.attrArr.size() == 0){//属性集为空
        parent.left.isLeaf = true; 
        parent.cclass = getMajorityClass(parent.left.data); 
      }else {
        cclass = checkIfLeaf(parent.left.data); 
        if (cclass == null){
          parent.left.isLeaf = false; 
          parent.left.cclass = null; 
//      System.out.println("make branch left: m:"+m); 
        }else {//训练集样本全属于同一类别
          parent.left.isLeaf = true; 
          parent.left.cclass = cclass; 
        }
      }
      //------------Right Child
      if (parent.right.data.size() == 1){//训练集为空
        parent.right.isLeaf = true; 
        parent.right.cclass = getClass(parent.right.data.get(0)); 
      }else if (parent.right.attrArr.size() == 0){//属性集为空
        parent.right.isLeaf = true; 
        parent.cclass = getMajorityClass(parent.right.data); 
      }else {
        cclass = checkIfLeaf(parent.right.data); 
        if (cclass == null){
          parent.right.isLeaf = false; 
          parent.right.cclass = null; 
//      System.out.println("make branch right: m:"+m); 
        }else {//训练集样本全属于同一类别
          parent.right.isLeaf = true; 
          parent.right.cclass = cclass; 
        }
      }
      if (!parent.left.isLeaf){
        recursiveSplit(parent.left,   nTreenum); 
      }
      if (!parent.right.isLeaf){
        recursiveSplit(parent.right,   nTreenum); 
      }
    }
  }
  /**
   * Given a data matrix,   return the most popular Y value (the class).
   * @param data  The data matrix
   * @return  The most popular class
   */
  private int getMajorityClass(List<int[]> data){
    int[] counts=new int[RandomForest.getC()]; 
    for (int[] record:data){
      int cclass=record[record.length-1]; //getClass(record); 
      counts[cclass-1]++; 
    }
    int index=-99; 
    int max=Integer.MIN_VALUE; 
    for (int i=0; i<counts.length; i++){
      if (counts[i] > max){
        max=counts[i]; 
        index=i+1; 
      }
    }
    return index; 
  }

  /**
   * Checks the {@link #calcEntropy(double[]) entropy} of an index in a data matrix at a particular attribute (m)
   * and returns the entropy. If the entropy is lower than the minimum to date (lowestE),   it is set to the minimum.
   *
   * The total entropy is calculated by getting the sub-entropy for below the split point and upper the split point.
   * The sub-entropy is calculated by first getting the {@link #getClassProbs(List) proportion} of each of the classes
   * in this sub-data matrix. Then the entropy is {@link #calcEntropy(double[]) calculated}. The lower sub-entropy
   * and upper sub-entropy are then weight averaged to obtain the total entropy.
   *
   * @param m    the attribute to split on
   * @param n    the index to check(rowID)
   * @param nsub    the num of records in the data matrix
   * @param lowestE  the minimum entropy to date
   * @param parent  the parent node
   * @return    the entropy of this split
   */
  private double checkPosition(int m,   int n,   int nsub,   DoubleWrap lowestE,   TreeNode parent,   int nTre){
    //             var,  index,  train.size,  lowest number,  for a tree
    //System.out.println("Checking position of the index attribute of tree :"+nTre); 
    if (n < 1){
      return 0; 
    }
    if (n > nsub){
      return 0; 
    }

    List<int[]> lower = getLower(parent.data,   n); 
    List<int[]> upper = getUpper(parent.data,   n); 
    if (lower == null){
      System.out.println("lower list null"); 
    }
    if (upper == null){
      System.out.println("upper list null"); 
    }
    double[] p = getClassProbs(parent.data); 
    double[] pl = getClassProbs(lower); 
    double[] pu = getClassProbs(upper); 
    double eP = calcEntropy(p); 
    double eL = calcEntropy(pl); 
    double eU = calcEntropy(pu); 

    double e = eP - eL * lower.size()/(double)nsub - eU * upper.size()/(double)nsub;  
//  out.write(m+",  "+n+",  "+parent.data.get(n)[m]+",  "+e+"\n"); 
    if (e > lowestE.d){
      lowestE.d = e; 
//    System.out.print("-"); 
      parent.splitAttributeM = m; 
      parent.splitValue = parent.data.get(n)[m]; 
      parent.left.data = lower; 
      parent.right.data = upper; 

    }
    return e; //entropy
  }
  /**
   * Given a data record,   return the Y value - take the last index.
   *
   * @param record  the data record
   * @return    its y value (class)
   */
  public static int getClass(int[] record){
    return record[RandomForest.getM()]; 
  }
  /**
   * Given a data matrix,   check if all the y values are the same. If so,  
   * return that y value,   null if not
   *
   * @param data  the data matrix
   * @return    the common class (null if not common)
   */
  private Integer checkIfLeaf(List<int[]> data){
//  System.out.println("checkIfLeaf"); 
    boolean isLeaf = true; 
    int classA = getClass(data.get(0)); 
    for (int i=1;  i<data.size();  i++){
      int[] recordB = data.get(i); 
      if (classA != getClass(recordB)){
        isLeaf = false; 
        break; 
      }
    }
    if (isLeaf){
      return getClass(data.get(0)); 
    }else{
      return null; 
    }
  }
  /**
   * Split a data matrix and return the upper portion.
   *
   * @param data  the data matrix to be split
   * @param nSplit  index in a sub-data matrix that we will return all data records above it
   * @return    the upper sub-data matrix
   */
  private List<int[]> getUpper(List<int[]> data,   int nSplit){
    int d =  data.size(); 
    List<int[]> upper = new ArrayList<int[]>(d-nSplit); 
    for (int n=nSplit;  n<d;  n++){  
      upper.add(data.get(n)); 
    }
    return upper; 
  }
  /**
   * Split a data matrix and return the lower portion.
   *
   * @param data  the data matrix to be split
   * @param nSplit  this index in a sub-data matrix that return all data records below it
   * @return    the lower sub-data matrix
   */
  private List<int[]> getLower(List<int[]> data,   int nSplit){
    List<int[]> lower = new ArrayList<int[]>(nSplit); 
    for (int n=0;  n<nSplit;  n++){
      lower.add(data.get(n)); 
    }
    return lower; 
  }
  /**
   * This class compares two data records by numerically comparing a specified attribute.
   *
   * @author kapelner
   *
   */
  private class AttributeComparator implements Comparator{
    /** the specified attribute. */
    private int m; 
    /**
     * Create a new comparator.
     * @param m  the attribute in which to compare on.
     */
    public AttributeComparator(int m){
      this.m = m; 
    }
    /**
     * Compare the two data records. They must be of type int[].
     *
     * @param o1  data record A
     * @param o2  data record B
     * @return    -1 if A[m] < B[m],   1 if A[m] > B[m],   0 if equal
     */
    public int compare(Object o1,   Object o2){
      int a = ((int[])o1)[m]; 
      int b = ((int[])o2)[m]; 
      if (a < b){
        return -1; 
      }
      if (a > b){
        return 1; 
      }else{
        return 0; 
      }
    }
  }
  /**
   * Sorts a data matrix by an attribute from lowest record to highest record.
   * @param data    the data matrix to be sorted
   * @param m    the attribute to sort on
   */
  @SuppressWarnings("unchecked")
  private void sortAtAttribute(List<int[]> data,   int m){
    Collections.sort(data,   new AttributeComparator(m)); 
  }
  /**
   * Given a data matrix,   return a probability mass function representing.
   * the frequencies of a class in the matrix (the y values)
   *
   * @param records  the data matrix to be examined
   * @return    the probability mass function
   */
  private double[] getClassProbs(List<int[]> records){

    double dN = records.size(); 

    int[] counts = new int[RandomForest.getC()]; //the num of target class
//  System.out.println("counts:"); 
//  for (int i:counts)
//    System.out.println(i+" "); 

    for (int[] record:records){
      counts[getClass(record)-1]++; 
    }
    double[] ps = new double[RandomForest.getC()]; 
    for (int j=0;  j<RandomForest.getC();  j++){
      ps[j] = counts[j]/dN; 
    }
//  System.out.print("probs:"); 
//  for (double p:ps)
//    System.out.print(" "+p); 
//  System.out.print("\n"); 
    return ps; 
  }
  /** ln(2). */
  private static final double LOGOFTWO = Math.log(2); 
  /**
   * Given a probability mass function indicating the frequencies of.
   * class representation,   calculate an "entropy" value using the method
   * in Tan Steinbach Kumar's "Data Mining" textbook
   *
   * @param ps    the probability mass function
   * @return    the entropy value calculated
   */
  private double calcEntropy(double[] ps){
    double e = 0; 
    for (double p:ps){
      if (p != 0) {
        e += p * Math.log(p)/Math.log(2); 
      }
    }
    return -e;  //according to TSK p158
  }
  /**
   * Of the M attributes,   select {@link RandomForest#Ms Ms} at random.
   *
   * @return  The list of the Ms attributes' indices
   */
  private ArrayList<Integer> getVarsToInclude() {
    boolean[] whichVarsToInclude = new boolean[RandomForest.getM()]; 

    for (int i=0;  i<RandomForest.getM();  i++){
      whichVarsToInclude[i]=false; 
    }

    while (true){
      int a = (int)Math.floor(Math.random() * RandomForest.getM()); //左闭右开 [0，1)
      whichVarsToInclude[a] = true; 
      int n = 0; 
      for (int i=0;  i<RandomForest.getM();  i++){
        if (whichVarsToInclude[i]) {
          n++; 
        }
      }   
      if (n == RandomForest.getMs()){
        break; 
      }
    }

    ArrayList<Integer> shortRecord = new ArrayList<Integer>(RandomForest.getMs()); 

    for (int i=0;  i<RandomForest.getM();  i++){
      if (whichVarsToInclude[i]) {
        shortRecord.add(i); 
      }
    } 
    return shortRecord; 
  }

  /**
   * Create a boostrap sample of a data matrix.
   * @param data  the data matrix to be sampled
   * @param train  the bootstrap sample
   * @param val  the records that are absent in the bootstrap sample
   * @param numb    the tree number
   */
  private void bootStrapSample(ArrayList<int[]> data,   ArrayList<int[]> train,   ArrayList<int[]> val,   int numb){
    ArrayList<Integer> indices = new ArrayList<Integer>(nN); 
    for (int n=0;  n<nN;  n++){
      indices.add((int)Math.floor(Math.random() * nN)); 
    }
    ArrayList<Boolean> isIn = new ArrayList<Boolean>(nN); 
    for (int n=0;  n<nN;  n++){
      isIn.add(false);  //initialize it first
    }
    for (int index:indices){
      train.add((data.get(index)).clone()); //train has duplicated  record
      isIn.set(index,   true); 
    }
    for (int i=0;  i<nN;  i++){
      if (!isIn.get(i)) {
        val.add((data.get(i)).clone()); 
      }
    }
//  System.out.println("bootstrap nN:"+nN+" size of bootstrap:"+bootstrap.size()); 
  }
  /**
   * Recursively deletes all data records from the tree. This is run after the tree.
   * has been computed and can stand alone to classify incoming data.
   *
   * @param node  initially,   the root node of the tree
   */
  private void flushData(TreeNode node){
    node.data = null; 
    if (node.left != null){
      flushData(node.left); 
    }
    if (node.right != null){
      flushData(node.right); 
    }
  }

//  // possible to clone trees
//  private DTree(){}
//  public DTree clone(){
//  DTree copy=new DTree(); 
//  copy.root=root.clone(); 
//  return copy; 
//  }

  /**
   * Get the number of data records in the test data matrix that were classified correctly.
   */
  public int getNumCorrect(){
    return correct; 
  }
  /**
   * Get the number of data records left out of the bootstrap sample.
   */
  public int getTotalNumInTestSet(){
    return testN; 
  }
  /**
   * Get the importance level of attribute m for this tree.
   */
  public int getImportanceLevel(int m){
    return importances[m]; 
  }
//  private void PrintOutNode(TreeNode parent,  String init){
//  try {
//    System.out.println(init+"node: left"+parent.left.toString()); 
//  } catch (Exception e){
//    System.out.println(init+"node: left null"); 
//  }
//  try {
//    System.out.println(init+" right:"+parent.right.toString()); 
//  } catch (Exception e){
//    System.out.println(init+"node: right null"); 
//  }
//  try {
//    System.out.println(init+" isleaf:"+parent.isLeaf); 
//  } catch (Exception e){}
//  try {
//    System.out.println(init+" splitAtrr:"+parent.splitAttributeM); 
//  } catch (Exception e){}
//  try {
//    System.out.println(init+" splitval:"+parent.splitValue); 
//  } catch (Exception e){}
//  try {
//    System.out.println(init+" class:"+parent.cclass); 
//  } catch (Exception e){}
//  try {
//    System.out.println(init+" data size:"+parent.data.size()); 
//    PrintOutClasses(parent.data); 
//  } catch (Exception e){
//    System.out.println(init+" data: null"); 
//  }
//  }
//  private void PrintOutClasses(List<int[]> data){
//  try {
//    System.out.print(" (n="+data.size()+") "); 
//    for (int[] record:data)
//    System.out.print(getClass(record)); 
//    System.out.print("\n"); 
//  }
//  catch (Exception e){
//    System.out.println("PrintOutClasses: data null"); 
//  }
//  }
//  public static void PrintBoolArray(boolean[] b) {
//  System.out.print("vars to include: "); 
//  for (int i=0; i<b.length; i++)
//    if (b[i])
//    System.out.print(i+" "); 
//  System.out.print("\n\n"); 
//  }
//
//  public static void PrintIntArray(List<int[]> lower) {
//  System.out.println("tree"); 
//  for (int i=0; i<lower.size(); i++){
//    int[] record=lower.get(i); 
//    for (int j=0; j<record.length; j++){
//    System.out.print(record[j]+" "); 
//    }
//    System.out.print("\n"); 
//  }
//  System.out.print("\n"); 
//  System.out.print("\n"); 
//  }
  public ArrayList<Integer> getPredictions() {
    return predictions;
  }
  public void setPredictions(ArrayList<Integer> predictionss) {
    this.predictions = predictionss;
  }
}
