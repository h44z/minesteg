#!/bin/bash
# run minesteg receiver without minecraft launcher

printf "\n$(date) | Starting unix game session! " >> receiver_run.txt
java -jar minesteg-receiver-1.0.0.jar --accessToken 0 --version 1.14.4 --assetIndex 1.14 --assetsDir /home/$USER/.minecraft/assets/ >> received_text.txt

