package site.ycsb.rf;


import java.util.ArrayList; 
/**
 * 
 */
public final class MainRun {
  private MainRun() {
    
  }
  @SuppressWarnings("static-access")
  public static void svm(){

    String trainPath = "C:\\Users\\69494\\Downloads\\Machine-Learning-Algorithm-master\\randomForest\\Data.txt"; 
    String testPath = "C:\\Users\\69494\\Downloads\\Machine-Learning-Algorithm-master\\randomForest\\test.txt"; 
    int numTrees = 100; 

    DescribeTrees dT = new DescribeTrees(); 
    ArrayList<int[]> train = dT.createInput(trainPath); 

    DescribeTrees dT2 = new DescribeTrees(); 
    ArrayList<int[]> test = dT2.createInput(testPath); 
    int categ = 0; 

    //the num of labels
    int trainLength = train.get(0).length; 
    for(int k=0;  k<train.size();  k++){
      if(train.get(k)[trainLength-1] < categ){
        continue; 
      }else{
        categ = train.get(k)[trainLength-1]; 
      }
    }

    RandomForest rf = new RandomForest(numTrees,  train,  test); 
    rf.setC(categ); //the num of labels
    rf.setM(train.get(0).length-1); //the num of Attr
    //属性扰动，每次从M个属性中随机选取Ms个属性，Ms = ln(m)/ln(2)
    rf.setMs((int)Math.round(Math.log(rf.getM())/Math.log(2) + 1)); //随机选择的属性数量
    rf.start(); 
  }
}
