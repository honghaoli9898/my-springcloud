#!/bin/bash
#公共变量
source ../sdps-base.sh

service_name=dynamic-route-server
jar_package_name=dynamic-route-server.jar

PID=$(ps -ef | grep ${project_dir}/${service_name}/${jar_package_name} | grep -v grep | awk '{ print $2 }')

if [ -z "$PID" ]
then
    echo Application is already stopped
else
    echo kill $PID
    kill -9 $PID
fi

nohup java -jar -Xms256M -Xmx256M ${project_dir}/${service_name}/${jar_package_name} >/dev/null &
START_PID=$(ps -ef | grep ${project_dir}/${service_name}/${jar_package_name} | grep -v grep | awk '{ print $2 }')
echo "Starting $service_name start_pid=$START_PID"
