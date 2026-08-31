package io.rcrm.api.company;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class AllEndpointsOfCompanyTest extends TestBase {

	public AllEndpointsOfCompanyTest() {
		super();
	}

	JavaFakerCompany javaFakerCompany = new JavaFakerCompany();
	String companyName = javaFakerCompany.getCompanyName();
	String companyWebsite = javaFakerCompany.getCompanyWebsite();
	String contactNumber = javaFakerCompany.getContactNumber();
	String companyAbout = javaFakerCompany.getCompanyAbout();
	String companylogo = javaFakerCompany.getLogoURL();
	String companyCity = javaFakerCompany.getCity();
	String companyAddress = javaFakerCompany.getAddress();
	String url = javaFakerCompany.getUrl();

	String slug;

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewCompany_POST() {

		Company company = new Company(companyName, companyWebsite, contactNumber, companylogo);
		company.setAbout_company(companyAbout);
		company.setCity(companyCity);
		company.setAddress(companyAddress);
		company.setLinkedin(url);
		company.setTwitter(url);
		company.setFacebook(url);

		Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Validate communication fields as null
		validateCommunicationFields(response,"");

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//company//createCompany.json"));

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		slug = jp.get("slug");
		// 2295174
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void verifyValidationOfCreateNewCompany() {

		Company company = new Company("", "", "", "");

		Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company);

		Assert.assertEquals(response.getStatusCode(), 422);

	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewCompany_POST", groups = "nightly-build")
	public void editCompanyBySlug_POST() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", slug);

		String basePath = "companies/{company}";

		// Here we can also use data provider.
		Company company = new Company(companyName, companyWebsite, contactNumber, companylogo);
		company.setAbout_company("edited " + companyAbout);
		company.setCity("edited " + companyCity);
		company.setAddress("edited " + companyAddress);
		
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				company);

		Assert.assertEquals(response1.getStatusCode(), 200);

		validateCommunicationFields(response1,"");

		response1.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//company//editCompany.json"));

	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void showAllCompanies_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Validate communication fields as null
		validateCommunicationFields(response,"data[0]");

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//company//getAllCompanies.json"));

	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = "createNewCompany_POST", groups = "nightly-build")
	public void searchCompanyByName_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("company_name", companyName);

		Response response = RestClient.doGet("JSON", baseURL, "companies/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//company//searchCompanyByFields.json"));

	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = "createNewCompany_POST", groups = "nightly-build")
	public void searchCompanyBySlug_GET() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", slug);

		String basePath = "companies/{company}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		validateCommunicationFields(response,"");
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//company//searchCompanyBySlug.json"));
	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewCompany_POST", priority = 100, groups = "nightly-build")
	public void deleteCompanyBySlug_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", slug);

		String basePath = "companies/{company}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		// Verify Response Code and body
		response.then().statusCode(200);
		response.then().body(Matchers.is("\"Deleted Successfully!\""));

	}

	private void validateCommunicationFields(Response response, String dataPath) {
		String pathPrefix = dataPath.isEmpty() ? "$" : dataPath;
		List<String> allFields = getAllCommunicationFields();
		for (String field : allFields) {
			response.then().body(pathPrefix, Matchers.hasKey(field));
			response.then().body(pathPrefix + "." + field, Matchers.nullValue());
		}
	}

	private List<String> getAllCommunicationFields() {
		return Arrays.asList(
				"last_meeting_created_on",
				"last_meeting_created_by"
		);
	}

}
