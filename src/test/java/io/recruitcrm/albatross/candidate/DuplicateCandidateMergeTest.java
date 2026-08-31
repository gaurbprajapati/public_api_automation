package io.recruitcrm.albatross.candidate;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.albatross.Candidate;
import io.rcrm.api.pojo.albatross.createCandidatePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DuplicateCandidateMergeTest extends TestBase {

    commanFunction function = new commanFunction();
    AllCrudFunctions albatrossFunction = new AllCrudFunctions();
    JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
    String albatrossAuthToken;
    String accountAPIKey;
    int ownerAccountID;
    String candidateFirstName = fakerCandidate.getFirstName();
    String candidateLastName = fakerCandidate.getLastName();
    String candidateContactNumber = fakerCandidate.getContactNumber();
    int genderId = 1;
    String fbLink = fakerCandidate.getUrl();
    String twitterLink = fakerCandidate.getUrl();
    String githubLink = fakerCandidate.getUrl();
    String linkedinLink = fakerCandidate.getUrl();
    String xingLink = fakerCandidate.getUrl();
    String city = fakerCandidate.getCity();
    String locality = fakerCandidate.getLocality();
    String address = fakerCandidate.getCandidateAddress();
    String candidateEmail = fakerCandidate.getEmailID();

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        accountAPIKey = ThreadManager.getAccountApiKey();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getCandidateDuplicateMergeData_LinkedinURL", groups = "nightly-build")
    public void createCandidateAndVerifyDuplicateCandidateMergeByLinkedinURL_Test(String candidateSlug, String candidateFirstname, String candidateLinkedinURL) {
        Map<String, String> authTokenMap = getAuthTokenMap(albatrossAuthToken);

        Candidate candidate = new Candidate(false, "", candidateFirstName, candidateLastName, candidateEmail, genderId,
                candidateContactNumber, address, city, "candidate summary", locality, fbLink, twitterLink, candidateLinkedinURL,
                githubLink, xingLink);
        createCandidatePage createCandidatePage = new createCandidatePage();
        createCandidatePage.setCandidate(candidate);
        String basePath = "candidates";

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true,
                createCandidatePage);

        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200, "Response Code must be 200!");
        Assert.assertEquals(jp.getString("message"), "Duplicate Candidate Updated Successfully!");
        Assert.assertEquals(jp.getString("message_type"), "is-success");
        Assert.assertEquals(jp.getString("data.candidate.slug"), candidateSlug, "Duplicate Candidate Merge Failed");

        Response response1 = function.getAllCandidates_GET(baseURL, accountAPIKey);
        List<Object> candidates = response1.jsonPath().getList("data");
        Assert.assertEquals(candidates.size(), 1, "Total candidates count doesn't match after merging");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getCandidateDuplicateMergeData_PhoneNumber", groups = "nightly-build")
    public void createCandidateAndVerifyDuplicateCandidateMergeByPhoneNumber_Test(String candidateSlug, String candidateFirstname, String candidatePhoneNumber) {
        Map<String, String> authTokenMap = getAuthTokenMap(albatrossAuthToken);

        Candidate candidate = new Candidate(false, "", candidateFirstName, candidateLastName, candidateEmail, genderId,
                candidatePhoneNumber, address, city, "candidate summary", locality, fbLink, twitterLink, linkedinLink,
                githubLink, xingLink);
        createCandidatePage createCandidatePage = new createCandidatePage();
        createCandidatePage.setCandidate(candidate);
        String basePath = "candidates";

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true,
                createCandidatePage);

        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200, "Response Code must be 200!");
        Assert.assertEquals(jp.getString("message"), "Duplicate Candidate Updated Successfully!");
        Assert.assertEquals(jp.getString("message_type"), "is-success");
        Assert.assertEquals(jp.getString("data.candidate.slug"), candidateSlug, "Duplicate Candidate Merge Failed");

        Response response1 = function.getAllCandidates_GET(baseURL, accountAPIKey);
        List<Object> candidates = response1.jsonPath().getList("data");
        Assert.assertEquals(candidates.size(), 1, "Total candidates count doesn't match after merging");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getCandidateDuplicateMergeData_LinkedinURL", groups = "nightly-build")
    public void importCandidateAndVerifyDuplicateCandidateMergeByLinkedinURL_Test(String candidateSlug, String candidateFirstName, String candidateLinkedinURL) {
        importCandidateAndVerifyDuplicateMergeAndDataOverriding(candidateSlug, candidateFirstName, "linkedin");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getCandidateDuplicateMergeData_PhoneNumber", groups = "nightly-build")
    public void importCandidateAndVerifyDuplicateCandidateMergeByPhoneNumber_Test(String candidateSlug, String candidateFirstName, String candidatePhoneNumber) {
        importCandidateAndVerifyDuplicateMergeAndDataOverriding(candidateSlug, candidateFirstName, "phone");
    }

    public void importCandidateAndVerifyDuplicateMergeAndDataOverriding(String candidateSlug, String candidateFirstName, String mergeUsing) {
        JsonPath users = function.getUsers(baseURL, accountAPIKey).jsonPath();
        int userId = users.getInt("[0].id");

        String importFilePath = "src/main/java/io/rcrm/api/testdata/merge_candidates_data.csv";
        Response response = albatrossFunction.importCsv(albatrossURL, albatrossAuthToken, "candidate", importFilePath, String.valueOf(userId), true, false, "", true, mergeUsing);

        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200, "Response Code must be 200!");
        Assert.assertEquals(jp.getString("message"), "Import Successful");
        Assert.assertEquals(jp.getString("message_type"), "is-success");

        String newCandidateFirstName = albatrossFunction.getCandidateResponse(albatrossURL, albatrossAuthToken, candidateSlug).jsonPath().getString("data.candidate.firstname");

        Assert.assertNotEquals(candidateFirstName, newCandidateFirstName, "Duplicate Candidate Merge Failed");
        Assert.assertEquals(newCandidateFirstName, "NewName", "Data Overriding of Duplicate Candidate Failed");

        Response response1 = function.getAllCandidates_GET(baseURL, accountAPIKey);
        List<Object> candidates = response1.jsonPath().getList("data");
        Assert.assertEquals(candidates.size(), 7, "Total candidates count doesn't match after merging");
    }

    @DataProvider
    public Object[][] getCandidateDuplicateMergeData_LinkedinURL() {
        Map<String, String> authTokenMap = getAuthTokenMap(albatrossAuthToken);

        function.enableMergeDuplicate(ownerAccountID, albatrossURL, albatrossAuthToken, "candidates");
        Candidate candidate = new Candidate(false, "", candidateFirstName, candidateLastName, candidateEmail, genderId,
                candidateContactNumber, address, city, "candidate summary", locality, fbLink, twitterLink, "https://www.linkedin.com/in/merge7",
                githubLink, xingLink);
        createCandidatePage createCandidatePage = new createCandidatePage();
        createCandidatePage.setCandidate(candidate);
        String basePath = "candidates";

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true,
                createCandidatePage);

        Assert.assertEquals(response.getStatusCode(), 200, "Response Code Must be 200!");

        JsonPath candidateJsonPath = response.jsonPath();
        String candidateSlug = candidateJsonPath.getString("data.candidate.slug");
        String candidateFirstName = candidateJsonPath.getString("data.candidate.firstname");
        String candidateLinkedinURL = candidateJsonPath.getString("data.candidate.profilelinkedin");

        return new Object[][] { { candidateSlug, candidateFirstName, candidateLinkedinURL } };
    }

    @DataProvider
    public Object[][] getCandidateDuplicateMergeData_PhoneNumber() {
        Map<String, String> authTokenMap = getAuthTokenMap(albatrossAuthToken);

        function.enableMergeDuplicate(ownerAccountID, albatrossURL, albatrossAuthToken, "candidates");
        Candidate candidate = new Candidate(false, "", candidateFirstName, candidateLastName, candidateEmail, genderId,
                "7777777777", address, city, "candidate summary", locality, fbLink, twitterLink, linkedinLink,
                githubLink, xingLink);
        createCandidatePage createCandidatePage = new createCandidatePage();
        createCandidatePage.setCandidate(candidate);
        String basePath = "candidates";

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true,
                createCandidatePage);

        Assert.assertEquals(response.getStatusCode(), 200, "Response Code Must be 200!");

        JsonPath candidateJsonPath = response.jsonPath();
        String candidateSlug = candidateJsonPath.getString("data.candidate.slug");
        String candidateFirstName = candidateJsonPath.getString("data.candidate.firstname");
        String candidatePhoneNumber = candidateJsonPath.getString("data.candidate.contactnumber");

        return new Object[][] { { candidateSlug, candidateFirstName, candidatePhoneNumber } };
    }

    private Map<String, String> getAuthTokenMap(Object authToken) {
        Map<String, String> authTokenMap;
        if(authToken instanceof Map){
            authTokenMap = (Map<String, String>) authToken;
        }else {
            String apiKey = (String) authToken;
            authTokenMap = new HashMap<String, String>();
            authTokenMap.put("Authorization", "Bearer " + apiKey);
        }
        return authTokenMap;
    }
}