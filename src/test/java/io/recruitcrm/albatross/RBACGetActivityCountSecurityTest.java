package io.recruitcrm.albatross;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.ITestContext;
import org.testng.annotations.*;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACGetActivityCountSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied: User is not authorized to view this entity's data";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> entitySlugsMap;
    private Map<String, Integer> entityTypeIdsMap;
    
    private String publicToken;
    private commanFunction function = new commanFunction();
    private String basePath = "expand-activity/get-activity-count";
    private JavaFakerTask fakerTask = new JavaFakerTask();
    private JavaFakerMeeting fakerMeeting = new JavaFakerMeeting();

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        entitySlugsMap = new HashMap<>();
        entityTypeIdsMap = new HashMap<>();
        
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        
        // Map entity types to their IDs
        entityTypeIdsMap.put("candidate", 5);
        entityTypeIdsMap.put("contact", 2);
        entityTypeIdsMap.put("company", 3);
        entityTypeIdsMap.put("job", 4);
        entityTypeIdsMap.put("deal", 11);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "activityCountViewAccessData", groups = {"role-based", "activity-count-view-access"})
    public void getActivityCountForCandidate_Test(String entityCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        // Use candidate as default entity type for RBAC testing
        String entityType = "candidate";
        int entityTypeId = entityTypeIdsMap.get(entityType);
        
        String entitySlug = ensureEntityWithActivitiesCreatedByRole(entityCreator, entityType);
        String executorToken = albatrossTknMap.get(executor);

        JSONObject activityCount = getActivityCountObject(entitySlug, entityTypeId);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, executorToken, null, true, activityCount);
        
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "activityCountViewAccessData", groups = {"role-based", "activity-count-view-access"})
    public void getActivityCountForContact_Test(String entityCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        // Use contact as default entity type for RBAC testing
        String entityType = "contact";
        int entityTypeId = entityTypeIdsMap.get(entityType);
        
        String entitySlug = ensureEntityWithActivitiesCreatedByRole(entityCreator, entityType);
        String executorToken = albatrossTknMap.get(executor);

        JSONObject activityCount = getActivityCountObject(entitySlug, entityTypeId);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, executorToken, null, true, activityCount);
        
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "activityCountViewAccessData", groups = {"role-based", "activity-count-view-access"})
    public void getActivityCountForCompany_Test(String entityCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        // Use company as default entity type for RBAC testing
        String entityType = "company";
        int entityTypeId = entityTypeIdsMap.get(entityType);
        
        String entitySlug = ensureEntityWithActivitiesCreatedByRole(entityCreator, entityType);
        String executorToken = albatrossTknMap.get(executor);

        JSONObject activityCount = getActivityCountObject(entitySlug, entityTypeId);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, executorToken, null, true, activityCount);
        
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "activityCountViewAccessData", groups = {"role-based", "activity-count-view-access"})
    public void getActivityCountForJob_Test(String entityCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        // Use job as default entity type for RBAC testing
        String entityType = "job";
        int entityTypeId = entityTypeIdsMap.get(entityType);
        
        String entitySlug = ensureEntityWithActivitiesCreatedByRole(entityCreator, entityType);
        String executorToken = albatrossTknMap.get(executor);

        JSONObject activityCount = getActivityCountObject(entitySlug, entityTypeId);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, executorToken, null, true, activityCount);
        
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "activityCountViewAccessDataForDeal", groups = {"role-based", "activity-count-view-access"})
    public void getActivityCountForDeal_Test(String entityCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        // Use deal as default entity type for RBAC testing
        String entityType = "deal";
        int entityTypeId = entityTypeIdsMap.get(entityType);
        
        String entitySlug = ensureEntityWithActivitiesCreatedByRole(entityCreator, entityType);
        String executorToken = albatrossTknMap.get(executor);

        JSONObject activityCount = getActivityCountObject(entitySlug, entityTypeId);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, executorToken, null, true, activityCount);
        
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            response.then().body("message_type", Matchers.containsString("is-success"));
            response.then().body("data", notNullValue());
            // Verify activity counts are present
            response.then().body("data.totalNotesCount", notNullValue());
            response.then().body("data.totalAppointmentsCount", notNullValue());
            response.then().body("data.totalTasksCount", notNullValue());
        } else if (expectedStatusCode == 401 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            // Some endpoints return 401 for forbidden, others return 422
            // Check for either access denied message or unauthorized error
            try {
                response.then().body("message", Matchers.anyOf(
                    equalTo(ACCESS_DENIED_MESSAGE),
                    Matchers.containsString("Access Denied"),
                    Matchers.containsString("Unauthorized")
                ));
            } catch (AssertionError e) {
                // If message validation fails, check for 422 status (validation error)
                throw e;
            }
        }
    }

    private String ensureEntityWithActivitiesCreatedByRole(String creator, String entityType) {
        String cacheKey = creator + "_" + entityType;
        if (!entitySlugsMap.containsKey(cacheKey)) {
            createEntityWithActivitiesFromRole(creator, entityType, cacheKey);
        }
        return entitySlugsMap.get(cacheKey);
    }

    private void createEntityWithActivitiesFromRole(String creator, String entityType, String cacheKey) {
        Integer creatorUserId = userIdsMap.get(creator);
        
        // Create entity based on type
        String entitySlug = createEntityByType(entityType, creatorUserId);
        
        // Create activities (note, task, meeting) using creator's token
        createActivitiesForEntity(entitySlug, entityType, creator);
        
        entitySlugsMap.put(cacheKey, entitySlug);
    }

    private String createEntityByType(String entityType, Integer ownerUserId) {
        String entitySlug;
        Response response = null;
        switch (entityType) {
            case "candidate":
                response = function.createEntityByRole(baseURL, publicToken, "candidate", ownerUserId);
                entitySlug = response.jsonPath().get("slug");
                break;
                
            case "company":
                response = function.createEntityByRole(baseURL, publicToken, "company", ownerUserId);
                entitySlug = response.jsonPath().get("slug");
                break;
                
            case "contact":
                response = function.createEntityByRole(baseURL, publicToken, "contact", ownerUserId);
                entitySlug = response.jsonPath().get("slug");
                break;
                
            case "job":
                response = function.createEntityByRole(baseURL, publicToken, "job", ownerUserId);
                entitySlug = response.jsonPath().get("slug");
                break;
                
            case "deal":
                response = function.createEntityByRole(baseURL, publicToken, "deal", ownerUserId);
                entitySlug = response.jsonPath().get("slug");
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        
        assertThat("Entity Slug should not be null", entitySlug, notNullValue());
        return entitySlug;
    }

    private void createActivitiesForEntity(String entitySlug, String entityType, String creator) {
        // Create a note
        JSONObject note = new JSONObject();
        note.put("relatedto", entitySlug);
        note.put("relatedtotypeid", getEntityId(entityType));
        note.put("description", "RBAC Test Note");
        Response noteResponse = RestClient.doPost("JSON", albatrossURL, "notes", albatrossTknMap.get(creator), null, true, note);
        assertThat("Failed to create note", noteResponse.getStatusCode(), equalTo(200));
        
        JSONObject task = new JSONObject();
        JSONObject taskObject = new JSONObject();
        task.put("title", "Task Title");
        task.put("type", 1);
        task.put("startdate", System.currentTimeMillis());
        task.put("reminder", 30);
        task.put("relatedto", entitySlug);
        task.put("relatedtotypeid", getEntityId(entityType));
        taskObject.put("task", task);

        Response taskResponse = RestClient.doPost("JSON", albatrossURL, "tasks", albatrossTknMap.get(creator), null, true, taskObject);
        assertThat("Failed to create task", taskResponse.getStatusCode(), equalTo(200));
        
        // Create a meeting
        JSONObject meetingObject = new JSONObject();
        JSONObject appointmentObject = new JSONObject();
        meetingObject.put("title", "Meeting Title");
        meetingObject.put("startdate", System.currentTimeMillis());
        meetingObject.put("enddate", System.currentTimeMillis() + 1000 * 60 * 60 * 2);
        meetingObject.put("reminder", 30);
        meetingObject.put("allday", 1);
        meetingObject.put("ownerid", userIdsMap.get(creator));
        meetingObject.put("relatedto", entitySlug);
        meetingObject.put("relatedtotypeid", getEntityId(entityType));
        appointmentObject.put("appointment", meetingObject);
        Response meetingResponse = RestClient.doPost("JSON", albatrossURL, "meetings", albatrossTknMap.get(creator), null, true, appointmentObject);
        assertThat("Failed to create meeting", meetingResponse.getStatusCode(), equalTo(200));
    }

    private JSONObject getActivityCountObject(String relatedToSlug, int relatedtotypeid) {
        JSONObject activityCount = new JSONObject();
        activityCount.put("relatedToSlug", relatedToSlug);
        activityCount.put("relatedtotypeid", relatedtotypeid);
        activityCount.put("page", "detailspage");
        activityCount.put("skipCountForType", -1);
        return activityCount;
    }

    public int getEntityId(String entityType) {
       switch (entityType) {
        case "candidate":
            return 5;
        case "company":
            return 3;
        case "contact":
            return 2;
        case "job":
            return 4;
        case "deal":
            return 11;
        default:
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
       }
    }

    @DataProvider(name = "activityCountViewAccessData", parallel = true)
    public Object[][] activityCountViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "activity count");
    }

    @DataProvider(name = "activityCountViewAccessDataForDeal", parallel = true)
    public Object[][] activityCountViewAccessDataForDeal(ITestContext context) {
        return RBAC6LevelDataProvider.getDealViewAccessData(context);
    }
}

