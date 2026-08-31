package io.rcrm.api.candidate;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.*;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.EducationHistory;
import io.rcrm.api.pojo.HiringStage;
import io.rcrm.api.pojo.WorkHistory;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class AllEndPointsOfCandidatesTest extends TestBase {

	public AllEndPointsOfCandidatesTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();

	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	// String CandidateEmail = "rcrmtest0@gmail.com";
	String CandidateEmail = fakerCandidate.getEmailID();
	String CandidateNumber = fakerCandidate.getContactNumber();
	int CandidateGender = fakerCandidate.getGender_id();
	int qualificatioId = fakerCandidate.getQualification_id();
	String specialization = fakerCandidate.getSpecialization();
	int work_ex_year = fakerCandidate.getWork_ex_year();
	String dob = fakerCandidate.getDOB();
	int current_salary = fakerCandidate.getCurrent_salary();
	int salary_expectation = fakerCandidate.getSalary_expectation();
	String resumeUrl = fakerCandidate.getResume();
	int willing_to_relocate = fakerCandidate.getWilling_to_relocate();
	String current_organization = fakerCandidate.getCurrentOrganization();
	String current_status = fakerCandidate.getCurrentEmploymentStatus();
	int notice_period = fakerCandidate.getNotice_period();
	int currency_id = fakerCandidate.getCurrency_id();
	String avatar = fakerCandidate.getCandidateAvatarUrl();
	String socialUrl = fakerCandidate.getUrl();
	String city = fakerCandidate.getCity();
	String locality = fakerCandidate.getLocality();
	String Address = fakerCandidate.getCandidateAddress();
	int relevant_experience = fakerCandidate.getRelevant_experience();
	String position = fakerCandidate.getPosition();
	String availableFrom = fakerCandidate.getAvailable_From();
	String salaryType = fakerCandidate.getSalary_type();
	String source = fakerCandidate.getSource();

	//adding state and country fields
	String state = fakerCandidate.getState();
	String country = fakerCandidate.getCountry();
	
	String candidate_slug = "";
	String work_company_name = fakerCandidate.getWorkCompanyName();
	String title = fakerCandidate.getJobTitle();
	int employment_type = fakerCandidate.getEmploymentType();
	int industry_id = fakerCandidate.getIndustryId();
	String work_location = fakerCandidate.getWorkLocation();
	int is_currently_working = fakerCandidate.currentlyWorking();
	int work_start_date = fakerCandidate.getStartDate();
	int work_end_date = fakerCandidate.getEndDateWithReferenceDate(work_start_date);
	String work_description = fakerCandidate.getDescription();
	int salary = fakerCandidate.getSalary();
	String institute_name = fakerCandidate.getInstituteName();
	String educational_qualification = fakerCandidate.getEducationalQualification();
	String educational_specialization = fakerCandidate.getSpecialization();
	String grade = fakerCandidate.getGrade();
	String education_location = fakerCandidate.getEducationLocation();
	int education_start_date = fakerCandidate.getStartDate();
	int education_end_date = fakerCandidate.getEndDateWithReferenceDate(education_start_date);
	String education_description = fakerCandidate.getDescription();
	// Not able to enter language skills via api
	// int language_id = fakerCandidate.getLanguage_id();
	// int language_proficiency_id = fakerCandidate.getLanguageProficiency_id();
	// LanguageSkills languageSkills = new
	// LanguageSkills(language_id,language_proficiency_id);
	String skills = fakerCandidate.getSkills();

	String slug = "";
	String educationId = "";
	String workId = "";
	String jobSlug;
	JsonPath candJson;
	Map<String, String> authTokenMap = null;
	Candidate candidate;
	WorkHistory workHistory;
	EducationHistory educationHistory;
	commanFunction function = new commanFunction();

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void createNewCandidate() {
		JsonPath company = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String current_organization_slug = company.getString("slug");

		candidate = new Candidate();

		// Assign all values to candidate object
		candidate.setFirst_name(CandidateFirstName);
		candidate.setLast_name(CandidateLastName);
		candidate.setEmail(CandidateEmail);
		candidate.setContact_number(CandidateNumber);
		candidate.setGenderId(CandidateGender);
		candidate.setQualification_id(qualificatioId);
		candidate.setSpecialization(specialization);
		candidate.setWork_ex_year(work_ex_year);
		candidate.setCandidate_dob(dob);
		candidate.setCurrent_salary(current_salary);
		candidate.setSalary_expectation(salary_expectation);
		candidate.setResume(resumeUrl);
		candidate.setWillingToRelocate(willing_to_relocate);
		candidate.setCurrent_organization(current_organization);
		candidate.setCurrent_organization_slug(current_organization_slug);
		candidate.setCurrent_status(current_status);
		candidate.setNotice_period(notice_period);
		candidate.setCurrency_id(currency_id);
		candidate.setAvatar(avatar);
		candidate.setFacebook(socialUrl);
		candidate.setTwitter(socialUrl);
		candidate.setLinkedin(socialUrl);
		candidate.setGithub(socialUrl);
		candidate.setXing(socialUrl);
		candidate.setCity(city);
		candidate.setLocality(locality);
		candidate.setAddress(Address);
		candidate.setRelevant_experience(relevant_experience);
		candidate.setPosition(position);
		candidate.setAvailable_from(availableFrom);
		candidate.setSalaryType(salaryType);
		candidate.setSource(source);
		// candidate.setLanguage_skills(languageSkills);
		candidate.setSkill(skills);
		candidate.setState(state);
		candidate.setCountry(country);

		// Create the candidate
		Response response1 = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		Assert.assertEquals(response1.getStatusCode(), 200);
		validateCommunicationFields(response1, "");
		response1.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//createCandidate.json"));

		// 4. get the response body:
		String responseBody = response1.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		candJson = response1.jsonPath();

		slug = candJson.get("slug");
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void verifyValidationOfCreateNewCandidate() {

		Candidate candidate = new Candidate(" ", " ", "", " ");

		Response response1 = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);


		// Verify Response using Assertion and Jsonpath
		// JsonPath jp = response1.jsonPath();
		Assert.assertEquals(response1.getStatusCode(), 422);

		// slug = jp.get("slug");
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "createNewCandidate", groups = "nightly-build")
	public void searchCandidates_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("first_name", CandidateFirstName);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//searchForCandidates.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "createNewCandidate", groups = "nightly-build")
	public void showAllcandidates_GET() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "3");
		Response response = RestClient.doGet("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Validate communication fields as null
		validateCommunicationFields(response, "data[0]");
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//getAllCandidates.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "createNewCandidate", groups = "nightly-build")
	public void editCandidateBySlug_POST() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}";

		// Here we can also use data provider.
		candidate.setFirst_name(CandidateFirstName + " Edited");
		candidate.setEmail("Edited_" + CandidateEmail);

		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				candidate);

		Assert.assertEquals(response1.getStatusCode(), 200);
		validateCommunicationFields(response1, "");
		response1.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//editCandidate.json"));

	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "createNewCandidate", groups = "nightly-build")
	public void assignCandidate_POST() {
		jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		response.prettyPrint();
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//assignCandidate.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "assignCandidate_POST", groups = "nightly-build")
	public void unassignCandidate_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}/unassign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Validate communication fields as null
		validateCommunicationFields(response, "");

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//unassignCandidate.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "unassignCandidate_POST", groups = "nightly-build")
	public void applyToJob_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}/apply";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//applyToJob.json"));

	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "createNewCandidate", groups = "nightly-build")
	public void searchCandidateBySlug_GET() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		// Validate communication fields as null
		validateCommunicationFields(response, "");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//findCandidateBySlug.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "applyToJob_POST", groups = "nightly-build")
	public void hiringStagesOfCandidate_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}/hiring-stages";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//hiringStagesOfCandidate.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "applyToJob_POST", groups = "nightly-build")
	public void hiringStagesOfCandidateForJob_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);
		pathParamters.put("job", jobSlug);

		String basePath = "candidates/{candidate}/hiring-stages/{job}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//hiringStagesOfCandidateForJob.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "applyToJob_POST", groups = "nightly-build")
	public void updateCandidateHiringStage_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);
		pathParamters.put("job", jobSlug);

		HiringStage newStage = new HiringStage();
		newStage.setStatus_id(8);
		newStage.setStage_date(fakerCandidate.getAvailable_From());
		newStage.setRemark(CandidateFirstName + " Test Remark");
		int userId = candJson.get("updated_by");
		newStage.setUpdated_by(userId);

		String basePath = "candidates/{candidate}/hiring-stages/{job}";

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				newStage);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//updateCandidateHiringStage.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "createNewCandidate", groups = "nightly-build")
	public void requestUpdateProfile_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}/request-update";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//requestUpdatedProfile.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "applyToJob_POST", groups = "nightly-build")
	public void updateCandidateVisibility_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);
		pathParamters.put("job", jobSlug);

		Map<String, String> params = new HashMap<String, String>();
		params.put("visibility", "0");
		String basePath = "candidates/{candidate}/visibility/{job}";

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), params, pathParamters, true,
				null);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//updateCandidateVisibilityInAJob.json"));
	}

	@Owner("Rahul Shibu")
	@Test(dependsOnMethods = "applyToJob_POST", groups = "nightly-build")
	public void returnAllHiringStagesOfCandidateForJobs_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}/history";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(
				matchesJsonSchemaInClasspath("publicApi//candidate//returnAllHiringStagesOfCandidateForJobs.json"));
	}

	@Owner("Rahul Shibu")
	@Test(priority = 10, groups = "nightly-build")
	public void deleteCandidate_DELETE() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCandidate", groups = "nightly-build")
	public void addWorkExperience_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		workHistory = new WorkHistory();
		String basePath = "candidates/work-history/create";
		workHistory.setCandidate_slug(slug);
		workHistory.setTitle(title);
		workHistory.setWork_company_name(work_company_name);
		workHistory.setEmployment_type(employment_type);
		workHistory.setIndustry_id(industry_id);
		workHistory.setWork_location(work_location);
		workHistory.setSalary(salary);
		workHistory.setWork_start_date(work_start_date);
		workHistory.setWork_end_date(work_end_date);
		workHistory.setWork_description(work_description);

		// Converting Object t
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(workHistory);
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				jsonArray);
		Assert.assertEquals(response1.getStatusCode(), 200);
		// Schema getting checked
		response1.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//addWorkExperience.json"));
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = { "createNewCandidate", "addWorkExperience_POST" }, groups = "nightly-build")

	public void getCandidateWorkExperience_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}/work-history";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);
		JsonPath jp = response.jsonPath();
		workId = jp.getString("id");
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//getWorkExperience.json"));
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = { "createNewCandidate", "addWorkExperience_POST", "getCandidateWorkExperience_GET" }, groups = "nightly-build")
	public void editWorkExperience_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String regex = "[^a-zA-Z0-9\\s]";
		String replaced = workId.replaceAll(regex, "");
		pathParamters.put("workId", replaced);

		String basePath = "candidates/work-history/{workId}";
		String workCompanyNameEdited = "Recruit CRM" + work_company_name;

		WorkHistory workHistoryObj = new WorkHistory(slug, workCompanyNameEdited, title);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				workHistoryObj);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//editWorkExperience.json"));
// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(jp.get("work_company_name"), workCompanyNameEdited, workCompanyNameEdited);

	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = { "createNewCandidate", "addWorkExperience_POST", "getCandidateWorkExperience_GET" }, groups = "nightly-build")
	public void deleteWorkExperience_DELETE() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String regex = "[^a-zA-Z0-9\\s]";
		String replaced = workId.replaceAll(regex, "");
		pathParamters.put("workId", replaced);

		String basePath = "candidates/work-history/{workId}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//deleteWorkExperience.json"));
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCandidate", groups = "nightly-build")
	public void addEducationHistory_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		educationHistory = new EducationHistory();
		String basePath = "candidates/education-history/create";
		educationHistory.setCandidate_slug(slug);
		educationHistory.setInstitute_name(institute_name);
		educationHistory.setEducational_qualification(educational_qualification);
		educationHistory.setEducational_specialization(educational_specialization);
		educationHistory.setGrade(grade);
		educationHistory.setEducation_location(education_location);
		educationHistory.setEducation_start_date(education_start_date);
		educationHistory.setEducation_end_date(education_end_date);
		educationHistory.setEducation_description(education_description);
		// Converting Object t
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(educationHistory);
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				jsonArray);
		Assert.assertEquals(response1.getStatusCode(), 200);
		// Schema getting checked
		response1.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//addEducationHistory.json"));
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = { "createNewCandidate", "addEducationHistory_POST" }, groups = "nightly-build")

	public void getCandidateEducationHistory() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}/education-history";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);
		JsonPath jp = response.jsonPath();
		educationId = jp.getString("id");
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//getEducationHistory.json"));
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = { "createNewCandidate", "addEducationHistory_POST", "getCandidateEducationHistory" }, groups = "nightly-build")
	public void editEducationHistoryVerify_POST() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String regex = "[^a-zA-Z0-9\\s]";
		String replaced = educationId.replaceAll(regex, "");
		pathParamters.put("educationId", replaced);

		String basePath = "candidates/education-history/{educationId}}";
		String newQualification = "MS in CS";
		EducationHistory educationHistoryObj = new EducationHistory(slug, newQualification);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				educationHistoryObj);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//editEducationHistory.json"));
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(jp.get("educational_qualification"), newQualification, newQualification);

	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = { "createNewCandidate", "addEducationHistory_POST", "getCandidateEducationHistory" }, groups = "nightly-build")
	public void deleteEducationHistory_Delete() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String regex = "[^a-zA-Z0-9\\s]";
		String replaced = educationId.replaceAll(regex, "");
		pathParamters.put("educationId", replaced);

		String basePath = "candidates/education-history/{educationId}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//candidate//deleteEducationHistory.json"));
	}


	private void validateCommunicationFields(Response response, String dataPath) {
		String pathPrefix = dataPath.isEmpty() ? "$" : dataPath;
		List<String> allFields = getAllCommunicationFields();
		for (String field : allFields) {
			response.then().body(pathPrefix, Matchers.hasKey(field));
			response.then().body(pathPrefix + "." + field, Matchers.nullValue());
		}
	}

	private List<String> getAllCommunicationFields() {
		return Arrays.asList(
				"last_calllog_added_on",
				"last_calllog_added_by",
				"last_email_sent_on",
				"last_email_sent_by",
				"last_sms_sent_on",
				"last_sms_sent_by",
				"last_meeting_created_on",
				"last_meeting_created_by",
				"last_linkedin_message_sent_on",
				"last_linkedin_message_sent_by",
				"last_communication"
		);
	}

}
