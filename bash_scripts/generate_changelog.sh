#!/bin/bash

# Get the commit hash of the develop branch
develop_commit_hash=$(git rev-parse origin/develop)

# Get the current head commit hash
current_commit_hash=$(git rev-parse origin/$GITHUB_HEAD_REF)

# Run the gradle task with the fromCommit and toCommit properties set
git log "$develop_commit_hash".."$current_commit_hash" --no-merges --pretty=format:\"%h - %s - %an\" > CHANGELOG.txt