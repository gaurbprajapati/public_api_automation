package io.recruitcrm.contractStaffing.expenseAndReimbursement.ClientPortal;

import io.rcrm.api.pojo.albatross.contractStaffing.CreateReimbursementRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;

import org.testng.annotations.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNull;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class CreateReimbursementInClientPortalTest extends ClientPortalExpenseReimbursementBaseTest {

    @Test(dataProvider = "addTimeSheetData")
    public void createReimbursementWithClientTkn(int timesheetID, String portalToken) {
        CreateReimbursementRequest createReimbursementRequest = CreateReimbursementRequest.builder()
                .description(fakerReimbursement.generateFakerDescription())
                .amount(40.00)
                .build();
        Response createReimbursementResponse = createReimbursement(timesheetID, createReimbursementRequest, portalToken);
        assertThat(createReimbursementResponse.getStatusCode(), is(403));
        JsonPath createReimbursementJsonPath = createReimbursementResponse.jsonPath();
        assertThat(createReimbursementJsonPath.getString("meta.message"), nullValue());
        assertThat(createReimbursementJsonPath.getString("errors[0].message"), is("Only agency and contractor users can create reimbursements"));
        assertNull(createReimbursementJsonPath.get("data"));
    }   

    public Response createReimbursement(int timesheetID, CreateReimbursementRequest createReimbursementRequest, String authToken) {
        return RestClient.doPostOnce("JSON", timesheetBaseURL, "timesheets/" + timesheetID + "/reimbursements", authToken, null, true, createReimbursementRequest);
    }

    @DataProvider(parallel = true)
    public Object[][] addTimeSheetData() {
        Object[] row = buildPortalData(1, false);
        int timesheetId = (Integer) row[3];
        String portalToken = (String) row[5];
        return new Object[][] { { timesheetId, portalToken } };
    }
}
