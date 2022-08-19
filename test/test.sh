#!/usr/bin/env bash
set -euo pipefail

die() {
    echo "$*" 1>&2
    exit 1
}

kafkacat=kcat
if ! which $kafkacat >/dev/null 2>&1; then
    kafkacat=kafkacat
fi

for cmd in $kafkacat curl jq; do
    which $cmd >/dev/null 2>&1 || die "$cmd must be installed"
done

version=$(curl --silent --fail -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
            --data '{"schema": "{\"type\": \"string\"}"}' \
            http://${SCHEMA_REGISTRY}/subjects/test_topic-key/versions | jq .id)

[[ "$version" =~ ^[0-9]+$ ]] || die "schema id should be an integer"

response=$(curl --silent --write-out '%{http_code}' --output /dev/null \
            -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
            --data '{"schema": "{\"type\": \"int\"}"}' \
            http://${SCHEMA_REGISTRY}/subjects/test_topic-key/versions)

[ "$response" = "409" ] || die "schema should be incompatible"

$kafkacat -L -b $KAFKA || die "could not interrogate broker"
