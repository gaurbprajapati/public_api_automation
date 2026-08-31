package io.recruitcrm.JobService;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class JobListBulkActionCustomisationTest extends TestBase {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    String ownerAuthToken;
    String teamMemberAuthToken;
    String apiAuthToken;
    Map<String, Integer> userIdMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAuthToken = ThreadManager.getOwnerAlbatrossToken();
        teamMemberAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        apiAuthToken = ThreadManager.getAccountApiKey();
        getUserView(ownerAuthToken);
        getUserView(teamMemberAuthToken);
    }

    @DataProvider(name = "jobEntityData")
    public Object[][] getJobEntityData() {
        return new Object[][]{{4}}; // Job entityId is 4
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void getJobBulkActionUserView_Success(int entityId) {
        userIdMap = getUserIdMap();
        String basePath = "custom-view/user-view";
        Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityId", String.valueOf(entityId));

        Response response = RestClient.doGet("JSON", jobServiceURL, basePath, ownerAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message"), "User View Fetched Successfully.");
        int expected = Integer.parseInt(userIdMap.get("accountOwner").toString());
        int actual = response.jsonPath().getInt("data.listActions.meta.updatedBy");
        Assert.assertEquals(actual, expected, "Mismatch in updatedBy field");

        //Verify the default state of bulk actions
        List<Map<String, Object>> settingActions = response.jsonPath().getList("data.listActions.setting");
		Assert.assertEquals(settingActions.size(), 9, "Expected 9 default bulk actions");
		
		Integer[] expectedActionIds = {1, 2, 3, 4, 5, 6, 7, 8 , 9};
		for (int i = 0; i < expectedActionIds.length; i++) {
			Assert.assertEquals(settingActions.get(i).get("id"), expectedActionIds[i], "Action at position " + (i + 1) + " should have ID " + expectedActionIds[i]);
		}
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/job/getBulkActionUserView.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void getJobBulkActionUserView_WithoutAuth(int entityId) {
        String basePath = "custom-view/user-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", String.valueOf(entityId));
        Response response = RestClient.doGet("JSON", jobServiceURL, basePath, "", queryParameters, null, true);

        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
        Assert.assertEquals(response.jsonPath().getInt("meta.responseType.code"), 104);
        Assert.assertEquals(response.jsonPath().get("data"), "Missing bearer token in header");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void getJobBulkActionUserView_InvalidAuth(int entityId) {
        String basePath = "custom-view/user-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", String.valueOf(entityId));
        Response response = RestClient.doGet("JSON", jobServiceURL, basePath, ownerAuthToken + "123", queryParameters, null, true);

        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void getJobBulkActionAccountView_Success(int entityId) {
        userIdMap = getUserIdMap();
        String basePath = "custom-view/account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", String.valueOf(entityId));
        
        Response response = RestClient.doGet("JSON", jobServiceURL, basePath, ownerAuthToken, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);
        int expected = Integer.parseInt(userIdMap.get("accountOwner").toString());
        int actual = response.jsonPath().getInt("data.listActions.meta.updatedBy");
		Assert.assertEquals(response.jsonPath().get("meta.message"), "Account View Fetched Successfully.");
        Assert.assertEquals(actual, expected, "Mismatch in updatedBy field");

        //Verify the default state of bulk actions
        Assert.assertEquals(response.jsonPath().getInt("data.listActionsLocked"), 0, "listActionsLocked should be 0 for default account view");
        List<Map<String, Object>> settingActions = response.jsonPath().getList("data.listActions.setting");
		Assert.assertEquals(settingActions.size(), 9, "Expected 7 default bulk actions");
		
		Integer[] expectedActionIds = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		for (int i = 0; i < expectedActionIds.length; i++) {
			Assert.assertEquals(settingActions.get(i).get("id"), expectedActionIds[i], "Action at position " + (i + 1) + " should have ID " + expectedActionIds[i]);
		}
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/job/getBulkActionAccountView.json"));
    }


    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void getJobBulkActionAccountView_WithoutAuth(int entityId) { 
        String basePath = "custom-view/account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", String.valueOf(entityId));
        Response response = RestClient.doGet("JSON", jobServiceURL, basePath, "", queryParameters, null, true);
        
        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void getJobBulkActionAccountView_InvalidAuth(int entityId) {
        String basePath = "custom-view/account-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", String.valueOf(entityId));
        Response response = RestClient.doGet("JSON", jobServiceURL, basePath, ownerAuthToken + "123", queryParameters, null, true);
        
        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
    }



    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void updateJobBulkActionUserView_Success(int entityId) {
        getUserView(teamMemberAuthToken);
        String basePath = "custom-view/user-view";

        //Update the user view
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityId", entityId);
        JSONArray listActions = new JSONArray(Arrays.asList(2, 1, 5, 6, 3, 4, 7));
        requestBody.put("listActions", listActions);
        Response response = RestClient.doPut("JSON", jobServiceURL, basePath, teamMemberAuthToken, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Updated User View Successfully.");

        //Verify the updated user view
        Response getUserView = RestClient.doGet("JSON", jobServiceURL, basePath +"?entityId=" + entityId, teamMemberAuthToken, null, null, true);
        Assert.assertEquals(getUserView.getStatusCode(), 200);
        Assert.assertEquals(getUserView.jsonPath().get("meta.message"), "User View Fetched Successfully.");
        List<Map<String, Object>> settingActions = getUserView.jsonPath().getList("data.listActions.setting");
        Assert.assertEquals(settingActions.size(), 7, "Expected 11 bulk actions");
        Integer[] expectedActionIds = {2, 1, 5, 6, 3, 4, 7};
        for (int i = 0; i < expectedActionIds.length; i++) {
            Assert.assertEquals(settingActions.get(i).get("id"), expectedActionIds[i], "Action at position " + (i + 1) + " should have ID " + expectedActionIds[i]);
        }
        
        //Restrict the user view and assert that the user view is replaced with the account view.
        JSONObject payload = new JSONObject();
        payload.put("entityId", entityId);
        JSONArray actions = new JSONArray(Arrays.asList(1,2,3,4,5,6,7));
        requestBody.put("listActions", actions);
        requestBody.put("listActionsLocked", 1);
        Response accountViewResponse = RestClient.doPut("JSON", jobServiceURL, "custom-view/account-view", ownerAuthToken, null, true, requestBody);
        Assert.assertEquals(accountViewResponse.getStatusCode(), 200);
        Assert.assertEquals(accountViewResponse.jsonPath().get("meta.message"), "Updated Account View Successfully.");

        //Get the user view for the team member
        List<Integer> userView = getUserView(teamMemberAuthToken);
        Assert.assertEquals(userView.size(), 7, "Expected 11 bulk actions");
        List<Integer> expectedActions = List.of(1, 2, 3, 4, 5, 6, 7);
        Assert.assertEquals(userView, expectedActions, "User view is not replaced with the account view, even the user veiw is restricted");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void updateJobBulkActionUserView_WithoutAuth(int entityId) {
        String basePath = "custom-view/user-view";
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityId", entityId);
        JSONArray listActions = new JSONArray(Arrays.asList(2, 1, 5, 6, 3, 4, 7));
        requestBody.put("listActions", listActions);
        Response response = RestClient.doPut("JSON", jobServiceURL, basePath, "", null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void updateJobBulkActionUserView_InvalidAuth(int entityId) {
        String basePath = "custom-view/user-view";
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityId", entityId);
        JSONArray listActions = new JSONArray(Arrays.asList(2, 1, 5, 6, 3, 4, 7));
        requestBody.put("listActions", listActions);
        Response response = RestClient.doPut("JSON", jobServiceURL, basePath, ownerAuthToken + "123", null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void updateJobBulkActionUserView_RestrictedForLockedUser(int entityId) {
        //Restrict the user view
        JSONObject payload = new JSONObject();
        payload.put("entityId", entityId);
        JSONArray actions = new JSONArray(Arrays.asList(1,2,3,4,5,6,7));
        payload.put("listActions", actions);
        payload.put("listActionsLocked", 1);
        Response accountViewResponse = RestClient.doPut("JSON", jobServiceURL, "custom-view/account-view", ownerAuthToken, null, true, payload);
        Assert.assertEquals(accountViewResponse.getStatusCode(), 200);
        Assert.assertEquals(accountViewResponse.jsonPath().get("meta.message"), "Updated Account View Successfully.");

        //Update the user view and assert that the user view is not updated
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityId", entityId);
        JSONArray userActions = new JSONArray(Arrays.asList(2,1,4,3,5,6,7));
        requestBody.put("listActions", userActions);
        Response userViewResponse = RestClient.doPut("JSON", jobServiceURL, "custom-view/user-view", teamMemberAuthToken, null, true, requestBody);
        Assert.assertEquals(userViewResponse.getStatusCode(), 401);
        Assert.assertEquals(userViewResponse.jsonPath().get("errors[0].message"), "Unauthorized");
        Assert.assertEquals(userViewResponse.jsonPath().get("meta.responseType.context"), "Error while processing request");
        Assert.assertEquals(userViewResponse.jsonPath().getInt("meta.responseType.code"), 101);

    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void updateJobBulkActionAccountView_Success(int entityId) {
        //Update the account view
        JSONObject payload = new JSONObject();
        payload.put("entityId", entityId);
        JSONArray actions = new JSONArray(Arrays.asList(2,1,4,3,6,5,7));
        payload.put("listActions", actions);
        payload.put("listActionsLocked", 1);
        Response accountViewResponse = RestClient.doPut("JSON", jobServiceURL, "custom-view/account-view", ownerAuthToken, null, true, payload);
        Assert.assertEquals(accountViewResponse.getStatusCode(), 200);
        Assert.assertEquals(accountViewResponse.jsonPath().get("meta.message"), "Updated Account View Successfully.");

        //Verify the updated account view
        Response getAccountView = RestClient.doGet("JSON", jobServiceURL, "custom-view/account-view?entityId=" + entityId, ownerAuthToken, null, null, true);
        Assert.assertEquals(getAccountView.getStatusCode(), 200);
        Assert.assertEquals(getAccountView.jsonPath().get("meta.message"), "Account View Fetched Successfully.");
        List<Map<String, Object>> settingActions = getAccountView.jsonPath().getList("data.listActions.setting");
        Assert.assertEquals(settingActions.size(), 7, "Expected 11 bulk actions");
        Integer[] expectedActionIds = {2, 1, 4, 3, 6, 5, 7};
        for (int i = 0; i < expectedActionIds.length; i++) {
            Assert.assertEquals(settingActions.get(i).get("id"), expectedActionIds[i], "Action at position " + (i + 1) + " should have ID " + expectedActionIds[i]);
        }

        //Verify that the user view for other user is updated.
        List<Integer> userView = getUserView(teamMemberAuthToken);
        Assert.assertEquals(userView.size(), 7, "Expected 11 bulk actions");
        List<Integer> expectedActions = List.of(2, 1, 4, 3, 6, 5, 7);
        Assert.assertEquals(userView, expectedActions, "User view is not updated for other user");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void updateJobBulkActionAccountView_WithoutAuth(int entityId) {
        String basePath = "custom-view/account-view";
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityId", entityId);
        JSONArray listActions = new JSONArray(Arrays.asList(2, 1, 5, 6, 3, 4, 7));
        requestBody.put("listActions", listActions);
        requestBody.put("listActionsLocked", 1);
        Response response = RestClient.doPut("JSON", jobServiceURL, basePath, "", null, true, requestBody);
        
        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void updateJobBulkActionAccountView_InvalidAuth(int entityId) {
        String basePath = "custom-view/account-view";
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityId", entityId);
        JSONArray listActions = new JSONArray(Arrays.asList(2, 1, 5, 6, 3, 4, 7));
        requestBody.put("listActions", listActions);
        requestBody.put("listActionsLocked", 1);
        Response response = RestClient.doPut("JSON", jobServiceURL, basePath, ownerAuthToken + "123", null, true, requestBody);
        

        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Unauthorised access");
    }



    @Owner("Raj Pandey")
    @Test(dataProvider = "jobEntityData", groups = {"job_service", "nightly-build"})
    public void verifyOtherUserCannotUpdateAccountView(int entityId) {
        JSONObject payload = new JSONObject();
        payload.put("entityId", entityId);
        JSONArray actions = new JSONArray(Arrays.asList(2,1,4,3,6,5,7));
        payload.put("listActions", actions);
        payload.put("listActionsLocked", 1);
        Response accountViewResponse = RestClient.doPut("JSON", jobServiceURL, "custom-view/account-view", teamMemberAuthToken, null, true, payload);

        Assert.assertEquals(accountViewResponse.getStatusCode(), 401);
        Assert.assertEquals(accountViewResponse.jsonPath().get("errors[0].message"), "Unauthorized");
        Assert.assertEquals(accountViewResponse.jsonPath().get("meta.responseType.context"), "Error while processing request");
        Assert.assertEquals(accountViewResponse.jsonPath().getInt("meta.responseType.code"), 101);
    }


    public List<Integer> getUserView(String token) {
        String basePath = "custom-view/user-view";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entityId", "4"); // Job entityId is 4
        Response response = RestClient.doGet("JSON", jobServiceURL, basePath, token, queryParameters, null, true);
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "User View Fetched Successfully.");
        return response.jsonPath().getList("data.listActions.setting.id");
    }


    public Map<String, Integer> getUserIdMap() {
        Response getUsers = allCrudFunctions.getUsers(albatrossURL, ownerAuthToken);
        JsonPath jp = getUsers.jsonPath();
        List<Map<String, Object>> records = jp.getList("data.records");
        for (Map<String, Object> record : records) {
            String role = (String) record.get("role");
            Integer id = (Integer) record.get("id");
            switch (role) {
                case "Account Owner":
                    userIdMap.put("accountOwner", id);
                    break;
                case "Admin":
                    userIdMap.put("admin", id);
                    break;
                case "Restricted Team Member":
                    userIdMap.put("resTeamMember", id);
                    break;
                case "Team Member":
                    userIdMap.put("teamMember", id);
                    break;
            }
        }
        return userIdMap;
    }

    
}
