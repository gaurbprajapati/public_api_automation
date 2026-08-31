package io.rcrm.api.contact;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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

@AccountType("Business")
public class SearchByContactSlugTest extends TestBase {

	public SearchByContactSlugTest() {
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

	String slug = "";
	String companySlug = "";
	String apiAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "contactData", groups = "nightly-build")
	public void searchContactBySlug_GET(String slug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", slug);
		String basePath = "contacts/{contact}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, true);

		response.then().statusCode(200);
		response.then().body("first_name", Matchers.is(ContactFirstName));
		response.then().body("last_name", Matchers.is(ContactLastName));
		response.then().body("email", Matchers.is(ContactEmail));
		response.then().body("contact_number", Matchers.is(contactNumber));
	}
	
	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void searchContactByInvalidSlug_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", slug + "123456");
		String basePath = "contacts/{contact}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, true);

		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Contact doesn't exist"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void searchContactWithUnauthorizedAccess_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", slug + "123456");
		String basePath = "contacts/{contact}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, apiAuthToken + "123", null, pathParamters, true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}
	
	@DataProvider
	public Object[][] contactData() {
		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, "");
		Response response = RestClient.doPost("JSON", baseURL, "contacts", apiAuthToken, null, true, contact);
		String slug = response.jsonPath().get("slug");

		return new Object[][] { { slug } };
	}
}
