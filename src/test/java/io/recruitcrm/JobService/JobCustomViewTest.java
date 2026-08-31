package io.recruitcrm.JobService;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import io.rcrm.api.pojo.candidateService.CustomViewRequest;
import io.rcrm.api.restclient.RestClient;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.Owner;

@AccountType("CrossAccount|automationForRevamp")
public class JobCustomViewTest extends TestBase {

    private Map<String, String> albatrossTknMap = new HashMap<>();
    private Map<String, Integer> userIdsMap = new HashMap<>();
    private static final String USER_VIEW = "user-view";
    private static final String ACCOUNT_VIEW = "account-view";
    private static final String BASE_PATH_USER_VIEW = "custom-view/" + USER_VIEW;
    private static final String BASE_PATH_ACCOUNT_VIEW = "custom-view/" + ACCOUNT_VIEW;
    private static final int JOB_ENTITY_ID = 4;

    private static final String SCHEMA_CUSTOM_VIEW_RESPONSE = "schemaValidation/customViewResponse.json";
    private static final String SCHEMA_CUSTOM_VIEW_GET_RESPONSE = "schemaValidation/customViewGetResponse.json";
    private static final String SCHEMA_UNAUTHORIZED_401 = "schemaValidation/unauthorized401Response.json";

    /** Fixed "random" sequence used to seed and assert order preservation when isDetailPage=true. Valid job action IDs are 1 to 11. */
    private static final List<Integer> EXPECTED_DETAIL_ACTION_ORDER = Arrays.asList(4, 1, 2, 9, 3, 10, 11, 5, 6, 7, 8);

    @BeforeClass
    public void setUp() {
        setupTokenAndUserIdSafely("Owner", "AccountOwner");
        setupTokenAndUserIdSafely("TeamMember", "TeamMember");
        getUserView(albatrossTknMap.get("AccountOwner"));
    }

    private void setupTokenAndUserIdSafely(String role, String mapKey) {
        String[] tokenAndUserId = ThreadManager.getAlbatrossTokenAndUserId(role);
        if (tokenAndUserId != null && tokenAndUserId.length == 2 && tokenAndUserId[1] != null) {
            albatrossTknMap.put(mapKey, tokenAndUserId[0]);
            userIdsMap.put(mapKey, Integer.parseInt(tokenAndUserId[1]));
        }
    }

    // ==================== Helper Methods ====================

    private CustomViewRequest createDetailViewRequest(List<Integer> actions, int locked) {
        CustomViewRequest request = new CustomViewRequest();
        request.setEntityId(JOB_ENTITY_ID);
        request.setIsDetailPage(true);
        request.setDetailActions(actions);
        request.setDetailActionsLocked(locked);
        return request;
    }

    private Map<String, String> createDetailPageQueryParams() {
        Map<String, String> params = new HashMap<>();
        params.put("entityId", String.valueOf(JOB_ENTITY_ID));
        params.put("isDetailPage", "true");
        return params;
    }

    private void assertUnauthorisedAccess(Response response) {
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
    }

