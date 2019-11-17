# Minesteg

This repository contains code that I have written while doing research for my bachelor thesis.
The aim of the work was to show that it is possible to use the video game Minecraft as a Covered Channel.

The code provided uses LSB-Embedding in order to hide secret messages in the position data of Minecraft.
Using this small changes in the angles of pitch and yaw gives an average datarate of 8.8 bit/s with about 20% Byte-loss.

The embedding can be easily be detected due to the LSB-Embedding. 
If a more secure steganographic channel is needed, a better embedding algorithm has to be developed. 
Pull requests welcome ;)


## Repository Contents

The repo contains three main folders.
 - mcserver: this folder contains a gradle script that can be used to decompile, recompile and run the Minecraft server code.
 - messageclient: here the code for the client utility, that is used to fill the message buffers of the steganographic system, can be found.
 - minesteg: the implementation of the steganographic system using LSB-Embedding can be found within this folder

To decompile and deobfuscate the Minecraft sourcecode, MCP (Mod Coder Pack) is used. Hexeption created a gradle script to simplify the usage of MCP. All gradle scripts of this repository are based on the work of Hexeption (`https://github.com/Hexeption/MCP-Reborn`).


## Usage and Development Documentation

### Setup
Clone this repo, and then run:
 - cd minesteg
 - ./gradlew setup
 - ./gradlew build
 - ./gradlew applyHooks
 - ./gradlew zipLauncherFolder -PbuildType=sender/receiver  # sender or receiver, depending on which configuration you want

The compiled zip file can then be copied to your minecraft installation directory. 
Extract the folder to `~/.minecraft/versions/`, then a new launch configuration can be created in the Minecraft-Launcher.

Use the messageclient to fill and read the message buffer that contains the secret message.


### Development
All three folders of this repository can be imported to IntelliJ (import the build.gradle files as projects).

Copy auth.properties.example to auth-receiver.properties and auth-sender.properties and edit the values accordingly.
The content of minesteg-*.properties might also be adjusted to fit your needs.



If you need more informations regarding the thesis, feel free to contact me via email: christoph.haas at student.uibk.ac.at.