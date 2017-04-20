Python Code Samples
===================

Sample code is built using <a href="http://flask.pocoo.org/" target="_blank"> Flask framework </a> to expose APIs to consume webhook events.

The following work flows are simulated using these code samples:

1. Update issue in Helpshift using Helpshift API on create issue event.
2. Send Slack alert on create issue and update issue event.
3. Create Jira ticket on issue create event and update Jira ticket on update issue event.


### Requirements <hr>

This sample code is Python 2 and Python 3 compatible.


### Usage <hr>

1. Replace all placeholder values with real values in <a href="config.json" target="_blank"> config file </a>.
   Read <a href="../docs/SLACK_SETUP.md" target="_blank"> Slack Setup </a>,
   <a href="../docs/JIRA_SETUP.md" target="_blank"> JIRA Setup </a> and
   <a href="https://success.helpshift.com/a/success-center/?p=web&s=premium-features&f=managing-your-api-keys" target="_blank">
   Managing your API keys for api keys </a> for how to retrieve real values.

2. Run setup.py file with install parameter. This will install the service dependencies.

    ````
    python setup.py install
    ````

3. Start flask server.

    ````
    python app.py
    ````
    This will start a test server listening on port 4567.

4. To test the server locally, we have provided some sample webhook events in the sample-payloads directory. You can
   directly feed these sample events to the server using curl. Note that since these are dummy payloads, the Helpshift
   handler will not work, and so should be commented out before running the server
   (see <a href="app.py" target="_blank">app.py</a>). Sample payloads can be found
   <a href="../sample-payloads/" target="_blank"> here </a>
  ````
  cd ../sample-payloads
  curl -X POST --data @create_issue_events.json http://localhost:4567/create_issue
  curl -X POST --data @update_status_event.json http://localhost:4567/update_issue
  ````
