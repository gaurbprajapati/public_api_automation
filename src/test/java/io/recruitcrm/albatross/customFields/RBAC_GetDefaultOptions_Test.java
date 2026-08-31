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
public class RBAC_GetDefaultOptions_Test extends TestBase {
    private Map<String, String> albatrossTknMap;
    private final String basePath = "custom-fields/get-default-options/{entity}";
    private final JavaFakerCustomField faker = new JavaFakerCustomField();
    private static final String SUCCESS_MESSAGE = "Success";
    private static final int SUCCESS_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        Map<String, Integer> userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "rbacGetDefaultOptionsAccessData", groups = {"rbac", "get-default-options"})
    public void getDefaultOptionsRBACAccess_Test(String executorRole, int entityId, int customFieldId, int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executorRole);
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("entity", String.valueOf(entityId));
        
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, executorToken, null, pathParameters, true);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription, customFieldId);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, int customFieldId) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected Status: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }
        if (expectedStatusCode == SUCCESS_STATUS_CODE && SUCCESS_MESSAGE.equals(expectedMessage)) {
            validateSuccessResponse(response, customFieldId);
        }
    }

    private void validateSuccessResponse(Response response, int customFieldId) {
        response.then().body("message", Matchers.is("Default options for entity custom fields"));
        response.then().body("message_type", Matchers.is("is-success"));
        assertThat("Response data should not be null", response.jsonPath().get("data"), notNullValue());
        Map<String, Object> responseData = response.jsonPath().getMap("data");
        assertThat("Custom field ID should be present in response", responseData.keySet(), hasItem(String.valueOf(customFieldId)));
    }

    @DataProvider(name = "rbacGetDefaultOptionsAccessData", parallel = true)
    public Object[][] rbacGetDefaultOptionsAccessData() {
        Object[][] customFieldData = getCustomFieldData();
        int entityId = (int) customFieldData[0][0];
        int customFieldId = (int) customFieldData[0][1];
        
        return new Object[][] {
            {"AccountOwner", entityId, customFieldId, 200, "Success", "Owner Token can get default options - TC001"},
            {"Admin", entityId, customFieldId, 200, "Success", "Admin Token can get default options - TC002"},
            {"TeamMember", entityId, customFieldId, 200, "Success", "TeamMember Token can get default options - TC003"},
            {"RestrictedTeamMember", entityId, customFieldId, 200, "Success", "Restricted Team Member Token can get default options - TC004"},
            {"CustomRoleTeamOnly", entityId, customFieldId, 200, "Success", "Custom Role Team Only Token can get default options - TC005"},
            // ERB Created - CustomRoleNothing Token is not restrected to hit endpoint - TITAN-21764
            {"CustomRoleNothing", entityId, customFieldId, 200, "Success", "Custom Role Nothing Token can get default options - TC006"},
        };
    }

    private Object[][] getCustomFieldData() {
        int entityId = faker.getValidEntityId();
        String entityName = faker.getEntityName(entityId);
        
        String customFieldName = "RBAC Test " + faker.getCustomFieldName(entityName);
        String customFieldOptions = faker.getNumberOfDefaultOptionsValues(3);
        
        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();
        
        List<DefaultOptionsValue> optionsList = new ArrayList<>();
        String[] options = customFieldOptions.split(",");
        for (int i = 0; i < options.length; i++) {
            DefaultOptionsValue option = new DefaultOptionsValue();
            option.setLabel(options[i].trim());
            option.setSequence_no(i + 1);
            option.setTempId(faker.getTempId());
            optionsList.add(option);
        }
        extraField.setDefaultoptionsvalue(optionsList);
        extraField.setColumnid(1);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype("dropdown");
        customField.setCustumField(extraField);
        
        String ownerToken = albatrossTknMap.get("AccountOwner");
        Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", ownerToken, null, false, customField);
        assertThat("Failed to create custom field for test data", response.getStatusCode(), is(200));
        int customFieldId = response.jsonPath().get("data.custumField.id");
        Object data[][] = {{ entityId, customFieldId }};
        return data;
    }
}

