package io.rcrm.api.job;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class CreateNewJobTest extends TestBase {

	public CreateNewJobTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	Faker faker = new Faker();
	JavaFakerJob jobFaker = new JavaFakerJob();

	String companyName = faker.company().name();
	String companyWebsite = "https://" + faker.company().url();

	String ContactFirstName = faker.name().firstName();
	String ContactLastName = faker.name().lastName();
	String ContactEmail = "rcrmtest0@gmail.com";
	String contactNumber = faker.phoneNumber().phoneNumber();

	String JobName = jobFaker.getJobName();
	int NoOfOpenings = jobFaker.getOpenings();
	int MaximumExperience = jobFaker.getMaximumExperience();
	int MinimumExperience = jobFaker.getMinimumExperience();
	int Min_annual_salary = jobFaker.getMin_annual_salary();
	int Max_annual_salary = jobFaker.getMax_annual_salary();

	String Company_slug = "";
	String Contact_slug = "";
	String job_slug = "";
	String longText = jobFaker.getJobDescriptionText() + jobFaker.getNoteForCandidate();
	String job_status = jobFaker.getJob_status();
	int showCompanyLogo = jobFaker.getShow_company_logo();

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void createNewCompany_POST() {

		Company company = new Company(companyName, companyWebsite, contactNumber, "");

		Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Company_slug = jp.get("slug");
		// 2295174
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCompany_POST", groups = "nightly-build")
	public void createNewContact_POST() {

		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, Company_slug);

		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Contact_slug = jp.get("slug");
		// 2295174
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = { "createNewCompany_POST", "createNewContact_POST" }, groups = "nightly-build")
	public void userCanCreateNewJobWithOnlyMandatoryFieldsAndVerifyReponseBodyAndCode200() {
		Job job = new Job();
		job.setName(JobName);
		job.setCompany_slug(Company_slug);
		job.setContact_slug(Contact_slug);
		job.setNumber_of_openings(NoOfOpenings);

		// Below fields can't be blank
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// get the response body:
		// String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response body
		int number_of_openings = jp.get("number_of_openings");
		String res_numberOf_openings = String.valueOf(number_of_openings);

		Assert.assertEquals(JobName, jp.get("name"), "Job Name");
		Assert.assertEquals(Company_slug, jp.get("company_slug"), "Company Slug");
		//Assert.assertEquals(Contact_slug, jp.get("contact_slug"), "Contact Slug");
		Assert.assertEquals(NoOfOpenings+"", res_numberOf_openings, "No Of Openings");

	
	}

	
	@Owner("Sandeep")
	@Test(dependsOnMethods = { "createNewCompany_POST", "createNewContact_POST" }, groups = "nightly-build")
	public void userCannotCreateNewJobWithInValidHiringPipelineID() {
		Job job = new Job();
		job.setName(JobName);
		job.setCompany_slug(Company_slug);
		job.setContact_slug(Contact_slug);
		job.setNumber_of_openings(NoOfOpenings);
		job.setHiring_pipeline_id(99999999);
		

		// Below fields can't be blank
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// get the response body:
		// String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200

		Assert.assertEquals(response.getStatusCode(), 422);


	
	}

	
	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void createNewJobWithInValidMandatoryFields() {
		Job job = new Job();
		job.setName(null);
		job.setCompany_slug(null);
		job.setContact_slug(null);
		job.setNumber_of_openings(0);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// get the response body:
		String responseBody = response.getBody().asString();


		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		// try {
		Assert.assertEquals(response.getStatusCode(), 422);
		// } catch (Exception e) {
		// int status = response.getStatusCode();
		// String responseStatus = String.valueOf(status);
		// Assert.fail(responseStatus + "\n" + responseBody);
		// }

		// Verify Response body
		// try {
		String jobname = jp.get("name[0]");

		Assert.assertEquals("The name field is required.", jp.get("name[0]"), "Job Name");
		Assert.assertEquals("The company slug field is required.", jp.get("company_slug[0]"), "Company Slug");


		/*
		 * } catch (Exception e) { Assert.fail(responseBody); }
		 */
	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void createNewJobWithEmptyRequestBody() {
		Job job = null;
		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		try {
			Assert.assertEquals(response.getStatusCode(), 422);
		} catch (Exception e) {
			int status = response.getStatusCode();
			String responseStatus = String.valueOf(status);
			Assert.fail(responseStatus + "\n" + responseBody);
		}

		String jobname = jp.get("name[0]");

		Assert.assertEquals("The name field is required.", jp.get("name[0]"), "Job Name");
		Assert.assertEquals("The company slug field is required.", jp.get("company_slug[0]"), "Company Slug");

	}

	@Owner("Sampurn Chouksey")
	@Test(invocationCount = 1,dependsOnMethods = { "createNewCompany_POST", "createNewContact_POST" }, groups = "nightly-build")
	public void createNewJob_POST() {

		Job job = new Job();
		job.setName(JobName);
		job.setCompany_slug(Company_slug);
		job.setContact_slug(Contact_slug);
		job.setNumber_of_openings(NoOfOpenings);
		job.setEnable_job_application_form(1);
		job.setMaximum_experience(MaximumExperience);
		job.setMinimum_experience(MinimumExperience);
		job.setMin_annual_salary(Min_annual_salary);
		job.setMax_annual_salary(Max_annual_salary);

		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setNote_for_candidates(jobFaker.getNoteForCandidate());
		// Address
		job.setCity(jobFaker.getJobCity());
		job.setCountry(jobFaker.getJobCountry());
		job.setState(jobFaker.getJobState());
		job.setLocality(jobFaker.getJobLocality());
		job.setAddress(jobFaker.getJobFullAddress());

		job.setCurrency_id(jobFaker.getCurrency_id());
		job.setQualification_id(jobFaker.qualification_id());
		job.setSalary_type(jobFaker.getSalary_type());
		job.setSpecialization(jobFaker.getSpecialization());
		job.setJob_skill(jobFaker.getSkills());
		job.setJob_status(job_status);
		job.setShow_company_logo(showCompanyLogo);
		job.setJob_type(jobFaker.getJobType());
		job.setJobCategory(jobFaker.getJobCategory());
		job.setPostal_code(jobFaker.getPostalCode());

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		job_slug = jp.get("slug");
		// 2295174

		Assert.assertEquals(JobName, jp.get("name"), "Job Name");
		Assert.assertEquals(Company_slug, jp.get("company_slug"), "Company Slug");
		//Assert.assertEquals(Contact_slug, jp.get("contact_slug"), "Contact Slug");
		Assert.assertEquals(NoOfOpenings+"", jp.get("number_of_openings").toString(), "No Of openings");
		Assert.assertEquals(job_status, jp.getString("job_status.id"), "Job Status");
		Assert.assertEquals(showCompanyLogo+"", jp.getString("show_company_logo"), "show company logo");

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Verify all field-wise validations .

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void createNewJobWithInvalidJobNameCompanySlugOpenings() {

		String invalidJobName = JobName + jobFaker.getJobDescriptionText();

		Job job = new Job();
		job.setName(invalidJobName);
		job.setCompany_slug("x0000");
		job.setNumber_of_openings(51);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// 4. get the response body:
		// String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), 422);

		String jobname = jp.get("name[0]");

		Assert.assertEquals(jp.get("name[0]"), "The name may not be greater than 300 characters.", "Job Name");
		Assert.assertEquals(jp.get("company_slug[0]"), "Invalid company slug", "Company Slug");
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = { "createNewCompany_POST" }, groups = "nightly-build")
	public void createNewJobWithInvalidContactSlug() {

		Job job = new Job();
		job.setCompany_slug(Company_slug);
		job.setContact_slug("Invalid Contact Slug");

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);


		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals("The contact_slug field invalid, contact_slug should be linked with provided company_slug",
				jp.get("contact_slug[0]"), "Contact Slug");

	}

	@Owner("Divya")
	@Test(dataProvider = "getInvalidMinimumExperienceFieldsValue", groups = "nightly-build")
	public void createNewJobWithInvalidMinimumExperience(int minimumExperience, int StatusCode,
			String ErrorMessage) {

		Job job = new Job();
		job.setMinimum_experience(minimumExperience); // minimum experience should be between 0 to 30

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		Assert.assertEquals(ErrorMessage, jp.get("minimum_experience[0]"), "minimum_experience");
	}

	@Owner("Divya")
	@Test(dataProvider = "getInvalidMaximumExperienceFieldsValue", groups = "nightly-build")
	public void createNewJobWithInvalidMaximumExperience(int maximumExperience, int StatusCode,
			String ErrorMessage) {

		Job job = new Job();
		job.setMaximum_experience(maximumExperience); // Maximum experience should be between 0 to 30

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		Assert.assertEquals(ErrorMessage, jp.get("maximum_experience[0]"), "maximum_experience");

	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getInvalidSpecializationFieldsValue", groups = "nightly-build")
	public void VerifyErrorMessagesCreateNewJobWithInvalidSpecialization(String Specialization, int StatusCode,
			String ErrorMessage) {

		Job job = new Job();
		job.setSpecialization(Specialization);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		Assert.assertEquals(ErrorMessage, jp.get("specialization[0]"), "Specialization");
	}

	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "getInvalidCityFieldsValue", groups = "nightly-build")
	public void userCannotCreateNewJobWithInvalidCityFieldValueAndVerifyResponseErrorMessages(String City,
			int StatusCode, String ErrorMessage) {

		Job job = new Job();
		job.setCity(City);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		Assert.assertEquals(ErrorMessage, jp.get("city[0]"), "City");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getInvalidLocalityFieldsValue", groups = "nightly-build")
	public void VerifyInvalidValidationForLocalityField_CreateNewjob(String Locality, int StatusCode,
			String ErrorMessage) {

		Job job = new Job();
		job.setLocality(Locality);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		Assert.assertEquals(ErrorMessage, jp.get("locality[0]"), "Locality");
	}

	@Owner("Yash Rampal")
	@Test(dataProvider = "getInvalidStateFieldsValue", groups = "nightly-build")
	public void VerifyInvalidValidationForStateField_CreateNewjob(String State, int StatusCode, String ErrorMessage) {

		Job job = new Job();
		job.setState(State);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 422
		Assert.assertEquals(response.getStatusCode(), StatusCode);
		Assert.assertEquals(ErrorMessage, jp.get("state[0]"), "State");
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = { "createNewCompany_POST", "createNewContact_POST" }, groups = "nightly-build")
	public void unauthorizedUserCannotCreateNewJob() {

		Job job = new Job();
		job.setName(JobName);
		job.setCompany_slug(Company_slug);
		job.setContact_slug(Contact_slug);
		job.setNumber_of_openings(NoOfOpenings);

		// Below fields can't be blank
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey()+"123", null, true, job);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		// Verify response status code: 200

		Assert.assertEquals(response.getStatusCode(), 401);

	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void verifyJobNameCharacterLimit() {
		// Create company
		Company company = new Company(companyName, companyWebsite, contactNumber, "");
		Company_slug = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company).jsonPath().get("slug");

		// Create contact
		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, Company_slug);
		Contact_slug = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact).jsonPath().get("slug");

		// Create job with invalid character count
		Job job = new Job();
		job.setName(StringUtils.repeat('e', 301));
		job.setCompany_slug(Company_slug);
		job.setContact_slug(Contact_slug);
		job.setNumber_of_openings(NoOfOpenings);
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jp.get("name[0]"), "The name may not be greater than 300 characters.", "The error message is not valid");
	}

	// *****************************************************************************************

	@DataProvider
	public Object[][] getInvalidMinimumExperienceFieldsValue() {
		Object data[][] = { { 31, 422, "The minimum experience may not be greater than 30." },
				{ 41, 422, "The minimum experience may not be greater than 30." }};
				//{ -1, 422, "The minimum experience may not be less than 0." } };
		return data;
	}

	@DataProvider
	public Object[][] getInvalidMaximumExperienceFieldsValue() {
		Object data[][] = { { 31, 422, "The maximum experience may not be greater than 30." },
				{ 41, 422, "The maximum experience may not be greater than 30." }};
				//{ -1, 422, "The maximum experience may not be less than 0." } };
		return data;
	}

	@DataProvider
	public Object[][] getInvalidSpecializationFieldsValue() {
		Object data[][] = { { null, 422, "The specialization must be a string." },
				{ longText, 422, "The specialization may not be greater than 100 characters." } };
		return data;
	}

	@DataProvider
	public Object[][] getInvalidCityFieldsValue() {
		Object data[][] = { { null, 422, "The city must be a string." },
				{ longText, 422, "The city may not be greater than 50 characters." } };
		return data;
	}

	@DataProvider
	public Object[][] getInvalidStateFieldsValue() {
		Object data[][] = { { null, 422, "The state must be a string." },
				{ longText, 422, "The state may not be greater than 50 characters." } };
		return data;
	}
	
	@DataProvider
	public Object[][] getInvalidLocalityFieldsValue() {
		Object data[][] = { { null, 422, "The locality must be a string." },
				{ longText, 422, "The locality may not be greater than 40 characters." } };
		return data;
	}

}
