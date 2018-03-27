#!/bin/bash

cd src/Lambency-Server

echo "update respistory"
#git pull origin dev
git fetch origin dev
git reset --hard origin/dev

echo "Repository updated"


echo "update dependencies and build project"
mvn -Dmaven.test.skip=true package

echo "project built"

cd /home/lambency/cs307

file="$(echo /home/lambency/cs307/src/Lambency-Server/target/*.jar)"

#kill previous server
pkill java
echo "Killed Previous Server"

echo $file

nohup java -jar $file > serverOutput.txt &
echo "Server Running"
