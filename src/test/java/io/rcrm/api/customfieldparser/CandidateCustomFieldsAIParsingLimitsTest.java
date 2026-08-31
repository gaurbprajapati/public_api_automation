package io.rcrm.api.customfieldparser;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.DefaultOptionsValue;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.pojo.albatross.NestedCustomFieldPojo;
import io.rcrm.api.pojo.albatross.UpdateFieldStatus;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AccountType("Business|AlbatrossTkn")
public class CandidateCustomFieldsAIParsingLimitsTest extends TestBase {

    private static final int CANDIDATE_ENTITY_TYPE_ID = 5;

    @Test(dataProvider = "tenAIEnabledFieldsData")
    public void createTenFieldsWithAIParsing(int columnIndex, String fieldName, String fieldType, String description) {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        Response response = createFieldWithAIParsing(authToken, columnIndex, fieldName, fieldType, description, null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"), "Custom Field Saved Successfully");
        Assert.assertEquals(jp.getString("message_type"), "is-success");
        Assert.assertTrue(jp.getBoolean("data.custumField.is_parser_enabled"));
        Assert.assertEquals(jp.getString("data.custumField.description"), description);
    }

    @Test
    public void createEleventhFieldShouldFailWithMaxLimit() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        for (int i = 1; i <= 10; i++) {
            Response response = createFieldWithAIParsing(authToken, i, "AI Field " + i, 
                    getFieldType(i), "AI parsing enabled field for parsing : " + i, null);
            Assert.assertEquals(response.getStatusCode(), 200);
        }
        
        Response response = createFieldWithAIParsing(authToken, 1, "AI Field 11", 
                "text", "This should fail", null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"), 
                "You have reached the maximum limit of 10 custom fields with AI parsing enabled for your plan.");
        Assert.assertEquals(jp.getString("message_type"), "is-danger");
    }

    @Test(dataProvider = "nineFieldsData")
    public void createNineFieldsThenUpdateWithAIParsing(int columnIndex, String fieldName, 
            String fieldType, String description) {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        Response createResponse = createFieldWithoutAIParsing(authToken, columnIndex, fieldName, fieldType, null);
        Assert.assertEquals(createResponse.getStatusCode(), 200);
        int fieldId = createResponse.jsonPath().getInt("data.custumField.id");
        
        Response updateResponse = updateFieldWithAIParsing(authToken, fieldId, columnIndex, 
                fieldName + " Updated", fieldType, description, null);
        
        Assert.assertEquals(updateResponse.getStatusCode(), 200);
        JsonPath jp = updateResponse.jsonPath();
        Assert.assertEquals(jp.getString("message"), "Custom Field Saved Successfully");
        Assert.assertTrue(jp.getBoolean("data.custumField.is_parser_enabled"));
        Assert.assertEquals(jp.getString("data.custumField.description"), description);
    }

    @Test
    public void dropdownWith101OptionsShouldNotAllowAIParsing() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        List<String> options = new ArrayList<>();
        for (int i = 1; i <= 101; i++) {
            options.add("Option " + i);
        }
        
