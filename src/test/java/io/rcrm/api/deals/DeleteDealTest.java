package io.rcrm.api.deals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class DeleteDealTest extends TestBase{

	public DeleteDealTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	String dealSlug = "";
	commanFunction function = new commanFunction();

	@Owner("Smit Patel")
	@Test(dataProvider = "getAddDealData", groups = "nightly-build")
	public void deleteDealBySlug_GET(String deal_Slug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", deal_Slug);

		String basePath = "deals/{deal}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		// Verify Response Code and body
		response.then().statusCode(200);
		response.then().body(Matchers.is("\"Deleted Successfully!\""));
		
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void deleteDealByInvalidSlug_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", dealSlug+"123");

		String basePath = "deals/{deal}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		// Verify Response Code and body
		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Deal doesn't exist"));
		
	}
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotDeleteDeal() {
		JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		 String dealSlug =jsonDeal.get("slug");
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", dealSlug);
		String basePath = "deals/{deal}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters, true);
		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
	@DataProvider
	public Object[][] getAddDealData() {

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = jsonCompany.get("slug");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");
		JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug,contactSlug).jsonPath();
		String jobSlug = jsonJob.get("slug");
		JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey(),companySlug, contactSlug, jobSlug).jsonPath();
		 dealSlug =jsonDeal.get("slug");

		Object data[][] = { { dealSlug } };

		return data;
	}
	}

