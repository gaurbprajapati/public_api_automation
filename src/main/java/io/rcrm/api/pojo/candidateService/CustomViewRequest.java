package io.rcrm.api.pojo.candidateService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomViewRequest {
    private int entityId;
    private Boolean isDetailPage;
    private List<Integer> detailActions;
    private Integer detailActionsLocked; // Optional field - only required for account-view

    // Custom constructor for cases where detailActionsLocked is not needed
    public CustomViewRequest(int entityId, boolean isDetailPage, List<Integer> detailActions) {
        this.entityId = entityId;
        this.isDetailPage = isDetailPage;
        this.detailActions = detailActions;
    }
}
