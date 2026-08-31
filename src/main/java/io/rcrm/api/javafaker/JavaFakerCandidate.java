package io.rcrm.api.javafaker;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.github.javafaker.Faker;

import io.rcrm.api.javafaker.neptune.JavaFakerSummary;

public class JavaFakerCandidate {

	// Locale locale = new Locale("en-IND");
	Faker faker = new Faker();

	public String getFirstName() {
		// Generating the first name
		String firstName = faker.name().firstName() + " Candidate";
		return firstName;
	}

	public String getLastName() {
		// Generating last name
		String lastName = faker.name().lastName();
		return lastName;
	}

	public String getEmailID() {
		// Generating email Id
		String emailId = faker.name().firstName() + "@yopmail.com";
		return emailId;
	}

	public String getContactNumber() {
		// Generating password
		String phoneNumber = faker.phoneNumber().cellPhone();
		return phoneNumber;
	}

	public int getGender_id() {
		// Generating gender id
		int gender_id = faker.number().numberBetween(1, 3);
		return gender_id;
	}

	public int getQualification_id() {
		// Generating the City
		int qualification_id = faker.number().numberBetween(1, 4);
		return qualification_id;
	}

	public String getSpecialization() {
		// Generating the City
		String specialization = faker.job().keySkills();
		return specialization;
	}

	public int getWork_ex_year() {
		// Generating the Work Experience Years
		int MaxExperience = faker.number().numberBetween(11, 30);
		return MaxExperience;
	}

	public String getDOB() {
		// Generating the DOB
		Date dateOfBirth1 = faker.date().past(9125, TimeUnit.DAYS);
		String dateOfBirth = String.valueOf(dateOfBirth1);
		return dateOfBirth;
	}

	public int getCurrent_salary() {
		// Generating the minimum Experience
		int salary = faker.number().numberBetween(500000, 1000000);
		return salary;
	}

	public int getSalary_expectation() {
		// Generating the Maximum Experience
		int max_annual_salary = faker.number().numberBetween(1100000, 4000000);
		return max_annual_salary;
	}

	public String getResume() {
		return "https://files-for-testing.s3-ap-southeast-1.amazonaws.com/avatar_4.jpg";
	}

	public String getLargeFileURL() {
		return "https://files-for-testing.s3-ap-southeast-1.amazonaws.com/file_20mb.pdf";
	}

	public String getResumeURL() {
		return "https://files-for-testing.s3-ap-southeast-1.amazonaws.com/Sandeep_resume.pdf";
	}

	public int getWilling_to_relocate() {
		// Generating Willing to Relocate number (0 or 1)
		return faker.number().numberBetween(0, 2);
	}

	public String getCurrentOrganization() {
		// Generating the CurrentOrganization
		String CurrentOrganization = faker.company().name();
		return CurrentOrganization;
	}

	public String getCurrentOrganizationSlug() {
		return faker.lorem().characters(10, true, true);
	}

	public String getCurrentEmploymentStatus() {
		// Generating the Current Employment Status
		String CurrentEmploymentStatus = faker.medical().diseaseName();
		return CurrentEmploymentStatus;
	}

	public int getNotice_period() {
		// Generating the notice_period
		int notice_period = faker.number().numberBetween(1, 999);
		return notice_period;
	}

	public int getCurrency_id() {
		// Generating the currencyId
		int currencyId = faker.number().numberBetween(1, 130);
		return currencyId;
	}

	public String getCandidateAvatarUrl() {
		// Get image urls
		int index = faker.number().numberBetween(1, 5);
		return "https://files-for-testing.s3-ap-southeast-1.amazonaws.com/avatar_"+index+".jpg";
	}

	public String getUrl() {
		// Generating the fb,Twitter,Github,Linkedin URL's
		String socialurl = "https://" + faker.internet().url();
		return socialurl;
	}

	public String getCity() {
		// Generating the City
		String city = faker.address().city();
		return city;
	}

	public String getLocality() {
		// Generating the Locality
		String locality = faker.address().streetAddress();
		return locality;
	}

