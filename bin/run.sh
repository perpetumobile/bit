#!/bin/sh

PROJ_DIR=~/bit
source $PROJ_DIR/bin/env.sh

CLASSPATH="$PROJ_DIR/properties/:$PROJ_DIR/build:"

LIBS=`ls -1 $PROJ_DIR/lib/*.jar`
LIBS=` echo $LIBS | sed -e 's/ /:/g'`

CLASSPATH="$CLASSPATH$LIBS"
export CLASSPATH

# JAVA=$JAVA_HOME/bin/java
# $JAVA $JAVAPARAMS $*

java $JAVAPARAMS $*