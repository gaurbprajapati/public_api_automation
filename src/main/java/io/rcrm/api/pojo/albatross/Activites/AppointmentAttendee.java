package io.rcrm.api.pojo.albatross.Activites;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentAttendee {
    private String attendeeid;
    private String attendeetype;
    private String email;
    private String name;
    private String icon;
}
