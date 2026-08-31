package io.rcrm.api.contact;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import com.github.javafaker.Faker;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.errorResponseBody;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllContactsTest extends TestBase{

	public GetAllContactsTest() {
		super();
	}
	
	commanFunction function = new commanFunction();
	AllCrudFunctions privateFunction = new AllCrudFunctions();
	JavaFakerContact contactFaker = new JavaFakerContact();
	
	Faker faker = new Faker();
	String ContactFirstName = faker.name().firstName();
	String ContactLastName = faker.name().lastName();
	String ContactEmail= "rcrmtest0@gmail.com";
	String contactNumber = faker.phoneNumber().phoneNumber();
	
	errorResponseBody errorResponseBody = new errorResponseBody();

	String slug = "";
	String apiAuthToken, albatrossTkn;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}
	
	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void showAllContacts_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(),
				queryParameters,null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//contact//getAllContacts.json"));

	}
	
	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotAccessShowAllContacts_GET() {


		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey() +"12345",
				queryParameters,null, true);

		Assert.assertEquals(response.getStatusCode(), 401);
		errorResponseBody.verify401ResponseBody(response, 401, "Unauthorized", true);
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createContactWithCustomFields", groups = "nightly-build")
	public void verifyCustomFieldValueInShowAllContacts_Test(String value, String value2) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "10");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "contacts", apiAuthToken, queryParameters, null, true);
		
		response.then().statusCode(200);
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		response.then().body("data.size()", Matchers.equalTo(2));
		response.then().body("data[0].custom_fields[0].value", Matchers.containsString(value2));
		response.then().body("data[1].custom_fields[0].value", Matchers.containsString(value));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//contact//getAllContacts.json"));
	}
	
	@DataProvider
	public Object[][] createContactWithCustomFields() {
		int entityId1, entityId2, columnId;
		String contactSlug = function.getEntityResponse(baseURL, apiAuthToken, "contact");
		entityId1 =  Integer.parseInt(privateFunction.getContactResponse(albatrossURL, albatrossTkn, contactSlug).jsonPath().get("data.contact.id"));
		contactSlug = function.getEntityResponse(baseURL, apiAuthToken, "contact");
		entityId2 =  Integer.parseInt(privateFunction.getContactResponse(albatrossURL, albatrossTkn, contactSlug).jsonPath().get("data.contact.id"));
		Response response = function.createCustomFieldsResponse(albatrossURL, albatrossTkn, "contact", "contactField", "phonenumber", "");
		columnId = response.jsonPath().get("data.custumField.columnid");
		String value1 = contactFaker.getContactNumber();
		String value2 = contactFaker.getContactNumber();
		privateFunction.updateCustomField("contact", albatrossURL, entityId1, albatrossTkn, "custcolumn" + columnId, value1);
		privateFunction.updateCustomField("contact", albatrossURL, entityId2, albatrossTkn, "custcolumn" + columnId, value2);
		return new Object[][] { { value1, value2 } };
	}
}
