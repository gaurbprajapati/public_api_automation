package io.rcrm.api.candidate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
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
public class SearchCandidateBySlugTest extends TestBase{

	public SearchCandidateBySlugTest() {
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
	//String CandidateEmail = "rcrmtest0@gmail.com";
	String CandidateEmail = fakerCandidate.getEmailID();
	String CandidateNumber = fakerCandidate.getContactNumber();
	String linkedinLink = fakerCandidate.getUrl();

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void createNewCandidateWithMandatoryFields() {

		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setLinkedin(linkedinLink);

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
	
	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByValidSlug() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		
		String basePath = "candidates/{candidate}";

		// Here we can also use data provider.
		//Candidate candidateObject = new Candidate(CandidateFirstName+"_Edited", CandidateLastName, "spi504234@yopmail.com","98765432109"); 

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);
		
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		
		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");

	}
	
	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateBySlugWithExpandAll() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("expand", "*");
		
		String basePath = "candidates/{candidate}";

		// `Here we can also use data provider.
		//Candidate candidateObject = new Candidate(CandidateFirstName+"_Edited", CandidateLastName, "spi504234@yopmail.com","98765432109"); 

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);
		
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		
		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");
		
		response.then().body("first_name", Matchers.is(CandidateFirstName));
		response.then().body("created_by.id", Matchers.notNullValue());	
		response.then().body("updated_by.id", Matchers.notNullValue());	
		response.then().body("owner.id", Matchers.notNullValue());	
		response.then().body("qualification.qualification_id", Matchers.notNullValue());	
		response.then().body("currency.currency_id", Matchers.notNullValue());	
		response.then().body("salary_type.id", Matchers.notNullValue());	

	}
	
	
	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByInvalidSlug() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug+"12345677");

		
		String basePath = "candidates/{candidate}";

		// Here we can also use data provider.
		//Candidate candidateObject = new Candidate(CandidateFirstName+"_Edited", CandidateLastName, "spi504234@yopmail.com","98765432109"); 

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 404);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		
		int errorCodeInt = jp.getInt("errorCode");
		boolean error = jp.getBoolean("error");
		
		String errorCodeString = String.valueOf(errorCodeInt);
		String errorString = String.valueOf(error);

		Assert.assertEquals("true", errorString, "error");
		Assert.assertEquals("404", errorCodeString, "errorCode");
		Assert.assertEquals("Candidate doesn't exist", jp.get("errorMessage"), "errorMessage");

		
	}

}
