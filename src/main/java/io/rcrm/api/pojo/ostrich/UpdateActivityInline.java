package io.rcrm.api.pojo.ostrich;

import com.fasterxml.jackson.annotation.JsonInclude;

public class UpdateActivityInline {

    private int activity_id;
    private int user_id;
    private int team_id;
    private int activity_type;
    private String eventType;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String event_type;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String attendee_entity;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String attendee_entity_type_id;

    public int getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(int activity_id) {
        this.activity_id = activity_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getTeam_id() {
        return team_id;
    }

    public void setTeam_id(int team_id) {
        this.team_id = team_id;
    }

    public int getActivity_type() {
        return activity_type;
    }

    public void setActivity_type(int activity_type) {
        this.activity_type = activity_type;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getAttendee_entity() {
        return attendee_entity;
    }

    public void setAttendee_entity(String attendee_entity) {
        this.attendee_entity = attendee_entity;
    }

    public String getAttendee_entity_type_id() {
        return attendee_entity_type_id;
    }

    public void setAttendee_entity_type_id(String attendee_entity_type_id) {
        this.attendee_entity_type_id = attendee_entity_type_id;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }
}
