package io.recruitcrm.CandidateService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.pojo.offlimit.MarkCandidateAsAvailable;
import io.rcrm.api.pojo.offlimit.MarkCandidateOffLimit;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCandidateQuickViewCountTest extends TestBase {
	String albatrossAuthToken;
	int ownerAccountID;

	public GetCandidateQuickViewCountTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		ownerAccountID = ThreadManager.getAccount().getAccountId();
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void testGetCountOfCandidatesInQuickViewWithNoCandidates_200() {
		String basePath = "candidates/quick-view-count";
		Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Quick View Count Fetch Successful");
		Assert.assertEquals(response.jsonPath().get("data[0].allCandidates").toString(), 0 + "");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/qVCandCount.json"));

	}

	@Owner("Yash Rampal")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void testGetCountOfCandidatesInQuickViewWithCandidates_200() {

		AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
		albatrossFunctions1.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		String basePath = "candidates/quick-view-count";
		Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Quick View Count Fetch Successful");
		Assert.assertEquals(response.jsonPath().get("data[0].allCandidates").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].myCandidates").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].notInAnyHotlist").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].myWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].allWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].offLimitCandidates").toString(), "0");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/qVCandCount.json"));
	}

	@Owner("Raj Pandey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void testGetCountOfCandidatesInQuickViewWithOffLimitMarkedCandidates_200() {

		AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
		String candidateSlug = albatrossFunctions1.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken())
				.jsonPath().get("data.candidate.slug");
		addToOfflimits(candidateSlug);
		String basePath = "candidates/quick-view-count";
		Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Quick View Count Fetch Successful");
		Assert.assertEquals(response.jsonPath().get("data[0].allCandidates").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].myCandidates").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].notInAnyHotlist").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].myWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].allWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].offLimitCandidates").toString(), "1");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/qVCandCount.json"));

		// Verify that Moving the Offlimit Candidate to Hotlist updates the Count for
		// Not in Any Hotlist.
		String hotlistId = addRecordToHotlist(candidateSlug);
		basePath = "candidates/quick-view-count";
		response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Quick View Count Fetch Successful");
		Assert.assertEquals(response.jsonPath().get("data[0].allCandidates").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].myCandidates").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].notInAnyHotlist").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].myWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].allWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].offLimitCandidates").toString(), "1");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/qVCandCount.json"));

		// Remove from Hotlist and verify the count
		removeRecordFromHotlist(candidateSlug, hotlistId);

		response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Quick View Count Fetch Successful");
		Assert.assertEquals(response.jsonPath().get("data[0].allCandidates").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].myCandidates").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].notInAnyHotlist").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].myWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].allWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].offLimitCandidates").toString(), "1");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/qVCandCount.json"));

		markAsAvailableCandidate(candidateSlug);
		response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Quick View Count Fetch Successful");
		Assert.assertEquals(response.jsonPath().get("data[0].allCandidates").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].myCandidates").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].notInAnyHotlist").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].myWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].allWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].offLimitCandidates").toString(), "0");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/qVCandCount.json"));

	}

	@Owner("Sampurn Chouksey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void testGetCountOfCandidatesInQuickViewInHotlist_200() {

		AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
		String candidateSlug = albatrossFunctions1.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken())
				.jsonPath().get("data.candidate.slug");
		String hotlistId = addRecordToHotlist(candidateSlug);
		String basePath = "candidates/quick-view-count";
		Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Quick View Count Fetch Successful");
		Assert.assertEquals(response.jsonPath().get("data[0].allCandidates").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].myCandidates").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].notInAnyHotlist").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].myWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].allWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].offLimitCandidates").toString(), "0");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/qVCandCount.json"));

		// Remove from Hotlist and verify the count
		removeRecordFromHotlist(candidateSlug, hotlistId);

		response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Quick View Count Fetch Successful");
		Assert.assertEquals(response.jsonPath().get("data[0].allCandidates").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].myCandidates").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].notInAnyHotlist").toString(), "1");
		Assert.assertEquals(response.jsonPath().get("data[0].myWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].allWebsiteApplicants").toString(), "0");
		Assert.assertEquals(response.jsonPath().get("data[0].offLimitCandidates").toString(), "0");
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/qVCandCount.json"));

	}

	@Owner("Gaurav Prajapati")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void testGetCountOfCandidatesInQuickViewWithInvalidToken_401() {
		AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
		albatrossFunctions1.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();

		String basePath = "candidates/quick-view-count";
		String invalidToken = "wrognBererToken"; // Simulating an incorrect token

		Response response = RestClient.doGet("JSON", candidatesURL, basePath, invalidToken, null, null, true);

		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Unauthorised access");
		Assert.assertEquals(response.jsonPath().get("meta.responseType.context").toString(), "Warning");
		Assert.assertEquals(response.jsonPath().get("meta.responseType.code").toString(), "104");
		Assert.assertEquals(response.jsonPath().get("meta.status").toString(), "401");
		Assert.assertEquals(response.jsonPath().get("data").toString(), "Invalid or expired token");

	}

	@Owner("Yash Rampal")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void testGetCountOfCandidatesInQuickViewWithWrongURL_404() {
		AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
		albatrossFunctions1.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();

		String wrongBasePath = "candidates/quick-view-coun"; // Incorrect endpoint
		String correctToken = albatrossAuthToken; // Using a valid token

		Response response = RestClient.doGet("JSON", candidatesURL, wrongBasePath, correctToken, null, null, true);

		Assert.assertEquals(response.getStatusCode(), 404);
		Assert.assertEquals(response.jsonPath().getInt("meta.status"), 404);
		Assert.assertTrue(response.jsonPath().getString("errors[0].message").contains("v2/candidates/quick-view-coun"),
				"Error message should reference the invalid path /v2/candidates/quick-view-coun");
	}

	@Owner("Raj Pandey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void testGetCountOfCandidatesInQuickViewWithWrongMethod_405() {
		AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
		albatrossFunctions1.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();

		String basePath = "candidates/quick-view-count";
		String correctToken = albatrossAuthToken; // Using a valid token

		// Using POST instead of GET to trigger 405 error
		Response response = RestClient.doPost1("JSON", candidatesURL, basePath, correctToken, null, null, true,
				null);

		Assert.assertEquals(response.getStatusCode(), 405);
		Assert.assertEquals(response.jsonPath().getInt("meta.status"), 405);
		Assert.assertTrue(response.jsonPath().getString("errors[0].message").contains("not supported"),
				"Expected 405 error message to indicate method not supported");
	}

	public void addToOfflimits(String candidateSlug) {
		JsonPath jp = RestClient
				.doGet("JSON", baseURL, "off-limit-status", ThreadManager.getAccountApiKey(), null, null, false)
				.jsonPath();
		int statusId = jp.get("[" + 0 + "].id");

		MarkCandidateOffLimit markCandidateOffLimit = new MarkCandidateOffLimit();
		markCandidateOffLimit.setCandidate_slugs(candidateSlug);
		markCandidateOffLimit.setStatus_id(String.valueOf(statusId));
		markCandidateOffLimit.setEnd_date(DateUtil.getTomorrowDateString());
		markCandidateOffLimit.setReason("Test Reason " + RandomStringUtils.randomAlphabetic(4));

		Response response = RestClient.doPost1("JSON", baseURL, "candidates/mark-off-limit",
				ThreadManager.getAccountApiKey(),
				null, null, false, markCandidateOffLimit);
		Assert.assertEquals(response.getStatusCode(), 200);
	}

	public String addRecordToHotlist(String candidateSlug) {
		commanFunction function = new commanFunction();
		JsonPath jsonCandidateHotlist = function
				.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		String candidateHotlistID = jsonCandidateHotlist.getString("id");
		HotlistRelated hotlistRelated = new HotlistRelated();
		hotlistRelated.setRelated(candidateSlug);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", candidateHotlistID);
		String basePath = "hotlists/{hotlist}/add-record";

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true,
				hotlistRelated);
		Assert.assertEquals(response.getStatusCode(), 200);
		return candidateHotlistID;
	}

	public void removeRecordFromHotlist(String candidateSlug, String hotlistID) {
		HotlistRelated hotlistRelated = new HotlistRelated();
		hotlistRelated.setRelated(candidateSlug);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}/remove-record";
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true,
				hotlistRelated);
		Assert.assertEquals(response.getStatusCode(), 200);

	}

	public void markAsAvailableCandidate(String candidateSlug) {
		MarkCandidateAsAvailable markCandidateAsAvailable = new MarkCandidateAsAvailable();
		markCandidateAsAvailable.setCandidate_slugs(candidateSlug);

		Response response = RestClient.doPost1("JSON", baseURL, "candidates/mark-as-available",
				ThreadManager.getAccountApiKey(),
				null, null, false, markCandidateAsAvailable);
		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		Assert.assertEquals(jp.getString("candidate_slugs[0]"), candidateSlug);
		Assert.assertEquals(jp.getString("remark"), "Records Were Updated");
	}

}
