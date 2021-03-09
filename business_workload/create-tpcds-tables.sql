CREATE EXTERNAL TABLE web_sales (
ws_sold_date_sk           INT,
    ws_sold_time_sk           INT,
    ws_ship_date_sk           INT,
    ws_item_sk                INT,
    ws_bill_customer_sk       INT,
    ws_bill_cdemo_sk          INT,
    ws_bill_hdemo_sk          INT,
    ws_bill_addr_sk           INT,
    ws_ship_customer_sk       INT,
    ws_ship_cdemo_sk          INT,
    ws_ship_hdemo_sk          INT,
    ws_ship_addr_sk           INT,
    ws_web_page_sk            INT,
    ws_web_site_sk            INT,
    ws_ship_mode_sk           INT,
    ws_warehouse_sk           INT,
    ws_promo_sk               INT,
    ws_order_number           INT,
    ws_quantity               INT,
    ws_wholesale_cost         DOUBLE,
    ws_list_price             DOUBLE,
    ws_sales_price            DOUBLE,
    ws_ext_discount_amt       DOUBLE,
    ws_ext_sales_price        DOUBLE,
    ws_ext_wholesale_cost     DOUBLE,
    ws_ext_list_price         DOUBLE,
    ws_ext_tax                DOUBLE,
    ws_coupon_amt             DOUBLE,
    ws_ext_ship_cost          DOUBLE,
    ws_net_paid               DOUBLE,
    ws_net_paid_inc_tax       DOUBLE,
    ws_net_paid_inc_ship      DOUBLE,
    ws_net_paid_inc_ship_tax  DOUBLE,
ws_net_profit             DOUBLE) 
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/web_sales';

