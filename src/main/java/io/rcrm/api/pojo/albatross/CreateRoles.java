package io.rcrm.api.pojo.albatross;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoles {
    private Integer id = null;
    private String role;
    private String useraccesjson;
}
