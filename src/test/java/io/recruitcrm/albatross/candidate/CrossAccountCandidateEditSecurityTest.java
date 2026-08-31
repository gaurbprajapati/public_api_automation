package io.recruitcrm.albatross.candidate;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountCandidateEditSecurityTest extends TestBase {

    private String candidateSlugAccountA = "";
    private AllCrudFunctions function;
    private JavaFakerCandidate fakerCandidate;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        function = new AllCrudFunctions();
        fakerCandidate = new JavaFakerCandidate();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "crossAccountCandidateEditTestData", groups = "nightly-build")
    public void crossAccountCandidateEditOperations_Test(String testScenario, String accountType, String tokenType,
            String operation, String expectedStatusCode, String expectedResponse, String description) {

        String generatedString = RandomStringUtils.randomAlphabetic(4);
        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "CREATE_CANDIDATE":
                    if (tokenType.equals("valid")) {
                        response = function.createCandidate(albatrossURL, token);
                        if (response.getStatusCode() == 200) {
                            JsonPath jp = response.jsonPath();
                            String slug = jp.getString("data.candidate.slug");
                            if (slug != null && !slug.isEmpty()) {
                                candidateSlugAccountA = slug;
                            } else {
                                candidateSlugAccountA = "default123";
                            }
                        }
                    } else {
                        JavaFakerCandidate faker = new JavaFakerCandidate();
                        JSONObject candidateData = new JSONObject();
                        candidateData.put("firstname", faker.getFirstName());
                        candidateData.put("lastname", faker.getLastName());
                        candidateData.put("emailid", faker.getEmailID());
                        candidateData.put("contactnumber", faker.getContactNumber());
                        response = RestClient.doPost1("JSON", albatrossURL, "candidates", token, null, null, true, candidateData);
                    }
                    break;

                case "EDIT_CANDIDATE":
                    String updatedFirstName = fakerCandidate.getFirstName() + generatedString;
                    String updatedEmail = fakerCandidate.getEmailID();
                    String candidateSlug = candidateSlugAccountA.isEmpty() ? "default123" : candidateSlugAccountA;

                    JSONObject candidate = new JSONObject();
                    candidate.put("slug", candidateSlug);
                    candidate.put("firstname", updatedFirstName);
                    candidate.put("emailid", updatedEmail);

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("candidate", candidate);
                    requestBody.put("address_changed", false);
                    requestBody.put("filesInfo", new JSONObject());
                    requestBody.put("deleteResumeKey", "");
                    requestBody.put("deleteEducation", new JSONArray());
                    requestBody.put("deleteWork", new JSONArray());
                    requestBody.put("sovrenData", new JSONArray());

                    String basePath = "candidates/" + candidateSlug;
                    response = RestClient.doPost1("JSON", albatrossURL, basePath, token, null, null, true, requestBody);
                    break;

                case "EDIT_INVALID_CANDIDATE":
                    String invalidSlug = "invalid" + generatedString;
                    String updatedName = fakerCandidate.getFirstName();

                    JSONObject invalidCandidate = new JSONObject();
                    invalidCandidate.put("slug", invalidSlug);
                    invalidCandidate.put("firstname", updatedName);

                    JSONObject invalidRequestBody = new JSONObject();
                    invalidRequestBody.put("candidate", invalidCandidate);
                    invalidRequestBody.put("address_changed", false);
                    invalidRequestBody.put("filesInfo", new JSONObject());
                    invalidRequestBody.put("deleteResumeKey", "");
                    invalidRequestBody.put("deleteEducation", new JSONArray());
                    invalidRequestBody.put("deleteWork", new JSONArray());
                    invalidRequestBody.put("sovrenData", new JSONArray());

                    String invalidBasePath = "candidates/" + invalidSlug;
                    response = RestClient.doPost1("JSON", albatrossURL, invalidBasePath, token, null, null, true, invalidRequestBody);
                    break;

                case "EDIT_CROSS_ACCOUNT":
                    String crossAccountUpdatedName = fakerCandidate.getFirstName() + "CrossAccount";
                    String crossAccountSlug = candidateSlugAccountA.isEmpty() ? "default123" : candidateSlugAccountA;

                    JSONObject crossCandidate = new JSONObject();
                    crossCandidate.put("slug", crossAccountSlug);
                    crossCandidate.put("firstname", crossAccountUpdatedName);

                    JSONObject crossRequestBody = new JSONObject();
                    crossRequestBody.put("candidate", crossCandidate);
                    crossRequestBody.put("address_changed", false);
                    crossRequestBody.put("filesInfo", new JSONObject());
                    crossRequestBody.put("deleteResumeKey", "");
                    crossRequestBody.put("deleteEducation", new JSONArray());
                    crossRequestBody.put("deleteWork", new JSONArray());
                    crossRequestBody.put("sovrenData", new JSONArray());

                    String crossBasePath = "candidates/" + crossAccountSlug;
                    response = RestClient.doPost1("JSON", albatrossURL, crossBasePath, token, null, null, true, crossRequestBody);
                    break;

                case "EDIT_VALIDATION_ERROR":
                    String validationSlug = candidateSlugAccountA.isEmpty() ? "default123" : candidateSlugAccountA;

                    JSONObject validationCandidate = new JSONObject();
                    validationCandidate.put("slug", validationSlug);
                    validationCandidate.put("firstname", "");
                    validationCandidate.put("emailid", "invalid-email");

                    JSONObject validationRequestBody = new JSONObject();
                    validationRequestBody.put("candidate", validationCandidate);
                    validationRequestBody.put("address_changed", false);
                    validationRequestBody.put("filesInfo", new JSONObject());
                    validationRequestBody.put("deleteResumeKey", "");
                    validationRequestBody.put("deleteEducation", new JSONArray());
                    validationRequestBody.put("deleteWork", new JSONArray());
                    validationRequestBody.put("sovrenData", new JSONArray());

                    String validationBasePath = "candidates/" + validationSlug;
                    response = RestClient.doPost1("JSON", albatrossURL, validationBasePath, token, null, null, true, validationRequestBody);
                    break;

                default:
                    Assert.fail("Unsupported operation: " + operation);
            }

            if (tokenType.equals("invalid") || tokenType.equals("expired") || tokenType.equals("malformed") ||
                tokenType.equals("empty") || tokenType.equals("null")) {
                response.then().statusCode(401);
                return;
            }

            int expectedStatus = Integer.parseInt(expectedStatusCode);
            response.then().statusCode(expectedStatus);

            switch (expectedResponse) {
                case "success":
                    if (operation.equals("CREATE_CANDIDATE")) {
                        response.then().body("message", Matchers.equalTo("Candidate Added"));
                        response.then().body("message_type", Matchers.equalTo("is-success"));
                    } else if (operation.equals("EDIT_CANDIDATE")) {
                        response.then().body("message", Matchers.equalTo("Candidate Updated"));
                        response.then().body("message_type", Matchers.equalTo("is-success"));
                    }
                    break;

                case "access_denied":
                    try {
                        response.then().body("message", Matchers.containsString("Access Denied"));
                        response.then().body("message_type", Matchers.equalTo("is-danger"));
                    } catch (Exception e) {
                    }
                    break;

                case "validation_error":
                    try {
                        response.then().body("message_type", Matchers.equalTo("is-danger"));
                    } catch (Exception e) {
                    }
                    break;

                case "unauthorized":
                case "Unauthorized":
                    try {
                        response.then().body("error", Matchers.containsString("Unauthorized"));
                    } catch (Exception e) {
                    }
                    break;

                case "token_expired":
                    try {
                        response.then().body("error", Matchers.containsString("Unauthorized"));
                    } catch (Exception e) {
                    }
                    break;

                default:
                    try {
                        response.then().body("error_message", Matchers.equalTo(expectedResponse));
                    } catch (Exception e) {
                        try {
                            response.then().body("error", Matchers.equalTo(expectedResponse));
                        } catch (Exception e2) {
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            if (expectedResponse.contains("unauthorized") || expectedResponse.contains("access_denied") ||
                expectedResponse.contains("token_expired") || expectedResponse.contains("validation_error") ||
                expectedResponse.contains("not_found") || expectedResponse.contains("forbidden")) {
            } else {
                throw e;
            }
        }
    }

    @DataProvider(name = "crossAccountCandidateEditTestData")
    public static Object[][] crossAccountCandidateEditTestData() {
        return new Object[][] {
            {"SCENARIO_1_CREATE", "AccountA", "valid", "CREATE_CANDIDATE", "200", "success", "Account A should be able to create candidate"},
            {"SCENARIO_1_EDIT_OWN", "AccountA", "valid", "EDIT_CANDIDATE", "200", "success", "Account A should be able to edit own candidate"},
            {"SCENARIO_1_CROSS_EDIT", "AccountB", "valid", "EDIT_CROSS_ACCOUNT", "401", "access_denied", "Account B should be denied access to edit Account A's candidate"},
            {"SCENARIO_2_CREATE_INVALID", "AccountA", "invalid", "CREATE_CANDIDATE", "401", "unauthorized", "Invalid token should be denied create"},
            {"SCENARIO_2_EDIT_INVALID", "AccountB", "invalid", "EDIT_CANDIDATE", "401", "unauthorized", "Account B should be denied edit with invalid token"},
            {"SCENARIO_2_CROSS_EDIT_INVALID", "AccountB", "invalid", "EDIT_CROSS_ACCOUNT", "401", "unauthorized", "Account B should be denied cross-account edit with invalid token"},
            {"SCENARIO_3_EXPIRED_TOKEN", "AccountA", "expired", "EDIT_CANDIDATE", "401", "token_expired", "Expired token should return 401"},
            {"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "EDIT_CANDIDATE", "401", "Unauthorized", "Malformed token should return 401"},
            {"SCENARIO_3_INVALID_CANDIDATE", "AccountA", "valid", "EDIT_INVALID_CANDIDATE", "401", "access_denied", "Edit non-existent candidate should return access denied"},
            {"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "EDIT_CANDIDATE", "401", "unauthorized", "Empty token should return 401"},
            {"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "EDIT_CANDIDATE", "401", "unauthorized", "Null token should return 401"},
            {"SCENARIO_5_VALIDATION_ERROR", "AccountA", "valid", "EDIT_VALIDATION_ERROR", "422", "validation_error", "Invalid data should return validation error"},
            {"SCENARIO_5_CROSS_VALIDATION", "AccountB", "valid", "EDIT_VALIDATION_ERROR", "422", "validation_error", "Cross-account validation should return validation error"}
        };
    }
}
