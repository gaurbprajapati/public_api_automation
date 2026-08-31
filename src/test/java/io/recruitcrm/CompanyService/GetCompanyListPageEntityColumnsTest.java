package io.recruitcrm.CompanyService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
public class GetCompanyListPageEntityColumnsTest extends TestBase {

    // Constants for reusability
    private static final String BASE_PATH = "entity-columns";
    private static final int COMPANY_ENTITY_TYPE_ID = 3;

    JavaFakerCustomField faker = new JavaFakerCustomField();
    String albatrossAuthToken;
    List<String> createdCustomFieldNames = new ArrayList<>();
    List<String> createdCustomFieldColumnIds = new ArrayList<>();
    ExecutorService executor;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        executor = Executors.newFixedThreadPool(6); // Create thread pool for parallel custom field creation

        // Create various types of custom fields for companies in parallel
        List<CompletableFuture<String>> customFieldFutures = new ArrayList<>();

        // Create simple custom fields in parallel
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomField("text"), executor));
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomField("number"), executor));
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomField("date"), executor));
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomField("date_time"), executor));
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomField("longtext"), executor));
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomField("phonenumber"), executor));
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomField("checkbox"), executor));
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomField("file"), executor));
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomField("social_profile"), executor));

        // Create custom fields with options in parallel
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomFieldWithOptions("dropdown"), executor));
        customFieldFutures.add(CompletableFuture.supplyAsync(() -> createCompanyCustomFieldWithOptions("multiselect"), executor));

        // Wait for all custom fields to be created and collect results
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                customFieldFutures.toArray(new CompletableFuture[0]));

        try {
            allFutures.join(); // Wait for all to complete
            // Collect all field names
            for (CompletableFuture<String> future : customFieldFutures) {
                createdCustomFieldNames.add(future.get());
            }
        } catch (Exception e) {
            Assert.fail("Failed to create custom fields: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyEntityColumnsTest_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        assertThat("Expected success message", response.jsonPath().getString("meta.message"), equalTo("Entity Column Fetched Successfully"));
        assertThat("Expected response type code 103", response.jsonPath().getInt("meta.responseType.code"), equalTo(103));
        assertThat("Expected success context", response.jsonPath().getString("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Expected status 200 in meta", response.jsonPath().getInt("meta.status"), equalTo(200));
        assertThat("Expected timestamp not null", response.jsonPath().get("meta.timestamp"), notNullValue());
        assertThat("Expected requestUuid not null", response.jsonPath().get("meta.requestUuid"), notNullValue());

        // Validate columns structure
        assertThat("Expected columns data not null", response.jsonPath().get("data[0].columns"), notNullValue());

        // Get columns map for validation
        Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");

        // List of all default/required columns based on the API response
        List<String> requiredColumns = Arrays.asList(
                "id", "parentcompanyid", "parentcompanyslug", "haschildren", "hasparent",
                "srno", "companyname", "aboutcompany", "parentcompanyname", "address", "city",
                "industryid", "industry", "website", "totalopenjob", "totalclosedjob",
                "totalonholdjob", "totalcanceledjob", "logo", "ownerid", "profilefacebook",
                "profiletwitter", "profilelinkedin", "ownername", "creatorname", "accountid",
                "deleted", "createdby", "createdon", "updatedon", "updatedby"
        );

        // Verify all required columns are present
        for (String column : requiredColumns) {
            assertThat("Missing expected column: " + column,
                    columnsMap.containsKey(column), is(true));
            assertThat("Column " + column + " should not be null",
                    columnsMap.get(column), notNullValue());
        }

        // Verify custom fields are present in the response
        boolean isCustomFieldPresent = columnsMap.keySet().stream()
                .anyMatch(name -> name.startsWith("custcolumn"));
        assertThat("Expected custom field columns to be present", isCustomFieldPresent, is(true));

        // Verify specific column properties for key fields
        assertThat("Expected id column label",
                response.jsonPath().getString("data[0].columns.id.label"), equalTo("Id"));
        assertThat("Expected id column entity",
                response.jsonPath().getString("data[0].columns.id.entity"), equalTo("companies"));
        assertThat("Expected id column field",
                response.jsonPath().getString("data[0].columns.id.field"), equalTo("id"));
        assertThat("Expected id column type",
                response.jsonPath().getString("data[0].columns.id.type"), equalTo("number"));

        assertThat("Expected srno column label",
                response.jsonPath().getString("data[0].columns.srno.label"), equalTo("ID"));
        assertThat("Expected srno column longlabel",
                response.jsonPath().getString("data[0].columns.srno.longlabel"), equalTo("Company ID"));
        assertThat("Expected srno column entity",
                response.jsonPath().getString("data[0].columns.srno.entity"), equalTo("companies"));
        assertThat("Expected srno column field",
                response.jsonPath().getString("data[0].columns.srno.field"), equalTo("srno"));
        assertThat("Expected srno column type",
                response.jsonPath().getString("data[0].columns.srno.type"), equalTo("number"));

        assertThat("Expected companyname column label",
                response.jsonPath().getString("data[0].columns.companyname.label"), equalTo("Company Name"));
        assertThat("Expected companyname column entity",
                response.jsonPath().getString("data[0].columns.companyname.entity"), equalTo("companies"));
        assertThat("Expected companyname column field",
                response.jsonPath().getString("data[0].columns.companyname.field"), equalTo("companyname"));
        assertThat("Expected companyname column type",
                response.jsonPath().getString("data[0].columns.companyname.type"), equalTo("text"));

        // Schema validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/company/getCompanyEntityColumns.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyEntityColumnsVerifyCustomFields_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

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
            String fieldPath = "data[0].columns." + customColumn + ".field";

            assertThat("Custom column " + customColumn + " should have label",
                    response.jsonPath().get(labelPath), notNullValue());
            assertThat("Custom column " + customColumn + " should have entity",
                    response.jsonPath().getString(entityPath), equalTo("companies"));
            assertThat("Custom column " + customColumn + " should have field",
                    response.jsonPath().getString(fieldPath), equalTo(customColumn));
            assertThat("Custom column " + customColumn + " should have type",
                    response.jsonPath().getString("data[0].columns." + customColumn + ".type"), notNullValue());
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyEntityColumnsVerifyColumnProperties_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

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

        // Company name field properties
        assertThat("companyname should allow on import",
                jsonPath.getBoolean("data[0].columns.companyname.allow_on_import"), is(true));
        assertThat("companyname should allow on export",
                jsonPath.getBoolean("data[0].columns.companyname.allow_on_export"), is(true));
        assertThat("companyname should be required on form",
                jsonPath.getBoolean("data[0].columns.companyname.required_on_form"), is(true));

        // Verify industry field properties (dropdown type)
        if (columnsMap.containsKey("industry")) {
            assertThat("industry should have dropdown_value",
                    jsonPath.getString("data[0].columns.industry.dropdown_value"), notNullValue());
            assertThat("industry should have dropdown_field",
                    jsonPath.getString("data[0].columns.industry.dropdown_field"), notNullValue());
            assertThat("industry type should be dropdown",
                    jsonPath.getString("data[0].columns.industry.type"), equalTo("dropdown"));
        }

        // Verify date fields
        if (columnsMap.containsKey("createdon")) {
            assertThat("createdon type should be date",
                    jsonPath.getString("data[0].columns.createdon.type"), equalTo("date"));
        }
        if (columnsMap.containsKey("updatedon")) {
            assertThat("updatedon type should be date",
                    jsonPath.getString("data[0].columns.updatedon.type"), equalTo("date"));
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyEntityColumnsUnauthorizedTest_401() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, "InvalidToken", queryParameters, null, true);
        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        assertThat("Expected unauthorized message", response.jsonPath().getString("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyEntityColumnsTest_404() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");

        Response response = RestClient.doGet("JSON", companyServiceURL, "ntity-columns", albatrossAuthToken, queryParameters, null, true); // Incorrect URL
        assertThat("Expected status code 404", response.getStatusCode(), equalTo(404));
        assertThat("Expected not found error", response.jsonPath().getString("error"), equalTo("Not Found"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyEntityColumnsTest_400() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", ""); // Missing entity

        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);
        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
        assertThat("Expected error context", response.jsonPath().getString("meta.responseType.context"), equalTo("Error while processing request"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyEntityColumnsTest_405() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");

        Response response = RestClient.doPost("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, true, null);
        assertThat("Expected status code 405", response.getStatusCode(), equalTo(405));
        assertThat("Expected method not allowed error", response.jsonPath().getString("errors[0].message"), containsString("not supported"));
        assertThat("Expected meta status 405", response.jsonPath().getInt("meta.status"), equalTo(405));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyEntityColumnsVerifyAllDefaultFields_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        Response response = RestClient.doGet("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");

        // Comprehensive list of all default fields based on the provided JSON response
        List<String> allDefaultFields = Arrays.asList(
                "id", "parentcompanyid", "parentcompanyslug", "haschildren", "hasparent",
                "srno", "companyname", "aboutcompany", "parentcompanyname", "address", "city",
                "industryid", "industry", "website", "totalopenjob", "totalclosedjob",
                "totalonholdjob", "totalcanceledjob", "logo", "ownerid", "profilefacebook",
                "profiletwitter", "profilelinkedin", "ownername", "creatorname", "accountid",
                "deleted", "createdby", "createdon", "updatedon", "updatedby"
        );

        // Create a map to track missing fields
        Map<String, Boolean> fieldPresenceMap = new HashMap<>();
        for (String field : allDefaultFields) {
            boolean present = columnsMap.containsKey(field);
            fieldPresenceMap.put(field, present);
            assertThat("Missing expected default field: " + field, present, is(true));
        }

        // Verify all fields are present
        long missingFields = fieldPresenceMap.values().stream().filter(present -> !present).count();
        assertThat("All default fields should be present. Missing: " + missingFields, missingFields, equalTo(0L));
    }

    private String createCompanyCustomField(String fieldType) {
        String fieldName = faker.getCustomFieldName("companies");
        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(faker.getColumnId());
        extraField.setEntitytypeid(COMPANY_ENTITY_TYPE_ID);
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

    private String createCompanyCustomFieldWithOptions(String fieldType) {
        String fieldName = faker.getCustomFieldName("companies");
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
        extraField.setEntitytypeid(COMPANY_ENTITY_TYPE_ID);
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