package io.recruitcrm.CandidateService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rcrm.api.pojo.reaper.Account;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.candidateService.BulkActionsCustomView;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetBulkCustomizationTest extends TestBase {
	String albatrossAuthToken;
	int ownerAccountID;
	int userId;

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		ownerAccountID = ThreadManager.getAccount().getAccountId();

		// Fetch user ID for use in multiple tests
		userId = ThreadManager.getOwner().getUserId();
		Assert.assertNotNull(userId, "User ID should not be null");
	}

	@Owner("Yash Rampal")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void testGetBulkCustomization_UserView_200() {
		String basePath = "custom-view/user-view";
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityId", "1");

		Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "User View Fetched Successfully.");
		Assert.assertEquals(response.jsonPath().getInt("data.listActions.meta.updatedBy"), userId,
				"Mismatch in updatedBy field");

		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/bulkCustomization.json"));
	}

	@Owner("Raj Pandey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void testGetBulkCustomization_AccountView_200() {
		ensureUserViewIsAvailable();
		String basePath = "custom-view/account-view";
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityId", "1");

		Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "Account View Fetched Successfully.");
		Assert.assertEquals(response.jsonPath().getInt("data.listActions.meta.updatedBy"), userId,
				"Mismatch in updatedBy field");
		Assert.assertEquals(response.jsonPath().getInt("data.accountId"), ownerAccountID, "Mismatch in accountId");

		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/bulkCustomization.json"));
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void updateBulkCustomization_UserView_200() {
		ensureUserViewIsAvailable();
		String basePath = "custom-view/user-view";
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityId", "1");

		List<Integer> bulkActionsCustomization = Arrays.asList(1, 2, 6, 7);
		BulkActionsCustomView request = new BulkActionsCustomView(1, bulkActionsCustomization);
		Response response = RestClient.doPut("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters, true,
				request);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "Updated User View Successfully.");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/putBulkCustmomization.json"));

		// Verify changes by retrieving user view again
		Response response2 = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters,
				null, true);
		List<Integer> actualBulkActions = response2.jsonPath().getList("data.listActions.setting.id");
		Assert.assertEquals(actualBulkActions, bulkActionsCustomization, "Bulk Actions mismatch after update!");
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void updateBulkCustomization_AccountView_200() {
		ensureUserViewIsAvailable();
		String basePath = "custom-view/account-view";
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityId", "1");

		List<Integer> bulkActionsCustomization = Arrays.asList(1, 2, 6, 7);
		BulkActionsCustomView accountViewRequest = new BulkActionsCustomView(1, bulkActionsCustomization, 1);
		BulkActionsCustomView userViewRequest = new BulkActionsCustomView(1, bulkActionsCustomization);

		// Fetch tokens for different roles
		String ownerToken = ThreadManager.getAlbatrossToken("Owner");

		// Perform the initial update using the owner/admin token
		Response response = RestClient.doPut("JSON", candidatesURL, basePath, ownerToken, queryParameters, true,
				accountViewRequest);
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "Updated Account View Successfully.");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/putBulkCustmomization.json"));

		// Verify changes
		Response response2 = RestClient.doGet("JSON", candidatesURL, basePath, ownerToken, queryParameters, null, true);
		List<Integer> actualBulkActions = response2.jsonPath().getList("data.listActions.setting.id");
		Assert.assertEquals(actualBulkActions, bulkActionsCustomization, "Bulk Actions mismatch after update!");
		Assert.assertEquals(response2.jsonPath().getInt("data.listActionsLocked"), 1,
				"List Actions was not locked properly!");
	}

	@Owner("Yash Rampal")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void updateBulkCustomizationAccountLockedUnauthorizedModification_401() {
		ensureUserViewIsAvailable();
		String basePath = "custom-view/account-view";
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityId", "1");

		List<Integer> bulkActionsCustomization = Arrays.asList(1, 2, 6, 7);
		BulkActionsCustomView accountViewRequest = new BulkActionsCustomView(1, bulkActionsCustomization, 1);
		BulkActionsCustomView userViewRequest = new BulkActionsCustomView(1, bulkActionsCustomization);

		// Fetch tokens for different roles
		String ownerToken = ThreadManager.getAlbatrossToken("Owner");
		String teamMemberToken = ThreadManager.getAlbatrossToken("TeamMember");

		// Perform the initial update using the owner/admin token
		Response response = RestClient.doPut("JSON", candidatesURL, basePath, ownerToken, queryParameters, true,
				accountViewRequest);
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "Updated Account View Successfully.");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/putBulkCustmomization.json"));

		// Verify changes
		Response response2 = RestClient.doGet("JSON", candidatesURL, basePath, ownerToken, queryParameters, null, true);
		List<Integer> actualBulkActions = response2.jsonPath().getList("data.listActions.setting.id");
		Assert.assertEquals(actualBulkActions, bulkActionsCustomization, "Bulk Actions mismatch after update!");
		Assert.assertEquals(response2.jsonPath().getInt("data.listActionsLocked"), 1,
				"List Actions was not locked properly!");

		// Attempt modification after locking, using a **team member's** token
		Response response3 = RestClient.doPut("JSON", candidatesURL, "custom-view/user-view", teamMemberToken, null,
				true, userViewRequest);
		Assert.assertEquals(response3.getStatusCode(), 401);
		Assert.assertEquals(response3.jsonPath().get("meta.responseType.context"), "Error while processing request",
				"Unexpected response when modifying locked view");
	}

	@Owner("Raj Pandey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void updateBulkCustomizationUserViewUnauthorized_401() {
		String basePath = "custom-view/user-view";
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityId", "1");

		List<Integer> bulkActionsCustomization = Arrays.asList(1, 2, 6, 7);
		BulkActionsCustomView request = new BulkActionsCustomView(1, bulkActionsCustomization);
		String invalidToken = "InvalidBearerToken";

		Response response = RestClient.doPut("JSON", candidatesURL, basePath, invalidToken, queryParameters, true,
				request);

		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void updateBulkCustomizationAccountViewUnauthorized_401() {
		String basePath = "custom-view/account-view";
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityId", "1");

		List<Integer> bulkActionsCustomization = Arrays.asList(1, 2, 6, 7);
		BulkActionsCustomView request = new BulkActionsCustomView(1, bulkActionsCustomization, 1);
		String invalidToken = "InvalidBearerToken";

		Response response = RestClient.doPut("JSON", candidatesURL, basePath, invalidToken, queryParameters, true,
				request);

		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
		Assert.assertEquals(response.jsonPath().get("meta.responseType.context"), "Warning");
	}

	private void ensureUserViewIsAvailable() {
		String basePath = "custom-view/user-view";
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityId", "1");
	
		Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters, null, true);
	
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "User View Fetched Successfully.");
		Assert.assertEquals(response.jsonPath().getInt("data.listActions.meta.updatedBy"), userId,
				"Mismatch in updatedBy field");
	
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/bulkCustomization.json"));
	}
	
}