	public String getCandidateAddress() {
		// Generating the Candidate Address
		String address = faker.address().fullAddress();
		return address;
	}

	public int getRelevant_experience() {
		// Generating the Relevant Experience
		int relevantExperience = faker.number().numberBetween(1, 10);
		return relevantExperience;
	}

	public String getPosition() {
		// Generating the position
		String position = faker.name().title();
		return position;
	}

	public String getAvailable_From() {
		// Generating the Available_From
		Date available_from1 = faker.date().future(10, TimeUnit.DAYS);
		String available_from = String.valueOf(available_from1);
		return available_from;
	}

	public String getSalary_type() {
		// Generating The Salary Type
		int salaryType = faker.number().numberBetween(1, 6);
		String salary_type = String.valueOf(salaryType);
		return salary_type;
	}

	public String getSource() {
		// Generating the Source
		String source = faker.address().state();
		return source;
	}

	public int getLanguage_id() {
		// Generating the Language Id
		int language_id = faker.number().numberBetween(1, 186);
		return language_id;
	}

	public int getLanguageProficiency_id() {
		// Generating the Language Proficiency Id
		int language_proficiency_id = faker.number().numberBetween(1, 7);
		return language_proficiency_id;
	}

	public String getSkills() {
		// Generating the skills
		String skill1 = faker.job().keySkills();
		String skill2 = faker.job().keySkills();
		String skill3 = faker.job().keySkills();
		String skills = skill1 + "," + skill2 + "," + skill3 + ",";
		return skills;
	}

	public String candidateSummary() {
		JavaFakerSummary javaFakerSummary = new JavaFakerSummary();
		return javaFakerSummary.getPromptText();
	}

	public String getState() {
		String state = faker.address().state();
		return state;
	}

	public String getCountry() {
		String country = faker.address().country();
		if (country.length() > 50) {
			return getCountry();
		}
		return country;
	}

	public String getWorkCompanyName() {
		String work_company_name = faker.company().name() + " Company";
		return work_company_name;
	}

	public String getWorkLocation() {
		String work_location = faker.address().city();
		return work_location;
	}

	public int getStartDate() {
		Date work_start_date1 = faker.date().past(30, TimeUnit.DAYS);
		return (int) work_start_date1.getTime();
	}

	public int getEndDateWithReferenceDate(int endDate) {
		Date startDate = new Date(endDate);
		Date endDate1 = faker.date().future(15, TimeUnit.DAYS, startDate);
		return (int) endDate1.getTime();
	}

	public String getDescription() {
		return faker.matz().quote() + "<br><br>" + faker.lorem().paragraph(1);
	}

	public int currentlyWorking() {
		return faker.number().numberBetween(0, 2);
	}

	public int getSalary() {
		return faker.number().numberBetween(0, 2147483647);
	}

	public String getJobTitle() {
		return faker.job().title();
	}

	public int getEmploymentType() {
		return faker.number().numberBetween(1, 3);
	}

	public int getIndustryId() {
		return faker.number().numberBetween(1, 100);
	}

	public String getInstituteName() {
		return faker.educator().university();
	}

	public String getEducationalQualification() {
		return faker.educator().course();
	}

	public String getGrade() {
		return faker.regexify("[A-D][+-]?|F");
	}

	public String getEducationLocation() {
		return faker.address().cityName();
	}

	public String getFileName() {
		return faker.file().fileName();
	}

	public String getCandidateSummary() {
		return faker.lorem().paragraph();
	}
	/*
	 * Misisng field to add "gender_id": 0, "willing_to_relocate": 0, "resume":
	 * null, "source": null,
	 * 
	 * "language_skills": null, "custom_fields": [ { "field_id": 1, "entity_type":
	 * "candidate", "field_name": "Candidate text Custom 15", "field_type": "text",
	 * "value": null },
	 */

	public String getInvalidCandidateSlug() {
		return faker.lorem().characters(15);
	}

	public boolean getRandomToggleState() {
		return faker.random().nextBoolean();
	}

