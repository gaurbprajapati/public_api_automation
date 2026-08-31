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
public class CreateCompanyCustomIndustryTest extends TestBase {

	public CreateCompanyCustomIndustryTest() {
		super();
	}

	JavaFakerCompany javaFakerCompany = new JavaFakerCompany();

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewCompanyCustomIndustry_Test() {

		CompanyIndustry industry = new CompanyIndustry();
		industry.setIndustryLabel(javaFakerCompany.getCompanyName());

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-industry-type", ThreadManager.getOwnerAlbatrossToken(), null, true,
				industry);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.containsString("Industry type added successfully"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("data.id", Matchers.notNullValue());
	}
	
	@Owner("Sai Teja SG")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void createExistingCompanyCustomIndustry_Test(String customIndustry) {

		CompanyIndustry industry = new CompanyIndustry();
		industry.setIndustryLabel(customIndustry);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-industry-type", ThreadManager.getOwnerAlbatrossToken(), null, true,
				industry);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.containsString("Industry type already exists"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("data.id", Matchers.notNullValue());
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createEmptyCompanyCustomIndustry_Test() {

		CompanyIndustry industry = new CompanyIndustry();
		industry.setIndustryLabel(null);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-industry-type", ThreadManager.getOwnerAlbatrossToken(), null, true,
				industry);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("message", Matchers.containsString("The industry label field is required."));
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCreateNewCompanyCustomIndustry_Test() {
		
		CompanyIndustry industry = new CompanyIndustry();
		industry.setIndustryLabel(javaFakerCompany.getCompanyName());

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-industry-type", ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
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
		String customIndustry = json.get("data.label");

		Object data[][] = { { customIndustry } };

		return data;
	}

}
