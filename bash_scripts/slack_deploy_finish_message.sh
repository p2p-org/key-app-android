#!/bin/bash

slack_ktlint_message="*The process of creating a new assembly is finished (100%)*:
• *Upload to Firebase* -:white_check_mark:
• *Upload to Slack* - :white_check_mark:"
curl \
-F token="$1" \
-F channel="$2" \
-F ts="$3" \
-F text="${slack_ktlint_message}" \
https://slack.com/api/chat.update
