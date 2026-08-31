package io.rcrm.api.pojo.scenariq;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedbackRequest {
    private int rating;
    private String message;
}
