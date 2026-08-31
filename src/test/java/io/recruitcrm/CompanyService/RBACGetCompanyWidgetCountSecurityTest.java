package io.recruitcrm.CompanyService;

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
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACGetCompanyWidgetCountSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied: User is not authorized to view this company's data";
    private static final String WIDGET_COUNT_SUCCESS_MESSAGE = "Widget Count fetched successfully";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> companyIdsMap;
    private Map<String, String> companySlugsMap;
    
    private String publicToken;
    private int entityTypeId = 3;   // Company entity type

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        companyIdsMap = new HashMap<>();
        companySlugsMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "companyWidgetCountViewAccessData", groups = {"role-based", "company-widget-count-view-access"})
    public void getCompanyWidgetCount_Test(String companyCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        int companyId = ensureCompanyCreatedByRole(companyCreator);
        String companySlug = companySlugsMap.get(companyCreator);
        String executorToken = albatrossTknMap.get(executor);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", String.valueOf(companyId));
        queryParams.put("recordSlug", companySlug);

        Response response = RestClient.doGet("JSON", companyServiceURL, "widget-count", 
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

    private int ensureCompanyCreatedByRole(String creator) {
        if (!companyIdsMap.containsKey(creator)) {
            createCompanyFromRole(creator);
        }
        return companyIdsMap.get(creator);
    }

    private void createCompanyFromRole(String creator) {
        JavaFakerCompany faker = new JavaFakerCompany();
        Company company = new Company();
        company.setCompany_name(faker.getCompanyName());
        company.setWebsite(faker.getUrl());
        company.setCity(faker.getCity());
        
        Response companyResponse = RestClient.doPost("JSON", baseURL, "companies", publicToken, null, true, company);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");
        assertThat("Company Slug should not be null", companySlug, notNullValue());

        int companyId = Integer.parseInt(ReaperIntegration.getEntityIdFromSlug("company", companySlug)
                .getBody().asString().replace("Corresponding entity for the slug is : ", "").trim());
        
        companyIdsMap.put(creator, companyId);
        companySlugsMap.put(creator, companySlug);
    }

    @DataProvider(name = "companyWidgetCountViewAccessData", parallel = true)
    public Object[][] companyWidgetCountViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "widget count for company");
    }
}
