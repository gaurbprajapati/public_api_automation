package io.rcrm.api.offlimit;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.*;
import io.rcrm.api.pojo.offlimit.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetOffLimitHistoryForCandidate_Test extends TestBase  {

	public GetOffLimitHistoryForCandidate_Test() {
		super();
	}

	commanFunction function = new commanFunction();
	String apiAuthToken;
	
	JavaFakerCandidate candidateFaker = new JavaFakerCandidate();
	JavaFakerCompany companyFaker = new JavaFakerCompany();
	JavaFakerDeal faker = new JavaFakerDeal();
	
	@BeforeClass(alwaysRun = true)	public void Setup() {
		apiAuthToken = ThreadManager.getAccountApiKey();
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getCandidateData", groups = "nightly-build")
	public void getEmptyOffLimitHistoryOfCandidate_Test(String slug) {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
		response.then().statusCode(200);
		response.then().body("current_page", Matchers.equalTo(1));
		response.then().body("data", Matchers.empty());
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "markCandidateAsOfflimit", groups = "nightly-build")
	public void getOffLimitHistoryOfCandidate_Test(String slug,String statusName, String reason) {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
		response.then().statusCode(200);
		response.then().body("current_page", Matchers.equalTo(1));
		response.then().body("data.size()", Matchers.equalTo(1));
		response.then().body("data[0].status_label", Matchers.equalToIgnoringCase(statusName));
		response.then().body("data[0].reason", Matchers.equalToIgnoringCase(reason));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//getOffLimitHistory.json"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "markCandidateAsOfflimitAndAvailable", groups = "nightly-build")
	public void getOffLimitHistoryOfAvailableCandidate_Test(String slug,String statusName, String reason) {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
		response.then().statusCode(200);
		response.then().body("current_page", Matchers.equalTo(1));
		response.then().body("data.size()", Matchers.equalTo(2));
		response.then().body("data[0].status_label", Matchers.equalToIgnoringCase("available"));
		response.then().body("data[1].status_label", Matchers.equalToIgnoringCase(statusName));
		response.then().body("data[1].reason", Matchers.equalToIgnoringCase(reason));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//getOffLimitHistoryMarkedAvailable.json"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getOffLimitHistoryOfCandidateWithInvalidSlug_Test() {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", candidateFaker.getInvalidCandidateSlug());

		Response response = RestClient.doGet("JSON", baseURL, "candidates/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
		
		response.then().statusCode(404);
		Assert.assertEquals(response.jsonPath().getInt("errorCode"), 404);
		Assert.assertEquals(response.jsonPath().get("errorMessage"), "Record doesn't exist");
	}
	
	
	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getOffLimitHistoryOfCandidateWithUnauthorizedAccess_Test() {
	    Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", candidateFaker.getInvalidCandidateSlug());

		Response response = RestClient.doGet("JSON", baseURL, "candidates/{slug}/off-limit-history", apiAuthToken + "123", null, pathParams, false);

	    response.then().statusCode(401);
	    Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}
	
	@DataProvider(parallel = true)
	public Object[][] getCandidateData() {
		String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath().get("slug");
		return new Object[][]  {{candidateSlug}};
	}
	
	@DataProvider(parallel = true)
	public Object[][] markCandidateAsOfflimit() {
		String[] candidateData = createAndMarkCandidateAsOffLimit();
		String slug = candidateData[0];
		String statusName = candidateData[1];
		String reason = candidateData[2];
		return new Object[][] { {slug, statusName, reason}};
	}

	@DataProvider(parallel = true)
	public Object[][] markCandidateAsOfflimitAndAvailable() {
		String[] candidateData = createAndMarkCandidateAsOffLimit();
		String candidateSlug = candidateData[0];

		MarkCandidateAsAvailable markCandidateAsAvailable = new MarkCandidateAsAvailable();
		markCandidateAsAvailable.setCandidate_slugs(candidateSlug);

		Response response = RestClient.doPost1("JSON", baseURL, "candidates/mark-as-available", apiAuthToken, null, null, false, markCandidateAsAvailable);
		response.then().statusCode(200);

		String statusName = candidateData[1];
		String reason = candidateData[2];
		return new Object[][] { {candidateSlug, statusName, reason}};
	}

	private String[] createAndMarkCandidateAsOffLimit() {
		String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath().get("slug");

		Response response = RestClient.doGet("JSON", baseURL, "off-limit-status", apiAuthToken, null, null, false);
		JsonPath jp = response.jsonPath();
		int statusId = jp.get("[0].id");
		String statusName = jp.getString("[0].status_label");
		String reason = companyFaker.getRandomReason();

		MarkCandidateOffLimit markCandidateOffLimit = new MarkCandidateOffLimit();
		markCandidateOffLimit.setCandidate_slugs(candidateSlug);
		markCandidateOffLimit.setStatus_id(String.valueOf(statusId));
		markCandidateOffLimit.setEnd_date(faker.getDealDate());
		markCandidateOffLimit.setReason(reason);

		Response markOffLimitRes = RestClient.doPost1("JSON", baseURL, "candidates/mark-off-limit", apiAuthToken, null, null, false, markCandidateOffLimit);
		markOffLimitRes.then().statusCode(200);

		return new String[] { candidateSlug, statusName, reason };
	}
}
