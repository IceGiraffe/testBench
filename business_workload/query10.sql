drop table tmp22;
create table tmp22 as select  ca_zip, ca_city, sum(ws_sales_price)
 from  item IT
JOIN web_sales WS on (WS.ws_item_sk = IT.i_item_sk)
JOIN customer C on (WS.ws_bill_customer_sk =C. c_customer_sk)
JOIN customer_address CA on (C.c_current_addr_sk = CA.ca_address_sk)
JOIN date_dim DD on (WS.ws_sold_date_sk = DD.d_date_sk and DD.d_qoy = 2 and DD.d_year = 2000)
left outer join (select i_item_id from item  where i_item_sk in (2, 3, 5, 7, 11, 13, 17, 19, 23, 29)) t1 on (IT.i_item_id=t1.i_item_id)
where
substr(ca_zip,1,5) in ('85669', '86197','88274','83405','86475', '85392', '85460', '80348', '81792')
 group by ca_zip, ca_city
 order by ca_zip, ca_city
limit 100;

