#!/bin/sh

PROJ_DIR=~/bit
source $PROJ_DIR/bin/env.sh

export CLASSPATH="$PROJ_DIR/properties/:$PROJ_DIR/webapp"

export CATALINA_BASE=$PROJ_DIR/webapp
export CATALINA_PID=$PROJ_DIR/bit.pid
export CATALINA_OPTS="-Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl -server -Xms512M -Xmx512M"

~/apache-tomcat-6.0.36/bin/startup.sh
