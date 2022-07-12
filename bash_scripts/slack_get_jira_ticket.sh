branch_name="$GITHUB_HEAD_REF"
cut_branch_name=$( echo ${branch_name##*/} )
IFS='-'
read -r -a array <<< "$cut_branch_name"
jira_ticket_name="${array[0]}-${array[1]}"
if [ -z "$jira_ticket_name" ]; then
  jira_ticket_name="$GITHUB_REF"
fi
jira_ticket_message="https://p2pvalidator.atlassian.net/browse/$jira_ticket_name"

curl \
-F token="$1" \
-F channel="$2" \
-F text="Jira ticket - ${jira_ticket_message}" \
https://slack.com/api/chat.postMessage
