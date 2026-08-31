package io.rcrm.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Call_Log {

	public Call_Log() {
		// TODO Auto-generated constructor stub
	}

	private String call_type;
	private String call_notes;
	private String contact_number;
	
	private int reminder;
	private String call_started_on;

	private String related_to = "";
	private String related_to_type = "";
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String collaborator_user_ids;
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private String collaborator_team_ids;
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer enable_auto_populate_teams = null;
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int created_by;
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int updated_by;
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private String duration;

	private String associated_companies="";
	private String associated_candidates="";
	private String associated_contacts="";
	private String associated_deals="";
	private String associated_jobs="";


	public String getCall_notes() {
		return call_notes;
	}

	public void setCall_notes(String call_notes) {
		this.call_notes = call_notes;
	}

	public String getCall_type() {
		return call_type;
	}

	public void setCall_type(String call_type) {
		this.call_type = call_type;
	}

	public int getReminder() {
		return reminder;
	}

	public void setReminder(int reminder) {
		this.reminder = reminder;
	}

	public String getCall_started_on() {
		return call_started_on;
	}

	public void setCall_started_on(String call_started_on) {
		this.call_started_on = call_started_on;
	}

	public String getContact_number() {
		return contact_number;
	}

	public void setContact_number(String contact_number) {
		this.contact_number = contact_number;
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
	
	
	public String getAssociated_companies() {
		return associated_companies;
	}

	public void setAssociated_companies(String associated_companies) {
		this.associated_companies = associated_companies;
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
	
	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

}
