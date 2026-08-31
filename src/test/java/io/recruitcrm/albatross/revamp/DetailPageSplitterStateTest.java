package io.recruitcrm.albatross.revamp;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

import static org.hamcrest.MatcherAssert.assertThat;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@AccountType("Business|AlbatrossTkn|automationForRevamp")
public class DetailPageSplitterStateTest extends TestBase {

    private static final String BASE_PATH = "detail-page-splitter/state";
    private static final double DETAIL_PAGE_SPLITTER_POINTS = 59.10359634997316;
    private static final double ACTIVITY_SIDEBAR_SPLITTER_POINTS = 40.89640365002684;

    private String albatrossTkn;
    private int accountId;
    private int userId;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        accountId = ThreadManager.getAccount().getAccountId();
        userId = ThreadManager.getOwner().getUserId();
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetDetailPageSplitterStateSuccess() {
        Response response = RestClient.doGet("JSON", albatrossURL, BASE_PATH, albatrossTkn, null, null, true);

        response.then().statusCode(200);

        response.then().body("silent_progress", is(true));
        response.then().body("message", equalTo(""));
        response.then().body("message_type", equalTo("is-success"));
        response.then().body("status", equalTo("success"));
        response.then().body("data.user_id", equalTo(userId));
        response.then().body("data.account_id", equalTo(accountId));
        response.then().body("data.splitter_state.detail_page_splitter_points", notNullValue());
        response.then().body("data.splitter_state.activity_sidebar_splitter_points", notNullValue());
        response.then().body(matchesJsonSchemaInClasspath("privateApi/revamp/detailPageSplitterState.json"));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "updateSplitterStateSuccessData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailPageSplitterStateSuccess(String testDescription, double detailPagePoints, double activitySidebarPoints) {
        String requestBody = buildSplitterStateRequest(detailPagePoints, activitySidebarPoints).toString();
        Response response = RestClient.doPost("JSON", albatrossURL, BASE_PATH, albatrossTkn, null, true, requestBody);

        response.then().statusCode(200);
        response.then().body(matchesJsonSchemaInClasspath("privateApi/revamp/detailPageSplitterState.json"));
        response.then().body("silent_progress", is(true));
        response.then().body("message", equalTo("Splitter state updated successfully."));
        response.then().body("message_type", equalTo("is-success"));
        response.then().body("status", equalTo("success"));
        response.then().body("data.user_id", equalTo(userId));
        response.then().body("data.account_id", equalTo(accountId));

        JsonPath jp = response.jsonPath();
        assertThat("detail_page_splitter_points should match request for " + testDescription, jp.getDouble("data.splitter_state.detail_page_splitter_points"), closeTo(detailPagePoints, 0.001));
        assertThat("activity_sidebar_splitter_points should match request for " + testDescription, jp.getDouble("data.splitter_state.activity_sidebar_splitter_points"), closeTo(activitySidebarPoints, 0.001));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "updateSplitterStateNegativeData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailPageSplitterStateNegative(String testDescription, String requestBody, int expectedStatusCode, String expectedMessageFragment) {
        Response response = RestClient.doPost("JSON", albatrossURL, BASE_PATH, albatrossTkn, null, true, requestBody);

        response.then().statusCode(expectedStatusCode);
        response.then().body("message", containsString(expectedMessageFragment));
        response.then().body(matchesJsonSchemaInClasspath("privateApi/revamp/detailPageSplitterStateError.json"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testGetDetailPageSplitterStateWithoutAuth() {
        Response response = RestClient.doGet("JSON", albatrossURL, BASE_PATH, null, null, null, true);

        response.then().statusCode(401);
        response.then().body("error", equalTo("Unauthorized"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailPageSplitterStateWithoutAuth() {
        JSONObject requestBody = buildSplitterStateRequest(DETAIL_PAGE_SPLITTER_POINTS, ACTIVITY_SIDEBAR_SPLITTER_POINTS);

        Response response = RestClient.doPost("JSON", albatrossURL, BASE_PATH, null, null, true, requestBody.toString());

        response.then().statusCode(401);
        response.then().body("error", equalTo("Unauthorized"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testUpdateDetailPageSplitterStateInvalidAuth() {
        JSONObject requestBody = buildSplitterStateRequest(DETAIL_PAGE_SPLITTER_POINTS, ACTIVITY_SIDEBAR_SPLITTER_POINTS);

        Response response = RestClient.doPost("JSON", albatrossURL, BASE_PATH, albatrossTkn + "invalid_token", null, true, requestBody.toString());

        response.then().statusCode(401);
        response.then().body("error", equalTo("Unauthorized"));
    }

    private JSONObject buildSplitterStateRequest(double detailPagePoints, double activitySidebarPoints) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("detail_page_splitter_points", detailPagePoints);
        requestBody.put("activity_sidebar_splitter_points", activitySidebarPoints);
        return requestBody;
    }

    @DataProvider(name = "updateSplitterStateSuccessData")
    public Object[][] updateSplitterStateSuccessData() {
        return new Object[][] {
                {"Default fractional split", DETAIL_PAGE_SPLITTER_POINTS, ACTIVITY_SIDEBAR_SPLITTER_POINTS},
                {"Minimum detail page points", 0.0, 100.0},
                {"Maximum detail page points", 100.0, 0.0},
                {"Equal split", 50.0, 50.0},
                {"Low detail page with high precision", 0.01, 99.99},
                {"High detail page with high precision", 99.99, 0.01},
        };
    }

    @DataProvider(name = "updateSplitterStateNegativeData")
    public Object[][] updateSplitterStateNegativeData() {
        return new Object[][] {
                {"Negative detail page points", buildSplitterStateRequest(-1, 50).toString(), 422, "must be at least 0"},
                {"Negative activity sidebar points", buildSplitterStateRequest(50, -1).toString(), 422, "must be at least 0"},
                {"Detail page points over 100", buildSplitterStateRequest(101, 0).toString(), 422, "must not be greater than 100"},
                {"Activity sidebar points over 100", buildSplitterStateRequest(0, 101).toString(), 422, "must not be greater than 100"},
                {"Both points over 100", buildSplitterStateRequest(150, 150).toString(), 422, "must not be greater than 100"},
                {"Empty request body", "{}", 422, "field is required"},
                {"Invalid string for detail page points", "{\"detail_page_splitter_points\":\"abc\",\"activity_sidebar_splitter_points\":50}", 422, "must be a number"},
                {"Null detail page points", "{\"detail_page_splitter_points\":null,\"activity_sidebar_splitter_points\":50}", 422, "must be a number"},
                {"Both points negative", buildSplitterStateRequest(-10, -10).toString(), 422, "must be at least 0"},
        };
    }
}