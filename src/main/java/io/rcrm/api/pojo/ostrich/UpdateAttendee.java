package io.rcrm.api.pojo.ostrich;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAttendee {

    private long meeting_id;
    private List<Attendee> attendees;
    private String event_type;

}
