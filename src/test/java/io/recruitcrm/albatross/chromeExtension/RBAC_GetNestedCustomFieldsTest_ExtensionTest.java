package io.recruitcrm.albatross.chromeExtension;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import org.hamcrest.Matchers;
import java.util.*;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.restassured.path.json.JsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBAC_GetNestedCustomFieldsTest_ExtensionTest extends TestBase {
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private final String basePath = "extensions/chrome/nested-custom-fields/get/{entityTypeId}";
    commanFunction function = new commanFunction();
    JavaFakerCustomField faker = new JavaFakerCustomField();

    private static final String SUCCESS_MESSAGE = "Success";
    private static final int SUCCESS_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "rbacGetNestedCustomFieldsAccessData", groups = {"rbac", "get-nested-custom-fields-extension"})
    public void getNestedCustomFieldsExtensionRBACAccess_Test(String executorRole, String entityTypeId, String entityName, Map<String, Object> fieldData, int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executorRole);
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("entityTypeId", entityTypeId);
        Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, executorToken, null, pathParameters, true);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription, fieldData);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, Map<String, Object> fieldData) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected Status: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }
        if (expectedStatusCode == SUCCESS_STATUS_CODE && SUCCESS_MESSAGE.equals(expectedMessage)) {
            if (fieldData == null) {
                validateEmptyDataResponse(response);
            } else {
                validateSuccessResponse(response, fieldData);
            }
        }
    }

    private void validateSuccessResponse(Response response, Map<String, Object> fieldData) {
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("message", Matchers.is("NestedCustomFields Dependency Data"));
        assertNestedCustomFieldIds(response, fieldData);
    }

    private void validateEmptyDataResponse(Response response) {
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("message", Matchers.is("NestedCustomFields Dependency Data"));
        response.then().body("data.isEmpty()", Matchers.is(true));
    }

    private void assertNestedCustomFieldIds(Response response, Map<String, Object> fieldData) {
        String parentCustomFieldId = fieldData.get("parentCustomFieldId").toString();
        String childCustomFieldId = fieldData.get("childCustomFieldId").toString();
        String parentOptionId = fieldData.get("parentOptionId").toString();
        String childOptionId1 = fieldData.get("childOptionId1").toString();
        String childOptionId2 = fieldData.get("childOptionId2").toString();

        assertThat(response.jsonPath().getMap("data[0]").containsKey(parentCustomFieldId), is(true));
        String childrenPath = "data[0]." + parentCustomFieldId + ".children";
        assertThat(response.jsonPath().getMap(childrenPath).containsKey(childCustomFieldId), is(true));
        String dependencyPath = "data[0]." + parentCustomFieldId + ".children." + childCustomFieldId + ".dependency";
        assertThat(response.jsonPath().getMap(dependencyPath).containsKey(parentOptionId), is(true));
        String dependencyArrayPath = "data[0]." + parentCustomFieldId + ".children." + childCustomFieldId + ".dependency." + parentOptionId;
        List<String> childOptions = response.jsonPath().getList(dependencyArrayPath, String.class);
        assertThat(childOptions, hasItems(childOptionId1, childOptionId2));
        assertThat(childOptions.size(), is(2));
    }

    @DataProvider(name = "rbacGetNestedCustomFieldsAccessData", parallel = true)
    public Object[][] rbacGetNestedCustomFieldsAccessData() {
        Map<String, Object> candidateFieldData = createNestedCustomDependency("candidate", "5");
        Map<String, Object> companyFieldData = createNestedCustomDependency("company", "3");
        Map<String, Object> contactFieldData = createNestedCustomDependency("contact", "2");
        Map<String, Object> jobFieldData = createNestedCustomDependency("job", "4");
        Map<String, Object> dealFieldData = createNestedCustomDependency("deal", "11");
        
        return new Object[][] {            
            // Candidate entity scenarios
            {"AccountOwner", "5", "candidate", candidateFieldData, 200, "Success", "Owner Token can get candidate nested custom fields from extension - TC001"},
            {"Admin", "5", "candidate", candidateFieldData, 200, "Success", "Admin Token can get candidate nested custom fields from extension - TC002"},
            {"TeamMember", "5", "candidate", candidateFieldData, 200, "Success", "TeamMember Token can get candidate nested custom fields from extension - TC003"},
            {"RestrictedTeamMember", "5", "candidate", candidateFieldData, 200, "Success", "Restricted Team Member Token can get candidate nested custom fields from extension - TC004"},
            {"CustomRoleTeamOnly", "5", "candidate", candidateFieldData, 200, "Success", "Custom Role Team Only Token can get candidate nested custom fields from extension - TC005"},
            {"CustomRoleNothing", "5", "candidate", candidateFieldData, 200, "Success", "Custom Role Nothing Token can get candidate nested custom fields from extension - TC006"},
            
            // Company entity scenarios
            {"AccountOwner", "3", "company", companyFieldData, 200, "Success", "Owner Token can get company nested custom fields from extension - TC007"},
            {"Admin", "3", "company", companyFieldData, 200, "Success", "Admin Token can get company nested custom fields from extension - TC008"},
            {"TeamMember", "3", "company", companyFieldData, 200, "Success", "TeamMember Token can get company nested custom fields from extension - TC009"},
            {"RestrictedTeamMember", "3", "company", companyFieldData, 200, "Success", "Restricted Team Member Token can get company nested custom fields from extension - TC010"},
            {"CustomRoleTeamOnly", "3", "company", companyFieldData, 200, "Success", "Custom Role Team Only Token can get company nested custom fields from extension - TC011"},
            {"CustomRoleNothing", "3", "company", companyFieldData, 200, "Success", "Custom Role Nothing Token can get company nested custom fields from extension - TC012"},
            
            // Contact entity scenarios
            {"AccountOwner", "2", "contact", contactFieldData, 200, "Success", "Owner Token can get contact nested custom fields from extension - TC013"},
            {"Admin", "2", "contact", contactFieldData, 200, "Success", "Admin Token can get contact nested custom fields from extension - TC014"},
            {"TeamMember", "2", "contact", contactFieldData, 200, "Success", "TeamMember Token can get contact nested custom fields from extension - TC015"},
            {"RestrictedTeamMember", "2", "contact", contactFieldData, 200, "Success", "Restricted Team Member Token can get contact nested custom fields from extension - TC016"},
            {"CustomRoleTeamOnly", "2", "contact", contactFieldData, 200, "Success", "Custom Role Team Only Token can get contact nested custom fields from extension - TC017"},
            {"CustomRoleNothing", "2", "contact", contactFieldData, 200, "Success", "Custom Role Nothing Token can get contact nested custom fields from extension - TC018"},

            // ERB created : Job and deal entity should not return nested custom fields - TITAN-21913

            // Job entity scenarios - Returns empty data
            {"AccountOwner", "4", "job", null, 200, "Success", "Owner Token gets empty data for job nested custom fields from extension - TC019"},

            // Deal entity scenarios - Returns empty data
            {"AccountOwner", "11", "deal", null, 200, "Success", "Owner Token gets empty data for deal nested custom fields from extension - TC020"}
        };
    }

    public Map<String, Object> createNestedCustomDependency(String entityType, String dependencyType) {
        String ownerToken = albatrossTknMap.get("AccountOwner");
        
        Map<String, Object> fieldData = new HashMap<>();

        String option1 = faker.getNumberOfDefaultOptionsValues(1);
        String option2 = faker.getNumberOfDefaultOptionsValues(1);
        String option3 = faker.getNumberOfDefaultOptionsValues(1);
        String parentFieldName = faker.getRandomCustomFieldName();
        String childFieldName = faker.getRandomCustomFieldName();

        Response parentResponse = function.createCustomFieldsResponse(albatrossURL, ownerToken, entityType, parentFieldName, "dropdown", option1 + "," + option2 + "," + option3);
        JsonPath parentJson = parentResponse.jsonPath();
        int parentCustomFieldId = parentJson.get("data.custumField.id");
        int parentOptionId = parentJson.get("data.custumField.defaultoptionsvalue[0].id");

        Response childResponse = function.createCustomFieldsResponse(albatrossURL, ownerToken, entityType, childFieldName, "multiselect", option1 + "," + option2 + "," + option3);
        JsonPath childJson = childResponse.jsonPath();
        int childCustomFieldId = childJson.get("data.custumField.id");
        int childOptionId1 = childJson.get("data.custumField.defaultoptionsvalue[0].id");
        int childOptionId2 = childJson.get("data.custumField.defaultoptionsvalue[1].id");

        function.createNestedDependency(albatrossURL, ownerToken, dependencyType, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2);

        fieldData.put("option1", option1);
        fieldData.put("option2", option2);
        fieldData.put("parentFieldName", parentFieldName);
        fieldData.put("childFieldName", childFieldName);
        fieldData.put("parentCustomFieldId", parentCustomFieldId);
        fieldData.put("parentOptionId", parentOptionId);
        fieldData.put("childCustomFieldId", childCustomFieldId);
        fieldData.put("childOptionId1", childOptionId1);
        fieldData.put("childOptionId2", childOptionId2);

        return fieldData;
    }
}

