package io.rcrm.api.contact;

import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1")
public class SearchByContactFieldsTest extends TestBase {

	public SearchByContactFieldsTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	JavaFakerContact contactFaker = new JavaFakerContact();
	JavaFakerCompany companyFaker = new JavaFakerCompany();
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();

	String ContactFirstName = contactFaker.getFirstName();
	String ContactLastName = contactFaker.getLastName();
	String ContactEmail = "rcrmtest0@gmail.com";
	String contactNumber = contactFaker.getContactNumber();

	String companyName = companyFaker.getCompanyName();
	String companyWebsite = companyFaker.getUrl();
	String companyCity = companyFaker.getCity();
	int industry_id = companyFaker.getIndustry_id();

	// Social Links
	String fbLink = fakerCandidate.getUrl();
	String twitterLink = fakerCandidate.getUrl();
	String githubLink = fakerCandidate.getUrl();
	String linkedinLink = fakerCandidate.getUrl();
	String xingLink = fakerCandidate.getUrl();

	String city = fakerCandidate.getCity();
	String locality = fakerCandidate.getLocality();
	String Address = fakerCandidate.getCandidateAddress();
	String AvatarURL = fakerCandidate.getCandidateAvatarUrl();
	String title = fakerCandidate.getPosition();

	String slug = "";
	String companySlug = "";
	int owner;
	String lastCommunication;
	String callCreatedOn;
	String meetingCreatedOn;
	String contactFullName;
	String todayDate = java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString();

	commanFunction commonfunction = new commanFunction();
	
	String apiAuthToken;

	@BeforeClass(alwaysRun = true)		public void setUp() {
			apiAuthToken = ThreadManager.getAccountApiKey();
		}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createNewCompanyDataProvider", groups = "nightly-build")
	public void createNewContactWithAllFields(String company_slug) {

		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, "");
		contact.setAddress(Address);
		contact.setCity(city);
		contact.setAvatar(AvatarURL);
		contact.setDesignation(title);
		contact.setFacebook(fbLink);
		contact.setTwitter(twitterLink);
		contact.setLinkedin(githubLink);
		contact.setXing(xingLink);
		contact.setLocality(locality);
		contact.setCompany_slug(company_slug);

		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		slug = jp.get("slug");
		owner = jp.get("owner");
		contactFullName = jp.get("first_name") + " " + jp.get("last_name");
		// 2295174

		// Verify response
		response.then().statusCode(200);
		response.then().body("first_name", Matchers.is(ContactFirstName));
		response.then().body("last_name", Matchers.is(ContactLastName));
		response.then().body("email", Matchers.is(ContactEmail));
		response.then().body("contact_number", Matchers.is(contactNumber));

		response.then().body("facebook", Matchers.is(fbLink));
		response.then().body("twitter", Matchers.is(twitterLink));
		response.then().body("linkedin", Matchers.is(githubLink));
		response.then().body("xing", Matchers.is(xingLink));


		response.then().body("last_calllog_added_on", Matchers.nullValue());
		response.then().body("last_calllog_added_by", Matchers.nullValue());
		response.then().body("last_email_sent_on", Matchers.nullValue());
		response.then().body("last_email_sent_by", Matchers.nullValue());
		response.then().body("last_sms_sent_on", Matchers.nullValue());
		response.then().body("last_sms_sent_by", Matchers.nullValue());
		response.then().body("last_meeting_created_on", Matchers.nullValue());
		response.then().body("last_meeting_created_by", Matchers.nullValue());
		response.then().body("last_message_sent_on", Matchers.nullValue());
		response.then().body("last_message_sent_by", Matchers.nullValue());
		response.then().body("last_communication", Matchers.nullValue());

