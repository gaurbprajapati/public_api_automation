package io.rcrm.api.pojo.albatross;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class New_activity_templatePage {
    private String name;

    @JsonProperty("template_body")
    private String templateBody;

    @JsonProperty("activity_type")
    private Integer activityType;

    @JsonProperty("relatedto_type_id")
    private Integer relatedToTypeId;

    @JsonProperty("is_shared")
    private String isShared;
}
