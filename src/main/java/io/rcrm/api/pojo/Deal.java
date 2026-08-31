package io.rcrm.api.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deal {

	private String name;
	private String deal_stage;
	private int deal_value;
	private String close_date;
	private String deal_type;
	private String company_slug = "";
	private String job_slug = "";
	private String contact_slugs="";

	private String candidate_slug="";
	//private String number_of_openings;
	private DealSplit deal_split;
	private String owner_id;
	private String reason;
	private int created_by;

	public Deal(String name, String deal_stage, int deal_value, String close_date, String deal_type, String company_slug, String job_slug, String contact_slugs, int owner_id, int created_by) {
		this.name = name;
		this.deal_stage = deal_stage;
		this.deal_value = deal_value;
		this.close_date = close_date;
		this.deal_type = deal_type;
		this.company_slug = company_slug;
		this.job_slug = job_slug;
		this.contact_slugs = contact_slugs;
		this.owner_id = String.valueOf(owner_id);
		this.created_by = created_by;
	}
}