        Response response = createFieldWithAIParsing(authToken, 1, "Dropdown 101 Options", 
                "dropdown", "Dropdown with 101 options", options);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"),
                "You cannot enable AI parsing for this custom field because it has more than 100 options.");
        Assert.assertEquals(jp.getString("message_type"), "is-danger");
    }

    @Test
    public void multiselectWith101OptionsShouldNotAllowAIParsing() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        List<String> options = new ArrayList<>();
        for (int i = 1; i <= 101; i++) {
            options.add("Option " + i);
        }
        
        Response response = createFieldWithAIParsing(authToken, 2, "Multiselect 101 Options", 
                "multiselect", "Multiselect with 101 options", options);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"),
                "You cannot enable AI parsing for this custom field because it has more than 100 options.");
        Assert.assertEquals(jp.getString("message_type"), "is-danger");
    }

    @Test
    public void dropdownWith100OptionsShouldAllowAIParsing() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        List<String> options = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            options.add("Option " + i);
        }
        
        Response response = createFieldWithAIParsing(authToken, 1, "Dropdown 100 Options", 
                "dropdown", "Dropdown with exactly 100 options", options);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"), "Custom Field Saved Successfully");
        Assert.assertTrue(jp.getBoolean("data.custumField.is_parser_enabled"));
    }


    @Test
    public void dateTimeFieldShouldNotAllowAIParsing() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        Response response = createFieldWithAIParsing(authToken, 1, "DateTime Field", 
                "date_time", "DateTime field with AI", null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"),
                "You cannot enable AI parsing for this custom field because it is a Date Time field.");
        Assert.assertEquals(jp.getString("message_type"), "is-danger");
    }

    @Test
    public void fileFieldShouldNotAllowAIParsing() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        Response response = createFieldWithAIParsing(authToken, 1, "File Field", 
                "file", "File field with AI", null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"),
                "You cannot enable AI parsing for this custom field because it is a File field.");
        Assert.assertEquals(jp.getString("message_type"), "is-danger");
    }

    @Test(dataProvider = "entityTypeFieldsData")
    public void entityTypeFieldsShouldNotAllowAIParsing(String entityType) {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        Response response = createFieldWithAIParsing(authToken, 1, entityType + " Field", 
                entityType, entityType + " field created with AI parsing enabled", null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"),
                "You cannot enable AI parsing for this custom field because it is an entity type field.");
        Assert.assertEquals(jp.getString("message_type"), "is-danger");
    }

    @Test
    public void descriptionBelowMinimumShouldFail() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        String shortDescription = "Short desc";
        
        Response response = createFieldWithAIParsing(authToken, 1, "Test Field", 
                "text", shortDescription, null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"),
                "Description should be at least 30 characters and less than 500 characters");
        Assert.assertEquals(jp.getString("message_type"), "is-danger");
    }

    @Test
    public void descriptionAtMinimumShouldSucceed() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        String minDescription = "This is exactly thirty chars!!";
        
        Response response = createFieldWithAIParsing(authToken, 1, "Test Field", 
                "text", minDescription, null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"), "Custom Field Saved Successfully");
        Assert.assertEquals(jp.getString("data.custumField.description"), minDescription);
    }

    @Test
    public void descriptionAtMaximumShouldSucceed() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("a");
        }
        String maxDescription = sb.toString();
        
        Response response = createFieldWithAIParsing(authToken, 1, "Test Field", 
                "text", maxDescription, null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"), "Custom Field Saved Successfully");
        Assert.assertEquals(jp.getString("data.custumField.description"), maxDescription);
    }

    @Test
    public void descriptionAboveMaximumShouldFail() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            sb.append("a");
        }
        String longDescription = sb.toString();
        
        Response response = createFieldWithAIParsing(authToken, 1, "Test Field", 
                "text", longDescription, null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"),
                "Description should be at least 30 characters and less than 500 characters");
        Assert.assertEquals(jp.getString("message_type"), "is-danger");
    }

    @Test
    public void emptyDescriptionShouldFail() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        Response response = createFieldWithAIParsing(authToken, 1, "Test Field", 
                "text", "", null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"),
                "Description should be at least 30 characters and less than 500 characters");
        Assert.assertEquals(jp.getString("message_type"), "is-danger");
    }

    @Test
    public void nullDescriptionShouldFail() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();
        
        Response response = createFieldWithAIParsing(authToken, 1, "Test Field", 
                "text", null, null);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"),
                "Set the fields description and is_parser_enabled");
        Assert.assertEquals(jp.getString("message_type"), "is-danger");
    }

    @DataProvider
    public Object[][] tenAIEnabledFieldsData() {
        String[] types = {"text", "longtext", "date", "number", "checkbox", 
                         "phonenumber", "email", "social_profile", "text", "longtext"};
        Object[][] data = new Object[10][];
        for (int i = 1; i <= 10; i++) {
            data[i - 1] = new Object[]{
                i,
                "AI Enabled Field " + i,
                types[i - 1],
                "AI parsing enabled field number " + i + " for comprehensive testing."
            };
        }
        return data;
    }

    @DataProvider
    public Object[][] nineFieldsData() {
        String[] types = {"text", "longtext", "date", "number", "checkbox", 
                         "phonenumber", "email", "social_profile", "text"};
        Object[][] data = new Object[9][];
        for (int i = 1; i <= 9; i++) {
            data[i - 1] = new Object[]{
                i,
                "Field Without AI " + i,
                types[i - 1],
                "Updated description with AI parsing enabled for field " + i + "."
            };
        }
        return data;
    }

    @DataProvider
    public Object[][] nestedDependencyData() {
        return new Object[][]{
            {"dropdown", "dropdown", 1, 2},
            {"dropdown", "multiselect", 3, 4},
            {"multiselect", "dropdown", 5, 6}
        };
    }

    @DataProvider
    public Object[][] entityTypeFieldsData() {
        return new Object[][]{
            {"candidate"},
            {"company"},
            {"contact"},
            {"job"},
            {"deals"},
            {"user"},
            {"team"}
        };
    }

    @Test
    public void eeoComplianceEnabledBlocksViolatingCustomField() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();

        setEeoCompliance(authToken, true);
            Response response = createFieldWithAIParsing(authToken, 1, "Candidate Gender",
                    "dropdown",
                    "This field captures candidate gender information for diversity and EEO reporting.",
                    List.of("Male", "Female", "Non-binary", "Prefer not to say"));

            Assert.assertEquals(response.getStatusCode(), 200);
            JsonPath jp = response.jsonPath();
            Assert.assertEquals(jp.getString("message"), "Input may violate EEO standards. Please check.");
            Assert.assertEquals(jp.getString("message_type"), "is-danger");

    }

    @Test
    public void eeoComplianceDisabledAllowsViolatingCustomField() {
        String authToken = ThreadManager.getOwnerAlbatrossToken();

        setEeoCompliance(authToken, false);

        Response response = createFieldWithAIParsing(authToken, 1, "Candidate Race",
                "dropdown",
                "This field captures candidate race or ethnicity for diversity and EEO reporting purposes.",
                List.of("Asian", "Black or African American", "Hispanic", "White", "Other"));

        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.getString("message"), "Custom Field Saved Successfully");
        Assert.assertEquals(jp.getString("message_type"), "is-success");
        Assert.assertTrue(jp.getBoolean("data.custumField.is_parser_enabled"));
    }

    private void setEeoCompliance(String authToken, boolean enabled) {
        UpdateFieldStatus settings = new UpdateFieldStatus();
        settings.setKey("eeocompliance");
        settings.setValue(enabled ? "1" : "0");
        settings.setTableFlag("account");
        settings.setId(ThreadManager.getAccount().getAccountId());
        Response r = RestClient.doPost("JSON", albatrossURL, "global/update-fields", authToken, null, true, settings);
        Assert.assertEquals(r.getStatusCode(), 200, "Failed to " + (enabled ? "enable" : "disable") + " EEO compliance");
    }

    private Response createFieldWithAIParsing(String authToken, int columnIndex, String name,
            String type, String description, List<String> options) {
        ExtraField ef = new ExtraField();
        ef.setEntitytypeid(CANDIDATE_ENTITY_TYPE_ID);
        ef.setExtrafieldname(name);
        ef.setExtrafieldtype(type);
        ef.setColumnid(columnIndex);
        ef.setDescription(description);
        ef.setIs_parser_enabled(Boolean.TRUE);
        ef.setDefaultvalue(null);
        
        if (options != null && !options.isEmpty()) {
            List<DefaultOptionsValue> optionsList = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                DefaultOptionsValue option = new DefaultOptionsValue();
                option.setLabel(options.get(i));
                option.setSequence_no(i + 1);
                option.setTempId(UUID.randomUUID().toString());
                optionsList.add(option);
            }
            ef.setDefaultoptionsvalue(optionsList);
        } else {
            ef.setDefaultoptionsvalue(new ArrayList<>());
        }
        
        CustomFieldAlbatross cf = new CustomFieldAlbatross();
        cf.setCustumField(ef);
        
        return RestClient.doPost("JSON", albatrossURL, "custom-fields", authToken, null, true, cf);
    }

    private Response createFieldWithoutAIParsing(String authToken, int columnIndex, String name, 
            String type, List<String> options) {
        ExtraField ef = new ExtraField();
        ef.setEntitytypeid(CANDIDATE_ENTITY_TYPE_ID);
        ef.setExtrafieldname(name);
        ef.setExtrafieldtype(type);
        ef.setColumnid(columnIndex);
        ef.setIs_parser_enabled(Boolean.FALSE);
        ef.setDefaultvalue(null);
        
        if (options != null && !options.isEmpty()) {
            List<DefaultOptionsValue> optionsList = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                DefaultOptionsValue option = new DefaultOptionsValue();
                option.setLabel(options.get(i));
                option.setSequence_no(i + 1);
                option.setTempId(UUID.randomUUID().toString());
                optionsList.add(option);
            }
            ef.setDefaultoptionsvalue(optionsList);
        } else {
            ef.setDefaultoptionsvalue(new ArrayList<>());
        }
        
        CustomFieldAlbatross cf = new CustomFieldAlbatross();
        cf.setCustumField(ef);
        
        return RestClient.doPost("JSON", albatrossURL, "custom-fields", authToken, null, false, cf);
    }

    private Response updateFieldWithAIParsing(String authToken, int fieldId, int columnIndex, 
            String name, String type, String description, List<String> options) {
        ExtraField ef = new ExtraField();
        ef.setEntitytypeid(CANDIDATE_ENTITY_TYPE_ID);
        ef.setExtrafieldname(name);
        ef.setExtrafieldtype(type);
        ef.setColumnid(columnIndex);
        ef.setDescription(description);
        ef.setIs_parser_enabled(Boolean.TRUE);
        ef.setDefaultvalue(null);
        
        if (options != null && !options.isEmpty()) {
            List<DefaultOptionsValue> optionsList = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                DefaultOptionsValue option = new DefaultOptionsValue();
                option.setLabel(options.get(i));
                option.setSequence_no(i + 1);
                option.setTempId(UUID.randomUUID().toString());
                optionsList.add(option);
            }
            ef.setDefaultoptionsvalue(optionsList);
        } else {
            ef.setDefaultoptionsvalue(new ArrayList<>());
        }
        
        CustomFieldAlbatross cf = new CustomFieldAlbatross();
        cf.setCustumField(ef);
        
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", String.valueOf(fieldId));
        
        return RestClient.doPost1("JSON", albatrossURL, "custom-fields/{id}", authToken,
                null, pathParams, false, cf);
    }

    private String getFieldType(int index) {
        String[] types = {"text", "longtext", "date", "number", "checkbox", 
                         "phonenumber", "email", "social_profile", "text"};
        return types[(index - 1) % types.length];
    }

}
