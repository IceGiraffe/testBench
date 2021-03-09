#!/bin/bash

file="data-kmeans"
rm -f $file
if [  -f "$file" ]; then
        echo "$file file exists"
        exit -1
fi
./generate-matrix $1 $2 $3
                      #row  col  blank
