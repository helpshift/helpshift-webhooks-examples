from flask import Flask, request, jsonify
from handlers import helpshift_handler, slack_handler, jira_handler
import logging
import json

app = Flask(__name__)
CONFIGS = {}


@app.route("/create_issue", methods=["POST"])
def create_issue_events():
    """
    This endpoint is used to capture create_issue events send by Helpshift webhooks.
    On issue creation event, following workflows are simulated:
    1. Slack notification is sent.
    2. Jira issue is created.
    3. Issue is updated in Helpshift if meta contains user_id value.
    """
    logging.info("Got create issue event.")
    data = request.json
    helpshift_handler.update_issue_meta(data['data'], CONFIGS['helpshift'])
    slack_handler.create_issue_event(data['data'], CONFIGS['slack'], CONFIGS['helpshift'])
    jira_handler.create_jira_ticket(data['data'], CONFIGS['jira'], CONFIGS['helpshift'])
    return jsonify(success=True), 200


@app.route("/update_issue", methods=["POST"])
def update_issue_events():
    """
    This endpoint is used to capture update_issue events send by Helpshift webhooks.
    On issue creation event, following workflows are simulated:
    1. Slack notification is sent.
    2. Jira issue is updated..
    """
    logging.info("Got update issue event.")
    data = request.json
    slack_handler.update_issue_event(data['data'], CONFIGS['slack'], CONFIGS['helpshift'])
    jira_handler.update_jira_ticket(data['data'], CONFIGS['jira'])
    return jsonify(success=True), 200


@app.errorhandler(500)
def internal_error(error):
    logging.error("Internal Server Error: " + str(error))
    return jsonify(success=False, error=str(error)), 500


@app.errorhandler(404)
def not_found(error):
    logging.error("Route not found: " + str(error))
    return jsonify(success=False, error=str(error)), 404


def load_configs(file_path):
    with open(file_path) as config_file:
        configs = json.load(config_file)
    return configs


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    logging.info("Started App")
    CONFIGS = load_configs("config.json")
    app.run(debug=True, host=CONFIGS['app']['host'], port=CONFIGS['app']['port'])
