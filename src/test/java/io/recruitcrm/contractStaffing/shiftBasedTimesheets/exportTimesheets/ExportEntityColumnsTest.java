package io.recruitcrm.contractStaffing.shiftBasedTimesheets.exportTimesheets;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class ExportEntityColumnsTest extends ExportTimesheetBaseTest {

    private String albatrossAuthToken;
    private static final String BASE_PATH = "entity-columns";
    private static final String ENTITY_TIMESHEET_DEAL = "timesheet_deal";
    private static final String ENTITY_CANDIDATES = "candidates";

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "entityColumnsTestData", groups = {"contract_staffing", "nightly-build"})
    public void getTimesheetsEntityColumnsTest(String testCase, String entity, String authToken, int expectedStatusCode,
            String expectedMessage, String schemaPath) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", entity);

        Response response = RestClient.doGet("JSON", timesheetBaseURL, BASE_PATH, authToken, queryParameters, null,
                true);

        assertThat("Expected status code " + expectedStatusCode + " for " + testCase, response.getStatusCode(),
                equalTo(expectedStatusCode));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected message for " + testCase, jsonPath.getString("meta.message"), equalTo(expectedMessage));
        assertThat("Expected status in meta for " + testCase, jsonPath.getInt("meta.status"),
                equalTo(expectedStatusCode));

        if (expectedStatusCode == 200) {
            assertThat("Expected response type code 103 for " + testCase, jsonPath.getInt("meta.responseType.code"),
                    equalTo(103));
            assertThat("Expected success context for " + testCase, jsonPath.getString("meta.responseType.context"),
                    equalTo("Request is successful"));
            assertThat("Expected data array not null for " + testCase, jsonPath.get("data"), notNullValue());
            assertThat("Expected data array not empty for " + testCase, jsonPath.getList("data").size(), greaterThan(0));
            assertThat("Expected columns object not null for " + testCase, jsonPath.get("data[0].columns"),
                    notNullValue());
        } else if (expectedStatusCode == 401) {
            assertThat("Expected response type code 104 for " + testCase, jsonPath.getInt("meta.responseType.code"),
                    equalTo(104));
            assertThat("Expected warning context for " + testCase, jsonPath.getString("meta.responseType.context"),
                    equalTo("Warning"));
            assertThat("Expected data string for " + testCase, jsonPath.get("data"), instanceOf(String.class));
            assertThat("Expected errors array for " + testCase, jsonPath.get("errors"), instanceOf(java.util.List.class));
        }

        response.then().assertThat().body(matchesJsonSchemaInClasspath(schemaPath));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyTimesheetDealEntityColumnsIncludePayAndBillCurrencyTest() {
        // PAY-712: entity-columns for timesheet_deal must now include payCurrency/billCurrency column definitions
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", ENTITY_TIMESHEET_DEAL);

        Response response = RestClient.doGet("JSON", timesheetBaseURL, BASE_PATH, albatrossAuthToken,
                queryParameters, null, true);

        assertThat("Expected 200 for timesheet_deal entity columns", response.getStatusCode(), equalTo(200));

        JsonPath jsonPath = response.jsonPath();
        Map<String, Object> payCurrencyColumn = jsonPath.getMap("data[0].columns.payCurrency");
        Map<String, Object> billCurrencyColumn = jsonPath.getMap("data[0].columns.billCurrency");

        assertThat("payCurrency column should be present for timesheet_deal", payCurrencyColumn, notNullValue());
        assertThat("payCurrency field should be payCurrency", payCurrencyColumn.get("field"), equalTo("payCurrency"));
        assertThat("payCurrency label should be Pay Currency", payCurrencyColumn.get("label"), equalTo("Pay Currency"));
        assertThat("payCurrency should be allowed on export", payCurrencyColumn.get("allow_on_export"), equalTo(true));

        assertThat("billCurrency column should be present for timesheet_deal", billCurrencyColumn, notNullValue());
        assertThat("billCurrency field should be billCurrency", billCurrencyColumn.get("field"), equalTo("billCurrency"));
        assertThat("billCurrency label should be Bill Currency", billCurrencyColumn.get("label"), equalTo("Bill Currency"));
        assertThat("billCurrency should be allowed on export", billCurrencyColumn.get("allow_on_export"), equalTo(true));
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "candidateEntityColumnsTestData", groups = {"contract_staffing", "nightly-build"})
    public void getCandidateEntityColumnsTest(String testCase, String entity, String authToken, int expectedStatusCode,
            String expectedMessage) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", entity);

        Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, authToken, queryParameters, null,
                true);

        assertThat("Expected status code " + expectedStatusCode + " for " + testCase, response.getStatusCode(),
                equalTo(expectedStatusCode));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected message for " + testCase, jsonPath.getString("meta.message"), equalTo(expectedMessage));
        assertThat("Expected status in meta for " + testCase, jsonPath.getInt("meta.status"),
                equalTo(expectedStatusCode));

        if (expectedStatusCode == 200) {
            assertThat("Expected response type code 103 for " + testCase, jsonPath.getInt("meta.responseType.code"),
                    equalTo(103));
            assertThat("Expected success context for " + testCase, jsonPath.getString("meta.responseType.context"),
                    equalTo("Request is successful"));
            assertThat("Expected data array not null for " + testCase, jsonPath.get("data"), notNullValue());
            assertThat("Expected data array not empty for " + testCase, jsonPath.getList("data").size(), greaterThan(0));
            assertThat("Expected columns object not null for " + testCase, jsonPath.get("data[0].columns"),
                    notNullValue());
        } else if (expectedStatusCode == 400) {
            assertThat("Expected data string for " + testCase, jsonPath.get("data"), instanceOf(String.class));
            assertThat("Expected errors array for " + testCase, jsonPath.get("errors"), instanceOf(java.util.List.class));
        }
    }

    @DataProvider(name = "entityColumnsTestData",parallel = true)
    public Object[][] entityColumnsTestData() {
        String validToken = ThreadManager.getOwnerAlbatrossToken();
        return new Object[][] {
                { "testcase1", ENTITY_TIMESHEET_DEAL, validToken, 200,
                        "Entity columns fetched successfully",
                        "privateApi/contractStaffing/getEntityColumnsSuccess.json" },
                { "testcase2", ENTITY_TIMESHEET_DEAL, "InvalidToken", 401, "Unauthorised access",
                        "privateApi/contractStaffing/getEntityColumnsUnauthorized.json" }
        };
    }

    @DataProvider(name = "candidateEntityColumnsTestData",parallel = true)
    public Object[][] candidateEntityColumnsTestData() {
        String validToken = albatrossAuthToken;
        return new Object[][] {
                { "testcase1", ENTITY_CANDIDATES, validToken, 200,
                        "Entity Column Fetched Successfully" },
                { "testcase2", ENTITY_CANDIDATES, "InvalidToken", 401, "Unauthorised access" }
        };
    }
}
