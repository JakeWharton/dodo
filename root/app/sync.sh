#!/usr/bin/with-contenv sh

if [ -n "$HEALTHCHECK_ID" ]; then
	curl -sS -X POST -o /dev/null "$HEALTHCHECK_HOST/$HEALTHCHECK_ID/start"
fi

# If the sync fails we want to avoid triggering the health check.
set -e

/app/bin/dodo sync \
	--db /data/dodo.db \
	--access-token "$ACCESS_TOKEN" \
	--access-secret "$ACCESS_SECRET" \
	--api-key "$API_KEY" \
	--api-secret "$API_SECRET"

if [ -n "$HEALTHCHECK_ID" ]; then
	curl -sS -X POST -o /dev/null --fail "$HEALTHCHECK_HOST/$HEALTHCHECK_ID"
fi
