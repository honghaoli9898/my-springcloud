#!/bin/bash
#公共变量
source /data/SDP7.1/sdps/sdp7.1/sdps-base.sh
service_name=seabox-proxy-server
jar_package_name=bigdata-seabox-proxy.jar
cd ${project_dir}/${service_name}

PID=$(ps -ef | grep ${project_dir}/${service_name}/${jar_package_name} | grep -v grep | awk '{ print $2 }')

if [ -z "$PID" ]
then
    echo Application is already stopped
else
    echo kill $PID
    kill -9 $PID
fi

#nohup java -jar -Xms256M -Xmx256M ${project_dir}/${service_name}/${jar_package_name} >/dev/null &
echo ${project_dir}/${service_name}/${jar_package_name}
#nohup java -jar -Xms256M -Xmx256M ${project_dir}/${service_name}/${jar_package_name} > /dev/null 2>&1 &
nohup java -jar -Xms512M -Xmx512M ${project_dir}/${service_name}/${jar_package_name} > /dev/null 2>&1 &

START_PID=$(ps -ef | grep ${project_dir}/${service_name}/${jar_package_name} | grep -v grep | awk '{ print $2 }')
echo "Starting $service_name start_pid=$START_PID"
