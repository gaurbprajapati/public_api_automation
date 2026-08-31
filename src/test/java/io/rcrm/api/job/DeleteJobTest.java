package io.rcrm.api.job;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
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
public class DeleteJobTest extends TestBase{

	public DeleteJobTest() {
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
	
	@Owner("Sampurn Chouksey")
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

	@Owner("Gaurav Prajapati")
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
	
	@Owner("Yash Rampal")
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

		job_slug = jp.get("slug");
		// Verify Response body
		int number_of_openings = jp.get("number_of_openings");
		String res_numberOf_openings = String.valueOf(number_of_openings);

		Assert.assertEquals(JobName, jp.get("name"), "Job Name");
		Assert.assertEquals(Company_slug, jp.get("company_slug"), "Company Slug");
		Assert.assertEquals(Contact_slug, jp.get("contact_slug"), "Contact Slug");
		Assert.assertEquals(NoOfOpenings+"", res_numberOf_openings, "No Of Openings");

	
	}
	
	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "userCanCreateNewJobWithOnlyMandatoryFieldsAndVerifyReponseBodyAndCode200", groups = "nightly-build")
	public void deleteJobBySlug_GET() {


		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("job", job_slug);

		String basePath = "jobs/{job}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),null,
				pathParamters, true);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(response.getStatusCode(), 200);
	
		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

//		boolean jobSucessStatus = jp.get("success");
		String jobSuccessMessage = jp.getString("message");
//		
//		Assert.assertEquals(jobSucessStatus, true);
		Assert.assertEquals(jobSuccessMessage, "Job deleted successfully.");
		
 }
	
	
	// Issue in API : https://rcrm.atlassian.net/browse/RCA-477
	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "userCanCreateNewJobWithOnlyMandatoryFieldsAndVerifyReponseBodyAndCode200", groups = "nightly-build")
	public void deleteJobByInvalidSlug_GET() {


		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("job", "9998888777666655544");

		String basePath = "jobs/{job}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),null,
				pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 404);
	
		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		
		int errorCodeInt = jp.getInt("errorCode");

		String errorCodeString = String.valueOf(errorCodeInt);

		Assert.assertEquals("404", errorCodeString, "errorCode");
		Assert.assertEquals("Job doesn't exist", jp.get("errorMessage"), "errorMessage");
		
 }

}
