package io.rcrm.api.pojo.nyma;

import com.fasterxml.jackson.annotation.JsonInclude;

public class UpdateStepsInEnrollmentPage {

	private int type;
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int step_no;
	private String template_content;
	private String template_subject;
	private String template_title;
	private String task_description;
	private String task_title;
	private String reminder;
	private String update_type;
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int seq_step_details_id;
	private int no_of_days;
	private int include_opt_out_link;
	private int time;
	private int id;

	// Constructors, getters, and setters

	public UpdateStepsInEnrollmentPage() {
	}

	// Getters and setters

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTemplate_content() {
		return template_content;
	}

	public void setTemplate_content(String template_content) {
		this.template_content = template_content;
	}

	public String getTemplate_subject() {
		return template_subject;
	}

	public void setTemplate_subject(String template_subject) {
		this.template_subject = template_subject;
	}

	public String getTemplate_title() {
		return template_title;
	}

	public void setTemplate_title(String template_title) {
		this.template_title = template_title;
	}

	public String getTask_description() {
		return task_description;
	}

	public void setTask_description(String task_description) {
		this.task_description = task_description;
	}

	public String getTask_title() {
		return task_title;
	}

	public void setTask_title(String task_title) {
		this.task_title = task_title;
	}

	public String getReminder() {
		return reminder;
	}

	public void setReminder(String reminder) {
		this.reminder = reminder;
	}

	public String getUpdate_type() {
		return update_type;
	}

	public void setUpdate_type(String update_type) {
		this.update_type = update_type;
	}

	public int getSeq_step_details_id() {
		return seq_step_details_id;
	}

	public void setSeq_step_details_id(int seq_step_details_id) {
		this.seq_step_details_id = seq_step_details_id;
	}

	public int getNo_of_days() {
		return no_of_days;
	}

	public void setNo_of_days(int no_of_days) {
		this.no_of_days = no_of_days;
	}

	public int getInclude_opt_out_link() {
		return include_opt_out_link;
	}

	public void setInclude_opt_out_link(int include_opt_out_link) {
		this.include_opt_out_link = include_opt_out_link;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStep_no() {
		return step_no;
	}

	public void setStep_no(int step_no) {
		this.step_no = step_no;
	}
}
