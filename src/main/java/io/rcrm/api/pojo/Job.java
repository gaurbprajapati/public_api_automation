package io.rcrm.api.pojo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

	private String slug = "";
	private String name = "";
	private String company_slug = "";
	private String contact_slug = "";
	private String secondary_contact_slugs = "";
	private String note_for_candidates = "";
	private int number_of_openings = 1;
	private int minimum_experience = 0;
	private int maximum_experience = 0;
	private int min_annual_salary = 0;
	private int max_annual_salary = 0;
	private String salary_type = "1";
	private String job_status = "1";
	private String latitude = "0";
	private String longitude = "0";
	private String city = "";
	private String locality = "";
	private String state = "";
	private String country = "";
	private String address = "";
	private int enable_job_application_form = 0;
	private int job_posting_status = 0;
	private int show_company_logo = 1;
	private String specialization = "";
	private int qualification_id = 0;
	private int currency_id = 53;
	private String job_description_text = "";
	private String job_description_file = "";
	private int hiring_pipeline_id = 0;
	private String job_skill = "";
	private int company_id;
	private String currency;
	private int minimum_salary;
	private int maximum_salary;
	private String educational_qualification;
	private String educational_specialization;
	private String fullAddress;
	private String skills;
	private int job_type = 1;
	private String job_category = "";
	private String collaborator_team_ids;

	private String job_questions = "";
	private String jobname;
	private String companyname;
	private String srno;
	private String id;
	private boolean checked;
	private String collaborator_user_ids;
	private int updated_by;
	private String postal_code;

	private List<CustomField> custom_fields = null;
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int owner_id;
	
	private int created_by;

	private XmlFeed xml_feeds;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Double pay_rate;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Double bill_rate;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer calculate_charge_by;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Double margin_percentage;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Double markup_percentage;

	// Custom setter methods for backward compatibility
	public void setJobCategory(String job_category) {
		this.job_category = job_category;
	}
	
	public void setSecondary_contact_slug(String secondary_contact_slugs) {
		this.secondary_contact_slugs = secondary_contact_slugs;
	}
	
	public Job(String name) {
		super();
		this.name = name;
	}

	public Job(String name, int number_of_openings, int minimum_experience, int maximum_experience, String salary_type,
			int maximum_salary, int minimum_salary, String educational_specialization, String city, String locality,
			String state, String country, String fullAddress, String skills,int job_type, String job_category) {

		super();
		this.name = name;
		this.number_of_openings = number_of_openings;
		this.minimum_experience = minimum_experience;
		this.maximum_experience = maximum_experience;
		this.salary_type = salary_type;
		this.minimum_salary = minimum_salary;
		this.maximum_salary = maximum_salary;
		this.educational_specialization = educational_specialization;
		this.city = city;
		this.locality = locality;
		this.state = state;
		this.country = country;
		this.fullAddress = fullAddress;
		this.skills = skills;
		this.job_type = job_type;
		this.job_category = job_category;
	}

	// constructor
	public Job(String name, String company_slug, String contact_slug, int number_of_openings,
			int enable_job_application_form) {

		this.name = name;
		this.company_slug = company_slug;
		this.contact_slug = contact_slug;
		this.number_of_openings = number_of_openings;
		this.enable_job_application_form = enable_job_application_form;

	}

	public Job(String name, String company_slug, String contact_slug, String note_for_candidates,
			int number_of_openings, int minimum_experience, int maximum_experience, int min_annual_salary,
			int max_annual_salary, String city, String locality, String country, String state, String address,
			int enable_job_application_form, String specialization, String salary_type, String job_description_text,
			int currency_id, int qualification_id, String job_skill) {
		super();
		this.name = name;
		this.company_slug = company_slug;
		this.contact_slug = contact_slug;
		this.note_for_candidates = note_for_candidates;
		this.number_of_openings = number_of_openings;
		this.minimum_experience = minimum_experience;
		this.maximum_experience = maximum_experience;
		this.min_annual_salary = min_annual_salary;
		this.max_annual_salary = max_annual_salary;
		this.city = city;
		this.locality = locality;
		this.country = country;
		this.state = state;
		this.address = address;
		this.enable_job_application_form = enable_job_application_form;
		this.specialization = specialization;
		this.salary_type = salary_type;
		this.job_description_text = job_description_text;
		this.currency_id = currency_id;
		this.qualification_id = qualification_id;
		this.job_skill = job_skill;
	}

	public Job(String name, String company_slug, String contact_slug, String note_for_candidates,
			int number_of_openings, int minimum_experience, int maximum_experience, int min_annual_salary,
			int max_annual_salary, String city, String locality, String country, String state, String address,
			int enable_job_application_form, String specialization, String salary_type, String job_description_text,
			int currency_id, int qualification_id, String job_skill,int job_type, String job_category) {
		super();
		this.name = name;
		this.company_slug = company_slug;
		this.contact_slug = contact_slug;
		this.note_for_candidates = note_for_candidates;
		this.number_of_openings = number_of_openings;
		this.minimum_experience = minimum_experience;
		this.maximum_experience = maximum_experience;
		this.min_annual_salary = min_annual_salary;
		this.max_annual_salary = max_annual_salary;
		this.city = city;
		this.locality = locality;
		this.country = country;
		this.state = state;
		this.address = address;
		this.enable_job_application_form = enable_job_application_form;
		this.specialization = specialization;
		this.salary_type = salary_type;
		this.job_description_text = job_description_text;
		this.currency_id = currency_id;
		this.qualification_id = qualification_id;
		this.job_skill = job_skill;
		this.job_type = job_type;
		this.job_category = job_category;
	}

	public Job(String jobname,String companyname,String srno,String city,String slug,String id,boolean checked) {
		this.jobname = jobname;
	    this.companyname = companyname;
	    this.srno = srno;
	    this.city = city;
	    this.slug = slug;
	    this.id = id;
	    this.checked = checked;
	}

	public Job(String name, String company_slug, String contact_slug, int owner_id, int created_by, String city, String job_description_text) {
		this.name = name;
		this.company_slug = company_slug;
		this.contact_slug = contact_slug;
		this.owner_id = owner_id;
		this.created_by = created_by;
		this.city = city;
		this.job_description_text = job_description_text;
	}
}