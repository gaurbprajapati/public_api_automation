package io.rcrm.api.company;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class UnauthorizedUserCompanyEndpointsTest extends TestBase{

	public UnauthorizedUserCompanyEndpointsTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	JavaFakerCompany faker = new JavaFakerCompany();

	String companyName = faker.getCompanyName();
	String companyWebsite = faker.getUrl();
	String contactNumber = "13456789087654";
	String companyCity = faker.getCity();
	String address = faker.getAddress();
	int industry_id = faker.getIndustry_id();
	String logo = faker.getLogoURL();

	String slug = "";
	
	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void unauthrizedUserCanntCreateNewCompany() {
		
		Company company = new Company();
		company.setCompany_name(companyName);

		Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey() +"invalid", null, true, company);

		// Get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response Code and body
		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));

	}

}
