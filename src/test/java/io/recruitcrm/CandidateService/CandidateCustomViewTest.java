package io.recruitcrm.CandidateService;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
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
public class CandidateCustomViewTest extends TestBase {

    Map<String, String> albatrossTknMap = new HashMap<>();
    Map<String, Integer> userIdsMap = new HashMap<>();

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

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCandidateDetailActionUserView_Success() throws InterruptedException {
        String basePath = "custom-view/user-view";

        // Update the user view
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(5);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 3, 5, 6, 7, 8, 9, 10));
        customViewRequest.setDetailActionsLocked(0);
        Response response = RestClient.doPut("JSON", candidatesURL, basePath, albatrossTknMap.get("TeamMember"), null, true, customViewRequest);

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("Updated User View Successfully."));

        // Verify the updated user view
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", "true");
        Response getUserView = RestClient.doGet("JSON", candidatesURL, basePath, albatrossTknMap.get("TeamMember"), queryParameters, null, true);
        assertThat(getUserView.getStatusCode(), is(200));

        assertThat(getUserView.jsonPath().get("meta.message"), is("User View Fetched Successfully."));
        List<Map<String, Object>> settingActions = getUserView.jsonPath().getList("data.listActions.setting");  // NEED TO CHANGE THIS TO DETAIL ACTIONS
        assertThat("Expected 10 detail actions", settingActions.size(), is(10));
        Integer[] expectedActionIds = {2, 1, 4, 3, 5, 6, 7, 8, 9, 10};
        for (int i = 0; i < expectedActionIds.length; i++) {
            assertThat("Action at position " + (i + 1) + " should have ID " + expectedActionIds[i], settingActions.get(i).get("id"), is(expectedActionIds[i]));
        }

        // Restrict the user view and assert that the user view is replaced with the account view.
        CustomViewRequest accountViewRequest = new CustomViewRequest();
        accountViewRequest.setEntityId(5);
        accountViewRequest.setIsDetailPage(true);
        accountViewRequest.setDetailActions(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        accountViewRequest.setDetailActionsLocked(1);
        Response accountViewResponse = RestClient.doPut("JSON", candidatesURL, "custom-view/account-view", albatrossTknMap.get("AccountOwner"), null, true, accountViewRequest);
        assertThat(accountViewResponse.getStatusCode(), is(200));
        assertThat(accountViewResponse.jsonPath().get("meta.message"), is("Updated Account View Successfully."));

        // Get the user view for the team member
        List<Integer> userView = getUserView(albatrossTknMap.get("TeamMember"));
        assertThat("Expected 10 detail actions", userView.size(), is(10));
        List<Integer> expectedActions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertThat("User view is not replaced with the account view, even when the user view is restricted", userView, is(expectedActions));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCandidateDetailActionUserView_WithoutAuth() {
        String basePath = "custom-view/user-view";
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(5);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 3));
        customViewRequest.setDetailActionsLocked(0);
        Response response = RestClient.doPut("JSON", candidatesURL, basePath, "", null, true, customViewRequest);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCandidateDetailActionUserView_InvalidAuth() {
        String basePath = "custom-view/user-view";
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(5);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 3));
        customViewRequest.setDetailActionsLocked(0);
        Response response = RestClient.doPut("JSON", candidatesURL, basePath, albatrossTknMap.get("AccountOwner") + "123", null, true, customViewRequest);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCandidateDetailActionUserView_RestrictedForLockedUser() {
        getUserView(albatrossTknMap.get("AccountOwner"));
        // Restrict the user view
        CustomViewRequest accountViewRequest = new CustomViewRequest();
        accountViewRequest.setEntityId(5);
        accountViewRequest.setIsDetailPage(true);
        accountViewRequest.setDetailActions(Arrays.asList(1, 2, 3, 4));
        accountViewRequest.setDetailActionsLocked(1);
        Response accountViewResponse = RestClient.doPut("JSON", candidatesURL, "custom-view/account-view", albatrossTknMap.get("AccountOwner"), null, true, accountViewRequest);
        assertThat(accountViewResponse.getStatusCode(), is(200));
        assertThat(accountViewResponse.jsonPath().get("meta.message"), is("Updated Account View Successfully."));

        // Update the user view and assert that the user view is not updated
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(5);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 3));
        Response userViewResponse = RestClient.doPut("JSON", candidatesURL, "custom-view/user-view", albatrossTknMap.get("TeamMember"), null, true, customViewRequest);
        assertThat(userViewResponse.getStatusCode(), is(401));
        assertThat(userViewResponse.jsonPath().get("errors[0].message"), is("Unauthorized"));
        assertThat(userViewResponse.jsonPath().get("meta.responseType.context"), is("Error while processing request"));
        assertThat(userViewResponse.jsonPath().getInt("meta.responseType.code"), is(101));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCandidateDetailActionAccountView_Success() {
        getUserView(albatrossTknMap.get("AccountOwner"));
        // Update the account view
        CustomViewRequest accountViewRequest = new CustomViewRequest();
        accountViewRequest.setEntityId(5);
        accountViewRequest.setIsDetailPage(true);
        accountViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 3));
        accountViewRequest.setDetailActionsLocked(1);
        Response accountViewResponse = RestClient.doPut("JSON", candidatesURL, "custom-view/account-view", albatrossTknMap.get("AccountOwner"), null, true, accountViewRequest);
        assertThat(accountViewResponse.getStatusCode(), is(200));
        assertThat(accountViewResponse.jsonPath().get("meta.message"), is("Updated Account View Successfully."));

        // Verify the updated account view
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", "true");
        Response getAccountView = RestClient.doGet("JSON", candidatesURL, "custom-view/account-view?entityId=5&isDetailPage=true", albatrossTknMap.get("AccountOwner"), queryParameters, null, true);
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
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCandidateDetailActionAccountView_WithoutAuth() {
        String basePath = "custom-view/account-view";
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(5);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 3));
        customViewRequest.setDetailActionsLocked(1);
        Response response = RestClient.doPut("JSON", candidatesURL, basePath, "", null, true, customViewRequest);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCandidateDetailActionAccountView_InvalidAuth() {
        String basePath = "custom-view/account-view";
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(5);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 3));
        customViewRequest.setDetailActionsLocked(1);
        Response response = RestClient.doPut("JSON", candidatesURL, basePath, albatrossTknMap.get("AccountOwner") + "123", null, true, customViewRequest);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void verifyOtherUserCannotUpdateAccountView() {
        CustomViewRequest accountViewRequest = new CustomViewRequest();
        accountViewRequest.setEntityId(5);
        accountViewRequest.setIsDetailPage(true);
        accountViewRequest.setDetailActions(Arrays.asList(2, 1, 4, 3));
        accountViewRequest.setDetailActionsLocked(1);
        Response accountViewResponse = RestClient.doPut("JSON", candidatesURL, "custom-view/account-view", albatrossTknMap.get("TeamMember"), null, true, accountViewRequest);

        assertThat(accountViewResponse.getStatusCode(), is(401));
        assertThat(accountViewResponse.jsonPath().get("errors[0].message"), is("Unauthorized"));
        assertThat(accountViewResponse.jsonPath().get("meta.responseType.context"), is("Error while processing request"));
        assertThat(accountViewResponse.jsonPath().getInt("meta.responseType.code"), is(101));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCustomViewUserView() {
        // Create the request payload using POJO
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(5);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 2, 4, 5));
        customViewRequest.setDetailActionsLocked(0);

        // Make PUT request to update custom view
        Response response = RestClient.doPut("JSON", candidatesURL, "custom-view/user-view", albatrossTknMap.get("AccountOwner"), null, true, customViewRequest);

        // Verify response status code
        response.then().statusCode(200);

        // Verify response body structure and content
        response.then().body("meta.message", is("Updated User View Successfully."));

        // Validate JSON schema
        response.then().body(matchesJsonSchemaInClasspath("schemaValidation/customViewResponse.json"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCustomViewAccountView() {
        // Create the request payload using POJO
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(5);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 2, 3, 3));
        customViewRequest.setDetailActionsLocked(0);

        // Make PUT request to update custom view
        Response response = RestClient.doPut("JSON", candidatesURL, "custom-view/account-view", albatrossTknMap.get("AccountOwner"), null, true, customViewRequest);

        // Verify response status code
        response.then().statusCode(200);

        // Validate JSON schema
        response.then().body(matchesJsonSchemaInClasspath("schemaValidation/customViewResponse.json"));
        // Verify response body structure and content
        response.then().body("meta.message", is("Updated Account View Successfully."));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCustomViewAccountViewWithIncorrectToken() {
        // Create the request payload using POJO
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(5);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 2, 3, 3));
        customViewRequest.setDetailActionsLocked(0);

        // Make PUT request to update custom view with incorrect token
        Response response = RestClient.doPut("JSON", candidatesURL, "custom-view/account-view", albatrossTknMap.get("AccountOwner") + "1234", null, true, customViewRequest);

        // Verify response status code is 401 Unauthorized
        response.then().statusCode(401);

        // Verify response body structure and content
        response.then().body("meta.message", is("Unauthorised access"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCustomViewUserViewWithEmptyBody() {
        // Make PUT request to update custom view with empty body (null payload)
        Response response = RestClient.doPut("JSON", candidatesURL, "custom-view/user-view", albatrossTknMap.get("AccountOwner"), null, true, null);

        // Verify response status code is 400 Bad Request
        response.then().statusCode(400);
        response.then().body("error", is("Bad Request"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCustomViewAccountViewWithEmptyBody() {
        // Make PUT request to update custom view with empty body (null payload)
        Response response = RestClient.doPut("JSON", candidatesURL, "custom-view/account-view", 
                albatrossTknMap.get("AccountOwner"), null, true, null);

        // Verify response status code is 400 Bad Request
        response.then().statusCode(400);
        response.then().body("error", is("Bad Request"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void updateCustomViewUserViewWithIncorrectToken() {
        // Create the request payload using POJO
        CustomViewRequest customViewRequest = new CustomViewRequest();
        customViewRequest.setEntityId(5);
        customViewRequest.setIsDetailPage(true);
        customViewRequest.setDetailActions(Arrays.asList(1, 2, 4, 5));

        // Make PUT request to update custom view with incorrect token
        Response response = RestClient.doPut("JSON", candidatesURL, "custom-view/user-view", albatrossTknMap.get("AccountOwner") + "1234", null, true, customViewRequest);

        // Verify response status code is 401 Unauthorized
        response.then().statusCode(401);

        // Verify response body structure and content
        response.then().body("meta.message", is("Unauthorised access"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateDetailActionUserView_Success() {
        String basePath = "custom-view/user-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", "true");

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossTknMap.get("AccountOwner"), queryParameters, null, true);

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("User View Fetched Successfully."));
        int expected = userIdsMap.get("AccountOwner");
        int actual = response.jsonPath().getInt("data.listActions.meta.updatedBy");
        assertThat("Mismatch in updatedBy field", actual, is(expected));

        // Verify the default state of detail actions
        List<Map<String, Object>> settingActions = response.jsonPath().getList("data.listActions.setting");
        assertThat("Expected 10 default detail actions", settingActions.size(), is(10));

        Integer[] expectedActionIds = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int i = 0; i < expectedActionIds.length; i++) {
            assertThat("Action at position " + (i + 1) + " should have ID " + expectedActionIds[i], settingActions.get(i).get("id"), is(expectedActionIds[i]));
        }
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/customViewGetResponse.json"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateDetailActionUserView_WithoutAuth() {
        String basePath = "custom-view/user-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", "true");
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, "", queryParameters, null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateDetailActionUserView_InvalidAuth() {
        String basePath = "custom-view/user-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", "true");
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossTknMap.get("AccountOwner") + "123", queryParameters, null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateDetailActionAccountView_Success() throws InterruptedException {
        getUserView(albatrossTknMap.get("AccountOwner"));
        String basePath = "custom-view/account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", "true");

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossTknMap.get("AccountOwner"), queryParameters, null, true);

        assertThat(response.getStatusCode(), is(200));
        int expected = userIdsMap.get("AccountOwner");
        int actual = response.jsonPath().getInt("data.listActions.meta.updatedBy");
        assertThat(response.jsonPath().get("meta.message"), is("Account View Fetched Successfully."));
        assertThat("Mismatch in updatedBy field", actual, is(expected));

        // Verify the default state of detail actions
        assertThat("detailActionsLocked should be 0 for default account view", response.jsonPath().getInt("data.detailActionsLocked"), is(0));
        List<Map<String, Object>> settingActions = response.jsonPath().getList("data.listActions.setting");
        assertThat("Expected 10 default detail actions", settingActions.size(), is(10));

        Integer[] expectedActionIds = {1, 2, 3, 4};
        for (int i = 0; i < expectedActionIds.length; i++) {
            assertThat("Action at position " + (i + 1) + " should have ID " + expectedActionIds[i], settingActions.get(i).get("id"), is(expectedActionIds[i]));
        }
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/customViewGetResponse.json"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateDetailActionAccountView_WithoutAuth() { 
        String basePath = "custom-view/account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", "true");
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, "", queryParameters, null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Missing bearer token in header"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateDetailActionAccountView_InvalidAuth() {
        String basePath = "custom-view/account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", "true");
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossTknMap.get("AccountOwner") + "123", queryParameters, null, true);

        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("meta.message"), is("Unauthorised access"));
        assertThat(response.jsonPath().get("meta.responseType.context"), is("Warning"));
        assertThat(response.jsonPath().getInt("meta.responseType.code"), is(104));
        assertThat(response.jsonPath().get("data"), is("Invalid or expired token"));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "customViewTestData", groups = {"candidate_service", "nightly-build"})
    public void getCustomView(String endpoint, String isDetailPage) {
        getUserView(albatrossTknMap.get("AccountOwner"));
        // Create query parameters
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", isDetailPage);

        // Make GET request to retrieve custom view
        Response response = RestClient.doGet("JSON", candidatesURL, "custom-view/" + endpoint, albatrossTknMap.get("AccountOwner"), queryParameters, null, true);

        // Verify response status code
        response.then().statusCode(200);

        // Verify response body structure and content based on endpoint
        if (endpoint.equals("account-view")) {
            response.then().body("meta.message", is("Account View Fetched Successfully."));
        } else if (endpoint.equals("user-view")) {
            response.then().body("meta.message", is("User View Fetched Successfully."));
        }

        // Validate JSON schema
        response.then().body(matchesJsonSchemaInClasspath("schemaValidation/customViewGetResponse.json"));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "customViewTestDataInvalidAuthToken", groups = {"candidate_service", "nightly-build"})
    public void getCustomViewInvalidAuthToken(String endpoint) {
        // Create query parameters
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", "true");

        // Make GET request to retrieve custom view
        Response response = RestClient.doGet("JSON", candidatesURL, "custom-view/" + endpoint,
                albatrossTknMap.get("AccountOwner") + "1234", queryParameters, null, true);

        // Verify response status code
        response.then().statusCode(401);
        response.then().body("meta.message", is("Unauthorised access"));
    }

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
            {"user-view"},
            {"account-view"}
        };
    }

    public List<Integer> getUserView(String token) {
        String basePath = "custom-view/user-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "5");
        queryParameters.put("isDetailPage", "true");
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, token, queryParameters, null, true);
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("meta.message"), is("User View Fetched Successfully."));
        return response.jsonPath().getList("data.listActions.setting.id");  // NEED TO CHANGE THIS TO DETAIL ACTIONS
    }
}