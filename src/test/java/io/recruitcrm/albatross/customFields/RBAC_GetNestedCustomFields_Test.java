package io.recruitcrm.albatross.customFields;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.hamcrest.Matchers;
import org.testng.Assert;
import java.util.*;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.albatross.*;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBAC_GetNestedCustomFields_Test extends TestBase {
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private final String basePath = "nested-custom-fields/get/{entity}";
    private JavaFakerCustomField faker = new JavaFakerCustomField();

    private static final String SUCCESS_MESSAGE = "Success";
    private static final int SUCCESS_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "rbacGetNestedCustomFieldsAccessData", groups = {"rbac", "get-nested-custom-fields"})
    public void getNestedCustomFieldsRBACAccess_Test(String executorRole, int entityId, int parentCustomFieldId, int childCustomFieldId, int parentOptionId, int childOptionId1, int childOptionId2, int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executorRole);
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("entity", String.valueOf(entityId));
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, executorToken, null, pathParameters, true);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription, parentCustomFieldId);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, int parentCustomFieldId) {
        int actualStatusCode = response.getStatusCode();
        
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected Status: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }

        if (expectedStatusCode == SUCCESS_STATUS_CODE && SUCCESS_MESSAGE.equals(expectedMessage)) {
            validateSuccessResponse(response, parentCustomFieldId);
        }
    }

    private void validateSuccessResponse(Response response, int parentCustomFieldId) {
        response.then().body("message", Matchers.is("NestedCustomFields Dependency Data"));
        response.then().body("message_type", Matchers.is("is-success"));
        
        Map<String, Object> responseData = response.jsonPath().getMap("data[0]");
        assertThat("Parent custom field ID should be present in response", responseData.keySet().iterator().next(), is(String.valueOf(parentCustomFieldId)));
    }

    @DataProvider(name = "rbacGetNestedCustomFieldsAccessData", parallel = true)
    public Object[][] rbacGetNestedCustomFieldsAccessData() {
        Object[][] nestedFieldData = getNestedCustomFieldData();
        int entityId = (int) nestedFieldData[0][0];
        int parentCustomFieldId = (int) nestedFieldData[0][1];
        int childCustomFieldId = (int) nestedFieldData[0][2];
        int parentOptionId = (int) nestedFieldData[0][3];
        int childOptionId1 = (int) nestedFieldData[0][4];
        int childOptionId2 = (int) nestedFieldData[0][5];
        
        return new Object[][] {
            // ERB created for Custom Role Nothing can access nested custom fields - TITAN-21888
            {"AccountOwner", entityId, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2, 200, "Success", "Owner Token can get nested custom fields - TC001"},
            {"Admin", entityId, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2, 200, "Success", "Admin Token can get nested custom fields - TC002"},
            {"TeamMember", entityId, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2, 200, "Success", "TeamMember Token can get nested custom fields - TC003"},
            {"RestrictedTeamMember", entityId, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2, 200, "Success", "Restricted Team Member Token can get nested custom fields - TC004"},
            {"CustomRoleTeamOnly", entityId, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2, 200, "Success", "Custom Role Team Only Token can get nested custom fields - TC005"},
            {"CustomRoleNothing", entityId, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2, 200, "Success", "Custom Role Nothing Token can get nested custom fields - TC006"}
        };
    }

    public Object[][] getNestedCustomFieldData() {
        String ownerToken = albatrossTknMap.get("AccountOwner");
        
        int entityId = faker.getValidEntityId();
        String entityName = faker.getEntityName(entityId);

        String parentCustomFieldName = "Parent " + faker.getCustomFieldName(entityName);
        String parentCustomFieldOptions = faker.getNumberOfDefaultOptionsValues(3);

        ExtraField parentExtraField = new ExtraField();
        CustomFieldAlbatross parentCustomField = new CustomFieldAlbatross();

        List<DefaultOptionsValue> parentOptionsList = new ArrayList<>();
        String[] parentOptions = parentCustomFieldOptions.split(",");
        for (int i = 0; i < parentOptions.length; i++) {
            DefaultOptionsValue parentOption = new DefaultOptionsValue();
            parentOption.setLabel(parentOptions[i].trim());
            parentOption.setSequence_no(i + 1);
            parentOption.setTempId(faker.getTempId());
            parentOptionsList.add(parentOption);
        }
        parentExtraField.setDefaultoptionsvalue(parentOptionsList);

        parentExtraField.setColumnid(1);
        parentExtraField.setEntitytypeid(entityId);
        parentExtraField.setExtrafieldname(parentCustomFieldName);
        parentExtraField.setExtrafieldtype("dropdown");
        parentCustomField.setCustumField(parentExtraField);

        Response parentResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", ownerToken, null, false, parentCustomField);

        Assert.assertEquals(parentResponse.getStatusCode(), 200);

        JsonPath parentJson = parentResponse.jsonPath();
        int parentCustomFieldId = parentJson.get("data.custumField.id");
        int parentOptionId = parentJson.get("data.custumField.defaultoptionsvalue[0].id");

        String childCustomFieldName = "Child " + faker.getCustomFieldName(entityName);
        String childCustomFieldOptions = faker.getNumberOfDefaultOptionsValues(3);

        ExtraField childExtraField = new ExtraField();
        CustomFieldAlbatross childCustomField = new CustomFieldAlbatross();

        List<DefaultOptionsValue> childOptionsList = new ArrayList<>();
        String[] childOptions = childCustomFieldOptions.split(",");
        for (int i = 0; i < childOptions.length; i++) {
            DefaultOptionsValue childOption = new DefaultOptionsValue();
            childOption.setLabel(childOptions[i].trim());
            childOption.setSequence_no(i + 1);
            childOption.setTempId(faker.getTempId());
            childOptionsList.add(childOption);
        }
        childExtraField.setDefaultoptionsvalue(childOptionsList);

        childExtraField.setColumnid(2);
        childExtraField.setEntitytypeid(entityId);
        childExtraField.setExtrafieldname(childCustomFieldName);
        childExtraField.setExtrafieldtype("multiselect");
        childCustomField.setCustumField(childExtraField);

        Response childResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", ownerToken, null, false, childCustomField);

        Assert.assertEquals(childResponse.getStatusCode(), 200);

        JsonPath childJson = childResponse.jsonPath();

        int childCustomFieldId = childJson.get("data.custumField.id");
        int childOptionId1 = childJson.get("data.custumField.defaultoptionsvalue[0].id");
        int childOptionId2 = childJson.get("data.custumField.defaultoptionsvalue[1].id");

        List<NestedCustomFieldPojo.Mapping> mappingsList = new ArrayList<>();

        NestedCustomFieldPojo.Mapping mapping1 = new NestedCustomFieldPojo.Mapping();
        mapping1.setParent_value_id(parentOptionId);
        mapping1.setChild_value_id(childOptionId1);
        mapping1.setChild_visibility(null);
        mappingsList.add(mapping1);

        NestedCustomFieldPojo.Mapping mapping2 = new NestedCustomFieldPojo.Mapping();

        mapping2.setParent_value_id(parentOptionId);
        mapping2.setChild_value_id(childOptionId2);
        mapping2.setChild_visibility(null);
        mappingsList.add(mapping2);

        NestedCustomFieldPojo nestedCustomField = new NestedCustomFieldPojo();
        nestedCustomField.setEntity(String.valueOf(entityId));
        nestedCustomField.setLevel(1);
        nestedCustomField.setDependency_id(String.valueOf(parentCustomFieldId));
        nestedCustomField.setParent_id(parentCustomFieldId);
        nestedCustomField.setChild_id(childCustomFieldId);
        nestedCustomField.setMappings(mappingsList);

        Response response = RestClient.doPost("JSON", albatrossURL, "nested-custom-fields/store", ownerToken, null, false, nestedCustomField);
        Assert.assertEquals(response.getStatusCode(), 200);

        response.then().body("message", Matchers.is("Field dependency added successfully"));
        response.then().body("message_type", Matchers.is("is-success"));

        Object data[][] = {{ entityId, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2 } };

        return data;
    }
}

