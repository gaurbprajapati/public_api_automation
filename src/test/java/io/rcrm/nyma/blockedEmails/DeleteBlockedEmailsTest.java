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
public class DeleteBlockedEmailsTest extends TestBase {
	commanFunction function = new commanFunction();
	String recordIDs = "";
	String domainRecordIDs = "";
	String addRandomString = RandomStringUtils.randomAlphabetic(4);
	ArrayList<Object> emailList = new ArrayList<Object>();


	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getBlockedEmailsTestData", groups = "nightly-build")
	public void blockedEmailsDELETE_Test(ArrayList<Object> emailsList) {
		BlockedEmailsPage blockedEmailsPage = new BlockedEmailsPage();
		blockedEmailsPage.setBlacklist_email_details(emailsList);
		blockedEmailsPage.setBlacklist_domain_details(emailList);
		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken(), null, true,
				blockedEmailsPage);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		int recordID = jp.get("data.records.id[0]");
		recordIDs = Integer.toString(recordID);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("recordId", recordIDs);
		String basePath = "blacklist-emailids/{recordId}";

		Response response1 = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		response1.then().statusCode(200);
		response1.then().body("status", Matchers.containsString("success"));
		response1.then().body("message_type", Matchers.containsString("is-success"));
		response1.then().body("message", Matchers.containsString("Delete from Blocked List Successful "));
		response1.then().body("action_name", Matchers.containsString("Delete from Blocked List"));

	}
	

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getBlockedDomainTestData", groups = "nightly-build")
	public void blockedDomainDELETE_Test(ArrayList<Object> domainList) {
		BlockedEmailsPage blockedEmailsPage = new BlockedEmailsPage();
		blockedEmailsPage.setBlacklist_email_details(emailList);
		blockedEmailsPage.setBlacklist_domain_details(domainList);
		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken(), null, true,
				blockedEmailsPage);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		int recordID = jp.get("data.records.id[0]");
		domainRecordIDs = Integer.toString(recordID);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("recordId", domainRecordIDs);
		String basePath = "blacklist-emailids/{recordId}";

		Response response1 = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		response1.then().statusCode(200);
		response1.then().body("status", Matchers.containsString("success"));
		response1.then().body("message_type", Matchers.containsString("is-success"));
		response1.then().body("message", Matchers.containsString("Delete from Blocked List Successful "));
		response1.then().body("action_name", Matchers.containsString("Delete from Blocked List"));

	}
	
	@Owner("Harika")
	@Test(dataProvider = "getBlockedDomainTestData", groups = "nightly-build")
	public void blockedDomainDELETE422_Test(ArrayList<Object> domainList) {
		BlockedEmailsPage blockedEmailsPage = new BlockedEmailsPage();
		blockedEmailsPage.setBlacklist_email_details(emailList);
		blockedEmailsPage.setBlacklist_domain_details(domainList);
		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken(), null, true,
				blockedEmailsPage);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		int recordID = jp.get("data.records.id[0]");
		domainRecordIDs = Integer.toString(recordID);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("recordId", " ");
		String basePath = "blacklist-emailids/{recordId}";

		Response response1 = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		response1.then().statusCode(422);

	}
    
	@Owner("Harika")
	@Test(dataProvider = "getBlockedEmailsTestData", groups = "nightly-build")
	public void blockedEmailsDELETE422_Test(ArrayList<Object> emailsList) {
		BlockedEmailsPage blockedEmailsPage = new BlockedEmailsPage();
		blockedEmailsPage.setBlacklist_email_details(emailsList);
		blockedEmailsPage.setBlacklist_domain_details(emailList);
		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken(), null, true,
				blockedEmailsPage);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		int recordID = jp.get("data.records.id[0]");
		recordIDs = Integer.toString(recordID);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("recordId", " ");
		String basePath = "blacklist-emailids/{recordId}";

		Response response1 = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		response1.then().statusCode(422);

	}
	
	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void deleteBlockedEmailsInvalidAuth_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("recordId", recordIDs);
		String basePath = "blacklist-emailids/{recordId}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"12345", null, pathParamters,
				true);

		response.then().statusCode(401);

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void deleteBlockedDomainInvalidAuth_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("recordId", domainRecordIDs);
		String basePath = "blacklist-emailids/{recordId}";

		Response response = RestClient.doDelete("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"12345", null, pathParamters,
				true);

		response.then().statusCode(401);

	}

	@DataProvider
	public Object[][] getBlockedEmailsTestData() {
		ArrayList<Object> emailsList = new ArrayList<Object>();
		ReceiverEmailsPage emailsPage = new ReceiverEmailsPage();
		emailsPage.setEmail("rcrmtest" + addRandomString + "@yopmail.com");
		emailsList.add(emailsPage);
		Object data[][] = { { emailsList } };
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