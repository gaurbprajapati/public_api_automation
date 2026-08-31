package io.recruitcrm.albatross.company;

import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.recruitcrm.albatross.contact.Contact;
import org.hamcrest.Matchers;
import org.testng.annotations.*;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.albatross.DuplicateMergeSetting;
import io.rcrm.api.pojo.albatross.Login;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateNewCompanyTest extends TestBase {

	public CreateNewCompanyTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	JavaFakerCompany faker;
	commanFunction function;
	String companyName;
	String companyWebsite;
	String companyCity;
	String address;
	int industry_id;
	String logo;
	String aboutCompany;
	JavaFakerContact contactFaker;
	String contactFirstName;
	String contactLastName;
	String contactEmail;
	String contactNumber;
	// String resume =
	// "https://files-for-testing.s3.ap-southeast-1.amazonaws.com/Anchit+Lalwani+Resume.pdf";

	String albatrossAuthToken;
	String apiAuthToken;
	int ownerAccountID;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		faker = new JavaFakerCompany();
		function = new commanFunction();
		companyName = faker.getCompanyName();
		companyWebsite = faker.getUrl();
		companyCity = faker.getCity();
		address = faker.getAddress();
		industry_id = faker.getIndustry_id();
		logo = faker.getLogoURL();
		aboutCompany = faker.getCompanyAbout();
		contactFaker = new JavaFakerContact();
		contactFirstName = contactFaker.getFirstName();
		contactLastName = contactFaker.getLastName();
		contactEmail = "rcrmtest0@gmail.com";
		contactNumber = contactFaker.getContactNumber();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
		ownerAccountID = ThreadManager.getAccount().getAccountId();
	}

	@Owner("Akshaya Uppala")
	@Test
	public void getTokenKey_test() {

		Login login = new Login();
		login.setEmail("dev31122021@yopmail.com");
		login.setPassword("123456");

		String baseURL = albatrossURL;
		// Response response = RestClient.doPost("multipart", baseURL, "parse",
		// authTokenMap, null, true, login);

		Response response = RestClient.doPost("JSON", baseURL, "login", albatrossAuthToken, null, true, login);


		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().statusCode(200);

	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewCompany_albatross_test() {

		Company company = new Company();
		company.setCompanyname(companyName);
		company.setWebsite(companyWebsite);
		company.setAboutcompany(companyName+"\n"+ companyWebsite +"\n"+ address);
		company.setCity(companyCity);
		company.setIndustryid(industry_id);
		company.setAddress(address);

		Contact contact = new Contact();
		contact.setFirstname(contactFirstName);
		contact.setLastname(contactLastName);
		contact.setContactnumber(contactNumber);
		contact.setEmail(contactEmail);
		contact.setStageid("1");

		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);
		companyJson.setContact(contact);

		String baseURL = albatrossURL;

		Response response = RestClient.doPost("JSON", baseURL, "companies", albatrossAuthToken, null, true, companyJson);


		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().statusCode(200);
		response.then().body("message", Matchers.containsString("Company Added"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void createNewCompanyWithInvalidData_albatross_test() {

		Company company = new Company();
		company.setCompanyname("");
		company.setWebsite(companyWebsite);
		company.setAboutcompany(companyName+"\n"+ companyWebsite +"\n"+ address);
		company.setCity(companyCity);
		company.setIndustryid(industry_id);
		company.setAddress(address);

		Contact contact = new Contact();
		contact.setFirstname(contactFirstName);
		contact.setLastname(contactLastName);
		contact.setContactnumber(contactNumber);
		contact.setEmail(contactEmail);
		contact.setStageid("1");

		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);
		companyJson.setContact(contact);

		String baseURL = albatrossURL;

		Response response = RestClient.doPost("JSON", baseURL, "companies", albatrossAuthToken, null, true, companyJson);


		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		response.then().statusCode(422);
		response.then().body("message", Matchers.containsString("Failed to Add Company : Company Name is mandatory"));
		response.then().body("message_type", Matchers.containsString("is-danger"));

	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getCompanyDuplicateNameTestData", groups = "nightly-build")
	public void createCompanyAndVerifyDuplicateMergeByName_Test(String companySlug, String companyName, String companyWebsite) {
		Company company = new Company();
		company.setCompanyname(companyName);
		company.setWebsite(companyWebsite);
		company.setAboutcompany(companyName + "\n" + companyWebsite + "\n" + address);
		company.setCity(companyCity);
		company.setIndustryid(industry_id);
		company.setAddress(address);

		Contact contact = new Contact();
		contact.setFirstname(contactFirstName);
		contact.setLastname(contactLastName);
		contact.setContactnumber(contactNumber);
		contact.setEmail(contactEmail);
		contact.setStageid("1");

		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);
		companyJson.setContact(contact);

		Response response = RestClient.doPost("JSON", albatrossURL, "companies", albatrossAuthToken,
				null, true, companyJson);

		JsonPath jsonPath = response.jsonPath();

		response.then().statusCode(200);
		Assert.assertEquals(jsonPath.getString("message"), "Duplicate Company Updated Successful ");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("data.company.slug"), companySlug);
		Assert.assertEquals(jsonPath.getString("data.company.city"), companyCity);
		Assert.assertEquals(jsonPath.getString("data.company.address"), address);
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getCompanyDuplicateWebsiteTestData", groups = "nightly-build")
	public void createCompanyAndVerifyDuplicateMergeByWebsite_Test(String companySlug, String companyName, String companyWebsite) {
		Company company = new Company();
		company.setCompanyname(companyName);
		company.setWebsite(companyWebsite);
		company.setAboutcompany(companyName + "\n" + companyWebsite + "\n" + address);
		company.setCity(companyCity);
		company.setIndustryid(industry_id);
		company.setAddress(address);

		Contact contact = new Contact();
		contact.setFirstname(contactFirstName);
		contact.setLastname(contactLastName);
		contact.setContactnumber(contactNumber);
		contact.setEmail(contactEmail);
		contact.setStageid("1");

		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);
		companyJson.setContact(contact);

		Response response = RestClient.doPost("JSON", albatrossURL, "companies", albatrossAuthToken,
				null, true, companyJson);

		JsonPath jsonPath = response.jsonPath();

		response.then().statusCode(200);
		Assert.assertEquals(jsonPath.getString("message"), "Duplicate Company Updated Successful ");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("data.company.slug"), companySlug, " expected companySlug :" + companySlug + " but actual " + jsonPath.getString("data.company.slug"));
		Assert.assertEquals(jsonPath.getString("data.company.city"), companyCity , " expected companyCity :" + companyCity + " but actual " + jsonPath.getString("data.company.city"));
		Assert.assertEquals(jsonPath.getString("data.company.address"), address , " expected address address : " + address + " but actual " + jsonPath.getString("data.company.address"));
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getCompanyDuplicateLinkedinTestData", groups = "nightly-build")
	public synchronized void createCompanyAndVerifyDuplicateMergeByLinkedinUrl_Test(String companySlug,
																					String linkedinUrl) {
		Company company = new Company(companyName, aboutCompany, companyCity, industry_id, companyWebsite, address);
		company.setProfilelinkedin(linkedinUrl);
		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);
		companyJson.setContact(new Contact(contactFirstName, contactLastName,contactEmail, contactFaker.getCity(), contactNumber, contactFaker.getStage()));

		Response response = RestClient.doPost("JSON", albatrossURL, "companies", albatrossAuthToken, null, true,
				companyJson);

		JsonPath jsonPath = response.jsonPath();

		response.then().statusCode(200);
		Assert.assertEquals(jsonPath.getString("message"), "Duplicate Company Updated Successful ");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("data.company.slug"), companySlug, " expected companySlug :" + companySlug + " but actual " + jsonPath.getString("data.company.slug"));
		Assert.assertEquals(jsonPath.getString("data.company.city"), companyCity , " expected companyCity :" + companyCity + " but actual " + jsonPath.getString("data.company.city"));
		Assert.assertEquals(jsonPath.getString("data.company.address"), address , " expected address address : " + address + " but actual " + jsonPath.getString("data.company.address"));
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void verifyLinkedInFieldDoesNotMergeDifferentCompAsDuplicate_Test() throws InterruptedException {
		String companyLinkedinUrl = faker.getUrl();
		function.enableMergeDuplicate(ownerAccountID, albatrossURL, albatrossAuthToken, "companies");
		CompanyJson companyJson1 = createCompanyJson(companyName, companyWebsite);
		CompanyJson companyJson2 = createCompanyJson(faker.getCompanyName(), companyLinkedinUrl);
		final Response[] responses = new Response[2];

		Thread thread1 = new Thread(() -> responses[0] = RestClient.doPost("JSON", albatrossURL, "companies",
				albatrossAuthToken, null, true, companyJson1));
		Thread thread2 = new Thread(() -> responses[1] = RestClient.doPost("JSON", albatrossURL, "companies",
				albatrossAuthToken, null, true, companyJson2));

		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		JsonPath jsonPath = responses[1].jsonPath();

		Assert.assertEquals(responses[1].getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message"), "Company Added");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("data.company.city"), companyCity);
		Assert.assertEquals(jsonPath.getString("data.company.address"), address);
		Assert.assertEquals(jsonPath.getString("data.company.profilelinkedin"), companyLinkedinUrl);
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getContactData", groups = "nightly-build")
	public void createNewCompanyWithExistingContactsAlb_Test(String contactSlug1, String contactSlug2,
															 String contactSlug3) {

		Company company = new Company();
		company.setCompanyname(companyName);
		company.setWebsite(companyWebsite);
		company.setCity(companyCity);
		company.setIndustryid(industry_id);
		company.setAddress(address);

		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);
		companyJson.setExistingContacts(contactSlug1 + "," + contactSlug2 + "," + contactSlug3);

		companyJson.setContact(new Contact(contactFirstName, contactLastName,contactEmail, contactFaker.getCity(), contactNumber, contactFaker.getStage()));

		Response response = RestClient.doPost("JSON", albatrossURL, "companies", albatrossAuthToken,
				null, true, companyJson);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.containsString("Company Added"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}


	@DataProvider
	public Object[][] getCompanyDuplicateNameTestData() {
		enableMergeDuplicateCompany();
		Map<String, String> companyData = getCompanyTestData();
		String companySlug = companyData.get("slug");
		String companyName = companyData.get("company_name");
		String randomCompanyWebsite = faker.getUrl();
		return new Object[][] { { companySlug, companyName, randomCompanyWebsite },
				{ companySlug, companyName.toLowerCase(), randomCompanyWebsite },
				{ companySlug, companyName.toUpperCase(), randomCompanyWebsite }, };
	}

	@DataProvider
	public Object[][] getCompanyDuplicateWebsiteTestData() {
		enableMergeDuplicateCompany();
		Map<String, String> companyData = getCompanyTestData();
		String companySlug = companyData.get("slug");
		String randomCompanyName = faker.getCompanyName();
		String companyWebsite = companyData.get("website").substring(12);
		System.out.println("companyWebsite "+ companyWebsite);
		String companyWebsiteWithHTTPS = "https://www." + companyWebsite;
		String companyWebsiteWithHTTP = "http://www." + companyWebsite;
		String companyWebsiteWithoutHTTP = "www." + companyWebsite;
		return new Object[][] { { companySlug, randomCompanyName, companyWebsite },
				{ companySlug, randomCompanyName, companyWebsiteWithHTTPS },
				{ companySlug, randomCompanyName, companyWebsiteWithHTTP },
				{ companySlug, randomCompanyName, companyWebsiteWithoutHTTP } };
	}

	public void enableMergeDuplicateCompany() {
		DuplicateMergeSetting duplicateMergeSetting = new DuplicateMergeSetting();
		duplicateMergeSetting.setId(ThreadManager.getAccount().getAccountId());
		duplicateMergeSetting.setKey("allowduplicatecompanies");
		duplicateMergeSetting.setTableFlag("account");
		duplicateMergeSetting.setValue("0");
		Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields",
				ThreadManager.getOwnerAlbatrossToken(), null, true, duplicateMergeSetting);
		response.then().statusCode(200);
	}

	public Map<String, String> getCompanyTestData() {
		Map<String, String> companyTestData = new HashMap<>();

		io.rcrm.api.pojo.Company company = new io.rcrm.api.pojo.Company(companyName, companyWebsite, contactNumber, faker.getLogoURL());
		company.setIndustry_id(industry_id);
		company.setLinkedin("https://www.linkedin.com/in/" + companyName.split(" ")[0]);
		company.setFacebook("https://www.facebook.com/" + companyName.split(" ")[0]);
		company.setTwitter("https://www.twitter.com/" + companyName.split(" ")[0]);
		company.setAbout_company("This is about company " + companyName);
		Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);

		JsonPath companyJsonPath  = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);

		String companySlug = companyJsonPath.getString("slug");
		String companyName = companyJsonPath.getString("company_name");
		String companyWebsite = companyJsonPath.getString("website");
		companyTestData.put("slug", companySlug);
		companyTestData.put("company_name", companyName);
		companyTestData.put("website", companyWebsite);
		return companyTestData;
	}

	@DataProvider(parallel = true)
	public Object[][] getCompanyDuplicateLinkedinTestData() {
		function.enableMergeDuplicate(ownerAccountID, albatrossURL, albatrossAuthToken, "companies");
		Map<String, String> companyData = getCompanyTestData();
		String companySlug = companyData.get("slug");
		String companyName = companyData.get("company_name").split(" ")[0];
		return new Object[][] { { companySlug, "http://www.linkedin.com/in/" + companyName },
				{ companySlug, "www.linkedin.com/in/" + companyName }, { companySlug, "linkedin.com/in/" +companyName } };
	}

	private CompanyJson createCompanyJson(String companyName, String companyLinkedinUrl) {
		Company company = new Company(companyName, aboutCompany, companyCity, industry_id, companyWebsite, address);
		company.setProfilelinkedin(companyLinkedinUrl);
		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);
		companyJson.setContact(new Contact(contactFirstName, contactLastName, contactEmail, contactFaker.getCity(), contactNumber, contactFaker.getStage()));
		return companyJson;
	}

	@DataProvider(parallel = true)
	public Object[][] getContactData() {

		String contactSlug1 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath()
				.getString("slug");
		String contactSlug2 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath()
				.getString("slug");
		String contactSlug3 = function.createNewContact_POST(baseURL, apiAuthToken, "").jsonPath()
				.getString("slug");

		Object data[][] = { { contactSlug1, contactSlug2, contactSlug3 } };

		return data;
	}
}