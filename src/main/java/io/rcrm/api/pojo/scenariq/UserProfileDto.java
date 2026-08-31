package io.rcrm.api.pojo.scenariq;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Long accountId;
    private String accountName;
    private boolean hasGitToken;
    private String timezone;
    private String defaultBranch;
    private String planType;
    private int scanCreditsTotal;
    private int scanCreditsUsed;
    private int scanCreditsRemaining;
    private int maxServices;
    private int maxInvites;
}
