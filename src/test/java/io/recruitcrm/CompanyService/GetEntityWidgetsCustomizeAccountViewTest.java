package io.recruitcrm.CompanyService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetEntityWidgetsCustomizeAccountViewTest extends TestBase {
    String albatrossAuthToken;
    String teamMemberAuthToken;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        teamMemberAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
    }

    @Owner("Harika")
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"company_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_Success(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", companyServiceURL, basePath, albatrossAuthToken, queryParameters, null,
                true);

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/company/GetEntityWidgetsCompanyActivityWidgets.json"));

        // Validate response status
        assertThat("Response status should be 200", response.getStatusCode(), equalTo(200));

        // Validate response structure
        assertThat("Meta object should not be null", response.jsonPath().get("meta"), notNullValue());
        assertThat("Meta status should be 200", response.jsonPath().get("meta.status"), equalTo(200));
        assertThat("Meta message should not be null", response.jsonPath().get("meta.message"), equalTo("Customize Account's View Fetched Successfully"));
        assertThat("Request UUID should not be null", response.jsonPath().get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", response.jsonPath().get("meta.timestamp"), notNullValue());

        // Validate responseType structure
        assertThat("ResponseType should not be null", response.jsonPath().get("meta.responseType"), notNullValue());
        assertThat("Response code should be 103", response.jsonPath().get("meta.responseType.code"), equalTo(103));
        assertThat("Context should match", response.jsonPath().get("meta.responseType.context"), equalTo("Request is successful"));

        // Validate data structure
        assertThat("Data should not be null", response.jsonPath().get("data"), notNullValue());
        
        // Validate data object default values based on view type
        validateDataObjectDefaultValues(response, viewType);
    }

    @Owner("Harika")
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"company_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_WithoutAuth(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", companyServiceURL, basePath, null, queryParameters, null, true);

        assertThat("Response status should be 401 for missing auth", response.getStatusCode(), equalTo(401));
        assertThat("Error message should indicate unauthorized access", response.jsonPath().get("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Harika")
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"company_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_InvalidAuth(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", companyServiceURL, basePath, "InvalidToken", queryParameters, null,
                true);

        assertThat("Response status should be 401 for invalid auth", response.getStatusCode(), equalTo(401));
        assertThat("Error message should indicate unauthorized access", response.jsonPath().get("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Harika")
    @Test(groups = {"company_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_MissingViewType() {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();

        Response response = RestClient.doGet("JSON", companyServiceURL, basePath, albatrossAuthToken, queryParameters, null,
                true);

        assertThat("Response status should be 400 for missing viewType", response.getStatusCode(), equalTo(400));
        validateInvalidResponse(response);
    }

    @Owner("Harika")
    @Test(groups = {"company_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_EmptyViewType() {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", "");

        Response response = RestClient.doGet("JSON", companyServiceURL, basePath, albatrossAuthToken, queryParameters, null,
                true);

        assertThat("Response status should be 400 for Empty viewType", response.getStatusCode(), equalTo(400));
        validateInvalidParameterResponse(response);
    }

    @Owner("Harika")
    @Test(dataProvider = "invalidViewTypeDataProvider", groups = {"company_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_InvalidViewType(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", companyServiceURL, basePath, albatrossAuthToken, queryParameters, null,
                true);

        assertThat("Response status should be 400 for Invalid viewType", response.getStatusCode(), equalTo(400));
        validateInvalidParameterResponse(response);
    }

    @Owner("Harika")
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"company_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_InvalidEndpoint(String viewType) {
        String basePath = "entity-widgets/customize-account-views"; // Incorrect endpoint
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", companyServiceURL, basePath, albatrossAuthToken, queryParameters, null,
                true);

        assertThat("Response status should be 404 for invalid endpoint", response.getStatusCode(), equalTo(404));
        assertThat("Error should indicate not found", response.jsonPath().get("error"), equalTo("Not Found"));
    }

    @Owner("Harika")
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"company_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_WrongMethod(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doPost("JSON", companyServiceURL, basePath, albatrossAuthToken, queryParameters, true,
                null);

        assertThat("Response status should be 405 for wrong method", response.getStatusCode(), equalTo(405));
        assertThat("Error should indicate method not allowed", response.jsonPath().get("error"), equalTo("Method Not Allowed"));
    }

    @Owner("Harika")
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"company_service", "nightly-build"})
    public void verifyOtherUserCannotGetWidgetsCustomizeAccountView(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", companyServiceURL, basePath, teamMemberAuthToken, queryParameters, null, true);

        assertThat("Response status should be 401 for missing auth", response.getStatusCode(), equalTo(401));
        assertThat("Error message should indicate unauthorized access", response.jsonPath().get("errors[0].message"), equalTo("Unauthorized"));
    }

    public void validateInvalidResponse(Response response){
        assertThat("ResponseType should not be null", response.jsonPath().get("meta.responseType"), notNullValue());
        assertThat("Response code should be 101", response.jsonPath().get("meta.responseType.code"), equalTo(101));
        assertThat("Error context should indicate processing error", response.jsonPath().get("meta.responseType.context"), equalTo("Error while processing request"));

        List<String> errorMessages = response.jsonPath().getList("errors.message");
        assertThat("Should contain exactly 2 error messages", errorMessages.size(), equalTo(2));
        assertThat("Should contain 'ViewType is not valid.' error message", errorMessages, hasItem("ViewType is not valid."));
        assertThat("Should contain 'Query parameter viewType cannot be null.' error message", errorMessages, hasItem("Query parameter viewType cannot be null."));

        List<Integer> errorCodes = response.jsonPath().getList("errors.errorType.code");
        List<String> errorContexts = response.jsonPath().getList("errors.errorType.context");

        assertThat("Should contain exactly 2 error codes", errorCodes.size(), equalTo(2));
        assertThat("All error codes should be 201", errorCodes, everyItem(equalTo(201)));

        assertThat("Should contain exactly 2 error contexts", errorContexts.size(), equalTo(2));
        assertThat("All error contexts should be 'Validation Error'", errorContexts, everyItem(equalTo("Validation Error")));
    }

    public void validateInvalidParameterResponse(Response response){
        assertThat("ResponseType should not be null", response.jsonPath().get("meta.responseType"), notNullValue());
        assertThat("Response code should be 101", response.jsonPath().get("meta.responseType.code"), equalTo(101));
        assertThat("Error context should indicate processing error", response.jsonPath().get("meta.responseType.context"), equalTo("Error while processing request"));

        assertThat("Should contain exactly 2 error messages", response.jsonPath().get("errors[0].message"), equalTo("ViewType is not valid."));
        assertThat("Should contain exactly 2 error messages", response.jsonPath().get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("Should contain exactly 2 error messages", response.jsonPath().get("errors[0].errorType.code"), equalTo(201));
    }


    private void validateDataObjectDefaultValues(Response response, String viewType) {
        JsonPath jsonPath = response.jsonPath();

        // Common validations
        assertThat("View should not be null", jsonPath.get("data.view"), notNullValue());
        assertThat("Hidden array should be empty", jsonPath.getList("data.view.hidden").size(), equalTo(0));
        assertThat("ViewType should be account_view", jsonPath.get("data.viewType"), equalTo("account_view"));

        // View-specific visible array validations
        Map<String, List<Integer>> expectedVisibleIds = new HashMap<>();
        expectedVisibleIds.put("company_widgets",
                IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList()));
        expectedVisibleIds.put("company_activity_widgets",
                IntStream.rangeClosed(1, 4).boxed().collect(Collectors.toList()));


        if (expectedVisibleIds.containsKey(viewType)) {
            validateVisibleArray(jsonPath, expectedVisibleIds.get(viewType));
        }
    }

    private void validateVisibleArray(JsonPath jsonPath, List<Integer> expectedIds) {
        List<Map<String, Object>> visibleItems = jsonPath.getList("data.view.visible");
        List<Integer> actualIds = jsonPath.getList("data.view.visible.id");

        assertThat("Visible array size mismatch", visibleItems.size(), equalTo(expectedIds.size()));
        assertThat("Visible array IDs mismatch", actualIds, containsInAnyOrder(expectedIds.toArray()));
    }

    @DataProvider(parallel = true)
    public Object[][] invalidViewTypeDataProvider() {
        return new Object[][] {
                { "invalid_view_type" },
                { "activity_widgets" },
                { "12345" },
                { "@#$@" },
                { "null" },
                { "undefined" }
        };
    }


    @DataProvider(parallel = true)
    public Object[][] validViewTypeDataProvider() {
        return new Object[][] {
                { "company_widgets" },
                { "company_activity_widgets" }
        };
    }
}

