#! /bin/bash

ant
cd ../Osm2GpsMid/
ant deploy
cp dist/Osm2GpsMid-0.4.55.jar /home/jan/Desktop/
cd /home/jan/Desktop/
 java -Xmx1024M -jar Osm2GpsMid-0.4.55.jar schleswig-holstein.osm luebeck
