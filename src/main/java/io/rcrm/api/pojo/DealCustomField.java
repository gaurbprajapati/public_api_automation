package io.rcrm.api.pojo;

import java.util.List;

public class DealCustomField {

	private String name;
	private String deal_stage;
	private int deal_value;
	private String close_date;
	private String deal_type;
	private String owner_id;
	private String reason;
	private List<CustomField> custom_fields;

	public DealCustomField() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeal_stage() {
		return deal_stage;
	}

	public void setDeal_stage(String deal_stage) {
		this.deal_stage = deal_stage;
	}

	public int getDeal_value() {
		return deal_value;
	}

	public void setDeal_value(int deal_value) {
		this.deal_value = deal_value;
	}

	public String getClose_date() {
		return close_date;
	}

	public void setClose_date(String close_date) {
		this.close_date = close_date;
	}

	public String getDeal_type() {
		return deal_type;
	}

	public void setDeal_type(String deal_type) {
		this.deal_type = deal_type;
	}

	public String getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(String owner_id) {
		this.owner_id = owner_id;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
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
