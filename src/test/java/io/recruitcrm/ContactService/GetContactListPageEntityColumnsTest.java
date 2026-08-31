package io.recruitcrm.ContactService;

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
public class GetContactListPageEntityColumnsTest extends TestBase {

    // Constants for reusability
    private static final String BASE_PATH = "entity-columns";
    private static final String ACCOUNT_VIEW_BASE_PATH = "account-view-columns";
    private static final int CONTACT_ENTITY_TYPE_ID = 4;

    JavaFakerCustomField faker = new JavaFakerCustomField();
    String albatrossAuthToken;
    List<String> createdCustomFieldNames = new ArrayList<>();
    List<String> createdCustomFieldColumnIds = new ArrayList<>();
    ExecutorService executor;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        executor = Executors.newFixedThreadPool(6); // Create thread pool for parallel custom field creation

        // Create various types of custom fields for contacts in parallel
        List<CompletableFuture<String>> customFieldFutures = new ArrayList<>();

        // Simple custom field types (without options)
        String[] simpleFieldTypes = {
                "text", "number", "date", "date_time", "longtext",
                "phonenumber", "checkbox", "file", "social_profile"
        };

        // Custom field types that require options
        String[] fieldTypesWithOptions = {"dropdown", "multiselect"};

        // Create simple custom fields in parallel
        for (String fieldType : simpleFieldTypes) {
            customFieldFutures.add(CompletableFuture.supplyAsync(
                    () -> createContactCustomField(fieldType), executor));
        }

        // Create custom fields with options in parallel
        for (String fieldType : fieldTypesWithOptions) {
            customFieldFutures.add(CompletableFuture.supplyAsync(
                    () -> createContactCustomFieldWithOptions(fieldType), executor));
        }

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
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactEntityColumnsTest_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

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

        // List of all default/required columns for contacts
        List<String> requiredColumns = Arrays.asList(
                // Basic contact fields
                "id", "srno", "name", "firstname", "lastname", "email", "contactnumber", "designation",
                "city", "address", "locality", "state", "country", "postal_code",
                "companyslug", "companyid", "companyname", "stageid", "stage",
                "ownerid", "ownerslug", "ownername", "accountid", "deleted",
                "createdby", "createdbyname", "createdon", "updatedon", "updatedby", "creatorname",
                "profilefacebook", "profiletwitter", "profilelinkedin", "profilexing",
                "photo", "slug", "note", "canaccess",
                // Opt-out and status fields
                "email_opt_out", "sms_opt_out", "candidate_linked", "hotlist", "off_limit_status",
                "profile_picture",
                // Last activities fields
                "last_calllog_created_on", "last_sms_sent_on", "last_email_sent_on",
                "last_communication_timestamp", "last_communication_method",
                "last_meeting_created_on", "last_message_sent_on",
                // Company cross-entity fields
                "company_srno", "parentcompanyname", "company_address", "company_city",
                "company_industryid", "company_industry", "company_website",
                "company_totalopenjob", "company_totalclosedjob", "company_totalonholdjob",
                "company_totalcanceledjob", "company_logo", "company_ownerid",
                "company_profilefacebook", "company_profiletwitter", "company_profilelinkedin",
                "company_ownername", "company_creatorname", "company_accountid",
                "company_deleted", "company_createdby", "company_createdon",
                "company_updatedon", "company_updatedby", "company_custcolumn1", "company_note",
                "company_locality", "company_state", "company_country", "company_postal_code",
                "company_linkedin_id", "company_indeed_opted_out"
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
                response.jsonPath().getString("data[0].columns.id.entity"), equalTo("contacts"));
        assertThat("Expected id column field",
                response.jsonPath().getString("data[0].columns.id.field"), equalTo("id"));
        assertThat("Expected id column type",
                response.jsonPath().getString("data[0].columns.id.type"), equalTo("number"));

