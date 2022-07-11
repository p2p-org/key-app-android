#!/bin/bash
slack_ktlint_message="Запущен процесс новой сборки (0%):
1 Ktlint :man-running:
2 Unit Tests :man-running:
4 Upload to Firebase :hourglass:
5 Upload to Slack :hourglass: "

echo $SLACK_BOT_TOKEN
echo $SLACK_CHANNEL_ID
SLACK_SEND_MESSAGE_RESPONSE=$(curl \
    -F token="${SLACK_BOT_TOKEN}" \
    -F channel="${SLACK_CHANNEL_ID}" \
    -F text="$slack_ktlint_message" \
    https://slack.com/api/chat.postMessage
    )
echo "$SLACK_SEND_MESSAGE_RESPONSE" > timestamp.json
echo $(sed -n 's|.*"ts":"\([^"]*\)".*|\1|p' timestamp.json ) > timestamp.txt
