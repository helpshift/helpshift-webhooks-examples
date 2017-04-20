package com.helpshift.examples.handlers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpshift.examples.Config;
import com.helpshift.examples.objects.Issue;
import com.helpshift.examples.objects.IssueUpdate;
import com.helpshift.examples.objects.WebhookData;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.http.HttpStatus;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlackHandler implements Runnable {

    final WebhookData webhookData;

    public SlackHandler(WebhookData webhookData) {
        this.webhookData = webhookData;
    }

    /* We are generating the simplest type of Slack message here, a plaintext message.
     * To see examples of other types of messages, see the Python sample code. For more details,
     * refer to https://api.slack.com/incoming-webhooks
     */
    private String generatePayload(String message) throws Exception {
        final Map<String, String> data = new HashMap<>();
        data.put("text", message);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(data);
    }

    private void postSlackMessage(String message) {
        try {
            String payload = generatePayload(message);

            System.out.println("Making an API call to " + Config.WEBHOOK_URL + " with payload\n\t" + payload);

            HttpResponse<String> response = Unirest.post(Config.WEBHOOK_URL).body(payload).asString();
            if (response.getStatus() != HttpStatus.SC_OK) {
                System.err.println("There was a problem POSTing to Slack - " + response.getBody());
                return;
            }

        } catch (Exception e) {
            System.err.println("Error generating JSON to send to Slack - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            if (webhookData.getEventType().equals("create_issue")) {
                Issue issue = webhookData.getIssue();

                /* This message can be customized to display more information about the issue, or
                 * to customize its appearance in Slack. Refer to
                 * https://api.slack.com/incoming-webhooks#advanced_message_formatting
                 */
                final String message2 = "New Issue #" + issue.getId() + ": " + issue.getTitle() +
                        ", reported by " + issue.getAuthorName() + " (" + issue.getAuthorEmail() + ")";
                postSlackMessage(message2);

            } else if (webhookData.getEventType().equals("update_issue")) {
                IssueUpdate update = webhookData.getIssue().getIssueUpdate();
                if (update != null) {
                    final String message2;
                    switch (update.getType()) {
                        case STATE_CHANGED:
                            if (update.getState().equals("new")) {
                                // don't post messages to Slack for this update
                                return;
                            }

                            message2 = "Issue #" + webhookData.getIssue().getId() +
                                    " was updated, the state changed to " + update.getState() +
                                    " at " + update.getChangedAt().atZone(ZoneId.of("UTC"));
                            postSlackMessage(message2);
                            break;
                        case TAGS_CHANGED:
                            message2 = "Issue #" + webhookData.getIssue().getId() +
                                    " was updated, the current tags are " + update.getTagsAsString();
                            postSlackMessage(message2);
                            break;
                        default:
                            // doing nothing for now
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error while handling webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
