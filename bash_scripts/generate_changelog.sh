#!/bin/bash

# Get the latest commit hash was saved in last_commit_hash.txt
last_commit_hash=$(cat last_commit_hash.txt)

# Get the current head commit hash
current_commit_hash="$(git shortlog origin/$3..origin/$GITHUB_HEAD_REF)"
echo "current commit: $current_commit_hash"

printf "$(git log "$last_commit_hash".."$current_commit_hash" --no-merges --pretty=format:\"%h - %s - %an\")" > changelog.txt

# Run the gradle task with the fromCommit and toCommit properties set
./gradlew gitChangelogTask -PfromCommit="$last_commit_hash" -PtoCommit="$current_commit_hash"