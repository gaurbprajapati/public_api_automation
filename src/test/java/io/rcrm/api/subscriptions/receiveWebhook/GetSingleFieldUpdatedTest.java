package io.rcrm.api.subscriptions.receiveWebhook;

import java.io.IOException;
import java.util.*;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.WebhookHelper;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class GetSingleFieldUpdatedTest extends TestBase {

	commanFunction function = new commanFunction();
	Map<String, String> pathParameters = new HashMap<String, String>();
	JsonPath responseFromWebhook, jsonPath;
	List<String> slugs = new ArrayList<String>();

	JavaFakerContact contactFaker = new JavaFakerContact();
	JavaFakerCompany companyFaker = new JavaFakerCompany();
	JavaFakerDeal dealFaker = new JavaFakerDeal();
	Random random = new Random();
	JavaFakerJob jobFaker=new JavaFakerJob();

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void updateFieldsOfCompany() {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
		
		WebhookHelper webhookHelper = new WebhookHelper();
		Subscription subscription = new Subscription("company.updated", webhookHelper.getTargetURL());
		RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");

		Company company = new Company();
		company.setCompany_name(companyFaker.getCompanyName());
		company.setAbout_company(companyFaker.getCompanyAbout());
		company.setAddress(companyFaker.getAddress());
		company.setCity(companyFaker.getCity());
		company.setContact_number(companyFaker.getContactNumber());
		company.setIndustry_id(companyFaker.getIndustry_id());
		company.setFacebook(companyFaker.getUrl());
		company.setTwitter(companyFaker.getUrl());
		company.setWebsite(companyFaker.getCompanyWebsite());

		pathParameters = new HashMap<String, String>();
		pathParameters.put("company", companySlug);

		Response response = RestClient.doPost1("JSON", baseURL, "companies/{company}", ThreadManager.getAccountApiKey(), null,
				pathParameters, false, company);

		jsonPath = response.jsonPath();
		responseFromWebhook = new JsonPath(webhookHelper.getData(companySlug));

		String[] expectedKeys = { "industry_id", "company_name", "city", "website", "facebook", "twitter", "address",
				"about_company" };
		String[] expectedLabels = { "Industry", "Company Name", "City", "Website", "Facebook Profile URL",
				"Twitter Profile URL", "Full Address", "About Company" };

		String actualKeys = responseFromWebhook.get("fields_changed.key").toString();
		String actualLabels = responseFromWebhook.get("fields_changed.label").toString();

		for (int i = 0; i < expectedKeys.length; i++) {
			Assert.assertTrue(actualKeys.contains(expectedKeys[i]),"Expected key not found : "+expectedKeys[i]);
			Assert.assertTrue(actualLabels.contains(expectedLabels[i]),"Expected label not found : "+expectedLabels[i]);
		}
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void updateFieldsOfContact() {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
		
		WebhookHelper webhookHelper = new WebhookHelper();
		Subscription subscription = new Subscription("contact.updated", webhookHelper.getTargetURL());
		RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
		String companySlug1 = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
				.get("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");

		Contact contact = new Contact();
		contact.setFirst_name(contactFaker.getFirstName());
		contact.setLast_name(contactFaker.getLastName());
		contact.setCompany_slug(companySlug1);
		contact.setContact_number(contactFaker.getContactNumber());
		contact.setEmail(contactFaker.getEmailID());
		contact.setDesignation(contactFaker.getDesignation());
		contact.setAddress(contactFaker.getAddress());
		contact.setCity(contactFaker.getCity());
		contact.setLocality(contactFaker.getLocality());
		contact.setFacebook(contactFaker.getUrl());
		contact.setLinkedin(contactFaker.getUrl());
		contact.setTwitter(contactFaker.getUrl());
		contact.setXing(contactFaker.getUrl());

		pathParameters = new HashMap<String, String>();
		pathParameters.put("contact", contactSlug);

		Response response = RestClient.doPost1("JSON", baseURL, "contacts/{contact}", ThreadManager.getAccountApiKey(), null,
				pathParameters, false, contact);

		jsonPath = response.jsonPath();
		responseFromWebhook = new JsonPath(webhookHelper.getData(contactSlug));

		String[] expectedKeys = { "first_name", "last_name", "email", "contact_number", "city",
				"address", "locality", "designation", "facebook", "twitter", "linkedin" };
		String[] expectedLabels = { "Company", "First Name", "Last Name", "Email", "Last Name", "City", "Full Address",
				"Locality", "Title", "Facebook Profile URL", "Twitter Profile URL", "Linkedin Profile URL" };

		String actualKeys = responseFromWebhook.get("fields_changed.key").toString();
		String actualLabels = responseFromWebhook.get("fields_changed.label").toString();

		for (int i = 0; i < expectedKeys.length; i++) {
			Assert.assertTrue(actualKeys.contains(expectedKeys[i]),"Expected key not found : "+expectedKeys[i]);
			Assert.assertTrue(actualLabels.contains(expectedLabels[i]),"Expected label not found : "+expectedLabels[i]);
		}
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void updateFieldsOfDeal() {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
		
		WebhookHelper webhookHelper = new WebhookHelper();
		Subscription subscription = new Subscription("deal.updated", webhookHelper.getTargetURL());
		RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

		String dealSlug = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");

		Deal deal = new Deal();
		deal.setName(dealFaker.getDealName());
		deal.setDeal_value(dealFaker.getDealValue());
		deal.setClose_date(dealFaker.getDealDate());
		deal.setDeal_stage("2");
		deal.setDeal_type("2");

		pathParameters = new HashMap<String, String>();
		pathParameters.put("deal", dealSlug);

		Response response = RestClient.doPost1("JSON", baseURL, "deals/{deal}", ThreadManager.getAccountApiKey(), null, pathParameters,
				true, deal);
		
		jsonPath = response.jsonPath();
		responseFromWebhook = new JsonPath(webhookHelper.getData(dealSlug));

		String[] expectedKeys = { "name", "deal_stage", "deal_value", "dealpercentagevalue", "close_date",
				"deal_type" };
		String[] expectedLabels = { "Name", "Stage", "Value", "dealpercentagevalue", "Close Date", "Deal Type" };

		String actualKeys = responseFromWebhook.get("fields_changed.key").toString();
		String actualLabels = responseFromWebhook.get("fields_changed.label").toString();

		for (int i = 0; i < expectedKeys.length; i++) {
			Assert.assertTrue(actualKeys.contains(expectedKeys[i]),"Expected key not found : "+expectedKeys[i]);
			Assert.assertTrue(actualLabels.contains(expectedLabels[i]),"Expected label not found : "+expectedLabels[i]);
		}
	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void updateFieldsOfJob(){
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
		WebhookHelper webhookHelper = new WebhookHelper();
		Subscription subscription = new Subscription("job.updated", webhookHelper.getTargetURL());
		RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, true, subscription);
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
				.get("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath()
				.get("slug");
		String contactSlug2=function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath()
				.get("slug");
		String jobSlug = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath()
				.get("slug");

		Response usersResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		Assert.assertEquals(usersResponse.getStatusCode(), 200, "Users not fetched successfully");

		JsonPath user = usersResponse.jsonPath();
		int accountOwnerid = user.get("[0].id");
		int adminId = user.get("[1].id");
		int resTeamMember = user.get("[2].id");
		int teamMember = user.get("[3].id");

		Job job = new Job();
		String JobName = jobFaker.getJobName() + " Edited";
		job.setName(JobName);
		job.setCompany_slug(companySlug);
		job.setContact_slug(contactSlug);
		job.setNumber_of_openings(random.nextInt(100));
		job.setJob_description_text("Sample JD Edited");
		job.setEnable_job_application_form(1);
		job.setSecondary_contact_slug(contactSlug2);
		job.setJob_skill("Java,CPP,Python,Javascript");
		job.setLocality(jobFaker.getJobLocality());
		job.setMaximum_experience(random.nextInt(30));
		job.setMinimum_experience(random.nextInt(10));
		job.setCity(jobFaker.getJobCity());
		job.setUpdated_by(adminId);
		job.setOwner_id(adminId);
		job.setCurrency_id(2);
		job.setCollaborator_user_ids(resTeamMember+","+teamMember);

		pathParameters = new HashMap<String, String>();
		pathParameters.put("job", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, "jobs/{job}", ThreadManager.getAccountApiKey(), null, pathParameters, true,
				job);
		Assert.assertEquals(response.getStatusCode(), 200, "Job not updated successfully");

		JsonPath updateResponse=response.jsonPath();
		responseFromWebhook = new JsonPath(webhookHelper.getData(jobSlug));

		String[] expectedKeys={"collaborator_user_ids", "name", "minimum_experience", "maximum_experience", "number_of_openings", "currency_id", "job_description_text", "job_description_file", "owner_id", "updated_by", "locality", "city", "job_skill"};
		String[] expectedLabels={"Collaborator User IDs", "Job Title", "Minimum Experience (Years)", "Maximum Experience (Years)", "Number Of Openings", "Currency", "Job Description Text", "Job Description File", "Owner", "Updated By", "Locality", "City", "Skills"};


		String[] fieldNames={"collaborator_users", "name", "minimum_experience", "maximum_experience", "number_of_openings", "currency_id", "job_description_text", "job_description_file", "owner", "updated_by", "locality", "city", "job_skill"};
		for(int i=0;i<fieldNames.length;i++){
			Assert.assertEquals(updateResponse.getString(fieldNames[i]),responseFromWebhook.getString(fieldNames[i]),"Response from Webhook is not matching with the response from API");
		}

		String actualKeys = responseFromWebhook.get("fields_changed.key").toString();
		String actualLabels = responseFromWebhook.get("fields_changed.label").toString();

		for (int i = 0; i < expectedKeys.length; i++) {
			Assert.assertTrue(actualKeys.contains(expectedKeys[i]),"Expected key not found : "+expectedKeys[i]);
			Assert.assertTrue(actualLabels.contains(expectedLabels[i]),"Expected label not found : "+expectedLabels[i]);
		}

	}
}
