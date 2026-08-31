package io.rcrm.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
    
    @JsonProperty("current_user")
    private CurrentUser currentUser;
    
    @JsonProperty("current_user_details")
    private CurrentUserDetails currentUserDetails;
}
