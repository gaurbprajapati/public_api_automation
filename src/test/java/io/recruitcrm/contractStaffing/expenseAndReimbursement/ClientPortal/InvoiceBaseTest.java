package io.recruitcrm.contractStaffing.expenseAndReimbursement.ClientPortal;

import com.qa.api.util.reaper.*;

import io.rcrm.api.pojo.albatross.contractStaffing.*;
import io.rcrm.api.pojo.invoiceService.TimesheetsInvoiceDataRequest;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerInvoice;
import io.rcrm.api.javafaker.ContractStaffing.JavaFakerReimbursement;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.*;
import org.testng.annotations.*;

import java.util.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public abstract class InvoiceBaseTest extends ClientPortalExpenseReimbursementBaseTest {

    int payBillTemplateId;
    String sfdtContent;
    String invoiceItemsStr;
    int currencyId;
    String invoicePrefix;
    String invoiceNumber;
    JavaFakerInvoice fakerInvoice;
    String albatrossAuthToken;
    String apiAuthToken;
    int accountId;
    int rcrmUserId;
    String rcrmEmailID;
    JavaFakerReimbursement fakerReimbursement;
    final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    @BeforeClass(alwaysRun = true)
    public void expenseAndReimbursementBaseSetup() {
        fakerInvoice = new JavaFakerInvoice();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        accountId = ThreadManager.getAccount().getAccountId();
        createRuleEngineTemplateHourBased(albatrossAuthToken);
        fakerReimbursement = new JavaFakerReimbursement();
        rcrmUserId = ThreadManager.getOwner().getUserId();
        rcrmEmailID = ThreadManager.getOwner().getEmail();
    }
    
    protected Response fetchTimesheetInvoiceData(List<Integer> timesheetIds, int templateId, String authToken) {
        TimesheetsInvoiceDataRequest request = TimesheetsInvoiceDataRequest.builder()
                .timesheetIds(timesheetIds)
                .templateId(templateId)
                .build();
        return postTimesheetsInvoices(request, authToken);
    }

    
    protected void getPayBillTemplateId(String templateName, String authToken) {
        Response payBillTemplateIdResponse = getInvoiceTemplate(authToken);
        assertThat(payBillTemplateIdResponse.statusCode(), is(200));
        if(templateName.equals("Contract Job")) {
            payBillTemplateId = payBillTemplateIdResponse.jsonPath().getInt("data.templates[0].id");
            sfdtContent = payBillTemplateIdResponse.jsonPath().getString("data.templates[0].templateTheme");
        } else if(templateName.contains("Week Days")) {
            payBillTemplateId = payBillTemplateIdResponse.jsonPath().getInt("data.templates[1].id");
            sfdtContent = payBillTemplateIdResponse.jsonPath().getString("data.templates[1].templateTheme");
        }
    }

    public void generateInvoiceItems(List<Integer> timesheetIds, String authToken) {
        Response tsResp = fetchTimesheetInvoiceData(timesheetIds, payBillTemplateId, authToken);
        if (tsResp.getStatusCode() != 200) {
            throw new IllegalStateException("fetchTimesheetInvoiceData failed: HTTP " + tsResp.getStatusCode());
        }
        String invoiceItemsStr = tsResp.jsonPath().getString("data.invoiceItems");
        this.invoiceItemsStr = invoiceItemsStr;
        currencyId = tsResp.jsonPath().getInt("data.currencyId");
    }

    protected List<JSONObject> convertTimesheetInvoiceItemsToSyncFusionRows(String invoiceItemsJson) {
        List<JSONObject> out = new ArrayList<>();
        if (invoiceItemsJson == null || invoiceItemsJson.isEmpty()) {
            return out;
        }
        JSONArray tsRows = new JSONArray(invoiceItemsJson.trim());
        for (int i = 0; i < tsRows.length(); i++) {
            JSONObject row = tsRows.getJSONObject(i);
            JSONArray fields = row.getJSONArray("fields");
            JSONObject fieldsMap = new JSONObject();
            for (int j = 0; j < fields.length(); j++) {
                JSONObject f = fields.getJSONObject(j);
                Object fid = f.opt("fieldId");
                String key = (fid == null || fid == JSONObject.NULL)
                        ? "null"
                        : String.valueOf(((Number) fid).intValue());
                fieldsMap.put(key, f.optString("fieldValue", ""));
            }
            JSONObject item = new JSONObject();
            item.put("fields", fieldsMap);
            out.add(item);
        }
        return out;
    }

    protected List<JSONObject> buildFieldNamesFromTemplateTableFields() {
        List<JSONObject> fieldNames = new ArrayList<>();
        fieldNames.add(new JSONObject().put("field", "8").put("label", "Description").put("type", "text"));
        fieldNames.add(new JSONObject().put("field", "315").put("label", "Regular Hours").put("type", "text"));
        fieldNames.add(new JSONObject().put("field", "316").put("label", "Overtime Hours").put("type", "text"));
        fieldNames.add(new JSONObject().put("field", "317").put("label", "Total Hours").put("type", "text"));
        fieldNames.add(new JSONObject().put("field", "1").put("label", "Amount").put("type", "text"));
        return fieldNames;
    }

    
    protected Response generateInvoiceTableForTimesheetInvoiceData(List<Integer> timesheetIds, int templateId,
            String authToken, int isPayAndBill) {
        List<JSONObject> syncRows = convertTimesheetInvoiceItemsToSyncFusionRows(invoiceItemsStr);
        List<JSONObject> fieldNames = buildFieldNamesFromTemplateTableFields();
        return function.generateInvoiceTableForPayBillRow(syncFunctionURL, authToken, sfdtContent, syncRows, fieldNames,
                isPayAndBill);
    }

    
    protected Response createInvoiceForTimesheetsWithSyncFusion(List<Integer> timesheetIds,
            String authToken, int companyId, int isPayBill, int contactId, double totalAmount) {


        List<JSONObject> syncRows = convertTimesheetInvoiceItemsToSyncFusionRows(invoiceItemsStr);
        List<JSONObject> fieldNames = buildFieldNamesFromTemplateTableFields();

        Response tableResp = function.generateInvoiceTableForPayBillRow(syncFunctionURL, authToken, sfdtContent, syncRows, fieldNames, isPayBill);
        if (tableResp.getStatusCode() != 200) {
            return tableResp;
        }

        String sfdtAfter = tableResp.jsonPath().getString("sfdt");

        JSONObject invoiceSfdtWrap = new JSONObject();
        invoiceSfdtWrap.put("sfdt", sfdtAfter);

        JSONObject associations1 = new JSONObject();
        associations1.put("2", new JSONArray());
        associations1.put("3", new JSONArray());
        associations1.put("4", new JSONArray());
        associations1.put("5", new JSONArray());
        associations1.put("11", new JSONArray());

        JSONObject body = new JSONObject();
        body.put("id", JSONObject.NULL);
        body.put("invoicePrefix", invoicePrefix);
        body.put("invoiceNumber", invoiceNumber);
        body.put("invoiceId", JSONObject.NULL);
        body.put("description", "");
        body.put("templateId", this.payBillTemplateId);
        body.put("companyId", companyId);
        body.put("contactId", contactId);
        body.put("statusId", fakerInvoice.getInvoiceStatusId());
		body.put("currencyId", fakerInvoice.getCurrencyId());
		body.put("paidOn", String.valueOf(fakerInvoice.getPaidOn()));
		body.put("dueDate", String.valueOf(fakerInvoice.getDueDate()));
		body.put("issueDate", String.valueOf(fakerInvoice.getIssueDate()));
        body.put("totalAmount", totalAmount);
        String invoicePdf = generateInvoicePdf(authToken);
        body.put("invoicePdf", invoicePdf);
        body.put("invoiceItems", invoiceItemsStr);
        body.put("invoiceSfdt", invoiceSfdtWrap.toString());
        body.put("timesheetIds", new JSONArray(timesheetIds));
        body.put("isPayBill", isPayBill);
        body.put("placementIds", new JSONArray());
        body.put("autoGenerateInvoiceId", true);
        body.put("associations", associations1);
        body.put("company", new JSONObject().put("companyId", companyId).put("name", fakerInvoice.getCompanyName()));
        body.put("contact", new JSONObject().put("contactId", contactId).put("name", fakerInvoice.getCompanyName()));

        return RestClient.doPost("JSON", invoiceServiceURL, "invoices", authToken, null, true, body.toString());
    }

    public String generateInvoicePdf(String albatrossTkn) {
        Response response1 = function.addBusinessDetails(invoiceServiceURL, albatrossTkn);
        invoicePrefix = response1.jsonPath().getString("data.invoiceIdPrefix");
        invoiceNumber   = response1.jsonPath().get("data.invoiceIdNumber");
        int accountId = response1.jsonPath().get("data.accountId");
        String fileName = invoicePrefix + "-" + String.valueOf(invoiceNumber);
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("fileName", fileName + ".pdf");
        pathParams.put("acl", "private");
        pathParams.put("key", String.valueOf(accountId) + "/invoices/" + fileName + "/" + fileName + ".pdf");
        Response response2 = RestClient.doGet("JSON", invoiceServiceURL, "invoice/files/generate-upload-url", albatrossTkn, pathParams, null, true);
        response2.then().statusCode(200);
        String s3Key = response2.jsonPath().getString("data.key");
        return s3Key;
    }

    @DataProvider(parallel = true)
    public Object[][] buildPortalData() {
        String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath()
                .getString("slug");
        String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath()
                .getString("slug");
        String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath()
                .getString("slug");
        String jobSlug = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath()
                .getString("slug");
        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug);
        int userId = function.getUsers(baseURL, apiAuthToken).jsonPath().getInt("[0].id");
        int candidateId = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug, "candidate")
                .jsonPath().getInt("data.candidate.id");
        JsonPath jobJsonPath = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job").jsonPath();
        int jobId = jobJsonPath.getInt("data.job.id");
        int companyId = jobJsonPath.getInt("data.job.companyid");
        int contactId = jobJsonPath.getInt("data.job.contactid");

        String email = JavaFakerReimbursement.generateFakerEmail();
        allCrudFunctions.updateCustomField("contact", albatrossURL, contactId, albatrossAuthToken, "email", email);

        enableTimesheetHourBased(candidateId, jobId, userId, albatrossAuthToken, 2, 200, 1);
        enableVmsLink(jobId, albatrossAuthToken);
        submitCandidateUrl(jobId, albatrossAuthToken);
        createClientPortalAccountAndLogin(contactId, "John", "Doe", email, accountId, jobId, true, "Example Company", companyId, rcrmEmailID, rcrmUserId);
        String portalToken = PortalThreadManager.getBearerToken();
        
        List<Integer> timesheetIDs = createSingleTimesheetForValidation(jobId, candidateId, 2, albatrossAuthToken);
        int timesheetId = timesheetIDs.get(0);
        int reimbursementId = createReimbursement("1 Lunch outside", 40.00, "test.pdf", timesheetId,
                    albatrossAuthToken);
        Response approve = updateReimbursementStatus(timesheetId, reimbursementId, "approve", null, albatrossAuthToken);
        assertThat(approve.getStatusCode(), is(200));
        Response timeLogsResponse = getTimeSheetTimeLogs(timesheetId, albatrossAuthToken);
        assertThat(timeLogsResponse.statusCode(), is(200));
        JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
        List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");

        List<Map<String, Object>> timeLogsList = generateTimeLogIdsforHourBased(timeLogs, timesheetId);
        TimeDetails timeDetails = generateTimeDetailsForHourBased(timeLogs, timesheetId);

        Map<String, Object> submitTimeLogsRequest = new HashMap<>();
        submitTimeLogsRequest.put("isApproved", 0);
        submitTimeLogsRequest.put("timeLogs", timeLogsList);
        submitTimeLogsRequest.put("timeDetails", Arrays.asList(timeDetails));

        Response submitResponse = submitTimeLogsForTimesheetHourBased(submitTimeLogsRequest, albatrossAuthToken);
        assertThat(submitResponse.statusCode(), is(200));

        int randomApprovalStatus = 4;
        ApproveTimesheetRequest approveRequest = buildApproveTimesheetRequest(randomApprovalStatus,
                "Rejected by automated test");

        Response approveResponse = approveTimesheet(timesheetId, approveRequest, albatrossAuthToken);
        assertThat(approveResponse.statusCode(), is(201));

        getPayBillTemplateId("Contract Job", albatrossAuthToken);
        generateInvoiceItems(Arrays.asList(timesheetId), albatrossAuthToken);
        Response invoiceResponse = createInvoiceForTimesheetsWithSyncFusion(Arrays.asList(timesheetId), albatrossAuthToken, companyId, 1,
                contactId, 40.00);
        assertThat(invoiceResponse.statusCode(), is(200));

        return new Object[][] { { timesheetId, reimbursementId, portalToken } };
    }

    protected int getPayBillTemplateId11(String templateName, String authToken) {
        Response payBillTemplateIdResponse = getInvoiceTemplate(authToken);
        assertThat(payBillTemplateIdResponse.statusCode(), is(200));
        int templateId = -1;
        if(templateName.equals("Contract Job")) {
            templateId = payBillTemplateIdResponse.jsonPath().getInt("data.templates[0].id");
        } else if(templateName.contains("Week Days")) {
            templateId = payBillTemplateIdResponse.jsonPath().getInt("data.templates[1].id");
            
        }
        return templateId;
    }
}