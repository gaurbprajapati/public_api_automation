package io.rcrm.api.company;

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
import io.rcrm.api.pojo.CompanyIndustry;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;

import io.restassured.module.jsv.JsonSchemaValidator;
import com.qa.api.util.Owner;

@AccountType("Business")
public class CompanyCustomIndustryTest extends TestBase {

	public CompanyCustomIndustryTest() {
		super();
	}

	JavaFakerCompany javaFakerCompany = new JavaFakerCompany();
	String companyName = javaFakerCompany.getCompanyName();

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewCustomIndustry_POST() {

		String customIndustry = javaFakerCompany.getCustomIndustry();
		CompanyIndustry company = new CompanyIndustry();
		company.setCompany_name(javaFakerCompany.getCompanyName());
		company.setCustom_industry(customIndustry);

		Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("industry_id", Matchers.notNullValue());

		response.then().assertThat()
				.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("publicApi//company//createCustomIndustry.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void editNewCustomIndustry_POST(String companySlug, String customIndustry) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", companySlug);

		String basePath = "companies/{company}";

		CompanyIndustry company = new CompanyIndustry();
		company.setCompany_name(javaFakerCompany.getCompanyName());
		company.setCustom_industry(customIndustry);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				company);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("industry_id", Matchers.notNullValue());

		response.then().assertThat()
				.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("publicApi//company//editCustomIndustry.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void getAllCustomIndustries_GET(String companySlug, String customIndustry) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "industries", ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath json = response.jsonPath();
		int size = json.getList("$").size() - 1;
		String label = json.get("[" + size + "].label");
		Assert.assertEquals(label, customIndustry);

		response.then().assertThat()
				.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("publicApi//company//getAllIndustries.json"));
	}

	@DataProvider
	public Object[][] createCustomIndustryDataProvider() {

		String customIndustry = javaFakerCompany.getCustomIndustry();
		CompanyIndustry company = new CompanyIndustry();
		company.setCompany_name(companyName);
		company.setCustom_industry(customIndustry);

		Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company);
		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath json = response.jsonPath();
		String companySlug = json.get("slug");

		Object data[][] = { { companySlug, customIndustry } };

		return data;
	}

}
