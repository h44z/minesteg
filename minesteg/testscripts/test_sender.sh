#!/bin/bash
line=$(head -n 1 testtext.txt)

# refill sendbuffer every minute
while true
do
    java -jar messageclient-1.0.0.jar -a "$line"
    sleep 1m
done
