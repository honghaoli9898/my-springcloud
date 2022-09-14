#!/bin/bash

jar_package_name=license-client-1.0.0.jar

PID=$(ps -ef | grep ${jar_package_name} | grep -v grep | awk '{ print $2 }')
if [ -z "$PID" ]
then
    echo Application is already stopped
else
    echo kill $PID
    kill -9 $PID
fi
