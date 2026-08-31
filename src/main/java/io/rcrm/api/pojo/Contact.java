package io.rcrm.api.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

	private String first_name;
	private String last_name;
	private String email;
	private String contact_number;
	private String company_slug="";
	
	private String avatar;
	
	private String designation;
	private String city;
	private String locality;
	private String address;
	
	private String facebook="";
	private String twitter="";
	private String linkedin="";
	private String xing="";
	private String stage_id = "";
	private String reason;
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int owner_id;
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int created_by;

	// constructor
	public Contact(String first_name, String last_name, String email, String contact_number,String company_slug) {

		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.contact_number = contact_number;
		this.company_slug=company_slug;
		
	}
	
	// constructor
	public Contact(String first_name, String last_name, String email, String contact_number,String company_slug, int owner_id, int created_by) {
			this.first_name = first_name;
			this.last_name = last_name;
			this.email = email;
			this.contact_number = contact_number;
			this.company_slug=company_slug;
			this.owner_id = owner_id;
			this.created_by = created_by;
	}

}