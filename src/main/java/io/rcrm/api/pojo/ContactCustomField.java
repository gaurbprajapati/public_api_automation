package io.rcrm.api.pojo;

import java.util.List;

public class ContactCustomField {

	private String first_name;
	private String last_name;
	private String email;
	private String contact_number;
	
	private List<CustomField> custom_fields;

	public ContactCustomField() {
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContact_number() {
		return contact_number;
	}

	public void setContact_number(String contact_number) {
		this.contact_number = contact_number;
	}
	
	public List<CustomField> getCustom_fields() {
		return custom_fields;
	}

	public void setCustom_fields(List<CustomField> custom_fields) {
		this.custom_fields = custom_fields;
	}
	
	public static class CustomField {
		private int field_id;
		private String value;

		public int getField_id() {
			return field_id;
		}

		public void setField_id(int field_id) {
			this.field_id = field_id;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

}