#!/bin/bash

# Set the branch names
start_branch=develop
end_branch=$(git rev-parse --abbrev-ref HEAD)

# Fetch the latest changes
git fetch origin

# Get the range of commits between the two branches
commits=$(git log --pretty=format:"%h - %s (%b) - %an" ${start_branch}..${end_branch})

changelog_message="*CHANGELOG*
$commits"

curl \
    -F token="$1" \
    -F channel="$2"\
    -F text="$changelog_message" \
    https://slack.com/api/chat.postMessage