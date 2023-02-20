#!/bin/bash

curl \
-F token="$1" \
-F channels="$2" \
-F initial_comment="List of changes included in the build" \
-F file=@$3 \
https://slack.com/api/files.upload

