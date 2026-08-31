package io.rcrm.api.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

	/*
	 * company_name field is required to create a new company
	 */
	private String company_name;
	private String website;
	private String contact_number;
	private String logo;

	private String city;
	private String address;

	private String facebook = "";
	private String twitter = "";
	private String linkedin = "";

	private int industry_id = 2;
	
	private String about_company;
	private String contact_slug = "";
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int owner_id;
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int created_by;
	

	// constructor
	public Company(String company_name, String website, String contact_number, String logo) {

		this.company_name = company_name;
		this.website = website;
		this.contact_number = contact_number;
		this.logo = logo;
	}

	public Company(String company_name, String website, String contact_number, String logo, int industry_id, int owner_id, int created_by) {
		this.company_name = company_name;
		this.website = website;
		this.contact_number = contact_number;
		this.logo = logo;
		this.owner_id = owner_id;
		this.created_by = created_by;
		this.industry_id = industry_id;
	}

	public Company(String company_name, String website, String contact_number, int owner_id, int created_by) {
		this.company_name = company_name;
		this.website = website;
		this.contact_number = contact_number;
		this.owner_id = owner_id;
		this.created_by = created_by;
	}


}
