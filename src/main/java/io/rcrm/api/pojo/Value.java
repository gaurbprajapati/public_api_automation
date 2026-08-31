package io.rcrm.api.pojo;

public class Value {

	private boolean candidate;
	private boolean company;
	private boolean contact;

	public Value() {
		super();
	}

	public Value(boolean candidate, boolean company, boolean contact) {
		this.candidate = candidate;
		this.company = company;
		this.contact = contact;
	}

	public boolean isCandidate() {
		return candidate;
	}

	public void setCandidate(boolean candidate) {
		this.candidate = candidate;
	}

	public boolean isCompany() {
		return company;
	}

	public void setCompany(boolean company) {
		this.company = company;
	}

	public boolean isContact() {
		return contact;
	}

	public void setContact(boolean contact) {
		this.contact = contact;
	}

}
