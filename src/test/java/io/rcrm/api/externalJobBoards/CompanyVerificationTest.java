package io.rcrm.api.externalJobBoards;

import java.util.*;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerUser;
import io.rcrm.api.pojo.albatross.DuplicateMergeSetting;
import io.rcrm.api.pojo.externalJobBoards.MannualVerification;
import io.rcrm.api.pojo.externalJobBoards.SystemVerification;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CompanyVerificationTest extends TestBase {

	JavaFakerUser faker;
	String albatrossTkn;
	String retoolTkn;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		faker = new JavaFakerUser();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
		retoolTkn = "Ajc3BMeOagcJrSigoMpvuBVfd2LyztAkSexuuizs4LYQmsBjDe8UWfcd4zZox95R";
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void verifyAccountWithSameDomain_Test() {

		int accountId = ThreadManager.getAccount().getAccountId();
		String website = "www.yopmail.com";

		updateAccountOwnerWebsite(accountId, website);

		String basePath = "accounts/verify";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, null, null, true, null);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(jsonPath.get("message"), "Account Verified Successfully");
		Assert.assertEquals(jsonPath.get("data.account.website"), website);
		Assert.assertEquals(jsonPath.getInt("user.accountid"), accountId);
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void verifyAccountForVerfiedAccount_Test() {

		int accountId = ThreadManager.getAccount().getAccountId();
		String website = "www.yopmail.com";

		updateAccountOwnerWebsite(accountId, website);

		String basePath = "accounts/verify";

		Response verifyResponse = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, null, null, true, null);

		Assert.assertEquals(verifyResponse.getStatusCode(), 200);
		JsonPath jp = verifyResponse.jsonPath();
		
		Assert.assertEquals(jp.get("message"), "Account Verified Successfully");

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, null, null, true, null);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(jsonPath.get("message"), "Account is already verified.");
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void verifyAccountWithDifferentDomain_Test() {

		int accountId = ThreadManager.getAccount().getAccountId();

		updateAccountOwnerWebsite(accountId, faker.getWebsite());

		String basePath = "accounts/verify";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, null, null, true, null);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(jsonPath.get("message"),
				"Your Account's Website & Account Owner Email don't match. Please check again or click here for a manual verification.");
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void verifyAccountWithoutEmail_Test() {

		String basePath = "accounts/verify";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, null, null, true, null);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(jsonPath.get("message"), "Website URL is required to verify account.");
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotVerifyAccount_Test() {

		String basePath = "accounts/verify";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn + "123", null, null, true, null);

		Assert.assertEquals(response.getStatusCode(), 401);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(jsonPath.get("error"), "Unauthorized");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void verifyManualVerification_Test() {

		String email = ThreadManager.getOwner().getEmail();
		int accountId = ThreadManager.getAccount().getAccountId();
		String website = "www.yopmail.com";

		updateAccountOwnerWebsite(accountId, website);

		MannualVerification manualVerification = new MannualVerification();

		manualVerification.setEmail(email);
		manualVerification.setWebsite(website);
		manualVerification.setComments(faker.getComments());

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("account_id", String.valueOf(accountId));

		String basePath = "accounts/manual-verify/{account_id}";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, null, pathParamters, true,
				manualVerification);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(jsonPath.get("message"), "Manual verification request sent successfully.");
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void verifyManualVerificationForVerfiedAccount_Test() {

		String email = ThreadManager.getOwner().getEmail();
		int accountId = ThreadManager.getAccount().getAccountId();
		String website = "www.yopmail.com";

		updateAccountOwnerWebsite(accountId, website);

		MannualVerification manualVerification = new MannualVerification();

		manualVerification.setEmail(email);
		manualVerification.setWebsite(website);
		manualVerification.setComments(faker.getComments());

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("account_id", String.valueOf(accountId));

		String basePath = "accounts/manual-verify/{account_id}";

		Response verifyResponse = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, null, pathParamters,
				true, manualVerification);

		Assert.assertEquals(verifyResponse.getStatusCode(), 200);
		JsonPath jp = verifyResponse.jsonPath();
		
		Assert.assertEquals(jp.get("message"), "Manual verification request sent successfully.");
		Assert.assertEquals(jp.get("message_type"), "is-success");

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, null, pathParamters, true,
				manualVerification);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(jsonPath.get("message"), "Account is already in manual verification process.");
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void verifyManualVerificationWithoutEmail_Test() {

		String email = ThreadManager.getOwner().getEmail();
		int accountId = ThreadManager.getAccount().getAccountId();

		MannualVerification manualVerification = new MannualVerification();

		manualVerification.setEmail(email);
		manualVerification.setWebsite(faker.getWebsite());
		manualVerification.setComments(faker.getComments());

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("account_id", String.valueOf(accountId));

		String basePath = "accounts/manual-verify/{account_id}";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, null, pathParamters, true,
				manualVerification);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(jsonPath.get("message"), "Website URL is required to verify account.");
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotVerifyManualVerification_Test() {

		MannualVerification manualVerification = new MannualVerification();

		int accountId = ThreadManager.getAccount().getAccountId();

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("account_id", String.valueOf(accountId));

		String basePath = "accounts/manual-verify/{account_id}";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn + "123", null,
				pathParamters, true, manualVerification);

		Assert.assertEquals(response.getStatusCode(), 401);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(jsonPath.get("error"), "Unauthorized");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void verifyCompanyVerificationForVerifiedAccount_Test() {

		String email = ThreadManager.getOwner().getEmail();
		int accountId = ThreadManager.getAccount().getAccountId();
		String website = "www.yopmail.com";

		updateAccountOwnerWebsite(accountId, website);

		SystemVerification systemVerification = new SystemVerification();
		systemVerification.setEmail(email);
		systemVerification.setWebsite(website);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("account_id", String.valueOf(accountId));

		Response verifyResponse = RestClient.doPost1("JSON", albatrossURL, "accounts/verify", albatrossTkn, null, null, true, null);

		Assert.assertEquals(verifyResponse.getStatusCode(), 200);
		JsonPath jp = verifyResponse.jsonPath();
		
		Assert.assertEquals(jp.get("message"), "Account Verified Successfully");

		String basePath = "retool/company-verify/{account_id}";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, retoolTkn, null, pathParamters, true, null);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(jsonPath.get("message"), "Successfully sent verification notification to all users for account ID: " + accountId);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void verifyCompanyVerificationForUnverifiedAccount_Test() {

		int accountId = ThreadManager.getAccount().getAccountId();

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("account_id", String.valueOf(accountId));

		String basePath = "retool/company-verify/{account_id}";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, retoolTkn, null, pathParamters, true,
				null);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(jsonPath.get("message"), "Account is not verified.");
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotVerifyCompanyVerification_Test() {

		int accountId = ThreadManager.getAccount().getAccountId();

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("account_id", String.valueOf(accountId));

		String basePath = "retool/company-verify/{account_id}";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, retoolTkn + "123", null, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 401);
		String responseBody = response.getBody().asString();
		
		Assert.assertTrue(responseBody.contains("Unauthorized"));
	}

	public void updateAccountOwnerWebsite(int accountId, String website) {

		DuplicateMergeSetting updateFields = new DuplicateMergeSetting();
		updateFields.setKey("website");
		updateFields.setValue(website);
		updateFields.setTableFlag("account");
		updateFields.setId(accountId);

		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossTkn, null, true,
				updateFields);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(jsonPath.get("message"), "Field Updated Successfully");
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
	}

}