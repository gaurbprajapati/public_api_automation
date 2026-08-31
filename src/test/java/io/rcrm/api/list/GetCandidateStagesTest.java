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
public class GetCandidateStagesTest extends TestBase {

	public GetCandidateStagesTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	Object accountAPIKey;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountAPIKey = ThreadManager.getAccountApiKey();
	}

	errorResponseBody errorBody = new errorResponseBody();

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void getAllCandidateStages_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "hiring-pipeline",accountAPIKey , queryParameters, null,
				true);

		response.then().statusCode(200);

		response.then().body("status_id", Matchers.notNullValue());
		response.then().body("label", Matchers.notNullValue());

	}

	@Owner("Sandeep")
	@Test(dataProvider = "getPipelineData", groups = "nightly-build")
	public void getAllCandidateStagesByHiringPipelineID_Test(String hiringPielineID, int statusCode, String message) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("hiring_pipeline_id", hiringPielineID);

		Response response = RestClient.doGet("JSON", baseURL, "hiring-pipeline", accountAPIKey, queryParameters, null,
				true);


		if (statusCode == 200) {
			response.then().statusCode(200);
			response.then().body("status_id", Matchers.notNullValue());
			response.then().body("label", Matchers.notNullValue());

		} else {
			errorBody.verify422ForHotlistEndpoint(response, statusCode, message, true);

		}

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllCandidateStages_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "hiring-pipeline", accountAPIKey+ "x001", queryParameters,
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
