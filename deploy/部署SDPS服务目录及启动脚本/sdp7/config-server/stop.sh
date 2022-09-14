#!/bin/bash

jar_package_name=config-server.jar

PID=$(ps -ef | grep ${jar_package_name} | grep -v grep | awk '{ print $2 }')
if [ -z "$PID" ]
then
    echo Application is already stopped
else
    echo kill $PID
    kill -9 $PID
fi
