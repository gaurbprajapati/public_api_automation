package io.recruitcrm.ContactService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetContactEntityColumnsTest extends TestBase {

	private static final String BASE_PATH = "entity-columns";
	String albatrossAuthToken;

	public GetContactEntityColumnsTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"contact_service", "nightly-build"})
	public void getContactEntityColumnsTest_200() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "contacts");
		Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

		assert response != null;
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Entity Column Fetched Successfully");
		Assert.assertEquals(response.jsonPath().getInt("meta.status"), 200);
		Assert.assertEquals(response.jsonPath().getInt("meta.responseType.code"), 103);
		Assert.assertEquals(response.jsonPath().getString("meta.responseType.context"), "Request is successful");

		Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");
		assertThat("Columns should not be null", columnsMap, notNullValue());
		assertThat("Columns should contain id", columnsMap.containsKey("id"), is(true));

		// Validate id column - important fields only (keys exist, not values)
		Map<String, Object> idColumn = response.jsonPath().getMap("data[0].columns.id");
		assertThat("Id column should not be null", idColumn, notNullValue());
		String[] idColumnImportantFields = {
				"entity", "field", "label", "longlabel", "external_label",
				"visible", "visible_locked", "detailPageVisible", "detailPageOrder",
				"detailPageOrderV2", "listPageOrder", "primaryOrder",
				"allow_on_apply", "required_on_apply"
		};
		for (String field : idColumnImportantFields) {
			assertThat("Id column missing important field: " + field, idColumn.containsKey(field), is(true));
		}
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"contact_service", "nightly-build"})
	public void getContactEntityColumnsUnauthorizedTest_401() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "contacts");

		Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, "InvalidToken", queryParameters, null, true);
        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"contact_service", "nightly-build"})
	public void getContactEntityColumnsTest_404() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "contacts");

		Response response = RestClient.doGet("JSON", contactServiceURL, "ntity-columns", albatrossAuthToken, queryParameters, null, true);
        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 404);
		Assert.assertEquals(response.jsonPath().get("error"), "Not Found");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"contact_service", "nightly-build"})
	public void getContactEntityColumnsTest_400() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "");

		Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);
        assert response != null;
        Assert.assertEquals(response.getStatusCode(), 400);
		Assert.assertEquals(response.jsonPath().get("meta.responseType.context"), "Error while processing request");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"contact_service", "nightly-build"})
	public void getContactEntityColumnsTest_405() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "contacts");

		Response response = RestClient.doPost("JSON", contactServiceURL, BASE_PATH, albatrossAuthToken,
				queryParameters, true, null);
		Assert.assertEquals(response.getStatusCode(), 405);
		Assert.assertEquals(response.jsonPath().get("error"), "Method Not Allowed");
	}

	@Owner("Suhel Bhadane")
	@Test(dataProvider = "entityDataProvider", groups = {"contact_service", "nightly-build"})
	public void getContactEntityColumnsTestInvalidEntityTypes_400(String entity, int expectedStatus) {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", entity);

		Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossAuthToken,
				queryParameters, null, true);

        assert response != null;
        Assert.assertEquals(response.getStatusCode(), expectedStatus, "Unexpected status for entity: " + entity);

		if (expectedStatus == 400) {
			Assert.assertEquals(response.jsonPath().getInt("meta.status"), 400, "Incorrect status code in response");
			Assert.assertEquals(response.jsonPath().getInt("meta.responseType.code"), 101, "Incorrect response code");
			Assert.assertEquals(response.jsonPath().getString("meta.responseType.context"), "Error while processing request", "Incorrect response context");
			Assert.assertEquals(response.jsonPath().getString("errors[0].message"), "Entity must be one of the predefined enum values", "Incorrect error message");
			Assert.assertEquals(response.jsonPath().getInt("errors[0].errorType.code"), 201, "Incorrect error type code");
			Assert.assertEquals(response.jsonPath().getString("errors[0].errorType.context"), "Validation Error", "Incorrect error type context");
		}
	}

	@DataProvider(name = "entityDataProvider")
	public Object[][] entityDataProvider() {
		return new Object[][] {
				{ "candidates", 422 },
				{ "jobs", 404 },
				{ "12345", 400 },
				{ "invalidEntity1121309", 400 },
				{ "@#$@", 400 }
		};
	}
}
