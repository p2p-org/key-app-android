#!/bin/bash

# Get the latest commit hash was saved in last_commit_hash.txt
last_commit_hash=$(cat last_commit_hash.txt)

# Get the current head commit hash
current_commit_hash=$(git rev-parse "$GITHUB_HEAD_REF")

# Run the gradle task with the fromCommit and toCommit properties set
./gradlew gitChangelogTask -PfromCommit="$last_commit_hash" -PtoCommit="$current_commit_hash"