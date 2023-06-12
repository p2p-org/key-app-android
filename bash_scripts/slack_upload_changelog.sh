#!/bin/bash

curl \
-F token="$1" \
-F channels="$2" \
-F file=@$3 \
https://slack.com/api/files.upload