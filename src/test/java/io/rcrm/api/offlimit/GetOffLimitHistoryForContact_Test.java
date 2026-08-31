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
public class GetOffLimitHistoryForContact_Test extends TestBase {

	public GetOffLimitHistoryForContact_Test() {
		super();
	}

	commanFunction function = new commanFunction();
	String apiAuthToken;
	
	JavaFakerContact contactFaker = new JavaFakerContact();
	JavaFakerCompany companyFaker = new JavaFakerCompany();
	JavaFakerDeal faker = new JavaFakerDeal();
	
	@BeforeClass(alwaysRun = true)	public void Setup() {
		apiAuthToken = ThreadManager.getAccountApiKey();
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getContactData", groups = "nightly-build")
	public void getEmptyOffLimitHistoryOfContact_Test(String slug) {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
		response.then().statusCode(200);
		response.then().body("current_page", Matchers.equalTo(1));
		response.then().body("data", Matchers.empty());
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "markContactAsOfflimit", groups = "nightly-build")
	public void getOffLimitHistoryOfContact_Test(String slug, String statusName, String reason) {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
		response.then().statusCode(200);
		response.then().body("current_page", Matchers.equalTo(1));
		response.then().body("data.size()", Matchers.equalTo(1));
		response.then().body("data[0].status_label", Matchers.equalToIgnoringCase(statusName));
		response.then().body("data[0].reason", Matchers.equalToIgnoringCase(reason));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//getOffLimitHistory.json"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "markContactAsOfflimitAndAvailable", groups = "nightly-build")
	public void getOffLimitHistoryOfAvailableContact_Test(String slug, String statusName, String reason) {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
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
	public void getOffLimitHistoryOfContactWithInvalidSlug_Test() {
		Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", contactFaker.getInvalidContactSlug());

		Response response = RestClient.doGet("JSON", baseURL, "contacts/{slug}/off-limit-history", apiAuthToken, null, pathParams, false);
		
		response.then().statusCode(404);
		Assert.assertEquals(response.jsonPath().getInt("errorCode"), 404);
		Assert.assertEquals(response.jsonPath().get("errorMessage"), "Record doesn't exist");
	}
	
	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getOffLimitHistoryOfContactWithUnauthorizedAccess_Test() {
	    Map<String, String> pathParams = new HashMap<>();
	    pathParams.put("slug", contactFaker.getInvalidContactSlug());

		Response response = RestClient.doGet("JSON", baseURL, "contacts/{slug}/off-limit-history", apiAuthToken + "123", null, pathParams, false);

	    response.then().statusCode(401);
	    Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}
	
	@DataProvider(parallel = true)
	public Object[][] getContactData() {
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String companySlug = jsonCompany.get("slug");
		String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath().get("slug");
		return new Object[][]  {{contactSlug}};
	}
	
	@DataProvider(parallel = true)
	public Object[][] markContactAsOfflimit() {
		String[] contactData = createAndMarkContactAsOffLimit();
		String slug = contactData[0];
		String statusName = contactData[1];
		String reason = contactData[2];
		return new Object[][] { {slug, statusName, reason}};
	}

	@DataProvider(parallel = true)
	public Object[][] markContactAsOfflimitAndAvailable() {
		String[] contactData = createAndMarkContactAsOffLimit();
		String contactSlug = contactData[0];

		MarkContactAsAvailable markContactAsAvailable = new MarkContactAsAvailable();
		markContactAsAvailable.setContact_slugs(contactSlug);

		Response response = RestClient.doPost1("JSON", baseURL, "contacts/mark-as-available", apiAuthToken, null, null, false, markContactAsAvailable);
		response.then().statusCode(200);

		String statusName = contactData[1];
		String reason = contactData[2];
		return new Object[][] { {contactSlug, statusName, reason}};
	}

	private String[] createAndMarkContactAsOffLimit() {
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String companySlug = jsonCompany.get("slug");
		String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath().get("slug");

		Response response = RestClient.doGet("JSON", baseURL, "off-limit-status", apiAuthToken, null, null, false);
		JsonPath jp = response.jsonPath();
		int statusId = jp.get("[0].id");
		String statusName = jp.getString("[0].status_label");
		String reason = companyFaker.getRandomReason();

		MarkContactOffLimit markContactOffLimit = new MarkContactOffLimit();
		markContactOffLimit.setContact_slugs(contactSlug);
		markContactOffLimit.setStatus_id(String.valueOf(statusId));
		markContactOffLimit.setEnd_date(faker.getDealDate());
		markContactOffLimit.setReason(reason);

		Response markOffLimitRes = RestClient.doPost1("JSON", baseURL, "contacts/mark-off-limit", apiAuthToken, null, null, false, markContactOffLimit);
		markOffLimitRes.then().statusCode(200);

		return new String[] { contactSlug, statusName, reason };
	}
}