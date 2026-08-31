package io.recruitcrm.albatross.contact;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.Contact.ContactPojo;
import io.rcrm.api.pojo.albatross.Contact.ContactPojo.SelectedCompany;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.testng.Assert;
import org.testng.annotations.*;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.recruitcrm.albatross.company.Company;
import io.recruitcrm.albatross.company.CompanyJson;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateNewContactTest extends TestBase {

	JavaFakerCompany javaFakerCompany;
	JavaFakerContact javaFakerContact;
	String companyName, companyWebsite, aboutCompany, companyAddress, address, companyCity;
	String contactFirstName, contactLastName, contactDesignation, contactEmailId, contactNumber, contactTwitter,
			contactLinkedIn, contactFacebook, contactLocality, contactAddress;
	commanFunction function;
	int industry_id;
	String albatrossAuthToken;
	String apiAuthToken;
	int ownerAccountID;

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
		contactAddress = javaFakerContact.getAddress();
		companyWebsite = javaFakerCompany.getUrl();
		companyCity = javaFakerCompany.getCity();
		address = javaFakerCompany.getAddress();
		industry_id = javaFakerCompany.getIndustry_id();
		function = new commanFunction();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		ownerAccountID = ThreadManager.getAccount().getAccountId();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getContactDuplicateLinkedinTestData", groups = "nightly-build")
	public synchronized void createContactAndVerifyDuplicateMergeByLinkedinUrl_Test(String contactSlug,
			String companySlug, String linkedinUrl, int companyId, String companyName) {

		ContactPojo.Contact contact = new ContactPojo.Contact(contactSlug, contactFirstName, contactLastName,
				contactDesignation, contactNumber, contactAddress, linkedinUrl);
		ContactPojo.SelectedCompany selectedCompany = new SelectedCompany(String.valueOf(companyId), companyName,
				companyId, companySlug);
		List<SelectedCompany> selectedcompanies = new ArrayList<>();
		selectedcompanies.add(selectedCompany);
		ContactPojo contactPojo = new ContactPojo(contact, selectedcompanies);

		Response response = RestClient.doPost("JSON", albatrossURL, "contacts", albatrossAuthToken, null, true,
				contactPojo);

		JsonPath jsonPath = response.jsonPath();
		response.then().statusCode(200);
		Assert.assertEquals(jsonPath.getString("message"), "Duplicate Contact Updated Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("data.contact.slug"), contactSlug);
		Assert.assertEquals(jsonPath.getString("data.contact.companyslug"), companySlug);
	}


    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void verifyLinkedInUrlReuseDoesNotMergeDuplicateContact_Test() throws InterruptedException, ExecutionException {
        function.enableMergeDuplicate(ownerAccountID, albatrossURL, albatrossAuthToken, "contacts");

        // Create a company
        Company company = new Company(companyName, aboutCompany, companyCity, industry_id, companyWebsite, address);
        CompanyJson companyJson = new CompanyJson();
        companyJson.setAddress_changed(true);
        companyJson.setCompany(company);

		companyJson.setContact(new Contact(contactFirstName, contactLastName,javaFakerContact.getEmailID(), javaFakerContact.getCity(), contactNumber, javaFakerContact.getStage()));

		CompletableFuture<JsonPath> companyFuture = CompletableFuture.supplyAsync(() ->
            RestClient.doPost("JSON", albatrossURL, "companies", albatrossAuthToken, null, true, companyJson).jsonPath()
        );

        JsonPath companyJsonPath = companyFuture.get();
        String companySlug = companyJsonPath.get("data.company.slug");
        int companyId = companyJsonPath.get("data.company.id");
        String companyName = companyJsonPath.get("data.company.companyname");

        // Create a contact
        CompletableFuture<JsonPath> contactFuture = CompletableFuture.supplyAsync(() -> 
            function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath()
        );

        JsonPath jsonContact = contactFuture.get();
        String contactSlug = jsonContact.get("slug");
        String contactName = jsonContact.getString("first_name").split(" ")[0];
        String contactLinkedinUrl = "http://www.linkedin.com/in/" + contactName;

        // Edit the contact and make its LinkedIn URL empty
        ContactPojo.Contact contact = new ContactPojo.Contact(contactSlug, contactFirstName, contactLastName, contactDesignation, contactNumber, contactAddress, "");
        ContactPojo.SelectedCompany selectedCompany = new SelectedCompany(String.valueOf(companyId), companyName, companyId, companySlug);
        List<SelectedCompany> selectedCompanies = new ArrayList<>();
        selectedCompanies.add(selectedCompany);
        ContactPojo contactPojo = new ContactPojo(contact, selectedCompanies);

        CompletableFuture<Response> editContactFuture = CompletableFuture.supplyAsync(() -> 
            RestClient.doPost("JSON", albatrossURL, "contacts/" + contactSlug, albatrossAuthToken, null, true, contactPojo)
        );

        Response editContactResponse = editContactFuture.get();
        editContactResponse.then().statusCode(200);

        // Create a new contact with the same LinkedIn URL
        ContactPojo.Contact contact2 = new ContactPojo.Contact(contactSlug, contactFirstName, contactLastName, contactDesignation, contactNumber, contactAddress, contactLinkedinUrl);
        ContactPojo.SelectedCompany selectedCompany2 = new SelectedCompany(String.valueOf(companyId), companyName, companyId, companySlug);
        List<SelectedCompany> selectedCompanies2 = new ArrayList<>();
        selectedCompanies2.add(selectedCompany2);
        ContactPojo contactPojo2 = new ContactPojo(contact2, selectedCompanies2);

        CompletableFuture<Response> newContactFuture = CompletableFuture.supplyAsync(() -> 
            RestClient.doPost("JSON", albatrossURL, "contacts", albatrossAuthToken, null, true, contactPojo2)
        );

        Response newContactResponse = newContactFuture.get();
        JsonPath jsonPath = newContactResponse.jsonPath();
        newContactResponse.then().statusCode(200);

        Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
        Assert.assertEquals(jsonPath.getString("message"), "Contact Added");
        Assert.assertEquals(jsonPath.getString("data.contact.firstname"), contact2.getFirstname());
        Assert.assertEquals(jsonPath.getString("data.contact.profilelinkedin"), contact2.getProfilelinkedin());
    }

	@DataProvider(parallel = true)
	public Object[][] getContactDuplicateLinkedinTestData() {
		function.enableMergeDuplicate(ownerAccountID, albatrossURL, albatrossAuthToken, "contacts");
		Company company = new Company(companyName, aboutCompany, companyCity, industry_id, companyWebsite, address);
		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);
		Contact contact = new Contact("","","","","","");
		companyJson.setContact(contact);
		JsonPath companyJsonPath = RestClient.doPost("JSON", albatrossURL, "companies", albatrossAuthToken, null, true, companyJson).jsonPath();
		String companySlug = companyJsonPath.get("data.company.slug");
		String companyName = companyJsonPath.get("data.company.companyname");
		int companyId = companyJsonPath.getInt("data.company.id");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");
		String contactName = jsonContact.getString("first_name").split(" ")[0];
		return new Object[][] {
				{ contactSlug, companySlug, "http://www.linkedin.com/in/" + contactName, companyId, companyName },
				{ contactSlug, companySlug, "www.linkedin.com/in/" + contactName, companyId, companyName },
				{ contactSlug, companySlug, "linkedin.com/in/" + contactName, companyId, companyName } };
	}

}
