package io.rcrm.api.users;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class SearchUsersTest extends TestBase {

	String apiAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchUsersWithId_Test() {

		Response userResponse = RestClient.doGet("JSON", baseURL, "users", apiAuthToken, null, null, true);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		int userId = user.get("[0].id");

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("user_id", String.valueOf(userId));

		Response response = RestClient.doGet("JSON", baseURL, "users/search", apiAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("[0].id", Matchers.is(userId));
		response.then().body("[0].last_name", Matchers.is("1"));
		response.then().body("[0].email", Matchers.is(ThreadManager.getAccount().getOwner().getEmail()));
		response.then().body("[0].email_signature_added", Matchers.is("No"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getSearchData", groups = "nightly-build")
	public void searchUsersWithOtherParameters_Test(String searchParameter, String searchValue, int recordCount) {

		Response userResponse = RestClient.doGet("JSON", baseURL, "users", apiAuthToken, null, null, true);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		int userId = user.get("[0].id");

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put(searchParameter, searchValue);

		Response response = RestClient.doGet("JSON", baseURL, "users/search", apiAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("size()", Matchers.is(recordCount));
		response.then().body("[0].id", Matchers.is(userId));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSearchUsers_Test() {

		Response response = RestClient.doGet("JSON", baseURL, "users/search", apiAuthToken + "123", null, null, true);

		Assert.assertEquals(response.getStatusCode(), 401);

		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider(parallel = true)
	public Object[][] getSearchData() {
		Object data[][] = { { "first_name", "Owner", 1 }, { "last_name", "1", 4 }, { "email", "yopmail.com", 4 },
				{ "contact_number", "555000", 4 } };
		return data;
	}
}