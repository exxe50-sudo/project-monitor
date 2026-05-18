#!/bin/sh
set -e

# Default values if not set
GATEWAY_HOST="${GATEWAY_HOST:-monitor-gateway}"
GATEWAY_PORT="${GATEWAY_PORT:-8080}"

echo "Configuring nginx with GATEWAY_HOST=$GATEWAY_HOST GATEWAY_PORT=$GATEWAY_PORT"

envsubst '${GATEWAY_HOST} ${GATEWAY_PORT}' \
    < /etc/nginx/conf.d/default.conf.template \
    > /etc/nginx/conf.d/default.conf

exec nginx -g "daemon off;"
