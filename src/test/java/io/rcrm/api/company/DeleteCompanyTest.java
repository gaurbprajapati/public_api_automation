package io.rcrm.api.company;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.annotations.*;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class DeleteCompanyTest extends TestBase {

	public DeleteCompanyTest() {
		super();
	}

	JavaFakerCompany faker = new JavaFakerCompany();
	String apiAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCompanyData", groups = "nightly-build")
	public void deleteCompanyBySlug_Test(String slug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", slug);

		String basePath = "companies/{company}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, true);
		response.then().statusCode(200);
		response.then().body(Matchers.containsString("Deleted Successfully!"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//company//deleteCompany.json"));

	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void deleteCompanyByInvalidSlug_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", faker.getInvalidCompanySlug());

		String basePath = "companies/{company}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, true);

		response.then().statusCode(404);
		response.then().body("errorMessage", Matchers.is("Company doesn't exist"));
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void deleteCompanyWithUnauthorizedAccess_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", faker.getInvalidCompanySlug());

		String basePath = "companies/{company}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiAuthToken + "123", null, pathParamters,
				true);
		response.then().statusCode(401);
	}

	@DataProvider(parallel = true)
	public Object[][] createCompanyData() {

		Company company = new Company(faker.getCompanyName(), faker.getCompanyWebsite(), faker.getContactNumber(),
				faker.getLogoURL());

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);
		JsonPath jp = response.jsonPath();

		String slug = jp.get("slug");

		return new Object[][] { { slug } };
	}

}
