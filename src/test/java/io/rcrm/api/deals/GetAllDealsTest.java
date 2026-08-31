package io.rcrm.api.deals;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllDealsTest extends TestBase{
	
	public GetAllDealsTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	commanFunction function = new commanFunction();
	AllCrudFunctions privateFunction = new AllCrudFunctions();
	JavaFakerDeal faker = new JavaFakerDeal();
	String albatrossTkn;
	String apiAuthToken;
	
	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void showAllDeals_GET() {
		function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "deals", ThreadManager.getAccountApiKey(),
				queryParameters,null, true);

		
		// Verify Response Code and body
				response.then().statusCode(200);
				response.then().body("data[0].id", Matchers.notNullValue());
				response.then().body("current_page", Matchers.comparesEqualTo(1));

	}
	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllDeals() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "10");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "deals", ThreadManager.getAccountApiKey()+"12345", queryParameters, null, true);


		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createDealWithCustomFields", groups = "nightly-build")
	public void verifyCustomFieldValueInShowAllDeals_Test(String date, String date2) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "10");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "deals", apiAuthToken, queryParameters, null, true);
		
		response.then().statusCode(200);
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		response.then().body("data.size()", Matchers.equalTo(2));
		response.then().body("data[0].custom_fields[0].value", Matchers.containsString(date2));
		response.then().body("data[1].custom_fields[0].value", Matchers.containsString(date));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//deal//getAllDeals.json"));
	}
	
	@DataProvider
	public Object[][] createDealWithCustomFields() {
		int entityId1, entityId2, columnId;
		JsonPath jsonDeal1 = function.createNewDealWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String entitySlug = jsonDeal1.get("slug");
		entityId1 = privateFunction.getDealResponse(albatrossURL, albatrossTkn, entitySlug).jsonPath().get("data.deal.id");
		JsonPath jsonDeal2 = function.createNewDealWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		entitySlug = jsonDeal2.get("slug");
		entityId2 = privateFunction.getDealResponse(albatrossURL, albatrossTkn, entitySlug).jsonPath().get("data.deal.id");
		Response response = function.createCustomFieldsResponse(albatrossURL, albatrossTkn, "deal", "dealField", "date", "");
		columnId = response.jsonPath().get("data.custumField.columnid");
		String value1 = faker.getDateCustomFieldValue();
		String value2 = faker.getDateCustomFieldValue();
		privateFunction.updateCustomField("deals", albatrossURL, entityId1, albatrossTkn, "custcolumn" + columnId, value1);
		privateFunction.updateCustomField("deals", albatrossURL, entityId2, albatrossTkn, "custcolumn" + columnId, value2);
		return new Object[][] { { value1, value2 } };
	}
}