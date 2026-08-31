package io.rcrm.api.pojo.externalJobBoards;

public class JobBoard {

	public JobBoard() {
		super();
	}

	public JobBoard(int job_board_id, JobBoardSettings settings) {
		super();
		this.job_board_id = job_board_id;
		this.settings = settings;
	}

	private int job_board_id;
	private JobBoardSettings settings;

	public int getJob_board_id() {
		return job_board_id;
	}

	public void setJob_board_id(int job_board_id) {
		this.job_board_id = job_board_id;
	}

	public JobBoardSettings getSettings() {
		return settings;
	}

	public void setSettings(JobBoardSettings settings) {
		this.settings = settings;
	}

}
