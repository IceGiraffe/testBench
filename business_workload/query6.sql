drop table tmp18;
create table tmp18 as select 
   substr(w_warehouse_name,1,20) as substr_w_warehouse
  ,sm_type
  ,web_name
  ,sum (case when (ws_ship_date_sk - ws_sold_date_sk <= 30)  then 1 else 0 end)  as  30days 
  ,sum(case when (ws_ship_date_sk - ws_sold_date_sk > 30)  and  (ws_ship_date_sk - ws_sold_date_sk <= 60) then 1 else 0 end)  as 31_60days 
  ,sum(case when (ws_ship_date_sk - ws_sold_date_sk > 60) and 
                     (ws_ship_date_sk - ws_sold_date_sk <= 90) then 1 else 0 end)  as 61_90days 
                  ,sum(case when (ws_ship_date_sk - ws_sold_date_sk > 90) and
                                     (ws_ship_date_sk - ws_sold_date_sk <= 120) then 1 else 0 end)  as 91_120days 
                                  ,sum(case when (ws_ship_date_sk - ws_sold_date_sk  > 120) then 1 else 0 end)  as  LT120days 
                                from
                                   web_sales WS
                                  JOIN  warehouse WH on (WS.ws_warehouse_sk = WH.w_warehouse_sk)
                                  JOIN  ship_mode SM  on (WS.ws_ship_mode_sk   = SM.sm_ship_mode_sk)
                                  JOIN  web_site WES  on (WS.ws_web_site_sk    = WES.web_site_sk)
                                  JOIN  date_dim DD  on (WS.ws_ship_date_sk   = DD.d_date_sk and DD.d_month_seq between 0 and 0 + 11 )
                                group by
                                   substr(w_warehouse_name,1,20)
                                  ,sm_type
                                  ,web_name
                                order by  substr_w_warehouse
                                        ,sm_type
                                               ,web_name
                                            limit  100
                                            ;

