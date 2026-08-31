package io.rcrm.api.pojo.externalJobBoards.logicmelon;

public class LogicmelonJobBoardSetting {
	private String username;
	private String password;
	private String apikey;

	public LogicmelonJobBoardSetting() {
		super();
	}

	public LogicmelonJobBoardSetting(String username, String password, String apikey) {
		super();
		this.username = username;
		this.password = password;
		this.apikey = apikey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

}
