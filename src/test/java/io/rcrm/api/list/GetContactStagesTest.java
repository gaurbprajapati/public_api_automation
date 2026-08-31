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
public class GetContactStagesTest extends TestBase {

	public GetContactStagesTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getAllContactStages_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "sales-pipeline", ThreadManager.getAccountApiKey(),
				queryParameters,null, true);

		response.then().statusCode(200);
		
		response.then().body("stage_id", Matchers.notNullValue());
		response.then().body("label", Matchers.notNullValue());
	}
	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllContactStages_Test() {
	
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "sales-pipeline", ThreadManager.getAccountApiKey()+"x001",
				queryParameters,null, true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
}
