#!/bin/bash

echo "Input file: $1";
./kronfit -i:$1 -n0:2 -m:"0.9 0.6; 0.6 0.1" -gi:100
tmp=$(echo $1|cut -d '.' -f1)
str="${tmp}-fit2"
echo $str
string="Estimated"
cat $str| while read line
do
  result=$(echo $line| grep "${string}")
  if [ "$result" != "" ]
  then
    param=$(echo $result| cut -d '[' -f2 | cut -d ']' -f1)
    para="\"${param}\""
    echo $para
    ./krongen -o:"${tmp}_new.txt" -n0:2 -m:"$para" -i:$2
  fi
done




