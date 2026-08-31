package io.rcrm.api.pojo.externalJobBoards.logicmelon;

public class LogicmelonJobBoard {
	private int job_id;
	private String job_slug;

	public LogicmelonJobBoard() {
		super();
	}

	public LogicmelonJobBoard(int job_id, String job_slug) {
		super();
		this.setJob_id(job_id);
		this.setJob_slug(job_slug);
	}

	public int getJob_id() {
		return job_id;
	}

	public void setJob_id(int job_id) {
		this.job_id = job_id;
	}

	public String getJob_slug() {
		return job_slug;
	}

	public void setJob_slug(String job_slug) {
		this.job_slug = job_slug;
	}

}
