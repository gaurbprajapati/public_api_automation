package io.rcrm.api.candidate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
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
public class DeleteCandidateTest extends TestBase {

	String slug = "";

	public DeleteCandidateTest() {
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

	@Owner("Yash Rampal")
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

		slug = jp.get("slug");

		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");

	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void deleteCandidateBySlug_POST() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		String basePath = "candidates/{candidate}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);
		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertTrue(responseBody.contains("Deleted Successfully!"), "Deleted Successfully!");

	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void deleteCandidateByInvalidSlug_POST() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug + "9876543");

		String basePath = "candidates/{candidate}";
		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int errorCodeInt = jp.getInt("errorCode");

		String errorCodeString = String.valueOf(errorCodeInt);

		Assert.assertEquals("404", errorCodeString, "errorCode");
		Assert.assertEquals("Candidate doesn't exist", jp.get("errorMessage"), "errorMessage");

	}

}
