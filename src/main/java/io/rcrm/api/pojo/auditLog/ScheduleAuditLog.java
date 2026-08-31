package io.rcrm.api.pojo.auditLog;

import java.util.List;

public class ScheduleAuditLog {

	private int interval_type;
	private List<String> recipients;

	public void setInterval_type(int intervalType) {
		this.interval_type = intervalType;
	}

	public void setRecipients(List<String> recipients) {
		this.recipients = recipients;
	}

	public List<String> getRecipients() {
		return recipients;
	}

	public int getInterval_type() {
		return interval_type;
	}
}