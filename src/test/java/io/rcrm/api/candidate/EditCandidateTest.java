package io.rcrm.api.candidate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

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
public class EditCandidateTest extends TestBase {

	String slug = "";
	String candidateSlug = "";
	Candidate candidate = null;
	commanFunction function = new commanFunction();

	public EditCandidateTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerJob jobFaker = new JavaFakerJob();

	// Personal Information
	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	//String CandidateEmail = "rcrmtest0@gmail.com";
	String CandidateEmail = fakerCandidate.getEmailID();
	String CandidateNumber = fakerCandidate.getContactNumber();

	int qualificatioId = fakerCandidate.getQualification_id();
	String specialization = fakerCandidate.getSpecialization();

	String title = fakerCandidate.getPosition();
	int work_ex_year = fakerCandidate.getWork_ex_year();
	int RelevantExp = fakerCandidate.getRelevant_experience();
	String salarytype = fakerCandidate.getSalary_type();
	int currencyId = fakerCandidate.getCurrency_id();
	String currentStatus = fakerCandidate.getCurrentEmploymentStatus();
	int noticePeriod = fakerCandidate.getNotice_period();
	String availableForm = fakerCandidate.getAvailable_From();

	String skills = fakerCandidate.getSkills();

	// Social Links
	String fbLink = fakerCandidate.getUrl();
	String twitterLink = fakerCandidate.getUrl();
	String githubLink = fakerCandidate.getUrl();
	String linkedinLink = fakerCandidate.getUrl();
	String xingLink = fakerCandidate.getUrl();

	String dob = fakerCandidate.getDOB();
	int current_salary = fakerCandidate.getCurrent_salary();
	int salary_expectation = fakerCandidate.getSalary_expectation();
	String current_organization = fakerCandidate.getCurrentOrganization();

	String city = fakerCandidate.getCity();
	String locality = fakerCandidate.getLocality();
	String Address = fakerCandidate.getCandidateAddress();
	String state = fakerCandidate.getState();
	String country = fakerCandidate.getCountry();
	String candidateSummary=fakerCandidate.candidateSummary();

	String source = fakerCandidate.getSource();

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void createNewCandidateWithMandatoryFields() {
		Candidate candidate1 = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);

