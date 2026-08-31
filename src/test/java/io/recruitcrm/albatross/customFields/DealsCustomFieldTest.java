package io.recruitcrm.albatross.customFields;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import java.util.ArrayList;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DealsCustomFieldTest extends TestBase {
    String albatrossAuthToken;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealLongTextCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Long Text Field";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealDateCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Date Field";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealNumberCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Number Field";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealCheckboxCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Checkbox Field";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealDateTimeCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Date-Time Field";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealFileCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal File Field";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealCandidateCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Candidate Field";
        String customFieldType = "candidate";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealCompanyCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Company Field";
        String customFieldType = "company";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealContactCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Contact Field";
        String customFieldType = "contact";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealJobCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Job Field";
        String customFieldType = "job";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealDealsCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Deals Field";
        String customFieldType = "deals";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealUserCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal User Field";
        String customFieldType = "user";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createDealTeamCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Team Field";
        String customFieldType = "team";
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
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealLongTextCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Long Text Field To Edit";
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
        editExtraField.setExtrafieldname("Updated Deal Long Text Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("Updated value");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Long Text Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealDateCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Date Field To Edit";
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
        editExtraField.setExtrafieldname("Updated Deal Date Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("2024-01-01");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Date Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealNumberCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Number Field To Edit";
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
        editExtraField.setExtrafieldname("Updated Deal Number Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("12345");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Number Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealCheckboxCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Checkbox Field To Edit";
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
        editExtraField.setExtrafieldname("Updated Deal Checkbox Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("true");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Checkbox Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealDateTimeCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Date-Time Field To Edit";
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
        editExtraField.setExtrafieldname("Updated Deal Date-Time Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("2024-01-01T12:00:00Z");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Date-Time Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealFileCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal File Field To Edit";
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
        editExtraField.setExtrafieldname("Updated Deal File Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("updated_file.pdf");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal File Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealCandidateCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Candidate Field To Edit";
        String customFieldType = "candidate";
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
        editExtraField.setExtrafieldname("Updated Deal Candidate Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("123");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Candidate Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealCompanyCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Company Field To Edit";
        String customFieldType = "company";
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
        editExtraField.setExtrafieldname("Updated Deal Company Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("456");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Company Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealContactCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Contact Field To Edit";
        String customFieldType = "contact";
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
        editExtraField.setExtrafieldname("Updated Deal Contact Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("789");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Contact Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealJobCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Job Field To Edit";
        String customFieldType = "job";
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
        editExtraField.setExtrafieldname("Updated Deal Job Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("1011");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Job Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealDealsCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Deals Field To Edit";
        String customFieldType = "deals";
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
        editExtraField.setExtrafieldname("Updated Deal Deals Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("2024");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Deals Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealUserCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal User Field To Edit";
        String customFieldType = "user";
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
        editExtraField.setExtrafieldname("Updated Deal User Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("user_updated");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal User Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void editDealTeamCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Team Field To Edit";
        String customFieldType = "team";
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
        editExtraField.setExtrafieldname("Updated Deal Team Field");
        editExtraField.setExtrafieldtype(customFieldType);
        editExtraField.setDefaultvalue("team_updated");
        editExtraField.setDefaultoptionsvalue(new ArrayList<>());
        editCustomField.setCustumField(editExtraField);

        String editPath = "custom-fields/" + customFieldId;
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, editPath, albatrossAuthToken, null, null, false, editCustomField);
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
        editResponse.then().body("message_type", Matchers.is("is-success"));
        editResponse.then().body("data.custumField.extrafieldname", Matchers.is("Updated Deal Team Field"));
        editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
        editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
        editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void deleteDealLongTextCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Long Text Field To Delete";
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
    public void deleteDealDateCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Date Field To Delete";
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
    public void deleteDealNumberCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Number Field To Delete";
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
    public void deleteDealCheckboxCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Checkbox Field To Delete";
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
    public void deleteDealDateTimeCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Date-Time Field To Delete";
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
    public void deleteDealFileCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal File Field To Delete";
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
    public void deleteDealCandidateCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Candidate Field To Delete";
        String customFieldType = "candidate";
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
    public void deleteDealCompanyCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Company Field To Delete";
        String customFieldType = "company";
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
    public void deleteDealContactCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Contact Field To Delete";
        String customFieldType = "contact";
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
    public void deleteDealJobCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Job Field To Delete";
        String customFieldType = "job";
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
    public void deleteDealDealsCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Deals Field To Delete";
        String customFieldType = "deals";
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
    public void deleteDealUserCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal User Field To Delete";
        String customFieldType = "user";
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
    public void deleteDealTeamCustomField_Test() {
        int entityId = 11;
        String customFieldName = "Deal Team Field To Delete";
        String customFieldType = "team";
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
}
