#!/bin/bash
slack_ktlint_message="Запущен процесс новой сборки (75%):
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
