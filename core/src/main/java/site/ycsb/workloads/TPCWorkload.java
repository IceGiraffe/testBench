package site.ycsb.workloads;

import java.util.ArrayList;
import java.io.File;
import java.io.*;
import java.util.HashMap;
import site.ycsb.generator.*;
import java.util.*;
import site.ycsb.*;
import java.util.Scanner;
import java.io.FileInputStream;

/**
* A class extends the CoreWrrklaod with TPC-DS specification.
*/
public class TPCWorkload extends CoreWorkload {
  /**
   * The scale of data. Unit: GB.
   */
  public static final String DATA_SCALE_PROPERTY = "datascale";
  public static final String DATA_SCALE_PROPERTY_DEFAULT = "1";

  public static final String DATA_DIR_PROPERTY = "./dataGen/";
  public static final String DATA_DIR_PROPERTY_DEFAULT = "./dataGen/";
  //the same variable in the Client.java
  public static final String QUERY_INDEX = "queryindex";
  public static final String QUERY_INDEX_DEFAULT = "1";

  protected NumberGenerator filesequence;
  protected HashMap<String, String> fieldProperties = new HashMap<String, String>();
  protected HashMap<String, String[]> tableAndFieldNames;
  protected String scale;
  protected String dataDir;
  protected ArrayList<String> fileName;
  protected ArrayList<File> fileList;
  protected String queryindex;

