#!/bin/bash

# this script should run on develop branch, i.e. when PR is merged
commit_message=$(git log -1 --pretty=format:'%b' develop)
jira_ticket=$(echo "$commit_message" | grep -Eo "PWN-[0-9]+")
echo "$jira_ticket"