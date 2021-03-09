package site.ycsb.knn;

/**
 * 
 */
public class Sample {
  private int label;
  private int [] pixels;
  public int getLabel() {
    return label;
  }
  public void setLabel(int labell) {
    this.label = labell;
  }
  public int [] getPixels() {
    return pixels;
  }
  public void setPixels(int [] pixelss) {
    this.pixels = pixelss;
  }
}
