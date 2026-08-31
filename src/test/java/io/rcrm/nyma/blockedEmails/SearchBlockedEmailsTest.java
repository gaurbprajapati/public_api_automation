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
public class SearchBlockedEmailsTest extends TestBase {
	commanFunction function = new commanFunction();
	String emailID = "";
	String domainName = "";

	String addRandomString = RandomStringUtils.randomAlphabetic(4);
	ArrayList<Object> emailList = new ArrayList<Object>();


	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getBlockedEmailsTestData", groups = "nightly-build")
	public void searchBlockedEmailsGET_Test(ArrayList<Object> receiverList) {
		BlockedEmailsPage blockedEmailsPage = new BlockedEmailsPage();
		blockedEmailsPage.setBlacklist_email_details(receiverList);
		blockedEmailsPage.setBlacklist_domain_details(emailList);
		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken(), null, true,
				blockedEmailsPage);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		emailID = jp.get("data.records.email_id[0]");
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("q", emailID);
		String basePath = "blacklist-emailids/search";
		Response response1 = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response1.then().body("status", Matchers.containsString("success"));
		response1.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getBlockedDomainTestData", groups = "nightly-build")
	public void searchBlockedDomainGET_Test(ArrayList<Object> domainList) {
		BlockedEmailsPage blockedEmailsPage = new BlockedEmailsPage();
		blockedEmailsPage.setBlacklist_email_details(emailList);
		blockedEmailsPage.setBlacklist_domain_details(domainList);
		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken(), null, true,
				blockedEmailsPage);
		response.then().statusCode(200);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		domainName = jp.get("data.records.email_id[0]");
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("q", domainName);
		String basePath = "blacklist-emailids/search";
		Response response1 = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().statusCode(200);
		response1.then().body("status", Matchers.containsString("success"));
		response1.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchBlockedEmailsInvalidAuth_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("q", emailID);
		String basePath = "blacklist-emailids/search";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null,
				true);
		response.then().statusCode(401);

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchBlockedDomainInvalidAuth_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("q", domainName);
		String basePath = "blacklist-emailids/search";
		Response response = RestClient.doGet("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", queryParameters, null,
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
