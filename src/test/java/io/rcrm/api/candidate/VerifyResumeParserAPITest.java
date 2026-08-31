package io.rcrm.api.candidate;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.*;

import java.io.File;
import java.util.*;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@AccountType("Business|AlbatrossTkn")
public class VerifyResumeParserAPITest extends TestBase {

    private String apiToken;
    private File resumeFile;
    private String albatrossAuthToken;

    private static final String ENDPOINT = "candidates/resume-parser";
    private static final String RESUME_PATH = System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/ArjunReddyResume.pdf";
    private static final String RESUME_EMAIL = "arjun82@gmail.com";

    JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
    commanFunction cf = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiToken = ThreadManager.getAccountApiKey();
        resumeFile = new File(RESUME_PATH);
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyResumeParserCreatesCandidateWithOverrideNo() {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("override_data", "no");

        Response response = RestClient.doPostMultipart(baseURL, ENDPOINT, apiToken, resumeFile, "file", "application/pdf", formParams, true);
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().getString("status"), equalTo("candidate_created"));
        assertThat(response.jsonPath().getString("message"), equalTo("Resume parsed successfully"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//parseResume.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "existingCandidateData", groups = "nightly-build")
    public void verifyResumeParserCreatesCandidateWithOverrideYes(String existingCandidateSlug) {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("override_data", "yes");

        Response response = RestClient.doPostMultipart(baseURL, ENDPOINT, apiToken, resumeFile, "file", "application/pdf", formParams, true);

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().getString("status"), equalTo("candidate_updated"));
        assertThat(response.jsonPath().getString("candidate.email"), equalTo(RESUME_EMAIL));
        assertThat(response.jsonPath().get("candidate"), notNullValue());
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyResumeParserWithInvalidToken() {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("override_data", "no");

        Response response = RestClient.doPostMultipart(baseURL, ENDPOINT, "InvalidToken", resumeFile, "file", "application/pdf", formParams, true);
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().getString("error"), equalTo("Unauthorized"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyResumeParserWithEmptyToken() {
        Map<String, String> formParams = new HashMap<>();
        formParams.put("override_data", "no");

        Response response = RestClient.doPostMultipart(baseURL, ENDPOINT, "", resumeFile, "file", "application/pdf", formParams, true);
        assertThat(response.getStatusCode(), is(422));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyResumeParserWithMissingFile() {
        Response response = RestClient.doPost1("JSON", baseURL, ENDPOINT, apiToken, null, null, true, new JSONObject());
        assertThat(response.getStatusCode(), is(422));
        assertThat(response.jsonPath().getString("errorMessage"), equalTo("The file field is required."));
    }

    @DataProvider
    public Object[][] existingCandidateData() {
        int ownerAccountID = ThreadManager.getAccount().getAccountId();
        cf.enableMergeDuplicate(ownerAccountID, albatrossURL, albatrossAuthToken, "candidates");
        Candidate candidate = new Candidate();
        candidate.setFirst_name(fakerCandidate.getFirstName());
        candidate.setLast_name(fakerCandidate.getLastName());
        candidate.setEmail(RESUME_EMAIL);
        candidate.setContact_number(fakerCandidate.getContactNumber());

        Response createResponse = RestClient.doPost("JSON", baseURL, "candidates", apiToken, null, true, candidate);
        JsonPath jp = createResponse.jsonPath();
        String candidateSlug = jp.getString("slug");

        return new Object[][]{{candidateSlug}};
    }
}
