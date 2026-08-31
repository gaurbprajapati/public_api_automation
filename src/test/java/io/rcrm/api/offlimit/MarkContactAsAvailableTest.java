package io.rcrm.api.offlimit;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.offlimit.MarkCandidateAsAvailable;
import io.rcrm.api.pojo.offlimit.MarkContactAsAvailable;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class MarkContactAsAvailableTest extends TestBase {

	public MarkContactAsAvailableTest() {
		super();
	}

	commanFunction function = new commanFunction();
	String contactSlug;

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void markAsAvailableContactWithInvalidSlug() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
		contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");

		MarkContactAsAvailable markContactAsAvailable = new MarkContactAsAvailable();
		markContactAsAvailable.setContact_slugs(contactSlug + RandomStringUtils.randomAlphabetic(5));

		Response response = RestClient.doPost1("JSON", baseURL, "contacts/mark-as-available", ThreadManager.getAccountApiKey(),
				null, null, false, markContactAsAvailable);

		Assert.assertEquals(response.getStatusCode(), 422);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(jp.get("contact_slugs[0]"), "Invalid contact slugs");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void markAsAvailableContactWithInvalidToken() {
		if(contactSlug == null) {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
			contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
		}

		MarkCandidateAsAvailable markCandidateAsAvailable = new MarkCandidateAsAvailable();
		markCandidateAsAvailable.setCandidate_slugs(contactSlug);

		Response response = RestClient.doPost1("JSON", baseURL, "contacts/mark-as-available", ThreadManager.getAccountApiKey()+"123",
				null, null, false, markCandidateAsAvailable);

		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}


}
