package io.rcrm.api.pojo.candidateService;

import lombok.Data;

@Data
public class RemoveFromHotlistRequest {
    private String entityname;
    private int[] selectedrows;
    private int hotlistid;
}
