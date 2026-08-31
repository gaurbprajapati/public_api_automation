package io.rcrm.api.pojo;

public class EducationHistory {
	private String candidate_slug = "";
	private String institute_name = "";
	private String educational_qualification = "";
	private String educational_specialization = "";
	private String grade = "";
	private String education_location = "";
	private int education_start_date = 0;
	private int education_end_date = 0;
	private String education_description = "";
	private String title;
	private String work_company_name;
	private String work_location;

	public EducationHistory() {

	}

	public EducationHistory(String candidate_slug, String institute_name, String educational_qualification,
			String grade, String education_location, int education_start_date, int education_end_date,
			String education_description) {
		super();
		this.candidate_slug = candidate_slug;
		this.institute_name = institute_name;
		this.educational_qualification = educational_qualification;
		this.grade = grade;
		this.education_location = education_location;
		this.education_start_date = education_start_date;
		this.education_end_date = education_end_date;
		this.education_description = education_description;
	}
	
	public EducationHistory(String title, String work_company_name, String work_location) {
		super();
		this.title = title;
		this.work_company_name = work_company_name;
		this.work_location = work_location;
	}

	
	public EducationHistory(String candidate_slug, String educational_qualification) {
		super();
		this.candidate_slug = candidate_slug;
		this.educational_qualification = educational_qualification;		
	}

	public String getCandidate_slug() {
		return candidate_slug;
	}

	public void setCandidate_slug(String candidate_slug) {
		this.candidate_slug = candidate_slug;
	}

	public String getInstitute_name() {
		return institute_name;
	}

	public void setInstitute_name(String institute_name) {
		this.institute_name = institute_name;
	}

	public String getEducational_qualification() {
		return educational_qualification;
	}

	public void setEducational_qualification(String educational_qualification) {
		this.educational_qualification = educational_qualification;
	}

	public String getEducational_specialization() {
		return educational_specialization;
	}

	public void setEducational_specialization(String educational_specialization) {
		this.educational_specialization = educational_specialization;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getEducation_location() {
		return education_location;
	}

	public void setEducation_location(String education_location) {
		this.education_location = education_location;
	}

	public int getEducation_start_date() {
		return education_start_date;
	}

	public void setEducation_start_date(int education_start_date) {
		this.education_start_date = education_start_date;
	}

	public int getEducation_end_date() {
		return education_end_date;
	}

	public void setEducation_end_date(int education_end_date) {
		this.education_end_date = education_end_date;
	}

	public String getEducation_description() {
		return education_description;
	}

	public void setEducation_description(String education_description) {
		this.education_description = education_description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getWork_company_name() {
		return work_company_name;
	}

	public void setWork_company_name(String work_company_name) {
		this.work_company_name = work_company_name;
	}

	public String getWork_location() {
		return work_location;
	}

	public void setWork_location(String work_location) {
		this.work_location = work_location;
	}

}
