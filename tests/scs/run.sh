#!/bin/bash


# Set following config options:
cat <<EOF > coaster-service.conf
# Set ipaddr of the headnode
IPADDR=128.135.250.235
WORKER_USERNAME="yadunandb"
# Space separated list of remote hosts as shown below
WORKER_HOSTS="communicado.ci.uchicago.edu bridled.ci.uchicago.edu"
# Worker concurrency of 2 will allow each worker to run 2 tasks in parallel
WORKER_CONCURRENCY=2
WORKER_LOGGING_LEVEL="DEBUG"
# Please make sure the the following directories are valid
WORKER_LOG_DIR="/home/yadunandb/workers/"
WORKER_LOCATION="/home/yadunandb/workers/"
# You do not need to change the following
WORKER_MODE=ssh
SSH_TUNNELING="no"
JOBSPERNODE=$WORKER_CONCURRENCY
EOF


start-coaster-service

if [ "$?" == "0" ]
then
    cp swift.conf wordcount/
fi
