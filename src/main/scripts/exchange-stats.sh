#!/bin/sh

### BEGIN INIT INFO
# Provides:          exchange-stats
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: initscript for exchange-stats java app
### END INIT INFO

# https://leonid.shevtsov.me/post/how-to-make-a-java-daemon-with-start-stop-daemon/

PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
DESC="exchange-stats"
NAME="exchange-stats"
CWD=/home/pi
USER=pi
GROUP=pi
JAVA=/usr/bin/java
JVM_ARGS=
JAR_PATH=/home/pi/exchange-stats.jar
JAVA_ARGS="$JVM_ARGS -jar $JAR_PATH"
PIDFILE=/home/pi/$NAME.pid
SCRIPTNAME=/etc/init.d/$NAME

# Load the VERBOSE setting and other rcS variables
. /lib/init/vars.sh

# Define LSB log_* functions.
# Depend on lsb-base (>= 3.2-14) to ensure that this file is present
# and status_of_proc is working.
. /lib/lsb/init-functions

# Test that Java is installed
if [ ! -x "$JAVA" ]; then
  log_failure_msg "Java executable not found at $JAVA"
  exit 2
fi

# Test that the application jar is present
if [ ! -r "$JAR_PATH" ]; then
  log_failure_msg "Application JAR not found at $JAR_PATH"
  exit 2
fi

#
# Function that starts the daemon/service
#
do_start()
{
  # Return
  #   0 if daemon has been started
  #   1 if daemon was already running
  #   2 if daemon could not be started
  start-stop-daemon --start \
    --quiet \
    --pidfile $PIDFILE \
    --user $USER \
    --exec $JAVA \
    --test > /dev/null \
    || return 1
  start-stop-daemon --start \
    --quiet \
    --make-pidfile \
    --pidfile $PIDFILE \
    --chuid $USER \
    --user $USER \
    --group $GROUP \
    --chdir $CWD \
    --background \
    --exec $JAVA \
    -- $JAVA_ARGS \
    || return 2
}

#
# Function that stops the daemon/service
#
do_stop()
{
  # Return
  #   0 if daemon has been stopped
  #   1 if daemon was already stopped
  #   2 if daemon could not be stopped
  #   other if a failure occurred
  start-stop-daemon --stop \
    --quiet \
    --user $USER \
    --pidfile $PIDFILE \
    --exec $JAVA \
    --retry=TERM/30/KILL/5
  RETVAL="$?"
  if [ "$RETVAL" = 2 ]; then
    return 2
  fi
  rm -f $PIDFILE
  return "$RETVAL"
}

#
# Function that checks if the daemon is running
#
do_status()
{
  start-stop-daemon \
    --start \
    --test \
    --oknodo \
    --pidfile $PIDFILE \
    --user $USER \
    --exec $JAVA 
}

case "$1" in
  start)
  if [ "$VERBOSE" != no ]; then
    log_daemon_msg "Starting $DESC" "$NAME"
  fi
  do_start
  case "$?" in
    0|1) 
      if [ "$VERBOSE" != no ]; then
        log_end_msg 0
      fi
      ;;
    2) 
      if [ "$VERBOSE" != no ]; then
        log_end_msg 1
      fi
      ;;
  esac
  ;;
  
  stop)
  if [ "$VERBOSE" != no ]; then
    log_daemon_msg "Stopping $DESC" "$NAME"
  fi
  do_stop
  case "$?" in
    0|1) 
      if [ "$VERBOSE" != no ]; then
        log_end_msg 0
      fi
      ;;
    2) 
      if [ "$VERBOSE" != no ]; then
        log_end_msg 1
      fi
      ;;
  esac
  ;;

  status)
  do_status
  ;;

  restart|force-reload)

  log_daemon_msg "Restarting $DESC" "$NAME"
  do_stop
  case "$?" in
    0|1)
    do_start
    case "$?" in
      0) 
        log_end_msg 0
        ;;
      1) 
        # Old process is still running
        log_end_msg 1
        ;;
      *) 
        # Failed to start
        log_end_msg 1
        ;;
    esac
    ;;
    *)
      # Failed to stop
      log_end_msg 1
      ;;
  esac
  ;;
  *)
  echo "Usage: $SCRIPTNAME {start|stop|status|restart|force-reload}" >&2
  exit 3
  ;;
esac
