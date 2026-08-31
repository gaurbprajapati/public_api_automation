package io.rcrm.api.pojo.externalJobBoards.broadbean;

public class BroadbeanPermission {
	private String enable_broadbean_to_accounts_user;

	public BroadbeanPermission() {
		super();
	}

	public BroadbeanPermission(String enable_broadbean_to_accounts_user) {
		this.enable_broadbean_to_accounts_user = enable_broadbean_to_accounts_user;
	}

	public String getEnable_broadbean_to_accounts_user() {
		return enable_broadbean_to_accounts_user;
	}

	public void setEnable_broadbean_to_accounts_user(String enable_broadbean_to_accounts_user) {
		this.enable_broadbean_to_accounts_user = enable_broadbean_to_accounts_user;
	}

}
