package io.rcrm.api.contact;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.pojo.ContactCustomField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateNewContactTest extends TestBase {

	public CreateNewContactTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	JavaFakerContact contactFaker = new JavaFakerContact();
	JavaFakerCompany companyFaker = new JavaFakerCompany();
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();

	String ContactFirstName = contactFaker.getFirstName();
	String ContactLastName = contactFaker.getContactLastName();
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
	
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

	String albatrossAuthToken;
	String apiAuthToken;

	@BeforeClass(alwaysRun = true)	public void Setup() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewCompanyWithMandatoryFields_POST() {

		Company company = new Company(companyName, companyWebsite, contactNumber, "");
		company.setIndustry_id(industry_id);
		Response response = RestClient.doPost("JSON", baseURL, "companies", ThreadManager.getAccountApiKey(), null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		companySlug = jp.get("slug");
		// 2295174

		response.then().statusCode(200);
		response.then().body("company_name", Matchers.is(companyName));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void createNewContactWithMandatoryFields_POST() {

		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, "");
		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		slug = jp.get("slug");
		// 2295174

		// Verify response
		response.then().statusCode(200);
		response.then().body("first_name", Matchers.is(ContactFirstName));
		response.then().body("last_name", Matchers.is(ContactLastName));
		response.then().body("email", Matchers.is(ContactEmail));
		response.then().body("contact_number", Matchers.is(contactNumber));
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void createNewContactWithEmptyRequestBody() {

		Contact contact = new Contact("", "", "", "", "");
		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

		// Verify response
		response.then().statusCode(422);
		response.then().body("first_name[0]", Matchers.is("The first name field is required."));
		response.then().body("last_name[0]", Matchers.is("The last name field is required."));

	}

	@Owner("Sai Teja SG")
	@Test(dependsOnMethods ="createNewCompanyWithMandatoryFields_POST", groups = "nightly-build")
	public void createNewContactWithAllFields() {

		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, companySlug);
		contact.setAddress(Address);
		contact.setCity(city);
		contact.setAvatar(AvatarURL);
		contact.setDesignation(title);
		contact.setFacebook(fbLink);
		contact.setTwitter(twitterLink);
		contact.setLinkedin(githubLink);
		contact.setXing(xingLink);
		contact.setLocality(locality);

		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

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
	}
	
	@Owner("Smit Patel")
	@Test(dataProvider = "getInvalidEmailFieldsValue", groups = "nightly-build")
	public void createNewContactWithInvalidEmailId(String emailId,int statusCode,String ErrorMessage) {

		Contact contact = new Contact(ContactFirstName, ContactLastName, emailId, contactNumber, "");
		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);

		// Verify response
		response.then().statusCode(statusCode);
		response.then().body("email[0]", Matchers.is(ErrorMessage));

	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getInvalidContactNumberFieldsValue", groups = "nightly-build")
	public void createNewContactWithInvalidContactNumber(String contactNo,int statusCode,String ErrorMessage) {

		Contact contact = new Contact("", "b", ContactEmail, contactNo, "");
		contact.setFacebook("1");
		contact.setTwitter("1");
		contact.setLinkedin("1");
		contact.setXing("1");
		Response response = RestClient.doPost("JSON", baseURL, "contacts", ThreadManager.getAccountApiKey(), null, true, contact);
		// Verify response
		response.then().statusCode(statusCode);
		response.then().body("contact_number[0]", Matchers.is(ErrorMessage));
		response.then().body("first_name[0]", Matchers.is("The first name field is required."));
		response.then().body("facebook[0]", Matchers.is("The facebook format is invalid."));
		response.then().body("twitter[0]", Matchers.is("The twitter format is invalid."));
		response.then().body("linkedin[0]", Matchers.is("The linkedin format is invalid."));
		response.then().body("xing[0]", Matchers.is("The xing format is invalid."));
		
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createContactWithDateTimeCustomField() {
		
		Response customFieldResponse = allCrudFunctions.createCustomFields(albatrossURL, albatrossAuthToken, "date_time", 2);
		Assert.assertEquals(customFieldResponse.getStatusCode(), 200);
		
		String randomDate = companyFaker.getDateTimeCustomFieldValue();

		ContactCustomField contact = new ContactCustomField();
		contact.setFirst_name(ContactFirstName);
		contact.setLast_name(ContactLastName);
		contact.setEmail(ContactEmail);
		contact.setContact_number(contactNumber);

		List<ContactCustomField.CustomField> customFields = new ArrayList<>();
		ContactCustomField.CustomField customField = new ContactCustomField.CustomField();
		customField.setField_id(1);
		customField.setValue(randomDate);
		customFields.add(customField);

		contact.setCustom_fields(customFields);

		Response response = RestClient.doPost("JSON", baseURL, "contacts", apiAuthToken, null, true, contact);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("first_name", Matchers.is(ContactFirstName));
		response.then().body("last_name", Matchers.is(ContactLastName));
		response.then().body("custom_fields[0].entity_type", Matchers.is("contact"));
		response.then().body("custom_fields[0].field_type", Matchers.is("date_time"));
		response.then().body("custom_fields[0].value", Matchers.startsWith(randomDate.substring(0, 19)));
	}
	
	@DataProvider
	public Object[][] getInvalidContactNumberFieldsValue() {
		Object data[][] = { { "MorbivenenatisAccusamuspossimusexdoloreNullamdelenitimaecenasscelerisquesenectusreprehenderitportaperreprehenderitTempora", 422, "The contact number may not be greater than 100 characters." } };
		return data;
	}
	
	@DataProvider
	public Object[][] getInvalidEmailFieldsValue() {
		Object data[][] = { 
				
				{ "plainaddress", 422, "The email must be a valid email address." },
				{ "#@%^%#$@#$@#.com", 422, "The email must be a valid email address." },
				{ "@example.com", 422, "The email must be a valid email address." },
				{ "Joe Smith <email@example.com>", 422, "The email must be a valid email address." },
				{ "email.example.com", 422, "The email must be a valid email address." },
				{ "email@example@example.com", 422, "The email must be a valid email address." },
				{ ".email@example.com", 422, "The email must be a valid email address." },
				{ "email.@example.com", 422, "The email must be a valid email address." },
				{ "email..email@example.com", 422, "The email must be a valid email address." },
				//{ "あいうえお@example.com", 422, "The email must be a valid email address." },
				//{ "email@example.com (Joe Smith)", 422, "The email must be a valid email address." },
				//{ "email@example", 422, "The email must be a valid email address." },
				//{ "email@-example.com", 422, "The email must be a valid email address." },
				//{ "email@example.web", 422, "The email must be a valid email address." },
				//{ "email@111.222.333.44444", 422, "The email must be a valid email address." },
				{ "email@example..com", 422, "The email must be a valid email address." },
				{ "Abc..123@example.com", 422, "The email must be a valid email address." },
				{ "”(),:;<>[\\]@example.com", 422, "The email must be a valid email address." },
				//{ "just”not”right@example.com", 422, "The email must be a valid email address." },
				{ "this\\ is\"really\"not\\allowed@example.com", 422, "The email must be a valid email address." },

				{ "MorbivenenatisAccusamuspossimusexdoloreNullamdelenitimaecenasscelerisquesenectusreprehenderitportaperreprehenderitTempora@yopmail.com",
						422, "The email may not be greater than 100 characters." } };
		return data;

	}

}
