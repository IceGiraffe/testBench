package site.ycsb.workloads;


import java.util.ArrayList;
import java.io.File;
import java.io.*;
import java.util.HashMap;

// import javafx.collections.transformation.FilteredList;
import site.ycsb.generator.*;

import java.util.*;

import site.ycsb.*;
import java.io.BufferedReader;  
import java.io.FileInputStream;  
import java.io.InputStreamReader;  
import java.util.ArrayList;  
import java.util.Random;  

class Edge implements Cloneable{  
    int v;     //v表示连接点的编号,w表示此边的权值  
    double weight;  
    int next;    //next负责连接和此点相关的边  
    Edge(){}  
    public Object clone(){  
        Edge temp=null;  
        try{    
            temp = (Edge)super.clone();   //浅复制    
        }catch(CloneNotSupportedException e) {    
            e.printStackTrace();    
        }     
        return temp;  
    }  
}  

class Louvain implements Cloneable{  
    int n; // 结点个数  
    int m; // 边数  
    int cluster[]; // 结点i属于哪个簇  
    Edge edge[]; // 邻接表  
    int head[]; // 头结点下标  
    int top; // 已用E的个数  
    double resolution; // 1/2m 全局不变  
    double node_weight[]; // 结点的权值  
    double totalEdgeWeight; // 总边权值  
    double[] cluster_weight; // 簇的权值  
    double eps = 1e-14; // 误差  
  
    int global_n; // 最初始的n  
    int global_cluster[]; // 最后的结果，i属于哪个簇  
    Edge[] new_edge;   //新的邻接表  
    int[] new_head;  
    int new_top = 0;  
    final int iteration_time = 3; // 最大迭代次数  
      
    Edge global_edge[];   //全局初始的临接表  只保存一次，永久不变，不参与后期运算  
    int global_head[];  
    int global_top=0;  
      
    void addEdge(int u, int v, double weight) {    
        if(edge[top]==null)  
            edge[top]=new Edge();  
        edge[top].v = v;  
        edge[top].weight = weight;  
        edge[top].next = head[u];  
        head[u] = top++;  
    }  
  
    void addNewEdge(int u, int v, double weight) {  
        if(new_edge[new_top]==null)  
            new_edge[new_top]=new Edge();  
        new_edge[new_top].v = v;  
        new_edge[new_top].weight = weight;  
        new_edge[new_top].next = new_head[u];  
        new_head[u] = new_top++;  
    }  
      
    void addGlobalEdge(int u, int v, double weight) {  
        if(global_edge[global_top]==null)  
            global_edge[global_top]=new Edge();  
        global_edge[global_top].v = v;  
        global_edge[global_top].weight = weight;  
        global_edge[global_top].next = global_head[u];  
        global_head[u] = global_top++;  
    }  
  
    void init(Vector<HashMap<String, ByteIterator>> s) {  
        try {  
            String encoding = "UTF-8";  
            // File file = new File(filePath);  
            if (true) { // 判断文件是否存在  
                // InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式  
                // BufferedReader bufferedReader = new BufferedReader(read);  
                // String lineTxt = null;  
                ////// 预处理部分 
                String s1 = "id0";
    			String s2 = "id1";
                HashMap<String, ByteIterator> lineTxt = new HashMap<String, ByteIterator>();
                int temp_ = 0;
                for (HashMap<String, ByteIterator> line : s) {
                    if (temp_ == 0)
                    {
                        lineTxt = line;
                        temp_++;
                        break;
                    }
                }
                global_n = n = Integer.parseInt(lineTxt.get(s1).toString());  
                m = Integer.parseInt(lineTxt.get(s2).toString());  
                m *= 2;  
                edge = new Edge[m];  
                head = new int[n];  
                for (int i = 0; i < n; i++)  
                    head[i] = -1;  
                top = 0;  
                  
                global_edge=new Edge[m];  
                global_head = new int[n];  
                for(int i=0;i<n;i++)  
                    global_head[i]=-1;  
                global_top=0;  
                global_cluster = new int[n];  
                for (int i = 0; i < global_n; i++)  
                    global_cluster[i] = i;  
                node_weight = new double[n];  
                totalEdgeWeight = 0.0;  
                int temp = 0;
                for (HashMap<String, ByteIterator> line : s) {  
                	if (temp == 0)
                	{
                		temp++;
                		continue;
                	}
                    int u = Integer.parseInt(line.get(s1).toString());  
                    int v = Integer.parseInt(line.get(s2).toString());  
                    double curw;  
                    curw = 1.0;  
                    addEdge(u, v, curw);  
                    addEdge(v, u, curw);  
                    addGlobalEdge(u,v,curw);  
                    addGlobalEdge(v,u,curw);  
                    totalEdgeWeight += 2 * curw;  
                    node_weight[u] += curw;  
                    if (u != v) {  
                        node_weight[v] += curw;  
                    }  
                }  
                resolution = 1 / totalEdgeWeight;  
            } else {  
                System.out.println("找不到指定的文件");  
            }  
        } catch (Exception e) {  
            System.out.println("读取文件内容出错");  
            e.printStackTrace();  
        }  
    }  
  
