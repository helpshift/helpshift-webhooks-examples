package com.helpshift.examples.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.helpshift.examples.Config;
import com.helpshift.examples.objects.IssueUpdate;
import com.helpshift.examples.objects.WebhookData;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import javafx.beans.binding.ObjectBinding;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JIRAHandler implements Runnable {

    final WebhookData webhookData;

    /* These two values are fixed for all JIRA installations */
    private static final String INPROGRESS_ID = "21";
    private static final String DONE_ID = "31";

    /* For each JIRA created to track a Helpshift issue, we save the JIRA ID against the Helpshift Issue ID
     * in this map. Each webhook payload will always contain the Helpshift Issue ID.
     */
    private static Map<Integer, String> issueIdMap = new HashMap<>();

    public JIRAHandler(WebhookData webhookData) {
        this.webhookData = webhookData;
    }

    private String generateCreatePayload() throws Exception {

        /* Here, we create the payload or request body for the Create JIRA API request. The JIRA REST API expects
         * a JSON map containing the issue fields. To see the exact format and the valid fields which can be set
         * when creating a JIRA, see https://docs.atlassian.com/jira/REST/cloud/#api/2/issue-createIssue .
         */
        Map<String, Object> fields = new HashMap<>();

        fields.put("summary", webhookData.getIssue().getTitle());
        if (webhookData.getIssue().hasMessages())
            fields.put("description", webhookData.getIssue().getFirstMessage().getBody());

        // Comment out these next two lines if you don't want to use custom fields.
        fields.put(Config.JIRA_ENDUSER_FIELDNAME, webhookData.getIssue().getAuthorName());
        fields.put(Config.JIRA_HSTICKET_FIELDNAME, "http://" + webhookData.getDomain() + ".helpshift.mobi/admin/issue/"
                + webhookData.getIssue().getId() + "/");

        Map<String, Object> project = new HashMap<>();
        project.put("key", Config.JIRA_PROJECT_KEY);
        fields.put("project", project);

        Map<String, Object> issueType = new HashMap<>();
        issueType.put("name", "Task");
        fields.put("issuetype", issueType);


        Map<String, Object> payload = new HashMap<>();
        payload.put("fields", fields);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(payload);

    }

    private String generateEditPayload() throws Exception {


        /* Here, we create the payload or request body for the Edit JIRA API request. The JIRA REST API expects
         * a JSON map containing the issue fields which are to be updated. To see the exact format and the valid
         * fields which can be set, see https://docs.atlassian.com/jira/REST/cloud/#api/2/issue-editIssue .
         */
        IssueUpdate update = webhookData.getIssue().getIssueUpdate();

        /* For status updates, we update the status of the task in JIRA */
        if (update.getType() == IssueUpdate.IssueUpdateType.STATE_CHANGED) {

            /* Here we map the issue state from the values in Helpshift to the expected status types in JIRA */
            final String state;
            switch (update.getState()) {
                case "agent_replied":
                case "new_for_agent":
                    state = INPROGRESS_ID;
                    break;
                case "resolved":
                case "rejected":
                    state = DONE_ID;
                    break;
                default:
                    throw new Exception("Unknown update state: " + update.getState() + "!");
            }

            Map<String, Object> transition = new HashMap<>();
            transition.put("id", state);

            Map<String, Object> payload = new HashMap<>();
            payload.put("transition", transition);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(payload);
        } else if (update.getType() == IssueUpdate.IssueUpdateType.TAGS_CHANGED) {
            /* For changes in issue tags, we update the labels for the task in JIRA */
            Map<String, Object> fields = new HashMap<>();
            fields.put("labels", webhookData.getIssue().getIssueUpdate().getTags());

            Map<String, Object> payload = new HashMap<>();
            payload.put("fields", fields);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(payload);
        } else {
            throw new Exception("Unknown update type!");
        }
    }

    private String generateCommentPayload() throws Exception {

        Map<String, Object> payload = new HashMap<>();

        /* Any messages added to the issue in Helpshift get added as comments on the task in JIRA */
        String body = webhookData.getIssue().getFirstMessage().getBody() +
                "\n - comment by " + webhookData.getIssue().getFirstMessage().getAuthor().getName();
        payload.put("body", body);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(payload);
    }

    private void createJIRA() {

        try {
            String payload = generateCreatePayload();

            System.out.println("Making an API call to " + Config.JIRA_API_ENDPOINT + " with payload\n\t" + payload);

            /* Note that we are using the JIRA REST API with Basic authentication (i.e. using username/password).
             * JIRA also supports Oauth, which is more secure, but that is out of the scope of these code samples.
             */
            HttpResponse<String> response = Unirest.post(Config.JIRA_API_ENDPOINT)
                    .basicAuth(Config.JIRA_USERNAME, Config.JIRA_PASSWORD)
                    .header("Content-Type", "application/json")
                    .body(payload).asString();

            if (response.getStatus() != HttpStatus.SC_CREATED) {
                System.err.println("There was a problem POSTing to JIRA - " + response.getBody());
                return;
            }

            try {
                /* We need to save the JIRA ID of the newly created JIRA ticket, so we can make further API calls
                 * to keep the ticket synced with the Helpshift issue. Here we are just saving it in an im-memory
                 * HashMap, a real system would user a more reliable storage, like a database.
                 */
                final ObjectMapper mapper = new ObjectMapper();
                final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
                final Map<String, Object> data = mapper.readValue(response.getBody(), type);

                issueIdMap.put(webhookData.getIssue().getId(), (String) data.get("key"));
            } catch (IOException e) {
                System.err.println("Error processing response - " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("Error creating issue in JIRA - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editJIRA() {

        /* If for any reason we don't have the JIRA ID corresponding to this Helpshift issue, we can't
          * make the required API calls. In a real system, there would be a more robust system for saving
          * this mapping.
          */
        if (!issueIdMap.containsKey(webhookData.getIssue().getId())) {
            System.err.println("Skipping JIRA update because we don't have the JIRA ID");
            return;
        }

        try {
            String payload = generateEditPayload();
            if (payload == null)
                return;

            final HttpRequestWithBody request;

            /* Based on whether we are updating the issue state, or updating the labels, the HTTP
             * request URL and method are different.
             */
            String url = Config.JIRA_API_ENDPOINT + issueIdMap.get(webhookData.getIssue().getId());
            if (webhookData.getIssue().getIssueUpdate().getType() == IssueUpdate.IssueUpdateType.STATE_CHANGED) {
                url = url + "/transitions";
                request = Unirest.post(url);
            } else {
                request = Unirest.put(url);
            }

            System.out.println("Making an API call to " + url + " with payload\n\t" + payload);

            HttpResponse<String> response = request.basicAuth(Config.JIRA_USERNAME, Config.JIRA_PASSWORD)
                    .header("Content-Type", "application/json")
                    .body(payload).asString();
            if (response.getStatus() != HttpStatus.SC_NO_CONTENT) {
                System.err.println("There was a problem " + request.getHttpMethod() + "ing to JIRA - ");
                System.out.println(response.getBody());
            }

        } catch (Exception e) {
            System.err.println("Error editing issue in JIRA - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addJIRAComment() {

        /* If for any reason we don't have the JIRA ID corresponding to this Helpshift issue, we can't
         * make the required API calls. In a real system, there would be more error handling here.
         */
        if (!issueIdMap.containsKey(webhookData.getIssue().getId())) {
            System.err.println("Skipping JIRA update because we don't have the JIRA ID");
            return;
        }

        try {
            String payload = generateCommentPayload();
            String url = Config.JIRA_API_ENDPOINT + issueIdMap.get(webhookData.getIssue().getId()) + "/comment";

            System.out.println("Making an API call to " + url + " with payload\n\t" + payload);

            HttpResponse<String> response = Unirest.post(url)
                    .basicAuth(Config.JIRA_USERNAME, Config.JIRA_PASSWORD)
                    .header("Content-Type", "application/json")
                    .body(payload).asString();

            if (response.getStatus() != HttpStatus.SC_CREATED) {
                System.err.println("There was a problem POSTing to JIRA - " + response.getBody());
                return;
            }

        } catch (Exception e) {
            System.err.println("Error creating comment in JIRA - " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void run() {
        try {

            /* In this sample code, we are simulating a system which creates a JIRA ticket to track Helpshift issues.
             * This can be useful when you are already using JIRA for issue tracking, and want your agents to keep
             * using it.
             * For each new Helpshift issue, a JIRA ticket is created. Any updates to the JIRA are also synced to
             * the ticket. This includes issue status updates, tag changes, and messages from agents and users.
             * This is a one-way sync only, from Helpshift to JIRA, but can also be extended to sync both ways.
             * See the other code samples for examples on how to use the Helpshift API to edit issues in Helpshift.
             */

            if (webhookData.getEventType().equals("create_issue")) {
                createJIRA();
            } else if (webhookData.getEventType().equals("update_issue")) {
                editJIRA();

                /* Messages can be added to issues in many ways, and don't always correspond to status updates. */
                if (webhookData.getIssue().hasMessages()) {
                    addJIRAComment();
                }
            }

        } catch (Exception e) {
            System.err.println("Error while handling webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
