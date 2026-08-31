package io.recruitcrm.albatross.chromeExtension;

import org.testng.Assert;
import org.testng.annotations.*;
import io.restassured.response.Response;
import com.qa.api.util.reaper.ThreadManager;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.*;
import io.rcrm.api.pojo.albatross.DuplicateMergeSetting;
import io.rcrm.api.pojo.chromeExtension.*;
import io.rcrm.api.pojo.chromeExtension.Companies.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.chromeExtension.Companies.Contact;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ContactTest_ExtensionTest extends TestBase {

	JavaFakerCompany javaFakerCompany;
	JavaFakerContact javaFakerContact;
	String companyName, companyWebsite, aboutCompany, companyAddress;
	String contactFirstName, contactLastName, contactDesignation, contactEmailId, contactNumber, contactTwitter,
			contactLinkedIn, contactFacebook, contactLocality;
	commanFunction function;
	String albatrossauthToken;
	String apiAuthToken;
	int ownerAccountID;
	private final Object lock = new Object();
	@BeforeClass(alwaysRun = true)	public void Setup() {
		javaFakerCompany = new JavaFakerCompany();
		javaFakerContact = new JavaFakerContact();
		companyName = javaFakerCompany.getCompanyName();
		companyWebsite = javaFakerCompany.getCompanyWebsite();
		aboutCompany = javaFakerCompany.getCompanyAbout();
		companyAddress = javaFakerCompany.getAddress();
		contactFirstName = javaFakerContact.getFirstName();
		contactLastName = javaFakerContact.getLastName();
		contactDesignation = javaFakerContact.getDesignation();
		contactEmailId = javaFakerContact.getEmailID();
		contactNumber = javaFakerContact.getContactNumber();
		contactTwitter = javaFakerContact.getContactTwitterURL();
		contactLinkedIn = javaFakerContact.getContactLinkedinURL();
		contactFacebook = javaFakerContact.getContactFacebookURL();
		contactLocality = javaFakerContact.getLocality();
		function = new commanFunction();
		albatrossauthToken = ThreadManager.getOwnerAlbatrossToken();
		ownerAccountID  = ThreadManager.getAccount().getAccountId();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getCompanyTestData", groups = "nightly-build")
	public void createNewContact_Extension(int companyId, String companyName) {
		RestAssured.baseURI = albatrossURL;
		Response response = RestAssured.given().header("cookie", "_extToken=" + albatrossauthToken)
				.multiPart("overrideData", false).multiPart("profilelinkedin", contactLinkedIn)
				.multiPart("profiletwitter", contactTwitter).multiPart("profilefacebook", contactFacebook)
				.multiPart("firstname", contactFirstName).multiPart("lastname", contactLastName)
				.multiPart("designation", contactDesignation).multiPart("email", contactEmailId)
				.multiPart("contactnumber", contactNumber).multiPart("locality", contactLocality)
				.multiPart("address", companyAddress).multiPart("companyid", companyId)
				.post("extensions/chrome/contact");

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.get("message"), "Add Contact From Extension Successful");

		String[] contactRequiredData = { "firstname", "lastname", "name", "designation", "email", "companyname",
				"contactnumber", "profilelinkedin", "profiletwitter", "locality", "address" };

		String[] contactExpectedData = { contactFirstName, contactLastName,
				contactFirstName + " " + contactLastName, contactDesignation, contactEmailId, companyName,
				contactNumber, contactLinkedIn, contactTwitter, contactLocality, companyAddress };

		assertContactDetails(jsonPath, contactRequiredData, contactExpectedData);
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getContactDuplicateTestData", groups = "nightly-build")
	public void createContactAndVerifyDuplicateContactMergeByEmailId_Extension(Map<String, String> testData,
			Boolean overrideData) {
		switch (overrideData.toString()) {
			case "false":
				RestAssured.baseURI = albatrossURL;
				Response response = RestAssured.given().header("cookie", "_extToken=" + albatrossauthToken)
						.multiPart("overrideData", overrideData).multiPart("profilelinkedin", contactLinkedIn)
						.multiPart("profiletwitter", contactTwitter).multiPart("profilefacebook", contactFacebook)
						.multiPart("firstname", contactFirstName).multiPart("lastname", contactLastName)
						.multiPart("designation", contactDesignation)
						.multiPart("email", testData.get("contactEmailId"))
						.multiPart("contactnumber", contactNumber).multiPart("address", companyAddress)
						.multiPart("locality", contactLocality).post("extensions/chrome/contact");

				JsonPath jsonPath = response.jsonPath();
				Assert.assertEquals(response.getStatusCode(), 200);
				Assert.assertEquals(jsonPath.get("message_type"), "is-success");
				Assert.assertEquals(jsonPath.get("message"), "Duplicate Contact Updated Successfully");
				Assert.assertEquals(jsonPath.get("data.contact.slug"), testData.get("contactSlug"));
				String[] contactRequiredData = { "firstname", "lastname", "name", "designation", "email",
						"companyname",
						"contactnumber", "profilelinkedin", "profiletwitter", "locality", "address" };

				String[] contactExpectedOverrideFalseData = { testData.get("contactFirstName"),
						testData.get("contactLastName"),
						testData.get("contactFirstName") + " " + testData.get("contactLastName"),
						testData.get("contactDesignation"), testData.get("contactEmailId"),
						testData.get("companyName"),
						testData.get("contactNumber"), contactLinkedIn, testData.get("contactTwitter"),
						contactLocality,
						testData.get("contactAddress") };
				assertContactDetails(jsonPath, contactRequiredData, contactExpectedOverrideFalseData);
				break;
		}
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getContactDuplicateTestData", groups = "nightly-build")
	public void createContactAndVerifyDuplicateContactMergeAndOverrideDataByEmailId_Extension(
			Map<String, String> testData,
			Boolean overrideData) {
		switch (overrideData.toString()) {
			case "true":
				RestAssured.baseURI = albatrossURL;
				Response response = RestAssured.given().header("cookie", "_extToken=" + albatrossauthToken)
						.multiPart("overrideData", overrideData).multiPart("profilelinkedin", contactLinkedIn)
						.multiPart("profiletwitter", contactTwitter).multiPart("profilefacebook", contactFacebook)
						.multiPart("firstname", contactFirstName).multiPart("lastname", contactLastName)
						.multiPart("designation", contactDesignation)
						.multiPart("email", testData.get("contactEmailId"))
						.multiPart("contactnumber", contactNumber).multiPart("address", companyAddress)
						.multiPart("locality", contactLocality).post("extensions/chrome/contact");

				JsonPath jsonPath = response.jsonPath();
				Assert.assertEquals(response.getStatusCode(), 200);
				Assert.assertEquals(jsonPath.get("message_type"), "is-success");
				Assert.assertEquals(jsonPath.get("message"), "Duplicate Contact Updated Successfully");
				Assert.assertEquals(jsonPath.get("data.contact.slug"), testData.get("contactSlug"));
				String[] contactRequiredData = { "firstname", "lastname", "name", "designation", "email",
						"companyname",
						"contactnumber", "profilelinkedin", "profiletwitter", "locality", "address" };

				String[] contactExpectedOverrideTrueData = { contactFirstName, contactLastName,
						contactFirstName + " " + contactLastName, contactDesignation,
						testData.get("contactEmailId"),
						testData.get("companyName"), contactNumber, contactLinkedIn, contactTwitter,
						contactLocality,
						companyAddress };
				assertContactDetails(jsonPath, contactRequiredData, contactExpectedOverrideTrueData);
				break;

		}
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createNewContactWithEmptyRequestBody_Extension() {
		RestAssured.baseURI = albatrossURL;
		Response response = RestAssured.given().header("cookie", "_extToken=" + albatrossauthToken)
				.multiPart("overrideData", false).multiPart("profilelinkedin", contactLinkedIn)
				.multiPart("profiletwitter", contactTwitter).multiPart("profilefacebook", contactFacebook)
				.multiPart("designation", contactDesignation).multiPart("email", contactEmailId)
				.multiPart("contactnumber", contactNumber).multiPart("locality", contactLocality)
				.multiPart("address", companyAddress).post("extensions/chrome/contact");

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"),
				"Failed To Add Contact From Extension : Either First Name or Last Name is mandatory");
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotCreateNewContact_Extension() {
		RestAssured.baseURI = albatrossURL;
		Response response = RestAssured.given()
				.header("cookie", "_extToken=" + albatrossauthToken + "abcd")
				.multiPart("overrideData", false).multiPart("profilelinkedin", contactLinkedIn)
				.multiPart("profiletwitter", contactTwitter).multiPart("profilefacebook", contactFacebook)
				.multiPart("firstname", contactFirstName).multiPart("lastname", contactLastName)
				.multiPart("designation", contactDesignation).multiPart("email", contactEmailId)
				.multiPart("contactnumber", contactNumber).multiPart("locality", contactLocality)
				.post("extensions/chrome/contact");

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createContactHotlist_Extension() {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("contacts");
		hotlist.setName(contactFirstName + "hotlist");
		List<Integer> selectedContacts = new ArrayList<>();
		hotlist.setSelectedrows(selectedContacts);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossauthToken, null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		Assert.assertEquals(jsonPath.get("message"), "Hotlist created and Contacts added successfully");
		Assert.assertEquals(jsonPath.get("data.hotlist[0].name"), contactFirstName + "hotlist");
		Assert.assertEquals(jsonPath.getInt("data.hotlist[0].shared"), 1);
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "hotlistWithoutContactTestData", groups = "nightly-build")
	public void addContactInExistingHotlist_Extension(String hotlistName, int contactId) {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("contacts");
		hotlist.setName(hotlistName);
		List<Integer> selectedContacts = new ArrayList<>();
		selectedContacts.add(contactId);
		hotlist.setSelectedrows(selectedContacts);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossauthToken, null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		Assert.assertEquals(jsonPath.get("message"), "Add To Hotlist Successful");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createContactHotlistWithEmptyRequestBody_Extension() {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("contacts");
		List<Integer> selectedContacts = new ArrayList<>();
		hotlist.setSelectedrows(selectedContacts);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossauthToken, null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Failed To Create Hotlist : The name field is required.");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotCreateContactHotlist_Extension() {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("contacts");
		List<Integer> selectedContacts = new ArrayList<>();
		hotlist.setSelectedrows(selectedContacts);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossauthToken + "abcd", null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");

	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getContactHotlist_Extension() {
		JsonPath jsonContactHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "contact").jsonPath();
		String hotlistName = jsonContactHotlist.get("name");
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("contacts");
		List<Integer> selectedContacts = new ArrayList<>();
		hotlist.setSelectedrows(selectedContacts);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists/get",
				albatrossauthToken, null, true, hotlist);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		int allHotlistSize = jsonPath.getList("data.records").size();
		Assert.assertEquals(allHotlistSize, 1);
		Assert.assertEquals(jsonPath.get("data[0].name"), hotlistName);
		Assert.assertEquals(jsonPath.get("data[0].entityname"), "contacts");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotGetContactHotlist_Extension() {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("contacts");
		List<Integer> selectedContacts = new ArrayList<>();
		hotlist.setSelectedrows(selectedContacts);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists/get",
				albatrossauthToken + "abcd", null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");

	}
	
	@Owner("Rahul Shibu")
	@Test(dataProvider = "hotlistWithContactTestData", groups = "nightly-build")
	public void getHotlistRelatedToContactId_Extension(String hotlistName,int contactId) {
		EntityRelatedHotlist entityRelatedHotlist = new EntityRelatedHotlist(contactId,"contacts");
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/related-hotlists",
				albatrossauthToken, null, true, entityRelatedHotlist);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		int allHotlistSize = jsonPath.getList("data.hotlists").size();
		Assert.assertEquals(allHotlistSize, 1);
		Assert.assertEquals(jsonPath.get("data.hotlists[0].name"), hotlistName);
		Assert.assertEquals(jsonPath.get("data.hotlists[0].entityname"), "contacts");
	}
	
	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getHotlistRelatedToContactIdWithEmptyRequestBody_Extension() {
		EntityRelatedHotlist entityRelatedHotlist = new EntityRelatedHotlist();
		entityRelatedHotlist.setPagename("contacts");
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/related-hotlists",
				albatrossauthToken, null, true, entityRelatedHotlist);
		JsonPath jsonPath = response.jsonPath();
		
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		int allHotlistSize = jsonPath.getList("data.hotlists").size();
		Assert.assertEquals(allHotlistSize, 0);
	}
	
	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotGetHotlistRelatedToContactId_Extension() {
		EntityRelatedHotlist entityRelatedHotlist = new EntityRelatedHotlist();
		entityRelatedHotlist.setPagename("contacts");
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/related-hotlists",
				albatrossauthToken+"abcd", null, true, entityRelatedHotlist);
		
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
	}


	@DataProvider(parallel = true)
	public Object[][] hotlistWithoutContactTestData() {
		Company company = new Company();
		Contact contact = new Contact();
		company.setCompanyname(companyName);
		company.setLogo(javaFakerCompany.getLogoURL());
		contact.setFirstname(contactFirstName);
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/company",
				albatrossauthToken, null, true, companies);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		int contactId = Integer.valueOf(jsonPath.getString("data.contact.id"));
		JsonPath jsonContactHotlist = function.createNewHotlist(baseURL, apiAuthToken, "contact").jsonPath();
		String hotlistName = jsonContactHotlist.get("name");
		return new Object[][] { { hotlistName, contactId } };
	}
	
	@DataProvider(parallel = true)
	public Object[][] hotlistWithContactTestData() {
		Company company = new Company();
		Contact contact = new Contact();
		company.setCompanyname(companyName);
		company.setLogo(javaFakerCompany.getLogoURL());
		contact.setFirstname(contactFirstName);
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/company",
				albatrossauthToken, null, true, companies);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		int contactId = Integer.valueOf(jsonPath.getString("data.contact.id"));

		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("contacts");
		hotlist.setName(javaFakerCompany.getCompanyName() + "hotlist");
		List<Integer> selectedContacts = new ArrayList<>();
		selectedContacts.add(contactId);
		hotlist.setSelectedrows(selectedContacts);
		Response responseHotlist = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossauthToken, null, true, hotlist);
		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath hotlistJsonPath = responseHotlist.jsonPath();
		String hotlistName = hotlistJsonPath.get("data.hotlist[0].name");
		return new Object[][] { { hotlistName, contactId } };
	}

	private void assertContactDetails(JsonPath jsonPath, String contactRequiredData[], String contactExpectedData[]) {
		for (int i = 0; i < contactRequiredData.length; i++) {
			Assert.assertEquals(jsonPath.get("data.contact." + contactRequiredData[i]), contactExpectedData[i]);
		}
	}

	@DataProvider
	public Object[][] getCompanyTestData() {
		Company company = new Company();
		company.setCompanyname(javaFakerCompany.getCompanyName());
		company.setLogo(javaFakerCompany.getLogoURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/company",
				albatrossauthToken, null, true, companies);
		JsonPath jsonPath = response.jsonPath();
		response.then().statusCode(200);
		String companyName = jsonPath.getString("data.company.companyname");
		int companyId = jsonPath.getInt("data.company.id");
		return new Object[][] { { companyId, companyName } };
	}

	@DataProvider(parallel = true)
	public Object[][] getContactDuplicateTestData() {
		enableMergeDuplicateContact();
		JsonPath companyJsonPath = function
				.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		JsonPath contactJsonPath = function.createNewContactWithAllFields(baseURL, apiAuthToken,
				companyJsonPath.getString("slug")).jsonPath();
		Map<String, String> contactTestData = new HashMap<>();
		contactTestData.put("companySlug", companyJsonPath.getString("slug"));
		contactTestData.put("companyName", companyJsonPath.getString("company_name"));
		contactTestData.put("companyWebsite", companyJsonPath.getString("website"));
		contactTestData.put("contactEmailId", contactJsonPath.getString("email"));
		contactTestData.put("contactFirstName", contactJsonPath.getString("first_name"));
		contactTestData.put("contactLastName", contactJsonPath.getString("last_name"));
		contactTestData.put("contactSlug", contactJsonPath.getString("slug"));
		contactTestData.put("contactLinkedin", contactJsonPath.getString("linkedin"));
		contactTestData.put("contactAddress", contactJsonPath.getString("address"));
		contactTestData.put("contactNumber", contactJsonPath.getString("contact_number"));
		contactTestData.put("contactDesignation", contactJsonPath.getString("designation"));
		contactTestData.put("contactFacebook", contactJsonPath.getString("facebook"));
		contactTestData.put("contactTwitter", contactJsonPath.getString("twitter"));
		return new Object[][] { { contactTestData, false }, { contactTestData, true } };
	}

	public void enableMergeDuplicateContact() {
		DuplicateMergeSetting duplicateMergeSetting = new DuplicateMergeSetting();
		duplicateMergeSetting.setId(ownerAccountID);
		duplicateMergeSetting.setKey("allowduplicatecontacts");
		duplicateMergeSetting.setTableFlag("account");
		duplicateMergeSetting.setValue("0");
		Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields",
				albatrossauthToken, null, true, duplicateMergeSetting);
		response.then().statusCode(200);
	}
}
