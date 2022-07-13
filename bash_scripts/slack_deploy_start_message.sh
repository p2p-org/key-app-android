#!/bin/bash

branch_name="$GITHUB_HEAD_REF"
cut_branch_name=$( echo ${branch_name##*/} )
IFS='-'
read -r -a array <<< "$cut_branch_name"
jira_ticket_message="https://p2pvalidator.atlassian.net/browse/${array[0]}-${array[1]}"

slack_ktlint_message="*The process of creating a new assembly is started (0%)*:
• *Jira Ticket* - $jira_ticket_message
• *Upload to Firebase* -:white_check_mark:
• *Upload to Slack* - :white_check_mark:"
SLACK_SEND_MESSAGE_RESPONSE=$(curl \
    -F token="$1" \
    -F channel="$2"\
    -F text="$slack_ktlint_message" \
    https://slack.com/api/chat.postMessage
)
echo "$SLACK_SEND_MESSAGE_RESPONSE" > timestamp.json
echo $(sed -n 's|.*"ts":"\([^"]*\)".*|\1|p' timestamp.json ) > timestamp.txt
