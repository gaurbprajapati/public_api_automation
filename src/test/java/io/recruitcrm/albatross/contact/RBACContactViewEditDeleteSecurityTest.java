package io.recruitcrm.albatross.contact;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.RBAC6LevelDataProvider;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerContact;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.json.JSONArray;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import java.util.ArrayList;
import java.util.List;
import io.rcrm.api.pojo.albatross.Contact.ContactPojo;
import io.rcrm.api.pojo.albatross.Contact.ContactPojo.Contact;
import io.rcrm.api.pojo.albatross.Contact.ContactPojo.SelectedCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.javafaker.JavaFakerMails;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACContactViewEditDeleteSecurityTest extends TestBase {

	JavaFakerContact contactFaker = new JavaFakerContact();
    JavaFakerMails mailsFaker = new JavaFakerMails();
    private final commanFunction function = new commanFunction();
    private final AllCrudFunctions crudFunctions = new AllCrudFunctions();

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> contactSlugsMap;
    private Map<String, Boolean> contactCreatedMap;
    private Map<String, String> entityIdMap; // Cache for entity IDs
    private String publicToken;
    private String commonCompanySlug;
    private String commonCompanyId;
	String ContactFirstName = contactFaker.getFirstName();
	String ContactLastName = contactFaker.getLastName();
	String ContactEmail = mailsFaker.getFakeEmail();
	String contactNumbers = contactFaker.getContactNumber();
	String commonCompanyName;

    // Constants for expected messages
    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
    private static final String CONTACT_UPDATED_MESSAGE = "Contact Updated";
    private static final String CONTACT_DELETED_MESSAGE = "Contact Deleted";
    private static final String FAILED_UPDATE_MESSAGE = "Failed to Update Contact : Access Denied";
    private static final String FAILED_DELETE_MESSAGE = "Access Denied";

    @BeforeClass(alwaysRun = true)    public void setup() {
        initializeMaps();
        setupTokensAndUserIds();
        initializeContactTracking();
        Response fullResponse = function.createNewCompanyWithMandatoryFields(baseURL, publicToken);
        commonCompanyName = fullResponse.jsonPath().get("company_name");
        commonCompanySlug = fullResponse.jsonPath().get("slug");
        commonCompanyId = getEntityId("company", commonCompanySlug);
    }

    private void initializeMaps() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        contactSlugsMap = new HashMap<>();
        contactCreatedMap = new HashMap<>();
        entityIdMap = new HashMap<>();
    }

    // Helper method to get entity ID with caching
    private String getEntityId(String entityType, String slug) {
        String cacheKey = entityType + ":" + slug;
        String entityId = entityIdMap.get(cacheKey);
        
        if (entityId == null) {
            // Fetch from ReaperIntegration if not in cache
            Response response = ReaperIntegration.getEntityIdFromSlug(entityType, slug);
            entityId = response.getBody().asString().replace("Corresponding entity for the slug is : ", "").trim();
            entityIdMap.put(cacheKey, entityId);
        }
        
        return entityId;
    }

    private void setupTokensAndUserIds() {
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();

        // Debug: print role, token and user id
        albatrossTknMap.entrySet().forEach(entry -> 
        System.out.println("Role: " + entry.getKey() + ", Token: " + entry.getValue() + ", User ID: " + userIdsMap.get(entry.getKey())));
    }

    private void initializeContactTracking() {
        String[] roles = {"AccountOwner", "Admin", "TeamMember", "RestrictedTeamMember", "CustomRoleTeamOnly", "CustomRoleNothing"};
        for (String role : roles) {
            contactCreatedMap.put(role, false);
        }
    }

    // Generic validation method for all response types
    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, 
                                String successField, Object successValue, String forbiddenField, String forbiddenValue) {
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + response.getStatusCode(), e);
        }

        if (expectedStatusCode == 200) {
            if (SUCCESS_MESSAGE.equals(expectedMessage)) {
                try {
                    if (successField != null && successValue != null) {
                        response.then().body(successField, Matchers.is(successValue));
                    }
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
                }
            }
        } else if (expectedStatusCode == 401) {
            if (FORBIDDEN_MESSAGE.equals(expectedMessage)) {
                try {
                    response.then().body(forbiddenField, Matchers.is(forbiddenValue));
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription 
                            + " - Expected '" + forbiddenValue + "' but got: " + response.jsonPath().getString(forbiddenField), e);
                }
            }
        }
    }

    private void validateContactResponse(Response response, int expectedStatusCode, String expectedMessage, String contactSlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.contact.slug", contactSlug, "message", ACCESS_DENIED_MESSAGE);
    }

    private void validateEditContactResponse(Response response, int expectedStatusCode, String expectedMessage, String contactSlug, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.contact.slug", contactSlug, "message", FAILED_UPDATE_MESSAGE);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "message", CONTACT_UPDATED_MESSAGE, "message", FAILED_UPDATE_MESSAGE);
    }

    private void validateDeleteContactResponse(Response response, int expectedStatusCode, String expectedMessage, String entityId, String testDescription) {
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "data.id[0]", Integer.parseInt(entityId), "message", FAILED_DELETE_MESSAGE);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription,
                       "message", CONTACT_DELETED_MESSAGE, "message", FAILED_DELETE_MESSAGE);
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "contactViewAccessData", groups = {"role-based", "contact-view-access"})
    public void viewContact_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        String contactSlug = ensureContactCreated(creator);
        String executorToken = albatrossTknMap.get(executor);
        
        Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", contactSlug);
		String basePath = "contacts/{contact}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, executorToken, null, pathParamters, true);
        validateContactResponse(response, expectedStatusCode, expectedMessage, contactSlug, testDescription);
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "contactEditAccessData", groups = {"role-based", "contact-edit-access"})
    public void editContact_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        String contactSlug = ensureContactCreated(creator);
        String executorToken = albatrossTknMap.get(executor);

        Contact contact = new Contact(contactSlug, ContactFirstName, ContactLastName, "", contactNumbers, "", "");
        SelectedCompany selectedCompany = new SelectedCompany(commonCompanyId, commonCompanyName, 1, commonCompanySlug);
        List<SelectedCompany> selectedCompanies = new ArrayList<>();
        selectedCompanies.add(selectedCompany);
        ContactPojo contactPojo = new ContactPojo(contact, selectedCompanies);

        // Execute edit request
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("contact", contactSlug);
        String basePath = "contacts/{contact}";
        
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, executorToken, null, pathParameters, true, contactPojo);
        validateEditContactResponse(response, expectedStatusCode, expectedMessage, contactSlug, testDescription);
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "contactDeleteAccessData", groups = {"role-based", "contact-delete-access"})
    public void deleteContact_Test(String creator, String executor, int expectedStatusCode, String expectedMessage, String testDescription) {
        // Create contact for deletion
        String contactSlug = createContactFromRole(creator);

        String executorToken = albatrossTknMap.get(executor);

        // Get entity ID and execute deletion
        String entityId = getEntityIdFromSlug(contactSlug);

        JSONObject requestBody = new JSONObject();
        requestBody.put("idsToDelete", new JSONArray().put(Integer.parseInt(entityId)));
        requestBody.put("slugsToDelete", new JSONArray().put(contactSlug));
        requestBody.put("tableFlag", "contact");

        String basePath = "global/delete-record";
        Response deleteResponse = RestClient.doPost1("JSON", albatrossURL, basePath, executorToken, null, null, true, requestBody);
        validateDeleteContactResponse(deleteResponse, expectedStatusCode, expectedMessage, entityId, testDescription);
    }

    private String getEntityIdFromSlug(String contactSlug) {
        return getEntityId("contact", contactSlug);
    }

    private String ensureContactCreated(String creatorRole) {
        Boolean isCreated = contactCreatedMap.get(creatorRole);
        if (isCreated == null || !isCreated) {
            createContactFromRole(creatorRole);
            contactCreatedMap.put(creatorRole, true);
        }
        return contactSlugsMap.get(creatorRole);
    }

    private String createContactFromRole(String role) {
        // Create contact using the public API Contact POJO with owner_id and created_by
        io.rcrm.api.pojo.Contact contact = new io.rcrm.api.pojo.Contact(
            ContactFirstName, 
            ContactLastName, 
            ContactEmail, 
            contactNumbers, 
            commonCompanySlug,  // company_slug parameter
            userIdsMap.get(role),  // owner_id
            userIdsMap.get(role)   // created_by
        );
        
        Response response = RestClient.doPost("JSON", baseURL, "contacts", publicToken, null, true, contact);
        response.then().statusCode(200);
        
        String contactSlug = response.jsonPath().get("slug");
        
        contactSlugsMap.put(role, contactSlug);
        contactCreatedMap.put(role, true);
        return contactSlug;
    }

    @DataProvider(name = "contactViewAccessData", parallel = true)
    public Object[][] contactViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "contact");
    }

    @DataProvider(name = "contactEditAccessData", parallel = true)
    public Object[][] contactEditAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getEditAccessData(context, "contact");
    }

    @DataProvider(name = "contactDeleteAccessData", parallel = true)
    public Object[][] contactDeleteAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getDeleteAccessData(context, "contact");
    }
}