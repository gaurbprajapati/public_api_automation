package io.rcrm.api.pojo.offlimit;

public class MarkCompanyAsAvailable {

	public MarkCompanyAsAvailable() {
	}

	private String company_slugs;
	private boolean mark_contact_available;
	private boolean mark_candidate_available;


	public String getCompany_slugs() {
		return company_slugs;
	}

	public void setCompany_slugs(String company_slugs) {
		this.company_slugs = company_slugs;
	}

	public boolean getMark_contact_available() {
		return mark_contact_available;
	}

	public void setMark_contact_available(boolean mark_contact_available) {
		this.mark_contact_available = mark_contact_available;
	}

	public boolean getMark_candidate_available() {
		return mark_candidate_available;
	}

	public void setMark_candidate_available(boolean mark_candidate_available) {
		this.mark_candidate_available = mark_candidate_available;
	}
}