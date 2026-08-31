package io.rcrm.api.pojo.scenariq;

import java.util.List;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancellationFeedbackRequest {
    private List<String> reasons;
    private String improvementSuggestion;
    private String alternativePlatform;
    private String password;
}
