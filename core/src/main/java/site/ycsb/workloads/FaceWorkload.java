package site.ycsb.workloads;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.io.File;
import java.io.*;
import java.util.HashMap;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import site.ycsb.generator.*;
import java.util.*;
import site.ycsb.*;
import java.io.FileInputStream;
import com.mongodb.gridfs.GridFS;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;

/**
* A class extends the CoreWrrklaod with TPC-DS specification.
*/
public class FaceWorkload extends CoreWorkload {
  /**
   * The scale of data. Unit: GB.
   */
  public static final String ALGORITHM = "algorithm";
  public static final String DATA_SCALE_PROPERTY = "datascale";
  public static final String DATA_SCALE_PROPERTY_DEFAULT = "1";

  public static final String DATA_DIR_PROPERTY = "./dataGen/";
  public static final String DATA_DIR_PROPERTY_DEFAULT = "./dataGen/";
  //the same variable in the Client.java
  public static final String QUERY_INDEX = "queryindex";
  public static final String QUERY_INDEX_DEFAULT = "1";

  protected NumberGenerator filesequence;
  protected NumberGenerator filesequenceRead;
  protected HashMap<String, String> fieldProperties = new HashMap<String, String>();
  protected HashMap<String, String[]> tableAndFieldNames;
  protected String scale;
  protected String dataDir;
  protected ArrayList<String> fileName;
  protected ArrayList<File> fileList;
  protected String queryindex;
  private static GridFS gridFS = null;
  private String algorithmName;

  public static GridFS myGrid() {
    return gridFS;
  }
  /**
   * may need to change the path
   * There is one opencv_java420.dll in ./opencv/
   */
  static {
    //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    String path = "/home/test/Bench/opencv/libopencv_java451.so";
    System.load(path);
  }
  
  public void init(Properties p) throws WorkloadException {
    /*
    if (p.getProperty(QUERY_INDEX).compareTo("0") != 0) {
      queryindex = p.getProperty(QUERY_INDEX);
    }
    //Genarate data with TPC-DS tools
    scale = p.getProperty(DATA_SCALE_PROPERTY, DATA_SCALE_PROPERTY_DEFAULT);
    dataDir = p.getProperty(DATA_DIR_PROPERTY, DATA_DIR_PROPERTY_DEFAULT);
    //createAndRunScriptForDataGen();

    // Store file list

     */
    algorithmName=p.getProperty(ALGORITHM);
    operationchooser = createOperationGenerator(p);
    fileName = new ArrayList<String>();
    fileList = new ArrayList<File>();
    getAllFileName("Img/img_align_celeba", fileName, fileList);
    filesequence = new CounterGenerator(0);
    filesequenceRead = new CounterGenerator(0);
    
  }