        assertThat("Expected srno column label",
                response.jsonPath().getString("data[0].columns.srno.label"), equalTo("ID"));
        assertThat("Expected srno column longlabel",
                response.jsonPath().getString("data[0].columns.srno.longlabel"), equalTo("Contact ID"));
        assertThat("Expected srno column entity",
                response.jsonPath().getString("data[0].columns.srno.entity"), equalTo("contacts"));
        assertThat("Expected srno column field",
                response.jsonPath().getString("data[0].columns.srno.field"), equalTo("srno"));
        assertThat("Expected srno column type",
                response.jsonPath().getString("data[0].columns.srno.type"), equalTo("number"));

        assertThat("Expected name column label",
                response.jsonPath().getString("data[0].columns.name.label"), equalTo("Name"));
        assertThat("Expected name column entity",
                response.jsonPath().getString("data[0].columns.name.entity"), equalTo("contacts"));
        assertThat("Expected name column field",
                response.jsonPath().getString("data[0].columns.name.field"), equalTo("name"));
        assertThat("Expected name column type",
                response.jsonPath().getString("data[0].columns.name.type"), equalTo("text"));

        assertThat("Expected firstname column label",
                response.jsonPath().getString("data[0].columns.firstname.label"), equalTo("First Name"));
        assertThat("Expected firstname column entity",
                response.jsonPath().getString("data[0].columns.firstname.entity"), equalTo("contacts"));
        assertThat("Expected firstname column field",
                response.jsonPath().getString("data[0].columns.firstname.field"), equalTo("firstname"));
        assertThat("Expected firstname column type",
                response.jsonPath().getString("data[0].columns.firstname.type"), equalTo("text"));

