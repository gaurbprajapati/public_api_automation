package io.rcrm.api.pojo.externalJobBoards;

public class JobBoardSettings {

	private String email;
	private String password;
	private String feed_id;

	public JobBoardSettings() {
		super();
		// TODO Auto-generated constructor stub
	}

	public JobBoardSettings(String email, String password, String feed_id) {
		super();
		this.email = email;
		this.password = password;
		this.feed_id = feed_id;
	}

	public String getUserEmail() {
		return email;
	}

	public void setUserEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFeed_id() {
		return feed_id;
	}

	public void setFeed_id(String feed_id) {
		this.feed_id = feed_id;
	}
}
