/**
 * Copyright (c) 2010 - 2016 Yahoo! Inc., 2016 YCSB contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package site.ycsb.db;

import site.ycsb.DB;
import site.ycsb.DBException;
import site.ycsb.ByteIterator;
import site.ycsb.Status;
import site.ycsb.StringByteIterator;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import site.ycsb.db.flavors.DBFlavor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

 
/**
 * A class that wraps a JDBC compliant database to allow it to be interfaced
 * with YCSB. This class extends {@link DB} and implements the database
 * interface used by YCSB client.
 *
 * <br>
 * Each client will have its own instance of this class. This client is not
 * thread safe.
 *
 * <br>
 * This interface expects a schema <key> <field1> <field2> <field3> ... All
 * attributes are of type TEXT. All accesses are through the primary key.
 * Therefore, only one index on the primary key is needed.
 */
public class JdbcDBClient extends DB {

  /** The class to use as the jdbc driver. */
  public static final String DRIVER_CLASS = "db.driver";

  /** The URL to connect to the database. */
  public static final String CONNECTION_URL = "db.url";

  /** The user name to use to connect to the database. */
  public static final String CONNECTION_USER = "db.user";

  /** The password to use for establishing the connection. */
  public static final String CONNECTION_PASSWD = "db.passwd";

  /** The batch size for batched inserts. Set to >0 to use batching */
  public static final String DB_BATCH_SIZE = "db.batchsize";

  /** The JDBC fetch size hinted to the driver. */
  public static final String JDBC_FETCH_SIZE = "jdbc.fetchsize";

  /** The JDBC connection auto-commit property for the driver. */
  public static final String JDBC_AUTO_COMMIT = "jdbc.autocommit";

  public static final String JDBC_BATCH_UPDATES = "jdbc.batchupdateapi";

  /** The name of the property for the number of fields in a record. */
  public static final String FIELD_COUNT_PROPERTY = "fieldcount";

  /** Default number of fields in a record. */
  public static final String FIELD_COUNT_PROPERTY_DEFAULT = "10";

  /** Representing a NULL value. */
  public static final String NULL_VALUE = "NULL";

  /** The primary key in the user table. */
  public static final String PRIMARY_KEY = "YCSB_KEY";

  /** The field name prefix in the table. */
  public static final String COLUMN_PREFIX = "FIELD";

  private List<Connection> conns;
  private boolean initialized = false;
  private Properties props;
  private int jdbcFetchSize;
  private int batchSize;
  private boolean autoCommit;
  private boolean batchUpdates;
  private static final String DEFAULT_PROP = "";
  private ConcurrentMap<StatementType, PreparedStatement> cachedStatements;
  private long numRowsInBatch = 0;
  /** DB flavor defines DB-specific syntax and behavior for the
   * particular database. Current database flavors are: {default, phoenix} */
  private DBFlavor dbFlavor;

  /**
   * Execute mysql script.
   */
  // 7.2.1.1. modify operations of specific databases
  public Status execScriptFile(DB db, String sqlFilePath) {
    String sql = getText(sqlFilePath);
    List<String> sqlarr = getSql(sql);
    for(int i=0; i<10; i++){
      System.out.println(i+":"+sqlarr.get(i));
    }
    try{
      execute(getConn(db), sqlarr);
      System.out.println("QUERY SUCCESSFULLY.");
    }catch(Exception e){
      e.printStackTrace();
      return Status.ERROR;
    }
    return Status.OK;
  }

