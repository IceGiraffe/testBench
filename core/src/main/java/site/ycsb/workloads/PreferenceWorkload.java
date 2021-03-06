package site.ycsb.workloads;

// import java.awt.*;
// import java.awt.image.BufferedImage;
// import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.io.File;
import java.io.*;
import java.util.HashMap;
// import javafx.collections.transformation.FilteredList;
import site.ycsb.generator.*;
import java.util.*;
import site.ycsb.*;
import java.util.Map.Entry;
// import java.io.FileInputStream;
// import com.mongodb.gridfs.GridFS;

/**
 * A class extends the CoreWrrklaod with Search Engine specification.
 */

public class PreferenceWorkload extends CoreWorkload{
  /**
   * The scale of data. Unit: GB.
   */
  public static final String DATA_PROPERTY = "path";
  // public static final String DATA_SCALE_PROPERTY = "datascale";
  // public static final String DATA_SCALE_PROPERTY_DEFAULT = "1";

  public static final String DATA_DIR_PROPERTY = "./dataGen/";
  public static final String DATA_DIR_PROPERTY_DEFAULT = "./dataGen/";
  //the same variable in the Client.java
  public static final String QUERY_INDEX = "queryindex";
  public static final String QUERY_INDEX_DEFAULT = "1";
  public static final String INSERT_COUNT_PROPERTY = "insertcount";
  public static final String QUERY_TASK = "task";

  protected NumberGenerator filesequence;
  protected HashMap<String, String> fieldProperties = new HashMap<String, String>();
  protected HashMap<String, String[]> tableAndFieldNames;
  protected String scale;
  protected String dataDir;
  protected String inputFileName;
  protected String tableName;
  protected ArrayList<String> fileName;
  protected ArrayList<File> fileList;
  protected String queryindex;
  protected String filePath;
  protected int recordOrder;
  protected Iterator<String> recordIt;
  protected Iterator<File> fileIt;
  protected List<String> records;
  protected int insertCount;
  protected int scaleTable;
  protected int insertionRetryLimit = 1;
  protected int insertionRetryInterval = 3;
  protected boolean orderedinserts = true;
  protected int zeropadding = 1;

  @Override
  public void init(Properties p) throws WorkloadException{
    // operationchooser = createOperationGenerator(p);
    // fileName = new ArrayList<String>();
    // fileList = new ArrayList<File>();
    // getAllFileName("./News/", fileName, fileList);
    System.out.println("initing!!!!!!!");
    // filePath = p.getProperty(DATA_PROPERTY);
    filePath = "./News/";
    fileName = new ArrayList<String>();
    fileList = new ArrayList<File>();
    // filesequence = new CounterGenerator(0);
    // filesequenceRead = new CounterGenerator(0);
    // insertCount = Integer.parseInt(p.getProperty(INSERT_COUNT_PROPERTY));
    recordOrder = 0;
    // getAllFileName(filePath, fileName, fileList);
    tableAndFieldNames = new HashMap<>();
    tableAndFieldNames.put("preference", new String[]{"_id", "id0", "id1"});
    buildRecord();
  }

