package io.recruitcrm.albatross.customFields;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.albatross.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.hamcrest.Matchers;
import java.util.*;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBAC_GetSocialLinksCustomFields_Test extends TestBase {
    private Map<String, String> albatrossTknMap;
    private final String basePath = "custom-fields/social-links/{id}";
    private final JavaFakerCustomField faker = new JavaFakerCustomField();

    private static final String SUCCESS_MESSAGE = "Success";
    private static final int SUCCESS_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        Map<String, Integer> userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "rbacGetSocialLinksAccessData", groups = {"rbac", "get-social-links"})
    public void getSocialLinksRBACAccess_Test(String executorRole, String entityName, int entityId, int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executorRole);
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", String.valueOf(entityId));
        
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, executorToken, null, pathParameters, true);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected Status: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }
        if (expectedStatusCode == SUCCESS_STATUS_CODE && SUCCESS_MESSAGE.equals(expectedMessage)) {
            validateSuccessResponse(response);
        }
    }

    private void validateSuccessResponse(Response response) {
        response.then().body("message_type", Matchers.is("is-success"));
        assertThat("Response data should not be null", response.jsonPath().get("data"), notNullValue());
        assertThat("Social link columns should be present", response.jsonPath().get("data.social_link_columns[0]"), notNullValue());
    }

    @DataProvider(name = "rbacGetSocialLinksAccessData", parallel = true)
    public Object[][] rbacGetSocialLinksAccessData() {
        createSocialLinksCustomField("candidates", 5);
        createSocialLinksCustomField("contacts", 2);
        createSocialLinksCustomField("companies", 3);
        createSocialLinksCustomField("jobs", 4);
        createSocialLinksCustomField("deals", 11);
        
        return new Object[][] {
            // ERB created for Custom Role Nothing - TITAN-21887

            // Candidates entity scenarios
            {"AccountOwner", "candidates", 5, 200, "Success", "Owner Token can get candidates social links - TC001"},
            {"Admin", "candidates", 5, 200, "Success", "Admin Token can get candidates social links - TC002"},
            {"TeamMember", "candidates", 5, 200, "Success", "TeamMember Token can get candidates social links - TC003"},
            {"RestrictedTeamMember", "candidates", 5, 200, "Success", "Restricted Team Member Token can get candidates social links - TC004"},
            {"CustomRoleTeamOnly", "candidates", 5, 200, "Success", "Custom Role Team Only Token can get candidates social links - TC005"},
            {"CustomRoleNothing", "candidates", 5, 200, "Success", "Custom Role Nothing Token can get candidates social links - TC006"},
            
            // Contacts entity scenarios
            {"AccountOwner", "contacts", 2, 200, "Success", "Owner Token can get contacts social links - TC007"},
            {"Admin", "contacts", 2, 200, "Success", "Admin Token can get contacts social links - TC008"},
            {"TeamMember", "contacts", 2, 200, "Success", "TeamMember Token can get contacts social links - TC009"},
            {"RestrictedTeamMember", "contacts", 2, 200, "Success", "Restricted Team Member Token can get contacts social links - TC010"},
            {"CustomRoleTeamOnly", "contacts", 2, 200, "Success", "Custom Role Team Only Token can get contacts social links - TC011"},
            {"CustomRoleNothing", "contacts", 2, 200, "Success", "Custom Role Nothing Token can get contacts social links - TC012"},
            
            // Companies entity scenarios
            {"AccountOwner", "companies", 3, 200, "Success", "Owner Token can get companies social links - TC013"},
            {"Admin", "companies", 3, 200, "Success", "Admin Token can get companies social links - TC014"},
            {"TeamMember", "companies", 3, 200, "Success", "TeamMember Token can get companies social links - TC015"},
            {"RestrictedTeamMember", "companies", 3, 200, "Success", "Restricted Team Member Token can get companies social links - TC016"},
            {"CustomRoleTeamOnly", "companies", 3, 200, "Success", "Custom Role Team Only Token can get companies social links - TC017"},
            {"CustomRoleNothing", "companies", 3, 200, "Success", "Custom Role Nothing Token can get companies social links - TC018"},
            
            // Jobs entity scenarios
            {"AccountOwner", "jobs", 4, 200, "Success", "Owner Token can get jobs social links - TC019"},
            {"Admin", "jobs", 4, 200, "Success", "Admin Token can get jobs social links - TC020"},
            {"TeamMember", "jobs", 4, 200, "Success", "TeamMember Token can get jobs social links - TC021"},
            {"RestrictedTeamMember", "jobs", 4, 200, "Success", "Restricted Team Member Token can get jobs social links - TC022"},
            {"CustomRoleTeamOnly", "jobs", 4, 200, "Success", "Custom Role Team Only Token can get jobs social links - TC023"},
            {"CustomRoleNothing", "jobs", 4, 200, "Success", "Custom Role Nothing Token can get jobs social links - TC024"},
            
            // Deals entity scenarios
            {"AccountOwner", "deals", 11, 200, "Success", "Owner Token can get deals social links - TC025"},
            {"Admin", "deals", 11, 200, "Success", "Admin Token can get deals social links - TC026"},
            {"TeamMember", "deals", 11, 200, "Success", "TeamMember Token can get deals social links - TC027"},
            {"RestrictedTeamMember", "deals", 11, 200, "Success", "Restricted Team Member Token can get deals social links - TC028"},
            {"CustomRoleTeamOnly", "deals", 11, 200, "Success", "Custom Role Team Only Token can get deals social links - TC029"},
            {"CustomRoleNothing", "deals", 11, 200, "Success", "Custom Role Nothing Token can get deals social links - TC030"},
        };
    }

    private void createSocialLinksCustomField(String entityName, int entityId) {
        String customFieldName = faker.getCustomFieldName(entityName);
        
        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();
        
        extraField.setColumnid(faker.getColumnId());
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setDefaultvalue(faker.getDefaultvalue("social links"));
        extraField.setExtrafieldtype("social_profile");
        customField.setCustumField(extraField);
        
        String ownerToken = albatrossTknMap.get("AccountOwner");
        Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", ownerToken, null, false, customField);
        assertThat("Failed to create social links custom field for " + entityName, response.getStatusCode(), is(200));
    }
}

