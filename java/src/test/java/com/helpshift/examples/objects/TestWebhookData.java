package com.helpshift.examples.objects;

import org.junit.Test;

import static org.junit.Assert.*;


public class TestWebhookData {


    @Test
    public void testCreateEvent()
    {
        try {
            final String json = "{\"event_id\":\"6e988a51-5a74-4936-a2ae-f70829a8bad2\",\"event_type\":\"create_issue\",\"event_timestamp\":1491213204044,\"domain\":\"wabbit\",\"app_id\":\"wabbit_app_20170209061222871-b957b7fee7356c1\",\"webhook\":{\"title\":\"all new issues\",\"id\":\"wabbit_webhook_20170403054643256-cfa8ae19db06dcf\"},\"data\":{\"meta\":{\"userType\":\"paid\",\"application\":{\"application-version\":\"1.0\",\"application-identifier\":\"com.example.qotdapp\",\"application-name\":\"Helpshift Demo\"},\"hardware\":{\"device-model\":\"GT-N7100\",\"battery-level\":\"100%\",\"battery-status\":\"Charging\",\"total-space-phone\":\"10.46 GB\",\"free-space-phone\":\"9.39 GB\"},\"other\":{\"os-version\":\"4.4.2\",\"platform\":\"android\",\"network-type\":\"WIFI\",\"country-code\":\"US\",\"library-version\":\"3.4.2\",\"language\":\"English\"}},\"title\":\"bezoar\",\"messages\":[{\"id\":\"wabbit_message_20170403095223994-510a33889478a1c\",\"body\":\"bezoar\",\"created_at\":1491213143992,\"author\":{\"name\":\"User\",\"id\":\"wabbit_profile_20170403095223985-48e6557eceb0660\",\"emails\":[\"user@example.mobi\"]}}],\"id\":68,\"author_name\":\"User\",\"author_email\":\"user@example.mobi\",\"domain\":\"wabbit\",\"state_data\":{\"state\":\"new\",\"changed_at\":1491213143992},\"created_at\":1491213143992}}";
            final WebhookData webhookData = new WebhookData(json);

            assertEquals("create_issue", webhookData.getEventType());
            assertEquals(68, webhookData.getIssue().getId());
        } catch (Exception e) {
            fail("Unexpected exception - " + e.getMessage());
            e.printStackTrace();
        }

    }
}