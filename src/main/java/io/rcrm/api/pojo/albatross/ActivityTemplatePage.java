package io.rcrm.api.pojo.albatross;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityTemplatePage {

    @JsonProperty("activity_template")
    private New_activity_templatePage activityTemplate;
}
