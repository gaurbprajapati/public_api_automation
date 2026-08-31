package io.recruitcrm.albatross.company;

import org.testng.Assert;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerContact;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditCompanyTest extends TestBase {

	public EditCompanyTest() {
		super();
	}

	JavaFakerCompany faker = new JavaFakerCompany();
	commanFunction function = new commanFunction();
	String companyName = faker.getCompanyName();
	String companyWebsite = faker.getUrl();
	String companyCity = faker.getCity();
	String address = faker.getAddress();
	int industry_id = faker.getIndustry_id();
	String logo = faker.getLogoURL();

	JavaFakerContact contactFaker = new JavaFakerContact();
	String contactFirstName = contactFaker.getFirstName();
	String contactLastName = contactFaker.getLastName();
	
	String albatrossAuthToken;
	String apiAuthToken;
	
	@BeforeClass(alwaysRun = true)	public void setUp(){
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyData", groups = "nightly-build")
	public void editExistingCompanyWithExistingContacts_Test(String companySlug, String contactSlug1,
			String contactSlug2) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", companySlug);

		String basePath = "companies/{company}";

		Company company = new Company();
		company.setCompanyname(companyName);
		company.setWebsite(companyWebsite);
		company.setCity(companyCity);
		company.setIndustryid(industry_id);
		company.setAddress(address);

		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);
		companyJson.setExistingContacts(contactSlug1 + "," + contactSlug2);

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath,albatrossAuthToken,
				null, pathParamters, true, companyJson);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.containsString("Company Updated"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotEditCompany_Test() {

		Company company = new Company();
		company.setCompanyname(companyName);

		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("company", faker.getInvalidCompanySlug());

		String basePath = "companies/{company}";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath,
				albatrossAuthToken + "123", null, pathParamters, true, companyJson);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyData() {

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken)
				.jsonPath().getString("slug");
		String contactSlug1 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath()
				.getString("slug");
		String contactSlug2 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath()
				.getString("slug");

		Object data[][] = { { companySlug, contactSlug1, contactSlug2 } };

		return data;
	}

}