package site.ycsb.SOMcluster;

/**
 *
 * @author Taha Emara 
 * Website: http://www.emaraic.com 
 * Email : taha@emaraic.com
 * Created on: Nov 22, 2017
 */
public class Iris {

  private double sepalLength;
  private double sepalWidth;
  private double petalLength;
  private double petalWidth;
  private String type;

  public double getSepalLength() {
    return sepalLength;
  }

  public void setSepalLength(double tSepalLength) {
    this.sepalLength = tSepalLength;
  }

  public double getSepalWidth() {
    return sepalWidth;
  }

  public void setSepalWidth(double tSepalWidth) {
    this.sepalWidth = tSepalWidth;
  }

  public double getPetalLength() {
    return petalLength;
  }

  public void setPetalLength(double tPetalLength) {
    this.petalLength = tPetalLength;
  }

  public double getPetalWidth() {
    return petalWidth;
  }

  public void setPetalWidth(double tPetalWidth) {
    this.petalWidth = tPetalWidth;
  }

  public String getType() {
    return type;
  }

  public void setType(String ttype) {
    this.type = ttype;
  }

  @Override
  public String toString() {
    return "Iris{" + "sepalLength= " + sepalLength + 
        ", sepalWidth= " + sepalWidth + ", petalLength= " + 
        petalLength + ", petalWidth= " + petalWidth + ", type= " + type + '}';
  }

}
