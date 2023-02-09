#!/bin/bash

commit_message=$(git log -1 --pretty=%B)
jira_ticket=$(echo "$commit_message" | grep -Eo "PWN-[0-9]+")
echo "CHECKING TICKET: $jira_ticket"