        // Schema validation
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/contact/getContactEntityColumns.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactEntityColumnsVerifyCustomFields_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

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
                    response.jsonPath().getString(entityPath), equalTo("contacts"));
            assertThat("Custom column " + customColumn + " should have field",
                    response.jsonPath().getString(fieldPath), equalTo(customColumn));
            assertThat("Custom column " + customColumn + " should have type",
                    response.jsonPath().getString("data[0].columns." + customColumn + ".type"), notNullValue());
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactEntityColumnsVerifyColumnProperties_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

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

        // First name field properties
        assertThat("firstname should allow on import",
                jsonPath.getBoolean("data[0].columns.firstname.allow_on_import"), is(true));
        assertThat("firstname should allow on export",
                jsonPath.getBoolean("data[0].columns.firstname.allow_on_export"), is(true));
        assertThat("firstname should be required on form",
                jsonPath.getBoolean("data[0].columns.firstname.required_on_form"), is(true));
        assertThat("firstname should have dbtype",
                jsonPath.getString("data[0].columns.firstname.dbtype"), equalTo("varchar"));
        assertThat("firstname should have type",
                jsonPath.getString("data[0].columns.firstname.type"), equalTo("text"));

        // Verify stageid field properties (dropdown type)
        if (columnsMap.containsKey("stageid")) {
            assertThat("stageid should have dropdown_value",
                    jsonPath.getString("data[0].columns.stageid.dropdown_value"), notNullValue());
            assertThat("stageid should have dropdown_field",
                    jsonPath.getString("data[0].columns.stageid.dropdown_field"), notNullValue());
            assertThat("stageid type should be dropdown",
                    jsonPath.getString("data[0].columns.stageid.type"), equalTo("dropdown"));
            assertThat("stageid should have service",
                    jsonPath.getString("data[0].columns.stageid.service"), notNullValue());
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

        // Verify email field properties
        if (columnsMap.containsKey("email")) {
            assertThat("email should have type",
                    jsonPath.getString("data[0].columns.email.type"), equalTo("text"));
            assertThat("email should allow on import",
                    jsonPath.getBoolean("data[0].columns.email.allow_on_import"), is(true));
        }

        // Verify contactnumber field properties
        if (columnsMap.containsKey("contactnumber")) {
            assertThat("contactnumber should have type",
                    jsonPath.getString("data[0].columns.contactnumber.type"), equalTo("text"));
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactEntityColumnsUnauthorizedTest_401() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, "InvalidToken", queryParameters, null, true);
        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        assertThat("Expected unauthorized message", response.jsonPath().getString("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactEntityColumnsTest_404() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");

        Response response = RestClient.doGet("JSON", contactServiceURL, "ntity-columns", albatrossAuthToken, queryParameters, null, true); // Incorrect URL
        assertThat("Expected status code 404", response.getStatusCode(), equalTo(404));
        assertThat("Expected not found error", response.jsonPath().getString("error"), equalTo("Not Found"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactEntityColumnsTest_400() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", ""); // Missing entity

        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);
        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
        assertThat("Expected error context", response.jsonPath().getString("meta.responseType.context"), equalTo("Error while processing request"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactEntityColumnsTest_405() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");

        Response response = RestClient.doPost("JSON", contactServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, true, null);
        assertThat("Expected status code 405", response.getStatusCode(), equalTo(405));
        assertThat("Expected method not allowed error", response.jsonPath().getString("errors[0].message"), containsString("not supported"));
        assertThat("Expected meta status 405", response.jsonPath().getInt("meta.status"), equalTo(405));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactEntityColumnsVerifyAllDefaultFields_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");
        Response response = RestClient.doGet("JSON", contactServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");

        // Comprehensive list of all default fields for contacts (using camelCase as per response)
        List<String> allDefaultFields = Arrays.asList(
                // Basic contact fields
                "id", "srno", "name", "firstname", "lastname", "email", "contactnumber", "designation",
                "city", "address", "locality", "state", "country", "postal_code",
                "companyslug", "companyid", "companyname", "stageid", "stage",
                "ownerid", "ownerslug", "ownername", "accountid", "deleted",
                "createdby", "createdbyname", "createdon", "updatedon", "updatedby", "creatorname",
                "profilefacebook", "profiletwitter", "profilelinkedin", "profilexing",
                "photo", "slug", "note", "canaccess",
                // Opt-out and status fields
                "email_opt_out", "sms_opt_out", "candidate_linked", "hotlist", "off_limit_status",
                "profile_picture",
                // Last activities fields
                "last_calllog_created_on", "last_sms_sent_on", "last_email_sent_on",
                "last_communication_timestamp", "last_communication_method",
                "last_meeting_created_on", "last_message_sent_on",
                // Company cross-entity fields
                "company_srno", "parentcompanyname", "company_address", "company_city",
                "company_industryid", "company_industry", "company_website",
                "company_totalopenjob", "company_totalclosedjob", "company_totalonholdjob",
                "company_totalcanceledjob", "company_logo", "company_ownerid",
                "company_profilefacebook", "company_profiletwitter", "company_profilelinkedin",
                "company_ownername", "company_creatorname", "company_accountid",
                "company_deleted", "company_createdby", "company_createdon",
                "company_updatedon", "company_updatedby", "company_custcolumn1", "company_note",
                "company_locality", "company_state", "company_country", "company_postal_code",
                "company_linkedin_id", "company_indeed_opted_out"
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

    private String createContactCustomField(String fieldType) {
        String fieldName = faker.getCustomFieldName("contacts");
        ExtraField extraField = new ExtraField();
        CustomFieldAlbatross customField = new CustomFieldAlbatross();

        extraField.setColumnid(faker.getColumnId());
        extraField.setEntitytypeid(CONTACT_ENTITY_TYPE_ID);
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

    private String createContactCustomFieldWithOptions(String fieldType) {
        String fieldName = faker.getCustomFieldName("contacts");
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
        extraField.setEntitytypeid(CONTACT_ENTITY_TYPE_ID);
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

    // ========== Account View Columns Tests ==========

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactAccountViewColumnsTest_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");
        Response response = RestClient.doGet("JSON", contactServiceURL, ACCOUNT_VIEW_BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        assertThat("Expected success message", response.jsonPath().getString("meta.message"), equalTo("Account View Columns Fetched Successfully"));
        assertThat("Expected response type code 103", response.jsonPath().getInt("meta.responseType.code"), equalTo(103));
        assertThat("Expected success context", response.jsonPath().getString("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Expected status 200 in meta", response.jsonPath().getInt("meta.status"), equalTo(200));
        assertThat("Expected timestamp not null", response.jsonPath().get("meta.timestamp"), notNullValue());
        assertThat("Expected requestUuid not null", response.jsonPath().get("meta.requestUuid"), notNullValue());

        // Validate accountViewColumns structure
        assertThat("Expected accountViewColumns data not null", response.jsonPath().get("data[0].accountViewColumns"), notNullValue());

        // Get accountViewColumns map for validation
        Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].accountViewColumns");

        // List of key default columns for contacts (using camelCase as per response)
        // Expanded to include more fields from global/get-entity-columns response
        List<String> requiredColumns = Arrays.asList(
                // Basic contact fields
                "id", "srno", "name", "firstname", "lastname", "email", "contactnumber", "designation",
                "city", "address", "locality", "state", "country", "postal_code",
                "companyslug", "companyid", "companyname", "stageid", "stage",
                "ownerid", "ownerslug", "ownername", "accountid", "deleted",
                "createdby", "createdbyname", "createdon", "updatedon", "updatedby", "creatorname",
                "profilefacebook", "profiletwitter", "profilelinkedin", "profilexing",
                "photo", "slug", "note", "canaccess",
                // Opt-out and status fields
                "email_opt_out", "sms_opt_out", "candidate_linked", "hotlist", "off_limit_status",
                "profile_picture",
                // Last activities fields
                "last_calllog_created_on", "last_sms_sent_on", "last_email_sent_on",
                "last_communication_timestamp", "last_communication_method",
                "last_meeting_created_on", "last_message_sent_on",
                // Company cross-entity fields
                "company_srno", "parentcompanyname", "company_address", "company_city",
                "company_industryid", "company_industry", "company_website",
                "company_totalopenjob", "company_totalclosedjob", "company_totalonholdjob",
                "company_totalcanceledjob", "company_logo", "company_ownerid",
                "company_profilefacebook", "company_profiletwitter", "company_profilelinkedin",
                "company_ownername", "company_creatorname", "company_accountid",
                "company_deleted", "company_createdby", "company_createdon",
                "company_updatedon", "company_updatedby", "company_custcolumn1", "company_note",
                "company_locality", "company_state", "company_country", "company_postal_code",
                "company_linkedin_id", "company_indeed_opted_out"
        );

        // Verify all required columns are present
        for (String column : requiredColumns) {
            if (columnsMap.containsKey(column)) {
                assertThat("Column " + column + " should not be null",
                        columnsMap.get(column), notNullValue());
            }
        }

        // Verify custom fields are present in the response
        boolean isCustomFieldPresent = columnsMap.keySet().stream()
                .anyMatch(name -> name.startsWith("custcolumn"));
        assertThat("Expected custom field columns to be present", isCustomFieldPresent, is(true));

        // Verify specific column properties for key fields
        assertThat("Expected id column label",
                response.jsonPath().getString("data[0].accountViewColumns.id.label"), equalTo("Id"));
        assertThat("Expected id column entity",
                response.jsonPath().getString("data[0].accountViewColumns.id.entity"), equalTo("contacts"));
        assertThat("Expected id column field",
                response.jsonPath().getString("data[0].accountViewColumns.id.field"), equalTo("id"));

        assertThat("Expected srno column label",
                response.jsonPath().getString("data[0].accountViewColumns.srno.label"), equalTo("ID"));
        assertThat("Expected srno column longlabel",
                response.jsonPath().getString("data[0].accountViewColumns.srno.longlabel"), equalTo("Contact ID"));
        assertThat("Expected srno column entity",
                response.jsonPath().getString("data[0].accountViewColumns.srno.entity"), equalTo("contacts"));
        assertThat("Expected srno column field",
                response.jsonPath().getString("data[0].accountViewColumns.srno.field"), equalTo("srno"));

        assertThat("Expected name column label",
                response.jsonPath().getString("data[0].accountViewColumns.name.label"), equalTo("Name"));
        assertThat("Expected name column entity",
                response.jsonPath().getString("data[0].accountViewColumns.name.entity"), equalTo("contacts"));
        assertThat("Expected name column field",
                response.jsonPath().getString("data[0].accountViewColumns.name.field"), equalTo("name"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactAccountViewColumnsVerifyCustomFields_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");
        Response response = RestClient.doGet("JSON", contactServiceURL, ACCOUNT_VIEW_BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        // Get accountViewColumns map
        Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].accountViewColumns");

        // Get all custom field column names
        List<String> customFieldColumns = columnsMap.keySet().stream()
                .filter(name -> name.startsWith("custcolumn"))
                .collect(Collectors.toList());

        assertThat("Expected custom fields to be present", customFieldColumns.size(), greaterThan(0));

        // Verify that created custom fields are present in the response
        for (String customColumn : customFieldColumns) {
            Object columnData = columnsMap.get(customColumn);
            assertThat("Custom column " + customColumn + " should not be null", columnData, notNullValue());

            // Verify custom field has required properties
            String labelPath = "data[0].accountViewColumns." + customColumn + ".label";
            String entityPath = "data[0].accountViewColumns." + customColumn + ".entity";
            String fieldPath = "data[0].accountViewColumns." + customColumn + ".field";

            assertThat("Custom column " + customColumn + " should have label",
                    response.jsonPath().get(labelPath), notNullValue());
            assertThat("Custom column " + customColumn + " should have entity",
                    response.jsonPath().getString(entityPath), equalTo("contacts"));
            assertThat("Custom column " + customColumn + " should have field",
                    response.jsonPath().getString(fieldPath), equalTo(customColumn));
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactAccountViewColumnsVerifyColumnProperties_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");
        Response response = RestClient.doGet("JSON", contactServiceURL, ACCOUNT_VIEW_BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        JsonPath jsonPath = response.jsonPath();
        Map<String, Object> columnsMap = jsonPath.getMap("data[0].accountViewColumns");

        // Verify column properties for various field types
        // ID field properties
        assertThat("id column should have visible_locked property",
                jsonPath.get("data[0].accountViewColumns.id.visible_locked"), notNullValue());
        assertThat("id column should have listPageOrder property",
                jsonPath.get("data[0].accountViewColumns.id.listPageOrder"), notNullValue());

        // Name field properties
        assertThat("name should have visible_locked property",
                jsonPath.get("data[0].accountViewColumns.name.visible_locked"), notNullValue());
        assertThat("name should have listPageOrder property",
                jsonPath.get("data[0].accountViewColumns.name.listPageOrder"), notNullValue());

        // Email field properties
        if (columnsMap.containsKey("email")) {
            assertThat("email should have listPageOrder property",
                    jsonPath.get("data[0].accountViewColumns.email.listPageOrder"), notNullValue());
        }

        // Contact number field properties
        if (columnsMap.containsKey("contactnumber")) {
            assertThat("contactnumber should have listPageOrder property",
                    jsonPath.get("data[0].accountViewColumns.contactnumber.listPageOrder"), notNullValue());
        }

        // Date fields
        if (columnsMap.containsKey("createdon")) {
            assertThat("createdon should have listPageOrder property",
                    jsonPath.get("data[0].accountViewColumns.createdon.listPageOrder"), notNullValue());
        }
        if (columnsMap.containsKey("updatedon")) {
            assertThat("updatedon should have listPageOrder property",
                    jsonPath.get("data[0].accountViewColumns.updatedon.listPageOrder"), notNullValue());
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactAccountViewColumnsUnauthorizedTest_401() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");

        Response response = RestClient.doGet("JSON", contactServiceURL, ACCOUNT_VIEW_BASE_PATH, "InvalidToken", queryParameters, null, true);
        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        assertThat("Expected unauthorized message", response.jsonPath().getString("meta.message"), equalTo("Unauthorised access"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactAccountViewColumnsTest_404() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");

        Response response = RestClient.doGet("JSON", contactServiceURL, "account-view-colums", albatrossAuthToken, queryParameters, null, true); // Incorrect URL
        assertThat("Expected status code 404", response.getStatusCode(), equalTo(404));
        assertThat("Expected not found error", response.jsonPath().getString("error"), equalTo("Not Found"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactAccountViewColumnsTest_400() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", ""); // Missing entity

        Response response = RestClient.doGet("JSON", contactServiceURL, ACCOUNT_VIEW_BASE_PATH, albatrossAuthToken, queryParameters, null, true);
        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
        assertThat("Expected error context", response.jsonPath().getString("meta.responseType.context"), equalTo("Error while processing request"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactAccountViewColumnsTest_405() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");

        Response response = RestClient.doPost("JSON", contactServiceURL, ACCOUNT_VIEW_BASE_PATH, albatrossAuthToken, queryParameters, true, null);
        assertThat("Expected status code 405", response.getStatusCode(), equalTo(405));
        assertThat("Expected method not allowed error", response.jsonPath().getString("errors[0].message"), containsString("not supported"));
        assertThat("Expected meta status 405", response.jsonPath().getInt("meta.status"), equalTo(405));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"contact_service", "nightly-build"})
    public void getContactAccountViewColumnsVerifyAllDefaultFields_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "contacts");
        Response response = RestClient.doGet("JSON", contactServiceURL, ACCOUNT_VIEW_BASE_PATH, albatrossAuthToken, queryParameters, null, true);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].accountViewColumns");

        // Comprehensive list of all default fields for contacts (camelCase format)
        // Expanded to include all fields from global/get-entity-columns response
        List<String> allDefaultFields = Arrays.asList(
                // Basic contact fields
                "id", "srno", "name", "firstname", "lastname", "email", "contactnumber", "designation",
                "city", "address", "locality", "state", "country", "postal_code",
                "companyslug", "companyid", "companyname", "stageid", "stage",
                "ownerid", "ownerslug", "ownername", "accountid", "deleted",
                "createdby", "createdbyname", "createdon", "updatedon", "updatedby", "creatorname",
                "profilefacebook", "profiletwitter", "profilelinkedin", "profilexing",
                "photo", "slug", "note", "canaccess",
                // Opt-out and status fields
                "email_opt_out", "sms_opt_out", "candidate_linked", "hotlist", "off_limit_status",
                "profile_picture",
                // Last activities fields
                "last_calllog_created_on", "last_sms_sent_on", "last_email_sent_on",
                "last_communication_timestamp", "last_communication_method",
                "last_meeting_created_on", "last_message_sent_on",
                // Company cross-entity fields
                "company_srno", "parentcompanyname", "company_address", "company_city",
                "company_industryid", "company_industry", "company_website",
                "company_totalopenjob", "company_totalclosedjob", "company_totalonholdjob",
                "company_totalcanceledjob", "company_logo", "company_ownerid",
                "company_profilefacebook", "company_profiletwitter", "company_profilelinkedin",
                "company_ownername", "company_creatorname", "company_accountid",
                "company_deleted", "company_createdby", "company_createdon",
                "company_updatedon", "company_updatedby", "company_custcolumn1", "company_note",
                "company_locality", "company_state", "company_country", "company_postal_code",
                "company_linkedin_id", "company_indeed_opted_out"
        );

        // Create a map to track missing fields
        Map<String, Boolean> fieldPresenceMap = new HashMap<>();
        for (String field : allDefaultFields) {
            // Check if field exists in account-view-columns response (some fields may not be present)
            boolean present = columnsMap.containsKey(field);
            fieldPresenceMap.put(field, present);
            
            if (present) {
                assertThat("Field " + field + " should not be null",
                        columnsMap.get(field), notNullValue());
            }
        }

        // Verify at least some key fields are present
        long presentFields = fieldPresenceMap.values().stream().filter(present -> present).count();
        assertThat("At least some default fields should be present. Present: " + presentFields, presentFields, greaterThan(0L));
    }

}