    void init_cluster() {  
        cluster = new int[n];  
        for (int i = 0; i < n; i++) { // 一个结点一个簇  
            cluster[i] = i;  
        }  
    }  
  
    boolean try_move_i(int i) { // 尝试将i加入某个簇  
        double[] edgeWeightPerCluster = new double[n];  
        for (int j = head[i]; j != -1; j = edge[j].next) {  
            int l = cluster[edge[j].v]; // l是nodeid所在簇的编号  
            edgeWeightPerCluster[l] += edge[j].weight;  
        }  
        int bestCluster = -1; // 最优的簇号下标(先默认是自己)  
        double maxx_deltaQ = 0.0; // 增量的最大值  
        boolean[] vis = new boolean[n];  
        cluster_weight[cluster[i]] -= node_weight[i];  
        for (int j = head[i]; j != -1; j = edge[j].next) {  
            int l = cluster[edge[j].v]; // l代表領接点的簇号  
            if (vis[l]) // 一个領接簇只判断一次  
                continue;  
            vis[l] = true;  
            double cur_deltaQ = edgeWeightPerCluster[l];  
            cur_deltaQ -= node_weight[i] * cluster_weight[l] * resolution;  
            if (cur_deltaQ > maxx_deltaQ) {  
                bestCluster = l;  
                maxx_deltaQ = cur_deltaQ;  
            }  
            edgeWeightPerCluster[l] = 0;  
        }  
        if (maxx_deltaQ < eps) {  
            bestCluster = cluster[i];  
        }  
         //System.out.println(maxx_deltaQ);  
        cluster_weight[bestCluster] += node_weight[i];  
        if (bestCluster != cluster[i]) { // i成功移动了  
            cluster[i] = bestCluster;  
            return true;  
        }  
        return false;  
    }  
  
    void rebuildGraph() { // 重构图  
        /// 先对簇进行离散化  
        int[] change = new int[n];  
        int change_size=0;  
        boolean vis[] = new boolean[n];  
        for (int i = 0; i < n; i++) {  
            if (vis[cluster[i]])  
                continue;  
            vis[cluster[i]] = true;  
            change[change_size++]=cluster[i];  
        }  
        int[] index = new int[n]; // index[i]代表 i号簇在新图中的结点编号  
        for (int i = 0; i < change_size; i++)  
            index[change[i]] = i;  
        int new_n = change_size; // 新图的大小  
        new_edge = new Edge[m];  
        new_head = new int[new_n];  
        new_top = 0;  
        double new_node_weight[] = new double[new_n]; // 新点权和  
        for(int i=0;i<new_n;i++)  
            new_head[i]=-1;  
          
        ArrayList<Integer>[] nodeInCluster = new ArrayList[new_n]; // 代表每个簇中的节点列表  
        for (int i = 0; i < new_n; i++)  
            nodeInCluster[i] = new ArrayList<Integer>();  
        for (int i = 0; i < n; i++) {  
            nodeInCluster[index[cluster[i]]].add(i);  
        }  
        for (int u = 0; u < new_n; u++) { // 将同一个簇的挨在一起分析。可以将visindex数组降到一维  
            boolean visindex[] = new boolean[new_n]; // visindex[v]代表新图中u节点到v的边在临街表是第几个（多了1，为了初始化方便）  
            double delta_w[] = new double[new_n]; // 边权的增量  
            for (int i = 0; i < nodeInCluster[u].size(); i++) {  
                int t = nodeInCluster[u].get(i);  
                for (int k = head[t]; k != -1; k = edge[k].next) {  
                    int j = edge[k].v;  
                    int v = index[cluster[j]];  
                    if (u != v) {  
                        if (!visindex[v]) {  
                            addNewEdge(u, v, 0);  
                            visindex[v] = true;  
                        }   
                        delta_w[v] += edge[k].weight;  
                    }  
                }  
                new_node_weight[u] += node_weight[t];  
            }  
            for (int k = new_head[u]; k != -1; k = new_edge[k].next) {  
                int v = new_edge[k].v;  
                new_edge[k].weight = delta_w[v];  
            }  
        }  
  
        // 更新答案  
        int[] new_global_cluster = new int[global_n];  
        for (int i = 0; i < global_n; i++) {  
            new_global_cluster[i] = index[cluster[global_cluster[i]]];  
        }  
        for (int i = 0; i < global_n; i++) {  
            global_cluster[i] = new_global_cluster[i];  
        }  
        top = new_top;  
        for (int i = 0; i < m; i++) {  
            edge[i] = new_edge[i];  
        }  
        for (int i = 0; i < new_n; i++) {  
            node_weight[i] = new_node_weight[i];  
            head[i] = new_head[i];  
        }  
        n = new_n;  
        init_cluster();  
    }  
  
