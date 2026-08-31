package io.recruitcrm.authservice.mfa;

import java.util.List;

import io.rcrm.api.pojo.authservice.TwoFactorAuthentication;

public class TwoFactorAuthenticationJson {
	private List<TwoFactorAuthentication> usersEnforced;
	// Getters and Setters
	public List<TwoFactorAuthentication> getUsersEnforced() {
		return usersEnforced;
	}

	public void setUsersEnforced(List<TwoFactorAuthentication> usersEnforced) {
		this.usersEnforced = usersEnforced;
	}

}
