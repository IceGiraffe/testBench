#!/bin/bash
for i in customer customer_address customer_demographics date_dim household_demographics income_band item promotion reason ship_mode time_dim warehouse web_page web_returns web_sales web_site
do
	mkdir $1$i
	mv $1$i.dat $1$i/
done
