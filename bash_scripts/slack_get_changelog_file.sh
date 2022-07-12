#!/bin/bash
git fetch
printf "$(git shortlog origin/develop..origin/$GITHUB_HEAD_REF)" > changelog.txt

curl \
-F token="$1" \
-F channels="$2" \
-F initial_comment="Список изменений включенных в сборку" \
-F file=@changelog.txt \
https://slack.com/api/files.upload

