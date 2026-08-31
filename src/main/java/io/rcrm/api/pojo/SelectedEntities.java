package io.rcrm.api.pojo;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "selected_entity", "offset", "page_size", "continuous_token" })
public class SelectedEntities {

    @JsonProperty("selected_entity")
    private List<SelectedEntity> selectedEntity;

    private int offset;

    @JsonProperty("page_size")
    private int pageSize;

    @JsonProperty("continuous_token")
    private String continuousToken;

    // Constructors, Getters, and Setters
    public SelectedEntities(List<SelectedEntity> selectedEntity, int offset, int pageSize, String continuousToken) {
        this.selectedEntity = selectedEntity;
        this.offset = offset;
        this.pageSize = pageSize;
        this.continuousToken = continuousToken;
    }

    public List<SelectedEntity> getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(List<SelectedEntity> selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getContinuousToken() {
        return continuousToken;
    }

    public void setContinuousToken(String continuousToken) {
        this.continuousToken = continuousToken;
    }
}
