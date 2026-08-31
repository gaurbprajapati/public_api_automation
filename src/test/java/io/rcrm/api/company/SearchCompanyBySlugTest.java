package io.rcrm.api.company;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class SearchCompanyBySlugTest extends TestBase {

	public SearchCompanyBySlugTest() {
		super();
	}

	commanFunction function = new commanFunction();
	JavaFakerCompany faker = new JavaFakerCompany();

	String companyName = faker.getCompanyName();
	String companyWebsite = faker.getUrl();
	String contactNumber = faker.getContactNumber();
	String companyCity = faker.getCity();
	String address = faker.getAddress();
	int industry_id = faker.getIndustry_id();
	String logo = faker.getLogoURL();
	
	String albatrossAuthToken;
	String apiAuthToken;
	
	@BeforeClass(alwaysRun = true)	public void setUp(){
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void searchCompanyByInvalidSlug_GET() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", faker.getInvalidCompanySlug());

		String basePath = "companies/{company}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiAuthToken, null,
				pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Company doesn't exist"));

	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyData() {
		String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath()
				.getString("slug");
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
		company.setContact_slug(contactSlug);

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null,
				true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath jsonPath = response.jsonPath();
		String companySlug = jsonPath.get("slug");

		Object data[][] = { { companySlug, contactSlug } };

		return data;
	}

}