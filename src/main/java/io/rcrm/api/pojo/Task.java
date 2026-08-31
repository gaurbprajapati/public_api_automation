package io.rcrm.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Task {
	
	private String title;
	private String description;
	private int reminder;
	private String start_date;
	private String related_to="";
	private String related_to_type="";
	private String collaborators;

	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int task_type_id;
	
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
	private String associated_companies="";
	private String associated_candidates="";
	private String associated_contacts="";
	private String associated_deals="";
	private String associated_jobs="";
	
	
	public Task() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Task(String title, String description, int reminder, String start_date, String related_to,
			String related_to_type, String collaborators, int task_type_id, String collaborator_team_ids, Integer enable_auto_populate_teams,
			int owner_id, int created_by, int updated_by, String associated_companies, String associated_candidates,
			String associated_contacts, String associated_deals, String associated_jobs) {
		super();
		this.title = title;
		this.description = description;
		this.reminder = reminder;
		this.start_date = start_date;
		this.related_to = related_to;
		this.related_to_type = related_to_type;
		this.collaborators = collaborators;
		this.task_type_id = task_type_id;
		this.collaborator_team_ids = collaborator_team_ids;
		this.enable_auto_populate_teams = enable_auto_populate_teams;
		this.owner_id = owner_id;
		this.created_by = created_by;
		this.updated_by = updated_by;
		this.associated_companies = associated_companies;
		this.associated_candidates = associated_candidates;
		this.associated_contacts = associated_contacts;
		this.associated_deals = associated_deals;
		this.associated_jobs = associated_jobs;
	}
	
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
	
	public String getCollaborators() {
		return collaborators;
	}
	
	public void setCollaborators(String collaborators) {
		this.collaborators = collaborators;
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

	public int getTask_type_id() {
		return task_type_id;
	}

	public void setTask_type_id(int task_type_id) {
		this.task_type_id = task_type_id;
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

}
