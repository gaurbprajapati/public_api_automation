package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.contractStaffing.*;
import io.rcrm.api.javafaker.ContractStaffing.*;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.io.File;
import java.nio.file.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class CreateReimbursementTest extends ExpenseAndReimbursementBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;
    private JavaFakerReimbursement fakerReimbursement;
    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
        fakerReimbursement = new JavaFakerReimbursement();
    }

    @Test(dataProvider = "addTimeSheetData")
    public void createReimbursement_Test(int jobId, int candidateId, int userId, int timesheetID , String documentToken, String documentFileName) {

        CreateReimbursementRequest createReimbursementRequest = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                .documentToken(documentToken)
                .fileName(documentFileName)
                .build();
        Response createReimbursementResponse = createReimbursement(timesheetID, createReimbursementRequest,
                albatrossAuthToken);
        assertThat(createReimbursementResponse.statusCode(), is(201));
        JsonPath createReimbursementJsonPath = createReimbursementResponse.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), is("Reimbursement created successfully"));
        assertThat(createReimbursementJsonPath.getInt("data.id"), greaterThan(0));
        assertThat(createReimbursementJsonPath.getInt("data.status"), is(1));
        assertThat(createReimbursementJsonPath.getDouble("data.amount"), is(40.00));
        assertThat(createReimbursementJsonPath.getInt("data.isPayable"), is(0));
        assertThat(createReimbursementJsonPath.getInt("data.isBillable"), is(0));
        assertThat(createReimbursementJsonPath.get("data.addedOn"), notNullValue());
    }

    @Test(dataProvider = "addTimeSheetData")
    public void createReimbursementWithInvalidTkn(int jobId, int candidateId, int userId, int timesheetID , String documentToken, String documentFileName) {

        CreateReimbursementRequest createReimbursementRequest = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                .documentToken(documentToken)
                .fileName(documentFileName)
                .build();
        Response createReimbursementResponse = createReimbursement(timesheetID, createReimbursementRequest,
                albatrossAuthToken + "123");
        assertThat(createReimbursementResponse.statusCode(), is(401));
        JsonPath createReimbursementJsonPath = createReimbursementResponse.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), is("Unauthorised access"));
    }

    @Test(dataProvider = "addTimeSheetData")
    public void createReimbursementWithBlankDescription(int jobId, int candidateId, int userId, int timesheetID , String documentToken, String documentFileName) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("")
                .amount(40.00)
                .documentToken(documentToken)
                .fileName(documentFileName)
                .build();
        Response response = createReimbursement(timesheetID, request, albatrossAuthToken);
        assertThat(response.statusCode(), is(400));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), is("Description cannot be blank"));
    }

    @Test(dataProvider = "addTimeSheetData")
    public void createReimbursementWithBlankAmount(int jobId, int candidateId, int userId, int timesheetID , String documentToken, String documentFileName) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(0.00)
                .documentToken(documentToken)
                .fileName(documentFileName)
                .build();
        Response response = createReimbursement(timesheetID, request, albatrossAuthToken);
        assertThat(response.statusCode(), is(400));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), is("Amount must be greater than 0"));
    }

    @Test
    public void createReimbursementWithInvalidTimesheetId() {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                .build();
        int invalidTimesheetId = fakerReimbursement.generateFakerId();
        Response response = createReimbursement(invalidTimesheetId, request, albatrossAuthToken);
        assertThat(response.statusCode(), is(404));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(createReimbursementJsonPath.getString("errors[0].message"), is("Timesheet id " + invalidTimesheetId + " not found."));
    }

    @Test(dataProvider = "addTimeSheetData")
    public void createReimbursementEmptyBody(int jobId, int candidateId, int userId, int timesheetID , String documentToken, String documentFileName) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder().build();
        Response response = createReimbursement(timesheetID, request, albatrossAuthToken);
        assertThat(response.statusCode(), is(400));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), anyOf(is("Description cannot be blank"), is("Amount must be greater than 0")));
    }

    @Test(dataProvider = "addTimeSheetDataWithoutReimbursement")
    public void createReimbursementWithReimbursementDisabled(int jobId, int candidateId, int userId, int timesheetID) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                .build();
        Response response = createReimbursement(timesheetID, request, albatrossAuthToken);

        assertThat(response.statusCode(), is(400));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(createReimbursementJsonPath.getString("errors[0].message"), is("Reimbursements are not enabled for this timesheet"));
    }

    @Test(dataProvider = "addTimeSheetDataWith10Reimbursements")
    public void verifyLimitExceededForReimbursements(int jobId, int candidateId, int userId, int timesheetID) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("1 Lunch outside")
                .amount(40.00)
                .build();
        Response response = createReimbursement(timesheetID, request, albatrossAuthToken);
        assertThat(response.statusCode(), is(400));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(createReimbursementJsonPath.getString("errors[0].message"), is("Maximum of 10 reimbursements allowed per timesheet"));
    }

    @Test(dataProvider = "buildAgencyData")
    public void createReimbursementWithInvoiceLinkedId(int timesheetId, int reimbursementId, String albatrossAuthToken) {
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site")
                .amount(40.00)
                .documentToken("receipts/xyz-v2.pdf")
                .fileName("ticket-v2.pdf")
                .build();
        Response response = createReimbursement(timesheetId, request, albatrossAuthToken);
        assertThat(response.statusCode(), is(409));
        JsonPath createReimbursementJsonPath = response.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(createReimbursementJsonPath.getString("errors[0].message"), is("Reimbursement cannot be modified while an invoice is linked to this timesheet"));

    }

    @DataProvider(parallel = true)
    public Object[][] addTimeSheetData() {
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
        Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates, albatrossAuthToken);
        assertThat(addTimesheetResponse.statusCode(), is(200));

        Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, albatrossAuthToken);
        assertThat(getAllTimesheetsResponse.statusCode(), is(200));
        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
        List<Map<String, Object>> timesheets = getAllTimesheetsJsonPath.getList("data");
        int timesheetID = 0;
        if (!timesheets.isEmpty()) {
            timesheetID = ((Integer) timesheets.get(0).get("id")).intValue();
        }

        String reimbursementTestImagePath = "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg";
        Path path = Paths.get(reimbursementTestImagePath);
		File file = new File(path.toString());
		String fileName = path.getFileName().toString();
        Response uploadReimbursementDocumentResponse = uploadReimbursementDocument(fileName, timesheetID, albatrossAuthToken);
        assertThat(uploadReimbursementDocumentResponse.statusCode(), is(200));
        JsonPath uploadReimbursementDocumentJsonPath = uploadReimbursementDocumentResponse.jsonPath();
        String documentToken = uploadReimbursementDocumentJsonPath.getString("data.documentToken");
        String documentFileName = uploadReimbursementDocumentJsonPath.getString("data.documentFileName");

        return new Object[][]{
                {jobId, candidateId, userId, timesheetID, documentToken, documentFileName},
        };
    }

    @DataProvider(parallel = true)
    public Object[][] addTimeSheetDataWithoutReimbursement() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 0);
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
        int timesheetID = 0;
        if (!timesheets.isEmpty()) {
            timesheetID = ((Integer) timesheets.get(0).get("id")).intValue();
        }
        return new Object[][]{
                {jobId, candidateId, userId, timesheetID},
        };
    }

    @DataProvider(parallel = true)
    public Object[][] addTimeSheetDataWith10Reimbursements() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        for (int i = 0; i < 10; i++) {
            createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetIDs.get(0), albatrossAuthToken);
        }
        return new Object[][]{
                {jobId, candidateId, userId, timesheetIDs.get(0)},
        };
    }
}
