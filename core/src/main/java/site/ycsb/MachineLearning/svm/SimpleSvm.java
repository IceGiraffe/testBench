package site.ycsb.svm;

import java.io.File; 

import java.io.IOException; 
import java.io.RandomAccessFile; 
import java.util.StringTokenizer; 
/**
 * svm.
 */
public class SimpleSvm {
  private int exampleNum; 
  private int exampleDim; 
  private double[] w; 
  private double lambda; 
  private double lr = 0.001; //0.00001
  private double threshold = 0.001; 
  private double cost; 
  private double[] grad; 
  private double[] yp; 
  public SimpleSvm(double paramLambda){

    lambda = paramLambda;   
    
  }
  
  private void costAndGrad(double[][] x, double[] y){
    cost =0; 
    for(int m=0; m<exampleNum; m++){
      yp[m]=0; 
      for(int d=0; d<exampleDim; d++){
        yp[m]+=x[m][d]*w[d]; 
      }
      
      if(y[m]*yp[m]-1<0){
        cost += (1-y[m]*yp[m]); 
      }
      
    }
    
    for(int d=0; d<exampleDim; d++){
      cost += 0.5*lambda*w[d]*w[d]; 
    }
    

    for(int d=0; d<exampleDim; d++){
      grad[d] = Math.abs(lambda*w[d]);   
      for(int m=0; m<exampleNum; m++){
        if(y[m]*yp[m]-1<0){
          grad[d]-= y[m]*x[m][d]; 
        }
      }
    }        
  }
  
  private void update(){
    for(int d=0; d<exampleDim; d++){
      w[d] -= lr*grad[d]; 
    }
  }
  
  public void train(double[][] x, double[] y, int maxIters){
    exampleNum = x.length; 
    if(exampleNum <=0) {
      System.out.println("num of example <=0!"); 
      return; 
    }
    exampleDim = x[0].length; 
    w = new double[exampleDim]; 
    grad = new double[exampleDim]; 
    yp = new double[exampleNum]; 
    
    for(int iter=0; iter<maxIters; iter++){
      
      costAndGrad(x, y); 
      //System.out.println("cost:"+cost); 
      if(cost< threshold){
        break; 
      }
      update(); 
      
    }
  }
  private int predict(double[] x){
    double pre=0; 
    for(int j=0; j<x.length; j++){
      pre+=x[j]*w[j]; 
    }
    if(pre >=0) {//这个阈值一般位于-1到1
      return 1; 
    }else {
      return -1; 
    }
  }
  
  public void test(double[][] testx, double[] testY){
    int error=0; 
    for(int i=0; i<testx.length; i++){
      if(predict(testx[i]) != testY[i]){
        error++; 
      }
    }
    System.out.println("total:"+testx.length); 
    System.out.println("error:"+error); 
    System.out.println("error rate:"+((double)error/testx.length)); 
    System.out.println("acc rate:"+((double)(testx.length-error)/testx.length)); 
  }
  
  
  
  public static void loadData(double[][]x, double[] y, String trainFile) throws IOException{
    
    File file = new File(trainFile); 
    RandomAccessFile raf = new RandomAccessFile(file, "r"); 
    StringTokenizer tokenizer, tokenizer2;  

    int index=0; 
    while(true){
      String line = raf.readLine(); 
      
      if(line == null) {
        break; 
      }
      tokenizer = new StringTokenizer(line, " "); 
      y[index] = Double.parseDouble(tokenizer.nextToken()); 
      //System.out.println(y[index]); 
      while(tokenizer.hasMoreTokens()){
        tokenizer2 = new StringTokenizer(tokenizer.nextToken(), ":"); 
        int k = Integer.parseInt(tokenizer2.nextToken()); 
        double v = Double.parseDouble(tokenizer2.nextToken()); 
        x[index][k] = v; 
        //System.out.println(k); 
        //System.out.println(v);         
      }  
      x[index][0] =1; 
      index++;     
    }
  }
  
  public static void rf(String[] args) throws IOException {
    // TODO Auto-generated method stub
    double[] y = new double[400]; 
    double[][] x = new double[400][11]; 
    String trainFile = "C:\\Users\\69494\\Downloads\\simpleSvm-master\\train_bc"; 
    loadData(x, y, trainFile); 
    
    
    SimpleSvm svm = new SimpleSvm(0.0001); 
    svm.train(x, y, 7000); 
    
    double[] testy = new double[283]; 
    double[][] testx = new double[283][11]; 
    String testFile = "C:\\Users\\69494\\Downloads\\simpleSvm-master\\testbc"; 
    loadData(testx, testy, testFile); 
    svm.test(testx,  testy); 
    
  }

}
