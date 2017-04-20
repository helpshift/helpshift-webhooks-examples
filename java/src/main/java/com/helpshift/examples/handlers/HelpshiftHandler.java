package com.helpshift.examples.handlers;


import com.helpshift.examples.Config;
import com.helpshift.examples.objects.WebhookData;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class HelpshiftHandler implements Runnable {

    final WebhookData webhookData;

    public enum Category {VIP, NEW};

    private static final Map<String, Category> USERMAP = new HashMap<>();

    static {
        /* In this sample, we are simulating the use case where you have a database of users, with
         * a predefined support level for each user. You can add tags to newly-created issues by looking
         * up the email of the user reporting the issue, then make an API call back to Helpshift to add
         * the tags on the issue.
         */
        USERMAP.put("user@example.org", Category.NEW);
        USERMAP.put("jdoe@example.org", Category.VIP);
        USERMAP.put("janedoe@example.org", Category.VIP);
    }

    public HelpshiftHandler(WebhookData webhookData) {
        this.webhookData = webhookData;
    }

    public void run() {

        if (webhookData.getEventType().equals("create_issue")) {
            final String userEmail = webhookData.getIssue().getAuthorEmail();
            if (USERMAP.containsKey(userEmail)) {
                final Category category = USERMAP.get(userEmail);

                /* The tags field is a JSON array of the tags to be added to the issue. The tags must already have
                 * been created in the Helpshift Dashboard before making this API call. For more details, refer
                 * to the Helpshift API documentation at https://apidocs.helpshift.com
                 */
                final String labelField = "[\"" + category.name().toLowerCase() + "\"]";
                final String url = Config.HS_API_ENDPOINT + webhookData.getIssue().getId();

                try {

                    System.out.println("Making an API call to " + url + " to set tags to " + labelField);

                    /* Helpshift API authentication is based on Basic HTTP Authentication, with the
                     * API key as username, and an empty string as password.
                     */
                    HttpResponse<String> response = Unirest.put(url)
                            .basicAuth(Config.HS_APIKEY, null)
                            .field("tags", labelField).asString();

                    if (response.getStatus() != HttpStatus.SC_OK) {
                        System.err.println("API request failed - ");
                        System.err.println(response.getBody());
                    }
                } catch (UnirestException e) {
                    System.err.println("Error making API request: " + e.getMessage());
                    e.printStackTrace();
                }

            }
        }
    }

}
