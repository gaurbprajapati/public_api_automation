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
public class AddToBlockedEmailsTest extends TestBase {
	ArrayList<Object> emailList = new ArrayList<Object>();
	commanFunction function = new commanFunction();
	String emailID = "";
	String recordIDs = "";
	String addRandomString = RandomStringUtils.randomAlphabetic(4);


	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getBlockedEmailsTestData", groups = "nightly-build")
	public void blockedEmailsPOST_Test(ArrayList<Object> emailList1, ArrayList<Object> domainList) {
		BlockedEmailsPage blockedEmailsPage = new BlockedEmailsPage();
		blockedEmailsPage.setBlacklist_email_details(emailList1);
		blockedEmailsPage.setBlacklist_domain_details(domainList);
		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken(), null, true,
				blockedEmailsPage);
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
	@Test(dataProvider = "getBlockedEmailsInvalidTestData", groups = "nightly-build")
	public void blockedInvalidEmailsPOST_Test(ArrayList<Object> emailList1, int statusCode, String statusMessage) {
		BlockedEmailsPage blockedEmailsPage = new BlockedEmailsPage();
		blockedEmailsPage.setBlacklist_email_details(emailList1);
		blockedEmailsPage.setBlacklist_domain_details(emailList);
		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken(), null, true,
				blockedEmailsPage);
		response.then().statusCode(statusCode);
		response.then().body("message", Matchers.containsString(statusMessage));
		// response.then().body("status", Matchers.containsString("fail"));
		// response.then().body("message_type", Matchers.containsString("is-danger"));

	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getBlockedEmailsTestData", groups = "nightly-build")
	public void blockedEmailsInvalidAuth_Test(ArrayList<Object> emailList1, ArrayList<Object> domainList) {
		BlockedEmailsPage blockedEmailsPage = new BlockedEmailsPage();
		blockedEmailsPage.setBlacklist_email_details(emailList1);
		blockedEmailsPage.setBlacklist_domain_details(domainList);

		Response response = RestClient.doPost("JSON", nymaURL, "blacklist-emailids", ThreadManager.getOwnerAlbatrossToken()+"12345", null, true,
				blockedEmailsPage);
		response.then().statusCode(401);
	}

	@DataProvider
	public Object[][] getBlockedEmailsTestData() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String candidateEntitySlug = jsonCandidate.get("slug");
		String candidateName = jsonCandidate.get("first_name") + " " + jsonCandidate.get("last_name");
		String candidateEmail = jsonCandidate.get("email");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), "").jsonPath();
		String contactEntitySlug = jsonContact.get("slug");
		String contactName = jsonContact.get("first_name") + " " + jsonCandidate.get("last_name");
		String contactEmail = jsonContact.get("email");

		ArrayList<Object> emailList1 = new ArrayList<Object>();
		ArrayList<Object> emailList2 = new ArrayList<Object>();
		ArrayList<Object> domainList = new ArrayList<Object>();
		DomainsPage domainsPage = new DomainsPage();
		ReceiverEmailsPage candEmailsPage = new ReceiverEmailsPage();
		ReceiverEmailsPage contEmailsPage = new ReceiverEmailsPage();

		candEmailsPage.setEmail(candidateEmail);
		candEmailsPage.setName(candidateName);
		candEmailsPage.setEntity_slug(candidateEntitySlug);
		candEmailsPage.setEntity_type(5);
		emailList1.add(candEmailsPage);

		contEmailsPage.setEmail(contactEmail);
		contEmailsPage.setName(contactName);
		contEmailsPage.setEntity_slug(contactEntitySlug);
		contEmailsPage.setEntity_type(2);
		emailList2.add(contEmailsPage);
		emailList1.addAll(emailList2);

		domainsPage.setDomain(addRandomString + ".com");
		domainList.add(domainsPage);
		Object data[][] = { { emailList1, domainList } };
		return data;
	}

	@DataProvider
	public Object[][] getBlockedEmailsInvalidTestData() {
		ArrayList<Object> receiverList = new ArrayList<Object>();
		ReceiverEmailsPage emailsPage = new ReceiverEmailsPage();
		emailsPage.setEmail("#@%^%#$@#$@#.com");
		receiverList.add(emailsPage);
		Object data[][] = { { receiverList, 422, "Failed To Add to Blocked List : " },
				{ emailList, 200, "Add to Blocked List Successful " } };
		return data;
	}
}