package io.rcrm.api.candidate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.CustomField;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class CreateNewCandidateTest extends TestBase {

	String slug = "";

	public CreateNewCandidateTest() {
		super();
	}

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerJob jobFaker = new JavaFakerJob();
	commanFunction commanFunction = new commanFunction();

	// Personal Information
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
	String current_organization_slug = fakerCandidate.getCurrentOrganizationSlug();
	String current_status = fakerCandidate.getCurrentEmploymentStatus();
	int notice_period = fakerCandidate.getNotice_period();
	int currency_id = fakerCandidate.getCurrency_id();
	String avatar = fakerCandidate.getCandidateAvatarUrl();
	String socialUrl = fakerCandidate.getUrl();
	String city = fakerCandidate.getCity();
	String locality = fakerCandidate.getLocality();
	String Address = fakerCandidate.getCandidateAddress();
	String state = fakerCandidate.getState();
	String country = fakerCandidate.getCountry();
	int relevant_experience = fakerCandidate.getRelevant_experience();
	String position = fakerCandidate.getPosition();
	String availableFrom = fakerCandidate.getAvailable_From();
	String salarytype = fakerCandidate.getSalary_type();
	String source = fakerCandidate.getSource();
	String currentStatus = fakerCandidate.getCurrentEmploymentStatus();
	int noticePeriod = fakerCandidate.getNotice_period();
	String skills = fakerCandidate.getSkills();
	String candidateSummary=fakerCandidate.candidateSummary();
	String longText = jobFaker.getJobDescriptionText() + jobFaker.getNoteForCandidate();

	String publicApiKey;

	@BeforeClass(alwaysRun = true)	public void setUp(){
		publicApiKey = ThreadManager.getAccountApiKey();
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void createNewCandidateWithResume_POST() {
		Map<String, String> formsdata = new HashMap<String, String>();
		formsdata.put("first_name", CandidateFirstName);
		formsdata.put("email", CandidateEmail);

		Response response1 = RestClient.doPost("multipart", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, formsdata);

		Assert.assertEquals(response1.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response1.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response1.jsonPath();

		String filename = jp.get("resume.filename");

		String resumeFileURL = jp.get("resume.file_link");

		Assert.assertTrue(resumeFileURL.contains("resume"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewCandidateWithResume() {

		Candidate candidate = new Candidate();

		candidate.setFirst_name(CandidateFirstName);
		candidate.setLast_name(CandidateLastName);
		candidate.setResume(fakerCandidate.getResumeURL());

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);


		JsonPath jp = response.jsonPath();
		Assert.assertNotNull(jp.get("created_by"));
		Assert.assertNotNull(jp.get("updated_by"));

		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void createNewCandidateWithCustomFieldsValueAsEmpty() {
		List<CustomField> custom_fields = new ArrayList<>();
		Candidate candidate = new Candidate();

		candidate.setFirst_name(CandidateFirstName);
		candidate.setLast_name(CandidateLastName);
		candidate.setEmail(fakerCandidate.getEmailID());
		candidate.setContact_number(fakerCandidate.getContactNumber());
		candidate.setCustom_fields(custom_fields);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		JsonPath jp = response.jsonPath();
		Assert.assertNotNull(jp.get("created_by"), "'created_by' field should not be null");
		Assert.assertNotNull(jp.get("updated_by"), "'updated_by' field should not be null");

		Assert.assertEquals(response.getStatusCode(), 200, "Expected HTTP status code 200, but got: " + response.getStatusCode());
	}

	@Owner("Gaurav Prajapati")
	@Test(description = "Add new candidate with all fields and verify all fields data", groups = "nightly-build")
	public void CreateNewCandidateWithAllFields() {

		// Personal Information
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber,
				CandidateGender, dob, willing_to_relocate, city, locality, Address, state, country,candidateSummary);

		// Academic Information
		candidate.setQualification_id(qualificatioId);
		candidate.setSpecialization(specialization);

		// Employment Information
		candidate.setCurrent_organization(current_organization);
		candidate.setPosition(position);
		candidate.setWork_ex_year(work_ex_year);
		candidate.setRelevant_experience(relevant_experience);
		candidate.setSalaryType(salarytype);
		candidate.setCurrency_id(currency_id);
		candidate.setCurrent_salary(current_salary);
		candidate.setSalary_expectation(salary_expectation);
		candidate.setCurrent_status(currentStatus);
		candidate.setNotice_period(noticePeriod);
		candidate.setAvailable_from(availableFrom);
		candidate.setSource(source);
		candidate.setState(state);
		candidate.setCountry(country);


		// Skills
		candidate.setSkill(skills);

		// Social Links
		candidate.setFacebook(socialUrl);
		candidate.setTwitter(socialUrl);
		candidate.setLinkedin(socialUrl);
		candidate.setGithub(socialUrl);
		candidate.setXing(socialUrl);
		candidate.setAvatar(avatar);
		candidate.setResume(resumeUrl);
		
		candidate.setCandidateSummary(candidateSummary);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);


		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 200);

		String qualificatioId_String = String.valueOf(qualificatioId);

		// Verify all fields value
		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");
		Assert.assertEquals(CandidateGender + "", jp.get("gender_id").toString(), "gender_id");
		Assert.assertEquals(qualificatioId_String, jp.get("qualification_id").toString(), "qualificatioId");

		Assert.assertEquals(Address, jp.get("address").toString(), "Address");
		Assert.assertEquals(locality, jp.get("locality").toString(), "locality");
		Assert.assertEquals(city, jp.get("city").toString(), "city");
		Assert.assertEquals(willing_to_relocate + "", jp.get("willing_to_relocate").toString(), "willing_to_relocate");
		Assert.assertEquals(state, jp.get("state"),"state");
		Assert.assertEquals(country,jp.get("country"),"country");

		Assert.assertEquals(specialization, jp.get("specialization").toString(), "specialization");
		Assert.assertEquals(current_organization, jp.get("current_organization").toString(), "current_organization");
		Assert.assertEquals(position, jp.get("position").toString(), "position");

		Assert.assertEquals(work_ex_year + "", jp.get("work_ex_year").toString(), "work_ex_year");
		Assert.assertEquals(relevant_experience + "", jp.get("relevant_experience").toString(), "relevant_experience");
		Assert.assertEquals(salarytype, jp.get("salary_type.id").toString(), "salary_type");
		Assert.assertEquals(currency_id + "", jp.get("currency_id").toString(), "currency_id");

		Assert.assertEquals(current_salary + "", jp.get("current_salary").toString(), "current_salary");
		Assert.assertEquals(salary_expectation + "", jp.get("salary_expectation").toString(), "salary_expectation");
		Assert.assertEquals(currentStatus, jp.get("current_status").toString(), "current_status");
		Assert.assertEquals(noticePeriod + "", jp.get("notice_period").toString(), "noticePeriod");

		Assert.assertEquals(source, jp.get("source").toString(), "source");

		// skill
		Assert.assertEquals(skills.replace(",",", "), jp.get("skill").toString(), "skills");

		// social links verification
		Assert.assertEquals(socialUrl, jp.get("facebook").toString(), "facebook");
		Assert.assertEquals(socialUrl, jp.get("twitter").toString(), "twitter");
		Assert.assertEquals(socialUrl, jp.get("github").toString(), "github");
		Assert.assertEquals(socialUrl, jp.get("linkedin").toString(), "linkedin");
		Assert.assertEquals(socialUrl, jp.get("xing").toString(), "xing");

		Assert.assertNotNull(jp.get("slug").toString());
		Assert.assertNotNull(jp.get("id").toString());

		String candidateSlug = jp.get("slug").toString();
		Assert.assertTrue(jp.get("resource_url").toString().contains(candidateSlug));
	}

	@Owner("Yash Rampal")
	@Test(description = "Verfiy 422 Status code - Unprocessable Entity", groups = "nightly-build")
	public void userCannotCreateNewCandidateWithEmptyMandatoryField() {

		Candidate candidate = new Candidate(" ", " ", "", " ");

		Response response1 = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);


		// 4. get the response body:
		String responseBody = response1.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response1.jsonPath();

		slug = jp.get("slug");
		// 2295174

		Assert.assertEquals(response1.getStatusCode(), 422);

		Assert.assertEquals("The first name field is required when last name is not present.", jp.get("first_name[0]"),
				"first_name");
		Assert.assertEquals("The last name field is required when first name is not present.", jp.get("last_name[0]"),
				"last_name");
		// Assert.assertEquals("The email field is required when contact number is not
		// present.", jp.get("email[0]"),
		// "email");
		// Assert.assertEquals("The contact number field is required when email is not
		// present.",
		// jp.get("contact_number[0]"), "contact_number");

	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void createNewCandidateWithMandatoryFields() {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 200);

		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");

	}

	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "getValidInvalidMandatoryFieldsValue", groups = "nightly-build")
	public void verifyAllMandatoryFieldsOfCreateNewCandidate(String FirstName, String LastName, String email,
			String contactNumber, int StatusCode, String ErrorMessage) {
		Candidate candidate = new Candidate(FirstName, LastName, email, contactNumber);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), StatusCode);

		Assert.assertEquals(FirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(LastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(email, jp.get("email"), "email");
		Assert.assertEquals(contactNumber, jp.get("contact_number").toString(), "contact_number");

	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getInvalidEmailFieldsValue", groups = "nightly-build")
	public void verifyInvalidValidationsForEmailField_CreateNewCandidate(String Email, int StatusCode,
			String ErrorMessage) {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, Email, CandidateNumber);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		response.then().body("email[0]", Matchers.equalTo(ErrorMessage));

		// try {
		// //Assert.assertEquals("The email format is invalid.", jp.get("email[0]"),
		// "email");
		//
		// response.then().body("email[0]", Matchers.containsString("The email must be a
		// valid email address."));
		// }catch(Exception e) {
		// Assert.assertEquals("The email must be a valid email address.",
		// jp.get("email[0]"), "email");
		// }

	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getInvalidGenderFieldsValue", groups = "nightly-build")
	public void verifyInvalidValidationsForGenderField_CreateNewCandidate(int Gender, int StatusCode,
			String ErrorMessage) {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setGenderId(Gender);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		if ((StatusCode == 422)) {
			Assert.assertEquals(ErrorMessage, jp.get("gender_id[0]"), "Gender");
		} else if (StatusCode == 200) {
			Assert.assertEquals(ErrorMessage, jp.get("gender_id").toString(), "Gender");
		}

	}

	@Owner("Rahul Shibu")
	@Test(description = "Invalid Qualification", dataProvider = "getInvalidQualificationFieldsValue", groups = "nightly-build")
	public void verifyInvalidValidationsForQualificationField_CreateNewCandidate(int QualificationId, int StatusCode,
			String ErrorMessage) {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setQualification_id(QualificationId);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		if ((StatusCode == 422)) {
			Assert.assertEquals(ErrorMessage, jp.get("qualification_id[0]"), "qualification_id");
		} else if (StatusCode == 200) {
			Assert.assertEquals(ErrorMessage, jp.get("qualification_id").toString(), "qualification_id");
		}
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getInvalidSpecializationFieldsValue", groups = "nightly-build")
	public void verifyInvalidValidationsForSpecializationField_CreateNewCandidate(String specialization, int StatusCode,
			String ErrorMessage) {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setSpecialization(specialization);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		if ((StatusCode == 422)) {
			Assert.assertEquals(ErrorMessage, jp.get("specialization[0]"), "specialization");
		} else if (StatusCode == 200) {
			Assert.assertEquals(ErrorMessage, jp.get("specialization").toString(), "specialization");
		}

	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getInvalidContactNumberFieldsValue", groups = "nightly-build")
	public void verifyInvalidValidationsForContactNumberField_CreateNewCandidate(String ContactNumber, int StatusCode,
			String ErrorMessage) {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, ContactNumber);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		Assert.assertEquals(ErrorMessage, jp.get("contact_number[0]"), "contact_number");

	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void verifyValidCurrentOrganizationSlug_CreateNewCandidate() {
		JsonPath companyJsonPath = commanFunction.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String organizationSlug = companyJsonPath.getString("slug");
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setCurrent_organization_slug(organizationSlug);
		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);
		response.prettyPrint();
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(organizationSlug, jp.getString("current_organization_slug"));
	}

	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "getInvalidCurrentOrganizationSlugValue", groups = "nightly-build")
	public void verifyInvalidCurrentOrganizationSlug_CreateNewCandidate(String slug, int StatusCode, String ErrorMessage) {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setCurrent_organization_slug(slug);
		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);
		response.prettyPrint();
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		Assert.assertEquals(ErrorMessage, jp.getString("errors[0]"));
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "invalidNameLengthData", groups = "nightly-build")
	public void invalidCandidateNameLengthTest(String CandidateFirstName, String CandidateLastName, String fieldName,
			String errorMessage) {
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail,
				CandidateNumber,
				CandidateGender, dob, willing_to_relocate, city, locality, Address, state, country, candidateSummary);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", publicApiKey, null,
				true, candidate);
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		List<String> firstNameErrors = jp.getList(fieldName);
		Assert.assertTrue(firstNameErrors.contains(errorMessage));
	}

	@DataProvider(name = "invalidNameLengthData", parallel = true)
	public Object[][] invalidNameLengthData() {
		return new Object[][] {
				{ "f".repeat(61), CandidateLastName, "first_name",
						"The first name may not be greater than 60 characters." },
				{ CandidateFirstName, "l".repeat(61), "last_name",
						"The last name may not be greater than 60 characters." }
		};
	}

	@DataProvider
	public Object[][] getValidInvalidMandatoryFieldsValue() {
		Object data[][] = {
				{ CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber, 200, "Successfully Created" },
				{ CandidateFirstName, "", CandidateEmail, "", 200, "Successfully Created" },
				{ CandidateFirstName, "", "", CandidateNumber, 200, "Successfully Created" },
				{ "", CandidateLastName, "", CandidateNumber, 200, "Successfully Created" },
				{ "", CandidateLastName, CandidateEmail, "", 200, "Successfully Created" }, };
		return data;
	}

	@DataProvider
	public Object[][] getInvalidGenderFieldsValue() {
		Object data[][] = { { 0, 200, "0" }, { 1, 200, "1" }, { 2, 200, "2" }, { 3, 200, "3" }, { 4, 200, "4" },
				{ 5, 422, "The selected gender id is invalid." } };
		return data;
	}

	@DataProvider
	public Object[][] getInvalidSpecializationFieldsValue() {
		Object data[][] = { { longText, 422, "The specialization may not be greater than 200 characters." },
				{ specialization, 200, specialization } };
		return data;
	}

	@DataProvider
	public Object[][] getInvalidQualificationFieldsValue() {
		Object data[][] = { { 10000, 422, "Invalid qualification id" }, { 001230, 422, "Invalid qualification id" },
				{ 1, 200, "1" }, { 2, 200, "2" }, { 3, 200, "3" }, { 4, 200, "4" }, { 5, 200, "5" }, { 6, 200, "6" }, };
		return data;
	}

	@DataProvider
	public Object[][] getInvalidContactNumberFieldsValue() {
		Object data[][] = { { longText, 422, "The contact number may not be greater than 100 characters." } };
		return data;
	}

	@DataProvider
	public Object[][] getInvalidEmailFieldsValue() {
		Object data[][] = { { "plainaddress", 422, "The email must be a valid email address." },
				{ "#@%^%#$@#$@#.com", 422, "The email must be a valid email address." },
				{ "@example.com", 422, "The email must be a valid email address." },
				{ "Joe Smith <email@example.com>", 422, "The email must be a valid email address." },
				{ "email.example.com", 422, "The email must be a valid email address." },
				{ "email@example@example.com", 422, "The email must be a valid email address." },
				{ ".email@example.com", 422, "The email must be a valid email address." },
				{ "email.@example.com", 422, "The email must be a valid email address." },
				{ "email..email@example.com", 422, "The email must be a valid email address." },
				{ "email@example.com (Joe Smith)", 422, "The email must be a valid email address." },
				{ "email@example", 422, "The email format is invalid." },
				{ "email@-example.com", 422, "The email must be a valid email address." },
				{ "email@example..com", 422, "The email must be a valid email address." },
				{ "Abc..123@example.com", 422, "The email must be a valid email address." },
				{ "”(),:;<>[\\]@example.com", 422, "The email must be a valid email address." },
				{ "this\\ is\"really\"not\\allowed@example.com", 422, "The email must be a valid email address." },
				{ "MorbivenenatisAccusamuspossimusexdoloreNullamdelenitimaecenasscelerisquesenectusreprehenderitportaperreprehenderitTempora@yopmail.com",
						422, "The email may not be greater than 100 characters." } };
		return data;

		// List of Invalid Email Addresses
		//
		// plainaddress
		// #@%^%#$@#$@#.com
		// @example.com
		// Joe Smith <email@example.com>
		// email.example.com
		// email@example@example.com
		// .email@example.com
		// email.@example.com
		// email..email@example.com
		// あいうえお@example.com
		// email@example.com (Joe Smith)
		// email@example
		// email@-example.com
		// email@example.web
		// email@111.222.333.44444
		// email@example..com
		// Abc..123@example.com
		//
		//
		//
		// List of Strange Invalid Email Addresses
		//
		// ”(),:;<>[\]@example.com
		// just”not”right@example.com
		// this\ is"really"not\allowed@example.com
	}

	@DataProvider
	public Object[][] getInvalidCurrentOrganizationSlugValue() {
		Object data[][] = { { current_organization_slug, 422, "Current organization slug is invalid" } };
		return data;
	}

}
