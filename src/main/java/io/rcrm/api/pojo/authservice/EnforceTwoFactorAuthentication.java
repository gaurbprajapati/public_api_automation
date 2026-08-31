package io.rcrm.api.pojo.authservice;

public class EnforceTwoFactorAuthentication {
	private String usersEnforced;

	public String getUsersEnforced() {
		return usersEnforced;
	}

	public void setUsersEnforced(String usersEnforced) {
		this.usersEnforced = usersEnforced;
	}
}
