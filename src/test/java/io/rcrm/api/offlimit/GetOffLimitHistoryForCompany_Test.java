package io.rcrm.api.offlimit;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import com.qa.api.util.DateUtil;
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
public class GetOffLimitHistoryForCompany_Test extends TestBase {

	public GetOffLimitHistoryForCompany_Test() {
		super();
	}

	commanFunction function = new commanFunction();
	String apiAuthToken;
	
	JavaFakerCompany companyFaker = new JavaFakerCompany();
	JavaFakerDeal faker = new JavaFakerDeal();
	
	@BeforeClass(alwaysRun = true)	public void Setup() {
		apiAuthToken = ThreadManager.getAccountApiKey();
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void getEmptyOffLimitHistoryOfCompany_Test(String slug) {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "companies/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
		response.then().statusCode(200);
		response.then().body("current_page", Matchers.equalTo(1));
		response.then().body("data", Matchers.empty());
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "markCompanyAsOfflimit", groups = "nightly-build")
	public void getOffLimitHistoryOfCompany_Test(String slug, String statusName, String reason) {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "companies/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
		response.then().statusCode(200);
		response.then().body("current_page", Matchers.equalTo(1));
		response.then().body("data.size()", Matchers.equalTo(1));
		response.then().body("data[0].status_label", Matchers.equalToIgnoringCase(statusName));
		response.then().body("data[0].reason", Matchers.equalToIgnoringCase(reason));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//getOffLimitHistory.json"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "markCompanyAsOfflimitAndAvailable", groups = "nightly-build")
	public void getOffLimitHistoryOfAvailableCompany_Test(String slug, String statusName, String reason) {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "companies/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
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
	public void getOffLimitHistoryOfCompanyWithInvalidSlug_Test() {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", companyFaker.getInvalidCompanySlug());

		Response response = RestClient.doGet("JSON", baseURL, "companies/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
		
		response.then().statusCode(404);
		Assert.assertEquals(response.jsonPath().getInt("errorCode"), 404);
		Assert.assertEquals(response.jsonPath().get("errorMessage"), "Record doesn't exist");
	}
	
	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getOffLimitHistoryOfCompanyWithUnauthorizedAccess_Test() {
	    Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", companyFaker.getInvalidCompanySlug());

		Response response = RestClient.doGet("JSON", baseURL, "companies/{slug}/off-limit-history", apiAuthToken + "123", null, pathParams, false);

	    response.then().statusCode(401);
	    Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}
	
	@DataProvider(parallel = true)
	public Object[][] getCompanyData() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath().get("slug");
		return new Object[][]  {{companySlug}};
	}
	
	@DataProvider(parallel = true)
	public Object[][] markCompanyAsOfflimit() {
		String[] companyData = createAndMarkCompanyAsOffLimit();
		String slug = companyData[0];
		String statusName = companyData[1];
		String reason = companyData[2];
		return new Object[][] { {slug, statusName, reason}};
	}

	@DataProvider(parallel = true)
	public Object[][] markCompanyAsOfflimitAndAvailable() {
		String[] companyData = createAndMarkCompanyAsOffLimit();
		String companySlug = companyData[0];

		MarkCompanyAsAvailable markCompanyAsAvailable = new MarkCompanyAsAvailable();
		markCompanyAsAvailable.setCompany_slugs(companySlug);
		markCompanyAsAvailable.setMark_contact_available(true);
		markCompanyAsAvailable.setMark_candidate_available(true);

		Response response = RestClient.doPost1("JSON", baseURL, "companies/mark-as-available", apiAuthToken, null, null, false, markCompanyAsAvailable);
		response.then().statusCode(200);

		String statusName = companyData[1];
		String reason = companyData[2];
		return new Object[][] { {companySlug, statusName, reason}};
	}

	private String[] createAndMarkCompanyAsOffLimit() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath().get("slug");

		Response response = RestClient.doGet("JSON", baseURL, "off-limit-status", apiAuthToken, null, null, false);
		JsonPath jp = response.jsonPath();
		int statusId = jp.get("[0].id");
		String statusName = jp.getString("[0].status_label");
		String reason = companyFaker.getRandomReason();

		MarkCompanyOffLimit markCompanyOffLimit = new MarkCompanyOffLimit();
		markCompanyOffLimit.setCompany_slugs(companySlug);
		markCompanyOffLimit.setStatus_id(String.valueOf(statusId));
		markCompanyOffLimit.setEnd_date(faker.getDealDate());
		markCompanyOffLimit.setReason(reason);
		markCompanyOffLimit.setMark_candidate_off_limit(true);
		markCompanyOffLimit.setMark_contact_off_limit(true);

		Response markOffLimitRes = RestClient.doPost1("JSON", baseURL, "companies/mark-off-limit", apiAuthToken, null, null, false, markCompanyOffLimit);
		markOffLimitRes.then().statusCode(200);

		return new String[] { companySlug, statusName, reason };
	}
}