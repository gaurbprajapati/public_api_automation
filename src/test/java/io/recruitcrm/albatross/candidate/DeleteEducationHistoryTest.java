package io.recruitcrm.albatross.candidate;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.EducationHistoryRequestInCandidateDetailPage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class DeleteEducationHistoryTest extends TestBase {

    JavaFakerCandidate fakerCandidate;
    AllCrudFunctions function;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        fakerCandidate = new JavaFakerCandidate();
        function = new AllCrudFunctions();
    }

    @DataProvider(name = "educationHistoryDataForDelete")
    public Object[][] createEducationHistoryDataForDelete() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candidateId = candidateJsonPath.getInt("data.candidate.id");
        String candidateSlug = candidateJsonPath.getString("data.candidate.slug");

        EducationHistoryRequestInCandidateDetailPage educationHistoryRequest = new EducationHistoryRequestInCandidateDetailPage(fakerCandidate.getInstituteName(), fakerCandidate.getEducationalQualification(), fakerCandidate.getSpecialization(), fakerCandidate.getGrade(), fakerCandidate.getEducationLocation(), fakerCandidate.getStartDate(), fakerCandidate.getEndDateWithReferenceDate(fakerCandidate.getStartDate()), fakerCandidate.getDescription().replaceAll("<[^>]*>", ""), 0, candidateId, candidateSlug);

        String basePath = "candidates/candidate-education/create";
        Response createResponse = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, educationHistoryRequest);

        int educationHistoryId = createResponse.jsonPath().getInt("data.id");

        return new Object[][]{
                {educationHistoryId, candidateSlug}
        };
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "educationHistoryDataForDelete", groups = "nightly-build")
    public void deleteEducationHistoryWithValidIds(int educationHistoryId, String candidateSlug) {
        Map<String, Object> requestBody = new HashMap<>();
        List<Integer> idsToDelete = new ArrayList<>();
        idsToDelete.add(educationHistoryId);
        requestBody.put("idsToDelete", idsToDelete);
        requestBody.put("candidateSlug", candidateSlug);

        String basePath = "candidates/candidate-education/delete-education";
        Response response = RestClient.doDeleteOnce("application/json", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        Assert.assertEquals(response.jsonPath().getString("message"), "Education history deleted successfully.", "Unexpected response message.");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "educationHistoryDataForDelete", groups = "nightly-build")
    public void deleteEducationHistoryVerify_401(int educationHistoryId, String candidateSlug) {
        Map<String, Object> requestBody = new HashMap<>();
        List<Integer> idsToDelete = new ArrayList<>();
        idsToDelete.add(educationHistoryId);
        requestBody.put("idsToDelete", idsToDelete);
        requestBody.put("candidateSlug", candidateSlug);

        String basePath = "candidates/candidate-education/delete-education";
        Response response = RestClient.doDeleteOnce("application/json", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "invalid", null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401, but got " + response.getStatusCode());
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void deleteEducationHistoryVerify_405() {
        int dummyEducationHistoryId = 99999999;
        String dummyCandidateSlug = fakerCandidate.getInvalidCandidateSlug();

        Map<String, Object> requestBody = new HashMap<>();
        List<Integer> idsToDelete = new ArrayList<>();
        idsToDelete.add(dummyEducationHistoryId);
        requestBody.put("idsToDelete", idsToDelete);
        requestBody.put("candidateSlug", dummyCandidateSlug);

        String basePath = "candidates/candidate-education/delete-education"+"invalid";
        Response response = RestClient.doDeleteOnce("application/json", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 405, "Expected status code 405, but got " + response.getStatusCode());
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void deleteMultipleEducationHistoryEntries() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candidateId = candidateJsonPath.getInt("data.candidate.id");
        String candidateSlug = candidateJsonPath.getString("data.candidate.slug");

        EducationHistoryRequestInCandidateDetailPage firstEducationRequest = new EducationHistoryRequestInCandidateDetailPage(fakerCandidate.getInstituteName(), fakerCandidate.getEducationalQualification(), fakerCandidate.getSpecialization(), fakerCandidate.getGrade(), fakerCandidate.getEducationLocation(), fakerCandidate.getStartDate(), fakerCandidate.getEndDateWithReferenceDate(fakerCandidate.getStartDate()), fakerCandidate.getDescription().replaceAll("<[^>]*>", ""), 0, candidateId, candidateSlug);
        EducationHistoryRequestInCandidateDetailPage secondEducationRequest = new EducationHistoryRequestInCandidateDetailPage(fakerCandidate.getInstituteName(), fakerCandidate.getEducationalQualification(), fakerCandidate.getSpecialization(), fakerCandidate.getGrade(), fakerCandidate.getEducationLocation(), fakerCandidate.getStartDate(), fakerCandidate.getEndDateWithReferenceDate(fakerCandidate.getStartDate()), fakerCandidate.getDescription().replaceAll("<[^>]*>", ""), 0, candidateId, candidateSlug);

        String basePath = "candidates/candidate-education/create";
        Response firstCreateResponse = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, firstEducationRequest);
        Response secondCreateResponse = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, secondEducationRequest);
        int firstEducationId = firstCreateResponse.jsonPath().getInt("data.id");
        int secondEducationId = secondCreateResponse.jsonPath().getInt("data.id");

        Map<String, Object> requestBody = new HashMap<>();
        List<Integer> idsToDelete = new ArrayList<>();
        idsToDelete.add(firstEducationId);
        idsToDelete.add(secondEducationId);
        requestBody.put("idsToDelete", idsToDelete);
        requestBody.put("candidateSlug", candidateSlug);

        String deleteBasePath = "candidates/candidate-education/delete-education";
        Response response = RestClient.doDeleteOnce("application/json", albatrossURL, deleteBasePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        Assert.assertEquals(response.jsonPath().getString("message"), "Education history deleted successfully.", "Unexpected response message.");
    }

    // The status code 404 bug occurs while deleting education history i have already reported it. Once it's fixed, I will automate the status code handling.
}