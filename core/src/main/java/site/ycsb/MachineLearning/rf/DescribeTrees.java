package site.ycsb.rf;


import java.io.BufferedReader; 
import java.io.FileReader; 
import java.io.IOException; 
import java.util.ArrayList; 
/**
 * 
 */
public class DescribeTrees {
  //把txt文件作为输入，导入到randomForest中
  private BufferedReader br = null; 


  public ArrayList<int[]> createInput(String path){
    ArrayList<int[]> dataInput = new ArrayList<int[]>(); 
    try {
      String sCurrentLine; 
      br = new BufferedReader(new FileReader(path)); 

      while ((sCurrentLine = br.readLine()) != null) {
        ArrayList<Integer> spaceIndex = new ArrayList<Integer>(); //空格的index
        int i; 
        if(sCurrentLine != null){
          sCurrentLine = " " + sCurrentLine + " "; 
          for(i=0;  i < sCurrentLine.length();  i++){
            if(Character.isWhitespace(sCurrentLine.charAt(i))){
              spaceIndex.add(i); 
            }
          }
          int[] dataPoint = new int[spaceIndex.size()-1]; 
          for(i=0;  i<spaceIndex.size()-1;  i++){
            dataPoint[i]=Integer.parseInt(sCurrentLine.substring(spaceIndex.get(i)+1,   spaceIndex.get(i+1))); 
          }
          /* print dataPoint
          for(k=0;  k<dataPoint.length;  k++){
            //System.out.print("-"); 
            System.out.print(dataPoint[k]); 
            System.out.print(" "); 

          }
          **/
          dataInput.add(dataPoint); 
        }
      }

    } catch (IOException e) {
      e.printStackTrace(); 
    } finally {
      try {
        if (br != null){
          br.close(); 
        }
      } catch (IOException ex) {
        ex.printStackTrace(); 
      }
    }
    return dataInput; 
  }
}
