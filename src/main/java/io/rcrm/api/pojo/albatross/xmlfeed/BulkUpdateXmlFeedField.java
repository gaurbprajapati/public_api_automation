package io.rcrm.api.pojo.albatross.xmlfeed;

import java.util.List;

public class BulkUpdateXmlFeedField {

    private String key;
    private String value;
    private String tableFlag;
    private List<Integer> id;
    private boolean isXMLFeedExisting;

    public BulkUpdateXmlFeedField() {}

    public BulkUpdateXmlFeedField(String key, String value, String tableFlag, List<Integer> id, boolean isXMLFeedExisting) {
        this.key = key;
        this.value = value;
        this.tableFlag = tableFlag;
        this.id = id;
        this.isXMLFeedExisting = isXMLFeedExisting;
    }

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

    public List<Integer> getId() {
        return id;
    }

    public void setId(List<Integer> id) {
        this.id = id;
    }

    public boolean getIsXMLFeedExisting() {
        return isXMLFeedExisting;
    }

    public void setIsXMLFeedExisting(boolean isXMLFeedExisting) {
        this.isXMLFeedExisting = isXMLFeedExisting;
    }
}
