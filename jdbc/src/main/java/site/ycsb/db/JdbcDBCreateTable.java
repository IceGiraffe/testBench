/**
 * Copyright (c) 2010 - 2016 Yahoo! Inc. All rights reserved.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Utility class to create the table to be used by the benchmark.
 * 
 * @author sudipto
 */
public final class JdbcDBCreateTable {

  public static final String TABLE_TYPE_PROPERTY = "TPC_DS";
  public static final String TABLE_TYPE_PROPERTY_DEFAULT = "GENERAL";

  private static void usageMessage() {
    System.out.println("Create Table Client. Options:");
    System.out.println("  -p   key=value properties defined.");
    System.out.println("  -P   location of the properties file to load.");
    System.out.println("  -n   name of the table.");
    System.out.println("  -f   number of fields (default 10).");
    System.out.println("  -F   field of workload (TPC_DS).");
  }

  private static void createTable(Properties props, String tablename) throws SQLException {
    String driver = props.getProperty(JdbcDBClient.DRIVER_CLASS);
    String username = props.getProperty(JdbcDBClient.CONNECTION_USER);
    String password = props.getProperty(JdbcDBClient.CONNECTION_PASSWD, "");
    String url = props.getProperty(JdbcDBClient.CONNECTION_URL);
    int fieldcount = Integer.parseInt(props.getProperty(JdbcDBClient.FIELD_COUNT_PROPERTY,
        JdbcDBClient.FIELD_COUNT_PROPERTY_DEFAULT));
    if (driver == null || username == null || url == null) {
      throw new SQLException("Missing connection information.");
    }

    Connection conn = null;

    try {
      System.out.println(driver);
      Class.forName(driver);
      conn = DriverManager.getConnection(url, username, password);
      Statement stmt = conn.createStatement();
     
      StringBuilder sql = new StringBuilder("DROP TABLE IF EXISTS ");
      sql.append(tablename);
      sql.append(";");

      stmt.execute(sql.toString());

      sql = new StringBuilder("CREATE TABLE ");
      sql.append(tablename);
      sql.append(" (YCSB_KEY VARCHAR PRIMARY KEY");

      for (int idx = 0; idx < fieldcount; idx++) {
        sql.append(", FIELD");
        sql.append(idx);
        sql.append(" TEXT");
      }
      sql.append(");");

      stmt.execute(sql.toString());

      System.out.println("Table " + tablename + " created..");
    } catch (ClassNotFoundException e) {
      throw new SQLException("JDBC Driver class not found.");
    } finally {
      if (conn != null) {
        System.out.println("Closing database connection.");
        conn.close();
      }
    }
  }

