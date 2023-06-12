#!/bin/bash

apk_file=$3
apk_filename=$(basename "$apk_file")
version_name=$(echo "$apk_filename" | sed 's/^key-app-\(.*\)$/\1/')

slack_ktlint_message="Build $version_name is published!"

curl \
  -F token="$1" \
  -F channels="$2" \
  -F file=@"$apk_file" \
  -F text="${slack_ktlint_message}" \
  https://slack.com/api/files.upload