  @Override
  public boolean doInsert(DB db, Object threadstate) {
    //Chosse a files
    try {
      return doInsertImg(db, threadstate);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean doInsertImg(DB db, Object threadstate) throws FileNotFoundException {
    //Chosse a files
    Status status = null;
    int filenum = filesequence.nextValue().intValue();
    //System.out.println(filenum);
    //for(int i = 0; i < filenum; ++i) {
      String dbfile = fileList.get(filenum).getName();
      System.out.println(dbfile);
      String path = "./Img/img_align_celeba/";
      String dp = path + dbfile;
      //Read lines from the file
      //Get information about the file: table name & field keys
      String tableName = "image";

      status = db.insertBig(tableName, dbfile, new FileInputStream(dp));
    //}

    return null != status && status.isOk();
  }


  public static int runShellCommand(String shellCommand) {
    Process process = null;
    int result = 0;
    try{
      process = Runtime.getRuntime().exec(shellCommand);
      int iWaitFor = process.waitFor();
      if (iWaitFor != 0) {
        // Error
        result = 0;
      }
      result = 1;
    } catch (Exception e) {
      result = 0;
    } finally {
      if (process != null) {
        process.destroy();
        process = null;
      }
      return result;
    }
  }

  public static void getAllFileName(String path, ArrayList<String> fileName, ArrayList<File> fileList) {
    File file = new File(path);
    File [] files = file.listFiles();
    String [] names = file.list();
    if(names != null) {
      fileName.addAll(Arrays.asList(names));
    }
    if(files != null) {
      fileList.addAll(Arrays.asList(files));
    }
    /* 
     for(File a:files) {
       if(a.isDirectory()) {
         getAllFileName(a.getAbsolutePath(),fileName);
       }
      }
    */
  }
  
  public static void createAndRunScriptForDataGen(){
    String scriptName = "dataGen.sh";
    try {
      FileWriter writer = new FileWriter(scriptName);
      writer.write("#!/bash/sh\n");
      writer.write("cd ./TPC_DS_DataGen/v2.8.0rc4/tools/\n");
      writer.write("mkdir dataGen\n");
      writer.write("./dsdgen -DIR ./dataGen -SCALE 1 \n");
      writer.write("wait\n");
      writer.write("rm ./dataGen/dbgen_version.dat");
      writer.close();
      int result = runShellCommand("chmod 664 dataGen.sh");
      result = runShellCommand("bash dataGen.sh");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //YYB: Execute sql script for business workload
  
  public boolean doTransaction(DB db, Object threadstate) {
    String operation = operationchooser.nextString();
    if(operation == null) {
      return false;
    }
    int filenum = filesequenceRead.nextValue().intValue();
    String dbfile = fileList.get(filenum).getName();
    String tableName = "image";
    String dpNew = "./ImgNew/img/" + dbfile;
    try {
      //System.out.println("before readBig");
      db.readBig(tableName, dbfile, new FileOutputStream(dpNew));
      //System.out.println(db.getClass());
      //System.out.println("after readBig");
    } catch(Exception e) {
      e.printStackTrace();
    }


    String classifier = "";
    switch (algorithmName) {
    case "ALT":
      classifier = "../../pku/opencv/opencv/data/haarcascades/haarcascade_frontalface_alt.xml";
      break;
    case "FIRST":
      classifier = "/home/pku/opencv/opencv/data/haarcascades/haarcascade_frontalcatface.xml";
      break;
    case "EXTEND":
      classifier = "/home/pku/opencv/opencv/data/haarcascades/haarcascade_frontalcatface_extended.xml";
      break;
    case "ALT2":
      //classifier = "/home/pku/opencv/opencv/data/haarcascades/haarcascade_frontalface_alt2.xml";
      classifier = "../../pku/opencv/opencv/data/haarcascades/haarcascade_frontalface_alt2.xml";
      break;
    default:
      classifier = "/home/pku/opencv/opencv/data/haarcascades/haarcascade_frontalface_default.xml";
      break;
    }
    try {
      //System.out.println(classifier);
      
      Mat mat = imgPath2Mat(dpNew);
      Mat result = detectFace(mat, classifier);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  public static Mat detectFace(Mat img, String classifier){
    System.out.println("detecting face");
    CascadeClassifier faceDetector = new CascadeClassifier(classifier);
    if(faceDetector.empty()) {
       System.out.println("empty!");
       boolean flag = faceDetector.load(classifier);
       System.out.println(flag);
    }
    MatOfRect faceDetections = new MatOfRect();
    faceDetector.detectMultiScale(img, faceDetections);
    Rect[] rects = faceDetections.toArray();
    Random r = new Random();

    if(rects != null && rects.length >= 1) {
      for(Rect rect: rects) {
        Imgproc.rectangle(img, new Point(rect.x, rect.y),
            new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 2);
        Mat faceROI = new Mat(img, rect);
        MatOfRect eyesDetections = new MatOfRect();
        save(img, rect, "./ImgNew/" + classifier + r.nextInt(1000) + ".jpg");
      }
    }
    return img;
  }

  private static void save(Mat img, Rect rect, String outFile) {
    Mat sub = img.submat(rect);
    Mat mat = new Mat();
    Size size = new Size(300, 300);
    Imgproc.resize(sub, mat, size);
    Imgcodecs.imwrite(outFile, mat);
  }

  public static Mat imgPath2Mat(String imagePath) throws IOException {
    BufferedImage image = ImageIO.read(new FileInputStream(imagePath));
    Mat matImage = BufImg2Mat(image, BufferedImage.TYPE_3BYTE_BGR, CvType.CV_8UC3);
    //System.out.println(matImage.dump());
    return matImage;
  }

  public static Mat BufImg2Mat(BufferedImage original, int imgType, int matType) {
    if(original == null) {
      throw new IllegalArgumentException("origin == null");
    }

    if(original.getType() != imgType) {
      BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), imgType);

      Graphics2D g = image.createGraphics();
      try {
        g.setComposite(AlphaComposite.Src);
        g.drawImage(original, 0, 0, null);
      } finally {
        g.dispose();
      }
    }

    byte[] pixels = ((DataBufferByte) original.getRaster().getDataBuffer()).getData();
    Mat mat = Mat.eye(original.getHeight(), original.getWidth(), matType);
    mat.put(0, 0, pixels);
    return mat;
  }

  public static BufferedImage Mat2BufImg(Mat matrix, String fileExtension) {
    MatOfByte mob = new MatOfByte();
    Imgcodecs.imencode(fileExtension, matrix, mob);

    byte[] byteArray = mob.toArray();
    BufferedImage bufImage = null;
    try {
      InputStream in = new ByteArrayInputStream(byteArray);
      bufImage = ImageIO.read(in);
    } catch (Exception e){
      e.printStackTrace();
    }
    return bufImage;
  }
}
