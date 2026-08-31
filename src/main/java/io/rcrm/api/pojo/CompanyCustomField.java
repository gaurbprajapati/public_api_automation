package io.rcrm.api.pojo;

import java.util.List;

public class CompanyCustomField {

	private String company_name;
	private String website;
	private String contact_number;
	private int industry_id = 2;
	private String about_company;

	private List<CustomField> custom_fields;

	public CompanyCustomField() {
	}

	public String getCompany_name() {
		return company_name;
	}

	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getContact_number() {
		return contact_number;
	}

	public void setContact_number(String contact_number) {
		this.contact_number = contact_number;
	}

	public int getIndustry_id() {
		return industry_id;
	}

	public void setIndustry_id(int industry_id) {
		this.industry_id = industry_id;
	}

	public String getAbout_company() {
		return about_company;
	}

	public void setAbout_company(String about_company) {
		this.about_company = about_company;
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