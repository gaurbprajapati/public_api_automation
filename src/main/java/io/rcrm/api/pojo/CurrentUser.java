package io.rcrm.api.pojo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {
    private int id;
    private String firstname;
    private String lastname;
    private String email;
    private String contactnumber;
    private String city;
    private String country;
    private String state;
    private String locale;
}
