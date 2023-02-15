#!/bin/bash

# Checkout to develop branch first
git checkout develop

# Get the commit hash of the develop branch
develop_commit_hash=$(git rev-parse develop)

# Get the current head commit hash
current_commit_hash=$(git rev-parse HEAD)

# Run the gradle task with the fromCommit and toCommit properties set
./gradlew gitChangelogTask -PfromCommit=$develop_commit_hash -PtoCommit=$current_commit_hash