package io.rcrm.api.pojo.externalJobBoards.broadbean;

public class BroadbeanJobBoard {
	private int job_board_id;
	private String job_board_setting_id;
	private String[] createdOn;
	private BroadbeanJobBoardSetting settings;

	public BroadbeanJobBoard() {
		super();
	}

	public BroadbeanJobBoard(int job_board_id, String job_board_setting_id, String[] createdOn, BroadbeanJobBoardSetting settings) {
		this.job_board_id = job_board_id;
		this.job_board_setting_id = job_board_setting_id;
		this.createdOn = createdOn;
		this.settings = settings;
	}

	public int getJob_board_id() {
		return job_board_id;
	}

	public void setJob_board_id(int job_board_id) {
		this.job_board_id = job_board_id;
	}

	public String getJob_board_setting_id() {
		return job_board_setting_id;
	}

	public void setJob_board_setting_id(String job_board_setting_id) {
		this.job_board_setting_id = job_board_setting_id;
	}

	public String[] getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String[] createdOn) {
		this.createdOn = createdOn;
	}

	public BroadbeanJobBoardSetting getSettings() {
		return settings;
	}

	public void setSettings(BroadbeanJobBoardSetting settings) {
		this.settings = settings;
	}
}
