package io.rcrm.api.emailsequence;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.DateUtil;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.nyma.AddEmailStepsToSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailStepToSequencePage;
import io.rcrm.api.pojo.nyma.SequenceSettingPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class SearchSequenceByFieldsTest extends TestBase {
	public SearchSequenceByFieldsTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	String candidateEntitySlug, contactSlug;
	int candidateSeqId;
	int contactSeqId;
	int userId;
	String candSeqName, contactSeqName;


	@Owner("Priyanka Shinde")
	@Test(dataProvider = "enrollSequenceDetails", groups = "nightly-build")
	public void searchSequenceBySequenceId(String entity, int EnrolledBy, int sequenceId, String sequenceName) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sequence_id", String.valueOf(sequenceId));

		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and JsonPath
		JsonPath jp = response.jsonPath();
		Integer id = jp.get("data.id[0]");
		Assert.assertTrue(id.equals(sequenceId));

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchSequenceBySequenceName() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sequence_name", candSeqName);

		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(candSeqName, jp.get("data.sequence_name[0]"), "SequenceName");

	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getSequenceTypeData", groups = "nightly-build")
	public void searchSequenceBySequenceType(String sequenceType, int statusCode) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sequence_type", sequenceType);

		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), statusCode);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(sequenceType, jp.get("data.sequence_type[0]"), "sequence_type");
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchSequenceByAdded_From() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("added_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchSequenceByAdded_To() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("added_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getExactSearchData", groups = "nightly-build")
	public void searchSequenceByAllFields(int exactSearch) throws ParseException {

		Map<String, String> queryParameters = new HashMap<String, String>();

		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		Date expectedDate2 = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		queryParameters.put("sequence_name", candSeqName);
		queryParameters.put("exact_search", String.valueOf(exactSearch));
		queryParameters.put("added_from", yesterdayDateString);
		queryParameters.put("added_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("updated_to", tomorrowDateString);
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");
		// cannot include slug here as if slug is added other parameters will be
		// ignored.

		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

//		Assert.assertEquals(CandidateEmail, jp.get("data.email[0]"), "email");

		String jpDate = jp.get("data.created_on[0]");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
		Assert.assertTrue(actualDate.before(expectedDate2), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);

		String jpDate2 = jp.get("data.updated_on[0]");
		Date actualDate2 = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate2);
		Assert.assertTrue(actualDate2.after(expectedDate), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate2);
		Assert.assertTrue(actualDate2.before(expectedDate2), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchSequenceBySortByAsc() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "created_on");
		queryParameters.put("sort_order", "asc");
		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchSequenceBySortByDesc() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "updated_on");
		queryParameters.put("sort_order", "desc");
		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 200);

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchSequenceByUpdated_From() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("updated_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchSequenceByUpdated_To() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("updated_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		String jpDate = jp.get("data[0].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@DataProvider
	public Object[][] getExactSearchData() {
		Object data[][] = { { 0 }, { 1 } };
		return data;
	}

	@DataProvider
	public Object[][] enrollSequenceDetails() {
		candidateEntitySlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
				.get("slug");

		String contactSlug = function
				.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), function
						.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug"))
				.jsonPath().get("slug");

		enrollEmailSequence("candidates", candidateEntitySlug);
		enrollEmailSequence("contacts", contactSlug);

		return new Object[][] { { "candidates", userId, candidateSeqId, candSeqName }
//		,{ "contacts", userId, contactSeqId,contactSeqName}
		};
	}

	private void enrollEmailSequence(String entity, String prospectSlug) {
		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();
		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);
		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setEntity_type(entity.equals("candidates") ? 5 : 2);
		createEmailSequence.setSeq_title(entity + " add sequence test " + RandomStringUtils.randomAlphabetic(4));
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequence);
		JsonPath jp = response.jsonPath();
		int seqId = jp.get("data.id");
		String seqName = jp.get("data.sequence_name");

		response.then().statusCode(200);
		if (entity.equals("candidates")) {
			candidateSeqId = seqId;
			candSeqName = seqName;
		} else {
			contactSeqId = seqId;
			contactSeqName = seqName;
		}

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
		createEmailStepToSequence.setStep_no(1);
		createEmailStepToSequence.setNo_of_days(2);
		createEmailStepToSequence
				.setTemplate_title(entity + " Email Template " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence
				.setTemplate_subject("Creating email Template for " + entity + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence
				.setTemplate_content(entity + " Template body " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence.setTime(3600);
		createEmailStepToSequence.setType(1);
		createEmailStepToSequence.setUpdate_type("all");
		createEmailStepToSequence.setInclude_opt_out_link(1);

		ArrayList<Object> emailStep = new ArrayList<>();
		emailStep.add(createEmailStepToSequence);
		AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
		addEmailStep.setSteps(emailStep);

		String basePath = "email-sequences/{id}/steps";
		Response responseAddEmailStep = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true, addEmailStep);

		responseAddEmailStep.then().statusCode(200);

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		JsonPath jp1 = getUsers.jsonPath();
		userId = jp1.get("data.records[0].id");

	}

	@DataProvider
	public Object[][] getSequenceTypeData() {

		Object data[][] = { { "candidate", 200 }, { "contact", 200 } };
		return data;
	}

}
