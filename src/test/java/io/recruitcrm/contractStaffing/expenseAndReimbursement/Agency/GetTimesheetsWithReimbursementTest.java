package io.recruitcrm.contractStaffing.expenseAndReimbursement.Agency;

import com.qa.api.util.TestUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.*;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class GetTimesheetsWithReimbursementTest extends ContractStaffingBaseTest {

    private String albatrossAuthToken;
    private String apiAuthToken;
    private int jobId;
    private int contractorId;
    private int userId;
    private int timesheetId;
    private static final int EXPECTED_REIMBURSEMENT_COUNT = 3;

    @BeforeClass
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplate(albatrossAuthToken);

        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        jobId = (Integer) testData[0];
        contractorId = (Integer) testData[1];
        userId = (Integer) testData[2];

        enableTimesheet(contractorId, jobId, userId, albatrossAuthToken, 2, 200,1);

        List<Integer> timesheetIds = createSingleTimesheetForValidation(jobId, contractorId, 2, albatrossAuthToken);
        assertThat("Expected a created timesheet id", timesheetIds, not(empty()));
        timesheetId = timesheetIds.get(0);

        for (int i = 0; i < EXPECTED_REIMBURSEMENT_COUNT; i++) {
            createReimbursement("Reimbursement line " + i, 10.0 + i, "test.pdf", timesheetId, albatrossAuthToken);
        }
    }

    @Test
    public void getTimesheetWithReimbursement_Test() {
        Response listResponse = fetchTimesheetsJobContractorGet(albatrossAuthToken, jobId, contractorId, 1, 100);
        assertTimesheetListResponseWithReimbursementCount(listResponse, timesheetId, EXPECTED_REIMBURSEMENT_COUNT);
    }

    @Test
    public void getTimesheetWithMissingJobId_Test() {
        Response response = fetchTimesheetsJobContractorGetWithoutJobId(albatrossAuthToken, contractorId, 1, 100);
        assertThat(response.getStatusCode(), is(400));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("error"), is("Bad Request"));

    }

    private Response executePostWithQueryParams(String endpoint, String authToken,
            Map<String, String> queryParams, Object payload) {
        Object requestPayload = payload;
        if (payload instanceof Map) {
            requestPayload = TestUtil.getSerializedJSON(payload);
        }
        return RestClient.doPost("JSON", timesheetBaseURL, endpoint, authToken, queryParams, true, requestPayload);
    }

    private Response fetchTimesheetsJobContractorGet(String authToken, int jobId, int contractorId, int page, int size) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("jobId", String.valueOf(jobId));
        queryParams.put("contractorId", String.valueOf(contractorId));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> body = new HashMap<>();
        body.put("sortPriorityList", new ArrayList<>());

        return executePostWithQueryParams("timesheets/job/contractor/get", authToken, queryParams, body);
    }

    
    private Response fetchTimesheetsJobContractorGetWithoutJobId(String authToken, int contractorId, int page,
            int size) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("contractorId", String.valueOf(contractorId));
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> body = new HashMap<>();
        body.put("sortPriorityList", new ArrayList<>());

        return executePostWithQueryParams("timesheets/job/contractor/get", authToken, queryParams, body);
    }

    private void assertTimesheetListResponseWithReimbursementCount(Response response, int expectedTimesheetId,
            int expectedReimbursementCount) {
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), containsString("Timesheets fetched successfully"));
        assertThat(jp.getInt("meta.status"), is(200));

        Object responseType = jp.get("meta.responseType");
        if (responseType instanceof String) {
            assertThat((String) responseType, is("SUCCESS"));
        }

        List<Map<String, Object>> data = jp.getList("data");
        assertThat(data, notNullValue());
        Optional<Map<String, Object>> row = data.stream()
                .filter(ts -> expectedTimesheetId == ((Number) ts.get("id")).intValue())
                .findFirst();
        assertThat("Timesheet should appear in list", row.isPresent(), is(true));
        assertThat(row.get().containsKey("reimbursementCount"), is(true));
        int count = ((Number) row.get().get("reimbursementCount")).intValue();
        assertThat(count, is(expectedReimbursementCount));
    }
}
