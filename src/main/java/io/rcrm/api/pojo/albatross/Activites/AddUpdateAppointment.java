package io.rcrm.api.pojo.albatross.Activites;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddUpdateAppointment {

    private boolean task;
    private Appointment appointment;
    private List<AppointmentAttendee> collaborator;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private Map<String, List<Object>> associated_data;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)// Map to handle dynamic or empty structure
    private List<AssociationData> association_data;
    private String actionSource;
    private List<Integer> collaborator_team_ids;
    private List<Integer> collaborator_user_ids;

}
