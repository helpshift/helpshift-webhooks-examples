package com.helpshift.examples.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class WebhookData {

    final String eventId;
    final String eventType;
    final Instant timestamp;
    final String domain;
    final String appId;
    final Webhook webhook;
    final Issue issue;


    /* The webhook data is a JSON map containing the relevant issue data based on the type of webhook
     * and the type of issue update.
     *
     * We are using <a href="https://github.com/FasterXML/jackson">Jackson</a> to parse the JSON in the
     * request body into a set of POJOs, which are used in the rest of the program to retrieve the webhook
     * data.
     * Note that Jackson's data binding feature provides a more automated way to do the above conversion,
     * we are doing it manually to better show the various event types and the data contained in the webhook
     * for each event type.
     */
    public WebhookData(String event_data) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        final Map<String, Object> data = mapper.readValue(event_data, type);

        /* These details are provided for every webhook invocation, they are useful when you have multiple
         * webhooks configured for different apps.
         */
        eventId = (String)data.get("event_id");
        eventType = (String)data.get("event_type");
        timestamp = Instant.ofEpochMilli((Long)data.get("event_timestamp"));
        domain = (String)data.get("domain");
        appId = (String)data.get("appId");

        /* The webhook map contains details about the triggered webhook - the name configured on the
         * Helpshift Dashboard, and a unique ID for each webhook
         */
        Map<String, Object> webhook_data = (Map<String, Object>)data.get("webhook");
        webhook = new Webhook(webhook_data);

        /* The exact contents of the data map will be different for different webhooks, and
         * will change based on the event type.
         */
        Map<String, Object> issue_data = (Map<String, Object>)data.get("data");
        issue = new Issue(issue_data);
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDomain() {
        return domain;
    }

    public String getAppId() {
        return appId;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public Issue getIssue() {
        return issue;
    }
}