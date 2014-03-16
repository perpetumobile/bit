#!/bin/sh

PROJ_DIR=~/bit
source $PROJ_DIR/bin/env.sh

export CATALINA_BASE=$PROJ_DIR/webapp
export CATALINA_PID=$PROJ_DIR/bit.pid

~/apache-tomcat-6.0.36/bin/shutdown.sh $@
