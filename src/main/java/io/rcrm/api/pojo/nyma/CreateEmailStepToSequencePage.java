package io.rcrm.api.pojo.nyma;

public class CreateEmailStepToSequencePage {

	int id;
	int step_no;
	int no_of_days;
	int time;
	int type;
	String template_title;
	String template_subject;
	String template_content;
	String update_type;
	int include_opt_out_link;

	public CreateEmailStepToSequencePage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CreateEmailStepToSequencePage(int id, int step_no, int no_of_days, int time, int type, String template_title,
			String template_subject, String template_content, String update_type,int include_opt_out_link) {
		super();
		this.id = id;
		this.step_no = step_no;
		this.no_of_days = no_of_days;
		this.time = time;
		this.type = type;
		this.template_title = template_title;
		this.template_subject = template_subject;
		this.template_content = template_content;
		this.update_type = update_type;
		this.include_opt_out_link = include_opt_out_link;
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

	public int getNo_of_days() {
		return no_of_days;
	}

	public void setNo_of_days(int no_of_days) {
		this.no_of_days = no_of_days;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTemplate_title() {
		return template_title;
	}

	public void setTemplate_title(String template_title) {
		this.template_title = template_title;
	}

	public String getTemplate_subject() {
		return template_subject;
	}

	public void setTemplate_subject(String template_subject) {
		this.template_subject = template_subject;
	}

	public String getTemplate_content() {
		return template_content;
	}

	public void setTemplate_content(String template_content) {
		this.template_content = template_content;
	}

	public String getUpdate_type() {
		return update_type;
	}

	public void setUpdate_type(String update_type) {
		this.update_type = update_type;
	}

	public int getInclude_opt_out_link() {
		return include_opt_out_link;
	}

	public void setInclude_opt_out_link(int include_opt_out_link) {
		this.include_opt_out_link = include_opt_out_link;
	}
	
	
	

}
