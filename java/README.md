Java Code Samples
=================

The Java sample code provides a simple application server implemented using the <a href="http://sparkjava.com/" target="_blank"> Spark framework </a>.
The app server consumes incoming webhook events from Helpshift, and processes them to simulate the following work flows -

1. Update issue in Helpshift using Helpshift API on create issue event.
2. Send Slack alert on create issue and update issue event.
3. Create Jira ticket on issue create event and update Jira ticket on update issue event.

#### Requirements
1. <a href="http://www.oracle.com/technetwork/java/javase/downloads/index.html" target="_blank"> Java SE Development Kit 1.8 or higher </a>
2. <a href="https://maven.apache.org/" target="_blank"> Maven </a>

#### Usage
1. Update the <a href="conf/config.properties" target="_blank"> properties file </a> to replace the placeholder values with real values.
   Read <a href="../docs/SLACK_SETUP.md" target="_blank"> Slack Setup </a>,
   <a href="../docs/JIRA_SETUP.md" target="_blank"> JIRA Setup </a> and
   <a href="https://success.helpshift.com/a/success-center/?p=web&s=premium-features&f=managing-your-api-keys" target="_blank">
   Managing your API keys for api keys </a> for how to retrieve real values.

2. Run mvn package. This will generate a self-contained jar with all dependencies included.
    ````
    mvn package
    ````

3. Start the server.
   ````
   java -jar target/webhook-examples-1.0.0-jar-with-dependencies.jar
   ````

   This will start a test server listening on port 4567.

4. To test the server locally, we have provided some sample webhook events in the sample-payloads directory. You can
   directly feed these sample events to the server using curl. Note that since these are dummy payloads, the Helpshift
   handler will not work, and so should be commented out before running the server
   (see <a href="src/main/java/com/helpshift/examples/Server.java" target="_blank"> Server.java </a>).
   Sample payloads can be found <a href="../sample-payloads/" target="_blank"> here </a>
  ````
  cd ../sample-payloads
  curl -X POST --data @create_issue_events.json http://localhost:4567/webhook
  curl -X POST --data @update_status_event.json http://localhost:4567/webhook
  ````
