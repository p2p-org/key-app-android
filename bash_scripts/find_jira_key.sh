#!/bin/bash
git fetch

pr_title=$(echo "${{ github.event.pull_request.title }}")
jira_ticket=$(echo "$commit_message" | grep -Eo "PWN-[0-9]+")
echo "message $commit_message"
echo "$jira_ticket"