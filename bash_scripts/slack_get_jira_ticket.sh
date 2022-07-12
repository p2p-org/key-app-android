branch_name="$GITHUB_HEAD_REF"
cut_branch_name=$( echo ${branch_name##*/} )
IFS='-'
read -r -a array <<< "$cut_branch_name"
jira_ticket_message="https://p2pvalidator.atlassian.net/browse/${array[0]}-${array[1]}"

curl \
-F token="$1" \
-F channel="$2" \
-F text="Jira ticket - ${jira_ticket_message}" \
https://slack.com/api/chat.postMessage
