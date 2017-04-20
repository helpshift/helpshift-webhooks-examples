import requests
import json
import logging


"""
This mapping dictionary is maintained for JIRA ticket updates. It contains an Helpshift issue id
mapped to JIRA ticket.
"""
JIRA_TICKETS_ID = {}

# These are status value key, 21 maps to In Progess and 31 to Done.
STATUS_IN_PROGRESS_ID = "21"
STATUS_DONE_ID = "31"


def construct_create_issue_payload(issue, project_key, hs_dashboard_url, hs_ticket_url_key, user_name_key):
    """
    Construct a payload which will be send to JIRA on issue creation.

    issue: Issue data as send in webhook payload.
    project_key: JIRA project key.
    hs_dashboard_url: Helpshift dashboard URL.
    hs_ticket_url_key: Custom field key defined to save Helpshift dashboard issue URL.
    user_name_key: Custom filed key defined to save issue author.
    return: Payload to create issue in JIRA on issue creation event.
    """
    return {
        "fields": {
            "project": {
                "key": project_key
            },
            "summary": issue['title'],
            "description": issue['messages'][0]['body'],
            "issuetype": {
                "name": "Task"
            },
            hs_ticket_url_key: hs_dashboard_url + str(issue['id']) + "/",
            user_name_key: issue['author_name'],
            "labels": issue['tags'] if 'tags' in issue else []
        }
    }


def make_api_request(payload, endpoint, username, password, method="POST"):
    """
    This function make's a JIRA api call given a payload, method type, url.

    payload: Payload to send while making JIRA api call.
    endpoint: JIRA API endpoint.
    username: JIRA username.
    password: JIRA password.
    method: HTTP request verb.
    return: API call response
    """
    return requests.request(method=method,
                            url=endpoint,
                            auth=(username, password),
                            data=json.dumps(payload),
                            headers={'content-type': "application/json"})


def create_jira_ticket(issue, jira_config, helpshift_config):
    """
    This function parses the issue and creates an issue in JIRA.
    It also maintains the Helpshift issue id -> JIRA ticket id. This mapping
    will be later used for updating tickets in JIRA.

    issue: Issue data as send in webhook payload.
    helpshift_config: Helpshift config object containing Helpshift configurations values.
    jira_config: JIRA config object containing JIRA configurations values.
    """
    payload = construct_create_issue_payload(issue,
                                             jira_config['project_key'],
                                             helpshift_config['dashboard_url'],
                                             jira_config['helpshift_ticket_url_key'],
                                             jira_config['username_key'])
    response = make_api_request(payload,
                                jira_config['endpoint'],
                                jira_config['username'],
                                jira_config['password'])
    if response.status_code == 201:
        JIRA_TICKETS_ID[issue['id']] = response.json()['id']
        logging.info("Success: Jira issue creation")
    else:
        logging.error("Error: Jira issue creation, Status code: " + str(response.status_code))
        logging.error(response.json())


def update_issue_tags(issue_id, tags, project_key, username, password, endpoint):
    """
    This function updates the JIRA ticket labels on issue tags update in Helpshift.

    issue_id: Issue id in Helpshift.
    tags: Updated tags list.
    project_key: JIRA project key.
    username: JIRA username.
    password: JIRA password.
    endpoint: JIRA API endpoint.
    """
    payload = {
        "fields": {
            "project": {
                "key": project_key
            },
            "labels": tags
        }
    }
    response = make_api_request(payload,
                                endpoint + JIRA_TICKETS_ID[issue_id],
                                username,
                                password,
                                "PUT")
    if response.status_code == 204:
        logging.info("Updated issue tags")
    else:
        logging.info("Error: Issue tags updation: " + str(response.status_code))
        logging.info(response.json())


def add_message(issue_id, message, username, password, endpoint):
    """
    This function add's a comment to JIRA ticket on message addition to issue in Helpshift.

    issue_id: Issue id in Helpshift.
    message: Message body of new message added in Helpshift.
    username: JIRA username.
    password: JIRA password.
    endpoint: JIRA API endpoint.
    """
    payload = {
        "id": JIRA_TICKETS_ID[issue_id],
        "body": message
    }
    response = make_api_request(payload,
                                endpoint + JIRA_TICKETS_ID[issue_id] + "/comment",
                                username,
                                password)
    if response.status_code == 201:
        logging.info("Add issue message")
    else:
        logging.info("Error: Issue message addition: " + str(response.status_code))
        logging.info(response.json())


def update_issue_status(issue_id, state, username, password, endpoint):
    """
    This function updates the JIRA ticket status on status update in Helpshift.

    issue_id: Issue id in Helpshift.
    state: State ids corresponding to issue state in Helpshift.
    username: JIRA username.
    password: JIRA password.
    url: JIRA API endpoint.
    """
    payload = {
        "transition": STATUS_DONE_ID if state in ["resolved", "rejected"] else STATUS_IN_PROGRESS_ID
    }
    response = make_api_request(payload,
                                endpoint + JIRA_TICKETS_ID[issue_id] + "/transitions",
                                username,
                                password)
    if response.status_code == 204:
        logging.info("Update issue status")
    else:
        logging.info("Error: Updating issue status: " + str(response.status_code))
        logging.info(response.json())


def update_jira_ticket(issue, jira_config):
    """
    This function parses the issue and updates an issue in JIRA.

    Following issue updates are handled:
    1. Issue tags updates. This will update JIRA ticket labels.
    2. New message added. New comment will be added to JIRA for each message in Helpshift.
    3. Issue status update. JIRA ticket status will be updated based on status change in Helpshift.

    issue: Issue data as send in webhook payload.
    jira_config: JIRA config object containing JIRA configurations values.
    """

    if issue['id'] not in JIRA_TICKETS_ID:
        logging.info("Issue id -> Jira ticket mapping not found.")
        return

    if 'tags' in issue['updates']:
        update_issue_tags(issue['id'],
                          issue['updates']['tags'],
                          jira_config['project_key'],
                          jira_config['username'],
                          jira_config['password'],
                          jira_config['endpoint'])
    elif 'messages' in issue['updates']:
        add_message(issue['id'],
                    issue['updates']['messages'][0]['body'],
                    jira_config['username'],
                    jira_config['password'],
                    jira_config['endpoint'])
    elif 'state_data' in issue['updates']:
        update_issue_status(issue['id'],
                            issue['updates']['state_data']['state'],
                            jira_config['username'],
                            jira_config['password'],
                            jira_config['endpoint'])
    else:
        logging.info("Unknown Event.")
        return
