package io.rcrm.api.notes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;


@AccountType("Business")
public class SearchNoteByAllFieldsTest extends TestBase {

	private static final String DATE_FORMAT = "dd-MM-yyyy";
	private static final String API_DATE_FORMAT = "yyyy-MM-dd";
	private static final String BASE_PATH = "notes/search";
	
	commanFunction function = new commanFunction();
	String accountAPIKey;

	public SearchNoteByAllFieldsTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountAPIKey = ThreadManager.getAccountApiKey();
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getNotesValidData", groups = "nightly-build")
	public void searchNoteByAllFields_noteTest(String entitySlug, String relatedToType) throws ParseException {
		String tomorrowDateString = DateUtil.getTomorrowDateString(DATE_FORMAT);
		String yesterdayDateString = DateUtil.getYesterdayDateString(DATE_FORMAT);

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("related_to", entitySlug);
		queryParameters.put("related_to_type", relatedToType);
		queryParameters.put("updated_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("added_to", tomorrowDateString);
		queryParameters.put("added_from", yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, BASE_PATH, accountAPIKey, queryParameters, null, true);
        assert response != null;
        JsonPath jp = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200, "Response Status Code must be 200!");
		Assert.assertTrue(jp.getList("data.related_to_type").contains(relatedToType),
			"'related_to_type' does not contain expected value: " + relatedToType);
		Assert.assertTrue(jp.prettify().contains("collaborator_users"), "'collaborator_users' not found in response");
		Assert.assertTrue(jp.prettify().contains("collaborator_teams"), "'collaborator_teams' not found in response");
		validateDateRanges(jp, yesterdayDateString, tomorrowDateString);
	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void searchNoteByAllInvalidFields_noteTest() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("related_to", "x0001");
		queryParameters.put("related_to_type", "Candidatex01");

		Response response = RestClient.doGet("JSON", baseURL, BASE_PATH, accountAPIKey, queryParameters, null, true);
        assert response != null;
        JsonPath jp = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422, "Expected status code 422");
		Assert.assertEquals(jp.getString("related_to_type[0]"), "The selected related to type is invalid.",
			"'related_to_type[0]' error message mismatch");
		Assert.assertEquals(jp.getString("related_to[0]"), "related to is not valid.",
			"'related_to[0]' error message mismatch");
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotAccessSearchNoteByAllFields_noteTest() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("related_to", "xooo1");
		queryParameters.put("related_to_type", "Candidate");

