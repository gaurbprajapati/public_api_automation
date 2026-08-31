package io.rcrm.api.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import io.rcrm.api.commanfunctions.commanFunction;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import java.util.List;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCollaboratorsTest extends TestBase {

	public GetCollaboratorsTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	Object accountOwnerAPIKey;
	Object albatrossTkn;
	commanFunction function = new commanFunction();

	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountOwnerAPIKey = ThreadManager.getAccountApiKey();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}

	
	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getAllCollaborators_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "collaborators", ThreadManager.getAccountApiKey(),
				queryParameters,null, true);

		response.then().statusCode(200);
		
		response.then().body("id", Matchers.notNullValue());
		response.then().body("first_name", Matchers.notNullValue());
		response.then().body("email", Matchers.notNullValue());

	}
	
	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserShouldNotAccessCollaborators_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "collaborators", ThreadManager.getAccountApiKey() +"1234",
				queryParameters,null, true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getUserValidTestData", groups = "nightly-build")
	public void verifyGetDeactivateUserInAllCollaborators_Test(ArrayList<Integer> userId) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");
		Response response = RestClient.doGet("JSON", baseURL, "collaborators", accountOwnerAPIKey, queryParameters,
				null, true);
		response.then().statusCode(200);
		JsonPath jp = response.jsonPath();
		List<String> userStatus = jp.getList("status");
		List<Integer> allIds = jp.getList("id");
		String userCorrectStatus[] = { "Active", "Active", "Active", "Deactivated" };
		for (int i = 0; i < userId.size(); i++) {
			Assert.assertEquals(userCorrectStatus[i], userStatus.get(i));
			Assert.assertTrue(allIds.contains(userId.get(i)));
		}
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getUserValidTestData", groups = "nightly-build")
	public void verifyGetDeactivateUser_Test(ArrayList<Integer> userId) {
		Response response = RestClient.doGet("JSON", baseURL, "users", accountOwnerAPIKey, null, null, true);

		response.then().statusCode(200);
		JsonPath jp = response.jsonPath();
		List<String> statuses = jp.getList("status");
		List<Integer> allIds = jp.getList("id");
		String userStatus[] = { "Active", "Active", "Active", "Deactivated" };
		for (int i = 0; i < userId.size(); i++) {
			Assert.assertEquals(userStatus[i], statuses.get(i));
			Assert.assertTrue(allIds.contains(userId.get(i)));
		}
	}

	@DataProvider(parallel = true)
	public Object[][] getUserValidTestData() {
		Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
		JsonPath user = userResponse.jsonPath();
		ArrayList<Integer> ids = new ArrayList<>();
		ids.add(user.getInt("[0].id"));
		ids.add(user.getInt("[1].id"));
		ids.add(user.getInt("[2].id"));
		ids.add(user.getInt("[3].id"));
		String teamMemberDeactivatedID = String.valueOf(ids.get(ids.size() - 1));
		String teamMemberFirstName = user.getString("[3].first_name");
		String teamMemberLastNameName = user.getString("[3].last_name");
		Response deactivateUser = function.deactivateUser(teamMemberDeactivatedID, teamMemberFirstName,
				teamMemberLastNameName, albatrossURL, albatrossTkn);
		return new Object[][] { { ids } };
	}
}
