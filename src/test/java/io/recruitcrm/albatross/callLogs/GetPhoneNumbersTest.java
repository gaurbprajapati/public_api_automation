package io.recruitcrm.albatross.callLogs;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetPhoneNumbersTest extends TestBase {

    commanFunction function = new commanFunction();
    AllCrudFunctions albatrossFunction = new AllCrudFunctions();
    String albatrossAuthToken;
    String apiToken;
    String entitySlug;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Harika")
    @Test(dataProvider = "getEntityData", groups = "nightly-build")
    public void getPhoneNumberFields(String entityType, int entitytypeId) {
        String entitySlug = createEntity(entityType);
        int entityId = getEntityId(entityType, entitySlug);
        createCustomField(entitytypeId);
        updateCustomField(entityType, entityId);

        String basePath = "field-values/phone-number";

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("entity_type", String.valueOf(entitytypeId));
        queryParameters.put("entity_slug", entitySlug);

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, queryParameters, null,true);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.equalTo("is-success"));
        if (!entityType.equals("company")) {
            validateField(response, 0, "standard", "phonenumber", "Phone", null);
            validateField(response, 1, "custom", "phonenumber", "Phone Number " + entitytypeId, "1234567890");
        } else {
            validateField(response, 0, "custom", "phonenumber", "Phone Number " + entitytypeId, "1234567890");
        }
    }

    @Owner("Harika")
    @Test(dataProvider = "getInvalidData", groups = "nightly-build")
    public void getPhoneNumberFieldsWithInvalidParams(String entityTypeId, String entityTypeSlug,String expectedMessage,int statusCode){
        String basePath = "field-values/phone-number";

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("entity_type", entityTypeId);
        queryParameters.put("entity_slug", entityTypeSlug);

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, queryParameters, null,true);

        Assert.assertEquals(response.getStatusCode(), statusCode);
        response.then().body("message", Matchers.equalTo(expectedMessage));
        response.then().body("message_type", Matchers.equalTo("is-danger"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getPhoneNumberFieldsWithInvalidAuth() {
        entitySlug = createEntity("candidate");

        String basePath = "field-values/phone-number";

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("entity_type", "5");
        queryParameters.put("entity_slug", entitySlug);

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken+"123", queryParameters, null,true);

        Assert.assertEquals(response.getStatusCode(), 401);
    }

    public void createCustomField(int entityId) {
        String customFieldName = "Phone Number"+" "+entityId;
        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(1);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype("phonenumber");
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath,albatrossAuthToken , null, false,
                customField);
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    public void updateCustomField(String entityType,int entityId){
        List<Integer> entityIds = Arrays.asList(entityId);

        UpdateFields updateFields = new UpdateFields();
        updateFields.setKey("custcolumn1");
        updateFields.setValue("1234567890");
        updateFields.setTableFlag(entityType);
        updateFields.setId(entityIds);

        String basePath = "global/update-fields";

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken,
                null, true, updateFields);

        Assert.assertEquals(response.getStatusCode(), 200);
    }

    public String createEntity(String realtedToType){
        String entitySlug = null;
        if (realtedToType.equals("candidate")) {
            JsonPath json = function.createNewCandidateWithMandatoryFields(baseURL, apiToken).jsonPath();
            entitySlug = json.get("slug");
        }

        if (realtedToType.equals("company")) {
            JsonPath json = function.createNewCompanyWithMandatoryFields(baseURL, apiToken).jsonPath();
            entitySlug = json.get("slug");
        }

        if (realtedToType.equals("contact")) {
            JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiToken).jsonPath();
            String companySlug = jsonCompany.get("slug");
            JsonPath json = function.createNewContact_POST(baseURL, apiToken, companySlug).jsonPath();
            entitySlug = json.get("slug");
        }
        return entitySlug;
    }

    public int getEntityId(String entityType, String entitySlug) {
        int entityId = 0;
        if (entityType.equals("candidate")) {
            entityId = albatrossFunction.getCandidateResponse(albatrossURL, albatrossAuthToken, entitySlug).jsonPath().get("data.candidate.id");
        }

        if (entityType.equals("company")) {
            entityId = albatrossFunction.getCompanyResponse(albatrossURL, albatrossAuthToken, entitySlug).jsonPath().get("data.company.id");
        }

        if (entityType.equals("contact")) {
            entityId = Integer.parseInt(albatrossFunction.getContactResponse(albatrossURL, albatrossAuthToken, entitySlug).jsonPath().get("data.contact.id"));
        }
        return entityId;
    }

    @DataProvider(parallel = true)
    public Object[][] getEntityData() {
        return new Object[][]{ { "candidate" ,5}, { "contact",2}, { "company",3 } };
    }

    @DataProvider(parallel = true)
    public Object[][] getInvalidData() {
        entitySlug = createEntity("candidate");
        return new Object[][]{{"", "", "The entity type field is mandatory.,The entity slug field is mandatory.", 422}, {"5", entitySlug + "123", "Something went wrong! Please try again later", 200}, {"8", entitySlug, "The entity type must be one of the following values: 2, 3, 4, 5, 11.", 422}};
    }

    private void validateField(Response response, int index, String recordType, String fieldType, String fieldName, String fieldValue) {
        response.then().body("data.fields[" + index + "].record_type", Matchers.equalTo(recordType));
        response.then().body("data.fields[" + index + "].field_type", Matchers.equalTo(fieldType));
        response.then().body("data.fields[" + index + "].field_name", Matchers.equalTo(fieldName));
        if (fieldValue != null) {
            response.then().body("data.fields[" + index + "].field_value", Matchers.equalTo(fieldValue));
        }
    }

}