#!/bin/bash

apk_file=$3
version_name=$(aapt dump badging "$apk_file" | awk -v FS="'" '/versionName=/{print $2}')

slack_ktlint_message="Build $version_name is published!"

curl \
  -F token="$1" \
  -F channels="$2" \
  -F file=@"$apk_file" \
  -F text="${slack_ktlint_message}" \
  https://slack.com/api/files.upload