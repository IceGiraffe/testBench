drop table tmp17;
create table tmp17 as select channel, col_name, d_year, d_qoy, i_category, COUNT(*) sales_cnt, SUM(ext_sales_price) sales_amt FROM (SELECT 'web' as channel, 'ws_bill_customer_sk' col_name, d_year, d_qoy, i_category, ws_ext_sales_price ext_sales_price
             FROM web_sales  JOIN  item  ON (web_sales.ws_item_sk=item.i_item_sk) JOIN  date_dim    on  (web_sales.ws_sold_date_sk=date_dim.d_date_sk)
                     WHERE ws_bill_customer_sk IS NULL
                                     )  foo 
                                    GROUP BY channel, col_name, d_year, d_qoy, i_category
                                    ORDER BY channel, col_name, d_year, d_qoy, i_category
                                    limit 100
                                    ;

