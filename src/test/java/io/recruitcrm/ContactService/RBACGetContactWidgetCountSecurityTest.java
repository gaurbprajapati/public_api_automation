package io.recruitcrm.ContactService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
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
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC|automationForRevamp")
public class RBACGetContactWidgetCountSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied: User is not authorized to view this contact's data";
    private static final String WIDGET_COUNT_SUCCESS_MESSAGE = "Widget Count fetched successfully";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> contactIdsMap;
    private Map<String, String> contactSlugsMap;
    
    private String publicToken;
    private String commonCompanySlug;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        contactIdsMap = new HashMap<>();
        contactSlugsMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
        
        // Create a common company for contacts
        io.rcrm.api.pojo.Company company = new io.rcrm.api.pojo.Company();
        company.setCompany_name("RBAC Test Company " + System.currentTimeMillis());
        Response companyResponse = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, company);
        commonCompanySlug = companyResponse.jsonPath().get("slug");
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "contactWidgetCountViewAccessData", groups = {"role-based", "contact-widget-count-view-access"})
    public void getContactWidgetCount_Test(String contactCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        int contactId = ensureContactCreatedByRole(contactCreator);
        String contactSlug = contactSlugsMap.get(contactCreator);
        String executorToken = albatrossTknMap.get(executor);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", String.valueOf(contactId));
        queryParams.put("recordSlug", contactSlug);

        Response response = RestClient.doGet("JSON", contactServiceURL, "widget-count", 
                executorToken, queryParams, null, true);

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
            response.then().body("meta.message", equalTo(WIDGET_COUNT_SUCCESS_MESSAGE));
            response.then().body("meta.status", equalTo(200));
            response.then().body("data", notNullValue());
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
        contactSlugsMap.put(creator, contactSlug);
    }

    @DataProvider(name = "contactWidgetCountViewAccessData", parallel = true)
    public Object[][] contactWidgetCountViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "widget count for contact");
    }
}