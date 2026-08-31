package io.recruitcrm.CompanyService;

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
public class CompanyCustomViewTest extends TestBase {

    // Constants
    private Map<String, String> albatrossTknMap = new HashMap<>();
    private Map<String, Integer> userIdsMap = new HashMap<>();
    private static final String USER_VIEW = "user-view";
    private static final String ACCOUNT_VIEW = "account-view";
    private static final String BASE_PATH_USER_VIEW = "custom-view/" + USER_VIEW;
    private static final String BASE_PATH_ACCOUNT_VIEW = "custom-view/" + ACCOUNT_VIEW;
    private static final int COMPANY_ENTITY_ID = 3;
    
    // Schema Paths
    private static final String SCHEMA_CUSTOM_VIEW_RESPONSE = "schemaValidation/customViewResponse.json";
    private static final String SCHEMA_CUSTOM_VIEW_GET_RESPONSE = "schemaValidation/customViewGetResponse.json";
    private static final String SCHEMA_UNAUTHORIZED_401 = "schemaValidation/unauthorized401Response.json";

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        setupTokenAndUserIdSafely("Owner", "AccountOwner");
        setupTokenAndUserIdSafely("TeamMember", "TeamMember");
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
        request.setEntityId(COMPANY_ENTITY_ID);
        request.setIsDetailPage(true);
        request.setDetailActions(actions);
        request.setDetailActionsLocked(locked);
        return request;
    }

    private Map<String, String> createDetailPageQueryParams() {
        Map<String, String> params = new HashMap<>();
        params.put("entityId", String.valueOf(COMPANY_ENTITY_ID));
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
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
    }

    public List<Integer> getUserView(String token) {
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH_USER_VIEW, token, createDetailPageQueryParams(), null, true);
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("User View Fetched Successfully."));
        return response.jsonPath().getList("data.listActions.setting.id");
    }

    // ==================== Update User View Tests ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCompanyDetailActionUserView_Success() {
        getUserView(albatrossTknMap.get("TeamMember"));

        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 5, 6, 3, 4, 7), 0);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("TeamMember"), null, true, request);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Updated User View Successfully.");

        Response getUserViewResponse = RestClient.doGet("JSON", companyServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("TeamMember"), createDetailPageQueryParams(), null, true);
        Assert.assertEquals(getUserViewResponse.getStatusCode(), 200);
        Assert.assertEquals(getUserViewResponse.jsonPath().get("meta.message"), "User View Fetched Successfully.");

        // Restrict with account view
        CustomViewRequest accountRequest = createDetailViewRequest(Arrays.asList(1, 2, 3, 4, 5, 6, 7), 1);
        Response accountResponse = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, accountRequest);
        Assert.assertEquals(accountResponse.getStatusCode(), 200);
        Assert.assertEquals(accountResponse.jsonPath().get("meta.message"), "Updated Account View Successfully.");

        List<Integer> userView = getUserView(albatrossTknMap.get("TeamMember"));
        Assert.assertEquals(userView.size(), 7, "Expected 7 bulk actions");
        Assert.assertEquals(userView, Arrays.asList(1, 2, 3, 4, 5, 6, 7), "User view is not replaced with the account view");
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCompanyDetailActionUserView_WithoutAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 0);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_USER_VIEW, "", null, true, request);

        assertUnauthorisedAccess(response);
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCompanyDetailActionUserView_InvalidAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 0);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner") + "1234", null, true, request);

        assertUnauthorizedAction(response);
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCompanyDetailActionUserView_RestrictedForLockedUser() {
        getUserView(albatrossTknMap.get("AccountOwner"));
        
        // Lock the account view
        CustomViewRequest accountRequest = createDetailViewRequest(Arrays.asList(1, 2, 3, 4), 1);
        Response accountResponse = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, accountRequest);
        assertThat(accountResponse.getStatusCode(), is(200));
        assertThat(accountResponse.jsonPath().get("meta.message"), is("Updated Account View Successfully."));

        // Try to update locked view as team member
        CustomViewRequest userRequest = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 0);
        userRequest.setDetailActionsLocked(null);
        Response userResponse = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("TeamMember"), null, true, userRequest);
        
        assertUnauthorizedAction(userResponse);
    }

    // ==================== Update Account View Tests ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCompanyDetailActionAccountView_Success() {
        getUserView(albatrossTknMap.get("AccountOwner"));
        
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 1);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, request);
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("Updated Account View Successfully."));

        Response getResponse = RestClient.doGet("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), createDetailPageQueryParams(), null, true);
        assertThat(getResponse.getStatusCode(), is(200));
        assertThat(getResponse.jsonPath().get("meta.message"), is("Account View Fetched Successfully."));
        
        List<Map<String, Object>> settingActions = getResponse.jsonPath().getList("data.listActions.setting");
        assertThat("Expected 4 detail actions", settingActions.size(), is(4));

        List<Integer> userView = getUserView(albatrossTknMap.get("TeamMember"));
        assertThat("Expected 4 detail actions", userView.size(), is(4));
        assertThat("User view should match account view", userView, is(Arrays.asList(2, 1, 4, 3)));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCompanyDetailActionAccountView_WithoutAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 1);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, "", null, true, request);

        assertUnauthorisedAccess(response);
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCompanyDetailActionAccountView_InvalidAuth() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 1);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner") + "1234", null, true, request);

        assertUnauthorizedAction(response);
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void verifyOtherUserCannotUpdateAccountView() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(2, 1, 4, 3), 1);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("TeamMember"), null, true, request);

        assertUnauthorizedAction(response);
    }

    // ==================== Custom View Update Tests with Schema Validation ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCustomViewUserView() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(1, 2, 4, 5), 0);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner"), null, true, request);

        response.then().statusCode(200);
        response.then().body("meta.message", is("Updated User View Successfully."));
        response.then().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCustomViewAccountView() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(1, 2, 3, 4), 0);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, request);

        response.then().statusCode(200);
        response.then().body("meta.message", is("Updated Account View Successfully."));
        response.then().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCustomViewAccountViewWithIncorrectToken() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(1, 2, 3, 4), 0);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner") + "1234", null, true, request);

        response.then().statusCode(401);
        response.then().body("meta.message", is("Unauthorised access"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCustomViewUserViewWithEmptyBody() {
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner"), null, true, null);

        response.then().statusCode(400);
        response.then().body("error", is("Bad Request"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCustomViewAccountViewWithEmptyBody() {
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), null, true, null);

        response.then().statusCode(400);
        response.then().body("error", is("Bad Request"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void updateCustomViewUserViewWithIncorrectToken() {
        CustomViewRequest request = createDetailViewRequest(Arrays.asList(1, 2, 4, 5), 0);
        Response response = RestClient.doPut("JSON", companyServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner") + "1234", null, true, request);

        response.then().statusCode(401);
        response.then().body("meta.message", is("Unauthorised access"));
    }

    // ==================== Get View Tests ====================

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyDetailActionUserView_Success() {
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner"), createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("User View Fetched Successfully."));
        assertThat("Mismatch in updatedBy field", response.jsonPath().getInt("data.listActions.meta.updatedBy"), is(userIdsMap.get("AccountOwner")));

        List<Map<String, Object>> settingActions = response.jsonPath().getList("data.listActions.setting");
        assertThat("Expected 8 default detail actions", settingActions.size(), is(8));
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_GET_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyDetailActionUserView_WithoutAuth() {
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH_USER_VIEW, "", createDetailPageQueryParams(), null, true);

        assertUnauthorisedAccess(response);
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyDetailActionUserView_InvalidAuth() {
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH_USER_VIEW, albatrossTknMap.get("AccountOwner") + "1234", createDetailPageQueryParams(), null, true);

        assertUnauthorizedAction(response);
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyDetailActionAccountView_Success() {
        getUserView(albatrossTknMap.get("AccountOwner"));
        
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner"), createDetailPageQueryParams(), null, true);

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("Account View Fetched Successfully."));
        assertThat("Mismatch in updatedBy field", response.jsonPath().getInt("data.listActions.meta.updatedBy"), is(userIdsMap.get("AccountOwner")));
        assertThat("detailActionsLocked should be 0", response.jsonPath().getInt("data.detailActionsLocked"), is(0));

        List<Map<String, Object>> settingActions = response.jsonPath().getList("data.listActions.setting");
        assertThat("Expected 8 default detail actions", settingActions.size(), is(8));
        response.then().assertThat().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_GET_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyDetailActionAccountView_WithoutAuth() {
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, "", createDetailPageQueryParams(), null, true);

        assertUnauthorisedAccess(response);
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyDetailActionAccountView_InvalidAuth() {
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH_ACCOUNT_VIEW, albatrossTknMap.get("AccountOwner") + "1234", createDetailPageQueryParams(), null, true);

        assertUnauthorizedAction(response);
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    // ==================== Data-Driven Tests ====================

    @Owner("Ajendra Singh")
    @Test(dataProvider = "customViewTestData", groups = {"company_service", "nightly-build"})
    public void getCustomView(String endpoint, String expectedMessage) {
        getUserView(albatrossTknMap.get("AccountOwner"));
        
        Response response = RestClient.doGet("JSON", companyServiceURL, "custom-view/" + endpoint, albatrossTknMap.get("AccountOwner"), createDetailPageQueryParams(), null, true);

        response.then().statusCode(200);
        response.then().body("meta.message", is(expectedMessage));
        response.then().body(matchesJsonSchemaInClasspath(SCHEMA_CUSTOM_VIEW_GET_RESPONSE));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "customViewTestDataInvalidAuthToken", groups = {"company_service", "nightly-build"})
    public void getCustomViewInvalidAuthToken(String endpoint) {
        Response response = RestClient.doGet("JSON", companyServiceURL, "custom-view/" + endpoint, albatrossTknMap.get("AccountOwner") + "1234", createDetailPageQueryParams(), null, true);

        response.then().statusCode(401);
        response.then().body("meta.message", is("Unauthorised access"));
    }

    // ==================== Data Providers ====================

    @DataProvider(name = "customViewTestData", parallel = true)
    public Object[][] getCustomViewTestData() {
        return new Object[][] {
            {USER_VIEW, "User View Fetched Successfully."},
            {ACCOUNT_VIEW, "Account View Fetched Successfully."}
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
