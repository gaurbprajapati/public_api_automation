package io.rcrm.api.pojo.offlimit;

public class MarkContactOffLimit {

	public MarkContactOffLimit() {
	}

	private String contact_slugs;
	private String status_id;
	private String end_date;
	private String reason;
	
	public String getContact_slugs() {
		return contact_slugs;
	}

	public void setContact_slugs(String contact_slugs) {
		this.contact_slugs = contact_slugs;
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
}