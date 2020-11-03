#!/usr/bin/with-contenv sh

if [ -z "$CRON" ]; then
	echo "
Not running in cron mode
"
	exit 0
fi

if [ ! -d /data ]; then
	echo "
ERROR: '/data' directory must be mounted
"
	exit 1
fi

if [ -z "$HEALTHCHECK_ID" ]; then
	echo "
NOTE: Define HEALTHCHECK_ID with https://healthchecks.io to monitor sync job"
fi

if [ -z "$ACCESS_TOKEN" ]; then
	echo "
ERROR: 'ACCESS_TOKEN' environment variable not set"
	exit 1
fi
if [ -z "$ACCESS_SECRET" ]; then
	echo "
ERROR: 'ACCESS_SECRET' environment variable not set"
	exit 1
fi
if [ -z "$API_KEY" ]; then
	echo "
ERROR: 'API_KEY' environment variable not set"
	exit 1
fi
if [ -z "$API_SECRET" ]; then
	echo "
ERROR: 'API_SECRET' environment variable not set"
	exit 1
fi

# Set up the cron schedule.
echo "
Initializing cron

$CRON
"
crontab -u abc -d # Delete any existing crontab.
echo "$CRON /usr/bin/flock -n /app/sync.lock /app/sync.sh" >/tmp/crontab.tmp
crontab -u abc /tmp/crontab.tmp
rm /tmp/crontab.tmp
