package io.recruitcrm.CandidateService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.rcrm.api.pojo.candidateService.PitchCandidateFields;
import io.rcrm.api.pojo.candidateService.SavedPitchedField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SaveSharedClientFieldsInPitchCandidateTest extends TestBase {
    @Owner("Gaurav Prajapati")
    @Test
    public void updatePitchedFields_200() {
        // Create the list of fields to update
        List<SavedPitchedField> savedPitchedFields = new ArrayList<>();
        savedPitchedFields.add(new SavedPitchedField("candidatename", 1));
        savedPitchedFields.add(new SavedPitchedField("contactemail", 0));

        // Create the request object
        PitchCandidateFields pitchCandidateFields = new PitchCandidateFields(savedPitchedFields);

        // Define the endpoint
        String basePath = "candidates/pitch-candidate/shared-fields/checkbox-state";

        // Make the PUT request
        Response response = RestClient.doPut("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, pitchCandidateFields);

        // Log the response
        response.prettyPrint();

        // Validate the response
        Assert.assertEquals(response.getStatusCode(), 200);
        String success = response.jsonPath().getString("success");

        // Optionally validate the response against a JSON schema
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/candidate/SaveSharedClientFieldsInPitchCandidate.json"));
    }

    // TODO : Failing because of wrong code.
    @Owner("Yash Rampal")
    @Test
    public void updatePitchedFields_401() {
        // Create the list of fields to update
        List<SavedPitchedField> savedPitchedFields = new ArrayList<>();
        savedPitchedFields.add(new SavedPitchedField("candidatename", 1));
        savedPitchedFields.add(new SavedPitchedField("contactemail", 0));

        // Create the request object
        PitchCandidateFields pitchCandidateFields = new PitchCandidateFields(savedPitchedFields);

        // Define the endpoint
        String basePath = "candidates/pitch-candidate/shared-fields/checkbox-state";

        // Make the PUT request
        Response response = RestClient.doPut("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "RandomSting", null, true, pitchCandidateFields);

        // Log the response
        response.prettyPrint();

        // Validate the response
        Assert.assertEquals(response.getStatusCode(), 401);

    }

    @Owner("Raj Pandey")
    @Test
    public void updatePitchedFields_404() {
        // Create the list of fields to update
        List<SavedPitchedField> savedPitchedFields = new ArrayList<>();
        savedPitchedFields.add(new SavedPitchedField("candidatename", 1));
        savedPitchedFields.add(new SavedPitchedField("contactemail", 0));

        // Create the request object
        PitchCandidateFields pitchCandidateFields = new PitchCandidateFields(savedPitchedFields);

        // Define the endpoint
        String basePath = "Candidatecandidates/pitch-candidate/shared-fields/checkbox-state";

        // Make the PUT request
        Response response = RestClient.doPut("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, pitchCandidateFields);

        // Log the response
        response.prettyPrint();

        // Validate the response
        Assert.assertEquals(response.getStatusCode(), 404);
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void updatePitchedFields_400() {
        // Create the list of fields to update
        List<SavedPitchedField> savedPitchedFields = new ArrayList<>();
        savedPitchedFields.add(new SavedPitchedField("candidatename", 1));
        savedPitchedFields.add(new SavedPitchedField("contactemail", 0));

        // Create the request object
        PitchCandidateFields pitchCandidateFields = new PitchCandidateFields(savedPitchedFields);

        // Define the endpoint
        String basePath = "candidates/pitch-candidate/shared-fields/checkbox-state";

        // Make the PUT request
        Response response = RestClient.doPut("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, pitchCandidateFields + "2w2n");

        // Log the response
        response.prettyPrint();

        // Validate the response
        Assert.assertEquals(response.getStatusCode(), 400);

    }
}