  //(YYB) Create TPC table
  private static void createTPCTable(Properties props) throws SQLException {
    String driver = props.getProperty(JdbcDBClient.DRIVER_CLASS);
    String username = props.getProperty(JdbcDBClient.CONNECTION_USER);
    String password = props.getProperty(JdbcDBClient.CONNECTION_PASSWD, "");
    String url = props.getProperty(JdbcDBClient.CONNECTION_URL);
    int fieldcount = Integer.parseInt(props.getProperty(JdbcDBClient.FIELD_COUNT_PROPERTY,
        JdbcDBClient.FIELD_COUNT_PROPERTY_DEFAULT));
    HashMap<String, String> tableAndField = new HashMap<String, String>();
    tableAndField.put("call_center", "(cc_call_center_sk varchar(255), cc_call_center_id text, cc_rec_start_date text, cc_rec_end_date text,  cc_closed_date_sk text, cc_open_date_sk text, cc_name text, cc_class text, cc_employees text, cc_sq_ft text,  cc_hours text , cc_manager text, cc_mkt_id text , cc_mkt_class text, cc_mkt_desc  text, cc_market_manager  text, cc_division text, cc_division_name text, cc_company text, cc_company_name text, cc_street_number  text, cc_street_name text, cc_street_type text, cc_suite_number text, cc_city text, cc_county text, cc_state text, cc_zip text, cc_country text, cc_gmt_offset text, cc_tax_percentage text, PRIMARY KEY(cc_call_center_sk))");

    tableAndField.put("household_demographics", "(hd_demo_sk varchar(255), hd_income_band_sk text, hd_buy_potential text,  hd_dep_count text, hd_vehicle_count text, PRIMARY KEY(hd_demo_sk))");

    tableAndField.put("store_sales", "(ss_sold_date_sk text, ss_sold_time_sk text, ss_item_sk varchar(255), ss_customer_sk text, ss_cdemo_sk text, ss_hdemo_sk text, ss_addr_sk text, ss_store_sk text, ss_promo_sk text, ss_ticket_number varchar(255), ss_quantity text, ss_wholesale_cost text, ss_list_price text, ss_sales_price text, ss_ext_discount_amt text, ss_ext_sales_price text, ss_ext_wholesale_cost text, ss_ext_list_price text, ss_ext_tax text, ss_coupon_amt text, ss_net_paid text, ss_net_paid_inc_tax text, ss_net_profit text, PRIMARY KEY(ss_item_sk, ss_ticket_number))");


    tableAndField.put("catalog_page", "(cp_catalog_page_sk varchar(255), cp_catalog_page_id text, cp_start_date_sk  text, cp_end_date_sk text, cp_department text, cp_catalog_number text, cp_catalog_page_number text, cp_description  text, cp_type text, PRIMARY KEY(cp_catalog_page_sk))");

    tableAndField.put("income_band", "(ib_income_band_sk varchar(255), ib_lower_bound text, ib_upper_bound text, PRIMARY KEY(ib_income_band_sk))");


    tableAndField.put("time_dim", "(t_time_sk varchar(255), t_time_id text, t_time text, t_hour text, t_minute text, t_second text, t_am_pm text, t_shift text, t_sub_shift text, t_meal_time text, PRIMARY KEY(t_time_sk))");

    tableAndField.put("catalog_returns", "(cr_returned_date_sk text, cr_returned_time_sk text, cr_item_sk varchar(255), cr_refunded_customer_sk text, cr_refunded_cdemo_sk text, cr_refunded_hdemo_sk text, cr_refunded_addr_sk text, cr_returning_customer_sk text, cr_returning_cdemo_sk text, cr_returning_hdemo_sk text, cr_returning_addr_sk text, cr_call_center_sk text, cr_catalog_page_sk text, cr_ship_mode_sk text, cr_warehouse_sk text, cr_reason_sk text, cr_order_number varchar(255), cr_return_quantity text, cr_return_amount text, cr_return_tax text, cr_return_amt_inc_tax text, cr_fee text, cr_return_ship_cost text, cr_refunded_cash text, cr_reversed_charge text, cr_store_credit text, cr_net_loss text, PRIMARY KEY(cr_item_sk, cr_order_number))");

    tableAndField.put("inventory", "(inv_date_sk varchar(255), inv_item_sk varchar(255), inv_warehouse_sk varchar(255), inv_quantity_on_hand text, PRIMARY KEY(inv_date_sk, inv_item_sk, inv_warehouse_sk))");


    tableAndField.put("warehouse", "(w_warehouse_sk varchar(255), w_warehouse_id text, w_warehouse_name text, w_warehouse_sq_ft text, w_street_number text, w_street_name text, w_street_type text, w_suite_number text, w_city text, w_county text, w_state text, w_zip text, w_country text, w_gmt_offset text, PRIMARY KEY(w_warehouse_sk))");

    tableAndField.put("catalog_sales", "(cs_sold_date_sk text, cs_sold_time_sk text, cs_ship_date_sk text, cs_bill_customer_sk text, cs_bill_cdemo_sk text, cs_bill_hdemo_sk text, cs_bill_addr_sk text, cs_ship_customer_sk text, cs_ship_cdemo_sk text, cs_ship_hdemo_sk text, cs_ship_addr_sk text, cs_call_center_sk text, cs_catalog_page_sk text, cs_ship_mode_sk text, cs_warehouse_sk text, cs_item_sk varchar(255), cs_promo_sk text, cs_order_number varchar(255), cs_quantity text, cs_wholesale_cost text, cs_list_price text, cs_sales_price text, cs_ext_discount_amt text, cs_ext_sales_price text, cs_ext_wholesale_cost text, cs_ext_list_price text, cs_ext_tax text, cs_coupon_amt text, cs_ext_ship_cost text, cs_net_paid text, cs_net_paid_inc_tax text, cs_net_paid_inc_ship text, cs_net_paid_inc_ship_tax text, cs_net_profit text, PRIMARY KEY(cs_item_sk, cs_order_number))");

    tableAndField.put("item", "(i_item_sk varchar(255), i_item_id text, i_rec_start_date text, i_rec_end_date text, i_item_desc text, i_current_price text, i_wholesale_cost text, i_brand_id text, i_brand text, i_class_id text, i_class text, i_category_id text, i_category text, i_manufact_id text, i_manufact text, i_size text, i_formulation text, i_color text, i_units text, i_container text, i_manager_id text, i_product_name text, PRIMARY KEY(i_item_sk))");

    tableAndField.put("web_page", "(wp_web_page_sk varchar(255), wp_web_page_id text, wp_rec_start_date text, wp_rec_end_date text, wp_creation_date_sk text, wp_access_date_sk text, wp_autogen_flag text, wp_customer_sk text, wp_url text, wp_type text, wp_char_count text, wp_link_count text, wp_image_count text, wp_max_ad_count text, PRIMARY KEY(wp_web_page_sk))");

    tableAndField.put("customer_address", "(ca_address_sk varchar(255), ca_address_id text, ca_street_number text, ca_street_name text, ca_street_type text, ca_suite_number text, ca_city text, ca_county text, ca_state text, ca_zip text, ca_country text, ca_gmt_offset text, ca_location_type text, PRIMARY KEY(ca_address_sk))");

    tableAndField.put("promotion", "(p_promo_sk varchar(255), p_promo_id text, p_start_date_sk text, p_end_date_sk text, p_item_sk text, p_cost text, p_response_target text, p_promo_name text, p_channel_dmail text, p_channel_email text, p_channel_catalog text, p_channel_tv text, p_channel_radio text, p_channel_press text, p_channel_event text, p_channel_demo text, p_channel_details text, p_purpose text, p_discount_active text, PRIMARY KEY(p_promo_sk))");

    tableAndField.put("web_returns", "(wr_returned_date_sk text, wr_returned_time_sk text, wr_item_sk varchar(255), wr_refunded_customer_sk text, wr_refunded_cdemo_sk text, wr_refunded_hdemo_sk text, wr_refunded_addr_sk text, wr_returning_customer_sk text, wr_returning_cdemo_sk text, wr_returning_hdemo_sk text, wr_returning_addr_sk text, wr_web_page_sk text, wr_reason_sk text, wr_order_number varchar(255), wr_return_quantity text, wr_return_amt text, wr_return_tax text, wr_return_amt_inc_tax text, wr_fee text, wr_return_ship_cost text, wr_refunded_cash text, wr_reversed_charge text, wr_account_credit text, wr_net_loss text, PRIMARY KEY(wr_item_sk, wr_order_number))");

    tableAndField.put("customer", "(c_customer_sk varchar(255), c_customer_id text, c_current_cdemo_sk text, c_current_hdemo_sk text, c_current_addr_sk text, c_first_shipto_date_sk text, c_first_sales_date_sk text, c_salutation text, c_first_name text, c_last_name text, c_preferred_cust_flag text, c_birth_day text, c_birth_month text, c_birth_year text, c_birth_country text, c_login text, c_email_address text, c_last_review_date text, PRIMARY KEY(c_customer_sk))");


    tableAndField.put("reason", "(r_reason_sk varchar(255), r_reason_id text, r_reason_desc char(100), PRIMARY KEY(r_reason_sk))");

    tableAndField.put("web_sales", "(ws_sold_date_sk text, ws_sold_time_sk text, ws_ship_date_sk text, ws_item_sk varchar(255), ws_bill_customer_sk text, ws_bill_cdemo_sk text, ws_bill_hdemo_sk text, ws_bill_addr_sk text, ws_ship_customer_sk text, ws_ship_cdemo_sk text, ws_ship_hdemo_sk text, ws_ship_addr_sk text, ws_web_page_sk text, ws_web_site_sk text, ws_ship_mode_sk text, ws_warehouse_sk text, ws_promo_sk text, ws_order_number varchar(255), ws_quantity text, ws_wholesale_cost text, ws_list_price text, ws_sales_price text, ws_ext_discount_amt text, ws_ext_sales_price text, ws_ext_wholesale_cost text, ws_ext_list_price text, ws_ext_tax text, ws_coupon_amt text, ws_ext_ship_cost text, ws_net_paid text, ws_net_paid_inc_tax text, ws_net_paid_inc_ship text, ws_net_paid_inc_ship_tax text, ws_net_profit text, PRIMARY KEY(ws_item_sk, ws_order_number))");

    tableAndField.put("customer_demographics", "(cd_demo_sk varchar(255), cd_gender text, cd_marital_status text, cd_education_status text, cd_purchase_estimate text, cd_credit_rating text, cd_dep_count text, cd_dep_employed_count text, cd_dep_college_count text, PRIMARY KEY(cd_demo_sk))");

    tableAndField.put("ship_mode", "(sm_ship_mode_sk varchar(255), sm_ship_mode_id text, sm_type char(30), sm_code text, sm_carrier text, sm_contract text, PRIMARY KEY(sm_ship_mode_sk))");

    tableAndField.put("web_site", "(web_site_sk varchar(255), web_site_id text, web_rec_start_date text, web_rec_end_date text, web_name text, web_open_date_sk text, web_close_date_sk text, web_class text, web_manager text, web_mkt_id text, web_mkt_class text, web_mkt_desc text, web_market_manager text, web_company_id text, web_company_name text, web_street_number text, web_street_name text, web_street_type text, web_suite_number text, web_city text, web_county text, web_state text, web_zip text, web_country text, web_gmt_offset text, web_tax_percentage text, PRIMARY KEY(web_site_sk))");

    tableAndField.put("date_dim", "(d_date_sk varchar(255), d_date_id text, d_date text, d_month_seq text, d_week_seq text, d_quarter_seq text, d_year text, d_dow text, d_moy text, d_dom text, d_qoy text, d_fy_year text, d_fy_quarter_seq text, d_fy_week_seq text, d_day_name char(9), d_quarter_name char(6), d_holiday text, d_weekend text, d_following_holiday text, d_first_dom text, d_last_dom text, d_same_day_ly text, d_same_day_lq text, d_current_day text, d_current_week text, d_current_month text, d_current_quarter text, d_current_year text, PRIMARY KEY(d_date_sk))");

    tableAndField.put("store", "(s_store_sk varchar(255), s_store_id text, s_rec_start_date text, s_rec_end_date text, s_closed_date_sk text, s_store_name text, s_number_employees text, s_floor_space text, s_hours text, s_manager text, s_market_id text, s_geography_class text, s_market_desc text, s_market_manager text, s_division_id text, s_division_name text, s_company_id text, s_company_name text, s_street_number text, s_street_name text, s_street_type text, s_suite_number text, s_city text, s_county text, s_state text, s_zip text, s_country text, s_gmt_offset text, s_tax_precentage text, PRIMARY KEY(s_store_sk))");

    tableAndField.put("store_returns", "(sr_returned_date_sk text, sr_return_time_sk text, sr_item_sk varchar(255), sr_customer_sk text, sr_cdemo_sk text, sr_hdemo_sk text, sr_addr_sk text, sr_store_sk text, sr_reason_sk text, sr_ticket_number varchar(255), sr_return_quantity text, sr_return_amt text, sr_return_tax text, sr_return_amt_inc_tax text, sr_fee text, sr_return_ship_cost text, sr_refunded_cash text, sr_reversed_charge text, sr_store_credit text, sr_net_loss text, PRIMARY KEY(sr_item_sk, sr_ticket_number))");

    if (driver == null || username == null || url == null) {
      throw new SQLException("Missing connection information.");
    }

    Connection conn = null;

    try {
      Class.forName(driver);
      conn = DriverManager.getConnection(url, username, password);
      Statement stmt = conn.createStatement();
      String tablename;
      String fields;
      StringBuilder sql;
      for (Entry<String, String> entry : tableAndField.entrySet()) {
        tablename = entry.getKey();
        fields = entry.getValue();

        sql = new StringBuilder("DROP TABLE IF EXISTS ");
        sql.append(tablename);
        sql.append(";");
        stmt.execute(sql.toString());

        sql = new StringBuilder("CREATE TABLE ");
        sql.append(tablename);
        sql.append(" ");
        sql.append(fields);
        sql.append(";");
        stmt.execute(sql.toString());
        System.out.println("Table " + tablename + " created..");
      }
    } catch (ClassNotFoundException e) {
      throw new SQLException("JDBC Driver class not found.");
    } finally {
      if (conn != null) {
        System.out.println("Closing database connection.");
        conn.close();
      }
    }
  }


