package io.rcrm.api.pojo.albatross;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdate {
    private User user;
    private UserDetails userdetails;
}
