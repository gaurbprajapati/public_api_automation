package io.recruitcrm.albatross.contact;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.RBAC2LevelAccessDataProvider;
import io.rcrm.api.javafaker.JavaFakerCompany;
import org.hamcrest.Matchers;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.pojo.albatross.Contact.ContactPojo.SelectedCompany;
import io.rcrm.api.pojo.albatross.Contact.ContactPojo;
import io.rcrm.api.pojo.albatross.Contact.ContactPojo.Contact;
import io.rcrm.api.pojo.Company;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.recruitcrm.albatross.company.CompanyJson;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import java.util.ArrayList;
import java.util.List;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACContactCreateSecurityTest extends TestBase {
    private final JavaFakerCompany fakerCompany = new JavaFakerCompany();
    private final JavaFakerContact fakerContact = new JavaFakerContact();
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> entityIdMap; // Cache for entity IDs
    private String commonCompanySlug;
    private String commonCompanyId;
    private String publicToken;
    private final commanFunction function = new commanFunction();

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";

    @BeforeClass(alwaysRun = true)    public void setupToken() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        entityIdMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        
        // Create a common company for all contacts
        commonCompanySlug = function.createNewCompanyWithMandatoryFields(baseURL, publicToken).jsonPath().get("slug");
        commonCompanyId = getEntityId("company", commonCompanySlug);
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

    private Response createContact(String token) {
        // Create contact with company association using ContactPojo
        Contact contact = 
            new Contact(
                "", // contactSlug (empty for new contact)
                fakerContact.getFirstName(),
                fakerContact.getLastName(),
                fakerContact.getDesignation(),
                fakerContact.getContactNumber(),
                fakerContact.getAddress(),
                "" // linkedinUrl (empty for now)
            );
        
        // Add company association
        List<SelectedCompany> selectedCompanies = new ArrayList<>();
        SelectedCompany selectedCompany = 
            new SelectedCompany(
                commonCompanyId, // company ID placeholder
                fakerCompany.getCompanyName(), // company name
                1, // company ID placeholder
                commonCompanySlug // company slug
            );
        selectedCompanies.add(selectedCompany);
        
        ContactPojo contactPojo = new ContactPojo(contact, selectedCompanies);

        String basePath = "contacts";
        return RestClient.doPost("JSON", albatrossURL, basePath, token, null, true, contactPojo);
    }

    
    private void validateContactResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
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
                    response.then().body("data.contact.slug", Matchers.notNullValue());
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
                }
            }
        } else if (expectedStatusCode == 401) {
            if (FORBIDDEN_MESSAGE.equals(expectedMessage)) {
                try {
                    response.then().body("message", Matchers.is(ACCESS_DENIED_MESSAGE));
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription 
                            + " - Expected '" + ACCESS_DENIED_MESSAGE + "' but got: " + response.jsonPath().getString("message"), e);
                }
            }
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "contact2LevelCreateAccessData", groups = {"role-based", "contact-2level-create-access"})
    public void createContactSecurityTest(String role, String access, int expectedStatusCode, String expectedMessage, String testDescription) {
        String roleToken = albatrossTknMap.get(role);
        Response createResponse = createContact(roleToken);
        validateContactResponse(createResponse, expectedStatusCode, expectedMessage, testDescription);
    }

    @DataProvider(name = "contact2LevelCreateAccessData", parallel = true)
    public Object[][] contact2LevelCreateAccessData(ITestContext context) {
        return RBAC2LevelAccessDataProvider.getContactAccessData(context);
    }
}
