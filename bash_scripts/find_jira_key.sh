#!/bin/bash

commit_message=$(git show -s --format=%B "$GITHUB_HEAD_REF")
jira_ticket=$(echo "$commit_message" | grep -Eo "PWN-[0-9]+")
echo "message $commit_message"
echo "$jira_ticket"