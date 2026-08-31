package io.rcrm.nyma.blockedEmails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.nyma.BlockedEmailsPage;
import io.rcrm.api.pojo.nyma.DomainsPage;
import io.rcrm.api.pojo.nyma.ReceiverEmailsPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class AllEndpointsOfBlockedEmailsTest extends TestBase {
	ArrayList<Object> emailList = new ArrayList<Object>();
	commanFunction function = new commanFunction();
	String emailID = "";
	String domainName = "";

	String recordIDs = "";
	String domainRecordIDS = "";

	String addRandomString = RandomStringUtils.randomAlphabetic(4);


	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getBlockedEmailsTestData", groups = "nightly-build")
	public void blockedEmailsPOST_Test(ArrayList<Object> emailList1) {
		BlockedEmailsPage blockedEmailspge = new BlockedEmailsPage();
		blockedEmailspge.setBlacklist_email_details(emailList1);
		blockedEmailspge.setBlacklist_domain_details(emailList);
		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken(), null, true,
				blockedEmailspge);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Add to Blocked List Successful "));
		response.then().body("action_name", Matchers.containsString("Add to Blocked List"));
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		emailID = jp.get("data.records.email_id[0]");
		int recordID = jp.get("data.records.id[0]");
		recordIDs = Integer.toString(recordID);

	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getBlockedDomainTestData", groups = "nightly-build")
	public void blockedDomainPOST_Test(ArrayList<Object> domainList) {
		BlockedEmailsPage blockedEmailspge = new BlockedEmailsPage();
		blockedEmailspge.setBlacklist_email_details(emailList);
		blockedEmailspge.setBlacklist_domain_details(domainList);
		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken(), null, true,
				blockedEmailspge);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Add to Blocked List Successful "));
		response.then().body("action_name", Matchers.containsString("Add to Blocked List"));
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		domainName = jp.get("data.records.email_id[0]");
		int recordID = jp.get("data.records.id[0]");
		domainRecordIDS = Integer.toString(recordID);

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchBlockedEmailsGET_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("q", emailID);
		String basePath = "blacklist-emailids/search";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchBlockedDomainGET_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("q", domainName);
		String basePath = "blacklist-emailids/search";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void blockedEmailsDELETE_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("recordId", recordIDs);
		String basePath = "blacklist-emailids/{recordId}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Delete from Blocked List Successful "));
		response.then().body("action_name", Matchers.containsString("Delete from Blocked List"));
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void blockedDomainDELETE_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("recordId", domainRecordIDS);
		String basePath = "blacklist-emailids/{recordId}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Delete from Blocked List Successful "));
		response.then().body("action_name", Matchers.containsString("Delete from Blocked List"));

	}

	@DataProvider
	public Object[][] getBlockedEmailsTestData() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		String candidateName = jsonCandidate.get("first_name") + " " + jsonCandidate.get("last_name");
		String candidateEmail = jsonCandidate.get("email");

		ArrayList<Object> emailList = new ArrayList<Object>();
		ReceiverEmailsPage candEmailsPage = new ReceiverEmailsPage();

		candEmailsPage.setEmail(candidateEmail);
		candEmailsPage.setName(candidateName);
		candEmailsPage.setEntity_slug(candidateEntitySlug);
		candEmailsPage.setEntity_type(5);
		emailList.add(candEmailsPage);

		Object data[][] = { { emailList } };
		return data;
	}

	@DataProvider
	public Object[][] getBlockedDomainTestData() {
		ArrayList<Object> domainList = new ArrayList<Object>();
		DomainsPage domainsPage = new DomainsPage();
		domainsPage.setDomain(addRandomString + ".com");
		domainList.add(domainsPage);

		Object data[][] = { { domainList } };
		return data;
	}
}