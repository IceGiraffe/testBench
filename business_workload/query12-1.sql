drop table tmp23;
create table tmp23 as SELECT  1.3*avg(ws_ext_discount_amt) 
         FROM date_dim DD2
                 JOIN web_sales WS2 on (DD2.d_date_sk = WS2.ws_sold_date_sk and DD2.d_date between '2001-01-04' and '2001-04-04')

