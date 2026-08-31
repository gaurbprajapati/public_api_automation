package io.recruitcrm.CandidateService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.PitchCandidatePipeline;
import io.rcrm.api.pojo.albatross.PitchPipelineStages;
import io.rcrm.api.pojo.candidateService.UpdatePitchCandidateStage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class UpdatePitchCandidateStageTest extends TestBase {
        commanFunction commanFunction = new commanFunction();
        Random random = new Random();

        @Owner("Gaurav Prajapati")
        @Test
        public void updatePitchStage_200() {
                JsonPath candidateResponse = pitchCandidatesToContact().jsonPath();
                ;
                String candidateId = candidateResponse.getString("id");
                String generatedString = RandomStringUtils.randomAlphabetic(4);
                Map<String, String> pathParameter1 = new HashMap<String, String>();
                pathParameter1.put("candidateId", String.valueOf(candidateId));
                // Getting the Record ID
                String getPitchData = "widgets/{candidateId}/pitch-candidate-data?entitytype=5&limit=20";
                Response getPitchCandidateContactData = RestClient.doGet("JSON", albatrossURL, getPitchData,
                                ThreadManager.getOwnerAlbatrossToken(), null, pathParameter1, true);
                Assert.assertEquals(getPitchCandidateContactData.getStatusCode(), 200);
                // Creating A Custom Pitch Stage
                String pitchStagesPath = "pitch-candidate-pipeline";
                String pitchStageName = "Candidate Service Pitch Stage";
                PitchPipelineStages pitchPipelineStages = new PitchPipelineStages();
                pitchPipelineStages.setLabel(pitchStageName);
                ArrayList<PitchPipelineStages> pitchPipelineStagesArrayList = new ArrayList<>();
                pitchPipelineStagesArrayList.add(pitchPipelineStages);
                PitchCandidatePipeline pitchCandidatePipeline = new PitchCandidatePipeline();
                pitchCandidatePipeline.setPitchPipelineStages(pitchPipelineStagesArrayList);

                Response response = RestClient.doPost("JSON", albatrossURL, pitchStagesPath,
                                ThreadManager.getOwnerAlbatrossToken(), null, true, pitchCandidatePipeline);
                Assert.assertEquals(response.getStatusCode(), 200);

                // Getting custom pitch stage ID
                Response response1 = RestClient.doGet("JSON", albatrossURL, pitchStagesPath,
                                ThreadManager.getOwnerAlbatrossToken(), null, null, true);
                Assert.assertEquals(response1.getStatusCode(), 200);
                int customStageID = response1.jsonPath().get("data.pitchPipelineStages[1].id");
                List<Integer> ids = new ArrayList<>();
                ids.add(candidateResponse.getInt("id"));
                // Updating Pitch Stage
                String updatePitchStagePath = "candidates/pitch-candidate/pitch-stage";
                Map<String, String> pathParameter = new HashMap<>();
                UpdatePitchCandidateStage updateCandidatePitchStage = new UpdatePitchCandidateStage();
                updateCandidatePitchStage.setIds(ids);
                // updateCandidatePitchStage.setStageDate(DateUtil.getTodayDateString("yyyy-MM-dd"));
                updateCandidatePitchStage.setRemark("Update Pitch Stage Remark " + generatedString);
                updateCandidatePitchStage.setStatusId(customStageID);
                Response response2 = RestClient.doPatchOnce("JSON", candidatesURL, updatePitchStagePath,
                                ThreadManager.getOwnerAlbatrossToken(), pathParameter, true, updateCandidatePitchStage);

                response2.prettyPrint();
                String messageUpdate = response2.jsonPath().getString("message");
                String successUpdate = response2.jsonPath().getString("success");
                Assert.assertEquals(response2.getStatusCode(), 200);
                if (!successUpdate.equals("true") && !messageUpdate.equals("Stage Updated Successfully")) {
                        Assert.fail("Updating Pitch stage failed");
                }
                response2.then().assertThat()
                                .body(matchesJsonSchemaInClasspath(
                                                "publicApi\\candidate\\pitchCandidateToContact.json"));
        }

        public Response pitchCandidatesToContact() {
                Response candidateResponse = commanFunction.createNewCandidateWithMandatoryFields(baseURL,
                                ThreadManager.getAccountApiKey());
                String companySlug = commanFunction
                                .createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
                                .jsonPath()
                                .getString("slug");
                Response contactResponse = commanFunction.createNewContact_POST(baseURL,
                                ThreadManager.getAccountApiKey(),
                                companySlug);
                String path = "pitch/{candidate}/contact/{contact}";
                Map<String, String> pathParameter = new HashMap<>();
                pathParameter.put("candidate", candidateResponse.jsonPath().getString("slug"));
                pathParameter.put("contact", contactResponse.jsonPath().getString("slug"));

                Response response = RestClient.doPost1("JSON", baseURL, path, ThreadManager.getAccountApiKey(), null,
                                pathParameter, true, null);
                Assert.assertEquals(response.getStatusCode(), 200);
                return candidateResponse;
        }
}
