package io.recruitcrm.albatross.offlimit;

import com.qa.api.util.*;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.offlimit.*;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus.offLimitStatus;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.*;

import io.rcrm.api.javafaker.JavaFakerCompany;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointsOfOffLimitAlbatrossTest extends TestBase {

	commanFunction function = new commanFunction();
	AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
	JavaFakerCompany faker = new JavaFakerCompany();

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void addOffLimitStatusTest() {
		String label = faker.getOfflimitStatusLabel();

		Response response = createOffLimitStatus(label, "B1", "#FEE2E1", "#9E4D4D");

		response.then().statusCode(200);
		response.then().body("data[0].status_label", Matchers.is(label));
		response.then().body("data[0].status_colour_id", Matchers.is("A1"));
		response.then().body("data[0].sequence_no", Matchers.is(1));
		response.then().body("data[0].account_id", Matchers.is(ThreadManager.getAccount().getAccountId()));
		response.then().body("data[0].default", Matchers.is("0"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "offLimitStatusLabelProvider", groups = "nightly-build")
	public void getOffLimitStatusTest(String label) {
		String basePath = "off-limit/status";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true);
		response.then().statusCode(200);

		int index = findStatusIndexByLabel(response, label);
		Assert.assertTrue(index >= 0, "Created off-limit status with label '" + label + "' was not found in the list response");

		response.then().body("data.offLimitStatus[" + index + "].id", Matchers.notNullValue());
		response.then().body("data.offLimitStatus[" + index + "].status_label", Matchers.is(label));
		response.then().body("data.offLimitStatus[" + index + "].status_colour_id", Matchers.is("A1"));
		response.then().body("data.offLimitStatus[" + index + "].sequence_no", Matchers.is(1));
		response.then().body("data.offLimitStatus[" + index + "].account_id", Matchers.is(ThreadManager.getAccount().getAccountId()));
		response.then().body("data.offLimitStatus[" + index + "].default", Matchers.is(0));
		response.then().body("data.offLimitStatus[" + index + "].offlimit_status_colour_id", Matchers.is("A1"));
		response.then().body("data.offLimitStatus[" + index + "].background_color_hex", Matchers.is("#FFD7D7"));
		response.then().body("data.offLimitStatus[" + index + "].text_color_hex", Matchers.is("#AB0A0A"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void updateOffLimitStatusTest() {
		String label = faker.getOfflimitStatusLabel();

		Response response = createOffLimitStatus(label, "B1", "#FFF7ED", "#8F6A3C");

		response.then().statusCode(200);
		response.then().body("data[0].status_label", Matchers.is(label));
		response.then().body("data[0].status_colour_id", Matchers.is("A1"));
		response.then().body("data[0].sequence_no", Matchers.is(1));
		response.then().body("data[0].account_id", Matchers.is(ThreadManager.getAccount().getAccountId()));
		response.then().body("data[0].default", Matchers.is("0"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "entityDataProvider", groups = "nightly-build")
	public void markOffLimitTest(int entity_type_id, int[] entity_ids, int status_id, String startDate, String endDate, String reason) {
		Response response = markOffLimit(entity_type_id, entity_ids, status_id, startDate, endDate, reason);

		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Mark Entity As Off-Limit Successful "));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "offLimitEntityProvider", groups = "nightly-build")
	public void markAsAvailableTest(int entity_type_id, int[] entity_ids) {
		Response response = markAsAvailable(entity_type_id, entity_ids);

		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Mark Entity As Available Successful "));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "availableEntityProvider", groups = "nightly-build")
	public void getOffLimitStatusHistoryTest(int entity_type_id, int[] entity_ids) {
		String basePath = "off-limit/history/{entity}/{id}";

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("entity", String.valueOf(entity_type_id));
		pathParameters.put("id", String.valueOf(entity_ids[0]));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);

		response.then().statusCode(200);
		response.then().body("data[0].entity_id", Matchers.is(entity_ids[0]));
		response.then().body("data[0].entity_type", Matchers.is(entity_type_id));
		response.then().body("data[0].status_label", Matchers.nullValue());
		response.then().body("data[0].account_id", Matchers.is(ThreadManager.getAccount().getAccountId()));
		response.then().body("data[0].status_colour_id", Matchers.nullValue());
		response.then().body("data[0].off_limit_reason", Matchers.nullValue());
		response.then().body("data[0].background_color_hex", Matchers.nullValue());
		response.then().body("data[0].text_color_hex", Matchers.nullValue());
		if (entity_type_id == 3) {
			response.then().body("data[0].off_limit_contact", Matchers.is(0));
			response.then().body("data[0].off_limit_candidate", Matchers.is(0));
		}
	}

	private Object[][] createOffLimitEntities() {
		int candidateId = createEntityId(5);
		int contactId = createEntityId(2);
		int companyId = createEntityId(3);
		int statusId = createNewOffLimitStatusId();

		String reason = "Test Reason " + RandomStringUtils.randomAlphabetic(4);
		String startDate = DateUtil.getTodayDateString();
		String endDate = DateUtil.getTomorrowDateString();

		Object[][] entities = {
				{5, new int[]{candidateId}},
				{2, new int[]{contactId}},
				{3, new int[]{companyId}}
		};

		for (Object[] entity : entities) {
			Response response = markOffLimit((int) entity[0], (int[]) entity[1], statusId, startDate, endDate, reason);
			Assert.assertEquals(response.getStatusCode(), 200, "Prerequisite: failed to mark entity_type_id " + entity[0] + " as off-limit");
		}
		return entities;
	}

	private int createEntityId(int entity_type_id) {
		switch (entity_type_id) {
			case 5:
				return albatrossFunctions.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("data.candidate.id");
			case 2: {
				String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
				String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().get("slug");
				return Integer.parseInt(albatrossFunctions.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), contactSlug).jsonPath().get("data.contact.id"));
			}
			case 3: {
				String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
				return albatrossFunctions.getCompanyResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), companySlug).jsonPath().get("data.company.id");
			}
			default:
				throw new IllegalArgumentException("Unsupported entity_type_id: " + entity_type_id);
		}
	}

	private Response createOffLimitStatus(String label, String offlimitStatusColourId, String backgroundColorHex, String textColorHex) {
		offLimitStatus offLimitStatus = new offLimitStatus();
		offLimitStatus.setStatus_label(label);
		offLimitStatus.setStatus_colour_id("A1");
		offLimitStatus.setSequence_no(1);
		offLimitStatus.setAccount_id(String.valueOf(ThreadManager.getAccount().getAccountId()));
		offLimitStatus.setDefaultStatus("0");
		offLimitStatus.setOfflimit_status_colour_id(offlimitStatusColourId);
		offLimitStatus.setBackground_color_hex(backgroundColorHex);
		offLimitStatus.setText_color_hex(textColorHex);
		offLimitStatus.setCount(0);

		OffLimitStatus offLimitStatusBody = new OffLimitStatus();
		offLimitStatusBody.setOffLimitStatus(new offLimitStatus[]{offLimitStatus});

		return RestClient.doPost1("JSON", albatrossURL, "off-limit/status", ThreadManager.getOwnerAlbatrossToken(), null, null, true, offLimitStatusBody);
	}

	private int findStatusIndexByLabel(Response response, String label) {
		int size = response.jsonPath().getList("data.offLimitStatus").size();
		for (int i = 0; i < size; i++) {
			if (response.jsonPath().getString("data.offLimitStatus[" + i + "].status_label").equals(label)) {
				return i;
			}
		}
		return -1;
	}

	private int createNewOffLimitStatusId() {
		String label = faker.getOfflimitStatusLabel();
		createOffLimitStatus(label, "A1", "#FEE2E1", "#9E4D4D").then().statusCode(200);

		Response response = RestClient.doGet("JSON", albatrossURL, "off-limit/status", ThreadManager.getOwnerAlbatrossToken(), null, null, true);
		int index = findStatusIndexByLabel(response, label);
		Assert.assertTrue(index >= 0, "Off-limit status with label '" + label + "' was not found after creation");

		return response.jsonPath().getInt("data.offLimitStatus[" + index + "].id");
	}

	private Response markOffLimit(int entity_type_id, int[] entity_ids, int status_id, String startDate, String endDate, String reason) {
		if (entity_type_id == 3) {
			MarkOffLimitCompany markOffLimitCompany = new MarkOffLimitCompany();
			markOffLimitCompany.setEntity_type_id(entity_type_id);
			markOffLimitCompany.setEntity_ids(entity_ids);
			markOffLimitCompany.setStatus_id(status_id);
			markOffLimitCompany.setStart_date(startDate);
			markOffLimitCompany.setEnd_date(endDate);
			markOffLimitCompany.setReason(reason);
			markOffLimitCompany.setMark_contact_off_limit(true);
			markOffLimitCompany.setMark_candidate_off_limit(true);

			return RestClient.doPost1("JSON", albatrossURL, "off-limit/mark-off-limit", ThreadManager.getOwnerAlbatrossToken(), null, null, true, markOffLimitCompany);
		}

		MarkOffLimit markOffLimit = new MarkOffLimit();
		markOffLimit.setEntity_type_id(entity_type_id);
		markOffLimit.setEntity_ids(entity_ids);
		markOffLimit.setStatus_id(status_id);
		markOffLimit.setStart_date(startDate);
		markOffLimit.setEnd_date(endDate);
		markOffLimit.setReason(reason);

		return RestClient.doPost1("JSON", albatrossURL, "off-limit/mark-off-limit", ThreadManager.getOwnerAlbatrossToken(), null, null, true, markOffLimit);
	}

	private Response markAsAvailable(int entity_type_id, int[] entity_ids) {
		if (entity_type_id == 3) {
			MarkAsAvailableCompany markAsAvailableCompany = new MarkAsAvailableCompany();
			markAsAvailableCompany.setEntity_type_id(entity_type_id);
			markAsAvailableCompany.setEntity_ids(entity_ids);
			markAsAvailableCompany.setMark_candidate_available(true);
			markAsAvailableCompany.setMark_contact_available(true);

			return RestClient.doPost1("JSON", albatrossURL, "off-limit/mark-as-available", ThreadManager.getOwnerAlbatrossToken(), null, null, true, markAsAvailableCompany);
		}

		MarkAsAvailable markAsAvailable = new MarkAsAvailable();
		markAsAvailable.setEntity_type_id(entity_type_id);
		markAsAvailable.setEntity_ids(entity_ids);

		return RestClient.doPost1("JSON", albatrossURL, "off-limit/mark-as-available", ThreadManager.getOwnerAlbatrossToken(), null, null, true, markAsAvailable);
	}

	@DataProvider
	public Object[][] offLimitStatusLabelProvider() {
		String label = faker.getOfflimitStatusLabel();
		createOffLimitStatus(label, "A1", "#FFD7D7", "#AB0A0A").then().statusCode(200);
		return new Object[][]{{label}};
	}

	@DataProvider
	public Object[][] entityDataProvider() {
		int candidateId = createEntityId(5);
		int contactId = createEntityId(2);
		int companyId = createEntityId(3);
		int statusId = createNewOffLimitStatusId();

		String reason = "Test Reason " + RandomStringUtils.randomAlphabetic(4);
		String startDate = DateUtil.getTodayDateString();
		String endDate = DateUtil.getTomorrowDateString();

		return new Object[][]{
				{5, new int[]{candidateId}, statusId, startDate, endDate, reason},
				{2, new int[]{contactId}, statusId, startDate, endDate, reason},
				{3, new int[]{companyId}, statusId, startDate, endDate, reason}
		};
	}

	@DataProvider
	public Object[][] availableEntityProvider() {
		Object[][] entities = createOffLimitEntities();
		for (Object[] entity : entities) {
			Response response = markAsAvailable((int) entity[0], (int[]) entity[1]);
			Assert.assertEquals(response.getStatusCode(), 200, "Prerequisite: failed to mark entity_type_id " + entity[0] + " as available");
		}
		return entities;
	}

	@DataProvider
	public Object[][] offLimitEntityProvider() {
		return createOffLimitEntities();
	}
}