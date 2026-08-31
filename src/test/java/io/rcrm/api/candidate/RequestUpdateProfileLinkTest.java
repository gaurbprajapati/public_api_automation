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
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class RequestUpdateProfileLinkTest extends TestBase {
	String slug = "";

	public RequestUpdateProfileLinkTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void authorizedUserCanRequestUpdateprofileLink_GET() {

		String candidateSlug;

		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");

		} catch (Exception e) {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");

		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/request-update";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),
				null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("url", Matchers.containsString("recruitcrm.net/update_resume_link/rcrm_" + candidateSlug));

	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void authorizedUserCannotRequestUpdateprofileLinkForInvalidCandidate_GET() {

		String candidateSlug;

		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");

		} catch (Exception e) {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");

		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug + "1234xyz");

		String basePath = "candidates/{candidate}/request-update";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),
				null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("errorMessage", Matchers.containsString("Candidate doesn't exist"));

	}
}
