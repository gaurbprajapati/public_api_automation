package io.rcrm.api.pojo.externalJobBoards.logicmelon;

public class LogicmelonExternalJobBoard {
	private int job_board_id;
	private LogicmelonJobBoardSetting settings;
	private int enable_logicmelon_to_accounts_user;

	public LogicmelonExternalJobBoard() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LogicmelonExternalJobBoard(int job_board_id, LogicmelonJobBoardSetting settings,
			int enable_logicmelon_to_accounts_user) {
		super();
		this.job_board_id = job_board_id;
		this.settings = settings;
		this.enable_logicmelon_to_accounts_user = enable_logicmelon_to_accounts_user;
	}

	public int getJob_board_id() {
		return job_board_id;
	}

	public void setJob_board_id(int job_board_id) {
		this.job_board_id = job_board_id;
	}

	public LogicmelonJobBoardSetting getSettings() {
		return settings;
	}

	public void setSettings(LogicmelonJobBoardSetting settings) {
		this.settings = settings;
	}

	public int getEnable_logicmelon_to_accounts_user() {
		return enable_logicmelon_to_accounts_user;
	}

	public void setEnable_logicmelon_to_accounts_user(int enable_logicmelon_to_accounts_user) {
		this.enable_logicmelon_to_accounts_user = enable_logicmelon_to_accounts_user;
	}

}
