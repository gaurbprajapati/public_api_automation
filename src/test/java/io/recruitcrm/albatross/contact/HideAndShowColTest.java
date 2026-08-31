package io.recruitcrm.albatross.contact;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.*;
import io.rcrm.api.pojo.albatross.Contact.*;
import io.rcrm.api.restclient.RestClient;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class HideAndShowColTest extends TestBase {

	String albatrossAuthToken;
	String apiAuthToken;
	int ownerAccountID;

	@BeforeClass(alwaysRun = true)	public void Setup() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		ownerAccountID = ThreadManager.getAccount().getAccountId();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getContactHideAndShowColTestData", groups = "nightly-build")
	public void verifyContactHideAndShowCol_Test(String viewType) {
		HideAndShowColContact hideAndShowCol = new HideAndShowColContact();
		hideAndShowCol.setDatatablekey(viewType);

		Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken, null, true,
				hideAndShowCol);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("user.role"), "Account Owner");
		Assert.assertEquals(jsonPath.getInt("user.accountid"), ownerAccountID);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void verifyContactHideAndShowColWithEmptyRequestBody_Test() {
		HideAndShowColContact hideAndShowCol = new HideAndShowColContact();
		hideAndShowCol.setDatatablekey("");

		Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken, null, true,
				hideAndShowCol);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is_danger");
		Assert.assertEquals(jsonPath.getString("message"), "Required data is missing in the request");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetHideAndShowCol_Test() {
		HideAndShowColContact hideAndShowCol = new HideAndShowColContact();
		hideAndShowCol.setDatatablekey("");

		Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken + "abc",
				null, true, hideAndShowCol);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.get("error"), "Unauthorized");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void verifyEntityViewLockSettings_Test() {
		EntityViewLockSettings entityViewLockSettings = new EntityViewLockSettings();
		entityViewLockSettings.setId(ownerAccountID);
		entityViewLockSettings.setKey("entity_view_lock_settings");

		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null,
				true, entityViewLockSettings);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("message"), "Update Field Successful ");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void verifyEntityViewLockSettingsWithEmptyRequestBody_Test() {
		EntityViewLockSettings entityViewLockSettings = new EntityViewLockSettings();
		entityViewLockSettings.setId(ownerAccountID);
		entityViewLockSettings.setKey("");

		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null,
				true, entityViewLockSettings);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.getString("message"), "The key field is required.");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotViewLockSettings_Test() {
		EntityViewLockSettings entityViewLockSettings = new EntityViewLockSettings();
		entityViewLockSettings.setId(ownerAccountID);
		entityViewLockSettings.setKey("");

		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken + "abc",
				null, true, entityViewLockSettings);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.get("error"), "Unauthorized");
	}

	@DataProvider(parallel = true)
	public Object[][] getContactHideAndShowColTestData() {
		return new Object[][] { { "contacts" }, { "contacts_others_view" } };
	}

}
