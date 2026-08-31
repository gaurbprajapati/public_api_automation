package io.rcrm.api.list;
import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetCandidateQuestionsTest extends TestBase {

	public GetCandidateQuestionsTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void getAllCandidateQuestions_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "candidate-questions", ThreadManager.getAccountApiKey(),
				queryParameters,null, true);

		response.then().statusCode(200);
		
		/*
		 * response.then().body("id", Matchers.notNullValue());
		 * response.then().body("question", Matchers.notNullValue());
		 */
		 
	}
	
	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllCandidateQuestions_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "candidate-questions", ThreadManager.getAccountApiKey() + "x001",
				queryParameters,null, true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

}