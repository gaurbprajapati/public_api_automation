package io.recruitcrm.ContactService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountContactDetailPageCandidatesPitchedTest extends TestBase {

    private int contactIdAccountA;
    private AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    private commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        createTestDataAccountA();
    }

    private void createTestDataAccountA() {
        try {
            Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey);
            assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
            JsonPath companyJp = companyResponse.jsonPath();
            String companySlug = companyJp.get("slug");
            assertThat("Company slug should not be null", companySlug, notNullValue());

            Response contactResponse = function.createNewContact_POST(baseURL, accountA_apiKey, companySlug);
            assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
            JsonPath contactJp = contactResponse.jsonPath();
            String contactSlug = contactJp.get("slug");
            assertThat("Contact slug should not be null", contactSlug, notNullValue());

            Response contactDetailsResponse = albatrossFunctions.getContactResponse(albatrossURL, accountA_Token, contactSlug);
            assertThat("Failed to get contact details from albatross API", contactDetailsResponse.getStatusCode(), equalTo(200));
            JsonPath contactDetailsJp = contactDetailsResponse.jsonPath();
            contactIdAccountA = contactDetailsJp.getInt("data.contact.id");
            assertThat("Contact ID should not be null", contactIdAccountA, greaterThan(0));

            Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, accountA_Token);
            assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
            JsonPath candidateJp = candidateResponse.jsonPath();
            String candidateSlug = candidateJp.get("data.candidate.slug");

            String pitchPath = "pitch/{candidate}/contact/{contact}";
            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("candidate", candidateSlug);
            pathParameters.put("contact", contactSlug);

            Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, accountA_apiKey, null, pathParameters, true, null);
            assertThat("Failed to pitch candidate to contact", pitchResponse.getStatusCode(), equalTo(200));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test data: " + e.getMessage(), e);
        }
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "crossAccountContactDetailPageCandidatesPitchedTestData", groups = {"contact_service", "nightly-build"})
    public void crossAccountContactDetailPageCandidatesPitchedOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;
        switch (operation.toUpperCase()) {
            case "GET_CONTACT_PITCHED_CANDIDATES_ACCOUNT_A":
                response = getContactPitchedCandidates(contactIdAccountA, token);
                break;
            case "GET_CONTACT_PITCHED_CANDIDATES_CROSS_ACCOUNT":
                response = getContactPitchedCandidates(contactIdAccountA, token);
                break;

            default:
                throw new RuntimeException("Unknown operation: " + operation);
        }
        int actualStatus = response.getStatusCode();
        int expectedStatus = Integer.parseInt(expectedStatusCode);
        String caseContext = "[" + testScenario + "] " + description + " - ";
        assertThat(caseContext + "Response status code should match expected (expected " + expectedStatusCode + ", got " + actualStatus + ")", actualStatus, is(equalTo(expectedStatus)));

        verifyResponse(response, expectedStatusCode, expectedResponse, operation, caseContext);
    }

    private Response getContactPitchedCandidates(int contactId, String token) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactId", String.valueOf(contactId));

        return RestClient.doPost1("JSON", contactServiceURL, "contacts/{contactId}/pitched-candidates/get",
                token, queryParams, pathParams, true, requestBody.toString());
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation, String caseContext) {
        switch (expectedResponse) {
            case "success":
                response.then().body("meta.message", containsString("success"));
                break;
            case "cross_account_isolation":
                if (response.getStatusCode() == 404) {
                    JsonPath isolationJp = response.jsonPath();
                    assertThat(caseContext + "Meta object should not be null", isolationJp.get("meta"), notNullValue());
                    assertThat(caseContext + "Meta status should be 404", (Integer) isolationJp.get("meta.status"), equalTo(404));
                    assertThat(caseContext + "Data should be null", isolationJp.get("data"), nullValue());
                } else if (response.getStatusCode() == 200) {
                    JsonPath isolationJp = response.jsonPath();
                    assertThat(caseContext + "Meta object should not be null", isolationJp.get("meta"), notNullValue());
                    List<Map<String, Object>> data = isolationJp.getList("data");
                    assertThat(caseContext + "Data should be empty for cross-account access", data.isEmpty(), is(true));
                }
                break;
            case "Unauthorised access":
                if (response.getStatusCode() == 401) {
                    response.then().body("meta.message", is("Unauthorised access"));
                }
                break;
            case "not found":
                response.then().body("errors[0].message", containsString("not found"));
                break;
            case "Bad Request":
                response.then().body("error", is("Bad Request"));
                break;
        }
    }

    @DataProvider(name = "crossAccountContactDetailPageCandidatesPitchedTestData")
    public static Object[][] crossAccountContactDetailPageCandidatesPitchedTestData() {
        return new Object[][]{
                {"SCENARIO_2_CROSS_ACCOUNT_ACCESS_B_TO_A", "AccountB", "valid", "GET_CONTACT_PITCHED_CANDIDATES_CROSS_ACCOUNT", "200", "cross_account_isolation", "Account B gets 200 with empty data for Account A's contact pitched candidates"},

                {"SCENARIO_3_INVALID_TOKEN_A", "AccountA", "invalid", "GET_CONTACT_PITCHED_CANDIDATES_ACCOUNT_A", "500", "Unauthorised access", "Account A should be denied access with invalid token"},

                {"SCENARIO_4_NONEXISTENT_ACCOUNT", "AccountC", "valid", "GET_CONTACT_PITCHED_CANDIDATES_ACCOUNT_A", "500", "Unauthorised access", "Non-existent account should return 500"},

                {"SCENARIO_4_EXPIRED_TOKEN", "AccountA", "expired", "GET_CONTACT_PITCHED_CANDIDATES_ACCOUNT_A", "500", "Unauthorised access", "Expired token should return 500"},

                {"SCENARIO_4_MALFORMED_TOKEN", "AccountA", "malformed", "GET_CONTACT_PITCHED_CANDIDATES_ACCOUNT_A", "500", "Unauthorised access", "Malformed token should return 500"},

                {"SCENARIO_5_EMPTY_TOKEN", "AccountA", "empty", "GET_CONTACT_PITCHED_CANDIDATES_ACCOUNT_A", "401", "Unauthorised access", "Empty token should return 401"},

                {"SCENARIO_5_NULL_TOKEN", "AccountA", "null", "GET_CONTACT_PITCHED_CANDIDATES_ACCOUNT_A", "500", "Unauthorised access", "Null token should return 500"},
        };
    }
}
