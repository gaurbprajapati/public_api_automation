package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.pojo.albatross.contractStaffing.TimesheetDate;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * POST {@code /v1/reimbursements/documents} — presigned upload URL for reimbursement receipts.
 */
@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class UploadReimbursementDocumentTest extends ExpenseAndReimbursementBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
    }

    @Test(dataProvider = "reimbursementEnabledTimesheetData")
    public void uploadReimbursementDocument_Test(int timesheetId) {
        String fileName = "receipt.pdf";
        Response response = uploadReimbursementDocument(fileName, timesheetId, albatrossAuthToken);

        assertThat(response.statusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Upload URL generated successfully"));
        assertThat(jp.getInt("meta.status"), is(200));
        assertThat(jp.getString("meta.requestUuid"), notNullValue());
        assertThat(jp.getInt("meta.responseType.code"), is(103));

        assertThat(jp.getString("data.documentToken"), notNullValue());
        assertThat(jp.getString("data.documentToken").length(), greaterThan(0));
        assertThat(jp.getString("data.documentFileName"), is(fileName));
        assertThat(jp.getString("data.presignedUploadUrl"), notNullValue());
        assertThat(jp.getString("data.presignedUploadUrl"), startsWith("https://"));
        assertThat(jp.getInt("data.expiresInMinutes"), is(5));
    }

    @Test(dataProvider = "reimbursementEnabledTimesheetData")
    public void uploadReimbursementDocumentWithBlankFileName_Test(int timesheetId) {
        Response response = uploadReimbursementDocument("", timesheetId, albatrossAuthToken);

        assertThat(response.statusCode(), is(400));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("fileName: must not be blank"));
        assertThat(jp.getString("meta.responseType"), is("ERROR"));
        assertThat(jp.getInt("meta.status"), is(400));
        assertThat(jp.get("data"), nullValue());
    }

    @Test
    public void uploadReimbursementDocumentWithInvalidTimesheetId_Test() {
        int invalidTimesheetId = JavaFakerReimbursement.generateFakerId();
        Response response = uploadReimbursementDocument("receipt.pdf", invalidTimesheetId, albatrossAuthToken);

        assertThat(response.statusCode(), is(404));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), is("Timesheet not found"));
        assertThat(jp.getString("meta.responseType"), is("ERROR"));
        assertThat(jp.getInt("meta.status"), is(404));
        assertThat(jp.get("data"), nullValue());
    }

    
    @DataProvider(parallel = true)
    public Object[][] reimbursementEnabledTimesheetData() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, 2,
                albatrossAuthToken, "1751328000", "1759017600");
        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");
        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, 2);
        Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates,
                albatrossAuthToken);
        assertThat(addTimesheetResponse.statusCode(), is(200));

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);
        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
        List<Map<String, Object>> timesheets = getAllTimesheetsJsonPath.getList("data");
        int timesheetId = 0;
        if (!timesheets.isEmpty()) {
            timesheetId = ((Integer) timesheets.get(0).get("id")).intValue();
        }

        return new Object[][] { { timesheetId } };
    }
}
