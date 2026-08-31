package io.rcrm.api.list;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.errorResponseBody;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetMultipleHiringPipelineListTest extends TestBase {

	public GetMultipleHiringPipelineListTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	Object accountAPIKey;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountAPIKey = ThreadManager.getAccountApiKey();
	}

	errorResponseBody errorBody = new errorResponseBody();

	@Owner("Sandeep")
	@Test(groups = "nightly-build")
	public void getAllMultipleHiringPipeline_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "hiring-pipelines", accountAPIKey, queryParameters, null,
				true);

		response.then().statusCode(200);

	}

	
	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllMultipleHiringPipeline_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "hiring-pipelines", accountAPIKey + "x001", queryParameters,
				null, true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] getPipelineData() {

		Object data[][] = { { "0", 200, "Success" }, { "99999999", 422, "Pipeline Not Found" } };
		return data;
	}
}
