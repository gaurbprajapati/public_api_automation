package io.recruitcrm.premiumJobBoard;

import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.jobboard.JavaFakerVonq;
import io.rcrm.api.pojo.premiumJobBoard.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import io.rcrm.api.pojo.premiumJobBoard.SaveCampaign.channels;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointsOfVonqTest extends TestBase {

    public AllEndpointsOfVonqTest() {
        super();
    }

    commanFunction function = new commanFunction();
    int jobId = -1;
    int savedCampaignId = -1;
    int savedDraftId = -1;

    JavaFakerVonq faker = new JavaFakerVonq();
    String jobBoardCompanyName = faker.getJobBoardCompanyName();
    String companyEmail = faker.getCompanyEmail();
    String jobBoardUrl = faker.getJobBoardUrl();
    String jobDescription = faker.getJobDescription();
    int generatedRandomNumber = faker.getGeneratedRandomNumber();
    String campaignName = faker.getCampaignName();
    String campaignId = faker.getCampaignId();
    String jobName, jobSlug, companyName, companySlug;

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

    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void fetchTokenFromVonqTest() {
        Response response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/token",
                ThreadManager.getOwnerAlbatrossToken(), null, null, true);

        response.then().statusCode(200);
        response.then().body("token", Matchers.notNullValue());
    }

    @Owner("Sampurn Chouksey")
    @Test(groups = "nightly-build")
    public void saveCampaignAsDraftTest() {
        if (jobId == -1) {
            jobId = createJob();
        }
        SaveCampaignAsDraft saveCampaignAsDraft = new SaveCampaignAsDraft();
        saveCampaignAsDraft.setCampaign_name(campaignName);
        saveCampaignAsDraft.setJob_id(jobId);
        saveCampaignAsDraft.setCampaign_data(CAMPAIGNDATAJSON);

        Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/draft",
                ThreadManager.getOwnerAlbatrossToken(), null, true, saveCampaignAsDraft);

        response.then().statusCode(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//vonq//saveCampaignAsDraft.json"));
        response.then().body("message", Matchers.is(campaignName + " was successfully saved as a draft."));
        response.then().body("status", Matchers.is("success"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.'draft campaign'.campaign_name", Matchers.is(campaignName));
        response.then().body("data.'draft campaign'.job_id", Matchers.is(jobId));
        response.then().body("data.'draft campaign'.campaign_data", Matchers.is(CAMPAIGNDATAJSON));

        savedDraftId = response.jsonPath().get("data.'draft campaign'.id");
    }

    @Owner("Gaurav Prajapati")
    @Test(dependsOnMethods = "saveCampaignAsDraftTest", groups = "nightly-build")
    public void getAllDraftCampaignTest() {
        GetCampaignDraftList getCampaignDraftList = new GetCampaignDraftList();
        getCampaignDraftList.setSort_by("created_on");
        getCampaignDraftList.setSort_order("desc");
        Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/draft-list",
                ThreadManager.getOwnerAlbatrossToken(), null, false, getCampaignDraftList);

        response.then().statusCode(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//vonq//getAllDraftCampaign.json"));
        response.then().body("records.current_page", Matchers.is(1));
        response.then().body("records.data[0].campaign_name", Matchers.is(campaignName));
        response.then().body("records.data[0].job_id", Matchers.is(jobId));
        response.then().body("records.data[0].job_name", Matchers.is(jobName));
        response.then().body("records.data[0].job_slug", Matchers.is(jobSlug));
    }

    @Owner("Yash Rampal")
    @Test(dependsOnMethods = "saveCampaignAsDraftTest", groups = "nightly-build")
    public void getSavedCampaignDraftByIdTest() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("id", String.valueOf(savedDraftId));
        Response response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/draft/{id}",
                ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, false);

        response.then().statusCode(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//vonq//getCampaignDraftById.json"));
        response.then().body("campaign_name", Matchers.is(campaignName));
        response.then().body("job_id", Matchers.is(jobId));
    }

    @Owner("Rahul Shibu")
    @Test(dependsOnMethods = "saveCampaignAsDraftTest", groups = "nightly-build")
    public void editCampaignDraftTest() {
        if (jobId == -1) {
            jobId = createJob();
        }
        SaveCampaignAsDraft saveCampaignAsDraft = new SaveCampaignAsDraft();
        saveCampaignAsDraft.setCampaign_name(campaignName);
        saveCampaignAsDraft.setJob_id(jobId);
        saveCampaignAsDraft.setCampaign_data(CAMPAIGNDATAJSON);

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("id", String.valueOf(savedDraftId));

        Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/draft/{id}",
                ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true, saveCampaignAsDraft);

        response.then().statusCode(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//vonq//saveCampaignAsDraft.json"));
        response.then().body("message", Matchers.is("Draft Campaign Updated"));
        response.then().body("status", Matchers.is("success"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.'draft campaign'.campaign_name", Matchers.is(campaignName));
        response.then().body("data.'draft campaign'.job_id", Matchers.is(jobId));
        response.then().body("data.'draft campaign'.campaign_data", Matchers.is(CAMPAIGNDATAJSON));
    }

    @Owner("Sampurn Chouksey")
    @Test(groups = "nightly-build")
    public void saveCampaignTest() {
        if (jobId == -1) {
            jobId = createJob();
        }
        SaveCampaign saveCampaign = new SaveCampaign();
        saveCampaign.setCampaign_name(campaignName);
        saveCampaign.setCampaign_id(campaignId);
        saveCampaign.setJob_id(jobId);
        saveCampaign.setTotal_channels("0");
        saveCampaign.setStatus("in progress");
        saveCampaign.setCurrency("USD");
        saveCampaign.setTotal_price(0);
        saveCampaign.setDraft_id("1");
        channels[] channels = new channels[1];
        channels[0] = new channels();
        channels[0].setChannel_id(String.valueOf(generatedRandomNumber));
        channels[0].setChannel_name(jobBoardCompanyName);
        channels[0].setIs_product(true);
        channels[0].setStatus("in progress");
        channels[0].setCurrency("USD");
        channels[0].setTotal_price(0);
        channels[0].setJob_board_link(jobBoardUrl);
        channels[0].setStart_date(faker.getStartDate());
        channels[0].setEnd_date(faker.getEndDate()); // current time + 1000 seconds in seconds
        saveCampaign.setChannels(channels);

        Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns",
                ThreadManager.getOwnerAlbatrossToken(), null, false, saveCampaign);

        response.then().statusCode(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//vonq//saveCampaign.json"));
        response.then().body("message", Matchers.is("Campaign Saved"));
        response.then().body("data.campaign.campaign_name", Matchers.is(campaignName));
        response.then().body("data.campaign.job_id", Matchers.is(jobId));
        response.then().body("data.campaign.campaign_id", Matchers.is(campaignId));
        response.then().body("data.campaign.status", Matchers.is("in progress"));
        response.then().body("data.campaign.currency", Matchers.is("USD"));
        response.then().body("data.campaign.total_price", Matchers.is(0));

        response.then().body("data.campaign_channels[0].channel_id",
                Matchers.is(String.valueOf(generatedRandomNumber)));
        response.then().body("data.campaign_channels[0].channel_name", Matchers.is(jobBoardCompanyName));
        response.then().body("data.campaign_channels[0].is_product", Matchers.is(true));
        response.then().body("data.campaign_channels[0].status", Matchers.is("in progress"));
        response.then().body("data.campaign_channels[0].currency", Matchers.is("USD"));
        response.then().body("data.campaign_channels[0].total_price", Matchers.is(0));
        response.then().body("data.campaign_channels[0].job_board_link", Matchers.is(jobBoardUrl));

        savedCampaignId = response.jsonPath().get("data.campaign.id");
    }

    @Owner("Gaurav Prajapati")
    @Test(dependsOnMethods = "saveCampaignTest", groups = "nightly-build")
    public void getAllSavedCampaignTest() {
        GetCampaignDraftList getCampaignDraftList = new GetCampaignDraftList();
        getCampaignDraftList.setSort_by("created_on");
        getCampaignDraftList.setSort_order("desc");
        Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/list",
                ThreadManager.getOwnerAlbatrossToken(), null, false, getCampaignDraftList);

        response.then().statusCode(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//vonq//getAllSavedCampaign.json"));
        response.then().body("records.current_page", Matchers.is(1));
        response.then().body("records.data[0].campaign_name", Matchers.is(campaignName));
        response.then().body("records.data[0].job_id", Matchers.is(jobId));
        response.then().body("records.data[0].campaign_id", Matchers.is(campaignId));
        response.then().body("records.data[0].status", Matchers.is("in progress"));
        response.then().body("records.data[0].currency", Matchers.is("USD"));
        response.then().body("records.data[0].total_price", Matchers.is(0));
        response.then().body("records.data[0].job_name", Matchers.is(jobName));
        response.then().body("records.data[0].job_slug", Matchers.is(jobSlug));
        response.then().body("records.data[0].company_name", Matchers.is(companyName));
        response.then().body("records.data[0].company_slug", Matchers.is(companySlug));

        response.then().body("records.data[0].channels[0].channel_id",
                Matchers.is(String.valueOf(generatedRandomNumber)));
        response.then().body("records.data[0].channels[0].channel_name", Matchers.is(jobBoardCompanyName));
        response.then().body("records.data[0].channels[0].status", Matchers.is("in progress"));
        response.then().body("records.data[0].channels[0].currency", Matchers.is("USD"));
        response.then().body("records.data[0].channels[0].total_price", Matchers.is(0));
        response.then().body("records.data[0].channels[0].job_board_link", Matchers.is(jobBoardUrl));
        response.then().body("records.data[0].channels[0].is_product", Matchers.is(1));
    }

    @Owner("Yash Rampal")
    @Test(dependsOnMethods = "saveCampaignTest", groups = "nightly-build")
    public void fetchCampaignAnalyticsVonqTest() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("id", String.valueOf(savedCampaignId));

        Response response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/{id}",
                ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

        response.then().statusCode(200);
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi//vonq//fetchCampaignAnalytics.json"));
    }

    @Owner("Rahul Shibu")
    @Test(dependsOnMethods = "saveCampaignTest", groups = "nightly-build")
    public void fetchCampaignLineChartAnalyticsVonqTest() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("id", String.valueOf(savedCampaignId));

        Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/line-chart/{id}",
                ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true, null);

        response.then().statusCode(200);
        response.then().body("categories", Matchers.empty());
        response.then().body("dataset", Matchers.nullValue());
    }

    @Owner("Sampurn Chouksey")
    @Test(dependsOnMethods = "saveCampaignTest", groups = "nightly-build")
    public void fetchCampaignAnalyticsPieChartVonqTest() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("id", String.valueOf(savedCampaignId));

        Response response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/pie-chart/{id}",
                ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

        response.then().statusCode(200);
    }

    @Owner("Gaurav Prajapati")
    @Test(dependsOnMethods = "saveCampaignTest", groups = "nightly-build")
    public void refreshCampaignAnalyticsVonqTest() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("id", String.valueOf(savedCampaignId));

        Response response = RestClient.doGet("JSON", jobBoardServiceURL, "vonq/campaigns/analytics/refresh/{id}",
                ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

        response.then().statusCode(200);
        response.then().body("message", Matchers.is("Analytics Refresh"));
        response.then().body("status", Matchers.is("success"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.updated_records", Matchers.is(0));
    }

    @Owner("Yash Rampal")
    @Test(groups = "nightly-build")
    public void incrementNameCampaignVonqTest() {
        if (jobId == -1) {
            jobId = createJob();
        }
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("id", String.valueOf(jobId));

        IncrementNameCampaign incrementNameCampaign = new IncrementNameCampaign();
        incrementNameCampaign.setCampaign_name(campaignName);

        Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/increment-name/{id}",
                ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true, incrementNameCampaign);

        response.then().statusCode(200);
        response.then().body("campaign_name", Matchers.is(campaignName + " 1"));
    }

    @Owner("Rahul Shibu")
    @Test(dependsOnMethods = "saveCampaignTest", groups = "nightly-build")
    public void takeCampaignOfflineTest() {
        TakeCampaignOffline takeCampaignOffline = new TakeCampaignOffline();
        takeCampaignOffline.setType("CAMPAIGN");
        takeCampaignOffline.setCampaign_id(savedCampaignId);

        Response response = RestClient.doPost1("JSON", jobBoardServiceURL, "vonq/campaigns/take-off-line",
                ThreadManager.getOwnerAlbatrossToken(), null, null, false, takeCampaignOffline);

        response.then().statusCode(200);
        response.then().body("message", Matchers.is("Campaign taken offline"));
        response.then().body("status", Matchers.is("success"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.campaign.campaignId[0]", Matchers.is("The campaign is not online."));
    }

    @Owner("Sampurn Chouksey")
    @Test(dependsOnMethods = "saveCampaignTest", groups = "nightly-build")
    public void deleteCampaignTest() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("id", String.valueOf(savedCampaignId));

        Response response = RestClient.doDelete("JSON", jobBoardServiceURL, "vonq/campaigns/{id}",
                ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, false);

        response.then().statusCode(200);
        response.then().body("message", Matchers.is("Campaign deleted"));
        response.then().body("status", Matchers.is("success"));
        response.then().body("message_type", Matchers.is("is-success"));
        response.then().body("data.campaign.campaignId[0]", Matchers.is("The campaign is not online."));
    }

    public int createJob() {
        jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

        AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
        Response getJobResponse = albatrossFunctions.getJobResponse(albatrossURL,
                ThreadManager.getOwnerAlbatrossToken(), jobSlug);
        JsonPath jpJob = getJobResponse.jsonPath();
        jobName = jpJob.get("data.job.name");
        jobSlug = jpJob.get("data.job.slug");
        companyName = jpJob.get("data.job.companyname");
        companySlug = jpJob.get("data.job.companyslug");
        return jpJob.get("data.job.id");
    }
}
