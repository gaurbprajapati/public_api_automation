package io.rcrm.api.pojo;

public class CustomField {

	private Integer field_id;
	private String value;
	private String entity_type;
	private String field_name;
	private String field_type;

	public CustomField() {
		super();
	}

	public CustomField(Integer field_id, String value, String entity_type, String field_name, String field_type) {
		this.field_id = field_id;
		this.value = value;
		this.entity_type = entity_type;
		this.field_name = field_name;
		this.field_type = field_type;

	}

	public Integer getField_id() {
		return field_id;
	}

	public void setField_id(Integer field_id) {
		this.field_id = field_id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getEntity_type() {
		return entity_type;
	}

	public void setEntity_type(String entity_type) {
		this.entity_type = entity_type;
	}

	public String getField_name() {
		return field_name;
	}

	public void setField_name(String field_name) {
		this.field_name = field_name;
	}

	public String getField_type() {
		return field_type;
	}

	public void setField_type(String field_type) {
		this.field_type = field_type;
	}

}
