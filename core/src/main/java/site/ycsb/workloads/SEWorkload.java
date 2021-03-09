package site.ycsb.workloads;


import java.util.ArrayList;
import java.io.File;
import java.io.*;
import java.util.HashMap;

import site.ycsb.generator.*;

import java.util.*;

import site.ycsb.*;

import com.hankcs.hanlp.*;

/**
 * A class extends the CoreWrrklaod with Search Engine specification.
 */
public class SEWorkload extends Workload {
  /**
   * The scale of data.
   */
  public static final String DATA_PROPERTY = "path";
  public static final String DATA_DIR_PROPERTY_DEFAULT = "./dataGen/";
  //the same variable in the Client.java
  public static final String QUERY_INDEX = "queryindex";
  public static final String QUERY_TASK = "task";
  public static final String INSERT_COUNT_PROPERTY = "insertcount";
  protected NumberGenerator filesequence;
  protected HashMap<String, String> fieldProperties = new HashMap<String, String>();
  protected HashMap<String, String[]> tableAndFieldNames;
  protected String scale;
  protected String dataDir;
  protected ArrayList<String> fileName;
  protected ArrayList<File> fileList;
  protected String queryindex;
  protected String inputFileName;
  protected String tableName;
  protected String task;
  protected String filePath;
  protected int scaleTable;
  protected int insertionRetryLimit = 0;
  protected int insertionRetryInterval = 3;
  protected boolean orderedinserts = true;
  protected int zeropadding = 1;
  protected int recordOrder;
  protected Iterator<String> recordIt;
  protected Iterator<File> fileIt;
  protected List<String> records;
  protected int insertCount;
  // 5.1 initiate workloads depends on implementations of workloads (prepare for loading data)(*)
  @Override
  public void init(Properties p) throws WorkloadException {

    if (p.getProperty(QUERY_TASK).equals("PageRank")) {
      task = "PageRank";
    } else {
      task = "NLP";
    }
    filePath = p.getProperty(DATA_PROPERTY);
    // BUSINESS: Choose a kind of query (choose a kind of algorithms of BUSINESS)(*)
    // BUSINESS: Store file list
    fileName = new ArrayList<String>();
    fileList = new ArrayList<File>();
    insertCount = Integer.parseInt(p.getProperty(INSERT_COUNT_PROPERTY));
    //getAllFileName("./TPC_DS_DataGen/v2.8.0rc4/tools/dataGen/", fileName, fileList);
    //filesequence = new CounterGenerator(0);
    getAllFileName(filePath, fileName, fileList);
    //fileName.add("webGraph.txt");
    recordOrder = 0;
    tableAndFieldNames = new HashMap<>();
    tableAndFieldNames.put("PageRank", new String[]{"_id", "id0", "id1"});
    tableAndFieldNames.put("NLP", new String[]{"pageId", "rawText", "top5KeyWords"});
    if (task == "PageRank") {
      buildPRRecord();
    } else {
      fileIt = fileList.iterator();
      recordOrder = 0;
    }
  }

  // 7.2.2. Load data