		//adding a call log to contact
		callCreatedOn = commonfunction
				.createNewCallLogWithEntitySlug(baseURL, ThreadManager.getAccountApiKey(), "contact", slug)
				.jsonPath().get("created_on");
		lastCommunication = "Call on " + java.time.OffsetDateTime.parse(callCreatedOn).toLocalDateTime().toString().replace('T', ' ');
	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = "createNewContactWithAllFields", dataProvider = "getExactSearchData", groups = "nightly-build")
	public void searchContactByAllFields_GET(int exactSearch) throws ParseException {

		Map<String, String> queryParameters = new HashMap<String, String>();

		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		Date expectedDate2 = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		// cannot include slug here as if slug is added other parameters will be ignored.
		queryParameters.put("first_name", ContactFirstName);
		queryParameters.put("last_name", ContactLastName);
		queryParameters.put("email", ContactEmail);
		queryParameters.put("linkedin", githubLink);
		queryParameters.put("xing", xingLink);
		queryParameters.put("contact_number", contactNumber);
		queryParameters.put("company_slug", companySlug);
		queryParameters.put("exact_search", String.valueOf(exactSearch));
		queryParameters.put("created_from", yesterdayDateString);
		queryParameters.put("created_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("updated_to", tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body("data.first_name[0]", Matchers.is(ContactFirstName));
		response.then().body("data.last_name[0]", Matchers.is(ContactLastName));
		response.then().body("data.email[0]", Matchers.is(ContactEmail));
		response.then().body("data.linkedin[0]", Matchers.is(githubLink));
		response.then().body("data.xing[0]", Matchers.is(xingLink));
		response.then().body("data.contact_number[0]", Matchers.is(contactNumber));
		response.then().body("data.company_slug[0]", Matchers.is(companySlug));

		validateCommunicationFields(response, "data[0]", callCreatedOn, owner, null, null, lastCommunication);

		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data.created_on[0]");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
		Assert.assertTrue(actualDate.before(expectedDate2), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);

		String jpDate2 = jp.get("data.updated_on[0]");
		Date actualDate2 = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate2);
		Assert.assertTrue(actualDate2.after(expectedDate), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate2);
		Assert.assertTrue(actualDate2.before(expectedDate2), "Actual date is not before the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByFirstName_GET() {
		//sending email to contact
		commonfunction.sendEmailToCandCont(1, "Contact", nymaURLv3, ContactEmail, contactFullName, slug, ThreadManager.getOwnerAlbatrossToken());
		lastCommunication = "Email on " + todayDate;

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("first_name", ContactFirstName);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body("data.first_name[0]", Matchers.is(ContactFirstName));

		validateCommunicationFields(response, "data[0]", callCreatedOn, owner, todayDate, null, lastCommunication);
	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByLastName_GET() {
		meetingCreatedOn = commonfunction.createNewMeetingWithEntitySlug(baseURL, ThreadManager.getAccountApiKey(),"contact",slug).jsonPath().get("created_on");
		lastCommunication = "Meeting on "+java.time.OffsetDateTime.parse(meetingCreatedOn).toLocalDateTime().toString().replace('T', ' ');

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("last_name", ContactLastName);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body("data.last_name[0]", Matchers.is(ContactLastName));
		validateCommunicationFields(response, "data[0]", callCreatedOn, owner, todayDate, meetingCreatedOn, lastCommunication);
	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByEmail_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("email", ContactEmail);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body("data.email[0]", Matchers.is(ContactEmail));

	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByLinkedin_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();

		queryParameters.put("linkedin", githubLink);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body("data.linkedin[0]", Matchers.is(githubLink));

	}
	
	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByXing_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();

		queryParameters.put("xing", xingLink);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body("data.xing[0]", Matchers.is(xingLink));

	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByContactNumber_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();

		queryParameters.put("contact_number", contactNumber);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body("data.contact_number[0]", Matchers.is(contactNumber));

		//Bug Automation- RCRM- 7803
		String randomString= RandomStringUtils.randomAlphabetic(10);
		queryParameters.replace("contact_number",randomString);
		Response response1 = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);
		Assert.assertEquals(response1.getStatusCode(), 422);
		Assert.assertEquals(response1.jsonPath().get("contact_number[0]"), "The value should be a number","Search is not working as expected, giving result with alphabetic contact number");


	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByCompanySlug_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();

		queryParameters.put("company_slug", companySlug);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body("data.company_slug[0]", Matchers.is(companySlug));
	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByInvalidFieldValues_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("first_name", "1234-" + ContactFirstName);
		queryParameters.put("last_name", "1234-" + ContactLastName);
		queryParameters.put("email", "1234-" + ContactEmail);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body(Matchers.is("[]"));

	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByContactSlug_GET() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("contact_slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body("data.slug[0]", Matchers.is(slug));
		response.then().body("data.first_name[0]", Matchers.is(ContactFirstName));
		response.then().body("data.last_name[0]", Matchers.is(ContactLastName));
		response.then().body("data.email[0]", Matchers.is(ContactEmail));
		response.then().body("data.linkedin[0]", Matchers.is(githubLink));
		response.then().body("data.xing[0]", Matchers.is(xingLink));
		response.then().body("data.contact_number[0]", Matchers.is(contactNumber));
	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByCreatedFrom() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("created_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		// Verify Response
		response.then().statusCode(200);
		response.then().body("data[0].first_name", Matchers.is(ContactFirstName));
		response.then().body("data[0].last_name", Matchers.is(ContactLastName));
		response.then().body("data[0].email", Matchers.is(ContactEmail));
		response.then().body("data[0].contact_number", Matchers.is(contactNumber));
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Smit Patel")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByCreatedTo() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("created_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);


		// Verify Response
		response.then().statusCode(200);
		response.then().body("data[0].first_name", Matchers.is(ContactFirstName));
		response.then().body("data[0].last_name", Matchers.is(ContactLastName));
		response.then().body("data[0].email", Matchers.is(ContactEmail));
		response.then().body("data[0].contact_number", Matchers.is(contactNumber));
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Akshaya Uppala")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByUpdatedFrom() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("updated_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		// Verify Response
		response.then().statusCode(200);
		response.then().body("data[0].first_name", Matchers.is(ContactFirstName));
		response.then().body("data[0].last_name", Matchers.is(ContactLastName));
		response.then().body("data[0].email", Matchers.is(ContactEmail));
		response.then().body("data[0].contact_number", Matchers.is(contactNumber));
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods = "createNewContactWithAllFields", groups = "nightly-build")
	public void searchContactByUpdatedTo() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("updated_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "contacts/search", ThreadManager.getAccountApiKey(), queryParameters, null,
				true);

		// Verify Response
		response.then().statusCode(200);
		response.then().body("data[0].first_name", Matchers.is(ContactFirstName));
		response.then().body("data[0].last_name", Matchers.is(ContactLastName));
		response.then().body("data[0].email", Matchers.is(ContactEmail));
		response.then().body("data[0].contact_number", Matchers.is(contactNumber));
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not after the expected date, expected : " + expectedDate
				+ " but found : " + actualDate);
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchContactByOwnerParameters_Test() {
		// create contact using public api
		JsonPath jsonContact = commonfunction.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath();
		String contactSlug = jsonContact.getString("slug");
		String contactName = jsonContact.getString("first_name");

		// get owner data from users end point
		Response userResponse = commonfunction.getUsers(baseURL, apiAuthToken);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		int id = user.get("[0].id");
		String ownerId = String.valueOf(id);
		String ownerName = user.get("[0].first_name");
		String ownerEmail = user.get("[0].email");

		// search contact by owner parameters
		String[] ownerParams = { "owner_id", "owner_name", "owner_email" };
		String[] ownerValues = { ownerId, ownerName, ownerEmail };

		for (int i = 0; i < ownerParams.length; i++) {
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put(ownerParams[i], ownerValues[i]);

			Response response = RestClient.doGet("JSON", baseURL, "contacts/search", apiAuthToken, queryParameters,
					null, true);

			Assert.assertEquals(response.getStatusCode(), 200);
			JsonPath jsonPath = response.jsonPath();

			Assert.assertEquals(jsonPath.getInt("data[0].owner"), Integer.parseInt(ownerId),
					"Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].first_name"), contactName, "Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].slug"), contactSlug, "Failed at " + ownerParams[i]);
		}
	}


	private void validateCommunicationFields(Response response, String dataPath,
											 String expectedCallCreatedOn, int expectedOwner,
											 String expectedEmailSentOn, String expectedMeetingCreatedOn,
											 String expectedLastCommunication) {

		String pathPrefix = dataPath.isEmpty() ? "$" : dataPath;
		List<String> allFields = getAllCommunicationFields();
		for (String field : allFields) {
			response.then().body(pathPrefix, Matchers.hasKey(field));
		}
		response.then()
				.body(pathPrefix + ".last_calllog_added_on", expectedCallCreatedOn == null ? Matchers.nullValue() : Matchers.is(expectedCallCreatedOn))
				.body(pathPrefix + ".last_calllog_added_by", expectedCallCreatedOn == null ? Matchers.nullValue() : Matchers.is(expectedOwner))
				.body(pathPrefix + ".last_email_sent_on", expectedEmailSentOn == null ? Matchers.nullValue() : Matchers.containsString(expectedEmailSentOn))
				.body(pathPrefix + ".last_email_sent_by", expectedEmailSentOn == null ? Matchers.nullValue() : Matchers.is(expectedOwner))
				.body(pathPrefix + ".last_sms_sent_on", Matchers.nullValue())
				.body(pathPrefix + ".last_sms_sent_by", Matchers.nullValue())
				.body(pathPrefix + ".last_meeting_created_on", expectedMeetingCreatedOn == null ? Matchers.nullValue() : Matchers.is(expectedMeetingCreatedOn))
				.body(pathPrefix + ".last_meeting_created_by", expectedMeetingCreatedOn == null ? Matchers.nullValue() : Matchers.is(expectedOwner))
				.body(pathPrefix + ".last_linkedin_message_sent_on", Matchers.nullValue())
				.body(pathPrefix + ".last_linkedin_message_sent_by", Matchers.nullValue())
				.body(pathPrefix + ".last_communication", Matchers.containsString(expectedLastCommunication));
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

	@DataProvider
	public Object[][] createNewCompanyDataProvider() {

		JsonPath json = commonfunction.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String company_slug = json.get("slug");
		this.companySlug = company_slug;

		Object data[][] = { { company_slug } };

		return data;
	}

	@DataProvider
	public Object[][] getExactSearchData() {
		Object data[][] = { { 0 }, { 1 } };
		return data;
	}
}
