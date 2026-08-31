package io.recruitcrm.contractStaffing.timesheetInvoice;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.ContractStaffing.JavaFakerPayBillStatus;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class PayBillStatusTest extends ContractStaffingBaseTest {

    private static final String UPDATE_PAY_BILL_SUCCESS_SCHEMA = "schemaValidation/updatePayBillStatusSuccess.json";

    String albatrossAuthToken;
    String apiAuthToken;
    JavaFakerPayBillStatus payBillStatusFaker;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        payBillStatusFaker = new JavaFakerPayBillStatus();
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void updatepaybill200_paid_test() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 0);
        int timesheetId = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken).get(0);

        int payStatusId = 1;
        String payoutNumber = payBillStatusFaker.getPayoutNumber();
        Long payoutPaidOn = payBillStatusFaker.getPayoutPaidOnCurrent();

        Map<String, Object> payload = createPayBillStatusPayload(
                payBillStatusFaker.getPayBillTypePaid(),
                payStatusId,
                payoutNumber,
                payoutPaidOn
        );

        Response response = updatePayBillStatusWithInvalidData(timesheetId, payload, albatrossAuthToken);

        assertThat(response.statusCode(), is(200));
        assertThat(response.asString(), matchesJsonSchemaInClasspath(UPDATE_PAY_BILL_SUCCESS_SCHEMA));

        Response getResponse = getTimeSheetTimeLogs(timesheetId, albatrossAuthToken);
        assertThat(getResponse.statusCode(), is(200));
        JsonPath getJsonPath = getResponse.jsonPath();

        assertThat(getJsonPath.getString("meta.message"), is("Time logs fetched successfully"));
        assertThat(getJsonPath.getInt("meta.status"), is(200));
        assertThat(getJsonPath.getInt("data.timesheetId"), is(timesheetId));

        Object payStatusIdValue = getJsonPath.get("data.payStatusId");
        assertThat(payStatusIdValue, is(notNullValue()));
        assertThat(((Number) payStatusIdValue).intValue(), is(payStatusId));

        assertThat(getJsonPath.getString("data.payoutNumber"), is(payoutNumber));

        Object payoutPaidOnValue = getJsonPath.get("data.payoutPaidOn");
        assertThat(payoutPaidOnValue, is(notNullValue()));
        assertThat(((Number) payoutPaidOnValue).longValue(), is(payoutPaidOn));
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void updatepaybill200_unpaid_test() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 0);
        int timesheetId = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken).get(0);

        int payStatusId = 2;
        Map<String, Object> payload = new HashMap<>();
        payload.put("payBillType", 1);
        payload.put("payStatusId", payStatusId);
        payload.put("payoutNumber", null);
        payload.put("payoutPaidOn", null);

        Response response = updatePayBillStatusWithInvalidData(timesheetId, payload, albatrossAuthToken);

        assertThat(response.statusCode(), is(200));
        assertThat(response.asString(), matchesJsonSchemaInClasspath(UPDATE_PAY_BILL_SUCCESS_SCHEMA));

        Response getResponse = getTimeSheetTimeLogs(timesheetId, albatrossAuthToken);
        assertThat(getResponse.statusCode(), is(200));
        JsonPath getJsonPath = getResponse.jsonPath();

        assertThat(getJsonPath.getString("meta.message"), is("Time logs fetched successfully"));
        assertThat(getJsonPath.getInt("meta.status"), is(200));
        assertThat(getJsonPath.getInt("data.timesheetId"), is(timesheetId));

        Object payStatusIdValue = getJsonPath.get("data.payStatusId");
        assertThat(payStatusIdValue, is(notNullValue()));
        assertThat(((Number) payStatusIdValue).intValue(), is(payStatusId));

        Object payoutNumberValue = getJsonPath.get("data.payoutNumber");
        assertThat(payoutNumberValue == null
                || (payoutNumberValue instanceof String && ((String) payoutNumberValue).equalsIgnoreCase("null")),
                is(true));

        Object payoutPaidOnValue = getJsonPath.get("data.payoutPaidOn");
        assertThat(payoutPaidOnValue == null
                || (payoutPaidOnValue instanceof String && ((String) payoutPaidOnValue).equalsIgnoreCase("null")),
                is(true));
    }

    //not included in xml (waiting for development)
    @Owner("Yash Rampal")
    @Test
    public void updatepaybill400_invalidPayBillType_test() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();
        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 0);
        int timesheetId = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken).get(0);
        Map<String, Object> payload = createPayBillStatusPayload(
                -1,
                payBillStatusFaker.getPayStatusIdPaid(),
                payBillStatusFaker.getPayoutNumber(),
                payBillStatusFaker.getPayoutPaidOnCurrent()
        );

        Response response = updatePayBillStatusWithInvalidData(timesheetId, payload, albatrossAuthToken);

        assertThat(response.statusCode(), is(400));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("message"), notNullValue());
    }

    //not included in xml (waiting for development)
    @Owner("Yash Rampal")
    @Test
    public void updatepaybill400_invalidPayStatusId_test() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 0);
        int timesheetId = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken).get(0);

        Map<String, Object> payload = createPayBillStatusPayload(
                payBillStatusFaker.getPayBillTypePaid(),
                -1,
                payBillStatusFaker.getPayoutNumber(),
                payBillStatusFaker.getPayoutPaidOnCurrent()
        );

        Response response = updatePayBillStatusWithInvalidData(timesheetId, payload, albatrossAuthToken);

        assertThat(response.statusCode(), is(400));
        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("message"), notNullValue());
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void updatepaybill401_invalidAuth_test() {
        Object[] testData = createSingleCandidateTestData(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken);
        int jobId = ((Number) testData[0]).intValue();
        int candidateId = ((Number) testData[1]).intValue();
        int userId = ((Number) testData[2]).intValue();

        enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 0);
        int timesheetId = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken).get(0);

        Map<String, Object> payload = loadPayBillStatusPayloadFromJson();
        String invalidAuthToken = "invalid_token_12345";
        Response response = updatePayBillStatusWithInvalidData(timesheetId, payload, invalidAuthToken);

        assertThat(response.statusCode(), is(401));
    }

}