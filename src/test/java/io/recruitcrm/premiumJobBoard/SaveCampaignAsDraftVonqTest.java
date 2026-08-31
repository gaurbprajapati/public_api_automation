package io.recruitcrm.premiumJobBoard;

import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.jobboard.JavaFakerVonq;
import io.rcrm.api.pojo.premiumJobBoard.SaveCampaignAsDraft;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SaveCampaignAsDraftVonqTest extends TestBase {

    public SaveCampaignAsDraftVonqTest() {
        super();
    }

    commanFunction function = new commanFunction();
    String generatedRandomString = RandomStringUtils.randomAlphabetic(4);
    int jobId = -1;

    JavaFakerVonq faker = new JavaFakerVonq();
    String jobBoardCompanyName = faker.getJobBoardCompanyName();
    String companyEmail = faker.getCompanyEmail();
    String jobDescription = faker.getJobDescription();
    int generatedRandomNumber = faker.getGeneratedRandomNumber();
    String campaignName = faker.getCampaignName();

    final String CAMPAIGNDATAJSON = "{\"campaign_form\":{\"labels\":null,\"recruiterInfo\":" +
            "{\"name\":\"" + jobBoardCompanyName + "\",\"emailAddress\":\"" + companyEmail + "\"}," +
            "\"postingDetails\":{\"title\":\"Construction Analyst Job\",\"description\":\"" + jobDescription + "\"," +
            "\"organization\":{\"name\":\"Ledner, Murray and KautzerLedner, Murray and KautzerLedner, Murray and KautzerLedner, Murray and Kautzer\","
            +
            "\"companyLogo\":\"\"},\"contactInfo\":{\"name\":\"\",\"emailAddress\":\"\",\"phoneNumber\":\"\"}," +
            "\"workingLocation\":{\"addressLine1\":\"\",\"addressLine2\":\"\",\"postcode\":\"\",\"city\":\"\"," +
            "\"country\":\"\",\"allowsRemoteWork\":0},\"yearsOfExperience\":0,\"employmentType\":\"permanent\"," +
            "\"weeklyWorkingHours\":{\"from\":0,\"to\":0},\"salaryIndication\":{\"period\":\"monthly\"," +
            "\"range\":{\"from\":0,\"to\":0,\"currency\":\"INR\"}},\"jobPageUrl\":\"https://"
            + System.getProperty("envname") + "web.recruitcrm.net/apply/16872398956200000198NWA\"," +
            "\"applicationUrl\":\"https://" + System.getProperty("envname")
            + "web.recruitcrm.net/apply/16872398956200000198NWA?source=%20Construction%20Analyst%20Job\"}," +
            "\"targetGroup\":{\"educationLevel\":[],\"seniority\":[],\"industry\":[],\"jobCategory\":[]}," +
            "\"orderedProducts\":[],\"orderedProductsSpecs\":{\"d6513524-bd5f-5b65-a242-1e5d24eefee6\":{}}," +
            "\"campaignName\":\" Construction Analyst Job\",\"companyId\":\"82033\",\"poNumber\":\"\",\"currency\":\"USD\","
            +
            "\"paymentMethod\":null},\"recruiter\":{},\"basket\":[]}";

    @Owner("Yash Rampal")
    @Test(groups = "nightly-build")
    public void saveCampaignAsDraftWithInvalidJobId() {
        // invalid job id
        jobId = -generatedRandomNumber;

        SaveCampaignAsDraft saveCampaignAsDraft = new SaveCampaignAsDraft();
        saveCampaignAsDraft.setCampaign_name(campaignName);
        saveCampaignAsDraft.setJob_id(jobId);
        saveCampaignAsDraft.setCampaign_data(CAMPAIGNDATAJSON);

        Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/draft",
                ThreadManager.getOwnerAlbatrossToken(), null, true, saveCampaignAsDraft);

        response.then().statusCode(404);
        response.then().body("error", Matchers.is(true));
        response.then().body("error_code", Matchers.is(404));
        response.then().body("error_message", Matchers.is("Invalid Job ID"));
        response.then().body("silent_progress", Matchers.is(false));
    }

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void saveCampaignAsDraftWithInvalidAuth() {
        if (jobId == -1)
            jobId = generatedRandomNumber;
        SaveCampaignAsDraft saveCampaignAsDraft = new SaveCampaignAsDraft();
        saveCampaignAsDraft.setCampaign_name(campaignName);
        saveCampaignAsDraft.setJob_id(jobId);
        saveCampaignAsDraft.setCampaign_data(CAMPAIGNDATAJSON);

        Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/draft",
                ThreadManager.getOwnerAlbatrossToken() + 123, null, true, saveCampaignAsDraft);

        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

    @Owner("Sampurn Chouksey")
    @Test(groups = "nightly-build")
    public void saveCampaignAsDraftWithEmptyFields() {
        if (jobId == -1)
            jobId = createJob();

        SaveCampaignAsDraft saveCampaignAsDraft = new SaveCampaignAsDraft();
        saveCampaignAsDraft.setCampaign_name("");
        saveCampaignAsDraft.setJob_id(jobId);
        saveCampaignAsDraft.setCampaign_data("");

        Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/draft",
                ThreadManager.getOwnerAlbatrossToken(), null, true, saveCampaignAsDraft);

        response.then().statusCode(422);
        response.then().body("message", Matchers.is("The given data was invalid."));
        response.then().body("errors.campaign_name[0]", Matchers.is("The campaign name field is required."));
        response.then().body("errors.campaign_data[0]", Matchers.is("The campaign data field is required."));
    }

    public int createJob() {
        String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

        AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
        Response getJobResponse = albatrossFunctions.getJobResponse(albatrossURL,
                ThreadManager.getOwnerAlbatrossToken(), jobSlug);
        JsonPath jpJob = getJobResponse.jsonPath();
        return jpJob.get("data.job.id");
    }
}
