package io.rcrm.api.pojo.reaper;

import java.util.Map;

public class UpdateEntityRequest {
    private String entityType;
    private Map<String, String> columnsAndValue;

    // Default constructor
    public UpdateEntityRequest() {
    }

    public UpdateEntityRequest(String entityType, Map<String, String> columnsAndValue) {
        this.entityType = entityType;
        this.columnsAndValue = columnsAndValue;
    }

    public Map<String, String> getColumnsAndValue() {
        return columnsAndValue;
    }

    public void setColumnsAndValue(Map<String, String> columnsAndValue) {
        this.columnsAndValue = columnsAndValue;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}
