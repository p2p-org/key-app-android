#!/bin/bash

commit_message=$(git cherry -v develop "$GITHUB_HEAD_REF")
jira_ticket=$(echo "$commit_message" | grep -Eo "PWN-[0-9]+")
echo "$jira_ticket"