  private static Connection getConn(DB db) {
    Properties props = db.getProperties();
    String urls = props.getProperty(CONNECTION_URL, DEFAULT_PROP);
    String user = props.getProperty(CONNECTION_USER, DEFAULT_PROP);
    String passwd = props.getProperty(CONNECTION_PASSWD, DEFAULT_PROP);
    String driver = props.getProperty(DRIVER_CLASS);
    Connection conn = null;
    try {
      Class.forName(driver);
      conn = (Connection) DriverManager.getConnection(urls, user, passwd);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return conn;
  }

  public static void execute(Connection conn, List<String> sqlFile) throws Exception {  
    Statement stmt = null;  
    stmt = conn.createStatement();  
    for (String sql : sqlFile) {  
      sql = sql.trim();
      if(sql != null && !sql.equals("")) {
        stmt.addBatch(sql);
      }
    }
    int[] rows = stmt.executeBatch();  
    System.out.println("Row count:" + Arrays.toString(rows));  
    conn.close();
  }

  public static String getText(String path){
    File file = new File(path);
    if(!file.exists()||file.isDirectory()){
      return null;
    }
    StringBuilder sb = new StringBuilder();
    try {
      FileInputStream fis = new FileInputStream(path);
      InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
      BufferedReader br = new BufferedReader(isr);
      String temp = null;
      temp = br.readLine();
      while(temp!=null) {
        if(temp.length()>=2){
          String str1 = temp.substring(0, 1);
          String str2 = temp.substring(0, 2);
          if(str1.equals("#")||str2.equals("--")||str2.equals("/*")||str2.equals("//")){
            temp = br.readLine();
            continue;
          }
          sb.append(temp+"\r\n");
        }
        temp = br.readLine();
      }
      br.close();
    } catch(Exception e){
      e.printStackTrace();
    }
    return sb.toString();
  }

  public static List<String> getSql(String sql){
    String s = sql;
    s = s.replaceAll("\r\n", "\r");
    s = s.replaceAll("\r", "\n");
    List<String> ret = new ArrayList<String>();
    String[] sqlarry = s.split(";");
    sqlarry = filter(sqlarry);
    ret = Arrays.asList(sqlarry);
    return ret;
  }

  public static String[] filter(String[] ss){
    List<String> strs = new ArrayList<String>();
    for(String s : ss){
      if(s!=null&&!s.equals("")){
        strs.add(s);
      }
    }
    String[] result = new String[strs.size()];
    for(int i=0; i<strs.size(); i++){
      result[i] = strs.get(i).toString();
    }
    return result;
  }


  /**
  public static void execSqlFileByMysql(String sqlFilePath) throws Exception{  
    Exception error = null;  
    String urls = props.getProperty(CONNECTION_URL, DEFAULT_PROP);
    String user = props.getProperty(CONNECTION_USER, DEFAULT_PROP);
    String passwd = props.getProperty(CONNECTION_PASSWD, DEFAULT_PROP);
    String driver = props.getProperty(DRIVER_CLASS);
    Connection conn = null;
    try {  
      Class.forName(driver);
      conn = DriverManager.getConnection(url, user, passwd);
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setAutoCommit(true);
      runner.setFullLineDelimiter(false);
      runner.setDelimiter(";");
      runner.setSendFullScript(false);  
      runner.setStopOnError(false);  
    //runner.setLogWriter(null); 
      runner.runScript(new InputStreamReader(new FileInputStream(sqlFilePath), "utf-8"));  
      close(conn);  
    } catch (Exception e) {  
      LOG.error("Failure....", e);  
      error = e;  
    }finally{  
      close(conn);  
    }  
    if(error != null){  
      throw error;  
    }  
  }

  
  public static void execSqlFileBySqlserver(String ip, String port, String userName, String pwd, String sqlFilePath, String dbName) throws Exception{  
    String driver = "net.sourceforge.jtds.jdbc.Driver";  
    String url = "jdbc:jtds:sqlserver://"+ip+":"+port;           
    Exception error = null;  
    Connection conn = null;  
    try {  
      replaceAndCreate(f1, f2, oldDbName, newDbName);  
      Class.forName(driver);  
      conn = DriverManager.getConnection(url, userName, pwd);  
      ScriptRunner runner = new ScriptRunner(conn);  
      runner.setAutoCommit(true);
      runner.setFullLineDelimiter(true);  
      runner.setDelimiter("GO");  
      runner.setSendFullScript(false);  
      runner.setStopOnError(false);  
    //runner.setLogWriter(null);  
      runner.runScript(new InputStreamReader(new FileInputStream(sqlFilePath), "utf8"));   
    } catch (Exception e) {  
      LOG.error("Failure....", e);  
      error = e;  
    }finally{  
      close(conn);   
      deleteFile(f2);  
    }  
    if(error != null){  
      throw error;  
    }
  }
  .**/

  private static void close(Connection conn){
    try {
      if(conn != null){
        conn.close();
      }
    } catch (Exception e) {
      if(conn != null){
        conn = null;
      }
    }
  }

  /**
   * Ordered field information for insert and update statements.
   */
  private static class OrderedFieldInfo {
    private String fieldKeys;
    private List<String> fieldValues;

    OrderedFieldInfo(String fieldKeys, List<String> fieldValues) {
      this.fieldKeys = fieldKeys;
      this.fieldValues = fieldValues;
    }

    String getFieldKeys() {
      return fieldKeys;
    }

    List<String> getFieldValues() {
      return fieldValues;
    }
  }

  /**
   * For the given key, returns what shard contains data for this key.
   *
   * @param key Data key to do operation on
   * @return Shard index
   */
  private int getShardIndexByKey(String key) {
    int ret = Math.abs(key.hashCode()) % conns.size();
    return ret;
  }

  /**
   * For the given key, returns Connection object that holds connection to the
   * shard that contains this key.
   *
   * @param key Data key to get information for
   * @return Connection object
   */
  private Connection getShardConnectionByKey(String key) {
    return conns.get(getShardIndexByKey(key));
  }

  private void cleanupAllConnections() throws SQLException {
    for (Connection conn : conns) {
      if (!autoCommit) {
        conn.commit();
      }
      conn.close();
    }
  }

  /** Returns parsed int value from the properties if set, otherwise returns -1. */
  private static int getIntProperty(Properties props, String key) throws DBException {
    String valueStr = props.getProperty(key);
    if (valueStr != null) {
      try {
        return Integer.parseInt(valueStr);
      } catch (NumberFormatException nfe) {
        System.err.println("Invalid " + key + " specified: " + valueStr);
        throw new DBException(nfe);
      }
    }
    return -1;
  }

  /** Returns parsed boolean value from the properties if set, otherwise returns defaultVal. */
  private static boolean getBoolProperty(Properties props, String key, boolean defaultVal) {
    String valueStr = props.getProperty(key);
    if (valueStr != null) {
      return Boolean.parseBoolean(valueStr);
    }
    return defaultVal;
  }

  // 7.1 initiate database
  @Override
  public void init() throws DBException {
    if (initialized) {
      System.err.println("Client connection already initialized.");
      return;
    }
    props = getProperties();
    String urls = props.getProperty(CONNECTION_URL, DEFAULT_PROP);
    String user = props.getProperty(CONNECTION_USER, DEFAULT_PROP);
    String passwd = props.getProperty(CONNECTION_PASSWD, DEFAULT_PROP);
    String driver = props.getProperty(DRIVER_CLASS);

    this.jdbcFetchSize = getIntProperty(props, JDBC_FETCH_SIZE);
    this.batchSize = getIntProperty(props, DB_BATCH_SIZE);

    this.autoCommit = getBoolProperty(props, JDBC_AUTO_COMMIT, true);
    this.batchUpdates = getBoolProperty(props, JDBC_BATCH_UPDATES, false);

    try {
      if (driver != null) {
        Class.forName(driver);
      }
      int shardCount = 0;
      conns = new ArrayList<Connection>(3);
      final String[] urlArr = urls.split(",");
      for (String url : urlArr) {
        System.out.println("Adding shard node URL: " + url);
        Connection conn = DriverManager.getConnection(url, user, passwd);

        // Since there is no explicit commit method in the DB interface, all
        // operations should auto commit, except when explicitly told not to
        // (this is necessary in cases such as for PostgreSQL when running a
        // scan workload with fetchSize)
        conn.setAutoCommit(autoCommit);

        shardCount++;
        conns.add(conn);
      }

      System.out.println("Using shards: " + shardCount + ", batchSize:" + batchSize + ", fetchSize: " + jdbcFetchSize);

      cachedStatements = new ConcurrentHashMap<StatementType, PreparedStatement>();

      this.dbFlavor = DBFlavor.fromJdbcUrl(urlArr[0]);
    } catch (ClassNotFoundException e) {
      System.err.println("Error in initializing the JDBS driver: " + e);
      throw new DBException(e);
    } catch (SQLException e) {
      System.err.println("Error in database operation: " + e);
      throw new DBException(e);
    } catch (NumberFormatException e) {
      System.err.println("Invalid value for fieldcount property. " + e);
      throw new DBException(e);
    }

    initialized = true;
  }

  @Override
  public void cleanup() throws DBException {
    if (batchSize > 0) {
      try {
        // commit un-finished batches
        for (PreparedStatement st : cachedStatements.values()) {
          if (!st.getConnection().isClosed() && !st.isClosed() && (numRowsInBatch % batchSize != 0)) {
            st.executeBatch();
          }
        }
      } catch (SQLException e) {
        System.err.println("Error in cleanup execution. " + e);
        throw new DBException(e);
      }
    }

    try {
      cleanupAllConnections();
    } catch (SQLException e) {
      System.err.println("Error in closing the connection. " + e);
      throw new DBException(e);
    }
  }

  private PreparedStatement createAndCacheInsertStatement(StatementType insertType, String key)
      throws SQLException {
    String insert = dbFlavor.createInsertStatement(insertType, key);
    PreparedStatement insertStatement = getShardConnectionByKey(key).prepareStatement(insert);
    PreparedStatement stmt = cachedStatements.putIfAbsent(insertType, insertStatement);
    if (stmt == null) {
      return insertStatement;
    }
    return stmt;
  }

  private PreparedStatement createAndCacheReadStatement(StatementType readType, String key)
      throws SQLException {
    String read = dbFlavor.createReadStatement(readType, key);
    PreparedStatement readStatement = getShardConnectionByKey(key).prepareStatement(read);
    PreparedStatement stmt = cachedStatements.putIfAbsent(readType, readStatement);
    if (stmt == null) {
      return readStatement;
    }
    return stmt;
  }

  private PreparedStatement createAndCacheDeleteStatement(StatementType deleteType, String key)
      throws SQLException {
    String delete = dbFlavor.createDeleteStatement(deleteType, key);
    PreparedStatement deleteStatement = getShardConnectionByKey(key).prepareStatement(delete);
    PreparedStatement stmt = cachedStatements.putIfAbsent(deleteType, deleteStatement);
    if (stmt == null) {
      return deleteStatement;
    }
    return stmt;
  }

  private PreparedStatement createAndCacheUpdateStatement(StatementType updateType, String key)
      throws SQLException {
    String update = dbFlavor.createUpdateStatement(updateType, key);
    PreparedStatement insertStatement = getShardConnectionByKey(key).prepareStatement(update);
    PreparedStatement stmt = cachedStatements.putIfAbsent(updateType, insertStatement);
    if (stmt == null) {
      return insertStatement;
    }
    return stmt;
  }

  private PreparedStatement createAndCacheScanStatement(StatementType scanType, String key)
      throws SQLException {
    String select = dbFlavor.createScanStatement(scanType, key);
    PreparedStatement scanStatement = getShardConnectionByKey(key).prepareStatement(select);
    if (this.jdbcFetchSize > 0) {
      scanStatement.setFetchSize(this.jdbcFetchSize);
    }
    PreparedStatement stmt = cachedStatements.putIfAbsent(scanType, scanStatement);
    if (stmt == null) {
      return scanStatement;
    }
    return stmt;
  }

  @Override
  public Status read(String tableName, String key, Set<String> fields, Map<String, ByteIterator> result) {
    try {
      StatementType type = new StatementType(StatementType.Type.READ, tableName, 1, "", getShardIndexByKey(key));
      PreparedStatement readStatement = cachedStatements.get(type);
      if (readStatement == null) {
        readStatement = createAndCacheReadStatement(type, key);
      }
      readStatement.setString(1, key);
      ResultSet resultSet = readStatement.executeQuery();
      if (!resultSet.next()) {
        resultSet.close();
        return Status.NOT_FOUND;
      }
      if (result != null && fields != null) {
        for (String field : fields) {
          String value = resultSet.getString(field);
          result.put(field, new StringByteIterator(value));
        }
      }
      resultSet.close();
      return Status.OK;
    } catch (SQLException e) {
      System.err.println("Error in processing read of table " + tableName + ": " + e);
      return Status.ERROR;
    }
  }

  @Override
  public Status scan(String tableName, String startKey, int recordcount, Set<String> fields,
                     Vector<HashMap<String, ByteIterator>> result) {
    try {
      StatementType type = new StatementType(StatementType.Type.SCAN, tableName, 1, "", getShardIndexByKey(startKey));
      PreparedStatement scanStatement = cachedStatements.get(type);
      if (scanStatement == null) {
        scanStatement = createAndCacheScanStatement(type, startKey);
      }
      scanStatement.setString(1, startKey);
      scanStatement.setInt(2, recordcount);
      ResultSet resultSet = scanStatement.executeQuery();
      for (int i = 0; i < recordcount && resultSet.next(); i++) {
        if (result != null && fields != null) {
          HashMap<String, ByteIterator> values = new HashMap<String, ByteIterator>();
          for (String field : fields) {
            String value = resultSet.getString(field);
            values.put(field, new StringByteIterator(value));
          }
          result.add(values);
        }
      }
      resultSet.close();
      return Status.OK;
    } catch (SQLException e) {
      System.err.println("Error in processing scan of table: " + tableName + e);
      return Status.ERROR;
    }
  }

  @Override
  public Status update(String tableName, String key, Map<String, ByteIterator> values) {
    try {
      int numFields = values.size();
      OrderedFieldInfo fieldInfo = getFieldInfo(values);
      StatementType type = new StatementType(StatementType.Type.UPDATE, tableName,
          numFields, fieldInfo.getFieldKeys(), getShardIndexByKey(key));
      PreparedStatement updateStatement = cachedStatements.get(type);
      if (updateStatement == null) {
        updateStatement = createAndCacheUpdateStatement(type, key);
      }
      int index = 1;
      for (String value: fieldInfo.getFieldValues()) {
        updateStatement.setString(index++, value);
      }
      updateStatement.setString(index, key);
      int result = updateStatement.executeUpdate();
      if (result == 1) {
        return Status.OK;
      }
      return Status.UNEXPECTED_STATE;
    } catch (SQLException e) {
      System.err.println("Error in processing update to table: " + tableName + e);
      return Status.ERROR;
    }
  }

  @Override
  public Status insert(String tableName, String key, Map<String, ByteIterator> values) {
    try {
      int numFields = values.size();
      OrderedFieldInfo fieldInfo = getFieldInfo(values);
      StatementType type = new StatementType(StatementType.Type.INSERT, tableName,
          numFields, fieldInfo.getFieldKeys(), getShardIndexByKey(key));
      PreparedStatement insertStatement = cachedStatements.get(type);
      if (insertStatement == null) {
        insertStatement = createAndCacheInsertStatement(type, key);
      }
      insertStatement.setString(1, key);
      int index = 2;
      for (String value: fieldInfo.getFieldValues()) {
        insertStatement.setString(index++, value);
      }
      
      // Using the batch insert API
      if (batchUpdates) {
        insertStatement.addBatch();
        // Check for a sane batch size
        if (batchSize > 0) {
          // Commit the batch after it grows beyond the configured size
          if (++numRowsInBatch % batchSize == 0) {
            int[] results = insertStatement.executeBatch();
            for (int r : results) {
              if (r != 1) {
                return Status.ERROR;
              }
            }
            // If autoCommit is off, make sure we commit the batch
            if (!autoCommit) {
              getShardConnectionByKey(key).commit();
            }
            return Status.OK;
          } // else, the default value of -1 or a nonsense. Treat it as an infinitely large batch.
        } // else, we let the batch accumulate
        // Added element to the batch, potentially committing the batch too.
        return Status.BATCHED_OK;
      } else {
        // Normal update
        int result = insertStatement.executeUpdate();
        // If we are not autoCommit, we might have to commit now
        if (!autoCommit) {
          // Let updates be batcher locally
          if (batchSize > 0) {
            if (++numRowsInBatch % batchSize == 0) {
              // Send the batch of updates
              getShardConnectionByKey(key).commit();
            }
            // uhh
            return Status.OK;
          } else {
            // Commit each update
            getShardConnectionByKey(key).commit();
          }
        }
        if (result == 1) {
          return Status.OK;
        }
      }
      return Status.UNEXPECTED_STATE;
    } catch (SQLException e) {
      System.err.println("Error in processing insert to table: " + tableName + e);
      return Status.ERROR;
    }
  }

  @Override
  public Status delete(String tableName, String key) {
    try {
      StatementType type = new StatementType(StatementType.Type.DELETE, tableName, 1, "", getShardIndexByKey(key));
      PreparedStatement deleteStatement = cachedStatements.get(type);
      if (deleteStatement == null) {
        deleteStatement = createAndCacheDeleteStatement(type, key);
      }
      deleteStatement.setString(1, key);
      int result = deleteStatement.executeUpdate();
      if (result == 1) {
        return Status.OK;
      }
      return Status.UNEXPECTED_STATE;
    } catch (SQLException e) {
      System.err.println("Error in processing delete to table: " + tableName + e);
      return Status.ERROR;
    }
  }

  private OrderedFieldInfo getFieldInfo(Map<String, ByteIterator> values) {
    String fieldKeys = "";
    List<String> fieldValues = new ArrayList<>();
    int count = 0;
    for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
      fieldKeys += entry.getKey();
      if (count < values.size() - 1) {
        fieldKeys += ",";
      }
      fieldValues.add(count, entry.getValue().toString());
      count++;
    }
    return new OrderedFieldInfo(fieldKeys, fieldValues);
  }
}