CREATE EXTERNAL TABLE date_dim
(
    d_date_sk                 INT               ,
    d_date_id                 STRING              ,
    d_date  STRING   ,
    d_month_seq               INT,
    d_week_seq                INT,
    d_quarter_seq             INT,
    d_year  INT,
    d_dow   INT,
    d_moy   INT,
    d_dom   INT,
    d_qoy   INT,
    d_fy_year                 INT,
    d_fy_quarter_seq          INT,
    d_fy_week_seq             INT,
    d_day_name                STRING,
    d_quarter_name            STRING,
    d_holiday                 STRING,
    d_weekend                 STRING,
    d_following_holiday       STRING,
    d_first_dom               INT,
    d_last_dom                INT,
    d_same_day_ly             INT,
    d_same_day_lq             INT,
    d_current_day             STRING,
    d_current_week            STRING,
    d_current_month           STRING,
    d_current_quarter         STRING,
    d_current_year            STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/date_dim';

CREATE EXTERNAL TABLE item
(
    i_item_sk                 INT               ,
    i_item_id                 STRING              ,
    i_rec_start_date          STRING   ,
    i_rec_end_date            STRING   ,
    i_item_desc               STRING,
    i_current_price           DOUBLE,
    i_wholesale_cost          DOUBLE,
    i_brand_id                INT,
    i_brand STRING    ,
    i_class_id                INT,
    i_class STRING    ,
    i_category_id             INT,
    i_category                STRING    ,
    i_manufact_id             INT,
    i_manufact                STRING    ,
    i_size  STRING    ,
    i_formulation             STRING    ,
    i_color STRING    ,
    i_units STRING    ,
    i_container               STRING    ,
    i_manager_id              INT,
    i_product_name            STRING 
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/item';


CREATE EXTERNAL TABLE warehouse
(
    w_warehouse_sk            INT               ,
    w_warehouse_id            STRING              ,
    w_warehouse_name          STRING ,
    w_warehouse_sq_ft         INT,
    w_street_number           STRING    ,
    w_street_name             STRING ,
    w_street_type             STRING    ,
    w_suite_number            STRING    ,
    w_city  STRING ,
    w_county STRING ,
    w_state STRING,
    w_zip   STRING    ,
    w_country                 STRING ,
    w_gmt_offset              DOUBLE
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/warehouse';

CREATE EXTERNAL TABLE promotion
(
    p_promo_sk                INT               ,
    p_promo_id                STRING              ,
    p_start_date_sk           INT,
    p_end_date_sk             INT,
    p_item_sk                 INT,
    p_cost  DOUBLE                 ,
    p_response_target         INT,
    p_promo_name              STRING    ,
    p_channel_dmail           STRING,
    p_channel_email           STRING,
    p_channel_catalog         STRING,
    p_channel_tv              STRING,
    p_channel_radio           STRING,
    p_channel_press           STRING,
    p_channel_event           STRING,
    p_channel_demo            STRING,
    p_channel_details         STRING,
    p_purpose                 STRING    ,
    p_discount_active         STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/promotion';



CREATE EXTERNAL TABLE time_dim
(
    t_time_sk                 INT               ,
    t_time_id                 STRING              ,
    t_time  INT,
    t_hour  INT,
    t_minute INT,
    t_second INT,
    t_am_pm STRING,
    t_shift STRING    ,
    t_sub_shift               STRING    ,
    t_meal_time               STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/time_dim';

CREATE EXTERNAL TABLE web_page
(
    wp_web_page_sk            INT               ,
    wp_web_page_id            STRING              ,
    wp_rec_start_date         STRING   ,
    wp_rec_end_date           STRING   ,
    wp_creation_date_sk       INT,
    wp_access_date_sk         INT,
    wp_autogen_flag           STRING,
    wp_customer_sk            INT,
    wp_url  STRING,
    wp_type STRING    ,
    wp_char_count             INT,
    wp_link_count             INT,
    wp_image_count            INT,
    wp_max_ad_count           INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/web_page';


CREATE EXTERNAL TABLE ship_mode
(
    sm_ship_mode_sk           INT               ,
    sm_ship_mode_id           STRING              ,
    sm_type STRING    ,
    sm_code STRING    ,
    sm_carrier                STRING    ,
    sm_contract               STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/ship_mode';


CREATE EXTERNAL TABLE customer_demographics
(
    cd_demo_sk                INT               ,
    cd_gender                 STRING,
    cd_marital_status         STRING,
    cd_education_status       STRING    ,
    cd_purchase_estimate      INT,
    cd_credit_rating          STRING    ,
    cd_dep_count              INT,
    cd_dep_employed_count     INT,
    cd_dep_college_count      INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/customer_demographics';

CREATE EXTERNAL TABLE customer
(
    c_customer_sk             INT               ,
    c_customer_id             STRING              ,
    c_current_cdemo_sk        INT,
    c_current_hdemo_sk        INT,
    c_current_addr_sk         INT,
    c_first_shipto_date_sk    INT,
    c_first_sales_date_sk     INT,
    c_salutation              STRING    ,
    c_first_name              STRING    ,
    c_last_name               STRING    ,
    c_preferred_cust_flag     STRING,
    c_birth_day               INT,
    c_birth_month             INT,
    c_birth_year              INT,
    c_birth_country           STRING ,
    c_login STRING    ,
    c_email_address           STRING    ,
    c_last_review_date        STRING 
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/customer';

CREATE EXTERNAL TABLE customer_address
(
    ca_address_sk             INT               ,
    ca_address_id             STRING              ,
    ca_street_number          STRING    ,
    ca_street_name            STRING ,
    ca_street_type            STRING    ,
    ca_suite_number           STRING    ,
    ca_city STRING ,
    ca_county                 STRING ,
    ca_state STRING,
    ca_zip  STRING    ,
    ca_country                STRING ,
    ca_gmt_offset             DOUBLE,
    ca_location_type          STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/customer_address';

CREATE EXTERNAL TABLE household_demographics
(
    hd_demo_sk                INT               ,
    hd_income_band_sk         INT,
    hd_buy_potential          STRING    ,
    hd_dep_count              INT,
hd_vehicle_count          INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/household_demographics';

CREATE EXTERNAL TABLE income_band
(
    ib_income_band_sk         INT               ,
    ib_lower_bound            INT,
    ib_upper_bound            INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/income_band';

CREATE EXTERNAL TABLE reason
(
    r_reason_sk               INT               ,
    r_reason_id               STRING              ,
    r_reason_desc             STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/reason';


CREATE EXTERNAL TABLE web_returns
(
    wr_returned_date_sk       INT,
    wr_returned_time_sk       INT,
    wr_item_sk                INT               ,
    wr_refunded_customer_sk   INT,
    wr_refunded_cdemo_sk      INT,
    wr_refunded_hdemo_sk      INT,
    wr_refunded_addr_sk       INT,
    wr_returning_customer_sk  INT,
    wr_returning_cdemo_sk     INT,
    wr_returning_hdemo_sk     INT,
    wr_returning_addr_sk      INT,
    wr_web_page_sk            INT,
    wr_reason_sk              INT,
    wr_order_number           INT               ,
    wr_return_quantity        INT,
    wr_return_amt             DOUBLE,
    wr_return_tax             DOUBLE,
    wr_return_amt_inc_tax     DOUBLE,
    wr_fee  DOUBLE,
    wr_return_ship_cost       DOUBLE,
    wr_refunded_cash          DOUBLE,
    wr_reversed_charge        DOUBLE,
    wr_account_credit         DOUBLE,
    wr_net_loss               DOUBLE
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/web_returns';



CREATE EXTERNAL TABLE web_site
(
    web_site_sk               INT               ,
    web_site_id               STRING              ,
    web_rec_start_date        STRING   ,
    web_rec_end_date          STRING   ,
    web_name STRING ,
    web_open_date_sk          INT,
    web_close_date_sk         INT,
    web_class                 STRING ,
    web_manager               STRING ,
    web_mkt_id                INT,
    web_mkt_class             STRING ,
    web_mkt_desc              STRING,
    web_market_manager        STRING ,
    web_company_id            INT,
    web_company_name          STRING    ,
    web_street_number         STRING    ,
    web_street_name           STRING ,
    web_street_type           STRING    ,
    web_suite_number          STRING    ,
    web_city STRING ,
    web_county                STRING ,
    web_state                 STRING,
    web_zip STRING    ,
    web_country               STRING ,
    web_gmt_offset            DOUBLE,
    web_tax_percentage        DOUBLE
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'  STORED AS TEXTFILE LOCATION '/tpcdsdata/web_site';

