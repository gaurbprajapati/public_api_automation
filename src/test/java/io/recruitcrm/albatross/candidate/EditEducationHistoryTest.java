package io.recruitcrm.albatross.candidate;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.EducationHistoryRequestInCandidateDetailPage;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditEducationHistoryTest extends TestBase {

    JavaFakerCandidate fakerCandidate;
    String instituteName;
    String educationalQualification;
    String educationalSpecialization;
    String grade;
    String educationLocation;
    int educationStartDate;
    int educationEndDate;
    String educationDescription;
    int isManuallyAdded;
    AllCrudFunctions function;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        fakerCandidate = new JavaFakerCandidate();
        instituteName = fakerCandidate.getInstituteName();
        educationalQualification = fakerCandidate.getEducationalQualification();
        educationalSpecialization = fakerCandidate.getSpecialization();
        grade = fakerCandidate.getGrade();
        educationLocation = fakerCandidate.getEducationLocation();
        educationStartDate = fakerCandidate.getStartDate();
        educationEndDate = fakerCandidate.getEndDateWithReferenceDate(educationStartDate);
        educationDescription = fakerCandidate.getDescription().replaceAll("<[^>]*>", "");
        isManuallyAdded = 1;
        function = new AllCrudFunctions();
    }

    @DataProvider(name = "educationHistoryData")
    public Object[][] createEducationHistoryData() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candidateId = candidateJsonPath.getInt("data.candidate.id");
        String candidateSlug = candidateJsonPath.getString("data.candidate.slug");
        EducationHistoryRequestInCandidateDetailPage educationHistoryRequest = new EducationHistoryRequestInCandidateDetailPage(fakerCandidate.getInstituteName(), fakerCandidate.getEducationalQualification(), fakerCandidate.getSpecialization(), fakerCandidate.getGrade(), fakerCandidate.getEducationLocation(), fakerCandidate.getStartDate(), fakerCandidate.getEndDateWithReferenceDate(educationStartDate), fakerCandidate.getDescription().replaceAll("<[^>]*>", ""), 0, candidateId, candidateSlug);
        String basePath = "candidates/candidate-education/create";
        Response createResponse = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, educationHistoryRequest);
        int educationHistoryId = createResponse.jsonPath().getInt("data.id");
        return new Object[][]{
                {educationHistoryId, candidateId, candidateSlug}
        };
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "educationHistoryData", groups = "nightly-build")
    public void editEducationHistoryWithValidFields(int educationHistoryId, int candidateId, String candidateSlug) {
        EducationHistoryRequestInCandidateDetailPage educationHistoryRequest = new EducationHistoryRequestInCandidateDetailPage(instituteName, educationalQualification, educationalSpecialization, grade, educationLocation, educationStartDate, educationEndDate, educationDescription, isManuallyAdded, candidateId,candidateSlug);
        educationHistoryRequest.setId(educationHistoryId);

        String basePath = "candidates/candidate-education/" + educationHistoryId;
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, educationHistoryRequest);

        Assert.assertEquals(response.jsonPath().getString("message"), "Candidate Education is updated successfully.", "Unexpected response message.");
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "educationHistoryData", groups = "nightly-build")
    public void editEducationHistoryVerify_401(int educationHistoryId, int candidateId, String candidateSlug) {
        EducationHistoryRequestInCandidateDetailPage educationHistoryRequest = new EducationHistoryRequestInCandidateDetailPage(instituteName, educationalQualification, educationalSpecialization, grade, educationLocation, educationStartDate, educationEndDate, educationDescription, isManuallyAdded, candidateId, candidateSlug);
        educationHistoryRequest.setId(educationHistoryId);

        String basePath = "candidates/candidate-education/" + educationHistoryId;
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "invalid", null, null, true, educationHistoryRequest);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401, but got " + response.getStatusCode());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void editEducationHistoryVerify_404() {
        int dummyEducationHistoryId = 99999999;
        int dummyCandidateId = 12345;
        String dummyCandidateSlug = fakerCandidate.getInvalidCandidateSlug();
        EducationHistoryRequestInCandidateDetailPage educationHistoryRequest = new EducationHistoryRequestInCandidateDetailPage(instituteName, educationalQualification, educationalSpecialization, grade, educationLocation, educationStartDate, educationEndDate, educationDescription, isManuallyAdded, dummyCandidateId, dummyCandidateSlug);
        educationHistoryRequest.setId(dummyEducationHistoryId);

        String basePath = "candidates/candidate-education/" + dummyEducationHistoryId;
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, educationHistoryRequest);

        Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404, but got " + response.getStatusCode());
    }
}