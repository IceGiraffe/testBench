/**
 * Copyright (c) 2016 YCSB contributors. All rights reserved.
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
package site.ycsb.db.flavors;

import site.ycsb.db.JdbcDBClient;
import site.ycsb.db.StatementType;
import java.util.HashMap;

/**
 * A default flavor for relational databases.
 */
public class DefaultDBFlavor extends DBFlavor {

  protected static HashMap<String, String> tableAndPK = new HashMap<String, String>();

  public DefaultDBFlavor() {
    super(DBName.DEFAULT);
    tableAndPK.put("usertable", "YCSB_KEY");
    tableAndPK.put("call_center", "cc_call_center_sk");
    tableAndPK.put("household_demographics", "hd_demo_sk");
    tableAndPK.put("store_sales", "ss_sold_date_sk");
    tableAndPK.put("catalog_page", "cp_catalog_page_sk");
    tableAndPK.put("income_band", "ib_income_band_sk");
    tableAndPK.put("time_dim", "t_time_sk");
    tableAndPK.put("catalog_returns", "cr_returned_date_sk");
    tableAndPK.put("inventory", "inv_date_sk");
    tableAndPK.put("warehouse", "w_warehouse_sk");
    tableAndPK.put("catalog_sales", "cs_sold_date_sk");
    tableAndPK.put("item", "i_item_sk");
    tableAndPK.put("web_page", "wp_web_page_sk");
    tableAndPK.put("customer_address", "ca_address_sk");
    tableAndPK.put("promotion", "p_promo_sk");
    tableAndPK.put("web_returns", "wr_returned_date_sk");
    tableAndPK.put("customer", "c_customer_sk");
    tableAndPK.put("reason", "r_reason_sk");
    tableAndPK.put("web_sales", "ws_sold_date_sk");
    tableAndPK.put("customer_demographics", "cd_demo_sk");
    tableAndPK.put("ship_mode", "sm_ship_mode_sk");
    tableAndPK.put("web_site", "web_site_sk");
    tableAndPK.put("date_dim", "d_date_sk");
    tableAndPK.put("store", "s_store_sk");
    tableAndPK.put("store_returns", "sr_returned_date_sk");
  }
  public DefaultDBFlavor(DBName dbName) {
    super(dbName);
  }

  @Override
  //(YYB) !!!!!!!!!!根据tablename替换JdbcDBClient.PRIMARY_KEY
  public String createInsertStatement(StatementType insertType, String key) {
    String tableName = insertType.getTableName();
    StringBuilder insert = new StringBuilder("INSERT INTO ");
    insert.append(insertType.getTableName());
    insert.append(" (" + tableAndPK.get(tableName) + "," + insertType.getFieldString() + ")");
    insert.append(" VALUES(?");
    for (int i = 0; i < insertType.getNumFields(); i++) {
      insert.append(",?");
    }
    insert.append(")");
    return insert.toString();
  }

  @Override
  public String createReadStatement(StatementType readType, String key) {
    StringBuilder read = new StringBuilder("SELECT * FROM ");
    read.append(readType.getTableName());
    read.append(" WHERE ");
    read.append(JdbcDBClient.PRIMARY_KEY);
    read.append(" = ");
    read.append("?");
    return read.toString();
  }

  @Override
  public String createDeleteStatement(StatementType deleteType, String key) {
    StringBuilder delete = new StringBuilder("DELETE FROM ");
    delete.append(deleteType.getTableName());
    delete.append(" WHERE ");
    delete.append(JdbcDBClient.PRIMARY_KEY);
    delete.append(" = ?");
    return delete.toString();
  }

  @Override
  public String createUpdateStatement(StatementType updateType, String key) {
    String[] fieldKeys = updateType.getFieldString().split(",");
    StringBuilder update = new StringBuilder("UPDATE ");
    update.append(updateType.getTableName());
    update.append(" SET ");
    for (int i = 0; i < fieldKeys.length; i++) {
      update.append(fieldKeys[i]);
      update.append("=?");
      if (i < fieldKeys.length - 1) {
        update.append(", ");
      }
    }
    update.append(" WHERE ");
    update.append(JdbcDBClient.PRIMARY_KEY);
    update.append(" = ?");
    return update.toString();
  }

  @Override
  public String createScanStatement(StatementType scanType, String key) {
    StringBuilder select = new StringBuilder("SELECT * FROM ");
    select.append(scanType.getTableName());
    select.append(" WHERE ");
    select.append(JdbcDBClient.PRIMARY_KEY);
    select.append(" >= ?");
    select.append(" ORDER BY ");
    select.append(JdbcDBClient.PRIMARY_KEY);
    select.append(" LIMIT ?");
    return select.toString();
  }
}
