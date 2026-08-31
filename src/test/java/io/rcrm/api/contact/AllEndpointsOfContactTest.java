package io.rcrm.api.contact;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.rcrm.api.commanfunctions.commanFunction;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class AllEndpointsOfContactTest extends TestBase {

	public AllEndpointsOfContactTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();
	JavaFakerContact javaFakerContact = new JavaFakerContact();
	String ContactFirstName = javaFakerContact.getFirstName();
	String ContactLastName = javaFakerContact.getLastName();
	String ContactEmail= javaFakerContact.getEmailID();
	String contactNumber = javaFakerContact.getContactNumber();
	String avatar = javaFakerContact.getAvatarUrl();
	String city = javaFakerContact.getCity();
	String address = javaFakerContact.getAddress();
	String locality = javaFakerContact.getLocality();
	String designation = javaFakerContact.getDesignation();
	String url = javaFakerContact.getUrl();
	String reason = javaFakerContact.getStageUpdateReason();
	String stageId;
	String stageLabel;
	

	String slug = "";
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewContact_POST() {

		
		Contact contact = new Contact(ContactFirstName,ContactLastName,ContactEmail,contactNumber,"");
		JsonPath jsonContactStages = function.getAllContactStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		stageId = jsonContactStages.getString("[0].stage_id");
		stageLabel = jsonContactStages.getString("[0].label");
		contact.setAvatar(avatar);
		contact.setCity(city);
		contact.setAddress(address);
		contact.setLocality(locality);
		contact.setLinkedin(url);
		contact.setFacebook(url);
		contact.setTwitter(url);
		contact.setXing(url);
		contact.setDesignation(designation);
		contact.setStage_id(stageId);
		
		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("contact_stage_remarks[0].stage_id", Matchers.is(Integer.parseInt(stageId)));
		response.then().body("contact_stage_remarks[0].stage_name", Matchers.is(stageLabel));

		// Validate communication fields as null
		validateCommunicationFields(response, "");

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//contact//createContact.json"));

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		slug = jp.get("slug");
		// 2295174
	}
	
	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void verifyValidationOfCreateNewContact() {

		Contact contact = new Contact("","","","","");
		
		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

		Assert.assertEquals(response.getStatusCode(), 422);

	}
	
	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewContact_POST", groups = "nightly-build")
	public void editContactBySlug_POST() {


		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", slug);

		String basePath = "contacts/{contact}";

		// Here we can also use data provider.
		
		Contact contact = new Contact(ContactFirstName+"-Edited",ContactLastName,ContactEmail,contactNumber,"");
		JsonPath jsonContactStages = function.getAllContactStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		stageId = jsonContactStages.getString("[1].stage_id");
		stageLabel = jsonContactStages.getString("[1].label");
		contact.setAvatar(avatar);
		contact.setCity("Edited " + city);
		contact.setAddress("Edited " + address);
		contact.setLocality("Edited " + locality);
		contact.setLinkedin(url);
		contact.setFacebook(url);
		contact.setTwitter(url);
		contact.setXing(url);
		contact.setDesignation("Edited " + designation);
		contact.setStage_id(stageId);
		contact.setReason(reason);
		
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true, contact);

		Assert.assertEquals(response1.getStatusCode(), 200);
		response1.then().body("contact_stage_remarks[0].stage_id", Matchers.is(Integer.parseInt(stageId)));
		response1.then().body("contact_stage_remarks[0].stage_name", Matchers.is(stageLabel));
		response1.then().body("contact_stage_remarks[0].reason", Matchers.is(reason));

		// Validate communication fields as null
		validateCommunicationFields(response1, "");

		response1.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//contact//editContact.json"));

 }
	
	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = "editContactBySlug_POST", groups = "nightly-build")
	public void showAllContacts_GET() {


		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(),
				queryParameters,null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("data[0].contact_stage_remarks[0].stage_id", Matchers.is(Integer.parseInt(stageId)));
		response.then().body("data[0].contact_stage_remarks[0].stage_name", Matchers.is(stageLabel));
		response.then().body("data[0].contact_stage_remarks[0].reason", Matchers.is(reason));

		// Validate communication fields as null
		validateCommunicationFields(response, "data[0]");

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//contact//getAllContacts.json"));

	}
	
	@Owner("Smit Patel")
	@Test(dependsOnMethods = "editContactBySlug_POST", groups = "nightly-build")
	public void searchContactByName_GET() {


		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("first_name", ContactFirstName);
		

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(),
				queryParameters,null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("data[0].contact_stage_remarks[0].stage_id", Matchers.is(Integer.parseInt(stageId)));
		response.then().body("data[0].contact_stage_remarks[0].stage_name", Matchers.is(stageLabel));
		response.then().body("data[0].contact_stage_remarks[0].reason", Matchers.is(reason));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//contact//searchContactByFields.json"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "editContactBySlug_POST", groups = "nightly-build")
	public void searchContactBySlug_GET() {


		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", slug);

		String basePath = "contacts/{contact}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(),null,
				pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("contact_stage_remarks[0].stage_id", Matchers.is(Integer.parseInt(stageId)));
		response.then().body("contact_stage_remarks[0].stage_name", Matchers.is(stageLabel));
		response.then().body("contact_stage_remarks[0].reason", Matchers.is(reason));

		// Validate communication fields as null
		validateCommunicationFields(response, "");
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//contact//searchContactBySlug.json"));
	
 }

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = "editContactBySlug_POST", groups = "nightly-build")
	public void getContactStageHistory() {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("contact", slug);
		String basePath = "contact/get-stage-history/{contact}";
		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParams, true);
		assert response != null : "Stage History Response was null";
		response.then().statusCode(200);
		response.then().body("contact_stage_remarks[0].stage_id", Matchers.is(Integer.parseInt(stageId)));
		response.then().body("contact_stage_remarks[0].stage_name", Matchers.is(stageLabel));
		response.then().body("contact_stage_remarks[0].reason", Matchers.is(reason));
		matchesJsonSchemaInClasspath("publicApi//contact//getContactStageHistory.json");
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
				"last_calllog_added_on",
				"last_calllog_added_by",
				"last_email_sent_on",
				"last_email_sent_by",
				"last_sms_sent_on",
				"last_sms_sent_by",
				"last_meeting_created_on",
				"last_meeting_created_by",
				"last_linkedin_message_sent_on",
				"last_linkedin_message_sent_by",
				"last_communication"
		);
	}

	/*
	 * @Test(dependsOnMethods = "createNewCandidate") public void
	 * searchCandidates_GET() {
	 * 
	 * 
	 * Map<String, String> queryParameters = new HashMap<String, String>();
	 * queryParameters.put("first_name", "Sandeep"); queryParameters.put("email",
	 * "spi504@yopmail.com");
	 * 
	 * Response response = RestClient.doGet("JSON", baseURL, "candidates/search",
	 * authTokenMap, queryParameters, true);
	 * 
	 * Assert.assertEquals(response.getStatusCode(), 200);
	 
	 * }
	 */
}
