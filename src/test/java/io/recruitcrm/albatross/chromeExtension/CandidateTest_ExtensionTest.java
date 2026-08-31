package io.recruitcrm.albatross.chromeExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.restassured.RestAssured;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.DuplicateMergeSetting;
import io.rcrm.api.pojo.chromeExtension.Hotlist;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import java.util.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class CandidateTest_ExtensionTest extends TestBase {

    JavaFakerCandidate javaFakerCandidate;
    String candidateFirstName, candidateLastName, candidateEmailId, candidateAddress;
    String candidateCity, candidateSummary, candidateLocality, candidateFacebook, candidateTwitter;
    String candidateLinkedIn, candidateGithub, candidateXing;
    int candidateGenderId;
    String candidateContactNumber;

    AllCrudFunctions function;
    String albatrossAuthToken;
    int ownerAccountID;
    String apiAuthToken;

    @BeforeClass(alwaysRun = true)    public void Setup() {
        javaFakerCandidate = new JavaFakerCandidate();
        candidateFirstName = javaFakerCandidate.getFirstName();
        candidateLastName = javaFakerCandidate.getLastName();
        candidateEmailId = javaFakerCandidate.getEmailID();
        candidateAddress = javaFakerCandidate.getCandidateAddress();
        candidateCity = javaFakerCandidate.getCity();
        candidateSummary = javaFakerCandidate.getCandidateSummary();
        candidateLocality = javaFakerCandidate.getLocality();
        candidateFacebook = javaFakerCandidate.getCandidateFacebookURL();
        candidateTwitter = javaFakerCandidate.getCandidateTwitterURL();
        candidateLinkedIn = javaFakerCandidate.getCandidateLinkedinURL();
        candidateGithub = javaFakerCandidate.getCandidateGithubURL();
        candidateXing = javaFakerCandidate.getCandidateXingURL();
        candidateGenderId = javaFakerCandidate.getGender_id();
        candidateContactNumber = javaFakerCandidate.getContactNumber();
        function = new AllCrudFunctions();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        apiAuthToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createCandidateHotlist_Extension() {
        Hotlist hotlist = new Hotlist();
        hotlist.setEntity_name("candidates");
        hotlist.setName(candidateFirstName + "hotlist");
        List<Integer> selectedCandidates = new ArrayList<>();
        hotlist.setSelectedrows(selectedCandidates);

        Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
                albatrossAuthToken, null, true, hotlist);

        response.then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", equalTo("Hotlist created and Candidates added successfully"))
                .body("data.hotlist[0].name", equalTo(candidateFirstName + "hotlist"))
                .body("data.hotlist[0].shared", equalTo(1));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void unauthorizedUserCannotCreateCandidateHotlist_Extension() {
        Hotlist hotlist = new Hotlist();
        hotlist.setEntity_name("candidates");
        List<Integer> selectedCandidates = new ArrayList<>();
        hotlist.setSelectedrows(selectedCandidates);

        Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
                albatrossAuthToken + "abcd", null, true, hotlist);

        response.then()
                .statusCode(200)
                .body("status", equalTo("fail"))
                .body("message", equalTo("Unauthorized access"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createCandidateHotlistWithEmptyRequestBody_Extension() {
        Hotlist hotlist = new Hotlist();
        hotlist.setEntity_name("candidates");
        List<Integer> selectedCandidates = new ArrayList<>();
        hotlist.setSelectedrows(selectedCandidates);

        Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/hotlists",
                albatrossAuthToken, null, true, hotlist);

        response.then()
                .statusCode(422)
                .body("status", equalTo("fail"))
                .body("message", equalTo("Failed To Create Hotlist : The name field is required."));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createNewCandidate_Extension() {
        RestAssured.baseURI = albatrossURL;
        Response response = RestAssured.given()
                .header("cookie", "_extToken=" + albatrossAuthToken)
                .multiPart("firstname", candidateFirstName)
                .multiPart("lastname", candidateLastName)
                .multiPart("emailid", candidateEmailId)
                .multiPart("contactnumber", candidateContactNumber)
                .post("extensions/chrome/candidate");

        JsonPath jsonPath = response.jsonPath();

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(jsonPath.get("message_type"), equalTo("is-success"));
        assertThat(jsonPath.get("message"), equalTo("Add Candidate From Extension Successful"));

        String[] candidateRequiredData = {"firstname", "lastname", "emailid", "contactnumber"};
        String[] candidateExpectedData = {candidateFirstName, candidateLastName, candidateEmailId, candidateContactNumber};

        for (int i = 0; i < candidateRequiredData.length; i++) {
            assertThat(jsonPath.get("data.candidate." + candidateRequiredData[i]),
                    equalTo(candidateExpectedData[i]));
        }
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void unauthorizedUserCannotCreateNewCandidate_Extension() {
        RestAssured.baseURI = albatrossURL;
        Response response = RestAssured.given()
                .header("cookie", "_extToken=" + albatrossAuthToken + "abcd")
                .multiPart("firstname", candidateFirstName)
                .multiPart("lastname", candidateLastName)
                .multiPart("emailid", candidateEmailId)
                .multiPart("contactnumber", candidateContactNumber)
                .post("extensions/chrome/candidate");

        JsonPath jsonPath = response.jsonPath();

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(jsonPath.get("status"), equalTo("fail"));
        assertThat(jsonPath.get("message"), equalTo("Unauthorized access"));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void createNewCandidateWithEmptyRequestBody_Extension() {
        RestAssured.baseURI = albatrossURL;
        Response response = RestAssured.given()
                .header("cookie", "_extToken=" + albatrossAuthToken)
                .multiPart("firstname","")
                .post("extensions/chrome/candidate");

        JsonPath jsonPath = response.jsonPath();

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(jsonPath.get("status"), equalTo("success"));
        assertThat(jsonPath.get("data.candidate.firstname"), equalTo(""));
        assertThat(jsonPath.getString("data.candidate.lastname"), nullValue());
        assertThat(jsonPath.get("message"), equalTo("Add Candidate From Extension Successful"));
    }


    @Owner("Rahul Shibu")
    @Test(dataProvider = "getCandidateDuplicateTestData", groups = "nightly-build")
    public void createCandidateAndVerifyDuplicateCandidateMergeByEmailId_Extension(Map<String, String> testData,
                                                                                   Boolean overrideData) {
        switch (overrideData.toString()) {
            case "false":
                RestAssured.baseURI = albatrossURL;
                Response response = RestAssured.given().header("cookie", "_extToken=" + albatrossAuthToken)
                        .multiPart("overrideData", overrideData)
                        .multiPart("firstname", candidateFirstName)
                        .multiPart("lastname", candidateLastName)
                        .multiPart("emailid", testData.get("candidateEmailId"))
                        .multiPart("contactnumber", candidateContactNumber)
                        .multiPart("address", candidateAddress)
                        .multiPart("city", candidateCity)
                        .multiPart("summary", candidateSummary)
                        .multiPart("locality", candidateLocality)
                        .multiPart("profilefacebook", candidateFacebook)
                        .multiPart("profiletwitter", candidateTwitter)
                        .multiPart("profilelinkedin", candidateLinkedIn)
                        .multiPart("profilegithub", candidateGithub)
                        .multiPart("profilexing", candidateXing)
                        .post("extensions/chrome/candidate");

                JsonPath jsonPath = response.jsonPath();

                assertThat(response.getStatusCode(), equalTo(200));
                assertThat(jsonPath.get("message_type"), equalTo("is-success"));
                assertThat(jsonPath.get("message"), equalTo("Duplicate Candidate Updated Successfully!"));
                assertThat(jsonPath.get("data.candidate.slug"), equalTo(testData.get("candidateSlug")));

                String[] candidateRequiredData = {"firstname", "lastname", "emailid", "contactnumber",
                        "address", "city", "summary", "locality"};

                String[] candidateExpectedOverrideFalseData = {
                        testData.get("candidateFirstName"),
                        testData.get("candidateLastName"),
                        testData.get("candidateEmailId"),
                        testData.get("candidateContactNumber"),
                        testData.get("candidateAddress"),
                        testData.get("candidateCity"),
                        jsonPath.get("data.candidate.summary"),
                        testData.get("candidateLocality")
                };

                assertCandidateDetails(jsonPath, candidateRequiredData, candidateExpectedOverrideFalseData);
                break;
        }
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "getCandidateDuplicateTestData", groups = "nightly-build")
    public void createCandidateAndVerifyDuplicateCandidateMergeAndOverrideDataByEmailId_Extension(Map<String, String> testData,
                                                                                                  Boolean overrideData) {
        switch (overrideData.toString()) {
            case "true":
                RestAssured.baseURI = albatrossURL;
                Response response = RestAssured.given().header("cookie", "_extToken=" + albatrossAuthToken)
                        .multiPart("overrideData", overrideData)
                        .multiPart("firstname", candidateFirstName)
                        .multiPart("lastname", candidateLastName)
                        .multiPart("emailid", testData.get("candidateEmailId"))
                        .multiPart("contactnumber", candidateContactNumber)
                        .multiPart("address", candidateAddress)
                        .multiPart("city", candidateCity)
                        .multiPart("summary", candidateSummary)
                        .multiPart("locality", candidateLocality)
                        .multiPart("profilefacebook", candidateFacebook)
                        .multiPart("profiletwitter", candidateTwitter)
                        .multiPart("profilelinkedin", candidateLinkedIn)
                        .multiPart("profilegithub", candidateGithub)
                        .multiPart("profilexing", candidateXing)
                        .post("extensions/chrome/candidate");

                JsonPath jsonPath = response.jsonPath();

                assertThat(response.getStatusCode(), equalTo(200));
                assertThat(jsonPath.get("message_type"), equalTo("is-success"));
                assertThat(jsonPath.get("message"), equalTo("Duplicate Candidate Updated Successfully!"));
                assertThat(jsonPath.get("data.candidate.slug"), equalTo(testData.get("candidateSlug")));

                String[] candidateRequiredData = {"firstname", "lastname", "emailid", "contactnumber",
                        "address", "city", "summary", "locality"};

                String[] candidateExpectedOverrideTrueData = {
                        candidateFirstName,
                        candidateLastName,
                        testData.get("candidateEmailId"),
                        candidateContactNumber,
                        candidateAddress,
                        candidateCity,
                        candidateSummary,
                        candidateLocality
                };

                assertCandidateDetailsForOverriddenData(jsonPath, candidateRequiredData, candidateExpectedOverrideTrueData);
                break;
        }
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "getCandidateDuplicateTestData", groups = "nightly-build")
    public void createCandidateAndVerifyDuplicateCandidateMergeByPhoneNumber_Extension(Map<String, String> testData,
                                                                                       Boolean overrideData) {
        switch (overrideData.toString()) {
            case "false":
                RestAssured.baseURI = albatrossURL;
                Response response = RestAssured.given().header("cookie", "_extToken=" + albatrossAuthToken)
                        .multiPart("overrideData", overrideData)
                        .multiPart("firstname", candidateFirstName)
                        .multiPart("lastname", candidateLastName)
                        .multiPart("emailid", candidateEmailId)
                        .multiPart("contactnumber", testData.get("candidateContactNumber"))
                        .multiPart("address", candidateAddress)
                        .multiPart("city", candidateCity)
                        .multiPart("summary", candidateSummary)
                        .multiPart("locality", candidateLocality)
                        .multiPart("profilefacebook", candidateFacebook)
                        .multiPart("profiletwitter", candidateTwitter)
                        .multiPart("profilelinkedin", candidateLinkedIn)
                        .multiPart("profilegithub", candidateGithub)
                        .multiPart("profilexing", candidateXing)
                        .post("extensions/chrome/candidate");
                JsonPath jsonPath = response.jsonPath();

                assertThat(response.getStatusCode(), equalTo(200));
                assertThat(jsonPath.get("message_type"), equalTo("is-success"));
                assertThat(jsonPath.get("message"), equalTo("Duplicate Candidate Updated Successfully!"));
                assertThat(jsonPath.get("data.candidate.slug"), equalTo(testData.get("candidateSlug")));

                String[] candidateRequiredData = {"firstname", "lastname", "emailid", "contactnumber",
                        "address", "city", "summary", "locality"};

                String[] candidateExpectedOverrideFalseData = {
                        testData.get("candidateFirstName"),
                        testData.get("candidateLastName"),
                        testData.get("candidateEmailId"),
                        testData.get("candidateContactNumber"),
                        testData.get("candidateAddress"),
                        testData.get("candidateCity"),
                        jsonPath.get("data.candidate.summary"),
                        testData.get("candidateLocality")
                };

                assertCandidateDetails(jsonPath, candidateRequiredData, candidateExpectedOverrideFalseData);
                break;
        }
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "getCandidateDuplicateTestData", groups = "nightly-build")
    public void createCandidateAndVerifyDuplicateCandidateMergeAndOverrideDataByPhoneNumber_Extension(Map<String, String> testData,
                                                                                                      Boolean overrideData) {
        switch (overrideData.toString()) {
            case "true":
                RestAssured.baseURI = albatrossURL;
                Response response = RestAssured.given().header("cookie", "_extToken=" + albatrossAuthToken)
                        .multiPart("overrideData", overrideData)
                        .multiPart("firstname", candidateFirstName)
                        .multiPart("lastname", candidateLastName)
                        .multiPart("emailid", candidateEmailId)
                        .multiPart("contactnumber", testData.get("candidateContactNumber"))
                        .multiPart("address", candidateAddress)
                        .multiPart("city", candidateCity)
                        .multiPart("summary", candidateSummary)
                        .multiPart("locality", candidateLocality)
                        .multiPart("profilefacebook", candidateFacebook)
                        .multiPart("profiletwitter", candidateTwitter)
                        .multiPart("profilelinkedin", candidateLinkedIn)
                        .multiPart("profilegithub", candidateGithub)
                        .multiPart("profilexing", candidateXing)
                        .post("extensions/chrome/candidate");

                JsonPath jsonPath = response.jsonPath();

                assertThat(response.getStatusCode(), equalTo(200));
                assertThat(jsonPath.get("message_type"), equalTo("is-success"));
                assertThat(jsonPath.get("message"), equalTo("Duplicate Candidate Updated Successfully!"));
                assertThat(jsonPath.get("data.candidate.slug"), equalTo(testData.get("candidateSlug")));

                String[] candidateRequiredData = {"firstname", "lastname", "emailid", "contactnumber",
                        "address", "city", "summary", "locality"};

                String[] candidateExpectedOverrideTrueData = {
                        candidateFirstName,
                        candidateLastName,
                        candidateEmailId,
                        testData.get("candidateContactNumber"),
                        candidateAddress,
                        candidateCity,
                        candidateSummary,
                        candidateLocality
                };

                assertCandidateDetailsForOverriddenData(jsonPath, candidateRequiredData, candidateExpectedOverrideTrueData);
                break;
        }
    }

    private void assertCandidateDetails(JsonPath jsonPath, String[] candidateRequiredData, String[] candidateExpectedData) {
        for (int i = 0; i < candidateRequiredData.length; i++) {
            String actualValue = jsonPath.getString("data.candidate." + candidateRequiredData[i]);
            String expectedValue = candidateExpectedData[i];

            if (actualValue != null && expectedValue != null  && actualValue.equals(expectedValue)) {
                continue;
            } else {
                assertThat("Mismatch in field: " + candidateRequiredData[i],
                        actualValue, equalTo(expectedValue));
            }
        }
    }

    private void assertCandidateDetailsForOverriddenData(JsonPath jsonPath, String[] candidateRequiredData, String[] candidateExpectedData) {
        for (int i = 0; i < candidateRequiredData.length; i++) {
            String actualValue = jsonPath.getString("data.candidate." + candidateRequiredData[i]);
            String expectedValue = candidateExpectedData[i];

            if (actualValue != null && expectedValue != null && !actualValue.equals(expectedValue)) {
                continue;
            } else {
                assertThat("Match in field: " + candidateRequiredData[i],
                        actualValue, equalTo(expectedValue));
            }
        }
    }

    @DataProvider(parallel = true)
    public Object[][] getCandidateDuplicateTestData() {
        enableMergeDuplicateCandidate();
        Response candidateResponse = function.createCandidate(albatrossURL, albatrossAuthToken);
        JsonPath candidateJsonPath = candidateResponse.jsonPath();
        Map<String, String> candidateTestData = new HashMap<>();
        candidateTestData.put("candidateEmailId", candidateJsonPath.getString("data.candidate.emailid"));
        candidateTestData.put("candidateFirstName", candidateJsonPath.getString("data.candidate.firstname"));
        candidateTestData.put("candidateLastName", candidateJsonPath.getString("data.candidate.lastname"));
        candidateTestData.put("candidateSlug", candidateJsonPath.getString("data.candidate.slug"));
        candidateTestData.put("candidateContactNumber", candidateJsonPath.getString("data.candidate.contactnumber"));
        candidateTestData.put("candidateAddress", candidateJsonPath.getString("data.candidate.address"));
        candidateTestData.put("candidateCity", candidateJsonPath.getString("data.candidate.city"));
        String summary = candidateJsonPath.getString("data.candidate.summary");
        candidateTestData.put("candidateSummary", summary);
        candidateTestData.put("candidateLocality", candidateJsonPath.getString("data.candidate.locality"));
        candidateTestData.put("candidateLinkedIn", candidateJsonPath.getString("data.candidate.profilelinkedin"));
        return new Object[][]{{candidateTestData, false}, {candidateTestData, true}};
    }

    public void enableMergeDuplicateCandidate() {
        DuplicateMergeSetting duplicateMergeSetting = new DuplicateMergeSetting();
        duplicateMergeSetting.setId(ownerAccountID);
        duplicateMergeSetting.setKey("allowduplicatecandidates");
        duplicateMergeSetting.setTableFlag("account");
        duplicateMergeSetting.setValue("0");
        Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields",
                albatrossAuthToken, null, true, duplicateMergeSetting);
        response.then().statusCode(200);
    }

}
