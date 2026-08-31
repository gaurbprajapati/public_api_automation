package io.rcrm.api.pojo.externalJobBoards;

public class MannualVerification {

	public MannualVerification() {
		super();
	}

	private String name;
	private String website;
	private String email;
	private String comments;

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}