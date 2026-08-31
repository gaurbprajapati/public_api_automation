package io.recruitcrm.report;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.pojo.enrollInSequence;
import io.rcrm.api.pojo.nyma.AddEmailStepsToSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailStepToSequencePage;
import io.rcrm.api.pojo.nyma.SequenceSettingPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.ExportCSV;
import io.recruitcrm.report.pojo.KpiLists;
import io.recruitcrm.report.pojo.TeamPerformanceReport;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class TeamPerformanceReportSeqKPI_Test extends TestBase {

    public TeamPerformanceReportSeqKPI_Test() {
        super();
    }

    commanFunction function = new commanFunction();
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    String userId;
    String candidateEntitySlug;
    String yesterdayTimestamp;
    String tomorrowTimestamp;
    int sequenceId;
    ListFunctions listFunctions = new ListFunctions();

    @BeforeClass(alwaysRun = true)    public void createEnrollSequence() {
        yesterdayTimestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(DateUtil.yesterday().getTime()));
        tomorrowTimestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(DateUtil.tommarrow().getTime()));
        userId = String.valueOf(ThreadManager.getOwner().getUserId());

        candidateEntitySlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
                .get("slug");
        sequenceId = createEmailSequence("candidates");
        enrollInSequence enrollInSequence = new enrollInSequence();

        enrollInSequence.setSequence_id(sequenceId);
        enrollInSequence.setProspect_slug(candidateEntitySlug);
        String basePath = "candidates/" + candidateEntitySlug + "/enroll";

        Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, enrollInSequence);
        Assert.assertEquals(response.statusCode(), 200);
        int enrollmentId = response.jsonPath().get("id");
        try {
            if (ReaperIntegration.updateSeqEnrollmentSteps(enrollmentId).getStatusCode() != 200) {
                Thread.sleep(15000);    //delay for Queue to process
                ReaperIntegration.updateSeqEnrollmentSteps(enrollmentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "getReportValidTestData", groups = "nightly-build")
    public void getSequenceReport(String reportType) {
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("page", "1");
        queryParameters.put("page_size", "50");
        queryParameters.put("userids", String.valueOf(ThreadManager.getOwner().getUserId()));
        queryParameters.put("sort_by", "id");
        queryParameters.put("sortOrder", "desc");
        queryParameters.put("reportType", reportType);
        queryParameters.put("fromDate", String.valueOf(yesterdayTimestamp));
        queryParameters.put("toDate", String.valueOf(tomorrowTimestamp));

        Response response = RestClient.doGet("JSON", nymaURL, "reports/get-sequence-stats", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null,
                true);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(jp.get("message_type"), "is-success");

        if(reportType.equals("seqcreated")) {
            Assert.assertEquals(jp.getInt("data[0].id"), sequenceId);
        } else {
            Assert.assertEquals(jp.get("data[0].entity_slug"), candidateEntitySlug);
        }
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getTeamPerformanceReport_SeqKPI() {
        JsonPath jsonGetCollabrators = listFunctions.getAllCollabrators(baseURL, ThreadManager.getAccountApiKeyMap()).jsonPath();
        ArrayList<Integer> recruiterIds = jsonGetCollabrators.get("id");

        ArrayList<Integer> teamIds =new ArrayList<Integer>();
        ArrayList<Object> kpiLists = new ArrayList<Object>();

        KpiLists KpiLists1 = new KpiLists();
        KpiLists1.setLabel("Sequence Created");
        KpiLists1.setValue("seqcreated");
        KpiLists1.setChecked(true);

        KpiLists KpiLists2 = new KpiLists();
        KpiLists2.setLabel("Sequence Enrollments");
        KpiLists2.setValue("seqenrollment");
        KpiLists2.setChecked(true);

        KpiLists KpiLists3 = new KpiLists();
        KpiLists3.setLabel("Sequence Open rate");
        KpiLists3.setValue("seqopenrate");
        KpiLists3.setChecked(true);

        KpiLists KpiLists4 = new KpiLists();
        KpiLists4.setLabel("Sequence Reply rate");
        KpiLists4.setValue("seqreplyrate");
        KpiLists4.setChecked(true);

        KpiLists KpiLists5 = new KpiLists();
        KpiLists5.setLabel("Sequence Unsubscribed rate");
        KpiLists5.setValue("sequnsubscriberate");
        KpiLists5.setChecked(true);

        kpiLists.add(KpiLists1);
        kpiLists.add(KpiLists2);
        kpiLists.add(KpiLists3);
        kpiLists.add(KpiLists4);
        kpiLists.add(KpiLists5);

        TeamPerformanceReport teamReport = new TeamPerformanceReport();
        teamReport.setRecruiter_ids(recruiterIds);
        teamReport.setTeam_ids(teamIds);
        teamReport.setKpi_lists(kpiLists);
        teamReport.setFrom_date(yesterdayTimestamp);
        teamReport.setTo_date(tomorrowTimestamp);

        Response response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", ThreadManager.getOwnerAlbatrossToken(), null, true, teamReport);
        response.then().statusCode(200);
        response.then().body("data."+userId+".seqcreated", Matchers.greaterThan(0));
        response.then().body("data."+userId+".seqenrollment", Matchers.greaterThan(0));
        response.then().body("data."+userId+".seqopenrate", Matchers.is(100));
        response.then().body("data."+userId+".seqreplyrate", Matchers.is(100));
        response.then().body("data."+userId+".sequnsubscriberate", Matchers.is(100));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void getSequenceReportWithoutRequiredFields() {
        Map<String, String> queryParameters = new HashMap<String, String>();

        Response response = RestClient.doGet("JSON", nymaURL, "reports/get-sequence-stats", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null,
                true);
        response.then().statusCode(422);
        JsonPath jp = response.jsonPath();

        String responseMessage = jp.get("message");
        String requiredFields[] = new String[]{"page", "page size", "sort by", "sort order", "report type", "userids", "from date", "to date"};
        for(String field: requiredFields){
            Assert.assertTrue(responseMessage.contains("The "+field+" field is required"));
        }
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getSequenceReportWithInvalidFields() {
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("page", "-1");
        queryParameters.put("page_size", "-1");
        queryParameters.put("userids", "0");
        queryParameters.put("sort_by", "abc");
        queryParameters.put("sortOrder", "xyz");
        queryParameters.put("reportType", "pqr");
        queryParameters.put("fromDate", "lmno");
        queryParameters.put("toDate", "def");

        Response response = RestClient.doGet("JSON", nymaURL, "reports/get-sequence-stats", ThreadManager.getOwnerAlbatrossToken(), queryParameters, null,
                true);
        response.then().statusCode(422);
        JsonPath jp = response.jsonPath();

        String responseMessage = jp.get("message");
        Assert.assertTrue(responseMessage.contains("The selected sort order is invalid"));
        Assert.assertTrue(responseMessage.contains("The selected report type is invalid"));
        Assert.assertTrue(responseMessage.contains("The from date must be a number"));
        Assert.assertTrue(responseMessage.contains("The from date must be greater than 0"));
        Assert.assertTrue(responseMessage.contains("The to date must be a number"));
        Assert.assertTrue(responseMessage.contains("The to date must be greater than 0"));
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "getReportValidTestData", groups = "nightly-build")
    public void verifyExportCSVForSequenceKPIs(String reportType) {
        ExportCSV exportCSV = new ExportCSV();
        exportCSV.setReportType(reportType);
        exportCSV.setStatusid("allEntities");
        exportCSV.setUserids(userId);
        exportCSV.setFromDate(yesterdayTimestamp);
        exportCSV.setToDate(tomorrowTimestamp);

        Response response = RestClient.doPost("JSON", nymaURL, "reports/export-data", ThreadManager.getOwnerAlbatrossToken(), null, true, exportCSV);
        response.then().statusCode(200);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(jp.get("message_type"), "is-success");
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void verifyExportCSVForSequenceKPIsWithoutRequiredFields() {
        Response response = RestClient.doPost("JSON", nymaURL, "reports/export-data", ThreadManager.getOwnerAlbatrossToken(), null, true, null);
        response.then().statusCode(422);

        JsonPath jp = response.jsonPath();
        String responseMessage = jp.get("message");

        String requiredFields[] = new String[]{"userids", "report type", "from date", "to date"};
        for(String field: requiredFields){
            Assert.assertTrue(responseMessage.contains("The "+field+" field is required"));
        }
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void verifyExportCSVForSequenceKPIsWithInvalidFields() {
        ExportCSV exportCSV = new ExportCSV();
        exportCSV.setReportType("abc");
        exportCSV.setStatusid("xyz");
        exportCSV.setUserids("0");
        exportCSV.setFromDate("pqr");
        exportCSV.setToDate("lmno");

        Response response = RestClient.doPost("JSON", nymaURL, "reports/export-data", ThreadManager.getOwnerAlbatrossToken(), null, true, exportCSV);
        response.then().statusCode(422);

        JsonPath jp = response.jsonPath();
        String responseMessage = jp.get("message");

        Assert.assertTrue(responseMessage.contains("The selected report type is invalid"));
        Assert.assertTrue(responseMessage.contains("The from date must be a number"));
        Assert.assertTrue(responseMessage.contains("he from date must be greater than 0"));
        Assert.assertTrue(responseMessage.contains("The to date must be a number"));
        Assert.assertTrue(responseMessage.contains("The to date must be greater than 0"));
    }


    @DataProvider
    public Object[][] getReportValidTestData() {
        Object data[][] = {
                {"seqcreated"}, {"seqenrollment"}, {"seqopenrate"}, {"seqreplyrate"}, {"sequnsubscriberate"}};
        return data;
    }


    private int createEmailSequence(String entity) {
        CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();
        SequenceSettingPage sequenceSetting = new SequenceSettingPage();
        sequenceSetting.setThread_emails_as_replies(1);
        sequenceSetting.setExecute_step_on_business_days(1);
        JSONObject settings = new JSONObject(sequenceSetting);

        createEmailSequence.setEntity_type(entity.equals("candidates") ? 5 : 2);
        createEmailSequence.setSeq_title(entity + " add sequence test " + RandomStringUtils.randomAlphabetic(4));
        createEmailSequence.setSeq_settings(settings.toString());
        createEmailSequence.setSilent_progress(false);
        createEmailSequence.setSave_steps(0);

        Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
                createEmailSequence);
        JsonPath jp = response.jsonPath();
        int seqId = jp.get("data.id");
        response.then().statusCode(200);

        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("id", String.valueOf(seqId));

        CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
        createEmailStepToSequence.setStep_no(1);
        createEmailStepToSequence.setNo_of_days(2);
        createEmailStepToSequence
                .setTemplate_title(entity + " Email Template " + RandomStringUtils.randomAlphabetic(4));
        createEmailStepToSequence
                .setTemplate_subject("Creating email Template for " + entity + RandomStringUtils.randomAlphabetic(4));
        createEmailStepToSequence
                .setTemplate_content(entity + " Template body " + RandomStringUtils.randomAlphabetic(4));
        createEmailStepToSequence.setTime(3600);
        createEmailStepToSequence.setType(1);
        createEmailStepToSequence.setUpdate_type("all");
        createEmailStepToSequence.setInclude_opt_out_link(1);

        ArrayList<Object> emailStep = new ArrayList<>();
        emailStep.add(createEmailStepToSequence);
        AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
        addEmailStep.setSteps(emailStep);

        String basePath = "email-sequences/{id}/steps";
        Response responseAddEmailStep = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
                pathParameters, true, addEmailStep);

        responseAddEmailStep.then().statusCode(200);

        return seqId;
    }
}
