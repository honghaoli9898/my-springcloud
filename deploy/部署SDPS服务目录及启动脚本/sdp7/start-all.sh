#!/bin/bash
cd eureka-server
./start.sh
cd ..
sleep 5
cd config-server
./start.sh
cd ..
sleep 10
cd gateway-server
./start.sh
cd ..
sleep 3
cd uaa-server
./start.sh
cd ..
sleep 3
cd user-center
./start.sh
cd ..
sleep 3
cd item-center
./start.sh
cd ..
sleep 3
cd bigdataCommon-proxy-server
./start.sh
cd ..
sleep 3
cd seabox-proxy-server
./start.sh
cd ..
sleep 3
cd dynamic-route-server
./start.sh
cd ..
sleep 3
cd job-admin
./start.sh
cd ..
sleep 3
cd job-executor
./start.sh
cd ..
sleep 3
cd ssh-web
./start.sh
cd ..
sleep 3
cd user-sync
./start.sh
cd ..
sleep 3
cd license-client
./start.sh
cd ..