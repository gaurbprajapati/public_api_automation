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
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class EditCompanyTest extends TestBase {

	public EditCompanyTest() {
		super();
	}

	commanFunction function = new commanFunction();
	JavaFakerCompany faker = new JavaFakerCompany();

	String companyName = faker.getCompanyName();
	String companyAbout = faker.getCompanyAbout();
	String companyWebsite = faker.getUrl();
	String contactNumber = faker.getContactNumber();
	String companyCity = faker.getCity();
	String address = faker.getAddress();
	int industry_id = faker.getIndustry_id();
	String logo = faker.getLogoURL();

	String albatrossAuthToken;
	String apiAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void editCompanyBySlug_POST(String companySlug) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", companySlug);

		String basePath = "companies/{company}";

		Company company = new Company();
		company.setCompany_name(companyName);
		company.setAbout_company(companyAbout);
		company.setCity(companyCity);
		company.setAddress(address);
		company.setIndustry_id(industry_id);
		company.setLogo(logo);
		company.setWebsite(companyWebsite);
		company.setLinkedin(companyWebsite);
		company.setTwitter(companyWebsite);
		company.setFacebook(companyWebsite);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null,
				pathParamters, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("company_name", Matchers.is(companyName));
		response.then().body("city", Matchers.is(companyCity));
		response.then().body("address", Matchers.is(address));
		response.then().body("facebook", Matchers.is(companyWebsite));
		response.then().body("twitter", Matchers.is(companyWebsite));
		response.then().body("linkedin", Matchers.is(companyWebsite));
		response.then().body("website", Matchers.is(companyWebsite));
		response.then().body("logo", Matchers.containsString("recruitcrm.net"));
		response.then().body("industry_id", Matchers.is(industry_id));
		response.then().body("about_company", Matchers.is(companyAbout));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "fieldDataMap", groups = "nightly-build")
	public void editCompanyBySlugWithEachField_POST(String fieldName, String fieldData) {

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken)
				.jsonPath().getString("slug");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", companySlug);

		String basePath = "companies/{company}";

		Company company = new Company();
		company.setCompany_name(companyName);
		company.setWebsite("");

		if (fieldName.equals("industry_id")) {
			company.setIndustry_id(Integer.parseInt(fieldData));
		} else if (fieldName.equals("website")) {
			company.setWebsite(fieldData);
		} else if (fieldName.equals("linkedin")) {
			company.setLinkedin(fieldData);
		} else if (fieldName.equals("about_company")) {
			company.setAbout_company(fieldData);
		}

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null,
				pathParamters, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		if (fieldName.equals("industry_id")) {
			response.then().body(fieldName, Matchers.is(Integer.parseInt(fieldData)));
		} else {
			response.then().body(fieldName, Matchers.is(fieldData));
		}
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void editCompanyBySlugWithoutCompanyName422_POST() {

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken)
				.jsonPath().getString("slug");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", companySlug);

		String basePath = "companies/{company}";

		Company company = new Company();
		company.setCompany_name("");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null,
				pathParamters, true, company);

		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body(Matchers.containsString("The company name field is required."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void editCompanyByInvalidSlug404_POST() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", faker.getInvalidCompanySlug());

		String basePath = "companies/{company}";

		Company company = new Company();
		company.setCompany_name(companyName);
		company.setWebsite(companyWebsite);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null,
				pathParamters, true, company);

		Assert.assertEquals(response.getStatusCode(), 404);

		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void editCompanyByInvalidFieldsValues422_POST(String companySlug) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", companySlug);

		String basePath = "companies/{company}";

		Company company = new Company();
		company.setCompany_name("SP");
		company.setIndustry_id(faker.getInvalidIndustryId());
		company.setWebsite("1" + companyWebsite);
		company.setLinkedin("1" + companyWebsite);
		company.setTwitter("1" + companyWebsite);
		company.setFacebook("1" + companyWebsite);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null,
				pathParamters, true, company);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("company_name[0]", Matchers.is("The company name must be at least 3 characters."));
		response.then().body("facebook[0]", Matchers.is("The facebook format is invalid."));
		response.then().body("twitter[0]", Matchers.is("The twitter format is invalid."));
		response.then().body("linkedin[0]", Matchers.is("The linkedin format is invalid."));
		response.then().body("industry_id[0]", Matchers.is("The selected industry id is invalid."));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyAndContactData", groups = "nightly-build")
	public void editCompanyWithExistingContacts_POST(String companySlug, String contactSlug1, String contactSlug2) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", companySlug);

		String basePath = "companies/{company}";

		Company company = new Company();
		company.setCompany_name(companyName);
		company.setContact_slug(contactSlug1 + "," + contactSlug2);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null,
				pathParamters, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("company_name", Matchers.is(companyName));
		response.then().body("contact_slug[0]", Matchers.is(contactSlug1));
		response.then().body("contact_slug[1]", Matchers.is(contactSlug2));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void editCompanyWithRandomContacts_POST(String companySlug) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", companySlug);

		String basePath = "companies/{company}";

		Company company = new Company();
		company.setCompany_name(companyName);
		company.setContact_slug(faker.getInvalidCompanySlug());

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null,
				pathParamters, true, company);

		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("contact_slug[0]", Matchers.is("Invalid contact slug"));
	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyData() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken)
				.jsonPath().getString("slug");

		Object data[][] = { { companySlug } };

		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyAndContactData() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken)
				.jsonPath().getString("slug");
		String contactSlug1 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath()
				.getString("slug");
		String contactSlug2 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath()
				.getString("slug");

		Object data[][] = { { companySlug, contactSlug1, contactSlug2 } };

		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] fieldDataMap() {
		return new Object[][] { { "industry_id", String.valueOf(industry_id) }, { "website", companyWebsite },
				{ "linkedin", companyWebsite }, { "about_company", companyAbout } };
	}

}
