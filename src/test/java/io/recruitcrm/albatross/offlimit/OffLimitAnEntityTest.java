package io.recruitcrm.albatross.offlimit;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.offlimit.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class OffLimitAnEntityTest extends TestBase {

	commanFunction function = new commanFunction();
	AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	int status_id;
	int contactId = -1;

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void markOffLimitTestWithInvalidAuth() {
		if(contactId == -1) {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
			String slug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
			contactId = Integer.parseInt(albatrossFunctions.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), slug).jsonPath().get("data.contact.id"));
		}
		MarkOffLimit markOffLimit = new MarkOffLimit();
		markOffLimit.setEntity_type_id(2);
		markOffLimit.setEntity_ids(new int[]{contactId});
		markOffLimit.setStatus_id(status_id);
		markOffLimit.setStart_date(DateUtil.getTodayDateString());
		markOffLimit.setEnd_date(DateUtil.getTomorrowDateString());
		markOffLimit.setReason("Test Reason " + generatedString);

		String basePath = "off-limit/mark-off-limit";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123",
				null, null, true, markOffLimit);

		response.then().statusCode(401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void markAsAvailableTestWithInvalidAuth() {
		if(contactId == -1) {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
			String slug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
			contactId = Integer.parseInt(albatrossFunctions.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), slug).jsonPath().get("data.contact.id"));
		}
		MarkAsAvailableCompany markOffLimitCompany = new MarkAsAvailableCompany();
		MarkAsAvailable markOffLimit = new MarkAsAvailable();
		markOffLimit.setEntity_type_id(2);
		markOffLimit.setEntity_ids(new int[]{contactId});

		String basePath = "off-limit/mark-as-available";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, null, true, markOffLimit);

		response.then().statusCode(401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");

	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getOffLimitStatusHistoryTestWithInvalidAuth() {
		if(contactId == -1) {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
			String slug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
			contactId = Integer.parseInt(albatrossFunctions.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), slug).jsonPath().get("data.contact.id"));
		}
		String basePath = "off-limit/history/{entity}/{id}";

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("entity", String.valueOf(2));
		pathParameters.put("id", String.valueOf(contactId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters, true);

		response.then().statusCode(401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}
}
