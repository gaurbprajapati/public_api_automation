package io.rcrm.api.list;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetJobStagesTest extends TestBase {

	public GetJobStagesTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	
	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void getAllJobStages_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "jobs-pipeline", ThreadManager.getAccountApiKey(),
				queryParameters,null, true);

		response.then().statusCode(200);
		
		response.then().body("id", Matchers.notNullValue());
		response.then().body("label", Matchers.notNullValue());
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllJobStages_Test() {

		
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "jobs-pipeline", ThreadManager.getAccountApiKey()+"001",
				queryParameters,null, true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
}
