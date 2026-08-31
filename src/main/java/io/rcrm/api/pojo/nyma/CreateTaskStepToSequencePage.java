package io.rcrm.api.pojo.nyma;

public class CreateTaskStepToSequencePage {

	int id;
	int step_no;
	int no_of_days;
	int time;
	int type;
	String task_title;
	String task_description;
	int reminder;
	String update_type;
	int task_type;

	public CreateTaskStepToSequencePage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CreateTaskStepToSequencePage(int id, int step_no, int no_of_days, int time, int type, String task_title,
			String task_description, int reminder, String update_type, int task_type) {
		super();
		this.id = id;
		this.step_no = step_no;
		this.no_of_days = no_of_days;
		this.time = time;
		this.type = type;
		this.task_title = task_title;
		this.task_description = task_description;
		this.reminder = reminder;
		this.update_type = update_type;
		this.task_type = task_type;
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

	public String getTask_title() {
		return task_title;
	}

	public void setTask_title(String task_title) {
		this.task_title = task_title;
	}

	public String getTask_description() {
		return task_description;
	}

	public void setTask_description(String task_description) {
		this.task_description = task_description;
	}

	public int getReminder() {
		return reminder;
	}

	public void setReminder(int reminder) {
		this.reminder = reminder;
	}

	public String getUpdate_type() {
		return update_type;
	}

	public void setUpdate_type(String update_type) {
		this.update_type = update_type;
	}

	public int getTask_type() {
		return task_type;
	}

	public void setTask_type(int task_type) {
		this.task_type = task_type;
	}

}
