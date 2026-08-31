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
public class GetCompanyCustomIndustryTest extends TestBase {

	public GetCompanyCustomIndustryTest() {
		super();
	}

	JavaFakerCompany javaFakerCompany = new JavaFakerCompany();

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void getAllCustomIndustries_Test(String industryId, String customIndustry) {

		Response response = RestClient.doPost("JSON", albatrossURL, "industries", ThreadManager.getOwnerAlbatrossToken(), null, true, null);

		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath json = response.jsonPath();
		int size = json.getList("data.customIndustries").size() - 1;

		response.then().body("data.defaultIndustries[0].id", Matchers.notNullValue());
		response.then().body("data.customIndustries[" + size + "].label", Matchers.containsString(customIndustry));
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserGetAllCustomIndustries_Test() {
		
		CompanyIndustry industry = new CompanyIndustry();
		industry.setIndustryLabel(javaFakerCompany.getCompanyName());

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-industry-type", ThreadManager.getOwnerAlbatrossToken()+"x003", null, true,
				industry);
		
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
		String customIndustry = json.get("data.label");

		Object data[][] = { { industryId, customIndustry } };

		return data;
	}

}
