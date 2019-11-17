@echo off
REM run minesteg receiver without minecraft launcher

@echo %date%-%time% - Starting windows game session! >> receiver_run.txt
java -jar minesteg-receiver-1.0.0.jar --accessToken 0 --version 1.14.4 --assetIndex 1.14 --assetsDir %appdata%/.minecraft/assets/ >> receiver_run.txt

