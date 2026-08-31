package io.rcrm.api.pojo.albatross;

import java.util.List;
import java.util.Map;

public class AddCallLog {

    private CallLog callLog;
    private Map<String, List<Object>> associatedData;

    // Getters and Setters
    public CallLog getCallLog() {
        return callLog;
    }

    public void setCallLog(CallLog callLog) {
        this.callLog = callLog;
    }

    public Map<String, List<Object>> getAssociatedData() {
        return associatedData;
    }

    public void setAssociatedData(Map<String, List<Object>> associatedData) {
        this.associatedData = associatedData;
    }
}
