#!/bin/bash
source ../sdps-base.sh
service_name=dynamic-route-server
jar_package_name=dynamic-route-server.jar
PID=$(ps -ef | grep ${project_dir}/${service_name}/${jar_package_name} | grep -v grep | awk '{ print $2 }')
echo "$service_name"
if [ -z "$PID" ]
then
    echo stopped
else
    echo running at $PID
fi

