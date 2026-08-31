package io.rcrm.api.pojo.albatross.Activites;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {

    private String title;
    private String description;
    private int status;
    private int type;
    private long startdate;
    private long enddate;
    private String reminder;
    private String creatorname;
    private String address;
    private int allday;
    private int ownerid;
    private int accountid;
    private String eventid;
    private String calendarid;
    private String relatedto;
    private String relatedtotypeid;
    private String relatedtoname;
    private String htmllink;
    private String emailbatchid;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int meetingtype;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int id;
    private int noCalInvites;
    private String relatedtofirstname;

}
