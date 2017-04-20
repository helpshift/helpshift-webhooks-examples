package com.helpshift.examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    /* Slack related fields */
    public final static String WEBHOOK_URL;

    /* JIRA related fields */
    public static final String JIRA_API_ENDPOINT;
    public static final String JIRA_USERNAME;
    public static final String JIRA_PASSWORD;
    public static final String JIRA_PROJECT_KEY;

    public static final String JIRA_HSTICKET_FIELDNAME;
    public static final String JIRA_ENDUSER_FIELDNAME;


    public static final String HS_API_ENDPOINT;
    public static final String HS_APIKEY;

    static {

        Properties properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream("conf/config.properties");
            if (inputStream != null) {
                properties.load(inputStream);
                System.out.println("Properties Loaded.");
            } else {
                throw new IOException("Config file not found.");
            }
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }

        WEBHOOK_URL = properties.getProperty("slack.webhook.url");

        JIRA_API_ENDPOINT = properties.getProperty("jira.api.endpoint");
        JIRA_USERNAME     = properties.getProperty("jira.api.username");
        JIRA_PASSWORD     = properties.getProperty("jira.api.password");
        JIRA_PROJECT_KEY = properties.getProperty("jira.project.key");

        JIRA_HSTICKET_FIELDNAME = properties.getProperty("jira.hsticket.fieldname");
        JIRA_ENDUSER_FIELDNAME  = properties.getProperty("jira.enduser.fieldname");

        HS_API_ENDPOINT = properties.getProperty("helpshift.api.endpoint");
        HS_APIKEY       = properties.getProperty("helpshift.api.key");

    }

    public static boolean check() {
        return true;
    }
}
