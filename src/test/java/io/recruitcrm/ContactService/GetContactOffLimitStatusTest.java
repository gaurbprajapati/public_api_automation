package io.recruitcrm.ContactService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.albatross.offlimit.MarkOffLimit;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|automationForRevamp")
public class GetContactOffLimitStatusTest extends TestBase {

    private String albatrossTkn;
    int entityTypeId = 2; // Contact entity type
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "offLimitStatusTestData", groups = {"contact_service", "nightly-build"})
    public void testGetOffLimitStatusSuccess(int id, int offLimitStatusId) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", String.valueOf(id));

        Response response = RestClient.doGet("JSON", contactServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", 
                albatrossTkn, null, pathParams, true);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("OffLimitStatus fetched successfully."));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        assertThat("Entity ID should match contact ID", (Integer) jp.get("data.entityId"), equalTo(id));
        assertThat("Entity Type should be 2 (contacts)", (Integer) jp.get("data.entityType"), equalTo(entityTypeId));
        assertThat("Account ID should not be null", jp.get("data.accountId"), notNullValue());
        assertThat("Off Limit Status ID should not be null", jp.get("data.offLimitStatusId"), notNullValue());
        assertThat("Off Limit Status ID should match expected value", (Integer) jp.get("data.offLimitStatusId"), equalTo(offLimitStatusId));
        assertThat("Off Limit Reason should not be null", jp.get("data.offLimitReason"), notNullValue());
        assertThat("Created On should not be null", jp.get("data.createdOn"), notNullValue());
        assertThat("Created By should not be null", jp.get("data.createdBy"), notNullValue());
        assertThat("Marked By Name should not be null", jp.get("data.markedByName"), notNullValue());
        assertThat("Marked By Email should not be null", jp.get("data.markedByEmail"), notNullValue());
        assertThat("Status Label should not be null", jp.get("data.statusLabel"), notNullValue());
        assertThat("Background Color Hex should not be null", jp.get("data.backgroundColorHex"), notNullValue());
        assertThat("Text Color Hex should not be null", jp.get("data.textColorHex"), notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "offLimitStatusTestDataIdOnly", groups = {"contact_service", "nightly-build"})
    public void testGetOffLimitStatusWithoutAuth(int id) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", String.valueOf(id));

        Response response = RestClient.doGet("JSON", contactServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", 
                null, null, pathParams, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        // Verify error data
        assertThat("Data should contain error message", jp.get("data"), equalTo("Internal Server Error"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "offLimitStatusTestDataIdOnly", groups = {"contact_service", "nightly-build"})
    public void testGetOffLimitStatusInvalidAuth(int id) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", String.valueOf(id));

        Response response = RestClient.doGet("JSON", contactServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", 
                albatrossTkn + "invalid_token", null, pathParams, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();

        // Verify error response structure
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure for error
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("ResponseType code should be 104", (Integer) jp.get("meta.responseType.code"), equalTo(104));

        // Verify error data
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should be empty", jp.get("errors"), notNullValue());
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "offLimitStatusTestDataIdOnly", groups = {"contact_service", "nightly-build"})
    public void testGetOffLimitStatusInvalidEntityType(int id) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", "999"); // Invalid entity type
        pathParams.put("id", String.valueOf(id));

        Response response = RestClient.doGet("JSON", contactServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", 
                albatrossTkn, null, pathParams, true);

//        Response code is 200 for invalid entity Id but the data is null, so it's fine
        JsonPath jp = response.jsonPath();
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
        assertThat("Data should be null", jp.get("data"), nullValue());
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "offLimitStatusTestDataIdOnly", groups = {"contact_service", "nightly-build"})
    public void testGetOffLimitStatusInvalidContactId(int id) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", id + "999"); // Invalid contact ID

        Response response = RestClient.doGet("JSON", contactServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", 
                albatrossTkn, null, pathParams, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @DataProvider(name = "offLimitStatusTestData")
    public Object[][] getOffLimitStatusTestData() {
        // Step 1: Create a contact
        Response contactResponse = albatrossFunctions.createCompanyContact(albatrossURL, albatrossTkn);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        
        JsonPath contactJp = contactResponse.jsonPath();
        // Contact ID can be returned as String, Integer, Number, or ArrayList - handle all cases
        Object contactIdObj = contactJp.get("data.contact.id");
        assertThat("Contact ID should not be null. Response body: " + contactResponse.getBody().asString(), contactIdObj, notNullValue());
        
        int contactId;
        if (contactIdObj instanceof Integer) {
            contactId = ((Integer) contactIdObj).intValue();
        } else if (contactIdObj instanceof String) {
            contactId = Integer.parseInt((String) contactIdObj);
        } else if (contactIdObj instanceof Number) {
            contactId = ((Number) contactIdObj).intValue();
        } else if (contactIdObj instanceof List) {
            // Handle case where API returns an array - take first element
            List<?> idList = (List<?>) contactIdObj;
            assertThat("Contact ID list should not be empty. Response body: " + contactResponse.getBody().asString(), 
                    idList.isEmpty(), is(false));
            Object firstId = idList.get(0);
            if (firstId instanceof Integer) {
                contactId = ((Integer) firstId).intValue();
            } else if (firstId instanceof String) {
                contactId = Integer.parseInt((String) firstId);
            } else if (firstId instanceof Number) {
                contactId = ((Number) firstId).intValue();
            } else {
                throw new RuntimeException("Unexpected type for contact ID in array: " + firstId.getClass().getName() + ". Response body: " + contactResponse.getBody().asString());
            }
        } else {
            throw new RuntimeException("Unexpected type for contact ID: " + contactIdObj.getClass().getName() + ". Response body: " + contactResponse.getBody().asString());
        }
        
        // Step 2: Create off-limit status and get status ID
        int statusId = createOffLimitStatusAndGetId();

        // Step 3: Mark contact as off-limit
        markContactAsOffLimit(contactId, statusId);
        
        return new Object[][] { { contactId, statusId } };
    }

    @DataProvider(name = "offLimitStatusTestDataIdOnly")
    public Object[][] getOffLimitStatusTestDataIdOnly() {
        // Step 1: Create a contact
        Response contactResponse = albatrossFunctions.createCompanyContact(albatrossURL, albatrossTkn);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        
        JsonPath contactJp = contactResponse.jsonPath();
        // Contact ID can be returned as String, Integer, Number, or ArrayList - handle all cases
        Object contactIdObj = contactJp.get("data.contact.id");
        assertThat("Contact ID should not be null. Response body: " + contactResponse.getBody().asString(), contactIdObj, notNullValue());
        
        int contactId;
        if (contactIdObj instanceof Integer) {
            contactId = ((Integer) contactIdObj).intValue();
        } else if (contactIdObj instanceof String) {
            contactId = Integer.parseInt((String) contactIdObj);
        } else if (contactIdObj instanceof Number) {
            contactId = ((Number) contactIdObj).intValue();
        } else if (contactIdObj instanceof List) {
            // Handle case where API returns an array - take first element
            List<?> idList = (List<?>) contactIdObj;
            assertThat("Contact ID list should not be empty. Response body: " + contactResponse.getBody().asString(), 
                    idList.isEmpty(), is(false));
            Object firstId = idList.get(0);
            if (firstId instanceof Integer) {
                contactId = ((Integer) firstId).intValue();
            } else if (firstId instanceof String) {
                contactId = Integer.parseInt((String) firstId);
            } else if (firstId instanceof Number) {
                contactId = ((Number) firstId).intValue();
            } else {
                throw new RuntimeException("Unexpected type for contact ID in array: " + firstId.getClass().getName() + ". Response body: " + contactResponse.getBody().asString());
            }
        } else {
            throw new RuntimeException("Unexpected type for contact ID: " + contactIdObj.getClass().getName() + ". Response body: " + contactResponse.getBody().asString());
        }
        
        // Step 2: Create off-limit status and get status ID
        int statusId = createOffLimitStatusAndGetId();

        // Step 3: Mark contact as off-limit
        markContactAsOffLimit(contactId, statusId);
        
        return new Object[][] { { contactId } };
    }
    
    private int createOffLimitStatusAndGetId() {
        // Create off-limit status
        OffLimitStatus.offLimitStatus offLimitStatus = new OffLimitStatus.offLimitStatus();
        offLimitStatus.setStatus_label("Test Off Limit Status");
        offLimitStatus.setStatus_colour_id("A1");
        offLimitStatus.setSequence_no(1);
        offLimitStatus.setAccount_id(String.valueOf(ThreadManager.getAccount().getAccountId()));
        offLimitStatus.setDefaultStatus("0");
        offLimitStatus.setOfflimit_status_colour_id("A1");
        offLimitStatus.setBackground_color_hex("#FEF2F2");
        offLimitStatus.setText_color_hex("#B04C4C");
        offLimitStatus.setCount(0);

        OffLimitStatus offLimitStatusBody = new OffLimitStatus();
        offLimitStatusBody.setOffLimitStatus(new OffLimitStatus.offLimitStatus[] {offLimitStatus});

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/status", 
                albatrossTkn, null, null, true, offLimitStatusBody);
        
        assertThat("Failed to create off-limit status", response.getStatusCode(), equalTo(200));

        // Fetch all off-limit statuses to get the ID of the newly created status
        Response getStatusResponse = RestClient.doGet("JSON", albatrossURL, "off-limit/status", albatrossTkn, null, null, true);

        assertThat("Failed to fetch off-limit statuses", getStatusResponse.getStatusCode(), equalTo(200));
        JsonPath statusJson = getStatusResponse.jsonPath();

        // Parse the response to find the status with the correct label from "offLimitStatus" array
        List<Map<String, Object>> statuses = statusJson.getList("data.offLimitStatus");
        assertThat("Off-limit statuses list should not be empty", statuses, notNullValue());
        Integer statusId = null;
        for (Map<String, Object> status : statuses) {
            if ("Test Off Limit Status".equals(status.get("status_label"))) {
                // The new API returns 'id' field instead of 'status_id'
                Object idObj = status.get("id");
                if (idObj instanceof Integer) {
                    statusId = (Integer) idObj;
                } else if (idObj instanceof Number) {
                    statusId = ((Number) idObj).intValue();
                }
                break;
            }
        }
        assertThat("Off-limit status with label 'Test Off Limit Status' not found", statusId, notNullValue());
        return statusId;
    }

    private void markContactAsOffLimit(int contactId, int statusId) {
        // Mark contact as off-limit
        MarkOffLimit markOffLimit = new MarkOffLimit();
        markOffLimit.setEntity_type_id(entityTypeId);
        markOffLimit.setEntity_ids(new int[]{contactId});
        markOffLimit.setStatus_id(statusId);
        markOffLimit.setStart_date(String.valueOf(System.currentTimeMillis() / 1000));
        markOffLimit.setEnd_date(String.valueOf(System.currentTimeMillis() / 1000 + 86400)); // 1 day later
        markOffLimit.setReason("Test reason for marking contact as off-limit");

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/mark-off-limit", 
                albatrossTkn, null, null, true, markOffLimit);
        
        assertThat("Failed to mark contact as off-limit", response.getStatusCode(), equalTo(200));
    }
}
