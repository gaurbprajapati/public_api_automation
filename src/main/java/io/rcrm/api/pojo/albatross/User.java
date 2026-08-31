package io.rcrm.api.pojo.albatross;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private int id;
    private RoleId roleid;
    private String firstname;
    private String lastname;
    private String locale;
    private int userstatus;
}
