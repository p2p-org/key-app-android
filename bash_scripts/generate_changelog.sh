#!/bin/bash

# Get the latest commit hash was saved in last_commit_hash.txt
develop_commit_hash=$(cat last_commit_hash.txt)

# Get the current head commit hash
current_commit_hash=$(git rev-parse origin/$GITHUB_HEAD_REF)

# Run the gradle task with the fromCommit and toCommit properties set
./gradlew gitChangelogTask -PfromCommit=$develop_commit_hash -PtoCommit=$current_commit_hash