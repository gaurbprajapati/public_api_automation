package io.recruitcrm.contractStaffing.expenseAndReimbursement.ClientPortal;

import io.rcrm.api.pojo.albatross.contractStaffing.CreateReimbursementRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.nullValue;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class UpdateReimbursementInClientPortalTest extends ClientPortalExpenseReimbursementBaseTest {

    @Test(dataProvider = "addPortalTimesheetIdReimbursementIdTokenData")
    public void updateReimbursementWithClientTkn(int timesheetID, int reimbursementID, String portalToken) {
        String reimbursementTestImagePath = "/src/main/java/io/rcrm/api/testdata/JobsBanner.jpg";
        Path path = Paths.get(reimbursementTestImagePath);
		File file = new File(path.toString());
		String fileName = path.getFileName().toString();
        Response uploadResponse = uploadReimbursementDocument(fileName, timesheetID, albatrossAuthToken);
        assertThat(uploadResponse.getStatusCode(), is(200));
        JsonPath uploadJson = uploadResponse.jsonPath();
        CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                .description("Train ticket to client site (updated)")
                .amount(48.00)
                .documentToken(uploadJson.getString("data.documentToken"))
                .fileName(uploadJson.getString("data.documentFileName"))
                .build();
        Response response = updateReimbursement(timesheetID, reimbursementID, request, portalToken);
        assertThat(response.getStatusCode(), is(403));
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("meta.message"), nullValue());
        assertThat(jp.getString("errors[0].message"), is("Only agency and contractor users can update reimbursements"));
        assertNull(jp.get("data"));
    }

    public Response updateReimbursement(int timesheetId, int reimbursementId, CreateReimbursementRequest request,
            String authToken) {
        return RestClient.doPatchOnce("JSON", timesheetBaseURL,
                "timesheets/" + timesheetId + "/reimbursements/" + reimbursementId, authToken, null, true, request);
    }
}
