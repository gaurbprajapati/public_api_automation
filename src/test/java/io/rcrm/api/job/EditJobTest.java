package io.rcrm.api.job;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import io.rcrm.api.commanfunctions.errorResponseBody;
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
public class EditJobTest extends TestBase{

	public EditJobTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	Faker faker = new Faker();
	JavaFakerJob jobFaker = new JavaFakerJob();
	errorResponseBody errorResponseBody = new errorResponseBody();

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
	String response_jobSlug = "";
	String longText = jobFaker.getJobDescriptionText() + jobFaker.getNoteForCandidate();
	
	@Owner("Gaurav Prajapati")
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

	@Owner("Yash Rampal")
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
	
	@Owner("Raj Pandey")
	@Test(dependsOnMethods = { "createNewCompany_POST", "createNewContact_POST" }, groups = "nightly-build")
	public void createNewWithOnlyMandatoryFields() {
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
	
	
	@Owner("Sandeep")
	@Test(dependsOnMethods = { "createNewCompany_POST", "createNewContact_POST" }, groups = "nightly-build")
	public void userCannotEditJobWithInvalidHiringPipelineID() {
		Job job = new Job();
		job.setName(JobName);
		job.setCompany_slug(Company_slug);
		job.setContact_slug(Contact_slug);
		job.setNumber_of_openings(NoOfOpenings);

		// Below fields can't be blank
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		job.setEnable_job_application_form(1);
		job.setHiring_pipeline_id(9999);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);

		// get the response body:
		// String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200

		Assert.assertEquals(response.getStatusCode(), 422);
		errorResponseBody.verify422ResponseBody(response, 422, "No Hiring Pipeline Found.", true);
		
	}
	
	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = { "createNewCompany_POST", "createNewContact_POST","createNewWithOnlyMandatoryFields" }, groups = "nightly-build")
	public void editAllFieldsOfJob_POST() {
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("job", job_slug);

		String basePath = "jobs/{job}";
		String jobName_Edit = JobName + " Edited";
		
		Job job = new Job();
		job.setName(jobName_Edit);
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
		job.setJob_type(jobFaker.getJobType());
		job.setJobCategory(jobFaker.getJobCategory());
		job.setPostal_code(jobFaker.getPostalCode());

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true, job);
		
		
		//Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		job_slug = jp.get("slug");
		// 2295174

		response.then().body("name", Matchers.is(jobName_Edit));
		response.then().body("number_of_openings", Matchers.is(NoOfOpenings));
		response.then().body("contact.first_name", Matchers.containsString(ContactFirstName));
		response.then().body("company.company_name", Matchers.is(companyName));
	}
	
	
	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = { "createNewCompany_POST", "createNewContact_POST","createNewWithOnlyMandatoryFields" }, groups = "nightly-build")
	public void editJobByInvalidJobSlug_POST() {


		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("job", job_slug+"12345");

		String basePath = "jobs/{job}";
		Job job = new Job(jobFaker.getJobName()+" "+jobFaker.getJobSeniority(),Company_slug,Contact_slug,jobFaker.getOpenings(),1);
		
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true, job);

		Assert.assertEquals(response.getStatusCode(), 404);
		
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Job doesn't exist"));
 }
	
	

}
