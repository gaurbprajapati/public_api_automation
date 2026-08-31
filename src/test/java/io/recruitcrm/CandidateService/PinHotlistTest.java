package io.recruitcrm.CandidateService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class PinHotlistTest extends TestBase {
        String albatrossAuthToken;
        String accountApiKey;
        commanFunction function = new commanFunction();

        public PinHotlistTest() {
                super();
        }

        @BeforeClass(alwaysRun = true)
        public void setUp() {
                albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
                accountApiKey = ThreadManager.getAccountApiKey();
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = "nightly-build")
        public void testPinningHotlist_200() {
                JsonPath jsonCandidateHotlist = function
                                .createNewHotlist(baseURL, accountApiKey, "candidate").jsonPath();
                String candidateHotlistID = jsonCandidateHotlist.getString("id");

                String basePath = "hotlists/{hotlistId}/pinned-hotlist";
                Map<String, String> pathParams = new HashMap<>();
                pathParams.put("hotlistId", candidateHotlistID);

                Response response = RestClient.doPost1("JSON", candidatesURL, basePath,
                                albatrossAuthToken, null, pathParams, true, null);

                JsonPath jsonResponse = response.jsonPath();

                assertThat(response.getStatusCode(), equalTo(200));
                assertThat(jsonResponse.getString("meta.message"), equalTo("Hotlist pinned successfully."));
                assertThat(jsonResponse.getString("meta.responseType.context"), equalTo("Request is successful"));
                assertThat(jsonResponse.getInt("meta.responseType.code"), equalTo(103));
                assertThat(jsonResponse.getInt("meta.status"), equalTo(200));
                assertThat(jsonResponse.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonResponse.getString("meta.timestamp"), notNullValue());
                assertThat(jsonResponse.get("data"), nullValue());

                response.then().assertThat()
                                .body(matchesJsonSchemaInClasspath("privateApi/candidate/pinHotlist.json"));
        }

        @Owner("Yash Rampal")
        @Test(groups = "nightly-build")
        public void testPinningHotlistWrongURL_404() {
                JsonPath jsonCandidateHotlist = function
                                .createNewHotlist(baseURL, accountApiKey, "candidate").jsonPath();
                String candidateHotlistID = jsonCandidateHotlist.getString("id");

                String basePath = "hotlists/{hotlistId}/pinned-hotlis"; // Incorrect URL
                Map<String, String> pathParams = new HashMap<>();
                pathParams.put("hotlistId", candidateHotlistID);

                Response response = RestClient.doPost1("JSON", candidatesURL, basePath,
                                albatrossAuthToken, null, pathParams, true, null);

                JsonPath jsonResponse = response.jsonPath();

                assertThat(response.getStatusCode(), equalTo(404));
                assertThat(jsonResponse.getString("error"), equalTo("Not Found"));
        }

        @Owner("Raj Pandey")
        @Test(groups = "nightly-build")
        public void testPinningHotlist_UnauthorizedAccess_401() {
                JsonPath jsonCandidateHotlist = function
                                .createNewHotlist(baseURL, accountApiKey, "candidate").jsonPath();
                String candidateHotlistID = jsonCandidateHotlist.getString("id");

                String basePath = "hotlists/{hotlistId}/pinned-hotlist";
                Map<String, String> pathParams = new HashMap<>();
                pathParams.put("hotlistId", candidateHotlistID);

                Response response = RestClient.doPost1("JSON", candidatesURL, basePath,
                                "InvalidBearerToken", null, pathParams, true, null);

                JsonPath jsonResponse = response.jsonPath();

                assertThat(response.getStatusCode(), equalTo(401));
                assertThat(jsonResponse.getString("meta.message"), equalTo("Unauthorised access"));
                assertThat(jsonResponse.getString("meta.responseType.context"), equalTo("Warning"));
                assertThat(jsonResponse.getInt("meta.responseType.code"), equalTo(104));
                assertThat(jsonResponse.getInt("meta.status"), equalTo(401));
                assertThat(jsonResponse.getString("meta.requestUuid"), notNullValue());
                assertThat(jsonResponse.getString("meta.timestamp"), notNullValue());
                assertThat(jsonResponse.getString("data"), equalTo("Internal Server Error"));
        }

        @Owner("Sampurn Chouksey")
        @Test(groups = "nightly-build")
        public void testPinningAlreadyPinnedHotlist_500() {
                JsonPath jsonCandidateHotlist = function
                                .createNewHotlist(baseURL, accountApiKey, "candidate").jsonPath();
                String candidateHotlistID = jsonCandidateHotlist.getString("id");

                String basePath = "hotlists/{hotlistId}/pinned-hotlist";
                Map<String, String> pathParams = new HashMap<>();
                pathParams.put("hotlistId", candidateHotlistID);

                // First pin
                Response initialResponse = RestClient.doPost1("JSON", candidatesURL,
                                basePath, albatrossAuthToken, null, pathParams, true, null);

                assertThat(initialResponse.getStatusCode(), equalTo(200));
                assertThat(initialResponse.jsonPath().getString("meta.message"),
                                equalTo("Hotlist pinned successfully."));

                // Duplicate pin
                Response duplicatePinResponse = RestClient.doPost1("JSON", candidatesURL,
                                basePath, albatrossAuthToken, null, pathParams, true, null);

                assertThat(duplicatePinResponse.getStatusCode(), equalTo(500));
                assertThat(duplicatePinResponse.jsonPath().getString("error"),
                                equalTo("Internal Server Error"));
                assertThat(duplicatePinResponse.jsonPath().getString("path"),
                                equalTo("/v2/hotlists/" + candidateHotlistID + "/pinned-hotlist"));
        }
}
