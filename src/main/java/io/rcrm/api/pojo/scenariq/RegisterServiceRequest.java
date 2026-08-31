package io.rcrm.api.pojo.scenariq;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterServiceRequest {
    private String serviceName;
    private String ownerName;
    private String serviceType;
    private String backendRepoUrl;
    private String automationRepoUrl;
    private String backendBranch;
    private String automationBranch;
    private String backendControllersPath;
    private String automationTestsPath;
    private String bindingKeyword;
    private String gitUsername;
    private String gitToken;
}
