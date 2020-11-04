#!/usr/bin/with-contenv sh

if [ -n "$HEALTHCHECK_ID" ]; then
	curl -sS -X POST -o /dev/null "$HEALTHCHECK_HOST/$HEALTHCHECK_ID/start"
fi

# If the sync fails we want to avoid triggering the health check.
set -e

curl -sS -X POST --fail http://localhost/sync

if [ -n "$HEALTHCHECK_ID" ]; then
	curl -sS -X POST -o /dev/null --fail "$HEALTHCHECK_HOST/$HEALTHCHECK_ID"
fi
