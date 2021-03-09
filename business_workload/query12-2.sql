drop table tmp24;
create table tmp24 as select
   sum(ws_ext_discount_amt)  as  Excess_Discount_Amount
from
item  IT
JOIN web_sales WS on (IT.i_item_sk = WS.ws_item_sk and IT. i_manufact_id = 90)
JOIN date_dim DD on (DD.d_date_sk = WS.ws_sold_date_sk and DD.d_date between '2001-01-04' and  '2001-04-04')
where
WS.ws_ext_discount_amt  >  3316.943087363066
order by Excess_Discount_Amount
limit 100;
