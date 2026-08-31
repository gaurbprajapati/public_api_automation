package io.rcrm.api.pojo.candidateService;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BulkActionsCustomView {
    private final int entityId;
    private final List<Integer> listActions;
    private int listActionsLocked; // Optional field

    public BulkActionsCustomView(int entityId, List<Integer> listActions) {
        this.entityId = entityId;
        this.listActions = listActions;
    }
}
