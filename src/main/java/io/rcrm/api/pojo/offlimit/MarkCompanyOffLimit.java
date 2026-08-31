package io.rcrm.api.pojo.offlimit;

public class MarkCompanyOffLimit {

	public MarkCompanyOffLimit() {
	}

	private String company_slugs;
	private String status_id;
	private String end_date;
	private String reason;
	private boolean mark_candidate_off_limit;
	private boolean mark_contact_off_limit;

	public String getCompany_slugs() {
		return company_slugs;
	}

	public void setCompany_slugs(String company_slugs) {
		this.company_slugs = company_slugs;
	}

	public String getStatus_id() {
		return status_id;
	}

	public void setStatus_id(String status_id) {
		this.status_id = status_id;
	}

	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public boolean isMark_candidate_off_limit() {
		return mark_candidate_off_limit;
	}

	public void setMark_candidate_off_limit(boolean mark_candidate_off_limit) {
		this.mark_candidate_off_limit = mark_candidate_off_limit;
	}

	public boolean isMark_contact_off_limit() {
		return mark_contact_off_limit;
	}

	public void setMark_contact_off_limit(boolean mark_contact_off_limit) {
		this.mark_contact_off_limit = mark_contact_off_limit;
	}
}