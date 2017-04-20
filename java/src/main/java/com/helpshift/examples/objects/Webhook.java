package com.helpshift.examples.objects;


import java.util.Map;

public class Webhook {

    final String title;
    final String id;

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    /* The Webhook name (as configured in the Helpshift Dashboard), and the unique ID
     * for the webhook.
     */
    public Webhook(Map<String, Object> data) {
        title = (String)data.get("title");
        id = (String)data.get("id");
    }
}