  /**
   * @param args
   */
  public static void main(String[] args) {

    if (args.length == 0) {
      usageMessage();
      System.exit(0);
    }

    String tablename = null;
    int fieldcount = -1;
    Properties props = new Properties();
    Properties fileprops = new Properties();

    // parse arguments
    int argindex = 0;
    while (args[argindex].startsWith("-")) {
      if (args[argindex].compareTo("-P") == 0) {
        argindex++;
        if (argindex >= args.length) {
          usageMessage();
          System.exit(0);
        }
        String propfile = args[argindex];
        argindex++;

        Properties myfileprops = new Properties();
        try {
          myfileprops.load(new FileInputStream(propfile));
        } catch (IOException e) {
          System.out.println(e.getMessage());
          System.exit(0);
        }

        // Issue #5 - remove call to stringPropertyNames to make compilable
        // under Java 1.5
        for (Enumeration<?> e = myfileprops.propertyNames(); e.hasMoreElements();) {
          String prop = (String) e.nextElement();

          fileprops.setProperty(prop, myfileprops.getProperty(prop));
        }
      // (YYB)
      } else if (args[argindex].compareTo("-F") == 0){ 
        argindex++;
        if (args[argindex].compareTo("TPC_DS") == 0) {
          props.setProperty(TABLE_TYPE_PROPERTY, args[argindex]);
          tablename = "TPCTable";
        } else {
          System.out.println("Invalid value for field of workload. (GENERAL and TPC_DS only)");
          System.exit(0);
        }
        argindex++;
      } else if (args[argindex].compareTo("-p") == 0) {
        argindex++;
        if (argindex >= args.length) {
          usageMessage();
          System.exit(0);
        }
        int eq = args[argindex].indexOf('=');
        if (eq < 0) {
          usageMessage();
          System.exit(0);
        }

        String name = args[argindex].substring(0, eq);
        String value = args[argindex].substring(eq + 1);
        props.put(name, value);
        argindex++;
      } else if (args[argindex].compareTo("-n") == 0) {
        argindex++;
        if (argindex >= args.length) {
          usageMessage();
          System.exit(0);
        }
        tablename = args[argindex++];
      } else if (args[argindex].compareTo("-f") == 0) {
        argindex++;
        if (argindex >= args.length) {
          usageMessage();
          System.exit(0);
        }
        try {
          fieldcount = Integer.parseInt(args[argindex++]);
        } catch (NumberFormatException e) {
          System.err.println("Invalid number for field count");
          usageMessage();
          System.exit(1);
        }
      } else {
        System.out.println("Unknown option " + args[argindex]);
        usageMessage();
        System.exit(0);
      }

      if (argindex >= args.length) {
        break;
      }
    }

    if (argindex != args.length) {
      usageMessage();
      System.exit(0);
    }

    // overwrite file properties with properties from the command line

    // Issue #5 - remove call to stringPropertyNames to make compilable under
    // Java 1.5
    for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
      String prop = (String) e.nextElement();

      fileprops.setProperty(prop, props.getProperty(prop));
    }

    props = fileprops;

    if (tablename == null) {
      System.err.println("table name missing.");
      usageMessage();
      System.exit(1);
    }

    if (fieldcount > 0) {
      props.setProperty(JdbcDBClient.FIELD_COUNT_PROPERTY, String.valueOf(fieldcount));
    }

    try {
      if (props.getProperty(TABLE_TYPE_PROPERTY, TABLE_TYPE_PROPERTY_DEFAULT).equals("TPC_DS")) {
        //创建24个表！！！！！
        createTPCTable(props);
      } else {
        createTable(props, tablename);
      }
      
    } catch (SQLException e) {
      System.err.println("Error in creating table. " + e);
      System.exit(1);
    }
  }

  /**
   * Hidden constructor.
   */
  private JdbcDBCreateTable() {
    super();
  }
}
