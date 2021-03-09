#!/bash/sh
cd ./TPC_DS_DataGen/v2.8.0rc4/tools/
mkdir dataGen
./dsdgen -DIR ./dataGen -SCALE 1 
wait
rm ./dataGen/dbgen_version.dat