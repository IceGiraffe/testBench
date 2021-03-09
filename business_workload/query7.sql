drop table tmp19;
create table tmp19 as select  r_reason_desc
       ,avg(ws_quantity) as avg_ws_quantity
           ,avg(wr_refunded_cash) as avg_wr_refunded
               ,avg(wr_fee) as avg_wr_fee
             from web_sales WS  
            JOIN  web_returns WR on (WS.ws_order_number = WR.wr_order_number and WS.ws_item_sk = WR.wr_item_sk)  
            JOIN  web_page  WP on(WS.ws_web_page_sk = WP.wp_web_page_sk)
            JOIN  customer_demographics  cd1  on (cd1.cd_demo_sk = WR.wr_refunded_cdemo_sk)
            JOIN customer_demographics cd2 on (cd2.cd_demo_sk = WR.wr_returning_cdemo_sk)
            JOIN customer_address CA on (CA.ca_address_sk = WR.wr_refunded_addr_sk)
            JOIN  date_dim DD on (WS.ws_sold_date_sk = DD.d_date_sk and DD.d_year = 2001)
            JOIN  reason RS on (RS.r_reason_sk = WR.wr_reason_sk)
            where
               (
                    (
                             cd1.cd_marital_status = 'S'
                                 and
                                     cd1.cd_marital_status = cd2.cd_marital_status
                                         and
                                             cd1.cd_education_status = 'Secondary'
                                                 and 
                                                     cd1.cd_education_status = cd2.cd_education_status
                                                         and
                                                             ws_sales_price between 100.00 and 150.00
                                                                )
                                                                   or
                                                                    (
                                                                             cd1.cd_marital_status = 'M'
                                                                                 and
                                                                                     cd1.cd_marital_status = cd2.cd_marital_status
                                                                                         and
                                                                                             cd1.cd_education_status = 'Primary' 
                                                                                                 and
                                                                                                     cd1.cd_education_status = cd2.cd_education_status
                                                                                                         and
                                                                                                             ws_sales_price between 50.00 and 100.00
                                                                                                                )
                                                                                                                   or
                                                                                                                    (
                                                                                                                             cd1.cd_marital_status = 'D'
                                                                                                                                 and
                                                                                                                                     cd1.cd_marital_status = cd2.cd_marital_status
                                                                                                                                         and
                                                                                                                                             cd1.cd_education_status = 'Unknown'
                                                                                                                                                 and
                                                                                                                                                     cd1.cd_education_status = cd2.cd_education_status
                                                                                                                                                         and
                                                                                                                                                             ws_sales_price between 150.00 and 200.00
                                                                                                                                                                )
                                                                                                                                                                   )
                                                                                                                                                                   and
                                                                                                                                                                   (
                                                                                                                                                                        (
                                                                                                                                                                                 ca_country = 'United States'
                                                                                                                                                                                     and
                                                                                                                                                                                         ca_state in ('GA', 'VA', 'IN')
                                                                                                                                                                                             and ws_net_profit between 100 and 200  
                                                                                                                                                                                                )
                                                                                                                                                                                                    or
                                                                                                                                                                                                        (
                                                                                                                                                                                                                 ca_country = 'United States'
                                                                                                                                                                                                                     and
                                                                                                                                                                                                                         ca_state in ('OK', 'SD', 'NM')
                                                                                                                                                                                                                             and ws_net_profit between 150 and 300  
                                                                                                                                                                                                                                )
                                                                                                                                                                                                                                    or
                                                                                                                                                                                                                                        (
                                                                                                                                                                                                                                                 ca_country = 'United States'
                                                                                                                                                                                                                                                     and
                                                                                                                                                                                                                                                         ca_state in ('WA', 'KY', 'NC')
                                                                                                                                                                                                                                                             and ws_net_profit between 50 and 250  
                                                                                                                                                                                                                                                                )
                                                                                                                                                                                                                                                                   )
                                                                                                                                                                                                                                                                group by r_reason_desc
                                                                                                                                                                                                                                                                order by r_reason_desc
                                                                                                                                                                                                                                                                        , avg_ws_quantity
                                                                                                                                                                                                                                                                                , avg_wr_refunded
                                                                                                                                                                                                                                                                                        , avg_wr_fee
                                                                                                                                                                                                                                                                                        limit 100
                                                                                                                                                                                                                                                                                        ;

