package io.rcrm.api.pojo.albatross.notification;

public class DeleteCandidateProfileFromExternalPages {

	private String authcode;

	public DeleteCandidateProfileFromExternalPages() {
	}

	public DeleteCandidateProfileFromExternalPages(String authcode) {
		this.authcode = authcode;
	}

	public String getAuthcode() {
		return authcode;
	}

	public void setAuthcode(String authcode) {
		this.authcode = authcode;
	}

}
