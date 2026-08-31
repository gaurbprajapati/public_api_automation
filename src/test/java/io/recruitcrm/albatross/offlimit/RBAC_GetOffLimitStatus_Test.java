package io.recruitcrm.albatross.offlimit;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.hamcrest.Matchers;
import java.util.*;
import com.qa.api.util.DateUtil;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.offlimit.*;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.javafaker.JavaFakerCompany;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBAC_GetOffLimitStatus_Test extends TestBase {
    private Map<String, String> albatrossTknMap;
    private final String basePath = "off-limit/get-status/{entityId}/{id}";
    private final AllCrudFunctions crudFunction = new AllCrudFunctions();
    private final JavaFakerCompany faker = new JavaFakerCompany();
    private Object apiAuthToken;

    private static final String SUCCESS_MESSAGE = "Success";
    private static final int SUCCESS_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        Map<String, Integer> userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
        apiAuthToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "rbacGetOffLimitStatusAccessData", groups = {"rbac", "get-offlimit-status"})
    public void getOffLimitStatusRBACAccess_Test(String executorRole, String entityName, int entityId, int recordId, int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executorRole);
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("entityId", String.valueOf(entityId));
        pathParameters.put("id", String.valueOf(recordId));
        
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, executorToken, null, pathParameters, true);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription, recordId);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, int recordId) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected Status: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }
        if (expectedStatusCode == SUCCESS_STATUS_CODE && SUCCESS_MESSAGE.equals(expectedMessage)) {
            validateSuccessResponse(response, recordId);
        }
    }

    private void validateSuccessResponse(Response response, int recordId) {
        response.then().body("message_type", Matchers.is("is-success"));
        assertThat("Off-limit data should not be null", response.jsonPath().get("data"), notNullValue());
        assertThat("Off-limit ID should be present", response.jsonPath().get("data.id"), notNullValue());
        assertThat("Entity ID should match record ID", response.jsonPath().get("data.entity_id"), is(recordId));
    }

    @DataProvider(name = "rbacGetOffLimitStatusAccessData", parallel = true)
    public Object[][] rbacGetOffLimitStatusAccessData() {
        int candidateId = createAndMarkOffLimit("candidates");
        int contactId = createAndMarkOffLimit("contacts");
        int companyId = createAndMarkOffLimit("companies");
        
        return new Object[][] {
            // ERB created - TITAN-21497
            
            // Candidates entity scenarios
            {"AccountOwner", "candidates", 5, candidateId, 200, "Success", "Owner Token can get candidates off-limit status - TC001"},
            {"Admin", "candidates", 5, candidateId, 200, "Success", "Admin Token can get candidates off-limit status - TC002"},
            {"TeamMember", "candidates", 5, candidateId, 200, "Success", "TeamMember Token can get candidates off-limit status - TC003"},
            {"RestrictedTeamMember", "candidates", 5, candidateId, 200, "Success", "Restricted Team Member Token can get candidates off-limit status - TC004"},
            {"CustomRoleTeamOnly", "candidates", 5, candidateId, 200, "Success", "Custom Role Team Only Token can get candidates off-limit status - TC005"},
            {"CustomRoleNothing", "candidates", 5, candidateId, 200, "Success", "Custom Role Nothing Token can get candidates off-limit status - TC006"},
            
            // Contacts entity scenarios
            {"AccountOwner", "contacts", 2, contactId, 200, "Success", "Owner Token can get contacts off-limit status - TC007"},
            {"Admin", "contacts", 2, contactId, 200, "Success", "Admin Token can get contacts off-limit status - TC008"},
            {"TeamMember", "contacts", 2, contactId, 200, "Success", "TeamMember Token can get contacts off-limit status - TC009"},
            {"RestrictedTeamMember", "contacts", 2, contactId, 200, "Success", "Restricted Team Member Token can get contacts off-limit status - TC010"},
            {"CustomRoleTeamOnly", "contacts", 2, contactId, 200, "Success", "Custom Role Team Only Token can get contacts off-limit status - TC011"},
            {"CustomRoleNothing", "contacts", 2, contactId, 200, "Success", "Custom Role Nothing Token can get contacts off-limit status - TC012"},
            
            // Companies entity scenarios
            {"AccountOwner", "companies", 3, companyId, 200, "Success", "Owner Token can get companies off-limit status - TC013"},
            {"Admin", "companies", 3, companyId, 200, "Success", "Admin Token can get companies off-limit status - TC014"},
            {"TeamMember", "companies", 3, companyId, 200, "Success", "TeamMember Token can get companies off-limit status - TC015"},
            {"RestrictedTeamMember", "companies", 3, companyId, 200, "Success", "Restricted Team Member Token can get companies off-limit status - TC016"},
            {"CustomRoleTeamOnly", "companies", 3, companyId, 200, "Success", "Custom Role Team Only Token can get companies off-limit status - TC017"},
            {"CustomRoleNothing", "companies", 3, companyId, 200, "Success", "Custom Role Nothing Token can get companies off-limit status - TC018"}
        };
    }

    private int createAndMarkOffLimit(String entityName) {
        String ownerToken = albatrossTknMap.get("AccountOwner");
        
        int recordId;
        String recordSlug;
        
        switch (entityName) {
            case "candidates":
                JsonPath candidateJsonPath = crudFunction.createCandidate(albatrossURL, ownerToken).jsonPath();
                recordId = candidateJsonPath.getInt("data.candidate.id");
                recordSlug = candidateJsonPath.getString("data.candidate.slug");
                break;
                
            case "contacts":
                JsonPath contactJsonPath = crudFunction.createCompanyContact(albatrossURL, ownerToken).jsonPath();
                recordId = contactJsonPath.getInt("data.contact.id");
                recordSlug = contactJsonPath.getString("data.contact.slug");
                break;
                
            case "companies":
                JsonPath companyJsonPath = crudFunction.createCompanyContact(albatrossURL, ownerToken).jsonPath();
                recordId = companyJsonPath.getInt("data.company.id");
                recordSlug = companyJsonPath.getString("data.company.slug");
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityName);
        }

        Response offlimitStatusResponse = RestClient.doGet("JSON", baseURL, "off-limit-status", apiAuthToken, null, null, false);
        assertThat("Failed to get off-limit status list for " + entityName, offlimitStatusResponse.getStatusCode(), is(200));
        JsonPath offlimitJsonPath = offlimitStatusResponse.jsonPath();
        int offlimitStatusId = offlimitJsonPath.get("[0].id");

        Object markOffLimitPayload;
        
        switch (entityName) {
            case "candidates":
                MarkCandidateOffLimit markCandidateOffLimit = new MarkCandidateOffLimit();
                markCandidateOffLimit.setCandidate_slugs(recordSlug);
                markCandidateOffLimit.setStatus_id(String.valueOf(offlimitStatusId));
                markCandidateOffLimit.setEnd_date(DateUtil.getTomorrowDateString());
                markCandidateOffLimit.setReason(faker.getRandomReason());
                markOffLimitPayload = markCandidateOffLimit;
                break;
                
            case "contacts":
                MarkContactOffLimit markContactOffLimit = new MarkContactOffLimit();
                markContactOffLimit.setContact_slugs(recordSlug);
                markContactOffLimit.setStatus_id(String.valueOf(offlimitStatusId));
                markContactOffLimit.setEnd_date(DateUtil.getTomorrowDateString());
                markContactOffLimit.setReason(faker.getRandomReason());
                markOffLimitPayload = markContactOffLimit;
                break;
                
            case "companies":
                MarkCompanyOffLimit markCompanyOffLimit = new MarkCompanyOffLimit();
                markCompanyOffLimit.setCompany_slugs(recordSlug);
                markCompanyOffLimit.setStatus_id(String.valueOf(offlimitStatusId));
                markCompanyOffLimit.setEnd_date(DateUtil.getTomorrowDateString());
                markCompanyOffLimit.setReason(faker.getRandomReason());
                markCompanyOffLimit.setMark_candidate_off_limit(false);
                markCompanyOffLimit.setMark_contact_off_limit(false);
                markOffLimitPayload = markCompanyOffLimit;
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported entity type for mark off-limit: " + entityName);
        }

        String markOffLimitEndpoint = entityName + "/mark-off-limit";
        Response response = RestClient.doPost1("JSON", baseURL, markOffLimitEndpoint, apiAuthToken, null, null, false, markOffLimitPayload);
        assertThat("Failed to mark " + entityName + " as off-limit", response.getStatusCode(), is(200));
        assertThat("Mark off-limit response should confirm update", response.jsonPath().getString("remark"), is("Records Were Updated"));

        return recordId;
    }
}


