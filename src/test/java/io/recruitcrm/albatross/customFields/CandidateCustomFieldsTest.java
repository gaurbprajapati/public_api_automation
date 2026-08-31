package io.recruitcrm.albatross.customFields;

import io.rcrm.api.testbase.TestBase;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.DefaultOptionsValue;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CandidateCustomFieldsTest extends TestBase {

    JavaFakerCustomField faker = new JavaFakerCustomField();
    String albatrossAuthToken;
    String invalidAuthToken;
    int entityId;

    public CandidateCustomFieldsTest() {
        super();
    }

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        invalidAuthToken = albatrossAuthToken + "123";
        entityId = 5;
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createCandidateLongTextCustomField_Test() {
        String customFieldName = "Long Text";
        String customFieldType = "longtext";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
        response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        response.then().body("data.custumField.columnid", Matchers.is(columnId));
        response.then().body("data.custumField.defaultvalue", Matchers.notNullValue());

    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void unauthorizedUserCannotCreateCandidateCustomField_Test() {
        String customFieldName = "Long Text Testing";
        String customFieldType = "longtext";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, invalidAuthToken, null, false, customField);
        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createCandidateDateCustomField_Test() {
        String customFieldName = "Date Field";
        String customFieldType = "date";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
        response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        response.then().body("data.custumField.columnid", Matchers.is(columnId));
        response.then().body("data.custumField.defaultvalue", Matchers.notNullValue());
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createCandidateNumberCustomField_Test() {
        String customFieldName = "Number";
        String customFieldType = "number";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
        response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        response.then().body("data.custumField.columnid", Matchers.is(columnId));
        response.then().body("data.custumField.defaultvalue", Matchers.notNullValue());
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createCandidateCheckboxCustomField_Test() {
        String customFieldName = "Checkbox";
        String customFieldType = "checkbox";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
        response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        response.then().body("data.custumField.columnid", Matchers.is(columnId));
        response.then().body("data.custumField.defaultvalue", Matchers.notNullValue());
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createCandidatePhoneNumberCustomField_Test() {
        String customFieldName = "Phone Number";
        String customFieldType = "phonenumber";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
        response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        response.then().body("data.custumField.columnid", Matchers.is(columnId));
        response.then().body("data.custumField.defaultvalue", Matchers.notNullValue());
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createCandidateEmailCustomField_Test() {
        String customFieldName = "Email Custom";
        String customFieldType = "email";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
        response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        response.then().body("data.custumField.columnid", Matchers.is(columnId));
        response.then().body("data.custumField.defaultvalue", Matchers.notNullValue());
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createCandidateFileCustomField_Test() {
        String customFieldName = "Custom File";
        String customFieldType = "file";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
        response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        response.then().body("data.custumField.columnid", Matchers.is(columnId));
        response.then().body("data.custumField.defaultvalue", Matchers.notNullValue());
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createCandidateDateTimeCustomField_Test() {
        String customFieldName = "Date-Time";
        String customFieldType = "date_time";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
        response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        response.then().body("data.custumField.columnid", Matchers.is(columnId));
        response.then().body("data.custumField.defaultvalue", Matchers.notNullValue());
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editCandidateDateTimeCustomField_Test() {

        String originalFieldName = "Date-Time";
        String customFieldType = "date_time";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(originalFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String updatedFieldName = "Renamed Date-Time";
        String updatedFieldType = "date_time";
        
        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();

        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(updatedFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(updatedFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void unauthorizedUserCannotEditCandidateCustomField_Test() {

        String customFieldName = "Test Field";
        String customFieldType = "date_time";
        int columnId = 1;
        int customFieldId = 1093163;

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue("");
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, invalidAuthToken, null, pathParams, false, customField);
        Assert.assertEquals(editResponse.getStatusCode(), 401);
        editResponse.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editCandidateLongTextCustomField_Test() {

        String originalFieldName = "Long Text";
        String customFieldType = "longtext";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(originalFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String updatedFieldName = "Renamed Long Text";
        String updatedFieldType = "longtext";
        
        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();

        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(updatedFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(updatedFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editCandidateDateCustomField_Test() {

        String originalFieldName = "Date Field";
        String customFieldType = "date";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(originalFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String updatedFieldName = "Renamed Date Field";
        String updatedFieldType = "date";
        
        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();

        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(updatedFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(updatedFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editCandidateNumberCustomField_Test() {

        String originalFieldName = "Number";
        String customFieldType = "number";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(originalFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String updatedFieldName = "Renamed Number";
        String updatedFieldType = "number";
        
        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();

        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(updatedFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(updatedFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editCandidateCheckboxCustomField_Test() {

        String originalFieldName = "Checkbox";
        String customFieldType = "checkbox";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(originalFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String updatedFieldName = "Renamed Checkbox";
        String updatedFieldType = "checkbox";
        
        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();

        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(updatedFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(updatedFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editCandidatePhoneNumberCustomField_Test() {

        String originalFieldName = "Phone Number";
        String customFieldType = "phonenumber";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(originalFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String updatedFieldName = "Renamed Phone Number";
        String updatedFieldType = "phonenumber";
        
        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();

        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(updatedFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(updatedFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editCandidateEmailCustomField_Test() {
        String originalFieldName = "Email Custom";
        String customFieldType = "email";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(originalFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String updatedFieldName = "Renamed Email Custom";
        String updatedFieldType = "email";
        
        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();

        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(updatedFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(updatedFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editCandidateFileCustomField_Test() {
        String originalFieldName = "Custom File";
        String customFieldType = "file";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(originalFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String updatedFieldName = "Renamed Custom File";
        String updatedFieldType = "file";
        
        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();

        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(updatedFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(updatedFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteCandidateLongTextCustomField_Test() {
        String customFieldName = "Long Text To Delete";
        String customFieldType = "longtext";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteCandidateDateCustomField_Test() {
        String customFieldName = "Date Field To Delete";
        String customFieldType = "date";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteCandidateNumberCustomField_Test() {
        String customFieldName = "Number Field To Delete";
        String customFieldType = "number";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteCandidateCheckboxCustomField_Test() {
        String customFieldName = "Checkbox Field To Delete";
        String customFieldType = "checkbox";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteCandidatePhoneNumberCustomField_Test() {
        String customFieldName = "Phone Number Field To Delete";
        String customFieldType = "phonenumber";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteCandidateEmailCustomField_Test() {
        String customFieldName = "Email Field To Delete";
        String customFieldType = "email";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteCandidateFileCustomField_Test() {
        String customFieldName = "File Field To Delete";
        String customFieldType = "file";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteCandidateDateTimeCustomField_Test() {
        String customFieldName = "Date Time Field To Delete";
        String customFieldType = "datetime";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void unauthorizedUserCannotDeleteCandidateCustomField_Test() {
        String customFieldName = "Field To Delete Unauthorized";
        String customFieldType = "longtext";
        int columnId = 1;

        String basePath = "custom-fields";

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, invalidAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 401);
        deleteResponse.then().body("error", Matchers.containsString("Unauthorized"));
    }


    @Owner("Smit Patel")
    @Test(dataProvider = "entityTypeData", groups = "nightly-build")
    public void createEntityTypeCustomFieldForCandidate_Test(String entityType, int columnId) {
        createEntityTypeCustomField(entityType, columnId);
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "candidateEntityCustomFieldData", groups = "nightly-build")
    public void editCandidateEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName,
                                                                    String customFieldType, int columnId, int entityId) {
        String updatedFieldName = faker.getRandomCustomFieldName();

        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();
        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(customFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "companyEntityCustomFieldData", groups = "nightly-build")
    public void editCompanyEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName,
                                                                  String customFieldType, int columnId, int entityId) {
        String updatedFieldName = faker.getRandomCustomFieldName();

        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();
        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(customFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "contactEntityCustomFieldData", groups = "nightly-build")
    public void editContactEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName,
                                                                  String customFieldType, int columnId, int entityId) {
        String updatedFieldName = faker.getRandomCustomFieldName();

        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();
        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(customFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "jobEntityCustomFieldData", groups = "nightly-build")
    public void editJobEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName,
                                                              String customFieldType, int columnId, int entityId) {
        String updatedFieldName = faker.getRandomCustomFieldName();

        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();
        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(customFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "dealEntityCustomFieldData", groups = "nightly-build")
    public void editDealEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName,
                                                               String customFieldType, int columnId, int entityId) {
        String updatedFieldName = faker.getRandomCustomFieldName();

        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();
        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(customFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "userEntityCustomFieldData", groups = "nightly-build")
    public void editUserEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String updatedFieldName = faker.getRandomCustomFieldName();

        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();
        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(customFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "teamEntityCustomFieldData", groups = "nightly-build")
    public void editTeamEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String updatedFieldName = faker.getRandomCustomFieldName();

        ExtraField updatedExtraField = new ExtraField();
        CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();
        updatedExtraField.setColumnid(columnId);
        updatedExtraField.setEntitytypeid(entityId);
        updatedExtraField.setExtrafieldname(updatedFieldName);
        updatedExtraField.setExtrafieldtype(customFieldType);
        updatedExtraField.setDefaultvalue("");
        updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
        updatedCustomField.setCustumField(updatedExtraField);

        java.util.Map<String, String> pathParams = new java.util.HashMap<>();
        pathParams.put("id", String.valueOf(customFieldId));
        String editPath = "custom-fields/{id}";

        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, pathParams, false, updatedCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "candidateEntityCustomFieldData", groups = "nightly-build")
    public void deleteCandidateEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "companyEntityCustomFieldData", groups = "nightly-build")
    public void deleteCompanyEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "contactEntityCustomFieldData", groups = "nightly-build")
    public void deleteContactEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "jobEntityCustomFieldData", groups = "nightly-build")
    public void deleteJobEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "dealEntityCustomFieldData", groups = "nightly-build")
    public void deleteDealEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "userEntityCustomFieldData", groups = "nightly-build")
    public void deleteUserEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "teamEntityCustomFieldData", groups = "nightly-build")
    public void deleteTeamEntityTypeCustomFieldForCandidate_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @DataProvider
    public Object[][] entityTypeData() {
        return new Object[][] {
                { "candidate", 1 },
                { "company", 2 },
                { "contact", 3 },
                { "job", 4 },
                { "deal", 5 },
                { "user", 6 },
                { "team", 7 }
        };
    }

    @DataProvider
    public Object[][] candidateEntityCustomFieldData() {
        String customFieldName = faker.getRandomCustomFieldName();
        String customFieldType = "candidate";
        int columnId = 1;

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();
        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        return new Object[][] { { customFieldId, customFieldName, customFieldType, columnId, entityId } };
    }

    @DataProvider
    public Object[][] companyEntityCustomFieldData() {
        String customFieldName = faker.getRandomCustomFieldName();
        String customFieldType = "company";
        int columnId = 1;

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();
        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        return new Object[][] { { customFieldId, customFieldName, customFieldType, columnId, entityId } };
    }

    @DataProvider
    public Object[][] contactEntityCustomFieldData() {
        String customFieldName = faker.getRandomCustomFieldName();
        String customFieldType = "contact";
        int columnId = 1;

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();
        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        return new Object[][] { { customFieldId, customFieldName, customFieldType, columnId, entityId } };
    }

    @DataProvider
    public Object[][] jobEntityCustomFieldData() {
        String customFieldName = faker.getRandomCustomFieldName();
        String customFieldType = "job";
        int columnId = 1;

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();
        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        return new Object[][] { { customFieldId, customFieldName, customFieldType, columnId, entityId } };
    }

    @DataProvider
    public Object[][] dealEntityCustomFieldData() {
        String customFieldName = faker.getRandomCustomFieldName();
        String customFieldType = "deal";
        int columnId = 1;

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();
        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        return new Object[][] { { customFieldId, customFieldName, customFieldType, columnId, entityId } };
    }

    @DataProvider
    public Object[][] userEntityCustomFieldData() {
        String customFieldName = faker.getRandomCustomFieldName();
        String customFieldType = "user";
        int columnId = 1;

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();
        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        return new Object[][] { { customFieldId, customFieldName, customFieldType, columnId, entityId } };
    }

    @DataProvider
    public Object[][] teamEntityCustomFieldData() {
        String customFieldName = faker.getRandomCustomFieldName();
        String customFieldType = "team";
        int columnId = 1;

        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();
        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
        int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

        return new Object[][] { { customFieldId, customFieldName, customFieldType, columnId, entityId } };
    }

    private void createEntityTypeCustomField(String entityType, int columnId) {
        String customFieldName = faker.getRandomCustomFieldName();
        String customFieldType = entityType;
        String basePath = "custom-fields";
        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();
        extraField.setColumnid(columnId);
        extraField.setEntitytypeid(entityId);
        extraField.setExtrafieldname(customFieldName);
        extraField.setExtrafieldtype(customFieldType);
        extraField.setDefaultvalue(null);
        extraField.setDefaultoptionsvalue(new ArrayList<>());
        customField.setCustumField(extraField);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false, customField);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
        response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        response.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

}
