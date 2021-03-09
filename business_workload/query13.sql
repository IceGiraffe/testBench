drop table tmp25;
create table tmp25 as select 
   count(distinct WS.ws_order_number) as  order_count
  ,sum(WS.ws_ext_ship_cost) as  total_shipping_cost
  ,sum(WS.ws_net_profit) as  total_net_profit
from
web_site WES
JOIN web_sales WS on (WS.ws_web_site_sk = WES.web_site_sk and WES.web_company_name = 'pri')
JOIN date_dim DD on (WS.ws_ship_date_sk = DD.d_date_sk and DD.d_date between '1999-5-01' and '1999-7-01')
JOIN customer_address CA on (WS.ws_ship_addr_sk = CA.ca_address_sk and CA.ca_state = 'MT')
LEFT OUTER JOIN web_sales WS2 on (WS.ws_order_number = WS2.ws_order_number)
LEFT OUTER JOIN web_returns WR on (WS.ws_order_number = WR.wr_order_number)
where  WS.ws_warehouse_sk <> WS2.ws_warehouse_sk
       and WS2.ws_item_sk is not null
           and WR.wr_item_sk is null
        order by order_count
        limit 100
        ;

