package io.rcrm.api.pojo.albatross;

public class GlobalUpdateFields {

    private String key;
    private int value;
    private String tableFlag;
    private Object id;
    private String reason;
    private String label;

    public GlobalUpdateFields() {}

    public GlobalUpdateFields(String key, int value, String tableFlag, Object id, String reason, String label) {
        this.key = key;
        this.value = value;
        this.tableFlag = tableFlag;
        this.id = id;
        this.reason = reason;
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getTableFlag() {
        return tableFlag;
    }

    public void setTableFlag(String tableFlag) {
        this.tableFlag = tableFlag;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}

