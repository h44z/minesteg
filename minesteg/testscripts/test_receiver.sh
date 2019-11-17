#!/bin/bash
# fetch received buffer every minute
printf "\n$(date) | Starting unix log session! " >> received_text.txt

mins=10
while true
do
    printf "\n$(date) | Received: " >> received_text.txt
    java -jar messageclient-1.0.0.jar -p 1098 -t >> received_text.txt
    sleep 1m
    : $((mins--))

    if [ $mins -le 0 ]
    then
        echo "#################################"
        echo "      10 Minutes are over!!!!    "
        echo "#################################"
    fi
done
