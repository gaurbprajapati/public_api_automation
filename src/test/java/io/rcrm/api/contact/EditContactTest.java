package io.rcrm.api.contact;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.json.*;
import org.testng.annotations.*;
import java.util.*;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.*;
import io.rcrm.api.commanfunctions.*;
import io.rcrm.api.javafaker.*;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditContactTest extends TestBase {

	public EditContactTest() {
		super();
	}

	commanFunction function = new commanFunction();
	JavaFakerContact faker = new JavaFakerContact();
	errorResponseBody errorResponseBody = new errorResponseBody();
	ContactCustomField contact = new ContactCustomField();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	String apiAuthToken;
	String albatrossAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "contactData", groups = "nightly-build")
	public void editContactByValidSlug_200(String slug) {
		String firstName = faker.getFirstName();
		String lastName = faker.getLastName();
		String emailId = faker.getEmailID();
		String contactNumber = faker.getContactNumber();

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", slug);

		String basePath = "contacts/{contact}";
		Contact contact = new Contact(firstName, lastName, emailId, contactNumber, "");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, true, contact);
		response.then().statusCode(200);

		response.then().body("first_name", Matchers.is(firstName));
		response.then().body("last_name", Matchers.is(lastName));
		response.then().body("email", Matchers.is(emailId));
		response.then().body("contact_number", Matchers.is(contactNumber));
		
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//contact//editContact.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void editContactByInvalidSlug_404() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", faker.getInvalidContactSlug());

		String basePath = "contacts/{contact}";
		Contact contact = new Contact(faker.getFirstName(), faker.getLastName(), "", "", "");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, true, contact);
		response.then().statusCode(404);

		errorResponseBody.verify422ResponseBody(response, 404, "Contact doesn't exist", true);
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "contactData", groups = "nightly-build")
	public void editContactWithEmptyRequest_422(String slug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", slug);

		String basePath = "contacts/{contact}";
		Contact contact = new Contact();

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, true, contact);
		response.then().statusCode(422);

		response.then().body("first_name[0]", Matchers.is("The first name field is required."));
		response.then().body("last_name[0]", Matchers.is("The last name field is required."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotEditContact_401() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", faker.getInvalidContactSlug());

		String basePath = "contacts/{contact}";
		Contact contact = new Contact();

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken + "123", null, pathParamters, true, contact);
		response.then().statusCode(401);

		response.then().body("error", Matchers.is("Unauthorized"));
	}

	// Bug Automation - TITAN-22572
	@Owner("Smit Patel")
	@Test(dataProvider = "contactData", groups = "nightly-build")
	public void verifyUpdatingSocialURLCustomFieldWithNull_200(String slug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", slug);
		JSONObject customField1 = new JSONObject();
		customField1.put("field_id", 1);
		customField1.put("value", JSONObject.NULL);
		JSONArray customFieldsArray = new JSONArray();
		customFieldsArray.put(customField1);
		JSONObject requestBody = new JSONObject();
		requestBody.put("custom_fields", customFieldsArray);

		Response response = RestClient.doPost1("JSON", baseURL, "contacts/{contact}", apiAuthToken, null, pathParamters, true, requestBody.toString());
		response.then().statusCode(200);		
		response.then().body("custom_fields[0].field_id", Matchers.is(1));
		response.then().body("custom_fields[0].value", Matchers.nullValue());
	}

	@DataProvider
	public Object[][] contactData() {
		Response customFieldResponse = allCrudFunctions.createCustomFields(albatrossURL, albatrossAuthToken, "social_profile", 2);
		customFieldResponse.then().statusCode(200);
		
		String socialProfileURL = faker.getUrl();
		ContactCustomField contact = new ContactCustomField();
		contact.setFirst_name(faker.getFirstName());
		contact.setLast_name(faker.getLastName());
		contact.setEmail(faker.getEmailID());
		contact.setContact_number(faker.getContactNumber());

		List<ContactCustomField.CustomField> customFields = new ArrayList<>();
		ContactCustomField.CustomField customField = new ContactCustomField.CustomField();
		customField.setField_id(1);
		customField.setValue(socialProfileURL);
		customFields.add(customField);
		contact.setCustom_fields(customFields);

		Response response = RestClient.doPost("JSON", baseURL, "contacts", apiAuthToken, null, true, contact);
		response.then().statusCode(200);
		String slug = response.jsonPath().getString("slug");
		return new Object[][] { { slug } };
	}
}