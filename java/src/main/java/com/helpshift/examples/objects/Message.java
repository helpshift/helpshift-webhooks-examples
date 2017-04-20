package com.helpshift.examples.objects;


import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Message {

    final String id;
    final String body;
    final Instant createdAt;
    final Author author;

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Author getAuthor() {
        return author;
    }

    public Message(Map<String, Object> data) {

        /* When a message is added to an issue, either by an agent or by the user, the webhook
         * data will contain an array of messages, each with the message body and the
         * message author included.
         */

        id = (String)data.get("id");
        body = (String)data.get("body");
        createdAt = Instant.ofEpochMilli((Long)data.get("created_at"));

        if (data.containsKey("author")) {
            author = new Author((Map<String, Object>)data.get("author"));
        } else {
            author = null;
        }

    }


    public class Author {
        final String name;
        final String id;
        final List<String> emails;

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public List<String> getEmails() {
            return emails;
        }

        public Author(Map<String, Object> data) {
            id = (String)data.get("id");
            name = (String)data.get("name");
            this.emails = (List<String>)data.get("emails");

        }

    }
}
