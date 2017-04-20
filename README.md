Helpshift Webhook Sample Code
=============================

This project provides sample code which demonstrates how to write applications in Python and Java which can consume Helpshift webhooks and act on them.

We provide examples for the scenarios listed below.


## Setting Webhooks in Helpshift

Webhooks enable you to subscribe for changes (create issues and update issues) based on specific criteria.
When a change occurs, an HTTPS POST will be sent to the target URL.
For more details on how to setup webhook, refer to
<a href="https://support.helpshift.com/a/helpshift/?l=en&s=api-access&f=what-are-helpshift-webhooks" target="_blank">
What Are Helpshift Webhooks, And How Do I Set Them Up? </a>

#### Note

Helpshift webhooks only support posting to secure https endpoints.
For local testing the project includes sample payloads which can be used with
the sample application servers. Sample payloads can be found
<a href="/sample-payloads/" target="_blank">here</a>

To test with live Helpshift webhooks, you will need to setup a secure https
server which proxies the webhook requests to the application servers.
Configuring secure https server is out of the scope of this document but you can refer to
<a href="http://chase-seibert.github.io/blog/2011/12/21/nginx-ssl-reverse-proxy-tutorial.html" target="_blank">
nginx + SSL reverse proxy tutorial </a>

## Slack Integration

The Slack integration demonstrates how to post messages to a Slack channel whenever an issue is created in Helpshift,
and when an issue is updated. The sample code uses Slack's incoming webhooks feature to post messages to the channel
for new issues as well as issue status updates.

<a href="/docs/SLACK_SETUP.md" target="_blank"> Slack Setup Instructions </a>

## JIRA Integration

The JIRA integration demonstrates how to create JIRA tickets corresponding to new issues in Helpshift, and how to
keep the issue and the JIRA ticket in sync. A JIRA ticket is created for each new issue in Helpshift, and for each
issue update, the JIRA ticket is updated accordingly. This includes -
- status updates - including mapping Helpshift status updates to the matching JIRA statuses
- tag updates - mapped to labels in JIRA
- new messages - added as comments to the JIRA ticket

<a href="/docs/JIRA_SETUP.md" target="_blank"> JIRA Setup Instructions </a>

## Helpshift Integration

The Helpshift integration demonstrates how to consume webhooks to be notified whenever a new issue is created in
Helpshift, then use the Helpshift API to achieve following use cases:

* Update tags on newly created issue based on database lookup on the user's email (see
<a href="java/src/main/java/com/helpshift/examples/handlers/HelpshiftHandler.java" target="_blank"> HelpshiftHandler.java </a>).
* Update custom meta on newly created issue based on database lookup on the user's id (see
<a href="python/handlers/helpshift_handler.py" target="_blank"> helpshift_handler.py </a>).

<a href="https://apidocs.helpshift.com" target="_blank"> Helpshift API Documentation </a>

#### Helpshift Integration Flow
![Helpshift Integration Flow](screenshots/helpshift/helpshift_integration_flow.png)

## Code Samples

This project includes sample application servers written in Java and Python to consume webhooks and implement the above integrations.
Follow the below links for running the sample code.
* <a href="/java/README.md" target="_blank"> Java </a>
* <a href="/python/README.md" target="_blank"> Python </a>

## License

````
Copyright 2017, Helpshift, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````

## Third Party

### Third Party Libraries
| Library    | License    | Copyright/Creator    |
|---------   | :---------:| ------------------  |
|<a href="http://sparkjava.com/" target="_blank"> Spark Framework </a>| <a href="https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)" target="_blank"> Apache License 2.0 </a> | (c) 2010 Christoph Hochstrasser |
|<a href="http://flask.pocoo.org/" target="_blank"> Flask Framework </a>| <a href="http://flask.pocoo.org/docs/0.12/license/#flask-license" target="_blank">Flask License</a> | (c) 2010 Armin Ronacher |


###### Contact <a href="https://support.helpshift.com" target="_blank"> Helpshift Support </a> for any issues and more details.
