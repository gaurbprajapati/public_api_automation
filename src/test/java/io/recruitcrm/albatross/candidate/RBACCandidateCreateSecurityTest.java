package io.recruitcrm.albatross.candidate;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.RBAC2LevelAccessDataProvider;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import org.hamcrest.Matchers;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.pojo.albatross.Candidate;
import io.rcrm.api.pojo.albatross.createCandidatePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACCandidateCreateSecurityTest extends TestBase {
    private final JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    
    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
   
    @BeforeClass(alwaysRun = true)    public void setupToken() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    private Response createCandidate(String token) {
        String candidateFirstName = fakerCandidate.getFirstName();
        String candidateLastName = fakerCandidate.getLastName();
        String candidateEmail = fakerCandidate.getEmailID();
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
        Candidate candidate = new Candidate(false, "", candidateFirstName, candidateLastName, candidateEmail, genderId,
                candidateContactNumber, address, city, "candidate summary", locality, fbLink, twitterLink, linkedinLink,
                githubLink, xingLink);
        createCandidatePage createCandidatePage = new createCandidatePage();
        createCandidatePage.setCandidate(candidate);
        String basePath = "candidates";
        return RestClient.doPost("JSON", albatrossURL, basePath, token, null, true, createCandidatePage);
    }
    
    private void validateCandidateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + response.getStatusCode(), e);
        }

        if (expectedStatusCode == 200) {
            if (SUCCESS_MESSAGE.equals(expectedMessage)) {
                try {
                    response.then().body("data.candidate.slug", Matchers.notNullValue());
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
                }
            }
        } else if (expectedStatusCode == 401) {
            if (FORBIDDEN_MESSAGE.equals(expectedMessage)) {
                try {
                    response.then().body("message", Matchers.is(ACCESS_DENIED_MESSAGE));
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription 
                            + " - Expected '" + ACCESS_DENIED_MESSAGE + "' but got: " + response.jsonPath().getString("message"), e);
                }
            }
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "candidate2LevelCreateAccessData", groups = {"role-based", "candidate-2level-create-access"})
    public void createCandidateSecurityTest(String role, String access, int expectedStatusCode, String expectedMessage, String testDescription) {
        String roleToken = albatrossTknMap.get(role);
        Response createResponse = createCandidate(roleToken);
        validateCandidateResponse(createResponse, expectedStatusCode, expectedMessage, testDescription);
    }

    @DataProvider(name = "candidate2LevelCreateAccessData", parallel = true)
    public Object[][] candidate2LevelCreateAccessData(ITestContext context) {
        return RBAC2LevelAccessDataProvider.getCandidateAccessData(context);
    }
}