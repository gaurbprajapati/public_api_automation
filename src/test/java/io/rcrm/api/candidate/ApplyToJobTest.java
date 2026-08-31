package io.rcrm.api.candidate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class ApplyToJobTest extends TestBase {

	String slug = "";

	public ApplyToJobTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	commanFunction function = new commanFunction();

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void authorizedUserCanApplyToJobWithValidData() {

		String candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
		String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/apply";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("candidate_slug", Matchers.is(candidateSlug));
		response.then().body("job_slug", Matchers.is(jobSlug));

		// response.then().body("status.status_id", Matchers.is(1));
		response.then().body("status.label", Matchers.is("Applied"));
		// response.then().body("remark", Matchers.is("Updated"));
		response.then().body("visibility", Matchers.is(1));
		response.then().body("stage_date", Matchers.notNullValue());

	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void authorizedUserCannotApplyToJobWithInvalidJobSlug() {
		String candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
		String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/apply";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug + "1234xyz");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 422);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().body("job_slug[0]", Matchers.containsString("Invalid job slug"));

	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void authorizedUserCannotApplyToJobWithInvalidCandidateSlug() {
		String candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
		String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug + "1234xyz");

		String basePath = "candidates/{candidate}/apply";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 404);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
	
		response.then().body("errorMessage", Matchers.containsString("Candidate doesn't exist"));

	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void authorizedUserCannotApplyToJobWithInvalidCandidateAndJobSlug() {
		String candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
		String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug + "1234xyz");

		String basePath = "candidates/{candidate}/apply";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug + "1234xyz");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 422);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().body("job_slug[0]", Matchers.containsString("Invalid job slug"));

	}

}
