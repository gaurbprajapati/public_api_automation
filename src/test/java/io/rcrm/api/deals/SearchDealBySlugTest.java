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
public class SearchDealBySlugTest extends TestBase{

	public SearchDealBySlugTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	String deal_slug = "";
	commanFunction function = new commanFunction();

	@Owner("Smit Patel")
	@Test(dataProvider="getDealSlug", groups = "nightly-build")
	public void searchDealBySlug_GET(String dealSlug,String dealName,int dealValue ) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", dealSlug);

		String basePath = "deals/{deal}";
		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);


		// Verify Response Code and body
		response.then().statusCode(200);
			response.then().body("id", Matchers.notNullValue());
			response.then().body("name", Matchers.containsString(dealName));
			response.then().body("deal_value", Matchers.is(dealValue));
			response.then().body("deal_stage.id", Matchers.is(1));
		
	}
	
	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void searchDealByInvalidSlug_GET() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", deal_slug+"12345");

		String basePath = "deals/{deal}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);


		//Verify Response Code and body
		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Deal doesn't exist"));

	}
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSearchDealBySlug() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", deal_slug);

		String basePath = "deals/{deal}";
		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters, true);


		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
	@DataProvider
	public Object[][] getDealSlug() {

		JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		 deal_slug=jsonDeal.get("slug");
			String dealName = jsonDeal.get("name");
			int dealValue = jsonDeal.get("deal_value");

		Object data[][] = { { deal_slug,dealName,dealValue } };

		return data;
	}
	
	}


