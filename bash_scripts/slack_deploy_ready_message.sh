#!/bin/bash

slack_ktlint_message="Запущен процесс новой сборки (50%):
1 Ktlint :white_check_mark:
3 Update gradle version :white_check_mark:
4 Upload to Firebase :man-running:
5 Upload to Slack :hourglass:
Сборка почти готова, ожидаем апрува от разработчика чтоб доставить вам новый билд ))"

curl \
-F token="${SLACK_BOT_TOKEN}" \
-F channel="${SLACK_CHANNEL_ID}" \
-F ts="$1" \
-F text="${slack_ktlint_message}" \
https://slack.com/api/chat.update
