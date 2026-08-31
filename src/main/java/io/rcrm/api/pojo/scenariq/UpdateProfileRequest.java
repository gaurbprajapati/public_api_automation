package io.rcrm.api.pojo.scenariq;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProfileRequest {
    private String name;
    private String email;
    private String timezone;
    private String defaultBranch;
}
