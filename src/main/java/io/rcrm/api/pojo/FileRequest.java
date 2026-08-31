package io.rcrm.api.pojo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileRequest {

    @JsonProperty("selected_files")
    private List<FileInfo> selectedFiles;

    // Getters and Setters
    public List<FileInfo> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(List<FileInfo> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }
}