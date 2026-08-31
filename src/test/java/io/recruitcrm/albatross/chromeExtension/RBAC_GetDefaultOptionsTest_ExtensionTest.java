package io.recruitcrm.albatross.chromeExtension;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBAC_GetDefaultOptionsTest_ExtensionTest extends TestBase {
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private final String basePath = "extensions/chrome/custom-fields/get-default-options/{entityTypeId}";
    commanFunction function = new commanFunction();

    private static final String SUCCESS_MESSAGE = "Success";
    private static final int SUCCESS_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "rbacGetDefaultOptionsAccessData", groups = {"rbac", "get-default-options-extension"})
    public void getDefaultOptionsExtensionRBACAccess_Test(String executorRole, String entityTypeId, String entityName, String option1, String option2, int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executorRole);
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("entityTypeId", entityTypeId);
        
        Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, executorToken, null, pathParameters, true);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription, option1, option2);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, String option1, String option2) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected Status: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }
        if (expectedStatusCode == SUCCESS_STATUS_CODE && SUCCESS_MESSAGE.equals(expectedMessage)) {
            if (option1 == null && option2 == null) {
                validateEmptyDataResponse(response);
            } else {
                validateSuccessResponse(response, option1, option2);
            }
        }
    }

    private void validateSuccessResponse(Response response, String option1, String option2) {
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("message", Matchers.is("Default options for entity custom fields"));
        response.then().body("data.values()[0][0].label", Matchers.is(option1));
        response.then().body("data.values()[0][1].label", Matchers.is(option2));
    }

    private void validateEmptyDataResponse(Response response) {
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("message", Matchers.is("Default options for entity custom fields"));
        response.then().body("data.isEmpty()", Matchers.is(true));
    }

    @DataProvider(name = "rbacGetDefaultOptionsAccessData", parallel = true)
    public Object[][] rbacGetDefaultOptionsAccessData() {
        createCustomFields();
        
        return new Object[][] {            
            // Candidate entity scenarios
            {"AccountOwner", "5", "candidate", "Candidate_Option 1", "Candidate_Option 2", 200, "Success", "Owner Token can get candidate default options from extension - TC001"},
            {"Admin", "5", "candidate", "Candidate_Option 1", "Candidate_Option 2", 200, "Success", "Admin Token can get candidate default options from extension - TC002"},
            {"TeamMember", "5", "candidate", "Candidate_Option 1", "Candidate_Option 2", 200, "Success", "TeamMember Token can get candidate default options from extension - TC003"},
            {"RestrictedTeamMember", "5", "candidate", "Candidate_Option 1", "Candidate_Option 2", 200, "Success", "Restricted Team Member Token can get candidate default options from extension - TC004"},
            {"CustomRoleTeamOnly", "5", "candidate", "Candidate_Option 1", "Candidate_Option 2", 200, "Success", "Custom Role Team Only Token can get candidate default options from extension - TC005"},
            {"CustomRoleNothing", "5", "candidate", "Candidate_Option 1", "Candidate_Option 2", 200, "Success", "Custom Role Nothing Token can get candidate default options from extension - TC006"},
            
            // Company entity scenarios
            {"AccountOwner", "3", "company", "Company_Option 1", "Company_Option 2", 200, "Success", "Owner Token can get company default options from extension - TC007"},
            {"Admin", "3", "company", "Company_Option 1", "Company_Option 2", 200, "Success", "Admin Token can get company default options from extension - TC008"},
            {"TeamMember", "3", "company", "Company_Option 1", "Company_Option 2", 200, "Success", "TeamMember Token can get company default options from extension - TC009"},
            {"RestrictedTeamMember", "3", "company", "Company_Option 1", "Company_Option 2", 200, "Success", "Restricted Team Member Token can get company default options from extension - TC010"},
            {"CustomRoleTeamOnly", "3", "company", "Company_Option 1", "Company_Option 2", 200, "Success", "Custom Role Team Only Token can get company default options from extension - TC011"},
            {"CustomRoleNothing", "3", "company", "Company_Option 1", "Company_Option 2", 200, "Success", "Custom Role Nothing Token can get company default options from extension - TC012"},
            
            // Contact entity scenarios
            {"AccountOwner", "2", "contact", "Contact_Option 1", "Contact_Option 2", 200, "Success", "Owner Token can get contact default options from extension - TC013"},
            {"Admin", "2", "contact", "Contact_Option 1", "Contact_Option 2", 200, "Success", "Admin Token can get contact default options from extension - TC014"},
            {"TeamMember", "2", "contact", "Contact_Option 1", "Contact_Option 2", 200, "Success", "TeamMember Token can get contact default options from extension - TC015"},
            {"RestrictedTeamMember", "2", "contact", "Contact_Option 1", "Contact_Option 2", 200, "Success", "Restricted Team Member Token can get contact default options from extension - TC016"},
            {"CustomRoleTeamOnly", "2", "contact", "Contact_Option 1", "Contact_Option 2", 200, "Success", "Custom Role Team Only Token can get contact default options from extension - TC017"},
            {"CustomRoleNothing", "2", "contact", "Contact_Option 1", "Contact_Option 2", 200, "Success", "Custom Role Nothing Token can get contact default options from extension - TC018"},

            // ERB created : Job and deal entity should not return default options - TITAN-21911

            // Job entity scenarios - Returns empty data
            {"AccountOwner", "4", "job", null, null, 200, "Success", "Owner Token gets empty data for job default options from extension - TC019"},

            // Deal entity scenarios - Returns empty data
            {"AccountOwner", "11", "deal", null, null, 200, "Success", "Owner Token gets empty data for deal default options from extension - TC025"},
        };
    }

    public Object[][] createCustomFields() {
        String ownerToken = albatrossTknMap.get("AccountOwner");
        Response companyResponse = function.createCustomFieldsResponse(albatrossURL, ownerToken, "company", "companyField", "dropdown", "Company_Option 1,Company_Option 2");
        Response contactResponse = function.createCustomFieldsResponse(albatrossURL, ownerToken, "contact", "contactField", "dropdown", "Contact_Option 1,Contact_Option 2");
        Response candidateResponse = function.createCustomFieldsResponse(albatrossURL, ownerToken, "candidate", "candidateField", "dropdown", "Candidate_Option 1,Candidate_Option 2");
        Response jobResponse = function.createCustomFieldsResponse(albatrossURL, ownerToken, "job", "jobField", "dropdown", "Job_Option 1,Job_Option 2");
        Response dealResponse = function.createCustomFieldsResponse(albatrossURL, ownerToken, "deal", "dealField", "dropdown", "Deal_Option 1,Deal_Option 2");
        return new Object[][]{{ companyResponse, contactResponse, candidateResponse, jobResponse, dealResponse }};
    }
}

