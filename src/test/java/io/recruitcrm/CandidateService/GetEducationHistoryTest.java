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
public class GetEducationHistoryTest extends TestBase {

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

        @Owner("Sampurn Chouksey")
        @Test
        public void getCandidateEducationHistoryVerify_200() {
                Map<String, String> pathParamters = new HashMap<String, String>();
                int candidateId = addEducationHistory();
                pathParamters.put("candidateId", candidateId + "");

                String basePath = "candidates/{candidateId}/education-history?page1=&size=1";

                Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(),
                                null, pathParamters, true);
                Assert.assertEquals(response.getStatusCode(), 200);
                response.then().assertThat()
                                .body(matchesJsonSchemaInClasspath("privateApi/candidate/GetEducationHistory.json"));
        }

        @Owner("Gaurav Prajapati")
        @Test
        public void getCandidateEducationHistoryVerify_401() {
                Map<String, String> pathParamters = new HashMap<String, String>();
                int candidateId = addEducationHistory();
                pathParamters.put("candidateId", String.valueOf(candidateId));

                String basePath = "candidates/{candidateId}/education-history?page1=&size=1";

                Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken() + fakerCandidate.getDescription(),
                                null, pathParamters, true);
                // Assert the response status and validate JSON schema
                Assert.assertEquals(response.getStatusCode(), 401);
                // TODO: Expected nother Message
                Assert.assertEquals(response.jsonPath().get("data"), "Internal Server Error");

        }

        @Owner("Yash Rampal")
        @Test
        public void getCandidateEducationHistoryVerify_404() {
                Map<String, String> pathParamters = new HashMap<String, String>();
                int candidateId = addEducationHistory();
                pathParamters.put("candidateId", candidateId + "");

                String basePath = "CCCcandidates/{candidateId}/education-history?page1=&size=1";

                Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(),
                                null, pathParamters, true);
                Assert.assertEquals(response.getStatusCode(), 404);
                Assert.assertEquals(response.jsonPath().get("error"), "Not Found");

        }

        public int addEducationHistory() {

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
                Response response = RestClient.doPost1("JSON", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(),
                                null, pathParams, true, educationHistoryRequest);
                Assert.assertEquals(response.getStatusCode(), 201);
                return candID;
        }

}