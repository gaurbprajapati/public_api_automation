package io.rcrm.api.javafaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.github.javafaker.Faker;

public class JavaFakerJob {

	Faker faker = new Faker(new Locale("fr"));

	public String getJobName() {
		// Generating the Job Name
		String jobName = faker.job().title() + " Job";
		return jobName;
	}

	public String getJobSeniority() {
		// Generating the Job Seniority
		String jobSeniority = faker.job().seniority();
		return jobSeniority;
	}

	public int getOpenings() {
		// Generating the no Of Openings
		int openings = faker.number().numberBetween(1, 50);
		return openings;
	}

	public String getJobDescriptionText() {
		// Generating the Job Description Text
		String jobDescriptionText = faker.lorem().paragraph(100);
		return jobDescriptionText;
	}

	public String getNoteForCandidate() {
		// Generating the note For Candidate
		String noteForCandidate = faker.matz().quote();
		return noteForCandidate;
	}

	public int getMinimumExperience() {
		// Generating the minimum Experience
		int miniExperience = faker.number().numberBetween(1, 10);
		return miniExperience ;
	}

	public int getMaximumExperience() {
		// Generating the Maximum Experience
		int MaxExperience = faker.number().numberBetween(11, 30);
		return MaxExperience ;
	}

	public int getMin_annual_salary() {
		// Generating the minimum annual salary
		int min_annual_salary = faker.number().numberBetween(500000, 1000000);
		return min_annual_salary;
	}

	public int getMax_annual_salary() {
		// Generating the Maximum annual salary
		int max_annual_salary = faker.number().numberBetween(1100000, 4000000);
		return max_annual_salary;
	}

	public String getJobCity() {
		// Generating the City
		String jobCity = faker.address().city();
		return jobCity;
	}

	public String getJobLocality() {
		// Generating the Locality
		String jobLocality = faker.address().streetAddress();
		return jobLocality;
	}

	public String getJobCountry() {
		// Generating the Country
		String jobCountry = faker.address().country();
		return jobCountry;
	}

	public String getJobState() {
		// Generating the state
		String jobState = faker.address().state();
		return jobState;
	}

	public String getJobFullAddress() {
		// Generating the full address
		String jobFullAddress = faker.address().fullAddress();
		return jobFullAddress;
	}

	public String getSpecialization() {
		// Generating the Specialization
		String specialization = faker.job().keySkills();
		return specialization;
	}

	public String getSalary_type() {
		// Generating the Salary type
		int salaryType = faker.number().numberBetween(1, 4);

		String salary_type = String.valueOf(salaryType);
		return salary_type;
	}

	public String getSalaryTypeAsString() {
		String[] salaryTypes = {"Annual Salary", "Monthly Salary", "Hourly Salary", "Daily Salary"};
		return salaryTypes[faker.number().numberBetween(0, 3)];
	}

	public int getCurrency_id() {
		// Generating the City
		int currencyId = faker.number().numberBetween(1, 130);
		return currencyId;
	}

	public String getPostalCode() {
		return String.valueOf(faker.number().numberBetween(100000,200000));
	}

	public int qualification_id() {
		// Generating the qualification id
		int qualification_id = faker.number().numberBetween(0, 7);
		return qualification_id;
	}

	public String getJob_status() {
		// generate a random number between 0 and 3
		int job_status = faker.number().numberBetween(0, 4);
		return String.valueOf(job_status);
	}

	public int getShow_company_logo() {
		return faker.number().numberBetween(0, 3);
	}

	public int getEnable_job_application_form() {
		return faker.number().numberBetween(0, 2);
	}

	public List<String> getCustomFields() {

		List<String> customFields = new ArrayList<>(2);

		for (int i = 0; i < 2; i++) {
			customFields.add(faker.matz().quote());
			int currencyId = faker.number().numberBetween(1000, 4000);

			String currency_id = String.valueOf(currencyId);
			customFields.add(currency_id);
		}
		return customFields;

	}

	public String getSkills() {
		String job_skill1 = faker.job().keySkills();
		String job_skill2 = faker.job().keySkills();
		String job_skill3 = faker.job().keySkills();
		String job_skill = job_skill1 + "," + job_skill2 + "," + job_skill3;
		return job_skill;
	}

	public int getJobType(){
		int type = faker.number().numberBetween(1, 3);
		return type;
	}

	public String getJobCategory(){
		String category = faker.job().title()+" category";
		return category;
	}

	public String getJobTitle(){
		String title = faker.job().title();
		return title;
	}

	public String getEducationQualification(){
		String qualification = faker.educator().course();
		return qualification;
	}

	public String getEducationSpecialization(){
		String specialization = faker.job().keySkills();
		return specialization;
	}

	public String getJobOwner(){
		String owner = faker.name().fullName();
		return owner;
	}

	public String getJobCustomField(String fieldType){
		return "Job Custom " + faker.number().numberBetween(0,9999) + fieldType;
	}

	public String getJobContent(String contact) {
		return String.join((CharSequence) " ",
				getJobTitle(),
				String.valueOf(getOpenings()),
				contact,
				getJobDescriptionText(),
				String.valueOf(getMaximumExperience()),
				String.valueOf(getMinimumExperience()),
				getSalary_type(),
				String.valueOf(getCurrency_id()),
				getEducationQualification(),
				getEducationSpecialization(),
				String.valueOf(getMin_annual_salary()),
				String.valueOf(getMax_annual_salary()),
				getSkills(),
				getJobCity(),
				getJobLocality(),
				getJobState(),
				getJobCountry(),
				getJobFullAddress(),
				getJobOwner(),
				getJobCustomField("Text"),
				getJobCustomField("Long Text"),
				getJobCustomField("Date"),
				getJobCustomField("Number"),
				getJobCustomField("Checkbox"),
				getJobCustomField("Dropdown"),
				getJobCustomField("Multiselect"),
				getJobCustomField("Phone Number"),
				getJobCustomField("Email Address"),
				getJobCustomField("File")
		);
	}
}
