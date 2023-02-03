#!/bin/bash
git fetch

printf "$(git log -1 --pretty=format:'%b' develop)" > changelog.txt

curl \
-F token="$1" \
-F channels="$2" \
-F initial_comment="List of changes included in the build" \
-F file=@changelog.txt \
https://slack.com/api/files.upload

