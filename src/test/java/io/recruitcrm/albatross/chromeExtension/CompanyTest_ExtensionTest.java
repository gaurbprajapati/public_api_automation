package io.recruitcrm.albatross.chromeExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import io.rcrm.api.commanfunctions.commanFunction;
import org.testng.Assert;
import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.*;
import io.rcrm.api.javafaker.*;
import io.rcrm.api.pojo.albatross.DuplicateMergeSetting;
import io.rcrm.api.pojo.chromeExtension.*;
import io.rcrm.api.pojo.chromeExtension.Companies.Company;
import io.rcrm.api.pojo.chromeExtension.Companies.Contact;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CompanyTest_ExtensionTest extends TestBase {

	JavaFakerCompany javaFakerCompany;
	JavaFakerContact javaFakerContact;
	String companyName, companyWebsite, aboutCompany, companyAddress;
	String contactFirstName, contactLastName, contactEmailId, contactDesignation, contactNumber;
	commanFunction function;
	String albatrossAuthToken;
	int ownerAccountID;
	String apiAuthToken;
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
		function = new commanFunction();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		ownerAccountID = ThreadManager.getAccount().getAccountId();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createNewCompany_Extension() {
		Company company = new Company(companyName, companyWebsite, aboutCompany, companyAddress);
		company.setProfilelinkedin(javaFakerCompany.getCompanyLinkedinURL());
		company.setLogo(javaFakerCompany.getLogoURL());
		Contact contact = new Contact(contactFirstName, contactLastName, contactDesignation, contactEmailId,
				contactNumber);
		contact.setProfilelinkedin(javaFakerContact.getContactLinkedinURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/company",
				albatrossAuthToken,
				null, true, companies);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.get("message"), "Add Company And Contact From Extension Successful");

		String companyRequiredData[] = { "companyname", "aboutcompany", "website", "address", "profilelinkedin" };
		String companyexpectedData[] = { companyName, aboutCompany, companyWebsite, companyAddress,
				company.getProfilelinkedin() };

		String contactRequiredData[] = { "firstname", "lastname", "designation", "email", "companyname",
				"contactnumber", "profilelinkedin" };
		String contactExpectedData[] = { contactFirstName, contactLastName, contactDesignation, contactEmailId,
				companyName, contactNumber, contact.getProfilelinkedin() };

		assertCompanyContactDetails(jsonPath, companyRequiredData, companyexpectedData, contactRequiredData,
				contactExpectedData);
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getCompanyDuplicateNameTestData", groups = "nightly-build")
	public void createCompanyAndVerifyDuplicateMergeByName_Extension(String companySlug, String companyName,
			String companyWebsite) {
		Company company = new Company(companyName, companyWebsite, aboutCompany, companyAddress);
		company.setProfilelinkedin(javaFakerCompany.getCompanyLinkedinURL());
		company.setLogo(javaFakerCompany.getLogoURL());
		Contact contact = new Contact(contactFirstName, contactLastName, contactDesignation, contactEmailId,
				contactNumber);
		contact.setProfilelinkedin(javaFakerContact.getContactLinkedinURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);

		Object result = postCompanyDataWithExponentialBackoff(companies, Object.class);

		if (result instanceof Boolean && !(Boolean) result) {
			Assert.fail("Failed to get a valid response after maximum retry attempts.");
		}

		Response response = (Response) result;

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("message"), "Duplicate Company Updated Successful ");

		String companyRequiredData[] = { "slug", "aboutcompany", "address" };
		String companyexpectedData[] = { companySlug, aboutCompany, companyAddress };

		String contactRequiredData[] = { "firstname", "lastname", "designation", "email", "contactnumber" };
		String contactExpectedData[] = { contactFirstName, contactLastName, contactDesignation, contactEmailId,
				contactNumber };

		assertCompanyContactDetails(jsonPath, companyRequiredData, companyexpectedData, contactRequiredData,
				contactExpectedData);
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getCompanyDuplicateWebsiteTestData", groups = "nightly-build")
	public void createCompanyAndVerifyDuplicateMergeByWebsite_Extension(String companySlug, String companyName,
			String companyWebsite) {
		Company company = new Company(companyName, companyWebsite, aboutCompany, companyAddress);
		company.setProfilelinkedin(javaFakerCompany.getCompanyLinkedinURL());
		company.setLogo(javaFakerCompany.getLogoURL());
		Contact contact = new Contact(contactFirstName, contactLastName, contactDesignation, contactEmailId,
				contactNumber);
		contact.setProfilelinkedin(javaFakerContact.getContactLinkedinURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);

		Object result = postCompanyDataWithExponentialBackoff(companies, Object.class);

		if (result instanceof Boolean && !(Boolean) result) {
			Assert.fail("Failed to get a valid response after maximum retry attempts.");
		}

		Response response = (Response) result;

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("message"), "Duplicate Company Updated Successful ");

		String companyRequiredData[] = { "slug", "aboutcompany", "address" };
		String companyexpectedData[] = { companySlug, aboutCompany, companyAddress };

		String contactRequiredData[] = { "firstname", "lastname", "designation", "email", "contactnumber" };
		String contactExpectedData[] = { contactFirstName, contactLastName, contactDesignation, contactEmailId,
				contactNumber };

		assertCompanyContactDetails(jsonPath, companyRequiredData, companyexpectedData, contactRequiredData,
				contactExpectedData);
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getContactCompanyTestData", groups = "nightly-build")
	public void createCompanyContactAndVerifyContactMergeByEmailId_Extension(String contactSlug, String contactName,
			String contactEmailId, String companyname, String companySlug) {
		Company company = new Company(companyName, companyWebsite, aboutCompany, companyAddress);
		company.setProfilelinkedin(javaFakerCompany.getCompanyLinkedinURL());
		company.setLogo(javaFakerCompany.getLogoURL());
		Contact contact = new Contact(contactFirstName, contactLastName, contactDesignation, contactEmailId,
				contactNumber);
		contact.setProfilelinkedin(javaFakerContact.getContactLinkedinURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);
		Object result = postCompanyDataWithExponentialBackoff(companies, Object.class);

		if (result instanceof Boolean && !(Boolean) result) {
			Assert.fail("Failed to get a valid response after maximum retry attempts.");
		}

		Response response = (Response) result;

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");

		String companyRequiredData[] = { "companyname", "aboutcompany", "website", "address", "profilelinkedin" };
		String companyexpectedData[] = { companyName, aboutCompany, companyWebsite, companyAddress,
				company.getProfilelinkedin() };

		String contactRequiredData[] = { "companyslug", "slug", "email", "companyname", "firstname", "lastname",
				"designation", "contactnumber", "profilelinkedin" };
		String contactExpectedData[] = { companySlug, contactSlug, contactEmailId, companyname, contactName,
				contactLastName, contactDesignation, contactNumber, contact.getProfilelinkedin() };

		assertCompanyContactDetails(jsonPath, companyRequiredData, companyexpectedData, contactRequiredData,
				contactExpectedData);

	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getCompanyOverrideDataByNameTestData", groups = "nightly-build")
	public void createDuplicateCompanyAndVerifyDataOverrideByCompanyName_Extension(String companySlug,
			String companyName) {

		Company company = new Company(companyName, javaFakerCompany.getCompanyWebsite(),
				javaFakerCompany.getCompanyAbout(), javaFakerCompany.getAddress());
		company.setProfilelinkedin(javaFakerCompany.getCompanyLinkedinURL());
		company.setLogo(javaFakerCompany.getLogoURL());
		Contact contact = new Contact(contactFirstName, contactLastName, contactDesignation, contactEmailId,
				contactNumber);
		contact.setProfilelinkedin(javaFakerContact.getContactLinkedinURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);
		companies.setOverrideData(true);
		synchronized (lock) {
			Object result = postCompanyDataWithExponentialBackoff(companies, Object.class);

			if (result instanceof Boolean && !(Boolean) result) {
				Assert.fail("Failed to get a valid response after maximum retry attempts.");
			}

			Response response = (Response) result;

			JsonPath jsonPath = response.jsonPath();
			Assert.assertEquals(response.getStatusCode(), 200);
			Assert.assertEquals(jsonPath.get("message_type"), "is-success");
			Assert.assertEquals(jsonPath.get("message"), "Duplicate Company Updated Successful ");

			String companyRequiredData[] = { "companyname", "slug", "aboutcompany", "website", "address",
					"profilelinkedin" };
			String companyexpectedData[] = { companyName, companySlug, company.getAboutcompany(), company.getWebsite(),
					company.getFulladdress(), company.getProfilelinkedin() };

			String contactRequiredData[] = { "firstname", "lastname", "designation", "email", "companyname",
					"contactnumber", "profilelinkedin" };
			String contactExpectedData[] = { contactFirstName, contactLastName, contactDesignation, contactEmailId,
					companyName, contactNumber, contact.getProfilelinkedin() };

			assertCompanyContactDetails(jsonPath, companyRequiredData, companyexpectedData, contactRequiredData,
					contactExpectedData);
		}
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getCompanyOverrideDataByWebsiteTestData", groups = "nightly-build")
	public void createDuplicateCompanyAndVerifyDataOverrideByCompanyWebsite_Extension(String companySlug,
			String companyWebsite) {

		JavaFakerCompany javaFakerCompany = new JavaFakerCompany();
		Company company = new Company(javaFakerCompany.getCompanyName(), companyWebsite,
				javaFakerCompany.getCompanyAbout(), javaFakerCompany.getAddress());
		company.setProfilelinkedin(javaFakerCompany.getCompanyLinkedinURL());
		company.setLogo(javaFakerCompany.getLogoURL());
		Contact contact = new Contact(contactFirstName, contactLastName, contactDesignation, contactEmailId,
				contactNumber);
		contact.setProfilelinkedin(javaFakerContact.getContactLinkedinURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);
		companies.setOverrideData(true);
		synchronized (lock) {
			Object result = postCompanyDataWithExponentialBackoff(companies, Object.class);

			if (result instanceof Boolean && !(Boolean) result) {
				Assert.fail("Failed to get a valid response after maximum retry attempts.");
			}

			Response response = (Response) result;

			JsonPath jsonPath = response.jsonPath();
			Assert.assertEquals(response.getStatusCode(), 200);
			Assert.assertEquals(jsonPath.get("message_type"), "is-success");
			Assert.assertEquals(jsonPath.get("message"), "Duplicate Company Updated Successful ");

			String companyRequiredData[] = { "companyname", "slug", "aboutcompany", "website", "address",
					"profilelinkedin" };
			String companyexpectedData[] = { company.getCompanyname(), companySlug, company.getAboutcompany(),
					companyWebsite, company.getFulladdress(), company.getProfilelinkedin() };

			String contactRequiredData[] = { "firstname", "lastname", "designation", "email", "companyname",
					"contactnumber", "profilelinkedin" };
			String contactExpectedData[] = { contactFirstName, contactLastName, contactDesignation, contactEmailId,
					company.getCompanyname(), contactNumber, contact.getProfilelinkedin() };

			assertCompanyContactDetails(jsonPath, companyRequiredData, companyexpectedData, contactRequiredData,
					contactExpectedData);

		}
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getContactCompanyTestData", groups = "nightly-build")
	public void createCompanyContactAndVerifyOverrideContactDataByEmailId_Extension(String contactSlug,
			String contactName, String contactEmailId, String companyname, String companySlug) {
		Company company = new Company(companyName, companyWebsite, aboutCompany, companyAddress);
		company.setProfilelinkedin(javaFakerCompany.getCompanyLinkedinURL());
		company.setLogo(javaFakerCompany.getLogoURL());
		Contact contact = new Contact(javaFakerContact.getFirstName(), javaFakerContact.getLastName(),
				javaFakerContact.getDesignation(), contactEmailId, javaFakerContact.getContactNumber());
		contact.setProfilelinkedin(javaFakerContact.getContactLinkedinURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);
		companies.setOverrideData(true);
		Object result = postCompanyDataWithExponentialBackoff(companies, Object.class);

		if (result instanceof Boolean && !(Boolean) result) {
			Assert.fail("Failed to get a valid response after maximum retry attempts.");
		}

		Response response = (Response) result;

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("message"), "Updated Company And Contact Successful ");

		String companyRequiredData[] = { "companyname", "aboutcompany", "website", "address", "profilelinkedin" };
		String companyexpectedData[] = { companyName, aboutCompany, companyWebsite, companyAddress,
				company.getProfilelinkedin() };

		String contactRequiredData[] = { "companyslug", "slug", "email", "companyname", "firstname", "lastname",
				"designation", "contactnumber", "profilelinkedin" };
		String contactExpectedData[] = { companySlug, contactSlug, contactEmailId, companyname,
				contact.getFirstname(), contact.getLastname(), contact.getDesignation(), contact.getContactnumber(),
				contact.getProfilelinkedin() };

		assertCompanyContactDetails(jsonPath, companyRequiredData, companyexpectedData, contactRequiredData,
				contactExpectedData);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createNewCompanyWithEmptyRequestBody_Extension() {
		Company company = new Company();
		company.setProfilelinkedin(javaFakerCompany.getCompanyLinkedinURL());
		company.setLogo(javaFakerCompany.getLogoURL());
		Contact contact = new Contact(contactFirstName, contactLastName, contactDesignation, contactEmailId,
				contactNumber);
		contact.setProfilelinkedin(javaFakerContact.getContactLinkedinURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/company",
				albatrossAuthToken,
				null, true, companies);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"),
				"Failed To Add Company And Contact From Extension : Company Name is mandatory");
		Assert.assertEquals(jsonPath.get("user.role"), "Account Owner");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotCreateNewCompany_Extension() {
		Company company = new Company(companyName, companyWebsite, aboutCompany, companyAddress);
		company.setProfilelinkedin(javaFakerCompany.getCompanyLinkedinURL());
		company.setLogo(javaFakerCompany.getLogoURL());
		Contact contact = new Contact(contactFirstName, contactLastName, contactDesignation, contactEmailId,
				contactNumber);
		contact.setProfilelinkedin(javaFakerContact.getContactLinkedinURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/company",
				albatrossAuthToken + "abcd", null, true, companies);

		JsonPath jsonPath = response.jsonPath();
		response.then().statusCode(200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createCompanyHotlist_Extension() {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("companies");
		hotlist.setName(companyName + "hotlist");
		List<Integer> selectedCompanies = new ArrayList<>();
		hotlist.setSelectedrows(selectedCompanies);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossAuthToken, null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		response.then().statusCode(200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		Assert.assertEquals(jsonPath.get("message"), "Hotlist created and Companies added successfully");
		Assert.assertEquals(jsonPath.get("data.hotlist[0].name"), companyName + "hotlist");
		Assert.assertEquals(jsonPath.getInt("data.hotlist[0].shared"), 1);
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "hotlistWithoutCompanyTestData", groups = "nightly-build")
	public void addCompanyInExistingHotlist_Extension(int companyId, String hotlistName) {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("companies");
		hotlist.setName(hotlistName);
		List<Integer> selectedCompanies = new ArrayList<>();
		selectedCompanies.add(companyId);
		hotlist.setSelectedrows(selectedCompanies);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossAuthToken, null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		Assert.assertEquals(jsonPath.get("message"), "Add To Hotlist Successful");
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "hotlistWithCompanyTestData", groups = "nightly-build")
	public void removeCompanyFromExitingHotlist_Extension(int companyId, String hotlistName) {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("companies");
		hotlist.setName(hotlistName);
		List<Integer> selectedCompanies = new ArrayList<>();
		hotlist.setSelectedrows(selectedCompanies);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossAuthToken, null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		Assert.assertEquals(jsonPath.get("message"), "Update Hotlist Successful");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createCompanyHotlistWithEmptyRequestBody_Extension() {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("companies");
		List<Integer> selectedCompanies = new ArrayList<>();
		hotlist.setSelectedrows(selectedCompanies);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossAuthToken, null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Failed To Create Hotlist : The name field is required.");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createCompanyHotlistWithEmptyEntityName_Extension() {
		Hotlist hotlist = new Hotlist();
		hotlist.setName(companyName + "hotlist");
		List<Integer> selectedCompanies = new ArrayList<>();
		hotlist.setSelectedrows(selectedCompanies);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossAuthToken, null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Failed To Create Hotlist : The entity name field is required.");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotCreateCompanyHotlist_Extension() {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("companies");
		List<Integer> selectedCompanies = new ArrayList<>();
		hotlist.setSelectedrows(selectedCompanies);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossAuthToken + "abcd", null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getCompanyHotlist_Extension() {
		JsonPath jsonCompanyHotlist = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "company")
				.jsonPath();
		String hotlistName = jsonCompanyHotlist.get("name");
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("companies");
		List<Integer> selectedCompanies = new ArrayList<>();
		hotlist.setSelectedrows(selectedCompanies);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists/get",
				albatrossAuthToken, null, true, hotlist);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		int allHotlistSize = jsonPath.getList("data.records").size();
		Assert.assertEquals(allHotlistSize, 1);
		Assert.assertEquals(jsonPath.get("data[0].name"), hotlistName);
		Assert.assertEquals(jsonPath.get("data[0].entityname"), "companies");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getCompanyHotlistWithEmptyEntityName_Extension() {
		Hotlist hotlist = new Hotlist();
		List<Integer> selectedCompanies = new ArrayList<>();
		hotlist.setSelectedrows(selectedCompanies);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists/get",
				albatrossAuthToken, null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "The entity name field is required.");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotGetCompanyHotlist_Extension() {
		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("companies");
		List<Integer> selectedCompanies = new ArrayList<>();
		hotlist.setSelectedrows(selectedCompanies);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/related-hotlists",
				albatrossAuthToken + "abcd", null, true, hotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "hotlistWithCompanyTestData", groups = "nightly-build")
	public void getHotlistRelatedToCompanyId_Extension(int companyId, String hotlistName) {
		EntityRelatedHotlist entityRelatedHotlist = new EntityRelatedHotlist(companyId, "companies");
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/related-hotlists",
				albatrossAuthToken, null, true, entityRelatedHotlist);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		int allHotlistSize = jsonPath.getList("data.hotlists").size();
		Assert.assertEquals(allHotlistSize, 1);
		Assert.assertEquals(jsonPath.get("data.hotlists[0].name"), hotlistName);
		Assert.assertEquals(jsonPath.get("data.hotlists[0].entityname"), "companies");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getHotlistRelatedToCompanyIdWithEmptyRequestBody_Extension() {
		EntityRelatedHotlist entityRelatedHotlist = new EntityRelatedHotlist();
		entityRelatedHotlist.setPagename("companies");
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/related-hotlists",
				albatrossAuthToken, null, true, entityRelatedHotlist);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		int allHotlistSize = jsonPath.getList("data.hotlists").size();
		Assert.assertEquals(allHotlistSize, 0);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotGetHotlistRelatedToCompanyId_Extension() {
		EntityRelatedHotlist entityRelatedHotlist = new EntityRelatedHotlist();
		entityRelatedHotlist.setPagename("companies");
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/related-hotlists",
				albatrossAuthToken + "abcd", null, true, entityRelatedHotlist);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
	}

	@DataProvider(parallel = true)
	public Object[][] hotlistWithoutCompanyTestData() {
		Company company = new Company();
		company.setCompanyname(companyName);
		company.setLogo(javaFakerCompany.getLogoURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/company",
				albatrossAuthToken, null, true, companies);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		int companyId = Integer.valueOf(jsonPath.getString("data.company.id"));
		JsonPath jsonCompanyHotlist = function.createNewHotlist(baseURL, apiAuthToken, "company").jsonPath();
		String hotlistName = jsonCompanyHotlist.get("name");
		return new Object[][] { { companyId, hotlistName } };
	}

	@DataProvider(parallel = true)
	public Object[][] hotlistWithCompanyTestData() {
		Company company = new Company();
		company.setCompanyname(companyName);
		company.setLogo(javaFakerCompany.getLogoURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/company",
				albatrossAuthToken, null, true, companies);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		int companyId = Integer.valueOf(jsonPath.getString("data.company.id"));

		Hotlist hotlist = new Hotlist();
		hotlist.setEntity_name("companies");
		hotlist.setName(javaFakerCompany.getCompanyName() + "hotlist");
		List<Integer> selectedCompanies = new ArrayList<>();
		selectedCompanies.add(companyId);
		hotlist.setSelectedrows(selectedCompanies);
		Response responseHotlist = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
				albatrossAuthToken, null, true, hotlist);
		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath hotlistJsonPath = responseHotlist.jsonPath();
		String hotlistName = hotlistJsonPath.get("data.hotlist[0].name");
		return new Object[][] { { companyId, hotlistName } };
	}

	private void assertCompanyContactDetails(JsonPath jsonPath, String companyRequiredData[],
			String companyExpectedData[], String contactRequiredData[], String contactExpectedData[]) {
		for (int i = 0; i < contactRequiredData.length; i++) {
			Assert.assertEquals(jsonPath.get("data.contact." + contactRequiredData[i]), contactExpectedData[i]);
		}
		for (int i = 0; i < companyRequiredData.length; i++) {
			Assert.assertEquals(jsonPath.get("data.company." + companyRequiredData[i]), companyExpectedData[i]);
		}
	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyOverrideDataByWebsiteTestData() {
		enableMergeDuplicateCompany();
		Map<String, String> companyData = getCompanyTestData();
		String companySlug = companyData.get("slug");
		String companyWebsite = companyData.get("website").substring(12);
		String companyWebsiteWithHTTPS = "https://www." + companyWebsite;
		String companyWebsiteWithHTTP = "http://www." + companyWebsite;
		String companyWebsiteWithoutHTTP = "www." + companyWebsite;
		return new Object[][] { { companySlug, companyWebsite }, { companySlug, companyWebsiteWithHTTPS } };
	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyOverrideDataByNameTestData() {
		enableMergeDuplicateCompany();
		Map<String, String> companyData = getCompanyTestData();
		String companySlug = companyData.get("slug");
		String companyName = companyData.get("company_name");
		String randomCompanyWebsite = javaFakerCompany.getCompanyWebsite();
		return new Object[][] { { companySlug, companyName }, { companySlug, companyName.toLowerCase() },
				{ companySlug, companyName.toUpperCase() } };
	}

	@DataProvider
	public Object[][] getContactCompanyTestData() {
		enableMergeDuplicateContact();
		String contactEmailId = javaFakerContact.getEmailID();
		String ContactName = javaFakerCompany.getCompanyName();
		String companyName = javaFakerCompany.getCompanyName();
		Company company = new Company();
		company.setCompanyname(companyName);
		company.setLogo(javaFakerCompany.getLogoURL());
		Contact contact = new Contact();
		contact.setFirstname(ContactName);
		contact.setEmail(contactEmailId);
		contact.setProfilelinkedin(javaFakerContact.getContactLinkedinURL());
		Companies companies = new Companies();
		companies.setCompany(company);
		companies.setContact(contact);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/company",
				albatrossAuthToken,
				null, true, companies);
		JsonPath jsonPath = response.jsonPath();
		response.then().statusCode(200);
		String contactSlug = jsonPath.getString("data.contact.slug");
		String companySlug = jsonPath.getString("data.company.slug");
		return new Object[][] { { contactSlug, ContactName, contactEmailId, companyName, companySlug } };
	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyDuplicateNameTestData() {
		enableMergeDuplicateCompany();
		Map<String, String> companyData = getCompanyTestData();
		String companySlug = companyData.get("slug");
		String companyName = companyData.get("company_name");
		String randomCompanyWebsite = javaFakerCompany.getCompanyWebsite();
		return new Object[][] { { companySlug, companyName, randomCompanyWebsite },
				{ companySlug, companyName.toLowerCase(), randomCompanyWebsite } };
	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyDuplicateWebsiteTestData() {
		enableMergeDuplicateCompany();
		Map<String, String> companyData = getCompanyTestData();
		String companySlug = companyData.get("slug");
		String randomCompanyName = javaFakerCompany.getCompanyName();
		String companyWebsite = companyData.get("website").substring(12);
		String companyWebsiteWithHTTPS = "https://www." + companyWebsite;
		String companyWebsiteWithHTTP = "http://www." + companyWebsite;
		String companyWebsiteWithoutHTTP = "www." + companyWebsite;
		return new Object[][] { { companySlug, randomCompanyName, companyWebsiteWithoutHTTP },
				{ companySlug, randomCompanyName, companyWebsiteWithHTTP } };
	}

	public void enableMergeDuplicateCompany() {
		DuplicateMergeSetting duplicateMergeSetting = new DuplicateMergeSetting();
		duplicateMergeSetting.setId(ownerAccountID);
		duplicateMergeSetting.setKey("allowduplicatecompanies");
		duplicateMergeSetting.setTableFlag("account");
		duplicateMergeSetting.setValue("0");
		Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", albatrossAuthToken, null,
				true,
				duplicateMergeSetting);
		response.then().statusCode(200);
	}

	public void enableMergeDuplicateContact() {
		DuplicateMergeSetting duplicateMergeSetting = new DuplicateMergeSetting();
		duplicateMergeSetting.setId(ownerAccountID);
		duplicateMergeSetting.setKey("allowduplicatecontacts");
		duplicateMergeSetting.setTableFlag("account");
		duplicateMergeSetting.setValue("0");
		Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", albatrossAuthToken, null,
				true,
				duplicateMergeSetting);
		response.then().statusCode(200);
	}

	public Map<String, String> getCompanyTestData() {
		Map<String, String> companyTestData = new HashMap<>();
		JsonPath companyJsonPath = function
				.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String companySlug = companyJsonPath.getString("slug");
		String companyName = companyJsonPath.getString("company_name");
		String companyWebsite = companyJsonPath.getString("website");
		companyTestData.put("slug", companySlug);
		companyTestData.put("company_name", companyName);
		companyTestData.put("website", companyWebsite);
		return companyTestData;
	}

	private <T> T postCompanyDataWithExponentialBackoff(Companies companies, Class<T> responseType) {
		int maxRetries = 8;
		long initialBackoffMillis = 500;
		for (int retryCount = 1; retryCount <= maxRetries; retryCount++) {
			try {
				Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/company",
						albatrossAuthToken, null, true, companies);
				String responseBody = response.getBody().asString();
				if (responseBody == null || responseBody.isEmpty()) {
					long backoffMillis = initialBackoffMillis * retryCount;
					Thread.sleep(backoffMillis);
				} else {
					return responseType.cast(response);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return responseType.cast(false);
	}
}
