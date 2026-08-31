package io.recruitcrm.CompanyService;

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
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.albatross.offlimit.MarkOffLimit;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACGetCompanyOffLimitStatusSecurityTest extends TestBase {

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied: User is not authorized to view this company's data";
    private static final String OFF_LIMIT_STATUS_SUCCESS_MESSAGE = "OffLimitStatus fetched successfully.";

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, Integer> companyIdsMap;
    private Map<String, Integer> offLimitStatusIdsMap;
    
    private String publicToken;
    private int entityTypeId = 3;   // Company entity type

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        companyIdsMap = new HashMap<>();
        offLimitStatusIdsMap = new HashMap<>();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        publicToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "companyOffLimitStatusViewAccessData", groups = {"role-based", "company-off-limit-status-view-access"})
    public void getCompanyOffLimitStatus_Test(String companyCreator, String executor, 
            int expectedStatusCode, String expectedMessage, String testDescription) {
        int companyId = ensureCompanyCreatedByRole(companyCreator);
        String executorToken = albatrossTknMap.get(executor);
        int offLimitStatusId = ensureOffLimitStatusCreated();

        // Mark company as off-limit if not already done
        markCompanyAsOffLimit(companyId, offLimitStatusId, companyCreator);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("entityTypeId", String.valueOf(entityTypeId));
        pathParams.put("id", String.valueOf(companyId));

        Response response = RestClient.doGet("JSON", companyServiceURL, "off-limit/entity/{entityTypeId}/status/{id}", 
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

    private void markCompanyAsOffLimit(int companyId, int statusId, String creator) {
        // Use AccountOwner token from the map instead of ThreadManager
        String albatrossTkn = albatrossTknMap.get("AccountOwner");
        
        MarkOffLimit markOffLimit = new MarkOffLimit();
        markOffLimit.setEntity_type_id(entityTypeId);
        markOffLimit.setEntity_ids(new int[]{companyId});
        markOffLimit.setStatus_id(statusId);
        markOffLimit.setStart_date(String.valueOf(System.currentTimeMillis() / 1000));
        markOffLimit.setEnd_date(String.valueOf(System.currentTimeMillis() / 1000 + 86400)); // 1 day later
        markOffLimit.setReason("RBAC test reason for marking company as off-limit");

        Response response = RestClient.doPost1("JSON", albatrossURL, "off-limit/mark-off-limit", albatrossTkn, null, null, true, markOffLimit);
        
        assertThat("Failed to mark company as off-limit", response.getStatusCode(), equalTo(200));
    }

    @DataProvider(name = "companyOffLimitStatusViewAccessData", parallel = true)
    public Object[][] companyOffLimitStatusViewAccessData(ITestContext context) {
        return RBAC6LevelDataProvider.getViewAccessData(context, "off-limit status for company");
    }
}