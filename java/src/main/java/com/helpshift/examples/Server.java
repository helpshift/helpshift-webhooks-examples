package com.helpshift.examples;

import com.helpshift.examples.handlers.HelpshiftHandler;
import com.helpshift.examples.handlers.JIRAHandler;
import com.helpshift.examples.handlers.SlackHandler;
import com.helpshift.examples.objects.WebhookData;

import static spark.Spark.*;


public class Server {

    public static void main(String[] args) {

        Config.check();

        /* We are using the Apache Spark framework to handle incoming POST requests. We just need to set up
         * a single handler for incoming POST requests. The body of the request will contain the webhook data
         * as a JSON map.
         */
        post("/webhook", (request, response) -> {

            final String body = request.body();
            System.out.println("POST to webhook - " + body);

            try {
                final WebhookData webhookData = new WebhookData(body);

                /* In this sample code, we take the webhook data and simulate three different use cases -
                 * 1. based on the email of the user reporting the issue, update tags on the Helpshift issue
                 * 2. Post an update to Slack on a particular channel with details of new issues and issue updates
                 * 3. Create a Task in JIRA, and update it corresponding to all updates to the issue in Helpshift
                 */
                new Thread(new HelpshiftHandler(webhookData)).start();
                new Thread(new SlackHandler(webhookData)).start();
                new Thread(new JIRAHandler(webhookData)).start();


            } catch (Exception e) {
                System.err.println("Error processing webhook objects: " + e.getMessage());
                e.printStackTrace();
                return "FAIL";
            }

            return "OK";
        });


    }

}