		Response response = RestClient.doGet("JSON", baseURL, BASE_PATH, accountAPIKey + "12345", queryParameters, null, true);
        assert response != null;
        JsonPath jp = response.jsonPath();
		
		Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401");
		Assert.assertTrue(jp.getString("error").contains("Unauthorized"),
			"'error' message does not contain 'Unauthorized'");
	}

	@Owner("Suhel Bhadane")
	@Test(dataProvider = "dateFilterScenarios", groups = "nightly-build")
	public void searchNoteByDateFilters(String filterType, String relatedToType, String relatedTo, 
			int expectedStatus, String expectedErrorKey, String expectedErrorMessage) throws ParseException {

		Map<String, String> queryParams = new HashMap<>();
		// Add date filter dynamically
		String dateValue = filterType.endsWith("_from") ? DateUtil.getYesterdayDateString(DATE_FORMAT) : DateUtil.getTomorrowDateString(DATE_FORMAT);
		queryParams.put(filterType, dateValue);
		
		// Add related fields if provided
		if (relatedToType != null) queryParams.put("related_to_type", relatedToType);
		if (relatedTo != null) queryParams.put("related_to", relatedTo);

		Response response = RestClient.doGet("JSON", baseURL, BASE_PATH, accountAPIKey, queryParams, null, true);
		assert response != null;
		Assert.assertEquals(response.getStatusCode(), expectedStatus, "Unexpected response status code!");

		if (expectedStatus == 200) {
			// Determine which date field to check and validate
			String dateField = filterType.startsWith("added") ? "created_on" : "updated_on";
			Date expectedDate = new SimpleDateFormat(DATE_FORMAT).parse(dateValue);
			Date actualDate = new SimpleDateFormat(API_DATE_FORMAT).parse(response.jsonPath().getString("data[0]." + dateField));
			if (filterType.endsWith("_from")) {
				Assert.assertTrue(actualDate.after(expectedDate), String.format("Expected %s to be after %s", actualDate, expectedDate));
			} else {
				Assert.assertTrue(actualDate.before(expectedDate), String.format("Expected %s to be before %s", actualDate, expectedDate));
			}
		} else {
			Assert.assertEquals(response.jsonPath().getString(expectedErrorKey), expectedErrorMessage);
		}
	}

	@Owner("Suhel Bhadane")
	@Test(groups = "nightly-build")
	public void searchNoteWithOnlyRelatedTo() {
		Response createNoteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "candidate");
		Assert.assertEquals(createNoteResponse.getStatusCode(), 200, "Failed to create test note!");
		String entitySlug = createNoteResponse.jsonPath().get("related_to");

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("related_to", entitySlug);

		Response response = RestClient.doGet("JSON", baseURL, BASE_PATH, accountAPIKey, queryParams, null, true);
        assert response != null;
        JsonPath jp = response.jsonPath();
		
		Assert.assertEquals(response.getStatusCode(), 422, "Expected status code 422 when only related_to is provided");
		Assert.assertEquals(jp.getString("related_to"), "related to type field is required.");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = "nightly-build")
	public void searchNoteWithOnlyRelatedToType() {
		Response createNoteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "candidate");
		Assert.assertEquals(createNoteResponse.getStatusCode(), 200, "Failed to create test note!");

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("related_to_type", "candidate");

		Response response = RestClient.doGet("JSON", baseURL, BASE_PATH, accountAPIKey, queryParameters, null, true);
        assert response != null;
        String actualMessage = response.jsonPath().getString("related_to[0]");

		Assert.assertEquals(response.getStatusCode(), 422, "Expected status code 422 when only related_to_type is provided");
		Assert.assertEquals(actualMessage, "The related to field is required when related to type is present.",
			"Error message should indicate related_to is required when related_to_type is provided");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = "nightly-build")
	public void searchNoteWithNoRelatedFields() {
		Response createNoteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "candidate");
		Assert.assertEquals(createNoteResponse.getStatusCode(), 200, "Failed to create test note!");
		
		Map<String, String> queryParameters = new HashMap<>();
		Response response = RestClient.doGet("JSON", baseURL, BASE_PATH, accountAPIKey, queryParameters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 when no related fields are provided");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = "nightly-build")
	public void searchNoteWithOtherFiltersOnly() {
		Response createNoteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "candidate");
		Assert.assertEquals(createNoteResponse.getStatusCode(), 200, "Failed to create test note!");

		String tomorrowDateString = DateUtil.getTomorrowDateString(DATE_FORMAT);
		String yesterdayDateString = DateUtil.getYesterdayDateString(DATE_FORMAT);

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("updated_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("added_to", tomorrowDateString);
		queryParameters.put("added_from", yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, BASE_PATH, accountAPIKey, queryParameters, null, true);
        assert response != null;
        JsonPath jp = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 when only date filters are provided");
		Assert.assertTrue(jp.prettify().contains("data"), "Response should contain data array");
	}

	@Owner("Suhel Bhadane")
	@Test(dataProvider = "getNotesValidData", groups = "nightly-build")
	public void searchNoteWithValidRelatedFields(String entitySlug, String relatedToType) {
		Response createNoteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "candidate");
		Assert.assertEquals(createNoteResponse.getStatusCode(), 200, "Failed to create test note!");
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("related_to", entitySlug);
		queryParameters.put("related_to_type", relatedToType);

		Response response = RestClient.doGet("JSON", baseURL, BASE_PATH, accountAPIKey, queryParameters, null, true);
        assert response != null;
        JsonPath jp = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200,
			"Expected status code 200 when both related fields are provided with valid values");
		Assert.assertTrue(jp.getList("data.related_to_type").contains(relatedToType),
			"'related_to_type' should contain expected value: " + relatedToType);
	}

	@Owner("Suhel Bhadane")
	@Test(dataProvider = "invalidRelatedFieldsScenarios", groups = "nightly-build")
	public void searchNoteWithInvalidRelatedFields(String relatedTo, String relatedToType, String expectedErrorKey, String expectedErrorMessage) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("related_to", relatedTo);
		queryParams.put("related_to_type", relatedToType);

		Response response = RestClient.doGet("JSON", baseURL, BASE_PATH, accountAPIKey, queryParams, null, true);
        assert response != null;
        JsonPath jp = response.jsonPath();
		
		Assert.assertEquals(response.getStatusCode(), 422, "Expected status code 422 when related fields are invalid");
		Assert.assertEquals(jp.getString(expectedErrorKey), expectedErrorMessage,
			"Error message mismatch for " + expectedErrorKey);
	}

	private void validateDateRanges(JsonPath jp, String expectedFromDate, String expectedToDate) throws ParseException {
		String jpDate1 = jp.get("data[0].created_on");
		String jpDate2 = jp.get("data[0].updated_on");
		Date actualCreatedDate = new SimpleDateFormat(API_DATE_FORMAT).parse(jpDate1);
		Date actualUpdatedDate = new SimpleDateFormat(API_DATE_FORMAT).parse(jpDate2);

		Date expectedYesterdayDate = new SimpleDateFormat(DATE_FORMAT).parse(expectedFromDate);
		Date expectedTomorrowDate = new SimpleDateFormat(DATE_FORMAT).parse(expectedToDate);

		Assert.assertTrue(actualCreatedDate.after(expectedYesterdayDate),
			"Actual created date is not after the expected date, expected : " + expectedYesterdayDate + " but found : " + actualCreatedDate);
		Assert.assertTrue(actualUpdatedDate.before(expectedTomorrowDate),
			"Actual updated date is not before the expected date, expected : " + expectedTomorrowDate + " but found : " + actualUpdatedDate);
	}

	@DataProvider(parallel = true)
	public Object[][] invalidRelatedFieldsScenarios() {
		Response response = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "candidate");
		Assert.assertEquals(response.getStatusCode(), 200, "Note creation failed in DataProvider!");
		String entitySlug = response.jsonPath().get("related_to");

		return new Object[][]{
			{"invalid-entity-slug", "candidate", "related_to", "The related_to value is invalid. It should be valid candidate slug"},
			{entitySlug, "InvalidType", "related_to_type[0]", "The selected related to type is invalid."}
		};
	}

	@DataProvider(parallel = true)
	public Object[][] dateFilterScenarios() {
		Response response = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "candidate");
		Assert.assertEquals(response.getStatusCode(), 200, "Note creation failed in DataProvider!");
		String entitySlug = response.jsonPath().get("related_to");

		return new Object[][]{
				// filterType, relatedToType, relatedTo, expectedStatus, errorKey, errorMessage
				{"added_from", "candidate", entitySlug, 200, null, null},
				{"added_to", "candidate", entitySlug, 200, null, null},
				{"updated_from", "candidate", entitySlug, 200, null, null},
				{"updated_to", "candidate", entitySlug, 200, null, null},

				{"added_from", null, null, 200, null, null},
				{"added_to", null, null, 200, null, null},
				{"updated_from", null, null, 200, null, null},
				{"updated_to", null, null, 200, null, null},

				{"added_from", "candidate", null, 422, "related_to[0]", "The related to field is required when related to type is present."},
				{"added_to", "candidate", null, 422, "related_to[0]", "The related to field is required when related to type is present."},
				{"updated_from", "candidate", null, 422, "related_to[0]", "The related to field is required when related to type is present."},
				{"updated_to", "candidate", null, 422, "related_to[0]", "The related to field is required when related to type is present."},

				{"added_from", null, entitySlug, 422, "related_to", "related to type field is required."},
				{"added_to", null, entitySlug, 422, "related_to", "related to type field is required."},
				{"updated_from", null, entitySlug, 422, "related_to", "related to type field is required."},
				{"updated_to", null, entitySlug, 422, "related_to", "related to type field is required."}
		};
	}


	@DataProvider(parallel = true)
	public Object[][] getNotesValidData() {
		String[] relatedTypes = {"candidate", "contact", "company", "job", "deal"};
		ExecutorService executor = Executors.newFixedThreadPool(5);
		List<Future<Object[]>> futures = new ArrayList<>();

		for (String relatedType : relatedTypes) {
			futures.add(executor.submit(() -> {
				Response response = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, relatedType);
				Assert.assertEquals(response.getStatusCode(), 200, "Response Status Code must be 200!");
				return new Object[]{response.jsonPath().get("related_to"), relatedType};
			}));
		}

		List<Object[]> dataList = new ArrayList<>();
		for (Future<Object[]> future : futures) {
			try {
				dataList.add(future.get());
			} catch (Exception e) {
				Assert.fail("Failed to create note with error message: " + e.getMessage());
			}
		}

		executor.shutdown();
		return dataList.toArray(new Object[0][]);
	}

}
