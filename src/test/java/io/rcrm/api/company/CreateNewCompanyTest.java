package io.rcrm.api.company;

import com.qa.api.util.reaper.ThreadManager;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.CompanyCustomField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateNewCompanyTest extends TestBase {

	public CreateNewCompanyTest() {
		super();
	}

	commanFunction function;
	AllCrudFunctions allCrudFunctions;
	JavaFakerCompany faker;

	String companyName;
	String companyWebsite;
	String contactNumber;
	String companyCity;
	String address;
	int industry_id;
	String logo;
	String apiAuthToken;
	String albatrossAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		function = new commanFunction();
		allCrudFunctions = new AllCrudFunctions();
		faker = new JavaFakerCompany();
		companyName = faker.getCompanyName();
		companyWebsite = faker.getUrl();
		contactNumber = faker.getContactNumber();
		companyCity = faker.getCity();
		address = faker.getAddress();
		industry_id = faker.getIndustry_id();
		logo = faker.getLogoURL();
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void createNewCompanyWithMandatoryFields_POST() {
		Company company = new Company(companyName, companyWebsite, contactNumber, "");
		company.setIndustry_id(industry_id);

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("company_name", Matchers.is(companyName));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewCompanyWithEmptyRequestBody_POST() {
		Company company = new Company("", "", "", "");

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("company_name[0]", Matchers.is("The company name field is required."));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
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

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

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
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void verifyInvalidCompanyName() {
		Company company = new Company();
		company.setCompany_name("NA");

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("company_name[0]", Matchers.is("The company name must be at least 3 characters."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void verifyInvalidCompanyLogo() {
		Company company = new Company();
		company.setCompany_name(companyName);
		company.setLogo("WW1");

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("logo[0]",
				Matchers.is("logo should be either a valid url or a valid image with max size of 2 MB"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void verifyEmptyCompanyName() {
		Company company = new Company();
		company.setCompany_name("");

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("company_name[0]", Matchers.is("The company name field is required."));
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void verifyCreateNewCompanyWithInvalidFieldsValue_POST() {
		Company company = new Company();
		company.setCompany_name("SP");
		company.setIndustry_id(faker.getInvalidIndustryId());
		company.setWebsite("1" + companyWebsite);
		company.setLinkedin("1" + companyWebsite);
		company.setTwitter("1" + companyWebsite);
		company.setFacebook("1" + companyWebsite);

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("company_name[0]", Matchers.is("The company name must be at least 3 characters."));
		response.then().body("facebook[0]", Matchers.is("The facebook format is invalid."));
		response.then().body("twitter[0]", Matchers.is("The twitter format is invalid."));
		response.then().body("linkedin[0]", Matchers.is("The linkedin format is invalid."));
		response.then().body("industry_id[0]", Matchers.is("The selected industry id is invalid."));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getContactData", groups = "nightly-build")
	public void createNewCompanyWithExistingContacts_POST(String contactSlug1, String contactSlug2) {
		Company company = new Company();
		company.setCompany_name(companyName);
		company.setContact_slug(contactSlug1 + "," + contactSlug2);

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("company_name", Matchers.is(companyName));
		response.then().body("contact_slug[0]", Matchers.is(contactSlug1));
		response.then().body("contact_slug[1]", Matchers.is(contactSlug2));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewCompanyWithRandomContacts_POST() {
		Company company = new Company();
		company.setCompany_name(companyName);
		company.setContact_slug(faker.getInvalidCompanySlug());

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("contact_slug[0]", Matchers.is("Invalid contact slug"));
	}

	@Owner("Sai Teja SG")
	@Test
	public void createCompanyWithDateTimeCustomField() {

		Response customFieldResponse = allCrudFunctions.createCustomFields(albatrossURL, albatrossAuthToken,
				"date_time", 3);
		Assert.assertEquals(customFieldResponse.getStatusCode(), 200);

		String randomDate = faker.getDateTimeCustomFieldValue();

		CompanyCustomField company = new CompanyCustomField();
		company.setCompany_name(companyName);
		company.setWebsite(companyWebsite);
		company.setContact_number(contactNumber);
		company.setAbout_company(faker.getCompanyAbout());

		List<CompanyCustomField.CustomField> customFields = new ArrayList<>();
		CompanyCustomField.CustomField customField = new CompanyCustomField.CustomField();
		customField.setField_id(1);
		customField.setValue(randomDate);
		customFields.add(customField);

		company.setCustom_fields(customFields);

		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("company_name", Matchers.containsString(companyName));
		response.then().body("custom_fields[0].entity_type", Matchers.is("company"));
		response.then().body("custom_fields[0].field_type", Matchers.is("date_time"));
		response.then().body("custom_fields[0].value", Matchers.startsWith(randomDate.substring(0, 19)));
	}

	@DataProvider(parallel = true)
	public Object[][] getContactData() {
		String contactSlug1 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath().getString("slug");
		String contactSlug2 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath().getString("slug");

		Object data[][] = { { contactSlug1, contactSlug2 } };

		return data;
	}
}
