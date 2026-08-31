package io.rcrm.api.pojo.albatross;

public class UpdateFieldStatus {
    private String key;
    private String value;
    private String tableFlag;
    private long id;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTableFlag() {
        return tableFlag;
    }

    public void setTableFlag(String tableFlag) {
        this.tableFlag = tableFlag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}