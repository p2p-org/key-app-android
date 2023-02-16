#!/bin/bash

# Get the latest commit hash was saved in last_commit_hash.txt
#last_commit_hash=$(cat last_commit_hash.txt)
last_commit_hash="$(git rev-parse origin/develop)"
last_commit_hash2=$(git rev-parse origin/develop)
echo "develop hash: $last_commit_hash"
echo "develop hash2: $last_commit_hash2"

# Get the current head commit hash
current_commit_hash="$(git rev-parse HEAD)"

# Run the git log command and save the result in CHANGELOG.txt
printf "$(git log "$last_commit_hash".."$current_commit_hash" --no-merges --pretty=format:\"%h - %s - %an\")" > changelog.txt
echo "resultt: $(git log "$last_commit_hash".."$current_commit_hash" --no-merges --pretty=format:\"%h - %s - %an\")"