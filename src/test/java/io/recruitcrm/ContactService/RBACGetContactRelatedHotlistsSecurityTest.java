package io.recruitcrm.ContactService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC|automationForRevamp")
public class RBACGetContactRelatedHotlistsSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
    private static final String RELATED_HOTLISTS_SUCCESS_MESSAGE = "Related hotlists fetched successfully.";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> contactIdsMap;
    private Map<String, String> contactSlugsMap;
    private Map<String, Boolean> hotlistCreatedMap;
    
    private String publicToken;
    private String commonCompanySlug;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        contactIdsMap = new HashMap<>();
        contactSlugsMap = new HashMap<>();
        hotlistCreatedMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        
        // Create a common company for contacts
        io.rcrm.api.pojo.Company company = new io.rcrm.api.pojo.Company();
        company.setCompany_name("RBAC Test Company " + System.currentTimeMillis());
        Response companyResponse = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, company);
        commonCompanySlug = companyResponse.jsonPath().get("slug");
    }

    // Check for view contact related hotlists access
    @Owner("Ajendra Singh")
    @Test(dataProvider = "contactViewAccessData", groups = {"role-based", "contact-related-hotlists-access"})
    public void getContactRelatedHotlists_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create contact from creator role if not already created and get the slug
        String contactSlug = ensureContactCreated(creator);

        // Ensure hotlist is created and contact is added to it
        ensureHotlistCreated(contactSlug);

        // Get contact ID from cache or fetch from ReaperIntegration
        Integer contactId = contactIdsMap.get(contactSlug);
        if (contactId == null) {
            contactId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("contact", contactSlug)
                    .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
            contactIdsMap.put(contactSlug, contactId);
        }

        String executorToken = albatrossTknMap.get(executor);

        // Create request body for related hotlists search
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "contacts");
        requestBody.put("recordId", contactId);

        Response response = RestClient.doPost1("JSON", contactServiceURL, "hotlists/related-hotlists/search/get", executorToken, null, null, true, requestBody.toString());

        int expectedStatusCodeUpdated = expectedStatusCode == 401 ? 403 : expectedStatusCode;
        String expectedMessageUpdated = expectedMessage.equals("Forbidden") ? "Access Denied" : expectedMessage;
        validateRelatedHotlistsResponse(response, expectedStatusCodeUpdated, expectedMessageUpdated, testDescription);
    }

    // Helper method to ensure hotlist is created and contact is added to it
    private void ensureHotlistCreated(String contactSlug) {
        Boolean isCreated = hotlistCreatedMap.get(contactSlug);
        if (isCreated == null || !isCreated) {
            createHotlistForContact(contactSlug);
            hotlistCreatedMap.put(contactSlug, true);
        }
    }

    // Helper method to create a hotlist and add contact to it
    private void createHotlistForContact(String contactSlug) {
        // Create a hotlist using the public API
        JSONObject hotlistPayload = new JSONObject();
        hotlistPayload.put("name", "RBAC Test Hotlist " + System.currentTimeMillis());
        hotlistPayload.put("related_to_type", "contact");
        hotlistPayload.put("shared", 1);

        Response hotlistResponse = RestClient.doPost1("JSON", baseURL, "hotlists", publicToken, null, null, true, hotlistPayload.toString());
        int hotlistStatusCode = hotlistResponse.getStatusCode();
        if (hotlistStatusCode != 200) {
            String errorBody = hotlistResponse.getBody().asString();
            throw new AssertionError("Failed to create hotlist - Expected status 200 but got " + hotlistStatusCode 
                    + ". Response body: " + errorBody);
        }

        int hotlistId = hotlistResponse.jsonPath().getInt("id");

        // Add contact to the hotlist
        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(contactSlug);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, publicToken, null, pathParameters, true, hotlistRelated);
        addResponse.then().statusCode(200);
    }

    private String ensureContactCreated(String creator) {
        if (!contactSlugsMap.containsKey(creator)) {
            createContactFromRole(creator);
        }
        return contactSlugsMap.get(creator);
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
        
        contactIdsMap.put(contactSlug, contactId);
        contactSlugsMap.put(creator, contactSlug);
    }

    private void validateRelatedHotlistsResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == 200 && SUCCESS_MESSAGE.equals(expectedMessage)) {
            response.then().body("meta.message", equalTo(RELATED_HOTLISTS_SUCCESS_MESSAGE));
            response.then().body("meta.status", equalTo(200));
            response.then().body("data", notNullValue());
            response.then().body("data", instanceOf(java.util.List.class));
        } else if (expectedStatusCode == 403 && ACCESS_DENIED_MESSAGE.equals(expectedMessage)) {
            response.then().body("errors[0].message", anyOf(
                equalTo("Access Denied: User is not authorized to view this contact's data"),
                containsString("Access Denied"),
                containsString("not authorized")
            ));
        }
    }

    @DataProvider(name = "contactViewAccessData", parallel = true)
    public Object[][] contactViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "related hotlists for contact");
    }
}
