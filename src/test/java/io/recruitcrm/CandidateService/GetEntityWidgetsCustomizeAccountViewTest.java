package io.recruitcrm.CandidateService;

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
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"candidate_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_Success(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters, null,
                true);


        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetEntityWidgetsCandidateActivityWidgets.json"));

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
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"candidate_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_WithoutAuth(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, null, queryParameters, null, true);

        assertThat("Response status should be 401 for missing auth", response.getStatusCode(), equalTo(401));
        assertThat("Error message should indicate unauthorized access", response.jsonPath().get("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Harika")
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"candidate_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_InvalidAuth(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, "InvalidToken", queryParameters, null,
                true);

        assertThat("Response status should be 401 for invalid auth", response.getStatusCode(), equalTo(401));
        assertThat("Error message should indicate unauthorized access", response.jsonPath().get("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_MissingViewType() {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters, null,
                true);

        assertThat("Response status should be 400 for missing viewType", response.getStatusCode(), equalTo(400));
        assertThat("Error should indicate invalid parameter", response.jsonPath().get("viewType"), equalTo("ViewType is not valid."));

    }

    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_EmptyViewType() {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", "");

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters, null,
                true);

        assertThat("Response status should be 400 for Empty viewType", response.getStatusCode(), equalTo(400));
        assertThat("Error should indicate invalid parameter", response.jsonPath().get("viewType"), equalTo("ViewType is not valid."));
    }

    @Owner("Harika")
    @Test(dataProvider = "invalidViewTypeDataProvider", groups = {"candidate_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_InvalidViewType(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters, null,
                true);

        assertThat("Response status should be 400 for Invalid viewType", response.getStatusCode(), equalTo(400));
        assertThat("Error should indicate invalid parameter", response.jsonPath().get("viewType"), equalTo("ViewType is not valid."));
    }

    @Owner("Harika")
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"candidate_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_InvalidEndpoint(String viewType) {
        String basePath = "entity-widgets/customize-account-views"; // Incorrect endpoint
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters, null,
                true);

        assertThat("Response status should be 404 for invalid endpoint", response.getStatusCode(), equalTo(404));
        assertThat("Error should indicate not found", response.jsonPath().get("error"), equalTo("Not Found"));
    }

    @Owner("Harika")
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"candidate_service", "nightly-build"})
    public void testEntityWidgetsCustomizeAccountView_WrongMethod(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doPost("JSON", candidatesURL, basePath, albatrossAuthToken, queryParameters, true,
                null);

        assertThat("Response status should be 405 for wrong method", response.getStatusCode(), equalTo(405));
        assertThat("Error should indicate method not allowed", response.jsonPath().get("error"), equalTo("Method Not Allowed"));
    }

    @Owner("Harika")
    @Test(dataProvider = "validViewTypeDataProvider", groups = {"candidate_service", "nightly-build"})
    public void verifyOtherUserCannotGetWidgetsCustomizeAccountView(String viewType) {
        String basePath = "entity-widgets/customize-account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", viewType);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, teamMemberAuthToken, queryParameters, null, true);

        assertThat("Response status should be 401 for missing auth", response.getStatusCode(), equalTo(401));
        assertThat("Error message should indicate unauthorized access", response.jsonPath().get("meta.message"), equalTo("Unauthorised access"));
    }


    private void validateDataObjectDefaultValues(Response response, String viewType) {
        JsonPath jsonPath = response.jsonPath();

        // Common validations
        assertThat("View should not be null", jsonPath.get("data.view"), notNullValue());
        assertThat("Hidden array should be empty", jsonPath.getList("data.view.hidden").size(), equalTo(0));
        assertThat("ViewType should be account_view", jsonPath.get("data.viewType"), equalTo("account_view"));

        // View-specific visible array validations
        Map<String, List<Integer>> expectedVisibleIds = new HashMap<>();
        expectedVisibleIds.put("candidate_widgets",
                IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
        expectedVisibleIds.put("candidate_activity_widgets",
                IntStream.rangeClosed(1, 5).boxed().collect(Collectors.toList()));


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
                { "candidate_widgets" },
                { "candidate_activity_widgets" }
        };
    }
}
