package io.recruitcrm.CandidateService;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.candidateService.EducationHistoryRequest;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateEducationHistoryTest extends TestBase {
    JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
    String instituteName = fakerCandidate.getInstituteName();
    String educationalQualification = fakerCandidate.getEducationalQualification();
    String educationalSpecialization = fakerCandidate.getSpecialization();
    String grade = fakerCandidate.getGrade();
    String educationLocation = fakerCandidate.getEducationLocation();
    int educationStartDate = fakerCandidate.getStartDate();
    int educationEndDate = fakerCandidate.getEndDateWithReferenceDate(educationStartDate);
    String educationDescription = fakerCandidate.getDescription();
    boolean isManuallyAdded = fakerCandidate.getRandomToggleState();
    AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();

    @Owner("Yash Rampal")
    @Test
    public void addEducationHistoryVerify_200() {

        // This code snippet is performing the following actions:
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candID = candidateJsonPath.get("data.candidate.id");

        // Create a request body
        EducationHistoryRequest educationHistoryRequest = new EducationHistoryRequest(instituteName,educationalQualification,
                educationalSpecialization, grade, educationLocation, String.valueOf(educationStartDate),
                String.valueOf(educationEndDate), educationDescription, candidateJsonPath.get("data.candidate.slug"),
                isManuallyAdded);
        // Define the endpoint and path parameters
        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candID));

        // Make the POST request
        Response response = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParams, true, educationHistoryRequest);

        // Print the response

        // Assert the response status and validate JSON schema
        Assert.assertEquals(response.getStatusCode(), 201);
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/candidate/CreateEducationHistory.json"));
    }

    @Owner("Raj Pandey")
    @Test
    public void addEducationHistoryVerify_401() {

        // This code snippet is performing the following actions:
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candID = candidateJsonPath.get("data.candidate.id");

        // Create a request body
        EducationHistoryRequest educationHistoryRequest = new EducationHistoryRequest(instituteName,educationalQualification,
                educationalSpecialization, grade, educationLocation, String.valueOf(educationStartDate),
                String.valueOf(educationEndDate), educationDescription, candidateJsonPath.get("data.candidate.slug"),
                isManuallyAdded);

        // Define the endpoint and path parameters
        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candID));

        // Make the POST request
        Response response = RestClient.doPost1("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + fakerCandidate.getDescription(), null, pathParams, true,
                educationHistoryRequest);

        // Print the response

        // Assert the response status and validate JSON schema
        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(response.jsonPath().get("data"), "Internal Server Error");
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void addEducationHistoryVerify_400() {

        // This code snippet is performing the following actions:
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candID = candidateJsonPath.get("data.candidate.id");

        // Create a request body
        EducationHistoryRequest educationHistoryRequest = new EducationHistoryRequest(instituteName,educationalQualification,
                educationalSpecialization, grade, educationLocation, String.valueOf(educationStartDate),
                String.valueOf(educationEndDate), educationDescription, candidateJsonPath.get("data.candidate.slug"),
                isManuallyAdded);

        // Define the endpoint and path parameters
        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candID));

        // Make the POST request
        Response response = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParams, true, educationHistoryRequest);

        // Print the response

        // Assert the response status and validate JSON schema
        Assert.assertEquals(response.getStatusCode(), 400);
        Assert.assertEquals(response.jsonPath().get("error"), "Bad Request");
    }

    @Owner("Gaurav Prajapati")
    @Test
    public void addEducationHistoryVerify_404() {

        // This code snippet is performing the following actions:
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candID = candidateJsonPath.get("data.candidate.id");

        // Create a request body
        EducationHistoryRequest educationHistoryRequest = new EducationHistoryRequest(instituteName,educationalQualification,
                educationalSpecialization, grade, educationLocation, String.valueOf(educationStartDate),
                String.valueOf(educationEndDate), educationDescription, candidateJsonPath.get("data.candidate.slug"),
                isManuallyAdded);

        // Define the endpoint and path parameters
        String basePath = "candidates/{candidateId}/education-history/RandomeString";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candID));

        // Make the POST request
        Response response = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParams, true, educationHistoryRequest);

        // Print the response

        // Assert the response status and validate JSON schema
        Assert.assertEquals(response.getStatusCode(), 404);
        Assert.assertEquals(response.jsonPath().get("error"), "Not Found");
    }
}