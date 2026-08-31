package io.recruitcrm.albatross.company;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.*;
import io.rcrm.api.pojo.albatross.CompanyIndustry;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetIndustries_Test extends TestBase {

	public GetIndustries_Test() {
		super();
	}

	String albatrossTknA;
	String albatrossTknB;
	String apiKeyA;
	String apiKeyB;
	JavaFakerCompany javaFakerCompany;
	JavaFakerPlacement placementFaker;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		javaFakerCompany = new JavaFakerCompany();
		placementFaker = new JavaFakerPlacement();
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getAllIndustriesWithValidToken_Test() {

		Response response = RestClient.doPost("JSON", albatrossURL, "industries", albatrossTknA, null, true, null);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("data.defaultIndustries[0].id", Matchers.notNullValue());
		response.then().body("data.defaultIndustries[0].label", Matchers.notNullValue());
		response.then().body("data.customIndustries", Matchers.empty());
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void getAllIndustriesWithCustomIndustry_Test(int industryId, String customIndustry) {

		Response response = RestClient.doPost("JSON", albatrossURL, "industries", albatrossTknA, null, true, null);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("data.defaultIndustries[0].id", Matchers.notNullValue());
		response.then().body("data.customIndustries[0].label", Matchers.containsString(customIndustry));
		response.then().body("data.customIndustries[0].id", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void getAllIndustriesWithCrossAccountToken_Test(int industryId, String customIndustry) {

		Response response = RestClient.doPost("JSON", albatrossURL, "industries", albatrossTknB, null, true, null);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.containsString("is-success"));

		response.then().body("data.defaultIndustries[0].id", Matchers.notNullValue());
		response.then().body("data.defaultIndustries[0].label", Matchers.notNullValue());
		response.then().body("data.customIndustries.size()", Matchers.equalTo(0));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void getAllIndustriesWithAdminToken_Test(int industryId, String customIndustry) {

		Response response = RestClient.doPost("JSON", albatrossURL, "industries", getRoleBasedToken("AccountA", "Admin"), null, true, null);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.containsString("is-success"));

		response.then().body("data.defaultIndustries[0].id", Matchers.notNullValue());
		response.then().body("data.defaultIndustries[0].label", Matchers.notNullValue());
		response.then().body("data.customIndustries", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void getAllIndustriesWithTeamMemberToken_Test(int industryId, String customIndustry) {

		Response response = RestClient.doPost("JSON", albatrossURL, "industries", getRoleBasedToken("AccountA", "Team Member"), null, true, null);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.containsString("is-success"));

		response.then().body("data.defaultIndustries[0].id", Matchers.notNullValue());
		response.then().body("data.defaultIndustries[0].label", Matchers.notNullValue());
		response.then().body("data.customIndustries", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomIndustryDataProvider", groups = "nightly-build")
	public void getAllIndustriesWithRestrictedToken_Test(int industryId, String customIndustry) {

		Response response = RestClient.doPost("JSON", albatrossURL, "industries", getRoleBasedToken("AccountA", "Restricted"), null, true, null);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.containsString("is-success"));

		response.then().body("data.defaultIndustries[0].id", Matchers.notNullValue());
		response.then().body("data.defaultIndustries[0].label", Matchers.notNullValue());
		response.then().body("data.customIndustries", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getAllIndustriesWithInvalidToken_Test() {

		Response response = RestClient.doPost("JSON", albatrossURL, "industries", placementFaker.getInvalidToken(), null, true, null);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] createCustomIndustryDataProvider() {

		CompanyIndustry industry = new CompanyIndustry();
		industry.setIndustryLabel(javaFakerCompany.getCustomIndustryLabel());

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-industry-type", albatrossTknA, null, true,
				industry);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.containsString("is-success"));

		JsonPath json = response.jsonPath();

		Object data[][] = { { json.get("data.id"), json.get("data.label") } };

		return data;
	}

}
