package io.rcrm.api.candidate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class UnauthorizedUserCandidateTest extends TestBase {

	public UnauthorizedUserCandidateTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	String candidateSlug = "";
	Candidate candidate = null;

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerJob jobFaker = new JavaFakerJob();

	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	String CandidateEmail = "rcrmtest0@gmail.com";
	String CandidateNumber = fakerCandidate.getContactNumber();
	commanFunction function = new commanFunction();

	@Owner("Gaurav Prajapati")
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

		slug = jp.get("slug");
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotCreateNewCandidate() {
		Candidate candidate1 = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);

		Response response = RestClient.doPost1("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey()+"12345", null, null, true,
				candidate1);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals("Unauthorized", jp.get("error"), "Error ");

	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSearchCandidate() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("first_name", "Sandeep");
		queryParameters.put("email", "spi504@yopmail.com");

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey()+"12345", queryParameters, null,
				true);


		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals("Unauthorized", jp.get("error"), "Error ");

	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllCandidateslist() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "10");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey()+"12345", queryParameters, null, true);


		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals("Unauthorized", jp.get("error"), "Error ");

	}

	/*
	 * Known Issue in Edit candidate with invalid apikey Along with Slug is passed
	 * blank
	 */
	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void unauthorizedUserCannotEditCandidate() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}";

		// Here we can also use data provider.
		Candidate candidateObject = new Candidate(CandidateFirstName + "_Edited", CandidateLastName,
				"spi504234@yopmail.com", "98765432109");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters, true,
				candidateObject);


		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals("Unauthorized", jp.get("error"), "Error ");

	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void unauthorizedUserCannotDeleteCandidate() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters, false);
		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals("Unauthorized", jp.get("error"), "Error ");

	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void unathorizedUserCannotApplyToJob() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", "23234234");

		String basePath = "candidates/{candidate}/apply";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", "23243324");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 401);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals("Unauthorized", jp.get("error"), "Error ");

	}
}
