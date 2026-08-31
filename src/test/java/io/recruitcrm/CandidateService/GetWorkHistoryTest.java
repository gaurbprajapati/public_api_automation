package io.recruitcrm.CandidateService;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.candidateService.WorkHistoryRequest;
import io.rcrm.api.restclient.RestClient;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetWorkHistoryTest extends TestBase {
        JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
        String workCompanyName = fakerCandidate.getWorkCompanyName();
        String title = fakerCandidate.getJobTitle();
        int employmentType = fakerCandidate.getEmploymentType();
        int industryId = fakerCandidate.getIndustryId();
        String workLocation = fakerCandidate.getWorkLocation();
        Boolean isCurrentlyWorking = fakerCandidate.getRandomToggleState();
        int workStartDate = fakerCandidate.getStartDate();
        int workEndDate = fakerCandidate.getEndDateWithReferenceDate(workStartDate);
        String workDescription = fakerCandidate.getDescription();
        int salary = fakerCandidate.getSalary();
        boolean isManuallyAdded = fakerCandidate.getRandomToggleState();
        WorkHistoryRequest workHistoryRequest;
        AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();

        @Owner("Gaurav Prajapati")
        @Test
        public void getCandidateWorkHistoryVerify_200() {
                Map<String, String> pathParamters = new HashMap<String, String>();
                int candidateId = addWorkHistory();
                pathParamters.put("candidateId", candidateId + "");

                String basePath = "candidates/{candidateId}/work-history?page=1&size=1";

                Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(),
                                null, pathParamters, true);
                Assert.assertEquals(response.getStatusCode(), 200);
                response.then().assertThat()
                                .body(matchesJsonSchemaInClasspath("privateApi/candidate/GetWorkHistoryTest.json"));
        }

        @Owner("Yash Rampal")
        @Test
        public void getCandidateWorkHistoryVerify_400() {
                Map<String, String> pathParamters = new HashMap<String, String>();
                int candidateId = addWorkHistory();
                pathParamters.put("candidateId", candidateId + "");

                String basePath = "candidates/{candidateId}/work-history?page=1&size=1"
                                + fakerCandidate.getDescription();
                ;

                Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(),
                                null, pathParamters, true);
                Assert.assertEquals(response.getStatusCode(), 400);
        }

        @Owner("Raj Pandey")
        @Test
        public void getCandidateWorkHistoryVerify_401() {
                Map<String, String> pathParamters = new HashMap<String, String>();
                int candidateId = addWorkHistory();
                pathParamters.put("candidateId", candidateId + "");

                String basePath = "candidates/{candidateId}/work-history?page=1&size=1";

                Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(),
                                null, pathParamters, true);
                Assert.assertEquals(response.getStatusCode(), 200);
                response.then().assertThat()
                                .body(matchesJsonSchemaInClasspath("privateApi/candidate/GetWorkHistoryTest.json"));
        }

        @Owner("Sampurn Chouksey")
        @Test
        public void getCandidateWorkHistoryVerify_404() {
                Map<String, String> pathParamters = new HashMap<String, String>();
                int candidateId = addWorkHistory();
                pathParamters.put("candidateId", candidateId + "");

                String basePath = "CCCcandidatescandidates/{candidateId}/work-history?page=1&size=1";

                Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(),
                                null, pathParamters, true);
                Assert.assertEquals(response.getStatusCode(), 404);
        }

        public int addWorkHistory() {

                Map<String, String> pathParamters = new HashMap<String, String>();
                JsonPath candidateJsonPath = albatrossFunctions1
                                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
                String candSlug = candidateJsonPath.get("data.candidate.slug");
                int candId = candidateJsonPath.get("data.candidate.id");
                workHistoryRequest = new WorkHistoryRequest(title, workCompanyName, employmentType, industryId,
                                workLocation, salary, isCurrentlyWorking, workStartDate, workEndDate, workDescription,
                                isManuallyAdded, candSlug);
                String basePath = "candidates/{candidateId}/work-history";
                pathParamters.put("candidateId", candidateJsonPath.getString("data.candidate.id"));
                Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath,
                                ThreadManager.getOwnerAlbatrossToken(),
                                null, pathParamters, true,
                                workHistoryRequest);
                Assert.assertEquals(response1.getStatusCode(), 201);
                return candId;
        }
}