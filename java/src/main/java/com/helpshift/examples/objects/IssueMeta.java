package com.helpshift.examples.objects;


import java.util.HashMap;
import java.util.Map;

public class IssueMeta {


    final Map<String, String> appMetadata;
    final Map<String, String> hardwareInfo;
    final Map<String, String> otherMetadata;
    final Map<String, String> customMetadata;

    private void processMap(Map<String, Object> source, Map<String, String> dest) {

        for (String key : source.keySet()) {
            String val = (String)source.get(key);
            dest.put(key, val);
        }
    }

    public IssueMeta(Map<String, Object> data) {

        /* For issues created from the Helpshift SDK, the issue meta will contain device information
         * which can be useful for debugging. This is divided into application metadata, hardware
         * information, and other metadata such as language and country code.
         */

        if (data.containsKey("application")) {
            appMetadata = new HashMap<>();
            processMap((Map<String, Object>)data.get("application"), appMetadata);
        } else {
            appMetadata = null;
        }

        if (data.containsKey("hardware")) {
            hardwareInfo = new HashMap<>();
            processMap((Map<String, Object>)data.get("hardware"), hardwareInfo);
        } else {
            hardwareInfo = null;
        }

        if (data.containsKey("other")) {
            otherMetadata = new HashMap<>();
            processMap((Map<String, Object>)data.get("other"), otherMetadata);
        } else {
            otherMetadata = null;
        }

        /* An application using the Helpshift SDK can also set its own custom metadata values
         * for each issue. These are returned separately from the other metadata values above.
         */
        customMetadata = new HashMap<>();
        for (String key: data.keySet()) {
            Object val = data.get(key);
            if (val instanceof String) {
                String value = (String)val;
                customMetadata.put(key, value);
            }
        }
    }
}
