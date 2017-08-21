package com.helpshift.examples.objects;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Issue {

    final int id;
    final String authorName;
    final String authorEmail;
    final Instant createdAt;
    final IssueUpdate issueUpdate;
    final String title;
    final IssueMeta issueMeta;
    final List<Message> messages = new ArrayList<>();
    final Assignee assignee;

    public int getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public IssueUpdate getIssueUpdate() {
        return issueUpdate;
    }

    public String getTitle() {
        return title;
    }

    public IssueMeta getIssueMeta() {
        return issueMeta;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public boolean hasMessages() {
        return messages.size() > 0;
    }

    public Message getFirstMessage() {
        if (messages.size() > 0)
            return messages.get(0);
        else
            return null;
    }

    public void addMessage(final Message message) {
        this.messages.add(message);
    }

    public Assignee getAssignee() { return assignee; }

    public Issue(Map<String, Object> data) {
        id = (Integer)data.get("id");
        createdAt = Instant.ofEpochMilli((Long)data.get("created_at"));
        authorName = (String)data.get("author_name");
        authorEmail = (String)data.get("author_email");

        if (data.containsKey("updates")) {
            Map<String, Object> updates = (Map<String, Object>)data.get("updates");
            issueUpdate = new IssueUpdate(this, updates);
        } else {
            issueUpdate = null;
        }

        if (data.containsKey("title")) {
            title = (String)data.get("title");
        } else {
            title = null;
        }

        if (data.containsKey("meta")) {
            issueMeta = new IssueMeta((Map<String, Object>)data.get("meta"));
        } else {
            issueMeta = null;
        }

        if (data.containsKey("messages")) {

            List<Object> messages = (List<Object>)data.get("messages");
            for (Object messageMap : messages) {
                Map<String, Object> messageData = (Map<String, Object>) messageMap;
                this.messages.add(new Message(messageData));
            }
        }

        if (data.containsKey("assignee")) {
            assignee = new Assignee ((Map<String, Object>)data.get("assignee"));
        }
    }
}
