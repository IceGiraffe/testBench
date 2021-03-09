drop table tmp21;
create table tmp21 as select  amc/pmc  am_pm_ratio
 from ( select count(*)  amc
           from web_page WP 
        JOIN web_sales WS  on (WS.ws_web_page_sk = WP.wp_web_page_sk and WP.wp_char_count between 5000 and 5200)
        JOIN time_dim TD  on (WS.ws_sold_time_sk = TD.t_time_sk and TD.t_hour between 8 and 8+1)
        JOIN household_demographics HD 
        on (WS.ws_ship_hdemo_sk = HD.hd_demo_sk and HD.hd_dep_count = 5))  at
          JOIN ( select count(*)  pmc
                   from web_page WP
                      JOIN web_sales WS on (WS.ws_web_page_sk = WP.wp_web_page_sk and WP.wp_char_count between 5000 and 5200)
                          JOIN time_dim TD on (WS.ws_sold_time_sk = TD.t_time_sk and TD.t_hour between 19 and 19+1)
                              JOIN household_demographics HD on (WS.ws_ship_hdemo_sk = HD.hd_demo_sk and HD.hd_dep_count = 5)) pt
                         order by am_pm_ratio
                        limit 100
                         ;

