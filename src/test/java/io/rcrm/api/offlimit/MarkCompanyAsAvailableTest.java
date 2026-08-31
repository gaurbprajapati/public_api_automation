package io.rcrm.api.offlimit;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.offlimit.MarkCandidateAsAvailable;
import io.rcrm.api.pojo.offlimit.MarkCompanyAsAvailable;
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
public class MarkCompanyAsAvailableTest extends TestBase {

	public MarkCompanyAsAvailableTest() {
		super();
	}

	commanFunction function = new commanFunction();
	String companySlug;
	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void markAsAvailableCompanyWithInvalidSlug() {
		if(companySlug == null)
			companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");

		MarkCompanyAsAvailable markCompanyAsAvailable = new MarkCompanyAsAvailable();
		markCompanyAsAvailable.setCompany_slugs(companySlug + RandomStringUtils.randomAlphabetic(5));
		markCompanyAsAvailable.setMark_contact_available(true);
		markCompanyAsAvailable.setMark_candidate_available(true);

		Response response = RestClient.doPost1("JSON", baseURL, "companies/mark-as-available", ThreadManager.getAccountApiKey(),
				null, null, false, markCompanyAsAvailable);

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(response.jsonPath().get("company_slugs[0]"), "Invalid company slugs");
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void markAsAvailableCompanyWithInvalidToken() {
		if(companySlug == null)
			companySlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");

		MarkCompanyAsAvailable markCompanyAsAvailable = new MarkCompanyAsAvailable();
		markCompanyAsAvailable.setCompany_slugs(companySlug);
		markCompanyAsAvailable.setMark_contact_available(true);
		markCompanyAsAvailable.setMark_candidate_available(true);

		Response response = RestClient.doPost1("JSON", baseURL, "companies/mark-as-available", ThreadManager.getAccountApiKey()+"123",
				null, null, false, markCompanyAsAvailable);

		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}


}
