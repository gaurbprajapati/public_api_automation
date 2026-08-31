package io.recruitcrm.albatross.company;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class BulkUpdateCompanyTest extends TestBase {

	public BulkUpdateCompanyTest() {
		super();
	}

	JavaFakerCompany faker = new JavaFakerCompany();
	commanFunction function = new commanFunction();
	AllCrudFunctions albatrossFunction = new AllCrudFunctions();
	
	String albatrossAuthToken;
	String apiAuthToken;
	
	@BeforeClass(alwaysRun = true)	public void setUp(){
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyAndContactData", groups = "nightly-build")
	public void bulkUpdateContactFieldForCompanies_Test(String companySlug1, String companySlug2, String contactSlug1,
			String contactSlug2) {

		int companyId1 = albatrossFunction
				.getCompanyResponse(albatrossURL, albatrossAuthToken, companySlug1).jsonPath()
				.get("data.company.id");
		int companyId2 = albatrossFunction
				.getCompanyResponse(albatrossURL, albatrossAuthToken, companySlug2).jsonPath()
				.get("data.company.id");

		List<Integer> companyIds = Arrays.asList(companyId1, companyId2);

		UpdateFields updateFields = new UpdateFields();
		updateFields.setKey("contactid");
		updateFields.setValue(contactSlug2 + "," + contactSlug2);
		updateFields.setTableFlag("company");
		updateFields.setId(companyIds);

		updateFields.setAddInValues(true);

		String basePath = "global/update-fields";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken,
				null, true, updateFields);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.containsString("Update Field Successful"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyAndContactData", groups = "nightly-build")
	public void bulkUpdateCompanyFieldForContacts_Test(String companySlug1, String companySlug2, String contactSlug1,
			String contactSlug2) {

		int companyId1 = albatrossFunction
				.getCompanyResponse(albatrossURL, albatrossAuthToken, companySlug1).jsonPath()
				.get("data.company.id");
		int companyId2 = albatrossFunction
				.getCompanyResponse(albatrossURL, albatrossAuthToken, companySlug2).jsonPath()
				.get("data.company.id");

		int contactId1 = Integer.parseInt(
				albatrossFunction.getContactResponse(albatrossURL, albatrossAuthToken, contactSlug1)
						.jsonPath().get("data.contact.id"));

		int contactId2 = Integer.parseInt(
				albatrossFunction.getContactResponse(albatrossURL, albatrossAuthToken, contactSlug2)
						.jsonPath().get("data.contact.id"));

		List<Integer> contactIds = Arrays.asList(contactId1, contactId2);

		UpdateFields updateFields = new UpdateFields();
		updateFields.setKey("companyid");
		updateFields.setValue(companyId1 + "," + companyId2);
		updateFields.setTableFlag("contact");
		updateFields.setId(contactIds);

		updateFields.setAddInValues(true);

		String basePath = "global/update-fields";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken,
				null, true, updateFields);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.containsString("Field Updated Successfully"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void bulkUpdateCompaniesWithEmptyData_Test() {

		List<Integer> companyIds = Arrays.asList(faker.getInvalidCompanyId(), faker.getInvalidCompanyId());

		UpdateFields updateFields = new UpdateFields();
		updateFields.setKey(null);
		updateFields.setValue(faker.getInvalidCompanySlug() + "," + faker.getInvalidCompanySlug());
		updateFields.setTableFlag("company");
		updateFields.setId(companyIds);

		updateFields.setAddInValues(true);

		String basePath = "global/update-fields";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken,
				null, true, updateFields);

		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("message", Matchers.containsString("The key field is required"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotBulkUpdateCompanies_Test() {

		List<Integer> companyIds = Arrays.asList(faker.getInvalidCompanyId(), faker.getInvalidCompanyId());

		UpdateFields updateFields = new UpdateFields();
		updateFields.setKey(faker.getCity());
		updateFields.setValue(faker.getInvalidCompanySlug() + "," + faker.getInvalidCompanySlug());
		updateFields.setTableFlag("company");
		updateFields.setId(companyIds);

		updateFields.setAddInValues(true);

		String basePath = "global/update-fields";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath,
				albatrossAuthToken + "123", null, true, updateFields);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyAndContactData() {
		String companySlug1 = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken)
				.jsonPath().getString("slug");
		String companySlug2 = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken)
				.jsonPath().getString("slug");
		String contactSlug1 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath()
				.getString("slug");
		String contactSlug2 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath()
				.getString("slug");

		Object data[][] = { { companySlug1, companySlug2, contactSlug1, contactSlug2 } };

		return data;
	}

}