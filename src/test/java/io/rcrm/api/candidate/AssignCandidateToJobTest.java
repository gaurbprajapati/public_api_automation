package io.rcrm.api.candidate;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AssignCandidateToJobTest extends TestBase {

	String slug = "";

	public AssignCandidateToJobTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void authorizedUserCanAssignCandidateToJobWithValidData() {

		String candidateSlug;
		String jobSlug;
		
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}catch(Exception e ){
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("candidate_slug", Matchers.is(candidateSlug));
		response.then().body("job_slug", Matchers.is(jobSlug));

		// response.then().body("status.status_id", Matchers.is(1));
		response.then().body("status.label", Matchers.is("Assigned"));
		// response.then().body("remark", Matchers.is("Updated"));
		response.then().body("visibility", Matchers.is(1));
		response.then().body("stage_date", Matchers.notNullValue());

		response.then().body("updated_on", Matchers.notNullValue());
		response.then().body("updated_by", Matchers.notNullValue());

	}
	
	
	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void authorizedUserCannotReAssignCandidateToJobSameJob() {

		String candidateSlug;
		String jobSlug;
		
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}catch(Exception e ){
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("candidate_slug", Matchers.is(candidateSlug));
		response.then().body("job_slug", Matchers.is(jobSlug));

		// response.then().body("status.status_id", Matchers.is(1));
		response.then().body("status.label", Matchers.is("Assigned"));
		// response.then().body("remark", Matchers.is("Updated"));
		response.then().body("visibility", Matchers.is(1));
		response.then().body("stage_date", Matchers.notNullValue());

		response.then().body("updated_on", Matchers.notNullValue());
		response.then().body("updated_by", Matchers.notNullValue());
		
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);
		
		Assert.assertEquals(response1.getStatusCode(), 422);

		response1.then().body("errorMessage", Matchers.containsString("Candidate is already assigned to this job"));
		
		
	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void authorizedUserCannotAssignCandidateToInvalidJob() {

		String candidateSlug;
		String jobSlug;
		
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}catch(Exception e ){
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug + "1234xyz");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("job_slug[0]", Matchers.containsString("Invalid job slug"));

	}

	
	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void authorizedUserCannotAssignInvalidCandidateToValidJob() {

		String candidateSlug;
		String jobSlug;
		
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}catch(Exception e ){
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug + "1234xyz");

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 404);

		response.then().body("errorMessage", Matchers.containsString("Candidate doesn't exist"));

	}
	
	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void authorizedUserCannotAssignInvalidCandidateToInvalidJob() {

		String candidateSlug;
		String jobSlug;
		
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}catch(Exception e ){
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug+"1234xyz");

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug + "1234xyz");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("job_slug[0]", Matchers.containsString("Invalid job slug"));
	
	}
	
	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void authorizedUserCannotAssignCandidateToJobWithEmptyQueryParameter() {

		String candidateSlug;
		String jobSlug;
		
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}catch(Exception e ){
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug+"1234xyz");

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug + "1234xyz");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("job_slug[0]", Matchers.containsString("The job slug field is required."));
	
	}
	
	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void authorizedUserCannotAssignCandidateToJobWithEmptyPathParameter() {

		String candidateSlug;
		String jobSlug;
		
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}catch(Exception e ){
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", "");

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug + "1234xyz");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 404);

		//response.then().body("job_slug[0]", Matchers.containsString("The job slug field is required."));
	
	}
	
	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void verifyAssignCandidateToJobOwnerAsAdmin() {
		
		JsonPath jsonPath = function.getUsers(baseURL, ThreadManager.getAccountApiKey()).jsonPath();

		int adminUserId = jsonPath.get("[1].id");
		Object[][] slugData = function.createCandidateAndJobByUserId(baseURL, ThreadManager.getAccountApiKey(),adminUserId);
		String candidateSlug = slugData[0][0].toString();
		String jobSlug = slugData[0][1].toString();
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);
		queryParameters.put("updated_by", String.valueOf(adminUserId));

		Response response2 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
				true, null);
		Assert.assertEquals(response2.getStatusCode(), 200, "assign To Job failed");

		response2.then().body("candidate_slug", Matchers.is(candidateSlug));
		response2.then().body("job_slug", Matchers.is(jobSlug));
		response2.then().body("updated_by", Matchers.is(adminUserId));
		
	}
}
