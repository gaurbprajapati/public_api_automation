package io.recruitcrm.albatross.candidate;

import com.qa.api.util.reaper.ThreadManager;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.*;
import io.rcrm.api.pojo.albatross.GetEntityColumns;
import io.rcrm.api.pojo.candidateService.HideAndShowColCandidate;
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
	@Test(dataProvider = "getCandidateHideAndShowColTestData", groups = "nightly-build")
	public void verifyCandidateHideAndShowCol_Test(String viewType) {
		HideAndShowColCandidate hideAndShowCol = new HideAndShowColCandidate();
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
	@Test(dataProvider = "getEntityTestData", groups = "nightly-build")
	public void verifyGetEntityColumns_Test(String entityType) {
		GetEntityColumns getEntityColumns = new GetEntityColumns();
		getEntityColumns.setEntity(entityType);
		Response response = RestClient.doPost("JSON", albatrossURL, "global/get-entity-columns", albatrossAuthToken,
				null, true, getEntityColumns);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is_success");
		String entity = jsonPath.getString("data.columns.id.entity");
		switch (entity) {
		case "candidates":
		case "reports_pitch_candidates":
			Assert.assertEquals("candidate", entityType);
			break;
		case "contacts":
			Assert.assertEquals("contact", entityType);
			break;
		case "reports_assigned_candidates":
		case "assigned_candidates":
			Assert.assertEquals("assignjobcandidate", entityType);
			break;
		default:
			break;
		}
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void verifyGetEntityColumnsWithEmptyRequestBody_Test() {
		HideAndShowColCandidate hideAndShowCol = new HideAndShowColCandidate();
		hideAndShowCol.setDatatablekey("");

		Response response = RestClient.doPost("JSON", albatrossURL, "global/get-entity-columns", albatrossAuthToken,
				null, true, hideAndShowCol);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 500);
		Assert.assertTrue(jsonPath.getString("message").contains("No such file or directory"),
				"Error message is not as expected");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetEntityColumn_Test() {
		HideAndShowColCandidate hideAndShowCol = new HideAndShowColCandidate();
		hideAndShowCol.setDatatablekey("");

		Response response = RestClient.doPost("JSON", albatrossURL, "global/save-state", albatrossAuthToken + "abc",
				null, true, hideAndShowCol);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.get("error"), "Unauthorized");
	}

	@DataProvider(parallel = true)
	public Object[][] getEntityTestData() {
		return new Object[][] { { "candidates" }, { "contacts" }, { "reports_pitch_candidates" },
				{ "reports_assigned_candidates" }, { "assigned_candidates" } };
	}

	@DataProvider(parallel = true)
	public Object[][] getCandidateHideAndShowColTestData() {
		return new Object[][] { { "candidates" }, { "candidates_others_view" }, { "assigned_candidates_others_view" },
				{ "reports_assigned_candidates" }, { "reports_assigned_candidates_others_view" },
				{ "reports_pitch_candidates" }, { "reports_pitch_candidates_others_view" } };
	}
}
