package com.helpshift.examples.objects;

import java.util.Map;

public class Assignee {

    final String name;
    final String id;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Assignee(Map<String, Object> data) {
        name = (String)data.get("name");
        id = (String)data.get("id");
    }
}
