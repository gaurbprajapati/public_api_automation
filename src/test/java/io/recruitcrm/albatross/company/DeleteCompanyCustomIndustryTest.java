package io.recruitcrm.albatross.company;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.albatross.CompanyIndustry;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DeleteCompanyCustomIndustryTest extends TestBase {

	public DeleteCompanyCustomIndustryTest() {
		super();
	}

	JavaFakerCompany javaFakerCompany = new JavaFakerCompany();

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void deleteCompanyCustomIndustry_Test(String industryId) {		
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("industry_id", industryId);

		String basePath = "custom-industry-type/{industry_id}";

		Response response = RestClient.doDelete("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.containsString("Industry type deleted successfully"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void deleteInvalidCompanyCustomIndustry_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("industry_id", javaFakerCompany.getRandomId());

		String basePath = "custom-industry-type/{industry_id}";

		Response response = RestClient.doDelete("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.containsString("Industry type does not exists"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void deleteNullCompanyCustomIndustry_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("industry_id", null);

		String basePath = "custom-industry-type/{industry_id}";

		Response response = RestClient.doDelete("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.containsString("Industry type does not exists"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}
	
	@Owner("Sai Teja SG")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void unauthorizedUserCreateNewCompanyCustomIndustry_Test(String industryId) {
		
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("industry_id", industryId);

		String basePath = "custom-industry-type/{industry_id}";

		Response response = RestClient.doDelete("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParamters, true);
		
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] createCustomIndustryDataProvider() {

		CompanyIndustry industry = new CompanyIndustry();
		industry.setIndustryLabel(javaFakerCompany.getCompanyName());

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-industry-type", ThreadManager.getOwnerAlbatrossToken(), null, true,
				industry);

		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath json = response.jsonPath();
		int id = json.get("data.id");
		String industryId = String.valueOf(id);

		Object data[][] = { { industryId } };

		return data;
	}

}
