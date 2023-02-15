#!/bin/bash

# Set the branch names
start_branch=develop
end_branch=$(git rev-parse --abbrev-ref HEAD)

# Fetch the latest changes
git fetch origin

# Check if the remote branch exists
remote_branch_exists=$(git show-ref --quiet refs/remotes/origin/$end_branch)

# Get the range of commits between the two branches
if [ $remote_branch_exists ]; then
  commits=$(git log --pretty=format:"%h - %s (%b) - %an" ${start_branch}..origin/$end_branch)
else
  commits=""
fi

# Check if commits are empty
if [ -z "$commits" ]; then
  echo "No commits found, skipping"
else
  changelog_message="*CHANGELOG*
$commits"

echo "$commits"
#  curl \
#    -F token="$1" \
#    -F channel="$2"\
#    -F text="$changelog_message" \
#    https://slack.com/api/chat.postMessage
fi