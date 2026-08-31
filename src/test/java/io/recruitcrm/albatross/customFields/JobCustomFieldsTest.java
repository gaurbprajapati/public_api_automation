package io.recruitcrm.albatross.customFields;

import io.rcrm.api.testbase.TestBase;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import java.util.ArrayList;
import org.testng.annotations.DataProvider;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class JobCustomFieldsTest extends TestBase {

    JavaFakerCustomField faker = new JavaFakerCustomField();
    String albatrossAuthToken;
    String invalidAuthToken;
    int entityId;

    public JobCustomFieldsTest() {
        super();
    }

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        invalidAuthToken = albatrossAuthToken + "123";
        entityId = 4;
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createJobLongTextCustomField_Test() {
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
    public void unauthorizedUserCannotCreateJobCustomField_Test() {
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
    public void createJobDateCustomField_Test() {
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
    public void createJobNumberCustomField_Test() {
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
    public void createJobCheckboxCustomField_Test() {
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
    public void createJobPhoneNumberCustomField_Test() {
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
    public void createJobEmailCustomField_Test() {
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
    public void createJobFileCustomField_Test() {
        String customFieldName = "File Field";
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
    public void createJobDateTimeCustomField_Test() {
        String customFieldName = "Date Time Field";
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
    public void editJobDateTimeCustomField_Test() {

        String customFieldName = "Date Time Field To Edit";
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
        int accountId = createResponse.jsonPath().getInt("data.custumField.accountid");

        ExtraField editExtraField = new ExtraField();
        CustomFieldAlbatross editCustomField = new CustomFieldAlbatross();

        editExtraField.setId(customFieldId);
        editExtraField.setAccountid(accountId);
        editExtraField.setColumnid(columnId);
        editExtraField.setEntitytypeid(entityId);
        editExtraField.setExtrafieldname("Updated Date Time Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("2024-12-31 23:59:59");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Date Time Field"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void unauthorizedUserCannotEditJobCustomField_Test() {

        String customFieldName = "Field To Edit Unauthorized";
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
        int accountId = createResponse.jsonPath().getInt("data.custumField.accountid");

        ExtraField editExtraField = new ExtraField();
        CustomFieldAlbatross editCustomField = new CustomFieldAlbatross();

        editExtraField.setId(customFieldId);
        editExtraField.setAccountid(accountId);
        editExtraField.setColumnid(columnId);
        editExtraField.setEntitytypeid(entityId);
        editExtraField.setExtrafieldname("Updated Field Unauthorized");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("Updated Value");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, invalidAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 401);
        editResponse.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editJobLongTextCustomField_Test() {

        String customFieldName = "Long Text Field To Edit";
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
        int accountId = createResponse.jsonPath().getInt("data.custumField.accountid");

        ExtraField editExtraField = new ExtraField();
        CustomFieldAlbatross editCustomField = new CustomFieldAlbatross();

        editExtraField.setId(customFieldId);
        editExtraField.setAccountid(accountId);
        editExtraField.setColumnid(columnId);
        editExtraField.setEntitytypeid(entityId);
        editExtraField.setExtrafieldname("Updated Long Text Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("Updated long text value");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Long Text Field"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editJobDateCustomField_Test() {

        String customFieldName = "Date Field To Edit";
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
        int accountId = createResponse.jsonPath().getInt("data.custumField.accountid");

        ExtraField editExtraField = new ExtraField();
        CustomFieldAlbatross editCustomField = new CustomFieldAlbatross();

        editExtraField.setId(customFieldId);
        editExtraField.setAccountid(accountId);
        editExtraField.setColumnid(columnId);
        editExtraField.setEntitytypeid(entityId);
        editExtraField.setExtrafieldname("Updated Date Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("2024-12-31");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Date Field"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editJobNumberCustomField_Test() {
        String customFieldName = "Number Field To Edit";
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
        int accountId = createResponse.jsonPath().getInt("data.custumField.accountid");

        ExtraField editExtraField = new ExtraField();
        CustomFieldAlbatross editCustomField = new CustomFieldAlbatross();

        editExtraField.setId(customFieldId);
        editExtraField.setAccountid(accountId);
        editExtraField.setColumnid(columnId);
        editExtraField.setEntitytypeid(entityId);
        editExtraField.setExtrafieldname("Updated Number Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("999");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Number Field"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editJobCheckboxCustomField_Test() {
        String customFieldName = "Checkbox Field To Edit";
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
        int accountId = createResponse.jsonPath().getInt("data.custumField.accountid");

        ExtraField editExtraField = new ExtraField();
        CustomFieldAlbatross editCustomField = new CustomFieldAlbatross();

        editExtraField.setId(customFieldId);
        editExtraField.setAccountid(accountId);
        editExtraField.setColumnid(columnId);
        editExtraField.setEntitytypeid(entityId);
        editExtraField.setExtrafieldname("Updated Checkbox Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("1");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Checkbox Field"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editJobPhoneNumberCustomField_Test() {
        String customFieldName = "Phone Number Field To Edit";
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
        int accountId = createResponse.jsonPath().getInt("data.custumField.accountid");

        ExtraField editExtraField = new ExtraField();
        CustomFieldAlbatross editCustomField = new CustomFieldAlbatross();

        editExtraField.setId(customFieldId);
        editExtraField.setAccountid(accountId);
        editExtraField.setColumnid(columnId);
        editExtraField.setEntitytypeid(entityId);
        editExtraField.setExtrafieldname("Updated Phone Number Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("+1-555-123-4567");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Phone Number Field"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editJobEmailCustomField_Test() {
        String customFieldName = "Email Field To Edit";
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
        int accountId = createResponse.jsonPath().getInt("data.custumField.accountid");

        ExtraField editExtraField = new ExtraField();
        CustomFieldAlbatross editCustomField = new CustomFieldAlbatross();

        editExtraField.setId(customFieldId);
        editExtraField.setAccountid(accountId);
        editExtraField.setColumnid(columnId);
        editExtraField.setEntitytypeid(entityId);
        editExtraField.setExtrafieldname("Updated Email Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("updated@example.com");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Email Field"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editJobFileCustomField_Test() {
        String customFieldName = "File Field To Edit";
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
        int accountId = createResponse.jsonPath().getInt("data.custumField.accountid");

        ExtraField editExtraField = new ExtraField();
        CustomFieldAlbatross editCustomField = new CustomFieldAlbatross();

        editExtraField.setId(customFieldId);
        editExtraField.setAccountid(accountId);
        editExtraField.setColumnid(columnId);
        editExtraField.setEntitytypeid(entityId);
        editExtraField.setExtrafieldname("Updated File Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("updated_file.pdf");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated File Field"));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
        editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteJobLongTextCustomField_Test() {
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
    public void deleteJobDateCustomField_Test() {
        int entityId = 4;
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
    public void deleteJobNumberCustomField_Test() {
        int entityId = 4;
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
    public void deleteJobCheckboxCustomField_Test() {
        int entityId = 4;
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
    public void deleteJobPhoneNumberCustomField_Test() {
        int entityId = 4;
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
    public void deleteJobEmailCustomField_Test() {
        int entityId = 4;
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
    public void deleteJobFileCustomField_Test() {
        int entityId = 4;
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
    public void deleteJobDateTimeCustomField_Test() {
        int entityId = 4;
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
    public void unauthorizedUserCannotDeleteJobCustomField_Test() {
        int entityId = 4;
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
    public void createEntityTypeCustomFieldForJob_Test(String entityType, int columnId) {
        createEntityTypeCustomField(entityType, columnId);
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "candidateEntityCustomFieldData", groups = "nightly-build")
    public void editCandidateEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
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
    @Test(dataProvider = "companyEntityCustomFieldData", groups = "nightly-build")
    public void editCompanyEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
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
    @Test(dataProvider = "contactEntityCustomFieldData", groups = "nightly-build")
    public void editContactEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
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
    @Test(dataProvider = "jobEntityCustomFieldData", groups = "nightly-build")
    public void editJobEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
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
    @Test(dataProvider = "dealEntityCustomFieldData", groups = "nightly-build")
    public void editDealEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
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
    @Test(dataProvider = "userEntityCustomFieldData", groups = "nightly-build")
    public void editUserEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
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
    @Test(dataProvider = "teamEntityCustomFieldData", groups = "nightly-build")
    public void editTeamEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
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
    @Test(dataProvider = "candidateEntityCustomFieldData", groups = "nightly-build")
    public void deleteCandidateEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "companyEntityCustomFieldData", groups = "nightly-build")
    public void deleteCompanyEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "contactEntityCustomFieldData", groups = "nightly-build")
    public void deleteContactEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "jobEntityCustomFieldData", groups = "nightly-build")
    public void deleteJobEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "dealEntityCustomFieldData", groups = "nightly-build")
    public void deleteDealEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "userEntityCustomFieldData", groups = "nightly-build")
    public void deleteUserEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "teamEntityCustomFieldData", groups = "nightly-build")
    public void deleteTeamEntityTypeCustomFieldForJob_Test(int customFieldId, String originalFieldName, String customFieldType, int columnId, int entityId) {
        String deletePath = "custom-fields/delete/" + customFieldId;
        Response deleteResponse = RestClient.doPost("JSON", albatrossURL, deletePath, albatrossAuthToken, null, false, null);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
        deleteResponse.then().body("message_type", Matchers.is("is-success"));
    }

    @DataProvider
    public Object[][] entityTypeData() {
        return new Object[][] {
                {"candidate", 1},
                {"company", 2},
                {"contact", 3},
                {"job", 4},
                {"deal", 5},
                {"user", 6},
                {"team", 7}
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

        return new Object[][] { {customFieldId, customFieldName, customFieldType, columnId, entityId} };
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

        return new Object[][] { {customFieldId, customFieldName, customFieldType, columnId, entityId} };
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

        return new Object[][] { {customFieldId, customFieldName, customFieldType, columnId, entityId} };
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

        return new Object[][] { {customFieldId, customFieldName, customFieldType, columnId, entityId} };
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

        return new Object[][] { {customFieldId, customFieldName, customFieldType, columnId, entityId} };
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

        return new Object[][] { {customFieldId, customFieldName, customFieldType, columnId, entityId} };
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

        return new Object[][] { {customFieldId, customFieldName, customFieldType, columnId, entityId} };
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