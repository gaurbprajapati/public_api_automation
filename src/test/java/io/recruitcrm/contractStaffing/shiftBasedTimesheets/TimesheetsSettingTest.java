package io.recruitcrm.contractStaffing.shiftBasedTimesheets;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.rcrm.api.commanfunctions.commanFunction;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;
import com.qa.api.util.reaper.ThreadManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class TimesheetsSettingTest extends ContractStaffingBaseTest{


    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;
    commanFunction function;

    @BeforeClass(alwaysRun = true)
    public void Setup() {
            albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
            ownerAccountID = ThreadManager.getAccount().getAccountId();
            apiAuthToken = ThreadManager.getAccountApiKey();
            function = new commanFunction();
            createRuleEngineTemplate(albatrossAuthToken);
    }


    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyTimesheetsPreferenceTest() {

        Response timesheetPerf = getTimeSheetSettingPreferences(albatrossAuthToken);

        assertThat(timesheetPerf.statusCode(), is(200));

        assertThat(timesheetPerf.jsonPath().getString("meta.message"),
                is("Timesheet setting preference fetched successfully"));
        assertThat(timesheetPerf.jsonPath().getString("meta.responseType.context"),
                is("Request is successful"));
        assertThat(timesheetPerf.jsonPath().getInt("meta.responseType.code"), is(103));
        assertThat(timesheetPerf.jsonPath().getString("meta.status"), is("200"));

        // Assert data section (all fields null)
        assertThat(timesheetPerf.jsonPath().get("data.timesheetFrequency"), nullValue());
        assertThat(timesheetPerf.jsonPath().get("data.timesheetStartDay"), nullValue());
        assertThat(timesheetPerf.jsonPath().get("data.approvers"), nullValue());
        assertThat(timesheetPerf.jsonPath().get("data.enabledBy"), nullValue());
        assertThat(timesheetPerf.jsonPath().get("data.templateId"), nullValue());
    }


    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "testTimesheetSettingsData", groups = {"contract_staffing", "nightly-build"})
    public void verifyTimesheetsSettingPreferenceTest(int jobId, int candidateId, int userId, int timesheetFrequency) {
        Response response = enableTimesheet(candidateId, jobId, ownerAccountID, albatrossAuthToken, timesheetFrequency,
                        200, 0);
        assertThat(response.statusCode(), is(200));

        Response timesheetPerf = getTimeSheetSettingPreferences(albatrossAuthToken);

        assertThat(timesheetPerf.statusCode(), is(200));
        assertThat(timesheetPerf.jsonPath().getString("meta.message"),
                is("Timesheet setting preference fetched successfully"));
        assertThat(timesheetPerf.jsonPath().getString("meta.responseType.context"),
                is("Request is successful"));
        assertThat(timesheetPerf.jsonPath().getList("data.approvers.clientIds"), empty());
        assertThat(timesheetPerf.jsonPath().getList("data.approvers.agencyIds"), not(empty()));

        assertThat(timesheetPerf.jsonPath().getInt("data.timesheetFrequency"), equalTo(timesheetFrequency));
        assertThat(timesheetPerf.jsonPath().getInt("data.timesheetStartDay"), equalTo(1));
        assertThat(timesheetPerf.jsonPath().getInt("data.enabledBy"), equalTo(userId));
        assertThat(timesheetPerf.jsonPath().get("data.templateId"), nullValue());
    }



    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyUnauthorizedUserTimesheetsSettingTest() {
        Response timesheetPerf = getTimeSheetSettingPreferences(albatrossAuthToken+"invalid");
        assertThat(timesheetPerf.statusCode(), is(401));
        assertThat(timesheetPerf.jsonPath().getString("meta.message"), equalTo("Unauthorised access"));
    }


    @DataProvider
    public Object[][] testTimesheetSettingsData() {
        Object[] testData = createContractStaffingTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int candidateId2 = ((Number) testData[2]).intValue();
        int candidateId3 = ((Number) testData[3]).intValue();
        int userId = ((Number) testData[4]).intValue();

        return new Object[][] {
                { jobId, candidateId2, userId, 3 }
        };
    }
}
