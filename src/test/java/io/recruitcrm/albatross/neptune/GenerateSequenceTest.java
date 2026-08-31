package io.recruitcrm.albatross.neptune;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.Login;
import io.rcrm.api.pojo.neptune.GenerateEmailSequence;
import io.rcrm.api.pojo.neptune.GenerateEmailSequenceStep;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GenerateSequenceTest extends TestBase {

    String albatrossToken;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "getValidTestData", groups = "nightly-build")
    public void generateEmailSequenceWithMandatoryFields_Test(String entityType, String prompt) {
        GenerateEmailSequence generateEmailSequence = new GenerateEmailSequence(entityType, prompt);
        String basePath = "generate-email-sequence";

        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, albatrossToken, null, true, generateEmailSequence);
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Owner("Harika")
    @Test(dataProvider = "getSequenceStepTestData", groups = "nightly-build")
    public void generateEmailSequenceStep(String entityType, int seqStepType, String lastResponse, String prompt) {
        GenerateEmailSequenceStep generateEmailSequenceStep = new GenerateEmailSequenceStep(entityType, seqStepType, lastResponse, prompt);
        String basePath = "generate-email-sequence";

        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, albatrossToken, null, true, generateEmailSequenceStep);
        Assert.assertEquals(response.getStatusCode(), 200);

    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void generateEmailSequenceWithoutRequiredFields() {
        Login invalidObj = new Login();
        String basePath = "generate-email-sequence";

        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, albatrossToken, null, true, invalidObj);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 400);
        Assert.assertEquals(jp.getString("meta.message_type"), "is-fail");
        Assert.assertEquals(jp.getString("meta.message"), "Validation error");
        Assert.assertEquals(jp.getString("errors.errors.prompt.errorMsg"), "Field required");
        Assert.assertEquals(jp.getString("errors.errors.entity_type.errorMsg"), "Field required");
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void generateEmailSequenceWithInvalidFields() {
        GenerateEmailSequenceStep generateEmailSequenceStep = new GenerateEmailSequenceStep("abc", 0, "", "");
        String basePath = "generate-email-sequence";

        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, albatrossToken, null, true, generateEmailSequenceStep);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 400);
        Assert.assertEquals(jp.getString("meta.message_type"), "is-fail");
        Assert.assertEquals(jp.getString("meta.message"), "Validation error");
        Assert.assertEquals(jp.getString("errors.errors.entity_type.errorMsg"), "entity_type is invalid");
        Assert.assertEquals(jp.getString("errors.errors.seq_step_type.errorMsg"), "seq_step_type is invalid");
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "getValidTestData", groups = "nightly-build")
    public void unauthorizedUserCannotGenerateEmailSequence_Test(String entityType, String prompt) {
        GenerateEmailSequence generateEmailSequence = new GenerateEmailSequence(entityType, prompt);
        String basePath = "generate-email-sequence";

        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, albatrossToken + "123", null, true, generateEmailSequence);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(jp.getString("detail"), "Unauthorized");
    }

    @DataProvider
    public Object[][] getValidTestData() {
        return new Object[][]{
                {"candidates", "Launch an email campaign focusing on our " +
                    "innovative recruitment software for HR managers and agencies, re-engage former candidates, gather " +
                    "feedback on our services, spotlight a recruitment event for IT professionals, introduce a new " +
                    "partnership with a tech leader to expand our candidate pool, and promote a referral program with " +
                    "rewards. This campaign will include 3 emails and 2 tasks, aiming to pique interest, inform, and " +
                    "motivate action, all in a professional, clear, and engaging manner suitable for each audience"}
        };
    }

    @DataProvider(parallel = true)
    public Object[][] getSequenceStepTestData() {
        return new Object[][]{
                {"contacts", 1, null, "Campaign for Banking Service"},
                {"contacts", 2, "Follow up contact", "Campaign for Banking Service"}
        };
    }

}
