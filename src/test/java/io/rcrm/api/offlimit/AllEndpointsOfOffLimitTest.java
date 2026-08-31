package io.rcrm.api.offlimit;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus;
import io.rcrm.api.pojo.offlimit.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import io.rcrm.api.testbase.TestBase.AccountType;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointsOfOffLimitTest extends TestBase {
	public AllEndpointsOfOffLimitTest() {
		super();
	}

	commanFunction function = new commanFunction();
	String generatedRandomString = RandomStringUtils.randomAlphabetic(4);
	String endDate = DateUtil.getTomorrowDateString();
	String statusName;
	int statusId;
	String candidateSlug;
	String companySlug;
	String contactSlug;

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOffLimitStatus() {
		//create new off limit status (albatross)
		addOffLimitStatus();

		Response response = RestClient.doGet("JSON", baseURL, "off-limit-status", ThreadManager.getAccountApiKey(), null, null, false);
		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		int size = jp.get("size()");
		int i = -1;
		for (i = 0; i < size; i++) {
			if (jp.get("[" + i + "].status_label").equals(statusName)) {
				break;
			}
		}
		if (i == -1) {
			Assert.fail("Status not found");
		}
		response.then().body("[" + i + "].id", Matchers.notNullValue());
		response.then().body("[" + i + "].status_label", Matchers.is(statusName));
		response.then().body("[" + i + "].status_colour_id", Matchers.is("A1"));
		response.then().body("[" + i + "].sequence_no", Matchers.is(1));
		response.then().body("[" + i + "].account_id", Matchers.is(ThreadManager.getAccount().getAccountId()));
		response.then().body("[" + i + "].default", Matchers.is(0));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//getOffLimitStatus.json"));

		statusId = jp.get("[" + i + "].id");
	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = {"getOffLimitStatus"}, groups = "nightly-build")
	public void markCandidateAsOffLimit() {
		JsonPath json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		candidateSlug = json.get("slug");

		MarkCandidateOffLimit markCandidateOffLimit = new MarkCandidateOffLimit();
		markCandidateOffLimit.setCandidate_slugs(candidateSlug);
		markCandidateOffLimit.setStatus_id(String.valueOf(statusId));
		markCandidateOffLimit.setEnd_date(endDate);
		markCandidateOffLimit.setReason("Test Reason " + generatedRandomString);

		Response response = RestClient.doPost1("JSON", baseURL, "candidates/mark-off-limit", ThreadManager.getAccountApiKey(),
				null, null, false, markCandidateOffLimit);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		Assert.assertEquals(jp.getInt("status_id"), statusId);
		Assert.assertEquals(jp.getString("reason"), "Test Reason " + generatedRandomString);
		Assert.assertEquals(jp.getString("end_date"), endDate);
		Assert.assertEquals(jp.getString("candidate_slugs[0]"), candidateSlug);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//markCandidateAsOffLimit.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = {"getOffLimitStatus"}, groups = "nightly-build")
	public void markCompanyAsOffLimit() {
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		companySlug = jsonCompany.get("slug");

		MarkCompanyOffLimit markCompanyOffLimit = new MarkCompanyOffLimit();
		markCompanyOffLimit.setCompany_slugs(companySlug);
		markCompanyOffLimit.setStatus_id(String.valueOf(statusId));
		markCompanyOffLimit.setEnd_date(endDate);
		markCompanyOffLimit.setReason("Test Reason " + generatedRandomString);
		markCompanyOffLimit.setMark_candidate_off_limit(true);
		markCompanyOffLimit.setMark_contact_off_limit(true);

		Response response = RestClient.doPost1("JSON", baseURL, "companies/mark-off-limit", ThreadManager.getAccountApiKey(),
				null, null, false, markCompanyOffLimit);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		Assert.assertEquals(jp.getInt("status_id"), statusId);
		Assert.assertEquals(jp.getString("reason"), "Test Reason " + generatedRandomString);
		Assert.assertEquals(jp.getString("end_date"), endDate);
		Assert.assertEquals(jp.getString("company_slugs[0]"), companySlug);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//markCompanyAsOffLimit.json"));

	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = {"getOffLimitStatus"}, groups = "nightly-build")
	public void markContactAsOffLimit() {
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = jsonCompany.get("slug");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		contactSlug = jsonContact.get("slug");

		MarkContactOffLimit markContactOffLimit = new MarkContactOffLimit();
		markContactOffLimit.setContact_slugs(contactSlug);
		markContactOffLimit.setStatus_id(String.valueOf(statusId));
		markContactOffLimit.setEnd_date(endDate);
		markContactOffLimit.setReason("Test Reason " + generatedRandomString);

		Response response = RestClient.doPost1("JSON", baseURL, "contacts/mark-off-limit", ThreadManager.getAccountApiKey(),
				null, null, false, markContactOffLimit);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		Assert.assertEquals(jp.getInt("status_id"), statusId);
		Assert.assertEquals(jp.getString("reason"), "Test Reason " + generatedRandomString);
		Assert.assertEquals(jp.getString("end_date"), endDate);
		Assert.assertEquals(jp.getString("contact_slugs[0]"), contactSlug);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//markContactAsOffLimit.json"));
	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = {"markCandidateAsOffLimit"}, groups = "nightly-build")
	public void getCandidatesOffLimit() {
		Response response = RestClient.doGet("JSON", baseURL, "candidates/off-limit", ThreadManager.getAccountApiKey(), null, null, false);

		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		response.then().body("data[0].slug", Matchers.is(candidateSlug));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//getCandidateOffLimit.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = {"markContactAsOffLimit"}, groups = "nightly-build")
	public void getContactsOffLimit() {
		Response response = RestClient.doGet("JSON", baseURL, "contacts/off-limit", ThreadManager.getAccountApiKey(), null, null, false);

		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		response.then().body("data[0].slug", Matchers.is(contactSlug));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//getContactOffLimit.json"));
	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = {"markCompanyAsOffLimit"}, groups = "nightly-build")
	public void getCompaniesOffLimit() {
		Response response = RestClient.doGet("JSON", baseURL, "companies/off-limit", ThreadManager.getAccountApiKey(), null, null, false);

		response.then().statusCode(200);
		response.then().body("data[0].slug", Matchers.is(companySlug));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//offlimit//getCompanyOffLimit.json"));

	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = {"markContactAsOffLimit"}, groups = "nightly-build")
	public void markAsAvailableContact() {
		MarkContactAsAvailable markContactAsAvailable = new MarkContactAsAvailable();
		markContactAsAvailable.setContact_slugs(contactSlug);

		Response response = RestClient.doPost1("JSON", baseURL, "contacts/mark-as-available", ThreadManager.getAccountApiKey(),
				null, null, false, markContactAsAvailable);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		Assert.assertEquals(jp.getString("contact_slugs[0]"), contactSlug);
		Assert.assertEquals(jp.getString("remark"), "Records Were Updated");
	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = {"markCompanyAsOffLimit"}, groups = "nightly-build")
	public void markAsAvailableCompany() {
		MarkCompanyAsAvailable markCompanyAsAvailable = new MarkCompanyAsAvailable();
		markCompanyAsAvailable.setCompany_slugs(companySlug);
		markCompanyAsAvailable.setMark_contact_available(true);
		markCompanyAsAvailable.setMark_candidate_available(true);

		Response response = RestClient.doPost1("JSON", baseURL, "companies/mark-as-available", ThreadManager.getAccountApiKey(),
				null, null, false, markCompanyAsAvailable);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		Assert.assertEquals(jp.getString("company_slugs[0]"), companySlug);
		Assert.assertEquals(jp.getString("remark"), "Records Were Updated");
		Assert.assertEquals(jp.getBoolean("mark_contact_available"), true);
		Assert.assertEquals(jp.getBoolean("mark_candidate_available"), true);

	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = {"markCandidateAsOffLimit"}, groups = "nightly-build")
	public void markAsAvailableCandidate() {
		MarkCandidateAsAvailable markCandidateAsAvailable = new MarkCandidateAsAvailable();
		markCandidateAsAvailable.setCandidate_slugs(candidateSlug);

		Response response = RestClient.doPost1("JSON", baseURL, "candidates/mark-as-available", ThreadManager.getAccountApiKey(),
				null, null, false, markCandidateAsAvailable);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		Assert.assertEquals(jp.getString("candidate_slugs[0]"), candidateSlug);
		Assert.assertEquals(jp.getString("remark"), "Records Were Updated");
	}

	public void addOffLimitStatus() {
		statusName = "Off Limit " + generatedRandomString;
		OffLimitStatus.offLimitStatus offLimitStatus = new OffLimitStatus.offLimitStatus();
		offLimitStatus.setStatus_label(statusName);
		offLimitStatus.setStatus_colour_id("A1");
		offLimitStatus.setSequence_no(1);
		offLimitStatus.setAccount_id(String.valueOf(ThreadManager.getAccount().getAccountId()));
		offLimitStatus.setDefaultStatus("0");
		offLimitStatus.setOfflimit_status_colour_id("A1");
		offLimitStatus.setBackground_color_hex("#FEF2F2");
		offLimitStatus.setText_color_hex("#B04C4C");
		offLimitStatus.setCount(0);

		OffLimitStatus offLimitStatusBody = new OffLimitStatus();
		offLimitStatusBody.setOffLimitStatus(new OffLimitStatus.offLimitStatus[] {offLimitStatus});

		String basePath = "off-limit/status";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, offLimitStatusBody);
		response.then().statusCode(200);
		response.then().body("data[0].status_label", Matchers.is(statusName));
	}
}
