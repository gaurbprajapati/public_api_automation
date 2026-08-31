package io.rcrm.api.commanfunctions.albatross;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.S3Uploader;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerXmlFeed;
import io.rcrm.api.pojo.albatross.*;
import io.rcrm.api.pojo.albatross.Contact.ContactPojo;
import io.rcrm.api.pojo.albatross.xmlfeed.ListXmlFeeds;
import io.rcrm.api.pojo.albatross.xmlfeed.SaveCustomXmlFeed;
import io.rcrm.api.pojo.candidateService.AddToHotlistRequest;
import io.rcrm.api.pojo.candidateService.HotLists;
import io.rcrm.api.pojo.comm.AssignNumber;
import io.rcrm.api.pojo.nyma.*;
import org.hamcrest.Matchers;

import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AllCrudFunctions {

	public AllCrudFunctions() {
		super();
		// TODO Auto-generated constructor stub
	}

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerCompany faker = new JavaFakerCompany();
	JavaFakerContact contactFaker = new JavaFakerContact();
	JavaFakerXmlFeed xmlFeedFaker = new JavaFakerXmlFeed();

	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	String CandidateEmail = "rcrmtest0@gmail.com";
	String CandidateNumber = fakerCandidate.getContactNumber();
	int genderId = 1;
	String fbLink = fakerCandidate.getUrl();
	String twitterLink = fakerCandidate.getUrl();
	String githubLink = fakerCandidate.getUrl();
	String linkedinLink = fakerCandidate.getUrl();
	String xingLink = fakerCandidate.getUrl();
	String city = fakerCandidate.getCity();
	String locality = fakerCandidate.getLocality();
	String Address = fakerCandidate.getCandidateAddress();

	String companyName = faker.getCompanyName();
	String companyWebsite = faker.getUrl();
	String contactNumber = "13456789087654";
	String companyCity = faker.getCity();
	String address = faker.getAddress();
	int industry_id = faker.getIndustry_id();

	String ContactFirstName = contactFaker.getFirstName();
	String ContactLastName = contactFaker.getLastName();
	String ContactEmail = "rcrmtest0@gmail.com";
	String contactNumbers = contactFaker.getContactNumber();
	String contactAvatarURL = fakerCandidate.getCandidateAvatarUrl();
	String contactFbLink = contactFaker.getUrl();
	String contactTwLink = contactFaker.getUrl();
	String contactLinkedinLink = contactFaker.getUrl();
	String contactXingLink = contactFaker.getUrl();

	public Response getUsers(String albatross_url, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("page", "1");
		pathParamters.put("page_size", "10");

		String basePath = "users/get";
		Response response = RestClient.doPost1("JSON", albatross_url, basePath, authTokenMap, pathParamters, null, true,
				null);


		response.then().statusCode(200);

		return response;
	}

	public Response createCandidate(String albatross_url, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Candidate candidate = new Candidate(false, "", CandidateFirstName, CandidateLastName, CandidateEmail, genderId,
				CandidateNumber, Address, city, "candidate summary", locality, fbLink, twitterLink, linkedinLink,
				githubLink, xingLink);
		createCandidatePage createCandidatePage = new createCandidatePage();
		createCandidatePage.setCandidate(candidate);
		String basePath = "candidates";
		Response response = RestClient.doPost("JSON", albatross_url, basePath, authTokenMap, null, true,
				createCandidatePage);


		response.then().statusCode(200);

		return response;
	}

	public Response createCompanyContact(String albatross_url, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		ContactPage contact = new ContactPage("", "", ContactFirstName, ContactLastName, "", ContactEmail,
				contactNumber, city, Address, contactFbLink, contactTwLink, linkedinLink);
		CompanyPage company = new CompanyPage("", companyName, "", industry_id, companyWebsite, companyCity,
				Address);
		CreateCompanyAndContactPage createCompanyAndContactPage = new CreateCompanyAndContactPage();
		createCompanyAndContactPage.setCompany(company);
		createCompanyAndContactPage.setAddress_changed(false);
		createCompanyAndContactPage.setContact(contact);

		String basePath = "companies";
		Response response = RestClient.doPost("JSON", albatross_url, basePath, authTokenMap, null, true,
				createCompanyAndContactPage);


		response.then().statusCode(200);

		return response;
	}

	public Response createContact(String albatross_url, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);


		ContactPojo.Contact contact = new ContactPojo.Contact();
		contact.setSlug("");
		contact.setFirstname(contactFaker.getFirstName());
		contact.setDesignation(contactFaker.getDesignation());
		contact.setContactnumber(contactFaker.getContactNumber());
		contact.setAddress(contactFaker.getAddress());
		contact.setCity(contactFaker.getCity());
		contact.setLocality(contactFaker.getLocality());
		contact.setProfilefacebook(contactFaker.getContactFacebookURL());
		contact.setProfiletwitter(contactFaker.getContactTwitterURL());
		contact.setProfilelinkedin(contactFaker.getContactLinkedinURL());
		contact.setProfilexing(contactFaker.getContactXingURL());
		contact.setCompanyid(0);
		contact.setOwnerid(0);
		contact.setFromQuickview(false);

		ContactPojo contactPojo = new ContactPojo(contact, new ArrayList<>());
		contactPojo.setAddress_changed(false);
		contactPojo.setFilesInfo(new HashMap<>());

		String basePath = "contacts";
		Response response = RestClient.doPost("JSON", albatross_url, basePath, authTokenMap, null, true, contactPojo);

		response.then().statusCode(200);

		return response;
	}

	public Response createCandidateQuestion(String albatross_url, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		CandQuesAlbatrossPage candQuesAlbatrossPage = new CandQuesAlbatrossPage();
		candQuesAlbatrossPage.setQuestion("What is your Name?");
		String basePath = "candidate-questions";
		Response response = RestClient.doPost("JSON", albatross_url, basePath, authTokenMap, null, true,
				candQuesAlbatrossPage);


		response.then().statusCode(200);

		return response;

	}

	public Response getJobResponse(String albatross_url, Object authToken, String jobSlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("jobSlug", jobSlug);
		String basePath = "jobs/{jobSlug}/get";

		Response response = RestClient.doPost1("JSON", albatross_url, basePath, authTokenMap, null, pathParamters, true,
				null);


		response.then().statusCode(200);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		return response;
	}

	public Response getCandidateResponse(String albatross_url, Object authToken, String candSlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candSlug", candSlug);
		String basePath = "candidates/{candSlug}/get";

		Response response = RestClient.doPost1("JSON", albatross_url, basePath, authTokenMap, null, pathParamters, true,
				null);


		response.then().statusCode(200);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		return response;
	}

	public Response getContactResponse(String albatross_url, Object authToken, String contactSlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contactSlug", contactSlug);
		String basePath = "contacts/{contactSlug}";

		Response response = RestClient.doGet("JSON", albatross_url, basePath, authTokenMap, null, pathParamters, true);


		response.then().statusCode(200);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		return response;
	}

	public Response getCompanyResponse(String albatross_url, Object authToken, String companySlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("companySlug", companySlug);
		String basePath = "companies/{companySlug}";

		Response response = RestClient.doGet("JSON", albatross_url, basePath, authTokenMap, null, pathParamters, true);

		response.then().statusCode(200);
		return response;
	}

	public Response getDealResponse(String albatross_url, Object authToken, String dealSlug) {
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("dealSlug", dealSlug);
		String basePath = "deals/{dealSlug}";

		Response response = RestClient.doGet("JSON", albatross_url, basePath, authToken, null, pathParameters, true);

		assert response != null : "Get Deal Response is null";
		response.then().statusCode(200);
		return response;
	}

	public Response getCallLogs(String albatross_url, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> queryParamters = new HashMap<String, String>();
		queryParamters.put("sort_by", "updatedon");
		queryParamters.put("sortOrder", "desc");
		queryParamters.put("page", "1");
		queryParamters.put("page_size", "25");
		queryParamters.put("fromNumber", null);
		queryParamters.put("toNumber", null);

		String basePath = "call-logs/get";

		Response response = RestClient.doPost1("JSON", albatross_url, basePath, authTokenMap, queryParamters, null, true,null);

        assert response != null;
        response.then().statusCode(200);

		return response;
	}

	public Response createTeam(String albatross_url, Object authToken, String label,
			List<String> userIds) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Team team = new Team();
		team.setLabel(label);
		team.setUserids(userIds);

		CreateTeam createTeam = new CreateTeam();
		createTeam.setTeam(team);

		String basePath = "teams";

		Response response = RestClient.doPost("JSON", albatross_url, basePath, authTokenMap, null, true, createTeam);


		response.then().statusCode(200);

		return response;
	}

	public Response deleteTeam(String albatross_url, Object authToken, int id) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		GlobalDelete globalDelete = new GlobalDelete();

		globalDelete.setIdsToDelete(id);
		globalDelete.setTableFlag("teams");
		globalDelete.setFieldKey("id");

		String basePath = "global/delete-record";

		Response response = RestClient.doPost("JSON", albatross_url, basePath, authTokenMap, null, true, globalDelete);


		response.then().statusCode(200);

		return response;
	}

	public Response createJobAssociatedFields(String albatross_url, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		CustumField custumField = new CustumField();
		custumField.setExtrafieldname("TEXT");
		custumField.setExtrafieldtype("text");
		custumField.setEntitytypeid(14);
		custumField.setColumnid(1);
		custumField.setDefaultvalue(null);

		CustomFieldWrapper customFieldWrapper = new CustomFieldWrapper();
		customFieldWrapper.setCustumField(custumField);
		String basePath = "custom-fields";
		Response response = RestClient.doPost("JSON", albatross_url, basePath, authTokenMap, null, true,
				customFieldWrapper);


		response.then().statusCode(200);

		return response;

	}

	private Map<String, String> getAuthTokenMap(Object authToken) {
		Map<String, String> authTokenMap = new HashMap<String, String>();
		if(authToken instanceof Map){
			authTokenMap = (Map<String, String>) authToken;
		}else {
			String apiKey = (String) authToken;
			authTokenMap = new HashMap<String, String>();
			authTokenMap.put("Authorization", "Bearer " + apiKey);
		}
		return authTokenMap;
	}

	public Response createCustomMeeting(String albatross_url, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		MeetingTypePage meetingTypePage = new MeetingTypePage();
		meetingTypePage.setLabel("Meeting Type " + RandomStringUtils.randomAlphabetic(4));
		meetingTypePage.setDefault(0);
		ArrayList<Object> meetingTypes = new ArrayList<>();
		meetingTypes.add(meetingTypePage);

		MeetingTypeCustomizationPage MeetingTypeCustomizationPage = new MeetingTypeCustomizationPage();
		MeetingTypeCustomizationPage.setCustomizedMeetingTypes(meetingTypes);

		String basePath = "meetings/customize-meeting-types";
		Response response = RestClient.doPost("JSON", albatross_url, basePath, authTokenMap, null, true,
				MeetingTypeCustomizationPage);
		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected status code to be 200, but received: " + response.getStatusCode());

		response.then().body("status", Matchers.containsString("success"));

		return response;
	}

	public Response getTaskTypeId(String albatross_url, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		TaskTypePage taskTypePage = new TaskTypePage();
		taskTypePage.setLabel("Task Type " + RandomStringUtils.randomAlphabetic(4));
		taskTypePage.setDefault(0);
		ArrayList<Object> taskTypes = new ArrayList<>();
		taskTypes.add(taskTypePage);

		TaskTypeCustomizationPage taskTypeCustomizationPage = new TaskTypeCustomizationPage();
		taskTypeCustomizationPage.setCustomizedTaskTypes(taskTypes);

		Response response = RestClient.doPost("JSON", albatross_url, "task-types", authTokenMap, null, true, taskTypeCustomizationPage);
		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected status code to be 200, but received: " + response.getStatusCode());

		response.then().body("status", Matchers.containsString("success"));

		return response;
	}

	public Response getContactStages(String albatross_url, Object authToken) {
		String basePath = "contacts/getStages";
		Response response = RestClient.doPost("JSON", albatross_url, basePath, authToken, null, true, null);
		response.then().statusCode(200);
		return response;
	}

	public Response getDealPipelineStages(String albatross_url, Object authToken) {
		String basePath = "deals/get-deal-pipeline-stages";
		Response response = RestClient.doPost("JSON", albatross_url, basePath, authToken, null, true, null);
		response.then().statusCode(200);
		return response;
	}

	public Response createCustomXmlFeed(String jobBoardServiceUrl, Object authToken) {
		SaveCustomXmlFeed saveCustomXmlFeed = new SaveCustomXmlFeed(xmlFeedFaker.getXmlFeedTitle(), xmlFeedFaker.getXmlHeader(), xmlFeedFaker.getXmlBody(), xmlFeedFaker.getDecodeValue(), xmlFeedFaker.getPreselectValue(), xmlFeedFaker.getJobLastUpdatedOnLimit());
		String basePath = "custom-xml/save";
		Response response = RestClient.doPost("JSON", jobBoardServiceUrl, basePath, authToken, null, true, saveCustomXmlFeed);
		response.then().statusCode(200);
		return response;
	}

	public Response getXmlFeedsList(String jobBoardServiceURL, Object authToken) {
		ListXmlFeeds listXmlFeeds = new ListXmlFeeds("updated_on", "asc");
		String basePath = "custom-xml/list";
		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, authToken, null, true, listXmlFeeds);
		response.then().statusCode(200);
		return response;
	}

	private Response postCustomFieldsWithRetry(String albatrossURL, Object authToken, CustomFieldWrapper customFieldWrapper) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		int maxRetries = 3;
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", authTokenMap, null, true,
					customFieldWrapper);
			if (response.getStatusCode() != 401) {
				response.then().statusCode(200);
				return response;
			}
			if (attempt < maxRetries) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Retry interrupted", e);
				}
			}
		}
		throw new RuntimeException("postCustomFields failed with 401 after " + maxRetries + " retries");
	}

	public Response createCustomFields(String albatrossURL, String authToken, String fieldType, int entityId) {
		CustumField custumField = new CustumField();
		custumField.setExtrafieldname(fieldType + " " +faker.getRandomId());
		custumField.setExtrafieldtype(fieldType);
		custumField.setEntitytypeid(entityId);
		custumField.setColumnid(1);
		custumField.setDefaultvalue(null);

		CustomFieldWrapper customFieldWrapper = new CustomFieldWrapper();
		customFieldWrapper.setCustumField(custumField);
		return postCustomFieldsWithRetry(albatrossURL, authToken, customFieldWrapper);
	}

	public Response createCustomFieldsWithUserDefinedNames(String albatrossURL, String authToken, String fieldType, int entityId, String fieldName, int columnId) {
		CustumField custumField = new CustumField();
		custumField.setExtrafieldname(fieldName);
		custumField.setExtrafieldtype(fieldType);
		custumField.setEntitytypeid(entityId);
		custumField.setColumnid(columnId);
		custumField.setDefaultvalue(null);

        // Add default options for dropdown and multiselect fields
        if ("dropdown".equals(fieldType) || "multiselect".equals(fieldType)) {
            List<DefaultOptionsValue> defaultOptionsValue = new ArrayList<>();

            DefaultOptionsValue option1 = new DefaultOptionsValue();
            option1.setLabel("Option A");
            option1.setSequence_no(1);
            defaultOptionsValue.add(option1);

            DefaultOptionsValue option2 = new DefaultOptionsValue();
            option2.setLabel("Option AB");
            option2.setSequence_no(2);
            defaultOptionsValue.add(option2);

            DefaultOptionsValue option3 = new DefaultOptionsValue();
            option3.setLabel("Option ABC");
            option3.setSequence_no(3);
            defaultOptionsValue.add(option3);

            custumField.setDefaultoptionsvalue(defaultOptionsValue);
        }

		CustomFieldWrapper customFieldWrapper = new CustomFieldWrapper();
		customFieldWrapper.setCustumField(custumField);
		return postCustomFieldsWithRetry(albatrossURL, authToken, customFieldWrapper);
	}

	public Response getPurchasedPhoneNumberId(String commURL, String authToken) {
		String basePath = "phone-numbers/purchased";

		Map<String, String> queryParamters = new HashMap<String, String>();
		queryParamters.put("page","1");
		queryParamters.put("page_size","10");

		Response response = RestClient.doGet("JSON", commURL, basePath, authToken, queryParamters, null, true);

		assert response != null;
		Assert.assertEquals(response.getStatusCode(), 200);

		return response;
	}

	public Response assignPhoneNumber(String commURL, String authToken, int userId, int phoneNumberId) {

		String basePath = "phone-numbers/assign";

		AssignNumber assignPhoneNumber = new AssignNumber();
		assignPhoneNumber.setUser_id(String.valueOf(userId));
		assignPhoneNumber.setPhone_number_id(String.valueOf(phoneNumberId));
		assignPhoneNumber.setNumber_title("Assign Phone Number");
		assignPhoneNumber.setVoice_reply("The person you are calling is not available");
		assignPhoneNumber.setMasked_number("+1234567890");

		Response response = RestClient.doPost("JSON", commURL, basePath, authToken, null, true, assignPhoneNumber);

		assert response != null;
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("message_type"), "is-success");
		Assert.assertEquals(response.jsonPath().get("message"), "Phone number assigned!");

        return response;
	}

	public Response createHotlistsForCandidates(String baseURL, String authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String basePath = "hotlists";
		HotLists hotlist = new HotLists();
		String hotlistName = "Candidate Hotlist " + RandomStringUtils.randomAlphabetic(4);
		hotlist.setName(hotlistName);
		hotlist.setRelated_to_type("candidate");
		hotlist.setShared(1);

		Response response = RestClient.doPost("JSON", baseURL, basePath, authTokenMap, null, true, hotlist);

		Assert.assertEquals(response.getStatusCode(), 200);
		return response;
	}

	public Response createCompanyHotlist(String albatrossURL, String authToken, String hotlistName, List<Integer> companyIds) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String basePath = "hotlists";

		AddToHotlistRequest requestBody = new AddToHotlistRequest();
		requestBody.setEntity_name("companies");
		requestBody.setSelectedrows(companyIds.stream().mapToInt(Integer::intValue).toArray());
		requestBody.setShared(true);
		requestBody.setName(new String[]{hotlistName});
		requestBody.setUpdateUserObj(false);
		requestBody.setFrom_add_to_hotlist_modal(true);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, requestBody);

		Assert.assertEquals(response.getStatusCode(), 200);
		return response;
	}

	public Response addCandidateToHotList(String albatrossURL, String authToken, int candidateId, int shared, String hotlistName) {
		String basePath = "hotlists";
		Map<String, String> headers = new HashMap<>();
		headers.put("authorization", "Bearer " + authToken);
		AddToHotlistRequest requestBody = new AddToHotlistRequest();
		requestBody.setEntity_name("candidates");
		requestBody.setSelectedrows(new int[]{candidateId});
		requestBody.setShared(shared == 1);
		requestBody.setName(new String[]{hotlistName});
		requestBody.setUpdateUserObj(false);
		requestBody.setFrom_add_to_hotlist_modal(true);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, headers, null, true, requestBody);

		Assert.assertEquals(response.getStatusCode(), 200);
		return response;
	}

	public Response createCandidateHotlist(String albatrossURL, String authToken, String hotlistName, List<Integer> candidateIds) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String basePath = "hotlists";

		AddToHotlistRequest requestBody = new AddToHotlistRequest();
		requestBody.setEntity_name("candidates");
		requestBody.setSelectedrows(candidateIds.stream().mapToInt(Integer::intValue).toArray());
		requestBody.setShared(true);
		requestBody.setName(new String[]{hotlistName});
		requestBody.setUpdateUserObj(false);
		requestBody.setFrom_add_to_hotlist_modal(true);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, requestBody);

		Assert.assertEquals(response.getStatusCode(), 200);
		return response;
	}
	
	public Response updateCustomField(String entityType,String albatrossURL, int entityId, String albatrossAuthToken, String key, String value) {
		List<Integer> entityIds = Arrays.asList(entityId);
		UpdateFields updateFields = new UpdateFields();
		updateFields.setKey(key);
		updateFields.setValue(value);
		updateFields.setTableFlag(entityType);
		updateFields.setId(entityIds);
		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null, true, updateFields);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalTo("is-success"));
		response.then().body("message", Matchers.equalTo("Field Updated Successfully"));
		return response;
	}

	public Response updateContactEmailField(String entityType,String albatrossURL, int entityId, String albatrossAuthToken, String key, String value) {
		JSONObject updateFields = new JSONObject();
		updateFields.put("key", key);
		updateFields.put("value", value);
		updateFields.put("tableFlag", entityType);
		updateFields.put("id", entityId);
		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null, true, updateFields);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalTo("is-success"));
		response.then().body("message", Matchers.equalTo("Field Updated Successfully"));
		return response;
	}

    public Response updateEmailOptOut(String entityType,String albatrossURL, int entityId, String albatrossAuthToken, String key, String value) {
        List<Integer> entityIds = Arrays.asList(entityId);
        UpdateFields updateFields = new UpdateFields();
        updateFields.setKey(key);
        updateFields.setValue(value);
        updateFields.setTableFlag(entityType);
        updateFields.setId(entityIds);
        Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null, true, updateFields);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.equalTo("is-success"));
        response.then().body("message", Matchers.equalTo("Candidates opted out successfully"));
        return response;
    }

	public Response importCsv(String baseUrl, String authToken, String entity, String filePath, String userId, Boolean overrideStatus, Boolean addToHotlist, String hotlistName, Boolean mergeDuplicates, String mergeUsing) {
		Path path = Paths.get(filePath);
		File file = new File(path.toString());
		String fileName = path.getFileName().toString();

		Map<String, String> authTokenMap = new HashMap<>();
		authTokenMap.put("Authorization", "Bearer " + authToken);

		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("requestType", "put");
		paramsMap.put("key", "Users/Import/" + userId + "/import");
		paramsMap.put("acl", "private");
		paramsMap.put("next_key", "no");
		paramsMap.put("fileName", fileName);

		// Get pre-signed URL first
		Response getPresignedUrlResponse = RestClient.doGet("JSON", baseUrl, "get-presigned-url", authTokenMap, paramsMap, null, true);
		Assert.assertEquals(getPresignedUrlResponse.getStatusCode(), 200, "Status Code must be 200!");

		String s3_bucket_url = getPresignedUrlResponse.jsonPath().getString("data.preSignedUrl");
		String s3_key = getPresignedUrlResponse.jsonPath().getString("data.key");

		// Put it in the bucket
		try {
			S3Uploader.uploadFileToS3(s3_bucket_url, file.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		JSONObject jsonObject = null;

		// Toggle between entities
		switch (entity) {
			case "candidate":
				try {
					String content = new String(Files.readAllBytes(Paths.get("src/test/resources/candidate_import_payload.json")));
					jsonObject = new JSONObject(content);
				} catch (IOException e) {
					e.printStackTrace();
				}
				jsonObject.put("entity", "candidates");
				break;
			case "contact":
				try {
					String content = new String(Files.readAllBytes(Paths.get("src/test/resources/contact_import_payload.json")));
					jsonObject = new JSONObject(content);
				} catch (IOException e) {
					e.printStackTrace();
				}
				jsonObject.put("entity", "companies");
				break;
			case "company":
				try {
					String content = new String(Files.readAllBytes(Paths.get("src/test/resources/company_import_payload.json")));
					jsonObject = new JSONObject(content);
				} catch (IOException e) {
					e.printStackTrace();
				}
				jsonObject.put("entity", "companies");
				break;
			case "job":
				try {
					String content = new String(Files.readAllBytes(Paths.get("src/test/resources/job_import_payload.json")));
					jsonObject = new JSONObject(content);
				} catch (IOException e) {
					e.printStackTrace();
				}
				jsonObject.put("entity", "jobs");
				break;
			default:
				break;
		}
		jsonObject.put("fileInfo", s3_key + "," + fileName);
		jsonObject.put("overrideData", overrideStatus);
		if (addToHotlist) {
			jsonObject.put("name", hotlistName);
		}
		jsonObject.put("mergeDuplicates", mergeDuplicates);
		jsonObject.put("mergeUsing", mergeUsing);

		Response vars = RestClient.doPost("JSON", baseUrl, "import/import-data", authTokenMap, null, false, jsonObject);
		Assert.assertEquals(vars.getStatusCode(), 200, "Status code did not match");

		return vars;
	}

	public Response createSequence(String nymaUrl, String authToken, int entityTypeId){
		Map<String, String> authTokenMap = new HashMap<>();
		authTokenMap.put("Authorization", "Bearer " + authToken);

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);
		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setEntity_type(entityTypeId);
		createEmailSequence.setSeq_title("candidate" + " add sequence test " + RandomStringUtils.randomAlphabetic(4));
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);


		Response response = RestClient.doPost("JSON", nymaUrl, "email-sequences", authTokenMap, null, true,
				createEmailSequence);

		response.then().statusCode(200);
		return response;
	}

	public Response addEmailStepToSequence(String nymaUrl, String authToken, int sequenceId, int stepNo) {
		Map<String, String> authTokenMap = new HashMap<>();
		authTokenMap.put("Authorization", "Bearer " + authToken);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(sequenceId));

		CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
		createEmailStepToSequence.setStep_no(stepNo);
		createEmailStepToSequence.setNo_of_days(2);
		createEmailStepToSequence.setTemplate_title("Test Email Template " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence.setTemplate_subject("Test Email Subject " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence.setTemplate_content("Test Email Content " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence.setTime(3600);
		createEmailStepToSequence.setType(1);
		createEmailStepToSequence.setUpdate_type("all");
		createEmailStepToSequence.setInclude_opt_out_link(1);

		ArrayList<Object> emailStep = new ArrayList<>();
		emailStep.add(createEmailStepToSequence);
		AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
		addEmailStep.setSteps(emailStep);

		String basePath = "email-sequences/{id}/steps";
		Response responseAddEmailStep = RestClient.doPost1("JSON", nymaUrl, basePath,authTokenMap, null,
				pathParameters, true, addEmailStep);

		responseAddEmailStep.then().statusCode(200);
		return responseAddEmailStep;
	}

	public Response addTaskStepToSequence(String nymaUrl, String authToken, int sequenceId, int stepNo) {
		Map<String, String> authTokenMap = new HashMap<>();
		authTokenMap.put("Authorization", "Bearer " + authToken);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(sequenceId));

		CreateTaskStepToSequencePage createTaskStepToSequence = new CreateTaskStepToSequencePage();
		createTaskStepToSequence.setNo_of_days(2);
		createTaskStepToSequence.setStep_no(stepNo);
		createTaskStepToSequence.setTime(3600);
		createTaskStepToSequence.setType(2);
		createTaskStepToSequence.setReminder(30);
		createTaskStepToSequence.setTask_title("Task step" + RandomStringUtils.randomAlphabetic(4));
		createTaskStepToSequence.setTask_description("Task remainder in sequence" + RandomStringUtils.randomAlphabetic(4));
		createTaskStepToSequence.setUpdate_type("all");

		ArrayList<Object> taskStep = new ArrayList<>();
		taskStep.add(createTaskStepToSequence);
		AddTaskStepsToSequencePage addTaskSteps = new AddTaskStepsToSequencePage();
		addTaskSteps.setSteps(taskStep);

		String basePath = "email-sequences/{id}/steps";
		Response responseTaskStep = RestClient.doPost1("JSON", nymaUrl, basePath, authTokenMap, null, pathParameters,
				true, addTaskSteps);

		responseTaskStep.then().statusCode(200);
		return responseTaskStep;
	}

	public Response addSmsStepToSequence(String nymaUrl, String authToken, int sequenceId, int stepNo) {
		Map<String, String> authTokenMap = new HashMap<>();
		authTokenMap.put("Authorization", "Bearer " + authToken);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(sequenceId));

		CreateSmsStepToSequencePage createSmsStepToSequencePage = new CreateSmsStepToSequencePage();
		createSmsStepToSequencePage.setType(3);
		createSmsStepToSequencePage.setStep_no(stepNo);
		createSmsStepToSequencePage.setNo_of_days(2);
		createSmsStepToSequencePage.setSms_template_title("Sms step template " + RandomStringUtils.randomAlphabetic(4));
		createSmsStepToSequencePage.setSms_template_content("Sms step template " + RandomStringUtils.randomAlphabetic(4));
		createSmsStepToSequencePage.setTime(3600);
		createSmsStepToSequencePage.setUpdate_type("all");

		ArrayList<Object> smsStep = new ArrayList<>();
		smsStep.add(createSmsStepToSequencePage);
		AddSmsStepsToSequencePage addSmsStep = new AddSmsStepsToSequencePage();
		addSmsStep.setSteps(smsStep);

		String basePath = "email-sequences/{id}/steps";
		Response responseSmsStep = RestClient.doPost1("JSON", nymaUrl, basePath, authTokenMap, null,
				pathParameters, true, addSmsStep);

		responseSmsStep.then().statusCode(200);

		return responseSmsStep;
	}

	public Response addLinkedInStepToSequence(String nymaUrl, String authToken, int sequenceId, int stepNo) {
		Map<String, String> authTokenMap = new HashMap<>();
		authTokenMap.put("Authorization", "Bearer " + authToken);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(sequenceId));

		CreateLinkedInStepToSequencePage createLinkedInStepToSequence = new CreateLinkedInStepToSequencePage();
		createLinkedInStepToSequence.setTime(36000);
		createLinkedInStepToSequence.setNo_of_days("2");
		createLinkedInStepToSequence.setUpdate_type("all");
		createLinkedInStepToSequence.setLinkedin_template_title("LinkedIn step" + RandomStringUtils.randomAlphabetic(4));
		createLinkedInStepToSequence.setLinkedin_template_content("LinkedIn remainder in sequence" + RandomStringUtils.randomAlphabetic(4));
		createLinkedInStepToSequence.setStep_no(stepNo);
		createLinkedInStepToSequence.setType(4);
		createLinkedInStepToSequence.setEmail_sms_linkedin_step_cnt(3);


		ArrayList<Object> step = new ArrayList<>();
		step.add(createLinkedInStepToSequence);
		AddTaskStepsToSequencePage addTaskSteps = new AddTaskStepsToSequencePage();
		addTaskSteps.setSteps(step);

		String basePath = "email-sequences/{id}/steps";
		Response responseLinkedInStep = RestClient.doPost1("JSON", nymaUrl, basePath, authTokenMap, null,
				pathParameters, true, addTaskSteps);

		responseLinkedInStep.then().statusCode(200);

		return responseLinkedInStep;
	}

	public Response createCandidateWithJson(String albatrossURL, Object authToken, JSONObject candidateJson) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		JSONObject payload = new JSONObject();
		payload.put("candidate", candidateJson);
		int maxRetries = 3;
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			Response response = RestClient.doPost("JSON", albatrossURL, "candidates", authTokenMap, null, true, payload);
			if (response.getStatusCode() != 401) {
				response.then().statusCode(200);
				return response;
			}
			if (attempt < maxRetries) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Retry interrupted", e);
				}
			}
		}
		throw new RuntimeException("createCandidateWithJson failed with 401 after " + maxRetries + " retries");
	}

	public Response createCompanyWithJson(String albatrossURL, Object authToken, JSONObject companyJson) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		int maxRetries = 3;
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			Response response = RestClient.doPost("JSON", albatrossURL, "companies", authTokenMap, null, true, companyJson);
			if (response.getStatusCode() != 401) {
				response.then().statusCode(200);
				return response;
			}
			if (attempt < maxRetries) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Retry interrupted", e);
				}
			}
		}
		throw new RuntimeException("createCompanyWithJson failed with 401 after " + maxRetries + " retries");
	}

	public Response linkCompanyToParentCompany(String albatrossURL, Object authToken, String parentCompanySlug, List<String> childCompanySlugs) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		CompanyInheritance companyInheritance = new CompanyInheritance();
		companyInheritance.setParent_company_slug(parentCompanySlug);
		companyInheritance.setChild_company_slugs(childCompanySlugs);
		String basePath = "companies/link-to-parent-company";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, companyInheritance);
		response.then().statusCode(200);
		return response;
	}

	public Response createHotlistWithJson(String albatrossURL, Object authToken, JSONObject hotlistJson) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		Response response = RestClient.doPost("JSON", albatrossURL, "hotlists", authTokenMap, null, true, hotlistJson);
		response.then().statusCode(200);
		return response;
	}

	public Response getWorkHistory(String albatrossURL, Object authToken, int candidateId) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String basePath = "candidates/candidate-work/"+candidateId;
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, authTokenMap, null, null, true);
		response.then().statusCode(200);
		return response;
	}

	public Response getEducationHistory(String albatrossURL, Object authToken, int candidateId) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String basePath = "candidates/candidate-education/"+candidateId;
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, authTokenMap, null, null, true);
		response.then().statusCode(200);
		return response;
	}

	public Map<String, Integer> getOffLimitStatusMap(String albatrossURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		int maxRetries = 3;
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			Response response = RestClient.doGet("JSON", albatrossURL, "off-limit/status", authTokenMap, null, null, true);
			if (response.getStatusCode() != 401) {
				response.then().statusCode(200);
				return parseOffLimitStatusMapFromResponse(response);
			}
			if (attempt < maxRetries) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Retry interrupted", e);
				}
			}
		}
		throw new RuntimeException("getOffLimitStatusMap failed with 401 after " + maxRetries + " retries");
	}

	public Map<String, Integer> getIndustryIdMapWithRetry(String albatrossURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		int maxRetries = 3;
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			Response response = RestClient.doPost("JSON", albatrossURL, "industries", authTokenMap, null, true, null);
			if (response.getStatusCode() != 401) {
				response.then().statusCode(200);
				return parseIndustryIdMapFromResponse(response);
			}
			if (attempt < maxRetries) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Retry interrupted", e);
				}
			}
		}
		throw new RuntimeException("getIndustryIdMapWithRetry failed with 401 after " + maxRetries + " retries");
	}

	private static Map<String, Integer> parseIndustryIdMapFromResponse(Response response) {
		JsonPath responseJson = response.jsonPath();
		Map<String, Integer> industryIdMap = new HashMap<>();
		List<Map<String, Object>> defaultIndustries = responseJson.getList("data.defaultIndustries");
		for (Map<String, Object> industry : defaultIndustries) {
			String label = (String) industry.get("label");
			Integer id = ((Number) industry.get("id")).intValue();
			industryIdMap.put(label, id);
		}
		List<Map<String, Object>> customIndustries = responseJson.getList("data.customIndustries");
		for (Map<String, Object> industry : customIndustries) {
			String label = (String) industry.get("label");
			Integer id = ((Number) industry.get("id")).intValue();
			industryIdMap.put(label, id);
		}
		industryIdMap.put("None", 0);
		return industryIdMap;
	}

	private static Map<String, Integer> parseOffLimitStatusMapFromResponse(Response response) {
		JSONObject responseJson = new JSONObject(response.getBody().asString());
		JSONObject data = responseJson.getJSONObject("data");
		JSONArray offLimitStatusArray = data.getJSONArray("offLimitStatus");
		Map<String, Integer> statusMap = new HashMap<>();
		for (int i = 0; i < offLimitStatusArray.length(); i++) {
			JSONObject statusObj = offLimitStatusArray.getJSONObject(i);
			statusMap.put(statusObj.getString("status_label"), statusObj.getInt("id"));
		}
		return statusMap;
	}

}


