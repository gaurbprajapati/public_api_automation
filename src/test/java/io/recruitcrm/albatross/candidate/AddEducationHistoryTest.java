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
public class AddEducationHistoryTest extends TestBase {

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
        isManuallyAdded = 0;
        function = new AllCrudFunctions();
    }

    @DataProvider(name = "candidateData")
    public Object[][] createCandidateData() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candidateId = candidateJsonPath.getInt("data.candidate.id");
        String candidateSlug = candidateJsonPath.getString("data.candidate.slug");

        return new Object[][]{
                {candidateId, candidateSlug}
        };
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "candidateData", groups = "nightly-build")
    public void addEducationHistoryWithValidFields(int candidateId, String candidateSlug) {
        EducationHistoryRequestInCandidateDetailPage educationHistoryRequest = new EducationHistoryRequestInCandidateDetailPage(instituteName, educationalQualification, educationalSpecialization, grade, educationLocation, educationStartDate, educationEndDate, educationDescription, isManuallyAdded, candidateId, candidateSlug);

        String basePath = "candidates/candidate-education/create";
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, educationHistoryRequest);
        Assert.assertEquals(response.jsonPath().getString("message"), "Candidate Education is created successfully.", "Unexpected response message.");
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "candidateData", groups = "nightly-build")
    public void addEducationHistoryVerify_401(int candidateId, String candidateSlug) {
        EducationHistoryRequestInCandidateDetailPage educationHistoryRequest = new EducationHistoryRequestInCandidateDetailPage(instituteName, educationalQualification, educationalSpecialization, grade, educationLocation, educationStartDate, educationEndDate, educationDescription, isManuallyAdded, candidateId, candidateSlug);

        String basePath = "candidates/candidate-education/create";
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "invalid", null, null, true, educationHistoryRequest);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401, but got " + response.getStatusCode());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "candidateData", groups = "nightly-build")
    public void addEducationHistoryVerify_422(int candidateId, String candidateSlug) {
        JSONObject educationJson = new JSONObject();
        educationJson.put("institute_name", instituteName);
        educationJson.put("educational_qualification", educationalQualification);
        educationJson.put("educational_specialization", educationalSpecialization);
        educationJson.put("grade", grade);
        educationJson.put("education_location", educationLocation);
        educationJson.put("education_start_date", 00);
        educationJson.put("education_end_date", educationEndDate);
        educationJson.put("education_description", educationDescription);
        educationJson.put("is_manually_added", isManuallyAdded);
        educationJson.put("candidate_slug", candidateSlug);

        String basePath = "candidates/candidate-education/create";
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, educationJson.toString());
        Assert.assertEquals(response.getStatusCode(), 422, "Expected status code 422, but got " + response.getStatusCode());
    }

    // The status code 404 bug occurs while adding education history i have already reported it. Once it's fixed, I will automate the status code handling.

}