package site.ycsb.ANN;

import java.util.ArrayList;
import java.util.List;
/**
 * 
 */
public class DataNode{
  private List<Float> mAttribList;
  private int type;

  public int getType(){
    return type;
  }

  public void setType(int typee){
    this.type = typee;
  }

  public List<Float> getAttribList(){
    return mAttribList;
  }

  public void addAttrib(Float value){
    mAttribList.add(value);
  }

  public DataNode(){
    mAttribList = new ArrayList<Float>();
  }

}
