#!/bin/bash
for i in {1..3}
do

	for j in {1..50}
	do
		echo $(($i*50))_$j
		java -jar GHS.jar graphs/gabriel_$(($i*50))_$j.dot -p performance.csv -cs 1 -v 10000
	done
done
