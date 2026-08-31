package io.rcrm.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileInfo {

    @JsonProperty("object_key")
    private String objectKey;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("folder_id")
    private String folderId;

    @JsonProperty("size")
    private String size;

    @JsonProperty("owner_id")
    private String ownerId;

    @JsonProperty("content_type")
    private String contentType;

    public FileInfo(String objectKey, String fileName, String folderId, String size, String ownerId, String contentType) {
        this.objectKey = objectKey;
        this.fileName = fileName;
        this.size = size;
        this.ownerId = ownerId;
        this.folderId = folderId;
        this.contentType = contentType;
    }

    // Getters and Setters
    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}

