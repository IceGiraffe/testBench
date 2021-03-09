drop table tmp20;
create table tmp20 as select 
   count(distinct WS.ws_order_number) as  order_count
  ,sum(ws_ext_ship_cost) as  total_shipping_cost
  ,sum(ws_net_profit) as  total_net_profit
from web_site WES
JOIN web_sales WS on (WS.ws_web_site_sk = WES.web_site_sk and WES.web_company_name = 'pri')
LEFT OUTER  JOIN (select ws_order_number
    from (select ws1.ws_order_number,ws1.ws_warehouse_sk wh1,ws2. ws_warehouse_sk wh2
         from web_sales ws1  JOIN  web_sales ws2
         on  (ws1.ws_order_number = ws2. ws_order_number) where 
        ws1.ws_warehouse_sk <> ws2. ws_warehouse_sk)  t1)  t2 
on (WS.ws_order_number=t2. ws_order_number)
LEFT OUTER JOIN (select  wr_order_number
    from web_returns WR JOIN  (select ws1.ws_order_number,ws1.ws_warehouse_sk wh1,ws2.ws_warehouse_sk wh2
         from web_sales ws1  JOIN web_sales ws2
         on  (ws1.ws_order_number = ws2.ws_order_number) 
        where  ws1.ws_warehouse_sk <> ws2.ws_warehouse_sk  )  ws_wh
                                on (WR.wr_order_number = ws_wh.ws_order_number)) t3
                            on (WS.ws_order_number=t3.wr_order_number)
                            JOIN date_dim DD on (WS.ws_ship_date_sk = DD.d_date_sk and DD.d_date between '2000-5-01' and  '2000-7-01')  
                            JOIN customer_address CA on (WS.ws_ship_addr_sk = CA.ca_address_sk and CA.ca_state = '0')
                            order by order_count
                            limit 100
                            ;

