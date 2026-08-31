package io.rcrm.api.pojo.ostrich;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Attendee {

    private String attendeeid;
    private int attendeetype;
    private String email;
    private long appointmentid;
    private String name;

}
