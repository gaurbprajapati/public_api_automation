package io.rcrm.api.offlimit;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.offlimit.*;
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
public class MarkCandidateAsAvailableTest extends TestBase {

	public MarkCandidateAsAvailableTest() {
		super();
	}

	commanFunction function = new commanFunction();
	String candidateSlug;

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void markAsAvailableCandidateWithInvalidSlug() {
		JsonPath json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		candidateSlug = json.get("slug");

		MarkCandidateAsAvailable markCandidateAsAvailable = new MarkCandidateAsAvailable();
		markCandidateAsAvailable.setCandidate_slugs(candidateSlug + RandomStringUtils.randomAlphabetic(5));

		Response response = RestClient.doPost1("JSON", baseURL, "candidates/mark-as-available", ThreadManager.getAccountApiKey(),
				null, null, false, markCandidateAsAvailable);

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(response.jsonPath().get("candidate_slugs[0]"), "Invalid candidate slugs");
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void markAsAvailableCandidateWithInvalidToken() {
		if(candidateSlug == null)
			candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");

		MarkCandidateAsAvailable markCandidateAsAvailable = new MarkCandidateAsAvailable();
		markCandidateAsAvailable.setCandidate_slugs(candidateSlug);

		Response response = RestClient.doPost1("JSON", baseURL, "candidates/mark-as-available", ThreadManager.getAccountApiKey()+"123",
				null, null, false, markCandidateAsAvailable);

		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}


}