    private void assertUnauthorizedAction(Response response) {
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("errors[0].message"), is("Unauthorized"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Error while processing request"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(101));
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_UNAUTHORIZED_401));
    }

    public List<Integer> getUserView(String token) {
        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH_USER_VIEW, token, createDetailPageQueryParams(), null, true);
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("User View Fetched Successfully."));
        return response.jsonPath().getList("data.listActions.setting.id");
    }

    private List<Integer> getDetailActionIdsFromResponse(Response response) {
        List<Object> rawIds = response.jsonPath().getList("data.listActions.setting.id");
        if (rawIds == null) return Collections.emptyList();
        List<Integer> ids = new ArrayList<>(rawIds.size());
        for (Object o : rawIds) {
            if (o instanceof Number) ids.add(((Number) o).intValue());
            else if (o != null) ids.add(Integer.parseInt(o.toString()));
        }
        return ids;
    }

    // ==================== Update User View Tests ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateJobDetailActionUserView_Success() {
        getUserView(albatrossTknMap.get("TeamMember"));

        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3, 5, 6, 7, 8, 9, 10), 0);
        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("TeamMember"), null, true, request);

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Updated User View Successfully.");

        Response getUserViewResponse = RestClient.doGet("JSON", jobServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("TeamMember"), createDetailPageQueryParams(), null, true);
        Assert.assertEquals(getUserViewResponse.getStatusCode(), 200);
        Assert.assertEquals(getUserViewResponse.jsonPath().get("meta.message"), "User View Fetched Successfully.");

        CustomViewRequest accountRequest = createDetailViewRequest(Arrays.asList(2, 1, 5, 4, 3, 11, 6, 9, 8, 7, 10), 1);
        Response accountResponse = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, accountRequest);
        Assert.assertEquals(accountResponse.getStatusCode(), 200);
        Assert.assertEquals(accountResponse.jsonPath().get("meta.message"), "Updated Account View Successfully.");

        List<Integer> userView = getUserView(albatrossTknMap.get("TeamMember"));
        Assert.assertEquals(userView.size(), 11, "Expected 10 detail actions");
        Assert.assertEquals(userView, Arrays.asList(2, 1, 5, 4, 3, 11, 6, 9, 8, 7, 10), "User view is not replaced with the account view");
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateJobDetailActionUserView_WithoutAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(3, 1, 4, 5), 0);
        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_USER_VIEW, "", null, true, request);

        assertUnauthorisedAccess(response);
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateJobDetailActionUserView_InvalidAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(3, 1, 4, 5), 0);
        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner") + "123", null, true, request);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateJobDetailActionUserView_RestrictedForLockedUser() {
        getUserView(albatrossTknMap.get("AccountOwner"));

        CustomViewRequest accountViewRequest = new CustomViewRequest();
        accountViewRequest.setEntityId(JOB_ENTITY_ID);
        accountViewRequest.setIsDetailPage(true);
        accountViewRequest.setDetailActions(Arrays.asList(1, 3, 4, 5));
        accountViewRequest.setDetailActionsLocked(1);
        Response accountViewResponse = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, accountViewRequest);
        assertThat(accountViewResponse.getStatusCode(), is(200));
        assertThat(accountViewResponse.jsonPath().get("meta.message"), is("Updated Account View Successfully."));

        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(JOB_ENTITY_ID);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(3, 1, 4, 5));
        Response userViewResponse = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("TeamMember"), null, true, customViewRequest);
        assertThat(userViewResponse.getStatusCode(), is(401));
        assertThat(userViewResponse.jsonPath().get("errors[0].message"), is("Unauthorized"));
        assertThat(userViewResponse.jsonPath().get("meta.responseType.context"), is("Error while processing request"));
        assertThat(userViewResponse.jsonPath().getInt("meta.responseType.code"), is(101));
    }

    // ==================== Update Account View Tests ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateJobDetailActionAccountView_Success() {
        getUserView(albatrossTknMap.get("AccountOwner"));

        CustomViewRequest accountViewRequest = new CustomViewRequest();
        accountViewRequest.setEntityId(JOB_ENTITY_ID);
        accountViewRequest.setIsDetailPage(true);
        accountViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 5));
        accountViewRequest.setDetailActionsLocked(1);
        Response accountViewResponse = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, accountViewRequest);
        assertThat(accountViewResponse.getStatusCode(), is(200));
        assertThat(accountViewResponse.jsonPath().get("meta.message"), is("Updated Account View Successfully."));

        Response getAccountView = RestClient.doGet("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW + "?entityId=" + JOB_ENTITY_ID + "&isDetailPage=true", albatrossTknMap.get("AccountOwner"), createDetailPageQueryParams(), null, true);
        assertThat(getAccountView.getStatusCode(), is(200));
        assertThat(getAccountView.jsonPath().get("meta.message"), is("Account View Fetched Successfully."));
        List<Map<String, Object>> settingActions = getAccountView.jsonPath().getList("data.listActions.setting");
        assertThat("Expected 4 detail actions", settingActions.size(), is(4));
        Integer[] expectedActionIds = {2, 1, 4, 5};
        for (int i = 0; i < expectedActionIds.length; i++) {
            assertThat("Action at position " + (i + 1) + " should have ID " + expectedActionIds[i], settingActions.get(i).get("id"), is(expectedActionIds[i]));
        }

        List<Integer> userView = getUserView(albatrossTknMap.get("TeamMember"));
        assertThat("Expected 4 detail actions", userView.size(), is(4));
        List<Integer> expectedActions = Arrays.asList(2, 1, 4, 5);
        assertThat("User view is not updated for other user", userView, is(expectedActions));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateJobDetailActionAccountView_WithoutAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(3, 1, 4, 5), 1);
        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, "", null, true, request);

        assertUnauthorisedAccess(response);
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateJobDetailActionAccountView_InvalidAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(3, 1, 4, 5), 1);
        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner") + "123", null, true, request);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void verifyOtherUserCannotUpdateAccountView() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(3, 1, 4, 5), 1);
        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("TeamMember"), null, true, request);

        assertUnauthorizedAction(response);
    }

    // ==================== Custom View Update Tests with Schema Validation ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateCustomViewUserView() {
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(JOB_ENTITY_ID);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 4, 5, 6));
        customViewRequest.setDetailActionsLocked(0);

        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner"), null, true, customViewRequest);

        response.then().statusCode(200);
        response.then().body("meta.message", is("Updated User View Successfully."));
        response.then().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateCustomViewAccountView() {
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(JOB_ENTITY_ID);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 3, 4, 5));
        customViewRequest.setDetailActionsLocked(0);

        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, customViewRequest);

        response.then().statusCode(200);
        response.then().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_RESPONSE));
        response.then().body("meta.message", is("Updated Account View Successfully."));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateCustomViewAccountViewWithIncorrectToken() {
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(JOB_ENTITY_ID);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 3, 4, 5));
        customViewRequest.setDetailActionsLocked(0);

        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner") + "1234", null, true, customViewRequest);

        response.then().statusCode(401);
        response.then().body("meta.message", is("Unauthorised access"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateCustomViewUserViewWithEmptyBody() {
        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner"), null, true, null);

        response.then().statusCode(400);
        response.then().body("error", is("Bad Request"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateCustomViewAccountViewWithEmptyBody() {
        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW,
                albatrossTknMap.get("AccountOwner"), null, true, null);

        response.then().statusCode(400);
        response.then().body("error", is("Bad Request"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void updateCustomViewUserViewWithIncorrectToken() {
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(JOB_ENTITY_ID);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 4, 5, 6));

        Response response = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner") + "1234", null, true, customViewRequest);

        response.then().statusCode(401);
        response.then().body("meta.message", is("Unauthorised access"));
    }

    // ==================== Get View Tests ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobDetailActionUserView_Success() {
        CustomViewRequest seed = new CustomViewRequest();
        seed.setEntityId(JOB_ENTITY_ID);
        seed.setIsDetailPage(true);
        seed.setDetailActions(EXPECTED_DETAIL_ACTION_ORDER);
        seed.setDetailActionsLocked(0);
        Response putResponse = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner"), null, true, seed);
        assertThat(putResponse.getStatusCode(), is(200));
        assertThat(putResponse.jsonPath().get("meta.message"), is("Updated User View Successfully."));

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner"), createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("User View Fetched Successfully."));
        assertThat("When isDetailPage=true, response must contain data.listActions.setting", response.jsonPath().get("data.listActions.setting"), notNullValue());
        assertThat("entityId in response", response.jsonPath().getInt("data.entityId"), is(JOB_ENTITY_ID));

        int expectedUserId = userIdsMap.get("AccountOwner");
        int actualUpdatedBy = response.jsonPath().getInt("data.listActions.meta.updatedBy");
        assertThat("Mismatch in updatedBy field", actualUpdatedBy, is(expectedUserId));

        List<Integer> returnedActionIds = getDetailActionIdsFromResponse(response);
        assertThat("Detail actions count", returnedActionIds.size(), is(EXPECTED_DETAIL_ACTION_ORDER.size()));
        assertThat("Detail action IDs must be returned in the same sequence as sent", returnedActionIds, is(EXPECTED_DETAIL_ACTION_ORDER));

        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_GET_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobDetailActionUserView_WithoutAuth() {
        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH_USER_VIEW, "", createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobDetailActionUserView_InvalidAuth() {
        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner") + "123", createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobDetailActionAccountView_Success() throws InterruptedException {
        getUserView(albatrossTknMap.get("AccountOwner"));

        CustomViewRequest seed = new CustomViewRequest();
        seed.setEntityId(JOB_ENTITY_ID);
        seed.setIsDetailPage(true);
        seed.setDetailActions(EXPECTED_DETAIL_ACTION_ORDER);
        seed.setDetailActionsLocked(0);
        Response putResponse = RestClient.doPut("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, seed);
        assertThat(putResponse.getStatusCode(), is(200));
        assertThat(putResponse.jsonPath().get("meta.message"), is("Updated Account View Successfully."));

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("Account View Fetched Successfully."));
        assertThat("When isDetailPage=true, response must contain data.listActions.setting", response.jsonPath().get("data.listActions.setting"), notNullValue());
        assertThat("entityId in response", response.jsonPath().getInt("data.entityId"), is(JOB_ENTITY_ID));
        assertThat("detailActionsLocked should be 0 for default account view", response.jsonPath().getInt("data.detailActionsLocked"), is(0));

        int expectedUserId = userIdsMap.get("AccountOwner");
        int actualUpdatedBy = response.jsonPath().getInt("data.listActions.meta.updatedBy");
        assertThat("Mismatch in updatedBy field", actualUpdatedBy, is(expectedUserId));

        List<Integer> returnedActionIds = getDetailActionIdsFromResponse(response);
        assertThat("Detail actions count", returnedActionIds.size(), is(EXPECTED_DETAIL_ACTION_ORDER.size()));
        assertThat("Detail action IDs must be returned in the same sequence as sent", returnedActionIds, is(EXPECTED_DETAIL_ACTION_ORDER));

        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_GET_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobDetailActionAccountView_WithoutAuth() {
        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, "", createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobDetailActionAccountView_InvalidAuth() {
        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner") + "123", createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    // ==================== Data-Driven Tests ====================

    @Owner("Ajendra Singh")
    @Test(dataProvider = "customViewTestData", groups = {"job_service", "nightly-build"})
    public void getCustomView(String endpoint, String isDetailPage) {
        getUserView(albatrossTknMap.get("AccountOwner"));

        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", String.valueOf(JOB_ENTITY_ID));
        queryParameters.put("isDetailPage", isDetailPage);

        Response response = RestClient.doGet("JSON", jobServiceURL, "custom-view/" + endpoint, albatrossTknMap.get("AccountOwner"), queryParameters, null, true);

        response.then().statusCode(200);

        if (endpoint.equals("account-view")) {
            response.then().body("meta.message", is("Account View Fetched Successfully."));
        } else if (endpoint.equals("user-view")) {
            response.then().body("meta.message", is("User View Fetched Successfully."));
        }

        if ("true".equals(isDetailPage)) {
            response.then().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_GET_RESPONSE));
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "customViewTestDataInvalidAuthToken", groups = {"job_service", "nightly-build"})
    public void getCustomViewInvalidAuthToken(String endpoint) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", String.valueOf(JOB_ENTITY_ID));
        queryParameters.put("isDetailPage", "true");

        Response response = RestClient.doGet("JSON", jobServiceURL, "custom-view/" + endpoint,
                albatrossTknMap.get("AccountOwner") + "1234", queryParameters, null, true);

        response.then().statusCode(401);
        response.then().body("meta.message", is("Unauthorised access"));
    }

    // ==================== Data Providers ====================

    @DataProvider(name = "customViewTestData", parallel = true)
    public Object[][] getCustomViewTestData() {
        return new Object[][] {
            {"user-view", "true"},
            {"account-view", "true"},
            {"user-view", "false"},
            {"account-view", "false"}
        };
    }

    @DataProvider(name = "customViewTestDataInvalidAuthToken", parallel = true)
    public Object[][] getCustomViewTestDataInvalidAuthToken() {
        return new Object[][] {
            {USER_VIEW},
            {ACCOUNT_VIEW}
        };
    }
}
