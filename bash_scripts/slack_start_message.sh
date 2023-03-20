#!/bin/bash

slack_ktlint_message="*The process of creating a new assembly is started (0%)*:
• *Upload to Firebase* -::athletic_shoe:
• *Upload to Slack* - :athletic_shoe:"
SLACK_SEND_MESSAGE_RESPONSE=$(curl \
    -F token="$1" \
    -F channel="$2"\
    -F text="$slack_ktlint_message" \
    https://slack.com/api/chat.postMessage
)
echo "$SLACK_SEND_MESSAGE_RESPONSE" > timestamp.json
echo $(sed -n 's|.*"ts":"\([^"]*\)".*|\1|p' timestamp.json ) > timestamp.txt