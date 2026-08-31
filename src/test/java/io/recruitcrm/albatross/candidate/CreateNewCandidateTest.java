package io.recruitcrm.albatross.candidate;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.albatross.Candidate;
import io.rcrm.api.pojo.albatross.createCandidatePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateNewCandidateTest extends TestBase{

    JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
    String albatrossAuthToken;
    String apiAuthToken;
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
    @BeforeClass(alwaysRun = true)    public void setUp(){
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "emailDataProvider", groups = "nightly-build")
    public void createNewCandidate(String candidateEmail) {
        Map<String, String> authTokenMap = getAuthTokenMap(albatrossAuthToken);

        Candidate candidate = new Candidate(false, "", candidateFirstName, candidateLastName, candidateEmail, genderId,
                candidateContactNumber, address, city, "candidate summary", locality, fbLink, twitterLink, linkedinLink,
                githubLink, xingLink);
        createCandidatePage createCandidatePage = new createCandidatePage();
        createCandidatePage.setCandidate(candidate);
        String basePath = "candidates";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true,
                createCandidatePage);

        JsonPath jsonPath = response.jsonPath();

        response.then().statusCode(200);
        Assert.assertEquals(jsonPath.getString("message"), "Candidate Added");
        Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
        Assert.assertEquals(jsonPath.getString("data.candidate.emailid"), candidateEmail);
    }

    @DataProvider
    public Object[][] emailDataProvider() {
        return new Object[][] { { "Jenkins.O'Brien@yopmail.com" },{"JenkinsBrien@yopmail.com"} };
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
