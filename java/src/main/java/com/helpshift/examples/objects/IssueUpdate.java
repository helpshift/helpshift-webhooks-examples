package com.helpshift.examples.objects;


import java.time.Instant;
import java.util.List;
import java.util.Map;

public class IssueUpdate {

    public enum IssueUpdateType { STATE_CHANGED, TAGS_CHANGED, UNKNOWN}

    final IssueUpdateType type;
    final String state;
    final Instant changedAt;
    final List<String> tags;

    public IssueUpdate(Issue issue, Map<String, Object> data) {
        /* For an issue update, we are considering following example scenarios -
        * The webhook data will contain a state_data map, which contains details of issue status updates, or
        * it will contain a tags map, which is for any changes to the issue tags.
         */
        if (data.containsKey("state_data")) {
            Map<String, Object> state_data = (Map<String, Object>)data.get("state_data");
            state = (String)state_data.get("state");
            changedAt = Instant.ofEpochMilli((Long)state_data.get("changed_at"));
            type = IssueUpdateType.STATE_CHANGED;
            tags = null;
        } else if (data.containsKey("tags")) {
            tags = (List<String>)data.get("tags");
            type = IssueUpdateType.TAGS_CHANGED;
            state = null;
            changedAt = Instant.EPOCH;
        } else {
            tags = null;
            state = null;
            changedAt = Instant.EPOCH;
            type = IssueUpdateType.UNKNOWN;
        }

        /* AN issue update could also be triggered by a message being added to the issue,
         * either by the end user or by an agent.
         */
        if (data.containsKey("messages")) {
            List<Object> messages = (List<Object>)data.get("messages");
            for (Object messageMap : messages) {
                Map<String, Object> messageData = (Map<String, Object>) messageMap;
                issue.addMessage(new Message(messageData));
            }
        }
    }

    public String getState() {
        return state;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public IssueUpdateType getType() {
        return type;
    }

    public List<String> getTags() {
        return tags;
    }

    private String join(List<String> strings) {
        if (strings.size() == 0)
            return "[]";

        final StringBuilder sb = new StringBuilder("[");
        sb.append(strings.get(0));

        for (String s : strings.subList(1, strings.size())) {
            sb.append(", ");
            sb.append(s);
        }

        sb.append("]");

        return sb.toString();
    }

    public String getTagsAsString() {
        return join(tags);
    }
}
