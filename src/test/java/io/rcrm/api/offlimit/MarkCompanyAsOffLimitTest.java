package io.rcrm.api.offlimit;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus;
import io.rcrm.api.pojo.offlimit.MarkCompanyOffLimit;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class MarkCompanyAsOffLimitTest extends TestBase {
	public MarkCompanyAsOffLimitTest() {
		super();
	}

	commanFunction function = new commanFunction();
	JavaFakerCompany javaFakerCompany = new JavaFakerCompany();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	int generatedInteger = Integer.parseInt(RandomStringUtils.randomNumeric(4));
	String statusName = "Off Limit " + generatedString;
	String endDate = DateUtil.getTomorrowDateString();
	String reason = "Off limit reason " + generatedString;
	String companySlug;
	int statusId;

	@DataProvider
	public Object[][] getInvalidData() {
		String invalidCompanySlug = javaFakerCompany.getInvalidCompanySlug();
		return new Object[][]{
				{invalidCompanySlug, statusId, endDate, reason, true, true},
				{companySlug, generatedInteger, endDate, reason, false, true},
				{companySlug, statusId, String.valueOf(generatedInteger), reason, false, false},
		};
	}

	@BeforeClass(alwaysRun = true)	public void setUp(){
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		companySlug = jsonCompany.get("slug");
		if(statusId == 0) statusId = getOffLimitStatus();
	}


	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvalidData", groups = "nightly-build")
	public void markCompanyAsOffLimitWithInvalidData(String companySlug, int statusId, String endDate, String reason,
													 boolean markCandidateOffLimit, boolean markContactOffLimit) {
		MarkCompanyOffLimit markCompanyOffLimit = new MarkCompanyOffLimit();
		markCompanyOffLimit.setCompany_slugs(companySlug);
		markCompanyOffLimit.setStatus_id(String.valueOf(statusId));
		markCompanyOffLimit.setEnd_date(endDate);
		markCompanyOffLimit.setReason(reason);
		markCompanyOffLimit.setMark_candidate_off_limit(markCandidateOffLimit);
		markCompanyOffLimit.setMark_contact_off_limit(markContactOffLimit);

		Response response = RestClient.doPost1("JSON", baseURL, "companies/mark-off-limit", ThreadManager.getAccountApiKey(),
				null, null, false, markCompanyOffLimit);

		Assert.assertEquals(response.getStatusCode(), 422);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void markCompanyAsOffLimitWithInvalidToken() {
		MarkCompanyOffLimit markCompanyOffLimit = new MarkCompanyOffLimit();
		markCompanyOffLimit.setCompany_slugs(companySlug);
		markCompanyOffLimit.setStatus_id(String.valueOf(statusId));
		markCompanyOffLimit.setEnd_date(endDate);
		markCompanyOffLimit.setReason(reason);
		markCompanyOffLimit.setMark_candidate_off_limit(true);
		markCompanyOffLimit.setMark_contact_off_limit(true);

		Response response = RestClient.doPost1("JSON", baseURL, "companies/mark-off-limit", ThreadManager.getAccountApiKey()+"123",
				null, null, false, markCompanyOffLimit);

		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}


	public void addOffLimitStatus() {
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
		response.then().body("data[0].status_label", Matchers.is("Off Limit " + generatedString));
	}

	public int getOffLimitStatus() {
		//create new off limit status (albatross)
		addOffLimitStatus();
		Response response = RestClient.doGet("JSON", baseURL, "off-limit-status", ThreadManager.getAccountApiKey(), null, null, false);
		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		response.then().body("[0].id", Matchers.notNullValue());
		return statusId = jp.getInt("[0].id");
	}
}