	public int getValidEntityTypeId() {
		int[] enitityTypeIds = { 2, 3, 5 };
		return enitityTypeIds[faker.random().nextInt(enitityTypeIds.length)];
	}

	public int getInvalidEntityTypeId() {
		return faker.random().nextInt(10, 99);
	}

	public String getCustomFieldName(String fieldName) {
		return fieldName + faker.number().numberBetween(0, 9) + " Custom Field";
	}

	public String getCustomFieldValue(String fieldName) {
		return fieldName + faker.number().numberBetween(0, 9) + " Custom Value";

	}

	public String getProfilePictureUrl() {
		int width = 200;  // Width of the image
		int height = 200; // Height of the image
		String category = "people"; // Category of the image
		return "https://placeimg.com/" + width + "/" + height + "/" + category + "?random=" + faker.number().randomNumber();
	}

	public String getCollegeName() {
		return faker.university().name();
	}

	public String getCandidateFacebookURL() {
		return "https://www.facebook.com/" + getFirstName();
	}

	public String getCandidateTwitterURL() {
		return "https://www.twitter.com/" + getFirstName();
	}

	public String getCandidateLinkedinURL() {
		return "https://www.linkedin.com/" + getFirstName();
	}

	public String getCandidateGithubURL() {
		return "https://www.github.com/" + getFirstName();
	}

	public String getCandidateXingURL() {
		return "https://www.xing.com/" + getFirstName();
	}

	public String getRandomId() {
		return "000" + faker.number().digits(3);
	}

	public String normalizeHtmlLineBreaks(String text) {
        return text.replaceAll("(?i)<br\\s*/?>", "<br>");
    }

	public String getCandidateContent(String selected_candidates_ids) {
		return String.join(" ",
				getFirstName() + " " + getLastName(),
				getFirstName(),
				getLastName(),
				getProfilePictureUrl(),
				getEmailID(),
				getContactNumber(),
				String.valueOf(getGender_id()),
				getJobTitle(),
				getWorkCompanyName(),
				String.valueOf(getEmploymentType()),
				String.valueOf(getSalary()),
				String.valueOf(getIndustryId()),
				String.valueOf(getStartDate()),
				String.valueOf(getEndDateWithReferenceDate(getStartDate())),
				getWorkLocation(),
				getDescription(),
				getCollegeName(),
				getEducationalQualification(),
				getSpecialization(),
				getGrade(),
				String.valueOf(getStartDate()),
				String.valueOf(getEndDateWithReferenceDate(getStartDate())),
				getEducationLocation(),
				getDescription(),
				getDOB(),
				getCity(),
				getLocality(),
				getState(),
				getCountry(),
				getCandidateAddress(),
				getPosition(),
				String.valueOf(getWilling_to_relocate()),
				getCurrentOrganization(),
				selected_candidates_ids,
				getCandidateSummary(),
				getCandidateFacebookURL(),
				getCandidateTwitterURL(),
				getCandidateLinkedinURL(),
				getCandidateGithubURL(),
				getCandidateXingURL(),
				getEducationalQualification(),
				getSpecialization(),
				String.valueOf(getRelevant_experience()),
				getSalary_type(),
				String.valueOf(getCurrency_id()),
				String.valueOf(getCurrent_salary()),
				String.valueOf(getSalary_expectation()),
				getCurrentEmploymentStatus(),
				String.valueOf(getNotice_period()),
				getAvailable_From(),
				getSkills(),
				String.valueOf(getLanguageProficiency_id()),
				getSource(),
				getCustomFieldName("Text"),
				getCustomFieldName("Long Text"),
				getCustomFieldName("Date"),
				String.valueOf(getCustomFieldName("Number")),
				getCustomFieldName("Checkbox"),
				getCustomFieldName("Dropdown"),
				getCustomFieldName("Multiselect"),
				getCustomFieldName("Phone Number"),
				getCustomFieldName("Email Address"),
				getCustomFieldName("File")
		);
	}

}
