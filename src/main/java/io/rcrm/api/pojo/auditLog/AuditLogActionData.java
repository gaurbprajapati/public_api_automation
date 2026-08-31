package io.rcrm.api.pojo.auditLog;

public class AuditLogActionData {
	
	private String entity_name;
	private int entity_type_id;
	private String[] entity_slugs;
	
	public AuditLogActionData() {
		super();
	}
	
	public AuditLogActionData(String entity_name, int entity_type_id, String[] entity_slugs) {
		super();
		this.entity_name = entity_name;
		this.entity_type_id = entity_type_id;
		this.entity_slugs = entity_slugs;
	}

	public String getEntity_name() {
		return entity_name;
	}
	public void setEntity_name(String entity_name) {
		this.entity_name = entity_name;
	}
	public int getEntity_type_id() {
		return entity_type_id;
	}
	public void setEntity_type_id(int entity_type_id) {
		this.entity_type_id = entity_type_id;
	}
	public String[] getEntity_slugs() {
		return entity_slugs;
	}
	public void setEntity_slugs(String[] entity_slugs) {
		this.entity_slugs = entity_slugs;
	}
	
	
}
