package io.rcrm.api.pojo;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

	private String first_name = "";
	private String last_name = "";
	private String email = "";
	private String contact_number = "";
	private int gender_id = 1;
	private int qualification_id = 0;
	private String specialization = "";
	private int work_ex_year = 0;
	private String candidate_dob = "";
	private int current_salary = 0;
	private int salary_expectation = 0;
	private String resume = "";
	private int willing_to_relocate = 0;
	private String current_organization = "";
	private String current_organization_slug = "";
	private String current_status = "";
	private int notice_period = 0;
	private int currency_id = 53;
	private String avatar = "";
	private String facebook = "";
	private String twitter = "";
	private String linkedin = "";
	private String github = "";
	private String xing = "";
	private String city = "";
	private String locality = "";
	private String address = "";
	private int relevant_experience = 0;
	private String position = "";
	private String available_from = "";
	private String salary_type = "1";
	private String source = "Default Value";
	// private LanguageSkills language_skills;
	private String skill = "";
	private String state = "";
	private String country = "";
	private String name;
	private String skills;
	private String title;
	private int total_experience;
	private String currency_symbol;
	private String employment_status;
	private String educational_qualification;
	private String educational_specialization;
	private ArrayList<WorkHistory> work_history;
	private ArrayList<EducationHistory> education_history;
	private String candidate_summary="";
	private List<CustomField> custom_fields;
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int owner_id;
	private int created_by;

	AcademicInformation academicInformation;


	public Candidate(String first_name, String last_name, String email, String contact_number, int gender_id,
			int qualification_id, String specialization, int work_ex_year, String candidate_dob, int current_salary,
			int salary_expectation, int willing_to_relocate, String current_organization, String current_status,
			int notice_period, int currency_id, String facebook, String twitter, String linkedin, String github,
			String xing, String city, String locality, String address, int relevant_experience, String position,
			String available_from, String salary_type, String source, String skill, String candidate_slug,
			String work_company_name, String title, int employment_type, int industry_id, String work_location,
			int is_currently_working, int work_start_date, int work_end_date, String work_description, int salary,String candidate_summary) {
		super();
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.contact_number = contact_number;
		this.gender_id = gender_id;
		this.qualification_id = qualification_id;
		this.specialization = specialization;
		this.work_ex_year = work_ex_year;
		this.candidate_dob = candidate_dob;
		this.current_salary = current_salary;
		this.salary_expectation = salary_expectation;
		this.willing_to_relocate = willing_to_relocate;
		this.current_organization = current_organization;
		this.current_status = current_status;
		this.notice_period = notice_period;
		this.currency_id = currency_id;
		this.facebook = facebook;
		this.twitter = twitter;
		this.linkedin = linkedin;
		this.github = github;
		this.xing = xing;
		this.city = city;
		this.locality = locality;
		this.address = address;
		this.relevant_experience = relevant_experience;
		this.position = position;
		this.available_from = available_from;
		this.salary_type = salary_type;
		this.source = source;
		this.skill = skill;
		this.candidate_summary=candidate_summary;
	}

	public Candidate(String first_name, String last_name, String email, String contact_number, int gender_id,
			int qualification_id, String specialization, int work_ex_year, String candidate_dob, int current_salary,
			int salary_expectation, int willing_to_relocate, String current_organization, String current_status,
			int notice_period, int currency_id, String facebook, String twitter, String linkedin, String github,
			String xing, String city, String locality, String address, String state, String country,
			int relevant_experience, String position, String available_from, String salary_type, String source,
			String skill) {
		super();
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.contact_number = contact_number;
		this.gender_id = gender_id;
		this.qualification_id = qualification_id;
		this.specialization = specialization;
		this.work_ex_year = work_ex_year;
		this.candidate_dob = candidate_dob;
		this.current_salary = current_salary;
		this.salary_expectation = salary_expectation;
		this.willing_to_relocate = willing_to_relocate;
		this.current_organization = current_organization;
		this.current_status = current_status;
		this.notice_period = notice_period;
		this.currency_id = currency_id;
		this.facebook = facebook;
		this.twitter = twitter;
		this.linkedin = linkedin;
		this.github = github;
		this.xing = xing;
		this.city = city;
		this.locality = locality;
		this.address = address;
		this.relevant_experience = relevant_experience;
		this.position = position;
		this.available_from = available_from;
		this.salary_type = salary_type;
		this.source = source;
		this.skill = skill;
		this.state = state;
		this.country = country;
	}

	public Candidate(String first_name, String last_name, String email, String contact_number) {

		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.contact_number = contact_number;

	}
	
	public Candidate(String first_name, String last_name, String email, String contact_number, String candidate_summary) {

		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.contact_number = contact_number;
		this.candidate_summary=candidate_summary;

	}

	public Candidate(String name, String skills, String title) {
		super();
		this.name = name;
		this.skills = skills;
		this.title = title;

	}

	public Candidate(String name, String skills, String title, String current_organization, int total_experience,
			String city, String locality, int current_salary, int salary_expectation, String employment_status,
			String educational_qualification, String educational_specialization, ArrayList<WorkHistory> work_history,
			ArrayList<EducationHistory> education_history) {
		super();
		this.name = name;
		this.skills = skills;
		this.title = title;
		this.current_organization = current_organization;
		this.total_experience = total_experience;
		this.city = city;
		this.locality = locality;
		this.current_salary = current_salary;
		this.salary_expectation = salary_expectation;
		this.employment_status = employment_status;
		this.educational_qualification = educational_qualification;
		this.educational_specialization = educational_specialization;
		this.work_history = work_history;
		this.education_history = education_history;

	}

	// Personal Information
	public Candidate(String first_name, String last_name, String email, String contact_number, int gender_id,
			String candidate_dob, int willing_to_relocate, String city, String locality, String address) {
		super();
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.contact_number = contact_number;
		this.gender_id = gender_id;
		this.candidate_dob = candidate_dob;
		this.willing_to_relocate = willing_to_relocate;
		this.city = city;
		this.locality = locality;
		this.address = address;
	}

	public Candidate(String first_name, String last_name, String email, String contact_number, int gender_id,
			String candidate_dob, int willing_to_relocate, String city, String locality, String address, String state,
			String country,String candidate_summary) {
		super();
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.contact_number = contact_number;
		this.gender_id = gender_id;
		this.candidate_dob = candidate_dob;
		this.willing_to_relocate = willing_to_relocate;
		this.city = city;
		this.locality = locality;
		this.address = address;
		this.state = state;
		this.country = country;
		this.candidate_summary=candidate_summary;
	}

	// Academic Information
	public Candidate(int qualification_id, String specialization) {
		super();
		this.qualification_id = qualification_id;
		this.specialization = specialization;
	}
	public Candidate(String first_name, String last_name) {
		super();
		this.first_name = first_name;
		this.last_name = last_name;
	}


	// Employment Information

	public Candidate(int current_salary, int salary_expectation, String current_organization, String current_status,
			int notice_period, int currency_id, int relevant_experience, String position, String available_from,
			String salary_type) {
		super();
		this.current_salary = current_salary;
		this.salary_expectation = salary_expectation;
		this.current_organization = current_organization;
		this.current_status = current_status;
		this.notice_period = notice_period;
		this.currency_id = currency_id;
		this.relevant_experience = relevant_experience;
		this.position = position;
		this.available_from = available_from;
		this.salary_type = salary_type;
	}

	public Candidate(int work_ex_year, int current_salary, int salary_expectation, String current_organization,
			String current_status, int notice_period, int currency_id, int relevant_experience, String position,
			String available_from, String salary_type) {
		super();
		this.work_ex_year = work_ex_year;
		this.current_salary = current_salary;
		this.salary_expectation = salary_expectation;
		this.current_organization = current_organization;
		this.current_status = current_status;
		this.notice_period = notice_period;
		this.currency_id = currency_id;
		this.relevant_experience = relevant_experience;
		this.position = position;
		this.available_from = available_from;
		this.salary_type = salary_type;
	}

	public Candidate(String first_name, String last_name, int owner_id, int created_by) {
		super();
		this.first_name = first_name;
		this.last_name = last_name;
		this.owner_id = owner_id;
		this.created_by = created_by;
	}

	// Custom setter methods for backward compatibility
	public void setSalaryType(String salary_type) {
		this.salary_type = salary_type;
	}
	
	public void setCandidateSummary(String candidate_summary) {
		this.candidate_summary = candidate_summary;
	}
	
	public void setGenderId(int gender_id) {
		this.gender_id = gender_id;
	}
	
	public void setWillingToRelocate(int willing_to_relocate) {
		this.willing_to_relocate = willing_to_relocate;
	}

}
