package io.recruitcrm.albatross.externalPages;

import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

public class GetAddress_Test extends TestBase {

	private Map<String, String> getCorsHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("origin", "https://stagingweb.recruitcrm.net");
		return headers;
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getAddressWithActualDataValidation_Test() {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("searchTerm", "pune");
		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-address", getCorsHeaders(), queryParams, null, true);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalTo("is-success"));
		response.then().body("status", Matchers.equalTo("success"));
		response.then().body("message", Matchers.containsString("Address retrieved successfully"));
		response.then().body("data", Matchers.notNullValue());
		response.then().body("data.size()", Matchers.greaterThan(0));
		response.then().body("data[0].full_address", Matchers.containsString("Pune"));
		response.then().body("data[0].locality", Matchers.equalTo("Pune"));
		response.then().body("data[0].city", Matchers.equalTo("Pune"));
		response.then().body("data[0].state", Matchers.equalTo("Maharashtra"));
		response.then().body("data[0].country", Matchers.equalTo("India"));
		response.then().body("data[0].postal_code", Matchers.equalTo("411001"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "validSearchTermDataProvider", groups = "nightly-build")
	public void getAddressWithValidSearchTerm_Test(String searchTerm) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("searchTerm", searchTerm);
		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-address", getCorsHeaders(), queryParams, null, true);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalTo("is-success"));
		response.then().body("status", Matchers.equalTo("success"));
		response.then().body("message", Matchers.containsString("Address retrieved successfully"));
		response.then().body("data", Matchers.notNullValue());
		response.then().body("data.size()", Matchers.greaterThan(0));
		response.then().body("data[0].full_address", Matchers.notNullValue());
		response.then().body("data[0].locality", Matchers.notNullValue());
		response.then().body("data[0].city", Matchers.notNullValue());
		response.then().body("data[0].state", Matchers.notNullValue());
		response.then().body("data[0].country", Matchers.notNullValue());
		response.then().body("data[0].postal_code", Matchers.notNullValue());
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getAddressWithEmptySearchTerm_Test() {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("searchTerm", "");
		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-address", getCorsHeaders(), queryParams, null, true);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalTo("is-success"));
		response.then().body("status", Matchers.equalTo("success"));
		response.then().body("data", Matchers.empty());
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getAddressWithoutSearchTerm_Test() {
		Map<String, String> queryParams = new HashMap<>();
		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-address", getCorsHeaders(), queryParams, null, true);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalTo("is-success"));
		response.then().body("status", Matchers.equalTo("success"));
		response.then().body("data", Matchers.empty());
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAddressWithSpecialCharactersInSearchTerm_Test() {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("searchTerm", "pune@#$%");
		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-address", getCorsHeaders(), queryParams, null, true);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalTo("is-success"));
		response.then().body("status", Matchers.equalTo("success"));
		response.then().body("data", Matchers.notNullValue());
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getAddressWithNumericSearchTerm_Test() {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("searchTerm", "411001");
		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-address", getCorsHeaders(), queryParams, null, true);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalTo("is-success"));
		response.then().body("status", Matchers.equalTo("success"));
		response.then().body("data", Matchers.notNullValue());
		response.then().body("data.size()", Matchers.greaterThan(0));
		response.then().body("data[0].postal_code", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getAddressWithoutCorsHeaders_Test() {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("searchTerm", "pune");
		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-address", null, queryParams, null, true);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalTo("is-danger"));
		response.then().body("status", Matchers.equalTo("fail"));
		response.then().body("message", Matchers.equalTo("You are not allowed to access this URL."));
		response.then().body("data", Matchers.empty());
		response.then().body("user", Matchers.nullValue());
		response.then().body("notifications", Matchers.notNullValue());
		response.then().body("application_version", Matchers.notNullValue());
	}

	@DataProvider
	public Object[][] validSearchTermDataProvider() {
		Object data[][] = {
				{ "mumbai" },
				{ "delhi" },
				{ "bangalore" },
				{ "New York" },
				{ "London" }
		};
		return data;
	}

}

