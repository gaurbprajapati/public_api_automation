package io.recruitcrm.albatross;

import java.io.IOException;

import com.qa.api.util.reaper.ThreadManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.hamcrest.Matchers;
import org.testng.Assert;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.javafaker.albatross.JavaFakerAccountAgreement;
import io.rcrm.api.pojo.albatross.*;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AccountAgreementTest extends TestBase {

	JavaFakerAccountAgreement faker = new JavaFakerAccountAgreement();
	String albatrossTkn;

	@BeforeClass(alwaysRun = true)	public void setUp() throws IOException {
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void saveAccountAgreementTest() {

		SubscriptionAgreement subscriptionAgreement = new SubscriptionAgreement();
		subscriptionAgreement.setTitle(faker.getSubscriptionAgreementTitle());
		subscriptionAgreement.setAgreementContent(faker.getAgreementContent());

		String basePath = "plans-and-billing/save-account-agreement";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true,
				subscriptionAgreement);
	
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("response_message"), "Successfully saved account agreements");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void saveAccountAgreementWithEmptyRequestBody_Test() {

		SubscriptionAgreement subscriptionAgreement = new SubscriptionAgreement();

		String basePath = "plans-and-billing/save-account-agreement";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true,
				subscriptionAgreement);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 404);
		Assert.assertEquals(jsonPath.getString("errorMessage"), "Agreement content is missing");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSaveAccountAgreement_Test() {

		SubscriptionAgreement subscriptionAgreement = new SubscriptionAgreement();
		subscriptionAgreement.setTitle(faker.getSubscriptionAgreementTitle());
		subscriptionAgreement.setAgreementContent(faker.getAgreementContent());

		String basePath = "plans-and-billing/save-account-agreement";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn + "123", null, true,
				subscriptionAgreement);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "saveAccountAgreement", groups = "nightly-build")
	public void getAccountAgreementTest(String getSubscriptionAgreementTitle) {

		SubscriptionAgreement subscriptionAgreement = new SubscriptionAgreement();

		String basePath = "plans-and-billing/get-account-agreements";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true,
				subscriptionAgreement);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("response_message"), "Successfully retrieved account agreements");
		Assert.assertNotNull(jsonPath.getString("data.agreement_data[0].id"));
		Assert.assertNotNull(jsonPath.getString("data.agreement_data[0].url"));
		Assert.assertEquals(jsonPath.getString("data.agreement_data[0].title"), getSubscriptionAgreementTitle);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getEmptyAccountAgreementTest() {

		SubscriptionAgreement subscriptionAgreement = new SubscriptionAgreement();

		String basePath = "plans-and-billing/get-account-agreements";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, subscriptionAgreement);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("response_message"), "Successfully retrieved account agreements");
		Assert.assertEquals(jsonPath.getString("data.agreement_data[0].title"), "Subscription Agreement (2026)");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAccountAgreement_Test() {

		SubscriptionAgreement subscriptionAgreement = new SubscriptionAgreement();
		subscriptionAgreement.setTitle(faker.getSubscriptionAgreementTitle());
		subscriptionAgreement.setAgreementContent(faker.getAgreementContent());

		String basePath = "plans-and-billing/get-account-agreements";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn + "123", null, true,
				subscriptionAgreement);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getAccountAgreementTemplateTest() {

		String basePath = "plans-and-billing/get-account-agreement-template";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTkn, null, null, true);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("response_message"), "Successfully retrieved account agreement template");
		Assert.assertEquals(jsonPath.getString("data.title"), "Subscription Agreement");
		Assert.assertTrue(jsonPath.getString("data.url").contains(""));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAccountAgreementTemplate_Test() {

		String basePath = "plans-and-billing/get-account-agreement-template";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTkn + "123", null, null, true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider(parallel = true)
	public Object[][] saveAccountAgreement() {
		
		SubscriptionAgreement subscriptionAgreement = new SubscriptionAgreement();
		subscriptionAgreement.setTitle(faker.getSubscriptionAgreementTitle());
		subscriptionAgreement.setAgreementContent(faker.getAgreementContent());

		Response response = RestClient.doPost("JSON", albatrossURL, "plans-and-billing/save-account-agreement",
				albatrossTkn, null, true, subscriptionAgreement);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("response_message"), "Successfully saved account agreements");

		return new Object[][] { { faker.getSubscriptionAgreementTitle() } };
	}
}
