package io.rcrm.api.emailsequence;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class GetAllEnrollmentStatusesTest extends TestBase {
	public GetAllEnrollmentStatusesTest() {
		// TODO Auto-generated constructor stub
		super();
	}


	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void getAllEnrollmentStatuses_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "enrollment-statuses", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		response.then().statusCode(200);

		response.then().body("stage_id", Matchers.notNullValue());
		response.then().body("label", Matchers.notNullValue());
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getInValidAPIKey", groups = "nightly-build")
	public void unauthorizedUserCannotGetGetAllEnrollmentStatuses_Test(int statusCode,
			String authTokenMapInValid) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "enrollment-statuses", authTokenMapInValid,
				queryParameters, null, true);

		response.then().statusCode(statusCode);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] getInValidAPIKey() {

		Object data[][] = { { 401, ThreadManager.getAccountApiKey()+"x001" } };
		return data;
	}
}
