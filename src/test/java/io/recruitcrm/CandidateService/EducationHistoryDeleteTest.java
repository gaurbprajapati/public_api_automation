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

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.reaper.ThreadManager;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.Collections;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EducationHistoryDeleteTest extends TestBase {

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
        public void deleteEducationHistoryVerify_200() {
                Map<String, String> candidateDetails = addEducationHistory();
                String candidateSlug = candidateDetails.get("candidateSlug");
                int educationHistoryId = Integer.parseInt(candidateDetails.get("educationHistoryId"));

                Map<String, String> pathParameters = new HashMap<>();
                pathParameters.put("candidateId", candidateDetails.get("candidateId"));
                String basePath = "candidates/{candidateId}/education-history";

                Map<String, Object> educationHistoryPayload = new HashMap<>();
                educationHistoryPayload.put("idsToDelete", Collections.singletonList(educationHistoryId)); // Changed to
                                                                                                           // Integer
                educationHistoryPayload.put("candidateSlug", candidateSlug);

                Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
                                educationHistoryPayload);

                // Logging and verifying the response
                Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200");
                response.then().assertThat()
                                .body(matchesJsonSchemaInClasspath("privateApi/candidate/EducationHistoryDelete.json"));
        }

        @Owner("Raj Pandey")
        @Test
        public void deleteEducationHistoryVerify_401() {
                Map<String, String> candidateDetails = addEducationHistory();
                String candidateSlug = candidateDetails.get("candidateSlug");
                int educationHistoryId = Integer.parseInt(candidateDetails.get("educationHistoryId"));

                Map<String, String> pathParameters = new HashMap<>();
                pathParameters.put("candidateId", candidateDetails.get("candidateId"));
                String basePath = "candidates/{candidateId}/education-history";

                Map<String, Object> educationHistoryPayload = new HashMap<>();
                educationHistoryPayload.put("idsToDelete", Collections.singletonList(educationHistoryId)); // Changed to
                                                                                                           // Integer
                educationHistoryPayload.put("candidateSlug", candidateSlug);

                Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken() + fakerCandidate.getDescription(), null,
                                pathParameters, true,
                                educationHistoryPayload);

                // Logging and verifying the response
                Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401");
                Assert.assertEquals(response.jsonPath().get("data"), "Internal Server Error");

        }

        @Owner("Sampurn Chouksey")
        @Test
        public void deleteEducationHistoryVerify_404() {
                Map<String, String> candidateDetails = addEducationHistory();
                String candidateSlug = candidateDetails.get("candidateSlug");
                int educationHistoryId = Integer.parseInt(candidateDetails.get("educationHistoryId"));

                Map<String, String> pathParameters = new HashMap<>();
                pathParameters.put("candidateId", candidateDetails.get("candidateId"));
                String basePath = "candidates/{candidateId}/education-history" + fakerCandidate.getDescription();

                Map<String, Object> educationHistoryPayload = new HashMap<>();
                educationHistoryPayload.put("idsToDelete", Collections.singletonList(educationHistoryId)); // Changed to
                                                                                                           // Integer
                educationHistoryPayload.put("candidateSlug", candidateSlug);

                Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
                                educationHistoryPayload);

                // Logging and verifying the response
                Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404");
                Assert.assertEquals(response.jsonPath().get("error"), "Not Found");
        }

        @Owner("Gaurav Prajapati")
        @Test
        public void deleteEducationHistoryVerify_400() {
                Map<String, String> candidateDetails = addEducationHistory();
                String candidateSlug = candidateDetails.get("candidateSlug" + fakerCandidate.getDescription());
                int educationHistoryId = Integer.parseInt(candidateDetails.get("educationHistoryId"));

                Map<String, String> pathParameters = new HashMap<>();
                pathParameters.put("candidateId", candidateDetails.get("candidateId"));
                String basePath = "candidates/{candidateId}/education-history";

                Map<String, Object> educationHistoryPayload = new HashMap<>();
                educationHistoryPayload.put("idsToDelete", Collections.singletonList(educationHistoryId)); // Changed to
                                                                                                           // Integer
                educationHistoryPayload.put("candidateSlug", candidateSlug);

                Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true,
                                educationHistoryPayload);

                // Logging and verifying the response
                Assert.assertEquals(response.jsonPath().get("errors[0].errorType.context"), "Validation Error");
                Assert.assertEquals(response.jsonPath().get("meta.responseType.context"),
                                "Error while processing request");
        }

        public Map<String, String> addEducationHistory() {
                JsonPath candidateJsonPath = albatrossFunctions1
                                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
                String candidateId = candidateJsonPath.getString("data.candidate.id");
                String candidateSlug = candidateJsonPath.getString("data.candidate.slug");

                EducationHistoryRequest educationHistoryRequest = new EducationHistoryRequest(instituteName,educationalQualification,
                educationalSpecialization, grade, educationLocation, String.valueOf(educationStartDate),
                String.valueOf(educationEndDate), educationDescription, candidateJsonPath.get("data.candidate.slug"),
                isManuallyAdded);

                String basePath = "candidates/{candidateId}/education-history";
                Map<String, String> pathParams = new HashMap<>();
                pathParams.put("candidateId", candidateId);

                Response postResponse = RestClient.doPost1("JSON", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(), null, pathParams, true,
                                educationHistoryRequest);

                JsonPath postJsonPath = postResponse.jsonPath();
                String educationHistoryId = postJsonPath.getString("data.id");


                Map<String, String> candidateDetails = new HashMap<>();
                candidateDetails.put("candidateSlug", candidateSlug);
                candidateDetails.put("educationHistoryId", educationHistoryId); // Stored as String to parse later
                candidateDetails.put("candidateId", candidateId);

                return candidateDetails;
        }
}