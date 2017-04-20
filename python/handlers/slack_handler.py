import requests
import json
import logging


def construct_create_issue_payload(issue, helpshift_dashboard_url):
    """
    Construct a payload which will be send to slack on issue creation.

    issue: Issue data as send in webhook payload.
    helpshift_dashboard_url: Helpshift dashboard URL.
    return: Payload to send slack alert on issue create event.
    """
    return {
        "text": "New Helpshift issue received #" + str(issue['id']),
        "attachments": [
            {
                "title": issue['title'],
                "title_link": helpshift_dashboard_url + str(issue['id']) + "/",
                "text": issue['messages'][0]['body']
            }
        ]
    }


def construct_update_issue_payload(issue, alert_text, helpshift_dashboard_url):
    """
    Construct a payload which will be send to slack on issue update.

    issue: Issue data as send in webhook payload.
    helpshift_dashboard_url: Helpshift dashboard URL.
    return: Payload to send slack alert on issue update event.
    """
    return {
        "text": "Helpshift issue updated #" + str(issue['id']),
        "attachments": [
            {
                "title": "Issue updated",
                "title_link": helpshift_dashboard_url + str(issue['id']) + "/",
                "text": alert_text
            }
        ]
    }


def send_alert(payload, webhook_url):
    """
    This will make an api call to slack to send alert.

    payload: Payload which will be send to slack.
    webhook_url: Webhook URL to post Slack alert.
    """
    response = requests.post(url=webhook_url,
                             data=json.dumps(payload),
                             headers={'content-type': "application/json"})
    if response.status_code == 200:
        logging.info("Success: Send alert to Slack.")
    else:
        logging.error("Error: Send alert to Slack, Status code:" + str(response.status_code))
        logging.error(response.json())


def create_issue_event(issue, slack_config, helpshift_config):
    """
    This function parses the issue data and send issue create alert to slack.

    issue: Issue data as send in webhook payload.
    slack_config: Slack config object containing Slack URL.
    helpshift_config: Helpshift config object containing Helpshift configurations values.
    """
    payload = construct_create_issue_payload(issue, helpshift_config['dashboard_url'])
    send_alert(payload, slack_config['webhook_url'])


def update_issue_event(issue, slack_config, helpshift_config):
    """
    This function parses the issue data and send issue update alert to slack.

    issue: Issue data as send in webhook payload.
    slack_config: Slack config object containing Slack URL.
    helpshift_config: Helpshift config object containing Helpshift configurations values.
    """
    if 'tags' in issue['updates']:
        alert_text = "Issue tags updated:" + str(issue['updates']['tags'])
    elif 'messages' in issue['updates']:
        alert_text = "New message added: " + issue['updates']['messages'][0]['body']
    elif 'state_data' in issue['updates']:
        alert_text = "Issue state changed to " + issue['updates']['state_data']['state']
    else:
        alert_text = "Issue updated: Unknown event"
    payload = construct_update_issue_payload(issue, alert_text, helpshift_config['dashboard_url'])
    send_alert(payload, slack_config['webhook_url'])
