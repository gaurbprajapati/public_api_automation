package io.recruitcrm.ContactService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.pojo.albatross.offlimit.MarkOffLimit;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC|automationForRevamp")
public class RBACGetContactOffLimitStatusSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied: User is not authorized to view this contact's data";
    private static final String OFF_LIMIT_STATUS_SUCCESS_MESSAGE = "OffLimitStatus fetched successfully.";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> contactIdsMap;
    private Map<String, Integer> offLimitStatusIdsMap;
    
    private String publicToken;
    private String commonCompanySlug;
    private int entityTypeId = 2;   // Contact entity type

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        contactIdsMap = new HashMap<>();
        offLimitStatusIdsMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        
        // Create a common company for contacts
        io.rcrm.api.pojo.Company company = new io.rcrm.api.pojo.Company();
        company.setCompany_name("RBAC Test Company " + System.currentTimeMillis());
        Response companyResponse = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, company);
        commonCompanySlug = companyResponse.jsonPath().get("slug");
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "contactOffLimitStatusViewAccessData", groups = {"role-based", "contact-off-limit-status-view-access"})
    public void getContactOffLimitStatus_Test(String contactCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        int contactId = ensureContactCreatedByRole(contactCreator);
        String executorToken = albatrossTknMap.get(executor);
        int offLimitStatusId = ensureOffLimitStatusCreated();

        // Mark contact as off-limit if not already done
        markContactAsOffLimit(contactId, offLimitStatusId, contactCreator);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", String.valueOf(contactId));

        Response response = RestClient.doGet("JSON", contactServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", 
                executorToken, null, pathParams, true);

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
            response.then().body("meta.message", equalTo(OFF_LIMIT_STATUS_SUCCESS_MESSAGE));
            response.then().body("meta.status", equalTo(200));
            response.then().body("data", notNullValue());
            response.then().body("data.entityId", notNullValue());
            response.then().body("data.entityType", equalTo(entityTypeId));
        } else if (expectedStatusCode == 401 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            response.then().body("meta.message", equalTo("Unauthorised access"));
        } else if (expectedStatusCode == 403 && FORBIDDEN_MESSAGE.equals(expectedMessage)) {
            response.then().body("message", equalTo(ACCESS_DENIED_MESSAGE));
        }
    }

    private int ensureContactCreatedByRole(String creator) {
        if (!contactIdsMap.containsKey(creator)) {
            createContactFromRole(creator);
        }
        return contactIdsMap.get(creator);
    }

    private void createContactFromRole(String creator) {
        JavaFakerContact faker = new JavaFakerContact();
        Contact contact = new Contact(
            faker.getFirstName(),
            faker.getLastName(),
            faker.getEmailID(),
            faker.getContactNumber(),
            commonCompanySlug,
            userIdsMap.get(creator),
            userIdsMap.get(creator)
        );
        
        Response contactResponse = RestClient.doPost("JSON", baseURL, "contacts", publicToken, null, true, contact);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        
        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");
        assertThat("Contact Slug should not be null", contactSlug, notNullValue());

        int contactId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("contact", contactSlug)
                .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
        
        contactIdsMap.put(creator, contactId);
    }

    private int ensureOffLimitStatusCreated() {
        String cacheKey = "default";
        if (!offLimitStatusIdsMap.containsKey(cacheKey)) {
            int statusId = createOffLimitStatusAndGetId();
            offLimitStatusIdsMap.put(cacheKey, statusId);
        }
        return offLimitStatusIdsMap.get(cacheKey);
    }

    private int createOffLimitStatusAndGetId() {
        // Use AccountOwner token from the map instead of ThreadManager
        String albatrossTkn = albatrossTknMap.get("AccountOwner");
        
        OffLimitStatus.offLimitStatus offLimitStatus = new OffLimitStatus.offLimitStatus();
        offLimitStatus.setStatus_label("RBAC Test Off Limit Status");
        offLimitStatus.setStatus_colour_id("A1");
        offLimitStatus.setSequence_no(1);
        offLimitStatus.setDefaultStatus("0");
        offLimitStatus.setOfflimit_status_colour_id("A1");
        offLimitStatus.setBackground_color_hex("#FEF2F2");
        offLimitStatus.setText_color_hex("#B04C4C");
        offLimitStatus.setCount(0);

        OffLimitStatus offLimitStatusBody = new OffLimitStatus();
        offLimitStatusBody.setOffLimitStatus(new OffLimitStatus.offLimitStatus[] {offLimitStatus});

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/status", albatrossTkn, null, null, true, offLimitStatusBody);
        
        assertThat("Failed to create off-limit status", response.getStatusCode(), equalTo(200));

        Response getStatusResponse = RestClient.doGet("JSON", albatrossURL, "off-limit/status", albatrossTkn, null, null, true);

        assertThat("Failed to fetch off-limit statuses", getStatusResponse.getStatusCode(), equalTo(200));
        JsonPath statusJson = getStatusResponse.jsonPath();

        List<Map<String, Object>> statuses = statusJson.getList("data.offLimitStatus");
        assertThat("Off-limit statuses list should not be empty", statuses, notNullValue());
        Integer statusId = null;
        for (Map<String, Object> status : statuses) {
            if ("RBAC Test Off Limit Status".equals(status.get("status_label"))) {
                Object idObj = status.get("id");
                if (idObj instanceof Integer) {
                    statusId = (Integer) idObj;
                } else if (idObj instanceof Number) {
                    statusId = ((Number) idObj).intValue();
                }
                break;
            }
        }
        assertThat("Off-limit status with label 'RBAC Test Off Limit Status' not found", statusId, notNullValue());
        return statusId;
    }

    private void markContactAsOffLimit(int contactId, int statusId, String creator) {
        // Use AccountOwner token from the map instead of ThreadManager
        String albatrossTkn = albatrossTknMap.get("AccountOwner");
        
        MarkOffLimit markOffLimit = new MarkOffLimit();
        markOffLimit.setEntity_type_id(entityTypeId);
        markOffLimit.setEntity_ids(new int[]{contactId});
        markOffLimit.setStatus_id(statusId);
        markOffLimit.setStart_date(String.valueOf(System.currentTimeMillis() / 1000));
        markOffLimit.setEnd_date(String.valueOf(System.currentTimeMillis() / 1000 + 86400)); // 1 day later
        markOffLimit.setReason("RBAC test reason for marking contact as off-limit");

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/mark-off-limit", albatrossTkn, null, null, true, markOffLimit);
        
        assertThat("Failed to mark contact as off-limit", response.getStatusCode(), equalTo(200));
    }

    @DataProvider(name = "contactOffLimitStatusViewAccessData", parallel = true)
    public Object[][] contactOffLimitStatusViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "off-limit status for contact");
    }
}
