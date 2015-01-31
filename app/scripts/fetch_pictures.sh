#!/bin/bash

if [ $# -ne 2 ]
then
	echo "usage: <image_count> <target_dir>"
	exit
fi

count=`expr $1 - 1`
dir=$2

if [[ $dir != */ ]]
then
	dir=$dir/
fi

for i in `seq 0 $count`; do
	curl -o $dir$i.png 'https://robohash.org/'$i'.png/bgset_any?ignoreext=false'
done
