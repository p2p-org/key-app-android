branch_name="$GITHUB_HEAD_REF"
cut_branch_name=$( echo ${branch_name##*/} )
IFS='-'
read -r -a array <<< "$cut_branch_name"
jira_ticket_message="https://p2pvalidator.atlassian.net/browse/${array[0]}-${array[1]}"
curl \
    -F token=${{ SLACK_BOT_TOKEN }} \
    -F channel=${{ SLACK_CHANNEL_ID }} \
    -F text="Ссылка на задачу в Jira - ${jira_ticket_message}" \
    https://slack.com/api/chat.postMessage
