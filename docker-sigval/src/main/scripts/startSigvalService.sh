#!/usr/bin/env bash

#
# This script is used to start the sign service applications of this tomcat instance.
#

usage() {
    echo "Usage: $0 [options...]" >&2
    echo
    echo "   -d                     Debug mode"
    echo "   -f, --folder           Configuration folder path (default /opt/tsltrust/sigval)"
    echo "   -h, --help             Prints this help"
    echo
}


SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Replace /cygdrive/c with c:/ (if running on Windows)
SCRIPT_DIR_WIN=`echo $SCRIPT_DIR | sed 's/\/cygdrive\/c/c:/g'`

# Remove /src/main/scripts
BASE_DIR_WIN=`echo $SCRIPT_DIR_WIN | sed 's/\/bin//g'`

# Tomcat
TOMCAT_HOME=$BASE_DIR_WIN
CATALINA_HOME=$TOMCAT_HOME

CONFIG_FOLDER=/opt/tsltrust/sigval

DEBUG_MODE=false

while :
do
    case "$1" in
	-h | --help)
	    usage
	    exit 0
	    ;;
  -f | --folder)
	    CONFIG_FOLDER="$2"
	    shift 2
	    ;;
	--)
	    shift
	    break;
	    ;;
	-d)
        DEBUG_MODE=true
	    shift
	    break;
	    ;;
	-*)
	    echo "Error: Unknown option: $1" >&2
	    usage
	    exit 0
	    ;;
	*)
	    break
	    ;;
    esac
done


# ENV variables
export SIGVAL_DATALOCATION=${CONFIG_FOLDER}

#
# System settings
#
export JAVA_OPTS="-XX:MaxPermSize=512m"
export CATALINA_OPTS="-Xms512m -Xmx1536m"
export JVM_MAX_HEAP="1536m"
export JVM_START_HEAP="512m"
export DEBUG_PORT=8000
export

export CATALINA_OPTS="\
          -Xmx${JVM_MAX_HEAP}\
          -Xms${JVM_START_HEAP}\
          -Dtomcat.tls.server-key=$SIGVAL_DATALOCATION/tomcat/tomcat-key.pem \
          -Dtomcat.tls.server-key-type=RSA \
          -Dtomcat.tls.server-certificate=$SIGVAL_DATALOCATION/tomcat/tomcat-cert.pem \
          -Dtomcat.tls.certificate-chain=$SIGVAL_DATALOCATION/tomcat/tomcat-chain.pem \
          -Dtomcat.loglevel.sigserv=INFO \
          -Dtomcat.maxlogdays=2 \
          -Dorg.apache.xml.security.ignoreLineBreaks=true \
"

#
# Debug
#
export JPDA_ADDRESS=8000
export JPDA_TRANSPORT=dt_socket

if [ $DEBUG_MODE == true ]; then
    echo "Running in debug"
    $CATALINA_HOME/bin/catalina.sh jpda run
else
    echo "Running in normal mode"
    $CATALINA_HOME/bin/catalina.sh run
fi
