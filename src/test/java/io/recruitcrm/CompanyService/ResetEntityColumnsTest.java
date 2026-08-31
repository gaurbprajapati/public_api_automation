package io.recruitcrm.CompanyService;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.global.UpdateFieldWidgetCustomizationRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ResetEntityColumnsTest extends TestBase {
    
    private static final String BASE_PATH = "entity-columns/reset";
    
    
    private String albatrossAuthToken;
    private String adminAuthToken;
    private String restrictedAuthToken;
    private int ownerAccountID;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        restrictedAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        adminAuthToken = ThreadManager.getAlbatrossToken("Admin");
        ownerAccountID = ThreadManager.getAccount().getAccountId();
    }

    @Owner("Harika")
    @Test(dataProvider = "resetEntityColumnsDataProvider", groups = {"company_service", "nightly-build"})
    public void resetEntityColumns_Success(String viewType, String isDetailPageReset) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        queryParameters.put("viewType", viewType);
        queryParameters.put("isDetailPageReset", isDetailPageReset);

        Response response = RestClient.doPatch("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true, null);

        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
        
        JsonPath jsonPath = response.jsonPath();
        
        boolean isAccountView = "othersView".equals(viewType);
        String expectedMessage = isAccountView ? "Account View Columns Fetched Successfully" : "Entity Column Fetched Successfully";
        
        if (isAccountView) {
            response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumnsAccountView.json"));
            assertThat("Account view columns should exist in data", jsonPath.get("data[0].accountViewColumns"), notNullValue());
        } else {
            response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumns.json"));
            assertThat("Columns should exist in data", jsonPath.get("data[0].columns"), notNullValue());
        }
        
        assertThat("Expected success message", jsonPath.get("meta.message"), equalTo(expectedMessage));
        assertThat("Expected status 200 in meta", jsonPath.getInt("meta.status"), equalTo(200));
        assertThat("Expected success context", jsonPath.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Expected response code 103", jsonPath.getInt("meta.responseType.code"), equalTo(103));
    }

    @Owner("Harika")
    @Test(groups = {"company_service", "nightly-build"})
    public void resetEntityColumns_WithoutAuth() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        queryParameters.put("viewType", "myView");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", companyServiceURL, BASE_PATH, "", queryParameters, null, true, null);

        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumnsUnauthorized.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected unauthorized access message", jsonPath.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Expected status 401 in meta", jsonPath.getInt("meta.status"), equalTo(401));
        assertThat("Expected warning context", jsonPath.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("Expected response code 104", jsonPath.getInt("meta.responseType.code"), equalTo(104));
        assertThat("Expected data to contain missing bearer token message", jsonPath.get("data"), equalTo("Missing bearer token in header"));
        assertThat("Errors array should exist", jsonPath.get("errors"), notNullValue());
    }

    @Owner("Harika")
    @Test(groups = {"company_service", "nightly-build"})
    public void resetEntityColumns_InvalidAuth() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        queryParameters.put("viewType", "myView");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken + "invalid", queryParameters, null, true, null);

        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumnsUnauthorized.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected unauthorized access message", jsonPath.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Expected status 401 in meta", jsonPath.getInt("meta.status"), equalTo(401));
        assertThat("Expected warning context", jsonPath.get("meta.responseType.context"), equalTo("Warning"));
        assertThat("Expected response code 104", jsonPath.getInt("meta.responseType.code"), equalTo(104));
        assertThat("Expected data to contain invalid token message", jsonPath.get("data"), equalTo("Invalid or expired token"));
        assertThat("Errors array should exist", jsonPath.get("errors"), notNullValue());
    }

    @Owner("Harika")
    @Test(groups = {"company_service", "nightly-build"})
    public void resetEntityColumns_MissingEntity() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("viewType", "myView");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true, null);

        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumnsValidationError.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected status 400 in meta", jsonPath.getInt("meta.status"), equalTo(400));
        assertThat("Expected error context", jsonPath.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("Expected response code 101", jsonPath.getInt("meta.responseType.code"), equalTo(101));
        assertThat("Error message should contain entity validation", jsonPath.get("errors[0].message"), equalTo("Entity cannot be null or blank"));
        assertThat("Error context should be Validation Error", jsonPath.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("Error code should be 201", jsonPath.getInt("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Rahul Shibu")
    @Test
    public void resetEntityColumns_MissingViewType() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true, null);

        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumnsValidationError.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected status 400 in meta", jsonPath.getInt("meta.status"), equalTo(400));
        assertThat("Expected error context", jsonPath.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("Expected response code 101", jsonPath.getInt("meta.responseType.code"), equalTo(101));
        assertThat("Error message should contain view type validation", jsonPath.get("errors[0].message"), equalTo("View type cannot be null or blank"));
        assertThat("Error context should be Validation Error", jsonPath.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("Error code should be 201", jsonPath.getInt("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Harika")
    @Test(groups = {"company_service", "nightly-build"})
    public void resetEntityColumns_InvalidEntity() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "invalid_entity");
        queryParameters.put("viewType", "myView");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true, null);

        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumnsValidationError.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected status 400 in meta", jsonPath.getInt("meta.status"), equalTo(400));
        assertThat("Expected error context", jsonPath.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("Expected response code 101", jsonPath.getInt("meta.responseType.code"), equalTo(101));
        assertThat("Error message should contain entity validation", jsonPath.get("errors[0].message"), equalTo("Entity must be one of the predefined enum values"));
        assertThat("Error context should be Validation Error", jsonPath.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("Error code should be 201", jsonPath.getInt("errors[0].errorType.code"), equalTo(201));
        
    }

    @Owner("Rahul Shibu")
    @Test
    public void resetEntityColumns_InvalidViewType() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        queryParameters.put("viewType", "invalidView");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", companyServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true, null);

        assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumnsValidationError.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected status 400 in meta", jsonPath.getInt("meta.status"), equalTo(400));
        assertThat("Expected error context", jsonPath.get("meta.responseType.context"), equalTo("Error while processing request"));
        assertThat("Expected response code 101", jsonPath.getInt("meta.responseType.code"), equalTo(101));
        assertThat("Error message should contain entity validation", jsonPath.get("errors[0].message"), equalTo("Invalid view type"));
        assertThat("Error context should be Validation Error", jsonPath.get("errors[0].errorType.context"), equalTo("Validation Error"));
        assertThat("Error code should be 201", jsonPath.getInt("errors[0].errorType.code"), equalTo(201));
    }

    @Owner("Harika")
    @Test(groups = {"company_service", "nightly-build"})
    public void verifyOtherUserCannotResetEntityColumnsForOthersView() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        queryParameters.put("viewType", "othersView");
        queryParameters.put("isDetailPageReset", "true");

        Response response = RestClient.doPatch("JSON", companyServiceURL, BASE_PATH, adminAuthToken, queryParameters, null, true, null);
        
        assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumnsUnauthorized.json"));
        
        JsonPath jsonPath = response.jsonPath();
        assertThat("Expected unauthorized access message", jsonPath.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Expected status 401 in meta", jsonPath.getInt("meta.status"), equalTo(401));
        assertThat("Expected warning context", jsonPath.get("meta.responseType.context"), equalTo("Warning"));
    }

    @Owner("Harika")
    @Test(groups = {"company_service", "nightly-build"})
    public void verifyRestrictionOnResetEntityColumns() {
        restrictEditOnInformationOverviewCustomization();
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("entity", "companies");
        queryParameters.put("viewType", "myView");
        queryParameters.put("isDetailPageReset", "true");

        Response response1 = RestClient.doPatch("JSON", companyServiceURL, BASE_PATH, adminAuthToken, queryParameters, null, true, null);
        
        assertThat("Expected status code 400", response1.getStatusCode(), equalTo(200));
        response1.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumns.json"));

        Response response2 = RestClient.doPatch("JSON", companyServiceURL, BASE_PATH, restrictedAuthToken, queryParameters, null, true, null);
        assertThat("Expected status code 401", response2.getStatusCode(), equalTo(401));
        response2.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/companyService/ResetEntityColumnsUnauthorized.json"));
    }

    public void restrictEditOnInformationOverviewCustomization() {
        UpdateFieldWidgetCustomizationRequest updateRequest = new UpdateFieldWidgetCustomizationRequest();
        updateRequest.setId(ownerAccountID);
        updateRequest.setSilentProcess(true);
        updateRequest.setKey("entity_view_lock_settings");
        updateRequest.setTableFlag("accountsettings");
        updateRequest.setValue("{\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"8\":0,\"9\":0,\"10\":0,\"16\":0,\"17\":1,\"18\":0,\"19\":0}");

        Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", albatrossAuthToken, null, true, updateRequest);

        assert response != null;
        assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat("Message type should be is-success", jp.get("message_type"), equalTo("is-success"));
    }

    @DataProvider(name = "resetEntityColumnsDataProvider", parallel = true)
    public Object[][] resetEntityColumnsDataProvider() {
        return new Object[][] {
                { "myView", "true" },
                { "myView", "false" },
                { "othersView", "true" },
                { "othersView", "false" }
        };
    }
}

