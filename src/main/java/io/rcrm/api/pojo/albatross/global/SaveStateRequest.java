package io.rcrm.api.pojo.albatross.global;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveStateRequest {
    private String datatablekey;
    private String columnstate;
    private Boolean fromDetailPage;
    private Boolean updateUserObj;
    private Boolean isListPageV2;
    private Boolean isDetailPageV2;
}

