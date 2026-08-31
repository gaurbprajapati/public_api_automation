package io.rcrm.api.pojo.auditLog;

public class AuditLogActionKey {
	private String action_key;
	private AuditLogActionData action_data;
	
	public AuditLogActionKey() {
		super();
	}

	public AuditLogActionKey(String action_key, AuditLogActionData action_data) {
		super();
		this.action_key = action_key;
		this.action_data = action_data;
	}
	
	public String getAction_key() {
		return action_key;
	}
	public void setAction_key(String action_key) {
		this.action_key = action_key;
	}
	public AuditLogActionData getAction_data() {
		return action_data;
	}
	public void setAction_data(AuditLogActionData action_data) {
		this.action_data = action_data;
	}
}
