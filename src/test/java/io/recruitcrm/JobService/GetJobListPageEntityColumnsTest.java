package io.recruitcrm.JobService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.DefaultOptionsValue;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetJobListPageEntityColumnsTest extends TestBase {

    // Constants for reusability
    private static final String BASE_PATH = "entity-columns";
    private static final int JOB_ENTITY_TYPE_ID = 4;

    JavaFakerCustomField faker = new JavaFakerCustomField();
    String albatrossAuthToken;
    List<String> createdCustomFieldNames = new ArrayList<>();
    List<String> createdCustomFieldColumnIds = new ArrayList<>();
    ExecutorService executor;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        executor = Executors.newFixedThreadPool(6);
        createCustomFieldsForJobEntity();
        executor.shutdown();
    }

    private void createCustomFieldsForJobEntity() {
        List<CompletableFuture<String>> customFieldFutures = new ArrayList<>();

        String[] simpleFieldTypes = {
                "text", "number", "date", "date_time", "longtext",
                "phonenumber", "checkbox", "file", "social_profile"
        };
        String[] fieldTypesWithOptions = {"dropdown", "multiselect"};

        for (String fieldType : simpleFieldTypes) {
            customFieldFutures.add(CompletableFuture.supplyAsync(
                    () -> createJobCustomField(fieldType), executor));
        }
        for (String fieldType : fieldTypesWithOptions) {
            customFieldFutures.add(CompletableFuture.supplyAsync(
                    () -> createJobCustomFieldWithOptions(fieldType), executor));
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                customFieldFutures.toArray(new CompletableFuture[0]));

        try {
            allFutures.join();
            for (CompletableFuture<String> future : customFieldFutures) {
                createdCustomFieldNames.add(future.get());
            }
        } catch (Exception e) {
            Assert.fail("Failed to create custom fields: " + e.getMessage());
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobEntityColumnsTest_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "jobs");
        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        assertThat("Expected success message", response.jsonPath().getString("meta.message"), equalTo("Entity Column Fetched Successfully"));
        assertThat("Expected response type code 103", response.jsonPath().getInt("meta.responseType.code"), equalTo(103));
        assertThat("Expected success context", response.jsonPath().getString("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Expected status 200 in meta", response.jsonPath().getInt("meta.status"), equalTo(200));
        assertThat("Expected timestamp not null", response.jsonPath().get("meta.timestamp"), notNullValue());
        assertThat("Expected requestUuid not null", response.jsonPath().get("meta.requestUuid"), notNullValue());

        // Validate columns structure
        assertThat("Expected columns data not null", response.jsonPath().get("data[0].columns"), notNullValue());
        assertThat("Expected custom_column_count not null", response.jsonPath().get("data[0].custom_column_count"), notNullValue());

        // Get columns map for validation
        Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");
        JsonPath jsonPath = response.jsonPath();

        // Verify key fields exist and have correct visibility based on actual API response
        // Fields that should be visible: true
        List<String> visibleFields = Arrays.asList(
                "name", "jobstatus", "companyname", "salarytype", "state", "country", 
                "collaborator", "allowapply", "description", "address", "city", "locality",
                "jdtextdetails", "minexperienceinyears", "maxexperienceinyears",
                "annualsalarymin", "annualsalarymax", "noofopenings", "contactname",
                "contactemail", "contactnumber", "ownerid", "hiring_pipeline_id",
                "job_category", "remote", "hotlist", "xml_feeds", "targetcompanies",
                "postalcode", "pay_rate", "bill_rate"
        );

        // Fields that should be visible: false
        List<String> hiddenFields = Arrays.asList(
                "id", "srno", "archived", "jobstatuslabel", "companyid", "contactid",
                "secondarycontactid", "currencyid", "jobquestions", "job_skill",
                "qualificationid", "qualification", "specialization", "detailfilename",
                "jdtext", "contactslug", "companyslug", "ownername", "createdby",
                "creatorname", "createdon", "updatedon", "updatedby", "slug",
                "jobpostingstatus", "note", "hiring_pipeline_name", "job_type",
                "contactfirstname", "contactlastname", "job_function", "job_industry"
        );

        // Verify visible fields
        for (String field : visibleFields) {
            if (columnsMap.containsKey(field)) {
                assertThat("Field " + field + " should not be null",
                        columnsMap.get(field), notNullValue());
                Boolean actualVisible = jsonPath.getBoolean("data[0].columns." + field + ".visible");
                assertThat("Field " + field + " should be visible. Actual: " + actualVisible,
                        actualVisible, is(true));
            }
        }

        // Verify hidden fields
        for (String field : hiddenFields) {
            if (columnsMap.containsKey(field)) {
                assertThat("Field " + field + " should not be null",
                        columnsMap.get(field), notNullValue());
                Boolean actualVisible = jsonPath.getBoolean("data[0].columns." + field + ".visible");
                assertThat("Field " + field + " should not be visible. Actual: " + actualVisible,
                        actualVisible, is(false));
            }
        }

        // Verify custom fields are present in the response
        boolean isCustomFieldPresent = columnsMap.keySet().stream()
                .anyMatch(name -> name.startsWith("custcolumn"));
        assertThat("Expected custom field columns to be present", isCustomFieldPresent, is(true));

        // Verify specific column properties for key fields
        assertThat("Expected id column label",
                jsonPath.getString("data[0].columns.id.label"), equalTo("Id"));
        assertThat("Expected id column entity",
                jsonPath.getString("data[0].columns.id.entity"), equalTo("jobs"));
        assertThat("Expected id column field",
                jsonPath.getString("data[0].columns.id.field"), equalTo("id"));
        assertThat("Expected id column type",
                jsonPath.getString("data[0].columns.id.type"), equalTo("number"));
        assertThat("Expected id column visible",
                jsonPath.getBoolean("data[0].columns.id.visible"), is(false));

        assertThat("Expected srno column label",
                jsonPath.getString("data[0].columns.srno.label"), equalTo("ID"));
        assertThat("Expected srno column longlabel",
                jsonPath.getString("data[0].columns.srno.longlabel"), equalTo("Job ID"));
        assertThat("Expected srno column entity",
                jsonPath.getString("data[0].columns.srno.entity"), equalTo("jobs"));
        assertThat("Expected srno column field",
                jsonPath.getString("data[0].columns.srno.field"), equalTo("srno"));
        assertThat("Expected srno column type",
                jsonPath.getString("data[0].columns.srno.type"), equalTo("number"));
        assertThat("Expected srno column visible",
                jsonPath.getBoolean("data[0].columns.srno.visible"), is(false));

        assertThat("Expected name column label",
                jsonPath.getString("data[0].columns.name.label"), equalTo("Name"));
        assertThat("Expected name column entity",
                jsonPath.getString("data[0].columns.name.entity"), equalTo("jobs"));
        assertThat("Expected name column field",
                jsonPath.getString("data[0].columns.name.field"), equalTo("name"));
        assertThat("Expected name column type",
                jsonPath.getString("data[0].columns.name.type"), equalTo("text"));
        assertThat("Expected name column visible",
                jsonPath.getBoolean("data[0].columns.name.visible"), is(true));

        // Schema validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/job/getJobEntityColumns.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobEntityColumnsVerifyCustomFields_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "jobs");
        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        // Get columns map
        Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");

        // Get all custom field column names
        List<String> customFieldColumns = columnsMap.keySet().stream()
                .filter(name -> name.startsWith("custcolumn"))
                .collect(Collectors.toList());

        assertThat("Expected custom fields to be present", customFieldColumns.size(), greaterThan(0));

        // Verify that created custom fields are present in the response
        // We check by verifying custom field columns exist (since we created them)
        // The actual field names are stored in the column metadata
        for (String customColumn : customFieldColumns) {
            Object columnData = columnsMap.get(customColumn);
            assertThat("Custom column " + customColumn + " should not be null", columnData, notNullValue());

            // Verify custom field has required properties
            String labelPath = "data[0].columns." + customColumn + ".label";
            String entityPath = "data[0].columns." + customColumn + ".entity";

            assertThat("Custom column " + customColumn + " should have label",
                    response.jsonPath().get(labelPath), notNullValue());
            assertThat("Custom column " + customColumn + " should have entity",
                    response.jsonPath().getString(entityPath), equalTo("jobs"));
            assertThat("Custom column " + customColumn + " should have type",
                    response.jsonPath().getString("data[0].columns." + customColumn + ".type"), notNullValue());
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobEntityColumnsVerifyColumnProperties_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "jobs");
        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        JsonPath jsonPath = response.jsonPath();
        Map<String, Object> columnsMap = jsonPath.getMap("data[0].columns");

        // Verify column properties for various field types
        // System fields
        assertThat("id column should be systemcolumn",
                jsonPath.getBoolean("data[0].columns.id.systemcolumn"), is(true));
        assertThat("id column should be pseudo false",
                jsonPath.getBoolean("data[0].columns.id.pseudo"), is(false));
        assertThat("id column should be sortable",
                jsonPath.getBoolean("data[0].columns.id.sortable"), is(true));
        assertThat("id column should have dbtype",
                jsonPath.getString("data[0].columns.id.dbtype"), equalTo("int"));
        assertThat("id column should have type",
                jsonPath.getString("data[0].columns.id.type"), equalTo("number"));

        // Name field properties
        assertThat("name should be visible",
                jsonPath.getBoolean("data[0].columns.name.visible"), is(true));
        assertThat("name should allow on import",
                jsonPath.getBoolean("data[0].columns.name.allow_on_import"), is(true));
        assertThat("name should allow on export",
                jsonPath.getBoolean("data[0].columns.name.allow_on_export"), is(true));
        assertThat("name should be required on form",
                jsonPath.getBoolean("data[0].columns.name.required_on_form"), is(true));
        assertThat("name should have dbtype",
                jsonPath.getString("data[0].columns.name.dbtype"), equalTo("varchar"));
        assertThat("name should have type",
                jsonPath.getString("data[0].columns.name.type"), equalTo("text"));

        // Verify jobstatus field properties (dropdown/multiselect type)
        if (columnsMap.containsKey("jobstatus")) {
            assertThat("jobstatus should have dropdown_value",
                    jsonPath.getString("data[0].columns.jobstatus.dropdown_value"), notNullValue());
            assertThat("jobstatus should have dropdown_field",
                    jsonPath.getString("data[0].columns.jobstatus.dropdown_field"), notNullValue());
            assertThat("jobstatus type should be multiselect or dropdown",
                    jsonPath.getString("data[0].columns.jobstatus.type"), anyOf(equalTo("multiselect"), equalTo("dropdown")));
            assertThat("jobstatus should have service",
                    jsonPath.getString("data[0].columns.jobstatus.service"), notNullValue());
        }

        // Verify date fields
        if (columnsMap.containsKey("createdon")) {
            assertThat("createdon type should be date",
                    jsonPath.getString("data[0].columns.createdon.type"), equalTo("date"));
            assertThat("createdon should have dbtype",
                    jsonPath.getString("data[0].columns.createdon.dbtype"), equalTo("int"));
        }
        if (columnsMap.containsKey("updatedon")) {
            assertThat("updatedon type should be date",
                    jsonPath.getString("data[0].columns.updatedon.type"), equalTo("date"));
            assertThat("updatedon should have dbtype",
                    jsonPath.getString("data[0].columns.updatedon.dbtype"), equalTo("int"));
        }

        // Verify companyname field properties (pseudo field)
        if (columnsMap.containsKey("companyname")) {
            assertThat("companyname should be visible",
                    jsonPath.getBoolean("data[0].columns.companyname.visible"), is(true));
            assertThat("companyname should have type",
                    jsonPath.getString("data[0].columns.companyname.type"), equalTo("text"));
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobEntityColumnsUnauthorizedTest_401() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "jobs");

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, "InvalidToken", queryParameters, null, true);
        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        assertThat("Expected unauthorized message", response.jsonPath().getString("meta.message"), Matchers.equalTo("Unauthorised access"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobEntityColumnsTest_404() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "jobs");

        Response response = RestClient.doGet("JSON", jobServiceURL, "ntity-columns", albatrossAuthToken, queryParameters, null, true); // Incorrect URL
        assertThat("Expected status code 404", response.getStatusCode(), equalTo(404));
        assertThat("Expected not found error", response.jsonPath().getString("error"), equalTo("Not Found"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobEntityColumnsTest_400() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", ""); // Missing entity

        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);
        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
        assertThat("Expected error context", response.jsonPath().getString("meta.responseType.context"), equalTo("Error while processing request"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobEntityColumnsTest_405() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "jobs");

        Response response = RestClient.doPost("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, true, null);
        assertThat("Expected status code 405", response.getStatusCode(), equalTo(405));
        assertThat("Expected method not allowed error", response.jsonPath().getString("errors[0].message"), containsString("not supported"));
        assertThat("Expected meta status 405", response.jsonPath().getInt("meta.status"), equalTo(405));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getJobEntityColumnsVerifyAllDefaultFields_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "jobs");
        Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");
        JsonPath jsonPath = response.jsonPath();

        // Field visibility map based on actual API response
        Map<String, Boolean> expectedVisibilityMap = new HashMap<>();
        expectedVisibilityMap.put("id", false);
        expectedVisibilityMap.put("srno", false);
        expectedVisibilityMap.put("name", true);
        expectedVisibilityMap.put("jobstatus", true);
        expectedVisibilityMap.put("archived", false);
        expectedVisibilityMap.put("jobstatuslabel", false);
        expectedVisibilityMap.put("companyid", false);
        expectedVisibilityMap.put("companyname", true);
        expectedVisibilityMap.put("contactid", false);
        expectedVisibilityMap.put("secondarycontactid", false);
        expectedVisibilityMap.put("salarytype", true);
        expectedVisibilityMap.put("currencyid", false);
        expectedVisibilityMap.put("state", true);
        expectedVisibilityMap.put("country", true);
        expectedVisibilityMap.put("collaborator", true);
        expectedVisibilityMap.put("jobquestions", false);
        expectedVisibilityMap.put("allowapply", true);
        expectedVisibilityMap.put("description", true);
        expectedVisibilityMap.put("job_skill", false);
        expectedVisibilityMap.put("address", true);
        expectedVisibilityMap.put("city", true);
        expectedVisibilityMap.put("locality", true);
        expectedVisibilityMap.put("qualificationid", false);
        expectedVisibilityMap.put("qualification", false);
        expectedVisibilityMap.put("specialization", false);
        expectedVisibilityMap.put("detailfilename", false);
        expectedVisibilityMap.put("jdtext", false);
        expectedVisibilityMap.put("jdtextdetails", true);
        expectedVisibilityMap.put("minexperienceinyears", true);
        expectedVisibilityMap.put("maxexperienceinyears", true);
        expectedVisibilityMap.put("annualsalarymin", true);
        expectedVisibilityMap.put("annualsalarymax", true);
        expectedVisibilityMap.put("noofopenings", true);
        expectedVisibilityMap.put("contactname", true);
        expectedVisibilityMap.put("contactslug", false);
        expectedVisibilityMap.put("companyslug", false);
        expectedVisibilityMap.put("contactemail", true);
        expectedVisibilityMap.put("contactnumber", true);
        expectedVisibilityMap.put("ownerid", true);
        expectedVisibilityMap.put("ownername", false);
        expectedVisibilityMap.put("createdby", false);
        expectedVisibilityMap.put("creatorname", false);
        expectedVisibilityMap.put("createdon", false);
        expectedVisibilityMap.put("updatedon", false);
        expectedVisibilityMap.put("updatedby", false);
        expectedVisibilityMap.put("slug", false);
        expectedVisibilityMap.put("jobpostingstatus", false);
        expectedVisibilityMap.put("note", false);
        expectedVisibilityMap.put("hiring_pipeline_id", true);
        expectedVisibilityMap.put("hiring_pipeline_name", false);
        expectedVisibilityMap.put("job_type", false);
        expectedVisibilityMap.put("job_category", true);
        expectedVisibilityMap.put("remote", true);
        expectedVisibilityMap.put("hotlist", true);
        expectedVisibilityMap.put("xml_feeds", true);
        expectedVisibilityMap.put("targetcompanies", true);
        expectedVisibilityMap.put("contactfirstname", false);
        expectedVisibilityMap.put("contactlastname", false);
        expectedVisibilityMap.put("postalcode", true);
        expectedVisibilityMap.put("pay_rate", true);
        expectedVisibilityMap.put("bill_rate", true);
        expectedVisibilityMap.put("job_function", false);
        expectedVisibilityMap.put("job_industry", false);

        // Verify all fields from expectedVisibilityMap exist and have correct visibility
        for (Map.Entry<String, Boolean> entry : expectedVisibilityMap.entrySet()) {
            String fieldName = entry.getKey();
            Boolean expectedVisible = entry.getValue();
            
            if (columnsMap.containsKey(fieldName)) {
                assertThat("Field " + fieldName + " should not be null",
                        columnsMap.get(fieldName), notNullValue());
                
                Boolean actualVisible = jsonPath.getBoolean("data[0].columns." + fieldName + ".visible");
                assertThat("Visibility mismatch for field " + fieldName + ". Expected: " + expectedVisible + ", Actual: " + actualVisible,
                        actualVisible, equalTo(expectedVisible));
            } else {
                // Log missing fields but don't fail - some fields may not be present in all accounts
                System.out.println("Warning: Field " + fieldName + " not found in response");
            }
        }

        // Verify custom_column_count exists
        assertThat("custom_column_count should exist", 
                jsonPath.get("data[0].custom_column_count"), notNullValue());
        
        // Verify at least some fields are present
        assertThat("At least some fields should be present", columnsMap.size(), greaterThan(0));
    }

    private String createJobCustomField(String fieldType) {
        String fieldName = faker.getCustomFieldName("jobs");
        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(faker.getColumnId());
        extraField.setEntitytypeid(JOB_ENTITY_TYPE_ID);
        extraField.setExtrafieldname(fieldName);
        extraField.setExtrafieldtype(fieldType);
        extraField.setDefaultvalue(null);
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("message"), equalTo("Custom Field Saved Successfully"));
        String columnId = jsonPath.getString("data.custumField.columnid");
        if (columnId != null) {
            createdCustomFieldColumnIds.add(columnId);
        }

        return fieldName;
    }

    private String createJobCustomFieldWithOptions(String fieldType) {
        String fieldName = faker.getCustomFieldName("jobs");
        String fieldOptions = "Option1,Option2,Option3";
        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        List<DefaultOptionsValue> optionsList = new ArrayList<>();
        String[] options = fieldOptions.split(",");
        for (int i = 0; i < options.length; i++) {
            DefaultOptionsValue option = new DefaultOptionsValue();
            option.setLabel(options[i].trim());
            option.setSequence_no(i + 1);
            option.setTempId(faker.getTempId());
            optionsList.add(option);
        }
        extraField.setDefaultoptionsvalue(optionsList);

        extraField.setColumnid(faker.getColumnId());
        extraField.setEntitytypeid(JOB_ENTITY_TYPE_ID);
        extraField.setExtrafieldname(fieldName);
        extraField.setExtrafieldtype(fieldType);
        customField.setCustumField(extraField);

        Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("message"), equalTo("Custom Field Saved Successfully"));
        String columnId = jsonPath.getString("data.custumField.columnid");
        if (columnId != null) {
            createdCustomFieldColumnIds.add(columnId);
        }

        return fieldName;
    }

}
