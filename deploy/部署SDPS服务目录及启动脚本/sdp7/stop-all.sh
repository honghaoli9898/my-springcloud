#!/bin/bash
cd ssh-web
./stop.sh
cd ..
cd job-executor
./stop.sh
cd ..
cd job-admin
./stop.sh
cd ..
cd item-center
./stop.sh
cd ..
cd bigdataCommon-proxy-server
./stop.sh
cd ..
cd seabox-proxy-server
./stop.sh
cd ..
cd dynamic-route-server
./stop.sh
cd ..
cd uaa-server
./stop.sh
cd ..
cd user-center
./stop.sh
cd ..
cd user-sync
./stop.sh
cd ..
cd gateway-server
./stop.sh
cd ..
sleep 5
cd eureka-server
./stop.sh
cd ..
cd config-server
./stop.sh
cd ..
cd license-client
./stop.sh
cd ..