  public void buildRecord() {
    try {
      recordOrder = 0;
      BufferedReader br = new BufferedReader(new FileReader("/home/test/Bench/Ptest_data.txt"));
      records = new LinkedList<>();
      String line;
      while((line = br.readLine()) != null) {
        records.add(line);
      }
      recordIt = records.iterator();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void getAllFileName(String path, ArrayList<String> fileName, ArrayList<File> fileList) {
    File file = new File(path);
    File [] files = file.listFiles();
    String [] names = file.list();
    if(names != null) {
      fileName.addAll(Arrays.asList(names));
    }
    if(file != null) { 
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
  @Override
  public boolean doInsert(DB db, Object threadstate) {
    //Chosse a files
    tableName = "preference";
    HashMap<String, ByteIterator> values;
    String[] fieldValues;
    ByteIterator data;
    //String record;
    Status status = null;
    String line = "null";
    int numOfRetries;
    //int count;
    BufferedReader br;
    Map<Integer, Integer> edges = new HashMap<Integer, Integer>();
    fieldValues = tableAndFieldNames.get(tableName);
    try {
      if (recordIt.hasNext()){
        line = recordIt.next();
        
        String[] strList = line.split(" ");
        values = new HashMap<>();
        ByteIterator user_ = new StringByteIterator(strList[0]);
        for (int j = 1; j < strList.length; j++){
          recordOrder++;
          values.put(fieldValues[1], user_);
          data = new StringByteIterator(strList[j]);
          values.put(fieldValues[2], data);
          numOfRetries = 0;
          scaleTable = recordOrder;
          do {
            String keys = buildKeyName(recordOrder);
            System.out.println("inserting " + keys);
            status = db.insert(tableName, keys, values);
            //System.err.println(status);
            if (null != status && status.isOk()) {
              break;
            }
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
              System.err.println("Error inserting, not retrying any more. number of attempts: " +
                  numOfRetries + "Insertion Retry Limit: " + insertionRetryLimit);
              break;
            }
          } while (true);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  protected String buildKeyName(long keynum) {
    if (!orderedinserts) {
      keynum = Utils.hash(keynum);
    }
    String value = Long.toString(keynum);
    int fill = zeropadding - value.length();
    String prekey = "user";
    for (int i = 0; i < fill; i++) {
      prekey += '0';
    }
    return prekey + value;
  }

  @Override
  public boolean doTransaction(DB db, Object threadstate) {
    Vector<HashMap<String, ByteIterator>> res = new Vector<HashMap<String, ByteIterator>>();
    Set<String> fSet = new HashSet<String>(Arrays.asList(tableAndFieldNames.get("preference")));
    db.scan("preference", buildKeyName(1), scaleTable, fSet, res);
    recommend(res);
    return true;
  }

  public static void recommend(Vector<HashMap<String, ByteIterator>> s){
    // Scanner scanner = new Scanner(System.in);
    // System.out.println("Input the total users number:");
    //??????????????????
    // int N = scanner.nextInt();
    
    Map<String, Integer> userItemLength = new HashMap<>(); //????????????????????????????????????????????????  eg: A 3
    Map<String, Set<String>> itemUserCollection = new HashMap<>(); //????????????????????????????????? eg: a A B
    Set<String> items = new HashSet<>(); //????????????????????????
    Set<String> users_ = new HashSet<>();
    Map<String, Integer> userID = new HashMap<>(); //????????????????????????????????????ID??????
    Map<Integer, String> idUser = new HashMap<>(); //?????????????????????ID?????????????????????
    // System.out.println("Input user--items maping infermation:<eg:A a b d>");
    // scanner.nextLine();
    // for(int i = 0; i < N ; i++){//????????????N????????? ????????????  ???????????????
    //   String[] user_item = scanner.nextLine().split(" ");
    //   int length = user_item.length;
    //   userItemLength.put(user_item[0], length-1);//eg: A 3
    //   userID.put(user_item[0], i);//??????ID?????????????????????????????????
    //   idUser.put(i, user_item[0]);
    //   //????????????--???????????????
    //   for(int j = 1; j < length; j ++){
    //     if(items.contains(user_item[j])){//?????????????????????????????????--??????????????????????????????????????????
    //       itemUserCollection.get(user_item[j]).add(user_item[0]);
    //     }else{//????????????????????????--??????????????????
    //       items.add(user_item[j]);
    //       itemUserCollection.put(user_item[j], new HashSet<String>());//????????????--??????????????????
    //       itemUserCollection.get(user_item[j]).add(user_item[0]);
    //     }
    //   }
    // }
    String s1 = "id0";
    String s2 = "id1";
    String v, w;
    int user_cnt = 0;
    int current_user_cnt = 0;
    String last_user = "null";
    for (HashMap<String, ByteIterator> line : s) {
      v = line.get(s1).toString();
      w = line.get(s2).toString();
      if (!users_.contains(v)){
        userID.put(v, users_.size());
        idUser.put(users_.size(), v);
        if (last_user != "null"){
          userItemLength.put(last_user, current_user_cnt);
        }
        last_user = v;
        current_user_cnt = 0;
        users_.add(v);
      }
      if(items.contains(w)){
        itemUserCollection.get(w).add(v);
      }
      else{
        items.add(w);
        itemUserCollection.put(w, new HashSet<String>());
        itemUserCollection.get(w).add(v);
      }
      current_user_cnt++;
    }
    int N = users_.size();
    int[][] sparseMatrix = new int[N][N]; //???????????????????????????????????????????????????????????????????????????
    System.out.println(itemUserCollection.toString());
    //?????????????????????????????????
    Set<Entry<String, Set<String>>> entrySet = itemUserCollection.entrySet();
    Iterator<Entry<String, Set<String>>> iterator = entrySet.iterator();
    while(iterator.hasNext()){
      Set<String> commonUsers = iterator.next().getValue();
      for (String user_u : commonUsers) {
        for (String user_v : commonUsers) {
          if(user_u.equals(user_v)){
            continue;
          }
          sparseMatrix[userID.get(user_u)][userID.get(user_v)] += 1; //????????????u?????????v??????????????????????????????
        }
      }
    }
    // System.out.println(userItemLength.toString());
    // System.out.println("Input the user for recommendation:<eg:A>");
    String recommendUser = "A";
    System.out.println(userID.get(recommendUser));
    //???????????????????????????????????????????????????
    int recommendUserId = userID.get(recommendUser);
    for (int j = 0; j < sparseMatrix.length; j++) {
        if(j != recommendUserId){
          // System.out.println(idUser.get(recommendUserId)+"--"+idUser.get(j)+"?????????:"+sparseMatrix[recommendUserId][j]/Math.sqrt(userItemLength.get(idUser.get(recommendUserId))*userItemLength.get(idUser.get(j))));
        }
    }
    
    //??????????????????recommendUser??????????????????
    for(String item: items){//?????????????????????
      Set<String> users = itemUserCollection.get(item); //?????????????????????????????????????????????
      if(!users.contains(recommendUser)){ //????????????????????????????????????????????????????????????????????????
        double itemRecommendDegree = 0.0;
        for(String user: users){
          // itemRecommendDegree += sparseMatrix[userID.get(recommendUser)][userID.get(user)]/Math.sqrt(userItemLength.get(recommendUser)*userItemLength.get(user));//???????????????
        }
        // System.out.println("The item "+item+" for "+recommendUser +"'s recommended degree:"+itemRecommendDegree);
      }
    }
  }
}
