package io.recruitcrm.CandidateService;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.candidateService.EducationHistoryRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class EditEducationHistoryTest extends TestBase {
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

    @Owner("Raj Pandey")
    @Test
    public void editEducationHistoryVerify_200() {
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candID = candidateJsonPath.get("data.candidate.id");

        EducationHistoryRequest educationHistoryRequest = new EducationHistoryRequest(instituteName,educationalQualification,
                educationalSpecialization, grade, educationLocation, String.valueOf(educationStartDate),
                String.valueOf(educationEndDate), educationDescription, candidateJsonPath.get("data.candidate.slug"),
                isManuallyAdded);

        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candID));

        Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParams, true, educationHistoryRequest);

        String educationHistoryId = response1.jsonPath().getString("data.id");

        basePath = "candidates/education-history/" + educationHistoryId;
        Map<String, String> pathParams1 = new HashMap<>();
        pathParams1.put("educationHistoryId", educationHistoryId);

        educationHistoryRequest.setInstituteName(fakerCandidate.getInstituteName());
        educationHistoryRequest.setEducationalQualification(fakerCandidate.getEducationalQualification());
        educationHistoryRequest.setEducationalSpecialization(fakerCandidate.getSpecialization());
        educationHistoryRequest.setGrade(fakerCandidate.getGrade());
        educationHistoryRequest.setEducationLocation(fakerCandidate.getEducationLocation());
        educationHistoryRequest.setEducationDescription(fakerCandidate.getDescription());
        educationHistoryRequest.setIsManuallyAdded(fakerCandidate.getRandomToggleState());

        Response response2 = RestClient.doPatchOnce("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken(), null, true, educationHistoryRequest);

        Assert.assertEquals(response2.getStatusCode(), 200);
        response2.then().body("meta.message", Matchers.is("Education history edited successfully."));
        response2.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/candidate/EditEducationHistory.json"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void editEducationHistoryVerify_401() {
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candID = candidateJsonPath.get("data.candidate.id");

        EducationHistoryRequest educationHistoryRequest = new EducationHistoryRequest(instituteName,educationalQualification,
                educationalSpecialization, grade, educationLocation, String.valueOf(educationStartDate),
                String.valueOf(educationEndDate), educationDescription, candidateJsonPath.get("data.candidate.slug"),
                isManuallyAdded);

        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candID));

        Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParams, true, educationHistoryRequest);

        String educationHistoryId = response1.jsonPath().getString("data.id");

        basePath = "candidates/education-history/" + educationHistoryId;

        educationHistoryRequest.setInstituteName(fakerCandidate.getInstituteName());
        educationHistoryRequest.setEducationalQualification(fakerCandidate.getEducationalQualification());
        educationHistoryRequest.setEducationalSpecialization(fakerCandidate.getSpecialization());
        educationHistoryRequest.setGrade(fakerCandidate.getGrade());
        educationHistoryRequest.setEducationLocation(fakerCandidate.getEducationLocation());
        educationHistoryRequest.setEducationDescription(fakerCandidate.getDescription());
        educationHistoryRequest.setIsManuallyAdded(fakerCandidate.getRandomToggleState());

        Response response2 = RestClient.doPatchOnce("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "1234", null, true, educationHistoryRequest);

        Assert.assertEquals(response2.getStatusCode(), 401);
        response2.then().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Gaurav Prajapati")
    @Test
    public void editEducationHistoryVerify_404() {
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candID = candidateJsonPath.get("data.candidate.id");

        EducationHistoryRequest educationHistoryRequest = new EducationHistoryRequest(instituteName,educationalQualification,
                educationalSpecialization, grade, educationLocation, String.valueOf(educationStartDate),
                String.valueOf(educationEndDate), educationDescription, candidateJsonPath.get("data.candidate.slug"),
                isManuallyAdded);

        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candID));

        Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParams, true, educationHistoryRequest);

        String educationHistoryId = response1.jsonPath().getString("data.id");

        basePath = "candidates/education-history/-" + educationHistoryId;

        educationHistoryRequest.setInstituteName(fakerCandidate.getInstituteName());
        educationHistoryRequest.setEducationalQualification(fakerCandidate.getEducationalQualification());
        educationHistoryRequest.setEducationalSpecialization(fakerCandidate.getSpecialization());
        educationHistoryRequest.setGrade(fakerCandidate.getGrade());
        educationHistoryRequest.setEducationLocation(fakerCandidate.getEducationLocation());
        educationHistoryRequest.setEducationDescription(fakerCandidate.getDescription());
        educationHistoryRequest.setIsManuallyAdded(fakerCandidate.getRandomToggleState());

        Response response2 = RestClient.doPatchOnce("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken(), null, true, educationHistoryRequest);


        Assert.assertEquals(response2.getStatusCode(), 404);
        response2.then().body("errors[0].message", Matchers.is("Education History id -" + educationHistoryId + " not found."));
    }

    @Owner("Yash Rampal")
    @Test
    public void editEducationHistoryVerify_400() {
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candID = candidateJsonPath.get("data.candidate.id");

        EducationHistoryRequest educationHistoryRequest = new EducationHistoryRequest(instituteName,educationalQualification,
                educationalSpecialization, grade, educationLocation, String.valueOf(educationStartDate),
                String.valueOf(educationEndDate), educationDescription, candidateJsonPath.get("data.candidate.slug"),
                isManuallyAdded);

        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candID));

        Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParams, true, educationHistoryRequest);

        String educationHistoryId = response1.jsonPath().getString("data.id");

        basePath = "candidates/education-history/" + educationHistoryId + fakerCandidate.getDescription();
        Map<String, String> pathParams1 = new HashMap<>();
        pathParams1.put("educationHistoryId", educationHistoryId);

        educationHistoryRequest.setInstituteName(fakerCandidate.getInstituteName());
        educationHistoryRequest.setEducationalQualification(fakerCandidate.getEducationalQualification());
        educationHistoryRequest.setEducationalSpecialization(fakerCandidate.getSpecialization());
        educationHistoryRequest.setGrade(fakerCandidate.getGrade());
        educationHistoryRequest.setEducationLocation(fakerCandidate.getEducationLocation());
        educationHistoryRequest.setEducationDescription(fakerCandidate.getDescription());
        educationHistoryRequest.setIsManuallyAdded(fakerCandidate.getRandomToggleState());

        Response response2 = RestClient.doPatchOnce("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken(), null, true, educationHistoryRequest);

        Assert.assertEquals(response2.getStatusCode(), 400);
        response2.then().body("error", Matchers.is("Bad Request"));
    }
}
