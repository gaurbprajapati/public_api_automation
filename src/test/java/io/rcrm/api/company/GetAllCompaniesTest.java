package io.rcrm.api.company;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllCompaniesTest extends TestBase {

	public GetAllCompaniesTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	commanFunction function = new commanFunction();
	AllCrudFunctions privateFunction = new AllCrudFunctions();
	JavaFakerCompany faker = new JavaFakerCompany();

	String companyName = faker.getCompanyName();
	String companyWebsite = faker.getUrl();
	String contactNumber = "13456789087654";
	String companyCity = faker.getCity();
	String address = faker.getAddress();
	int industry_id = faker.getIndustry_id();
	String logo = faker.getLogoURL();

	String slug = "";
	
	String apiAuthToken, albatrossTkn;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}
	
	@Owner("Smit Patel")
	@Test(groups = "nightly-build") // (invocationCount = 2)
	public void createNewCompanyWithAllFields_POST() {
		Company company = new Company();
		company.setCompany_name(companyName);
		company.setCity(companyCity);
		company.setAddress(address);
		company.setIndustry_id(industry_id);
		company.setLogo(logo);
		company.setWebsite(companyWebsite);
		company.setLinkedin(companyWebsite);
		company.setTwitter(companyWebsite);
		company.setFacebook(companyWebsite);

		Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null,
				true, company);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		slug = jp.get("slug");
		// 2295174

		// Get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response Code and body
		response.then().statusCode(200);
		response.then().body("company_name", Matchers.is(companyName));
		response.then().body("city", Matchers.is(companyCity));
		response.then().body("address", Matchers.is(address));
		response.then().body("facebook", Matchers.is(companyWebsite));
		response.then().body("twitter", Matchers.is(companyWebsite));
		response.then().body("linkedin", Matchers.is(companyWebsite));
		response.then().body("website", Matchers.is(companyWebsite));
		response.then().body("logo", Matchers.containsString("recruitcrm.net"));
		response.then().body("industry_id", Matchers.is(industry_id));

	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewCompanyWithAllFields_POST", groups = "nightly-build")
	public void showAllCompanies_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response Code and body
		response.then().statusCode(200);
		response.then().body("data.company_name[0]", Matchers.is(companyName));
		response.then().body("data.city[0]", Matchers.is(companyCity));
		response.then().body("data.address[0]", Matchers.is(address));
		response.then().body("data.facebook[0]", Matchers.is(companyWebsite));
		response.then().body("data.twitter[0]", Matchers.is(companyWebsite));
		response.then().body("data.linkedin[0]", Matchers.is(companyWebsite));
		response.then().body("data.website[0]", Matchers.is(companyWebsite));
		// commenting because logo response is depends on queue
		// response.then().body("data.logo[0]",
		// Matchers.containsString("recruitcrm.net"));
		response.then().body("data.industry_id[0]", Matchers.is(industry_id));

	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCompanyWithCustomFields", groups = "nightly-build")
	public void verifyCustomFieldValueInShowAllCompanies_Test(String value, String value2) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "10");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "companies", apiAuthToken, queryParameters, null, true);
	
		response.then().statusCode(200);
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		response.then().body("data.size()", Matchers.equalTo(2));
		response.then().body("data[0].custom_fields[0].value", Matchers.containsString(value2));
		response.then().body("data[1].custom_fields[0].value", Matchers.containsString(value));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//company//getAllCompanies.json"));
	}
	
	@DataProvider
	public Object[][] createCompanyWithCustomFields() {
		int entityId1, entityId2, columnId;
		String companySlug = function.getEntityResponse(baseURL, apiAuthToken, "company");
		entityId1 =  privateFunction.getCompanyResponse(albatrossURL, albatrossTkn, companySlug).jsonPath().get("data.company.id");
		companySlug = function.getEntityResponse(baseURL, apiAuthToken, "company");
		entityId2 =  privateFunction.getCompanyResponse(albatrossURL, albatrossTkn, companySlug).jsonPath().get("data.company.id");
		Response response = function.createCustomFieldsResponse(albatrossURL, albatrossTkn, "company", "companyField", "number", "");
		columnId = response.jsonPath().get("data.custumField.columnid");
		String value1 = String.valueOf(faker.getInvalidCompanyId());
		String value2 = String.valueOf(faker.getInvalidCompanyId());
		privateFunction.updateCustomField("company", albatrossURL, entityId1, albatrossTkn, "custcolumn" + columnId, value1);
		privateFunction.updateCustomField("company", albatrossURL, entityId2, albatrossTkn, "custcolumn" + columnId, value2);
		return new Object[][] { { value1, value2 } };
	}
}