  // 5.1 initiate workloads depends on implementations of workloads (prepare for loading data)(*)
  @Override
  public void init(Properties p) throws WorkloadException {
    // BUSINESS: Choose a kind of query (choose a kind of algorithms of BUSINESS)(*)
    if (p.getProperty(QUERY_INDEX).compareTo("0") != 0) {
      queryindex = p.getProperty(QUERY_INDEX);
    }
    // BUSINESS: Genarate data with TPC-DS tools (scale and path)
    scale = p.getProperty(DATA_SCALE_PROPERTY, DATA_SCALE_PROPERTY_DEFAULT);
    dataDir = p.getProperty(DATA_DIR_PROPERTY, DATA_DIR_PROPERTY_DEFAULT);
    createAndRunScriptForDataGen(scale);

    // BUSINESS: Store file list
    fileName = new ArrayList<String>();
    fileList = new ArrayList<File>();
    getAllFileName("./TPC_DS_DataGen/v2.8.0rc4/tools/dataGen/", fileName, fileList);
    filesequence = new CounterGenerator(0);
    
    // BUSINESS: Store tableNames and fieldNames
    tableAndFieldNames = new HashMap<String, String[]>();
    tableAndFieldNames.put("call_center", new String[]{"cc_call_center_sk", "cc_call_center_id", "cc_rec_start_date", "cc_rec_end_date", "cc_closed_date_sk", "cc_open_date_sk", "cc_name", "cc_class", "cc_employees", "cc_sq_ft", "cc_hours", "cc_manager", "cc_mkt_id", "cc_mkt_class", "cc_mkt_desc", "cc_market_manager", "cc_division", "cc_division_name", "cc_company", "cc_company_name", "cc_street_number", "cc_street_name", "cc_street_type", "cc_suite_number", "cc_city", "cc_county", "cc_state", "cc_zip", "cc_country", "cc_gmt_offset", "cc_tax_percentage"});
    tableAndFieldNames.put("household_demographics", new String[]{"hd_demo_sk", "hd_income_band_sk", "hd_buy_potential", "hd_dep_count", "hd_vehicle_count"});
    tableAndFieldNames.put("store_sales", new String[]{"ss_sold_date_sk",  "ss_sold_time_sk",  "ss_item_sk",  "ss_customer_sk",  "ss_cdemo_sk",  "ss_hdemo_sk",  "ss_addr_sk",  "ss_store_sk",  "ss_promo_sk",  "ss_ticket_number",  "ss_quantity",  "ss_wholesale_cost",  "ss_list_price",  "ss_sales_price",  "ss_ext_discount_amt",  "ss_ext_sales_price",  "ss_ext_wholesale_cost",  "ss_ext_list_price",  "ss_ext_tax",  "ss_coupon_amt",  "ss_net_paid",  "ss_net_paid_inc_tax",  "ss_net_profit"});
    tableAndFieldNames.put("catalog_page", new String[]{"cp_catalog_page_sk",  "cp_catalog_page_id",  "cp_start_date_sk",   "cp_end_date_sk",  "cp_department",  "cp_catalog_number",  "cp_catalog_page_number",  "cp_description",  "cp_type"});
    //tableAndFieldNames.put("catalog_sales", new String[]{"cs_sold_date_sk",  "cs_sold_time_sk",  "cs_ship_date_sk",  "cs_bill_customer_sk",  "cs_bill_cdemo_sk",  "cs_bill_hdemo_sk",  "cs_bill_addr_sk",  "cs_ship_customer_sk",  "cs_ship_cdemo_sk",  "cs_ship_hdemo_sk",  "cs_ship_addr_sk",  "cs_call_center_sk",  "cs_catalog_page_sk",  "cs_ship_mode_sk",  "cs_warehouse_sk",  "cs_item_sk",  "cs_promo_sk",  "cs_order_number",  "cs_quantity",  "cs_wholesale_cost",  "cs_list_price",  "cs_sales_price",  "cs_ext_discount_amt",  "cs_ext_sales_price",  "cs_ext_wholesale_cost",  "cs_ext_list_price",  "cs_ext_tax",  "cs_coupon_amt",  "cs_ext_ship_cost",  "cs_net_paid",  "cs_net_paid_inc_tax",  "cs_net_paid_inc_ship",  "cs_net_paid_inc_ship_tax",  "cs_net_profit" });
    //tableAndFieldNames.put("catalog_returns", new String[]{"cr_returned_date_sk",  "cr_returned_time_sk",  "cr_item_sk",  "cr_refunded_customer_sk",  "cr_refunded_cdemo_sk",  "cr_refunded_hdemo_sk",  "cr_refunded_addr_sk",  "cr_returning_customer_sk",  "cr_returning_cdemo_sk",  "cr_returning_hdemo_sk",  "cr_returning_addr_sk",  "cr_call_center_sk",  "cr_catalog_page_sk",  "cr_ship_mode_sk",  "cr_warehouse_sk",  "cr_reason_sk",  "cr_order_number",  "cr_return_quantity",  "cr_return_amount",  "cr_return_tax",  "cr_return_amt_inc_tax",  "cr_fee",  "cr_return_ship_cost",  "cr_refunded_cash",  "cr_reversed_charge",  "cr_store_credit",  "cr_net_loss" });
    //tableAndFieldNames.put("web_page", new String[]{"wp_web_page_sk",  "wp_web_page_id",  "wp_rec_start_date",  "wp_rec_end_date",  "wp_creation_date_sk",  "wp_access_date_sk",  "wp_autogen_flag",  "wp_customer_sk",  "wp_url",  "wp_type",  "wp_char_count",  "wp_link_count",  "wp_image_count", "wp_max_ad_count" });
    //tableAndFieldNames.put("web_site", new String[]{"web_site_sk",  "web_site_id",  "web_rec_start_date",  "web_rec_end_date",  "web_name",  "web_open_date_sk",  "web_close_date_sk",  "web_class",  "web_manager",  "web_mkt_id",  "web_mkt_class",  "web_mkt_desc",  "web_market_manager",  "web_company_id",  "web_company_name",  "web_street_number",  "web_street_name",  "web_street_type",  "web_suite_number",  "web_city",  "web_county",  "web_state",  "web_zip",  "web_country",  "web_gmt_offset",  "web_tax_percentage"});
    //tableAndFieldNames.put("warehouse", new String[]{"w_warehouse_sk",  "w_warehouse_id",  "w_warehouse_name",  "w_warehouse_sq_ft",  "w_street_number",  "w_street_name",  "w_street_type",  "w_suite_number",  "w_city",  "w_county",  "w_state",  "w_zip",  "w_country",  "w_gmt_offset" });
    //tableAndFieldNames.put("promotion", new String[]{"p_promo_sk",  "p_promo_id",  "p_start_date_sk",  "p_end_date_sk",  "p_item_sk",  "p_cost",  "p_response_target",  "p_promo_name",  "p_channel_dmail",  "p_channel_email",  "p_channel_catalog",  "p_channel_tv",  "p_channel_radio",  "p_channel_press",  "p_channel_event",  "p_channel_demo",  "p_channel_details",  "p_purpose",  "p_discount_active"});
    
    tableAndFieldNames.put("income_band", new String[]{"ib_income_band_sk",  "ib_lower_bound",  "ib_upper_bound"});
    tableAndFieldNames.put("time_dim", new String[]{"t_time_sk",  "t_time_id",  "t_time",  "t_hour",  "t_minute",  "t_second",  "t_am_pm",  "t_shift",  "t_sub_shift", "t_meal_time" });
    tableAndFieldNames.put("catalog_returns", new String[]{"cr_returned_date_sk",  "cr_returned_time_sk",  "cr_item_sk",  "cr_refunded_customer_sk",  "cr_refunded_cdemo_sk",  "cr_refunded_hdemo_sk",  "cr_refunded_addr_sk",  "cr_returning_customer_sk",  "cr_returning_cdemo_sk",  "cr_returning_hdemo_sk",  "cr_returning_addr_sk",  "cr_call_center_sk",  "cr_catalog_page_sk",  "cr_ship_mode_sk",  "cr_warehouse_sk",  "cr_reason_sk",  "cr_order_number",  "cr_return_quantity",  "cr_return_amount",  "cr_return_tax",  "cr_return_amt_inc_tax",  "cr_fee",  "cr_return_ship_cost",  "cr_refunded_cash",  "cr_reversed_charge",  "cr_store_credit",  "cr_net_loss" });
    tableAndFieldNames.put("inventory", new String[]{"inv_date_sk",  "inv_item_sk",  "inv_warehouse_sk",  "inv_quantity_on_hand" });
    tableAndFieldNames.put("warehouse", new String[]{"w_warehouse_sk",  "w_warehouse_id",  "w_warehouse_name",  "w_warehouse_sq_ft",  "w_street_number",  "w_street_name",  "w_street_type",  "w_suite_number",  "w_city",  "w_county",  "w_state",  "w_zip",  "w_country",  "w_gmt_offset" });
    tableAndFieldNames.put("catalog_sales", new String[]{"cs_sold_date_sk",  "cs_sold_time_sk",  "cs_ship_date_sk",  "cs_bill_customer_sk",  "cs_bill_cdemo_sk",  "cs_bill_hdemo_sk",  "cs_bill_addr_sk",  "cs_ship_customer_sk",  "cs_ship_cdemo_sk",  "cs_ship_hdemo_sk",  "cs_ship_addr_sk",  "cs_call_center_sk",  "cs_catalog_page_sk",  "cs_ship_mode_sk",  "cs_warehouse_sk",  "cs_item_sk",  "cs_promo_sk",  "cs_order_number",  "cs_quantity",  "cs_wholesale_cost",  "cs_list_price",  "cs_sales_price",  "cs_ext_discount_amt",  "cs_ext_sales_price",  "cs_ext_wholesale_cost",  "cs_ext_list_price",  "cs_ext_tax",  "cs_coupon_amt",  "cs_ext_ship_cost",  "cs_net_paid",  "cs_net_paid_inc_tax",  "cs_net_paid_inc_ship",  "cs_net_paid_inc_ship_tax",  "cs_net_profit" });
    tableAndFieldNames.put("item", new String[]{"i_item_sk",  "i_item_id",  "i_rec_start_date",  "i_rec_end_date",  "i_item_desc",  "i_current_price",  "i_wholesale_cost",  "i_brand_id",  "i_brand",  "i_class_id",  "i_class",  "i_category_id",  "i_category",  "i_manufact_id",  "i_manufact",  "i_size",  "i_formulation",  "i_color",  "i_units",  "i_container",  "i_manager_id",  "i_product_name"});
    tableAndFieldNames.put("web_page", new String[]{"wp_web_page_sk",  "wp_web_page_id",  "wp_rec_start_date",  "wp_rec_end_date",  "wp_creation_date_sk",  "wp_access_date_sk",  "wp_autogen_flag",  "wp_customer_sk",  "wp_url",  "wp_type",  "wp_char_count",  "wp_link_count",  "wp_image_count", "wp_max_ad_count" });
    tableAndFieldNames.put("customer_address", new String[]{"ca_address_sk",  "ca_address_id",  "ca_street_number",  "ca_street_name",  "ca_street_type",  "ca_suite_number",  "ca_city",  "ca_county",  "ca_state",  "ca_zip",  "ca_country",  "ca_gmt_offset",  "ca_location_type" });
    tableAndFieldNames.put("promotion", new String[]{"p_promo_sk",  "p_promo_id",  "p_start_date_sk",  "p_end_date_sk",  "p_item_sk",  "p_cost",  "p_response_target",  "p_promo_name",  "p_channel_dmail",  "p_channel_email",  "p_channel_catalog",  "p_channel_tv",  "p_channel_radio",  "p_channel_press",  "p_channel_event",  "p_channel_demo",  "p_channel_details",  "p_purpose",  "p_discount_active"});
    tableAndFieldNames.put("web_returns", new String[]{"wr_returned_date_sk",  "wr_returned_time_sk",  "wr_item_sk",  "wr_refunded_customer_sk",  "wr_refunded_cdemo_sk",  "wr_refunded_hdemo_sk",  "wr_refunded_addr_sk",  "wr_returning_customer_sk",  "wr_returning_cdemo_sk",  "wr_returning_hdemo_sk",  "wr_returning_addr_sk",  "wr_web_page_sk",  "wr_reason_sk",  "wr_order_number",  "wr_return_quantity",  "wr_return_amt",  "wr_return_tax",  "wr_return_amt_inc_tax",  "wr_fee",  "wr_return_ship_cost",  "wr_refunded_cash",  "wr_reversed_charge",  "wr_account_credit",  "wr_net_loss"});
    tableAndFieldNames.put("customer", new String[]{"c_customer_sk",  "c_customer_id", "c_current_cdemo_sk", "c_current_hdemo_sk",  "c_current_addr_sk",  "c_first_shipto_date_sk",  "c_first_sales_date_sk",  "c_salutation",  "c_first_name",  "c_last_name",  "c_preferred_cust_flag",  "c_birth_day",  "c_birth_month",  "c_birth_year" , "c_birth_country", "c_login", "c_email_address", "c_last_review_date"});
    tableAndFieldNames.put("reason", new String[]{"r_reason_sk",  "r_reason_id",  "r_reason_desc" });
    tableAndFieldNames.put("web_sales", new String[]{"ws_sold_date_sk",  "ws_sold_time_sk",  "ws_ship_date_sk",  "ws_item_sk",  "ws_bill_customer_sk",  "ws_bill_cdemo_sk",  "ws_bill_hdemo_sk",  "ws_bill_addr_sk",  "ws_ship_customer_sk",  "ws_ship_cdemo_sk",  "ws_ship_hdemo_sk",  "ws_ship_addr_sk",  "ws_web_page_sk",  "ws_web_site_sk",  "ws_ship_mode_sk",  "ws_warehouse_sk",  "ws_promo_sk",  "ws_order_number",  "ws_quantity",  "ws_wholesale_cost",  "ws_list_price",  "ws_sales_price",  "ws_ext_discount_amt",  "ws_ext_sales_price",  "ws_ext_wholesale_cost",  "ws_ext_list_price",  "ws_ext_tax",  "ws_coupon_amt",  "ws_ext_ship_cost",  "ws_net_paid",  "ws_net_paid_inc_tax",  "ws_net_paid_inc_ship",  "ws_net_paid_inc_ship_tax",  "ws_net_profit" });
    tableAndFieldNames.put("customer_demographics", new String[]{"cd_demo_sk",  "cd_gender",  "cd_marital_status",  "cd_education_status",  "cd_purchase_estimate",  "cd_credit_rating",  "cd_dep_count",  "cd_dep_employed_count",  "cd_dep_college_count"});
    tableAndFieldNames.put("ship_mode", new String[]{"sm_ship_mode_sk",  "sm_ship_mode_id",  "sm_type",  "sm_code",  "sm_carrier",  "sm_contract" });
    tableAndFieldNames.put("web_site", new String[]{"web_site_sk",  "web_site_id",  "web_rec_start_date",  "web_rec_end_date",  "web_name",  "web_open_date_sk",  "web_close_date_sk",  "web_class",  "web_manager",  "web_mkt_id",  "web_mkt_class",  "web_mkt_desc",  "web_market_manager",  "web_company_id",  "web_company_name",  "web_street_number",  "web_street_name",  "web_street_type",  "web_suite_number",  "web_city",  "web_county",  "web_state",  "web_zip",  "web_country",  "web_gmt_offset",  "web_tax_percentage"});
    tableAndFieldNames.put("date_dim", new String[]{"d_date_sk",  "d_date_id",  "d_date",  "d_month_seq",  "d_week_seq",  "d_quarter_seq",  "d_year",  "d_dow",  "d_moy",  "d_dom",  "d_qoy",  "d_fy_year",  "d_fy_quarter_seq",  "d_fy_week_seq",  "d_day_name",  "d_quarter_name",  "d_holiday",  "d_weekend",  "d_following_holiday",  "d_first_dom",  "d_last_dom",  "d_same_day_ly",  "d_same_day_lq",  "d_current_day",  "d_current_week",  "d_current_month", "d_current_quarter",  "d_current_year"});
    tableAndFieldNames.put("store", new String[]{"s_store_sk",  "s_store_id",  "s_rec_start_date",  "s_rec_end_date",  "s_closed_date_sk",  "s_store_name",  "s_number_employees",  "s_floor_space",  "s_hours",  "s_manager",  "s_market_id",  "s_geography_class",  "s_market_desc",  "s_market_manager",  "s_division_id",  "s_division_name",  "s_company_id",  "s_company_name",  "s_street_number",  "s_street_name",  "s_street_type", "s_suite_number", "s_city",  "s_county",  "s_state",  "s_zip",  "s_country",  "s_gmt_offset",  "s_tax_precentage"});
    tableAndFieldNames.put("store_returns", new String[]{"sr_returned_date_sk",  "sr_return_time_sk",  "sr_item_sk",  "sr_customer_sk",  "sr_cdemo_sk", "sr_hdemo_sk",  "sr_addr_sk",  "sr_store_sk",  "sr_reason_sk",  "sr_ticket_number",  "sr_return_quantity",  "sr_return_amt",  "sr_return_tax", "sr_return_amt_inc_tax", "sr_fee", "sr_return_ship_cost", "sr_refunded_cash", "sr_reversed_charge", "sr_store_credit",  "sr_net_loss"});
  
  }
   // 7.2.2. Load data
  @Override
  public boolean doInsert(DB db, Object threadstate) {
    // BUSINESS: generate and load data into mysql
    // Chosse a files 
    int filenum = filesequence.nextValue().intValue();

    String dbfile = fileList.get(filenum).getName();

    String dp = "./TPC_DS_DataGen/v2.8.0rc4/tools/dataGen/" + dbfile;
    // Read lines from the file
    FileInputStream fileInputStream = null; 
    Scanner scanner = null;
    //Get information about the file: table name & field keys
    String tableName = fileName.get(filenum).split("\\.")[0];


    String[] fieldkeys = tableAndFieldNames.get(tableName);
    // Insert data line by line 
    HashMap<String, ByteIterator> values;
    String[] fieldValues;
    ByteIterator data;
    String record;
    Status status = null;
    int numOfRetries;
    int count;

    try {
      fileInputStream = new FileInputStream(dp);
      scanner = new Scanner(fileInputStream);
      //hasNextLine? hasNext?
      while (scanner.hasNextLine()) {
        numOfRetries = 0;
        count = 1; //dbkey : fieldValues[0]
        // Read one line
        record = String.valueOf(scanner.nextLine());
        // Split one line into values
        fieldValues = record.split("\\|");
        // Store key-value pairs one by one
        values = new HashMap<>();
        for (; count < fieldValues.length; count++) {
          if (!fieldValues[count].isEmpty()) {
            data = new StringByteIterator(fieldValues[count]);
            values.put(fieldkeys[count], data);
          }
        }
        // Insert key-value pairs one by one
        do{
          // fieldValues[0], as dbkey, is inserted into table with func(db.insert())
          status = db.insert(tableName, fieldValues[0], values);
          System.out.print(".");
          if (null != status && status.isOk()) {
            break;
          }
          // Retry if configured. Without retrying, the load process will fail
          // even if one single insertion fails. User can optionally configure
          // an insertion retry limit (default is 0) to enable retry.
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
            System.err.println("Error inserting, not retrying any more. number of attempts: " + numOfRetries + "Insertion Retry Limit: " + insertionRetryLimit);
            break;
          }
        } while (true);
      }
      fileInputStream.close();
      scanner.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Fail to read TPC-DS data fiels:"+dbfile);
    }
    System.out.println("Data have been deleted. File Number"+filenum);
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
  
  public static void createAndRunScriptForDataGen(String scale){
    String scriptName = "dataGen.sh";

    try {
      FileWriter writer = new FileWriter(scriptName);
      writer.write("#!/bash/sh\n");
      writer.write("cd ./TPC_DS_DataGen/v2.8.0rc4/tools/\n");
      writer.write("mkdir dataGen\n");
      writer.write("./dsdgen -DIR ./dataGen -SCALE "+scale+" \n");
      writer.write("wait\n");
      writer.write("rm ./dataGen/dbgen_version.dat");
      writer.close();
      int result = runShellCommand("chmod 664 dataGen.sh");
      result = runShellCommand("bash dataGen.sh");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  
  // 7.2.1. Run workloads (choose a kind of algorithms of BUSINESS to run)(*)
  @Override
  public boolean doTransaction(DB db, Object threadstate) {
    // BUSINESS: run sql query according to predefined queryindex
    //YYB: Execute sql script for business workload
    String sqlFilePath = null;
    sqlFilePath = "./business_workload/query"+queryindex+".sql";
    System.out.println("\n"+sqlFilePath+"\n");
    // 7.2.1.1. modify operations of specific databases
    db.execScriptFile(db, sqlFilePath);
    return true;
  }

}
