import requests
import json
import logging

"""
This is a lookup dictionary used to simulate the external database source.
In production, this should be replaced by actual database.
"""
USER_PROFILES = {"1": {"user_name": "User 1", "profile": "free"},
                 "2": {"user_name": "User 2", "profile": "VIP"},
                 "3": {"user_name": "User 3", "profile": "free"},
                 "4": {"user_name": "User 4", "profile": "pro"},
                 "5": {"user_name": "User 5", "profile": "free"}}


def fetch_user_profile(user_id):
    """
    This function lookup a dictionary given an user ID. In production, this should be replaced
    by querying external database.

    user_id: User ID using which external Database will be queried to retrieve user profile.
    return: Returns an user profile corresponding to the user ID, if not found returns a default profile type.
    """
    if user_id in USER_PROFILES:
        return USER_PROFILES[user_id]
    else:
        return {"profile": "free"}


def update_issue(issue_id, payload, api_endpoint, api_key):
    """
    This function updates the issue in Helpshift with the given payload.

    issue_id: Helpshift issue id.
    payload: Payload to update issue in Helpshift.
    api_endpoint: Helpshift API endpoint.
    api_key: Helpshift API key.
    """
    response = requests.put(url=api_endpoint + str(issue_id),
                            headers={'content-type': "application/x-www-form-urlencoded"},
                            auth=(api_key, ""),
                            data=payload)
    if response.status_code == 200:
        logging.info("Success: Updated issue in Helpshift")
    else:
        logging.error("Error: Unable to update issue in Helpshift")
        logging.error(response.json())


def update_issue_meta(issue, helpshift_config):
    """
    This function parses the webhook payload and extracts the user ID to read user profile information.
    After reading the user profile type, issue is updated in Helpshift using rest api. Only keys specified
    in meta during issue creation should be updated.

    issue: Issue object as send in webhook payload.
    helpshift_config: Helpshift config object containing Helpshift configurations values.
    """
    if 'meta' in issue and 'user_id' in issue['meta']:
        user_profile = fetch_user_profile(issue['meta']['user_id'])
        payload = {"meta": json.dumps({"user_profile": user_profile['profile']})}
        update_issue(issue['id'], payload, helpshift_config['api_endpoint'], helpshift_config['api_key'])
