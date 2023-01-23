#!/bin/bash
git fetch
# since we are building the app after the pull request is merged in develop branch
# we are getting the develop's last commit description
printf "$(git log -1 --pretty=format:'%b' develop)" > changelog.txt

curl \
-F token="$1" \
-F channels="$2" \
-F initial_comment="List of changes included in the build" \
-F file=@changelog.txt \
https://slack.com/api/files.upload