  public void buildPRRecord() {
    try {
      recordOrder = 0;
      BufferedReader br = new BufferedReader(new FileReader((fileList.get(0))));
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


  @Override
  public boolean doInsert(DB db, Object threadstate) {

    tableName = task;
    // Insert data line by line
    HashMap<String, ByteIterator> values;
    String[] fieldValues;
    ByteIterator data;
    //String record;
    Status status = null;
    int numOfRetries;
    //int count;
    BufferedReader br;
    Map<Integer, Integer> edges = new HashMap<Integer, Integer>();
    fieldValues = tableAndFieldNames.get(tableName);
    if (task == "PageRank") {
      try {
          String line = "0 0";
          if (recordIt.hasNext()) {
            line = recordIt.next();
          }
          while (line.startsWith("#")) {
            if (recordIt.hasNext()) {
              line = recordIt.next();
            }
          }
          recordOrder++;
          String[] strList = line.split("\t");
          values = new HashMap<>();

          for (int i = 1; i < fieldValues.length; i++) {
            data = new StringByteIterator(strList[i-1]);
            values.put(fieldValues[i], data);
          }
          //v = Integer.parseInt(strList[0]);
          //w = Integer.parseInt(strList[1]);
          //edges.put(v, w);
          numOfRetries = 0;
          scaleTable = recordOrder;
          do {
            String keys = buildKeyName(recordOrder);
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

      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      try {
        ArrayList<String> content;
        String line;
        File tmpfile = fileList.get(0);

          if (fileIt.hasNext()) {
            tmpfile = fileIt.next();
          }
          content = new ArrayList<String>();
          br = new BufferedReader(new FileReader(tmpfile));
          while ((line = br.readLine()) != null) {
            content.add(line);
          }
          values = new HashMap<>();
          data = new StringByteIterator(String.join("", content));
          values.put("pageId", new StringByteIterator(tmpfile.getName()));
          values.put("rawText", data);
          values.put("Top5KeyWords", new StringByteIterator(""));


          numOfRetries = 0;
          do {
            status = db.insert(tableName, buildKeyName(++recordOrder), values);
            scaleTable = recordOrder;
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

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return true;
  }


  public static int runShellCommand(String shellCommand) {
    Process process = null;
    int result = 0;
    try {
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

  @Override
  public void cleanup()  throws WorkloadException {

  }

  public static void getAllFileName(String path, ArrayList<String> fileName, ArrayList<File> fileList) {
    File file = new File(path);
    File[] tmpLists = file.listFiles();
    for (int i = 0; i < tmpLists.length; i++) {
      if (tmpLists[i].isFile()) {
        fileName.add(tmpLists[i].getName());
        fileList.add(tmpLists[i]);
      }
    }
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

  // 7.2.1. Run workloads (choose a kind of algorithms of BUSINESS to run)(*)
  @Override
  public boolean doTransaction(DB db, Object threadstate) {


    // BUSINESS: run sql query according to predefined queryindex
    //YYB: Execute sql script for business workload
    if (task == "PageRank") {

      Vector<HashMap<String, ByteIterator>> res = new Vector<HashMap<String, ByteIterator>>();
      Set<String> fSet = new HashSet<String>(Arrays.asList(tableAndFieldNames.get(task)));
      db.scan(task, buildKeyName(1), scaleTable, fSet, res);
      Graph g = new Graph();

      g.readVector(res);
      g.calPageRank();
      try {
        g.writePageRanks("TEST.TXT");

      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      Vector<HashMap<String, ByteIterator>> res = new Vector<HashMap<String, ByteIterator>>();
      Set<String> fSet = new HashSet<String>(Arrays.asList(tableAndFieldNames.get(task)));
      db.scan(task, buildKeyName(1), scaleTable, fSet, res);
      List<String> keywordList;
      String tmpText;
      HashMap<String, ByteIterator> tmp;
      for (int i = 0; i < res.size(); i++) {
        tmp = res.get(i);
        tmpText = tmp.get("rawText").toString();
        keywordList = HanLP.extractKeyword(tmpText, 5);
        res.get(i).put("Top5KeyWords", new StringByteIterator(String.join("\t", keywordList)));
        db.update("NLP", buildKeyName(i), res.get(i));
      }
    }
    //sqlFilePath = "./business_workload/query" + queryindex + ".sql";
    //System.out.println("\n" + sqlFilePath + "\n");
    // 7.2.1.1. modify operations of specific databases
    //db.execScriptFile(db, sqlFilePath);
    return true;
  }

  public class Graph {
    private Set<Integer> vertexSet = new HashSet<Integer>();
    Map<Integer, List<Integer>> adj = new HashMap<Integer, List<Integer>>();//in edges
    Map<Integer, Integer> outEdges = new HashMap<Integer, Integer>(); // out edges degree
    Map<Integer, Double> pageRanks = new HashMap<Integer, Double>();
    double eps = 1e-8;
    double factor = 0.85;
    public int edgeNums = 0;

    public Graph() {
    }

    ;

    public Graph(Set<Integer> vertexSet, Map<Integer, List<Integer>> adjs, int nums) {
      this.vertexSet = vertexSet;
      this.adj = adjs;
      this.edgeNums = nums;
    }

    public int addEdge(int node1, int node2) {
      if (!vertexSet.contains(node1)) {
        vertexSet.add(node1);
        pageRanks.put(node1, 1.0);
        adj.put(node1, new LinkedList<Integer>());
      }
      if (!vertexSet.contains(node2)) {
        vertexSet.add(node2);
        pageRanks.put(node2, 1.0);
        adj.put(node2, new LinkedList<Integer>());
      }
      if (!outEdges.containsKey(node2)) {
        outEdges.put(node2, 1);
      } else {
        int var = outEdges.get(node2);
        outEdges.put(node2, var + 1);
      }
      adj.get(node1).add(node2);
      return 0;
    }

    public void readVector(Vector<HashMap<String, ByteIterator>> s) {
      String s1 = "id0";
      String s2 = "id1";
      Integer v, w;
      for (HashMap<String, ByteIterator> line : s) {
        v = Integer.parseInt(line.get(s1).toString());
        w = Integer.parseInt(line.get(s2).toString());
        this.addEdge(w, v);
      }
    }

    public void readFile(String fin) throws IOException {
      BufferedReader br = new BufferedReader(new FileReader(fin));
      String line = null;
      String[] strList;
      Integer v, w;
      while ((line = br.readLine()) != null) {
        if (!line.startsWith("#")) {
          strList = line.split("\t");
          //System.out.println(strList);
          v = Integer.parseInt(strList[0]);
          w = Integer.parseInt(strList[1]);
          //System.out.print(v);
          //System.out.println(w);
          this.addEdge(w, v);
        }
      }
      //this.pageRanks = new double[vertexSet.size()];
      //Arrays.fill(pageRanks,1.0);

    }

    public void getNumber() {
      System.out.println("Vertex: " + this.vertexSet.size());
      System.out.println("Edges: " + this.edgeNums);
    }

    public void writeFiles(String fout) throws IOException {
      BufferedWriter bw = new BufferedWriter(new FileWriter(fout));
      Iterator it = vertexSet.iterator();
      Integer v, w;
      List<Integer> lst;
      while (it.hasNext()) {
        v = (Integer) it.next();
        bw.write(v.toString());
        lst = adj.get(v);
        for (int i = 0; i < lst.size(); i++) {
          bw.write("\t");
          bw.write(lst.get(i).toString());
        }
        bw.write("\r\n");
        bw.flush();
      }
      bw.close();
    }

    public void calPageRank() {

      double common = (1 - this.factor) / this.vertexSet.size();
      while (true) {
        for (Integer n : vertexSet) {
          double sum = 0.0;
          for (Integer node : adj.get(n)) {
            sum += pageRanks.get(node) / outEdges.get(node);
          }
          double newPR = common + this.factor * sum;
          if (Math.abs(pageRanks.get(n) - newPR) > eps) {
            pageRanks.put(n, newPR);
          } else {
            return;
          }
        }
      }
    }

    public void writePageRanks(String outFile) throws IOException {
      BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
      for (Integer n : pageRanks.keySet()) {
        bw.write(n.toString() + " " + pageRanks.get(n).toString() + "\r\n");
      }
      bw.flush();
      bw.close();
    }
  }


}
