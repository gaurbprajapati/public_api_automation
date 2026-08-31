package io.rcrm.api.pojo.albatross;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DeleteCallRecording {
    
    private CallLog callLog;
    
    public DeleteCallRecording() {
        super();
    }
    
    public CallLog getCallLog() {
        return callLog;
    }
    
    public void setCallLog(CallLog callLog) {
        this.callLog = callLog;
    }
    
    public static class CallLog {

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        private int id;

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        private String recording;
        
        public CallLog() {
            super();
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getRecording() {
            return recording;
        }
        
        public void setRecording(String recording) {
            this.recording = recording;
        }
    }
} 