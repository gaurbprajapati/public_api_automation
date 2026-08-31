package io.rcrm.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Meeting {

	private String title;
	private String description;
	private String address;
	private int reminder;
	private String start_date;
	private String end_date;
	private String related_to = "";
	private String related_to_type = "";
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String collaborator_user_ids;
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private String collaborator_team_ids;
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer enable_auto_populate_teams = null;
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int owner_id;

	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int created_by;
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int updated_by;
	
	private int do_not_send_calendar_invites;
	
	private String associated_companies="";
	private String associated_candidates="";
	private String associated_contacts="";
	private String associated_deals="";
	private String associated_jobs="";
	private String attendee_contacts = "";
	private String attendee_candidates = "";
	private String attendee_users = "";
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int meeting_type_id;

	public Meeting() {
	}

	// getter and setter methods:

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getReminder() {
		return reminder;
	}

	public void setReminder(int reminder) {
		this.reminder = reminder;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}
	
	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public String getRelated_to() {
		return related_to;
	}

	public void setRelated_to(String related_to) {
		this.related_to = related_to;
	}

	public String getRelated_to_type() {
		return related_to_type;
	}

	public void setRelated_to_type(String related_to_type) {
		this.related_to_type = related_to_type;
	}

	
	
	public String getAssociated_companies() {
		return associated_companies;
	}

	public void setAssociated_companies(String associated_companies) {
		this.associated_companies = associated_companies;
	}
	
	public String getAssociated_candidates() {
		return associated_candidates;
	}

	public void setAssociated_candidates(String associated_candidates) {
		this.associated_candidates = associated_candidates;
	}
	
	public String getAssociated_contacts() {
		return associated_contacts;
	}

	public void setAssociated_contacts(String associated_contacts) {
		this.associated_contacts = associated_contacts;
	}
	
	public String getAssociated_deals() {
		return associated_deals;
	}

	public void setAssociated_deals(String associated_deals) {
		this.associated_deals = associated_deals;
	}
	public String getAssociated_jobs() {
		return associated_jobs;
	}

	public void setAssociated_jobs(String associated_jobs) {
		this.associated_jobs = associated_jobs;
	}

	public String getCollaborator_user_ids() {
		return collaborator_user_ids;
	}

	public void setCollaborator_user_ids(String collaborator_user_ids) {
		this.collaborator_user_ids = collaborator_user_ids;
	}

	public String getCollaborator_team_ids() {
		return collaborator_team_ids;
	}

	public void setCollaborator_team_ids(String collaborator_team_ids) {
		this.collaborator_team_ids = collaborator_team_ids;
	}

	public Integer getEnable_auto_populate_teams() {
		return enable_auto_populate_teams;
	}

	public void setEnable_auto_populate_teams(Integer enable_auto_populate_teams) {
		this.enable_auto_populate_teams = enable_auto_populate_teams;
	}

	public int getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(int owner_id) {
		this.owner_id = owner_id;
	}

	public int getCreated_by() {
		return created_by;
	}

	public void setCreated_by(int created_by) {
		this.created_by = created_by;
	}

	public int getUpdated_by() {
		return updated_by;
	}

	public void setUpdated_by(int updated_by) {
		this.updated_by = updated_by;
	}

	public int getDo_not_send_calendar_invites() {
		return do_not_send_calendar_invites;
	}

	public void setDo_not_send_calendar_invites(int do_not_send_calendar_invites) {
		this.do_not_send_calendar_invites = do_not_send_calendar_invites;
	}	
	
	public int getMeeting_type_id() {
		return meeting_type_id;
	}

	public void setMeeting_type_id(int meeting_type_id) {
		this.meeting_type_id = meeting_type_id;
	}

	public String getAttendee_users() {
		return attendee_users;
	}

	public void setAttendee_users(String attendee_users) {
		this.attendee_users = attendee_users;
	}

	public String getAttendee_contacts() {
		return attendee_contacts;
	}

	public void setAttendee_contacts(String attendee_contacts) {
		this.attendee_contacts = attendee_contacts;
	}

	public String getAttendee_candidates() {
		return attendee_candidates;
	}

	public void setAttendee_candidates(String attendee_candidates) {
		this.attendee_candidates = attendee_candidates;
	}
}

