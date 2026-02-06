#!/usr/bin/env bash
set -euo pipefail

# Quick smoke test for the Job Stories Tracker backend
# Usage:
#  ./scripts/test-api.sh                # uses http://localhost:8080 and no auth (dev profile)
#  BASE_URL=http://localhost:8080 ./scripts/test-api.sh
#  AUTH=jobstories:jobstories ./scripts/test-api.sh  # use Basic auth (if not running dev profile)

BASE_URL=${BASE_URL:-http://localhost:8080}
AUTH=${AUTH:-}

if ! command -v jq >/dev/null 2>&1; then
  echo "ERROR: jq is required for this script. Install with: brew install jq (macOS) or apt install jq"
  exit 1
fi

auth_args=()
if [[ -n "$AUTH" ]]; then
  auth_args=( -u "$AUTH" )
fi

echo "Base URL: $BASE_URL"
if [[ -n "$AUTH" ]]; then
  echo "Using Basic auth from AUTH env var"
else
  echo "No auth (dev mode expected)"
fi

# 1) Create a story
echo "\n==> Creating a story"
created_json=$(curl -s ${auth_args[@]} -H "Content-Type: application/json" \
  -d '{"title":"Smoke Test","situation":"CTX","task":"TASK","action":"ACT","result":"RES","tags":"smoke"}' \
  "$BASE_URL/api/stories")

echo "Created JSON:"
echo "$created_json" | jq .

id=$(echo "$created_json" | jq -r '.id')
if [[ "$id" == "null" || -z "$id" ]]; then
  echo "Failed to get id from create response. Exiting."
  exit 1
fi

# 2) Get by id
echo "\n==> GET by id: $id"
curl -s ${auth_args[@]} "$BASE_URL/api/stories/$id" | jq .

# 3) List stories
echo "\n==> List stories"
curl -s ${auth_args[@]} "$BASE_URL/api/stories" | jq .

# 4) Negative test: non-existent id
echo "\n==> GET non-existent id (expect 404)"
status=$(curl -s -o /dev/null -w "%{http_code}" ${auth_args[@]} "$BASE_URL/api/stories/00000000-0000-0000-0000-000000000000")
echo "HTTP status: $status"

echo "\nSmoke tests complete."