    void print() {  
        for (int i = 0; i < global_n; i++) {  
            System.out.println(i + ": " + global_cluster[i]);  
        }  
        System.out.println("-------");  
    }  
  
    void louvain() {  
        init_cluster();  
        int count = 0; // 迭代次数  
        boolean update_flag; // 标记是否发生过更新  
        do { // 第一重循环，每次循环rebuild一次图  
        //    print(); // 打印簇列表  
            count++;  
            cluster_weight = new double[n];  
            for (int j = 0; j < n; j++) { // 生成簇的权值  
                cluster_weight[cluster[j]] += node_weight[j];  
            }  
            int[] order = new int[n]; // 生成随机序列  
            for (int i = 0; i < n; i++)  
                order[i] = i;  
            Random random = new Random();  
            for (int i = 0; i < n; i++) {  
                int j = random.nextInt(n);  
                int temp = order[i];  
                order[i] = order[j];  
                order[j] = temp;  
            }  
            int enum_time = 0; // 枚举次数，到n时代表所有点已经遍历过且无移动的点  
            int point = 0; // 循环指针  
            update_flag = false; // 是否发生过更新的标记  
            do {  
                int i = order[point];  
                point = (point + 1) % n;  
                if (try_move_i(i)) { // 对i点做尝试  
                    enum_time = 0;  
                    update_flag = true;  
                } else {  
                    enum_time++;  
                }  
            } while (enum_time < n);  
            if (count > iteration_time || !update_flag) // 如果没更新过或者迭代次数超过指定值  
                break;  
            rebuildGraph(); // 重构图  
        } while (true);  
    }  
}  

public class LouvainWorkload extends CoreWorkload{

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

  @Override
  public void init(Properties p) throws WorkloadException {

    // filePath = p.getProperty(DATA_PROPERTY);
    // BUSINESS: Choose a kind of query (choose a kind of algorithms of BUSINESS)(*)
    // BUSINESS: Store file list
    // fileName = new ArrayList<String>();
    // fileList = new ArrayList<File>();
    // insertCount = Integer.parseInt(p.getProperty(INSERT_COUNT_PROPERTY));
    //getAllFileName("./TPC_DS_DataGen/v2.8.0rc4/tools/dataGen/", fileName, fileList);
    //filesequence = new CounterGenerator(0);
    // getAllFileName(filePath, fileName, fileList);
    //fileName.add("webGraph.txt");
    recordOrder = 0;
    tableAndFieldNames = new HashMap<>();
    tableAndFieldNames.put("community", new String[]{"_id", "id0", "id1"});
    buildRecord();
  }

  public void buildRecord() {
    try {
      recordOrder = 0;
      BufferedReader br = new BufferedReader(new FileReader("/home/test/Bench/facebook_combined.txt"));
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
    //Chosse a files
    tableName = "community";
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
          System.out.println(fieldValues[1]);
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
    Set<String> fSet = new HashSet<String>(Arrays.asList(tableAndFieldNames.get("community")));
    db.scan("community", buildKeyName(1), scaleTable, fSet, res);
    Louvain a = new Louvain();  
    double beginTime = System.currentTimeMillis();  
    a.init(res);  
    a.louvain();  
    return true;
  }

}
