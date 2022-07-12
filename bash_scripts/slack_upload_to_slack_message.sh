#!/bin/bash

branch_name="$GITHUB_HEAD_REF"
cut_branch_name=$( echo ${branch_name##*/} )
IFS='-'
read -r -a array <<< "$cut_branch_name"
jira_ticket_message="https://p2pvalidator.atlassian.net/browse/${array[0]}-${array[1]}"

slack_ktlint_message="Еhe process of creating a new assembly is started (75%):
Jira Ticket - $jira_ticket_message
1 Ktlint :white_check_mark:
2 Unit Tests :white_check_mark:
4 Upload to Firebase :white_check_mark:
5 Upload to Slack :man-running:"

curl \
-F token="$1" \
-F channel="$2" \
-F ts="$3" \
-F text="${slack_ktlint_message}" \
https://slack.com/api/chat.update
