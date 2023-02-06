#!/bin/bash
git fetch

printf "$(git shortlog origin/$3..origin/$GITHUB_HEAD_REF)" > changelog.txt

curl \
-F token="$1" \
-F channels="$2" \
-F initial_comment="List of changes included in the build" \
-F file=@changelog.txt \
https://slack.com/api/files.upload

