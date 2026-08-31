package io.recruitcrm.albatross.account;

import io.rcrm.api.pojo.albatross.SignUp;

public class SignUpJson {

	private SignUp user;
	private String inviteuser = "";

	public SignUp getUser() {
		return user;
	}

	public void setUser(SignUp user) {
		this.user = user;
	}
	
	public String getInviteuser() {
		return inviteuser;
	}

	public void setInviteuser(String inviteuser) {
		this.inviteuser = inviteuser;
	}
}
