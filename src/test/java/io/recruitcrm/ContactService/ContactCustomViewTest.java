package io.recruitcrm.ContactService;

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
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class ContactCustomViewTest extends TestBase {

    // Constants
    private Map<String, String> albatrossTknMap = new HashMap<>();
    private Map<String, Integer> userIdsMap = new HashMap<>();
    private static final String USER_VIEW = "user-view";
    private static final String ACCOUNT_VIEW = "account-view";
    private static final String BASE_PATH_USER_VIEW = "custom-view/" + USER_VIEW;
    private static final String BASE_PATH_ACCOUNT_VIEW = "custom-view/" + ACCOUNT_VIEW;
    private static final int CONTACT_ENTITY_ID = 2;
    
    // Schema Paths
    private static final String SCHEMA_CUSTOM_VIEW_RESPONSE = "schemaValidation/customViewResponse.json";
    private static final String SCHEMA_CUSTOM_VIEW_GET_RESPONSE = "schemaValidation/customViewGetResponse.json";
    private static final String SCHEMA_UNAUTHORIZED_401 = "schemaValidation/unauthorized401Response.json";

    @BeforeClass(alwaysRun = true)
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
        request.setEntityId(CONTACT_ENTITY_ID);
        request.setIsDetailPage(true);
        request.setDetailActions(actions);
        request.setDetailActionsLocked(locked);
        return request;
    }

    private Map<String, String> createDetailPageQueryParams() {
        Map<String, String> params = new HashMap<>();
        params.put("entityId", String.valueOf(CONTACT_ENTITY_ID));
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
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH_USER_VIEW, token, createDetailPageQueryParams(), null, true);
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("User View Fetched Successfully."));
        return response.jsonPath().getList("data.listActions.setting.id");
    }

    // ==================== Update User View Tests ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateContactDetailActionUserView_Success() {
        getUserView(albatrossTknMap.get("TeamMember"));

        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3, 5, 6, 7, 8, 9), 0);
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("TeamMember"), null, true, request);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Updated User View Successfully.");

        Response getUserViewResponse = RestClient.doGet("JSON", contactServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("TeamMember"), createDetailPageQueryParams(), null, true);
        Assert.assertEquals(getUserViewResponse.getStatusCode(), 200);
        Assert.assertEquals(getUserViewResponse.jsonPath().get("meta.message"), "User View Fetched Successfully.");

        // Restrict with account view
        CustomViewRequest accountRequest = createDetailViewRequest(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9), 1);
        Response accountResponse = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, accountRequest);
        Assert.assertEquals(accountResponse.getStatusCode(), 200);
        Assert.assertEquals(accountResponse.jsonPath().get("meta.message"), "Updated Account View Successfully.");

        List<Integer> userView = getUserView(albatrossTknMap.get("TeamMember"));
        Assert.assertEquals(userView.size(), 9, "Expected 9 detail actions");
        Assert.assertEquals(userView, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9), "User view is not replaced with the account view");
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateContactDetailActionUserView_WithoutAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 0);
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_USER_VIEW, "", null, true, request);

        assertUnauthorisedAccess(response);
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateContactDetailActionUserView_InvalidAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 0);
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner") + "123", null, true, request);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateContactDetailActionUserView_RestrictedForLockedUser() {
        getUserView(albatrossTknMap.get("AccountOwner"));
        // Restrict the user view
        CustomViewRequest accountViewRequest = new CustomViewRequest();
        accountViewRequest.setEntityId(CONTACT_ENTITY_ID);
        accountViewRequest.setIsDetailPage(true);
        accountViewRequest.setDetailActions(Arrays.asList(1, 2, 3, 4));
        accountViewRequest.setDetailActionsLocked(1);
        Response accountViewResponse = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, accountViewRequest);
        assertThat(accountViewResponse.getStatusCode(), is(200));
        assertThat(accountViewResponse.jsonPath().get("meta.message"), is("Updated Account View Successfully."));

        // Update the user view and assert that the user view is not updated
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(CONTACT_ENTITY_ID);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 3));
        Response userViewResponse = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("TeamMember"), null, true, customViewRequest);
        assertThat(userViewResponse.getStatusCode(), is(401));
        assertThat(userViewResponse.jsonPath().get("errors[0].message"), is("Unauthorized"));
        assertThat(userViewResponse.jsonPath().get("meta.responseType.context"), is("Error while processing request"));
        assertThat(userViewResponse.jsonPath().getInt("meta.responseType.code"), is(101));
    }

    // ==================== Update Account View Tests ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateContactDetailActionAccountView_Success() {
        getUserView(albatrossTknMap.get("AccountOwner"));
        // Update the account view
        CustomViewRequest accountViewRequest = new CustomViewRequest();
        accountViewRequest.setEntityId(CONTACT_ENTITY_ID);
        accountViewRequest.setIsDetailPage(true);
        accountViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 3));
        accountViewRequest.setDetailActionsLocked(1);
        Response accountViewResponse = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, accountViewRequest);
        assertThat(accountViewResponse.getStatusCode(), is(200));
        assertThat(accountViewResponse.jsonPath().get("meta.message"), is("Updated Account View Successfully."));

        // Verify the updated account view
        Response getAccountView = RestClient.doGet("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW + "?entityId=" + CONTACT_ENTITY_ID + "&isDetailPage=true", albatrossTknMap.get("AccountOwner"), createDetailPageQueryParams(), null, true);
        assertThat(getAccountView.getStatusCode(), is(200));
        assertThat(getAccountView.jsonPath().get("meta.message"), is("Account View Fetched Successfully."));
        List<Map<String, Object>> settingActions = getAccountView.jsonPath().getList("data.listActions.setting");
        assertThat("Expected 4 detail actions", settingActions.size(), is(4));
        Integer[] expectedActionIds = {2, 1, 4, 3};
        for (int i = 0; i < expectedActionIds.length; i++) {
            assertThat("Action at position " + (i + 1) + " should have ID " + expectedActionIds[i], settingActions.get(i).get("id"), is(expectedActionIds[i]));
        }

        // Verify that the user view for other user is updated.
        List<Integer> userView = getUserView(albatrossTknMap.get("TeamMember"));
        assertThat("Expected 4 detail actions", userView.size(), is(4));
        List<Integer> expectedActions = Arrays.asList(2, 1, 4, 3);
        assertThat("User view is not updated for other user", userView, is(expectedActions));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateContactDetailActionAccountView_WithoutAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 1);
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, "", null, true, request);

        assertUnauthorisedAccess(response);
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateContactDetailActionAccountView_InvalidAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 1);
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner") + "123", null, true, request);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void verifyOtherUserCannotUpdateAccountView() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 1);
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("TeamMember"), null, true, request);

        assertUnauthorizedAction(response);
    }

    // ==================== Custom View Update Tests with Schema Validation ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateCustomViewUserView() {
        // Create the request payload using POJO
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(CONTACT_ENTITY_ID);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 2, 4, 5));
        customViewRequest.setDetailActionsLocked(0);

        // Make PUT request to update custom view
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner"), null, true, customViewRequest);

        // Verify response status code
        response.then().statusCode(200);

        // Verify response body structure and content
        response.then().body("meta.message", is("Updated User View Successfully."));

        // Validate JSON schema
        response.then().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateCustomViewAccountView() {
        // Create the request payload using POJO
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(CONTACT_ENTITY_ID);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 2, 3, 4));
        customViewRequest.setDetailActionsLocked(0);

        // Make PUT request to update custom view
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, customViewRequest);

        // Verify response status code
        response.then().statusCode(200);

        // Validate JSON schema
        response.then().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_RESPONSE));
        // Verify response body structure and content
        response.then().body("meta.message", is("Updated Account View Successfully."));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateCustomViewAccountViewWithIncorrectToken() {
        // Create the request payload using POJO
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(CONTACT_ENTITY_ID);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 2, 3, 4));
        customViewRequest.setDetailActionsLocked(0);

        // Make PUT request to update custom view with incorrect token
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner") + "1234", null, true, customViewRequest);

        // Verify response status code is 401 Unauthorized
        response.then().statusCode(401);

        // Verify response body structure and content
        response.then().body("meta.message", is("Unauthorised access"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateCustomViewUserViewWithEmptyBody() {
        // Make PUT request to update custom view with empty body (null payload)
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner"), null, true, null);

        // Verify response status code is 400 Bad Request
        response.then().statusCode(400);
        response.then().body("error", is("Bad Request"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateCustomViewAccountViewWithEmptyBody() {
        // Make PUT request to update custom view with empty body (null payload)
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, 
                albatrossTknMap.get("AccountOwner"), null, true, null);

        // Verify response status code is 400 Bad Request
        response.then().statusCode(400);
        response.then().body("error", is("Bad Request"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void updateCustomViewUserViewWithIncorrectToken() {
        // Create the request payload using POJO
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(CONTACT_ENTITY_ID);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 2, 4, 5));

        // Make PUT request to update custom view with incorrect token
        Response response = RestClient.doPut("JSON", contactServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner") + "1234", null, true, customViewRequest);

        // Verify response status code is 401 Unauthorized
        response.then().statusCode(401);

        // Verify response body structure and content
        response.then().body("meta.message", is("Unauthorised access"));
    }

    // ==================== Get View Tests ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactDetailActionUserView_Success() {
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner"), createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("User View Fetched Successfully."));
        int expected = userIdsMap.get("AccountOwner");
        int actual = response.jsonPath().getInt("data.listActions.meta.updatedBy");
        assertThat("Mismatch in updatedBy field", actual, is(expected));

        // Verify the default state of detail actions
        List<Map<String, Object>> settingActions = response.jsonPath().getList("data.listActions.setting");
        assertThat("Expected default detail actions", settingActions.size(), greaterThanOrEqualTo(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_GET_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactDetailActionUserView_WithoutAuth() {
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH_USER_VIEW, "", createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactDetailActionUserView_InvalidAuth() {
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner") + "123", createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactDetailActionAccountView_Success() throws InterruptedException {
        getUserView(albatrossTknMap.get("AccountOwner"));
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(200));
        int expected = userIdsMap.get("AccountOwner");
        int actual = response.jsonPath().getInt("data.listActions.meta.updatedBy");
        assertThat(response.jsonPath().get("meta.message"), is("Account View Fetched Successfully."));
        assertThat("Mismatch in updatedBy field", actual, is(expected));

        // Verify the default state of detail actions
        assertThat("detailActionsLocked should be 0 for default account view", response.jsonPath().getInt("data.detailActionsLocked"), is(0));
        List<Map<String, Object>> settingActions = response.jsonPath().getList("data.listActions.setting");
        assertThat("Expected default detail actions", settingActions.size(), greaterThanOrEqualTo(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_GET_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactDetailActionAccountView_WithoutAuth() { 
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, "", createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactDetailActionAccountView_InvalidAuth() {
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner") + "123", createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    // ==================== Data-Driven Tests ====================

    @Owner("Ajendra Singh")
    @Test(dataProvider = "customViewTestData", groups = {"contact_service", "nightly-build"})
    public void getCustomView(String endpoint, String isDetailPage) {
        getUserView(albatrossTknMap.get("AccountOwner"));
        // Create query parameters
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", String.valueOf(CONTACT_ENTITY_ID));
        queryParameters.put("isDetailPage", isDetailPage);

        // Make GET request to retrieve custom view
        Response response = RestClient.doGet("JSON", contactServiceURL, "custom-view/" + endpoint, albatrossTknMap.get("AccountOwner"), queryParameters, null, true);

        // Verify response status code
        response.then().statusCode(200);

        // Verify response body structure and content based on endpoint
        if (endpoint.equals("account-view")) {
            response.then().body("meta.message", is("Account View Fetched Successfully."));
        } else if (endpoint.equals("user-view")) {
            response.then().body("meta.message", is("User View Fetched Successfully."));
        }

        // Validate JSON schema only when isDetailPage is true
        // When isDetailPage is false, account-view response doesn't include listActions
        if ("true".equals(isDetailPage)) {
            response.then().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_GET_RESPONSE));
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "customViewTestDataInvalidAuthToken", groups = {"contact_service", "nightly-build"})
    public void getCustomViewInvalidAuthToken(String endpoint) {
        // Create query parameters
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", String.valueOf(CONTACT_ENTITY_ID));
        queryParameters.put("isDetailPage", "true");

        // Make GET request to retrieve custom view
        Response response = RestClient.doGet("JSON", contactServiceURL, "custom-view/" + endpoint,
                albatrossTknMap.get("AccountOwner") + "1234", queryParameters, null, true);

        // Verify response status code
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