		Response response = RestClient.doPost1("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, null, true,
				candidate1);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 200);

		slug = jp.get("slug");

		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");

	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void CreateNewCandidateWithAllFields() {

		// Personal Information
		candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber, 1, dob, 1,
				city, locality, Address, state, country,candidateSummary);
		// Academic Information
		candidate.setQualification_id(qualificatioId);
		candidate.setSpecialization(specialization);

		// Employment Information
		candidate.setCurrent_organization(current_organization);
		candidate.setPosition(title);
		candidate.setWork_ex_year(work_ex_year);
		candidate.setRelevant_experience(RelevantExp);
		candidate.setSalaryType(salarytype);
		candidate.setCurrency_id(currencyId);
		candidate.setCurrent_salary(current_salary);
		candidate.setSalary_expectation(salary_expectation);
		candidate.setCurrent_status(currentStatus);
		candidate.setNotice_period(noticePeriod);
		candidate.setAvailable_from(availableForm);

		// Skills
		candidate.setSkill(skills);

		// Social Links
		candidate.setFacebook(fbLink);
		candidate.setTwitter(twitterLink);
		candidate.setLinkedin(linkedinLink);
		candidate.setGithub(githubLink);
		candidate.setXing(xingLink);

		candidate.setSource(source);

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
		Assert.assertEquals("1", jp.get("gender_id").toString(), "gender_id");
		Assert.assertEquals(qualificatioId_String, jp.get("qualification_id").toString(), "qualificatioId");

		Assert.assertEquals(Address, jp.get("address").toString(), "Address");
		Assert.assertEquals(locality, jp.get("locality").toString(), "locality");
		Assert.assertEquals(city, jp.get("city").toString(), "city");
		Assert.assertEquals("1", jp.get("willing_to_relocate").toString(), "willing_to_relocate");
		Assert.assertEquals(state,jp.get("state"),"State");
		Assert.assertEquals(country,jp.get("country"),"Country");

		Assert.assertEquals(specialization, jp.get("specialization").toString(), "specialization");
		Assert.assertEquals(current_organization, jp.get("current_organization").toString(), "current_organization");
		Assert.assertEquals(title, jp.get("position").toString(), "position");

		Assert.assertEquals(work_ex_year + "", jp.get("work_ex_year").toString(), "work_ex_year");
		Assert.assertEquals(RelevantExp + "", jp.get("relevant_experience").toString(), "relevant_experience");
		Assert.assertEquals(salarytype, jp.get("salary_type.id").toString(), "salary_type");
		Assert.assertEquals(currencyId + "", jp.get("currency_id").toString(), "currency_id");
		Assert.assertEquals(currencyId + "", jp.get("currency_id").toString(), "currency_id");

		Assert.assertEquals(current_salary + "", jp.get("current_salary").toString(), "current_salary");
		Assert.assertEquals(salary_expectation + "", jp.get("salary_expectation").toString(), "salary_expectation");
		Assert.assertEquals(currentStatus, jp.get("current_status").toString(), "current_status");
		Assert.assertEquals(noticePeriod + "", jp.get("notice_period").toString(), "noticePeriod");

		Assert.assertEquals(source, jp.get("source").toString(), "source");

		// skill
		Assert.assertEquals(skills.replace(",",", "), jp.get("skill").toString(), "skills");

		// social links verification
		Assert.assertEquals(fbLink, jp.get("facebook").toString(), "facebook");
		Assert.assertEquals(twitterLink, jp.get("twitter").toString(), "twitter");
		Assert.assertEquals(githubLink, jp.get("github").toString(), "github");
		Assert.assertEquals(linkedinLink, jp.get("linkedin").toString(), "linkedin");
		Assert.assertEquals(xingLink, jp.get("xing").toString(), "xing");

		Assert.assertNotNull(jp.get("slug").toString());
		Assert.assertNotNull(jp.get("id").toString());

		candidateSlug = jp.get("slug").toString();
		Assert.assertTrue(jp.get("resource_url").toString().contains(candidateSlug));
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void editCandidateBySlug_POST() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}";
		String CandidateFirstNameEdited = CandidateFirstName + " SahebRao";
		String CandidateSummaryNew=candidateSummary+" : Edited By Sachi Joshi";

		// Here we can also use data provider.
		Candidate candidateObject = new Candidate(CandidateFirstNameEdited, CandidateLastName, "rcrmtest1@gmail.com",
				"98765432109",CandidateSummaryNew);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				candidateObject);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(CandidateFirstNameEdited, jp.get("first_name"), "first_name");

	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "CreateNewCandidateWithAllFields")
	public void editCandidateAndVerify422() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}";

		// Here we can also use data provider. No fields data is changed
		Candidate candidate = new Candidate(CandidateFirstName,
				CandidateLastName, CandidateEmail,
				CandidateNumber);

		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				candidate);

		Assert.assertEquals(response1.getStatusCode(), 422);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response1.jsonPath();

		int errorCodeInt = jp.getInt("errorCode");
		String errorCodeString = String.valueOf(errorCodeInt);

		Assert.assertEquals("422", errorCodeString, "errorCode");
		Assert.assertEquals("At least one value must change!", jp.get("errorMessage"), "errorMessage");

	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void editCandidateByInvalidSlugAndVerify404() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug + "x0000");

		String basePath = "candidates/{candidate}";

		// Here we can also use data provider.
		Candidate candidateObject = new Candidate(CandidateFirstName + CandidateNumber, CandidateLastName,
				CandidateEmail, CandidateNumber);

		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				candidateObject);

		Assert.assertEquals(response1.getStatusCode(), 404);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response1.jsonPath();

		Assert.assertEquals(jp.getString("errors[0]"), "Candidate doesn't exist", "Error message for invalid candidate slug");

	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void editAllFieldsOfCandidateAndVerifyResponse() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}";
		String CandidateFirstNameEdited = CandidateFirstName + "SahebRao";

		// Here we can also use data provider.
		Candidate candidateObject = new Candidate(CandidateFirstNameEdited, CandidateLastName, CandidateEmail,
				CandidateNumber, 1, dob, 1, city, locality, Address, state, country, candidateSummary);

		// Academic Information
		candidateObject.setQualification_id(qualificatioId);
		candidateObject.setSpecialization(specialization);

		// Employment Information
		candidateObject.setCurrent_organization(current_organization);
		candidateObject.setPosition(title);
		candidateObject.setWork_ex_year(work_ex_year);
		candidateObject.setRelevant_experience(RelevantExp);
		candidateObject.setSalaryType(salarytype);
		candidateObject.setCurrency_id(currencyId);
		candidateObject.setCurrent_salary(current_salary);
		candidateObject.setSalary_expectation(salary_expectation);
		candidateObject.setCurrent_status(currentStatus);
		candidateObject.setNotice_period(noticePeriod);
		candidateObject.setAvailable_from(availableForm);

		// Skills
		candidateObject.setSkill(skills);

		// Social Links
		candidateObject.setFacebook(fbLink);
		candidateObject.setTwitter(twitterLink);
		candidateObject.setLinkedin(linkedinLink);
		candidateObject.setGithub(githubLink);
		candidateObject.setXing(xingLink);

		candidateObject.setSource(source);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				candidateObject);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(CandidateFirstNameEdited, jp.get("first_name"), "first_name");

		String qualificatioId_String = String.valueOf(qualificatioId);

		// Verify all fields value
		Assert.assertEquals(CandidateFirstNameEdited, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");
		Assert.assertEquals("1", jp.get("gender_id").toString(), "gender_id");
		Assert.assertEquals(qualificatioId_String, jp.get("qualification_id").toString(), "qualificatioId");

		Assert.assertEquals(Address, jp.get("address").toString(), "Address");
		Assert.assertEquals(locality, jp.get("locality").toString(), "locality");
		Assert.assertEquals(city, jp.get("city").toString(), "city");
		Assert.assertEquals("1", jp.get("willing_to_relocate").toString(), "willing_to_relocate");
		Assert.assertEquals(state, jp.get("state"),"State");
		Assert.assertEquals(country, jp.get("country"),"Country");

		Assert.assertEquals(specialization, jp.get("specialization").toString(), "specialization");
		Assert.assertEquals(current_organization, jp.get("current_organization").toString(), "current_organization");
		Assert.assertEquals(title, jp.get("position").toString(), "position");

		Assert.assertEquals(work_ex_year + "", jp.get("work_ex_year").toString(), "work_ex_year");
		Assert.assertEquals(RelevantExp + "", jp.get("relevant_experience").toString(), "relevant_experience");
		Assert.assertEquals(salarytype, jp.get("salary_type.id").toString(), "salary_type");
		Assert.assertEquals(currencyId + "", jp.get("currency_id").toString(), "currency_id");
		Assert.assertEquals(currencyId + "", jp.get("currency_id").toString(), "currency_id");

		Assert.assertEquals(current_salary + "", jp.get("current_salary").toString(), "current_salary");
		Assert.assertEquals(salary_expectation + "", jp.get("salary_expectation").toString(), "salary_expectation");
		Assert.assertEquals(currentStatus, jp.get("current_status").toString(), "current_status");
		Assert.assertEquals(noticePeriod + "", jp.get("notice_period").toString(), "noticePeriod");

		Assert.assertEquals(source, jp.get("source").toString(), "source");

		// skill
		Assert.assertEquals(skills.replace(",",", "), jp.get("skill").toString(), "skills");

		// social links verification
		Assert.assertEquals(fbLink, jp.get("facebook").toString(), "facebook");
		Assert.assertEquals(twitterLink, jp.get("twitter").toString(), "twitter");
		Assert.assertEquals(githubLink, jp.get("github").toString(), "github");
		Assert.assertEquals(linkedinLink, jp.get("linkedin").toString(), "linkedin");
		Assert.assertEquals(xingLink, jp.get("xing").toString(), "xing");

		Assert.assertNotNull(jp.get("slug").toString());
		Assert.assertNotNull(jp.get("id").toString());

		candidateSlug = jp.get("slug").toString();
		Assert.assertTrue(jp.get("resource_url").toString().contains(candidateSlug));
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void editCandidateByValidOrganizationSlug() {
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("candidate", slug);
		String basePath = "candidates/{candidate}";

		Candidate candidateObject = new Candidate(CandidateFirstName + CandidateNumber, CandidateLastName, CandidateEmail, CandidateNumber);
		JsonPath companyJp = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String current_organization_slug = companyJp.getString("slug");
		String current_organization = companyJp.getString("company_name");
		candidateObject.setCurrent_organization_slug(current_organization_slug);
		candidateObject.setCurrent_organization(fakerCandidate.getCurrentOrganization());

		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParameters, true, candidateObject);

		response1.prettyPrint();

		Assert.assertEquals(response1.getStatusCode(), 200);
		JsonPath jp = response1.jsonPath();
		Assert.assertEquals(jp.getString("current_organization"), current_organization, "Current Organization Name");
		Assert.assertEquals(jp.getString("current_organization_slug"), current_organization_slug, "Current Organization Slug");
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void editCandidateByInvalidOrganizationSlug() {
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("candidate", slug);
		String basePath = "candidates/{candidate}";

		Candidate candidateObject = new Candidate(CandidateFirstName + CandidateNumber, CandidateLastName, CandidateEmail, CandidateNumber);
		candidateObject.setCurrent_organization_slug(fakerCandidate.getCurrentOrganizationSlug());
		candidateObject.setCurrent_organization(fakerCandidate.getCurrentOrganization());

		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParameters, true, candidateObject);

		response1.prettyPrint();

		Assert.assertEquals(response1.getStatusCode(), 422);
		JsonPath jp = response1.jsonPath();
		Assert.assertEquals(jp.getString("errors[0]"), "Current organization slug is invalid", "Error Message for Invalid Current Organization Slug");
	}

}
