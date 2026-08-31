package io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.Filters.common.ContractStaffingFilterBase;
import io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.model.ContractorStatusFilterTestContext;
import io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.model.FilterContractorStatusData;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class ContractorStatusFilterTestSupport extends ContractStaffingFilterBase {

    protected static ContractorStatusFilterTestContext contractorStatusFilterContext;

    protected synchronized void ensureContractorStatusFilterTestData() {
        if (contractorStatusFilterContext != null) {
            return;
        }
        initializeAuthAndFunction();
        contractorStatusFilterContext = buildContractorStatusFilterTestContext();
    }

    private ContractorStatusFilterTestContext buildContractorStatusFilterTestContext() {
        try {
            long ts = System.currentTimeMillis();
            long daySeconds = 86400L;
            long today = System.currentTimeMillis() / 1000L;

            Integer templateId = createContractorStatusRuleTemplate();

            FilterContractorStatusData assigned = createContractorWithTimesheetEnabled(templateId,
                    copyTimesheetConfigWithJobDates(today - (7 * daySeconds), today + (7 * daySeconds)),
                    "ContractorAssignedCo_" + ts, 1, "Assigned");
            Thread.sleep(1500);

            FilterContractorStatusData available = createContractorWithTimesheetEnabled(templateId,
                    copyTimesheetConfigWithJobDates(today - (60 * daySeconds), today - (30 * daySeconds)),
                    "ContractorAvailableCo_" + ts, 0, "Available");
            Thread.sleep(1500);

            return new ContractorStatusFilterTestContext(assigned, available);
        } catch (Exception e) {
            throw new AssertionError("Error creating contractor status filter test data: " + e.getMessage(), e);
        }
    }

    private Integer createContractorStatusRuleTemplate() {
        Map<String, Object> config = getFilterTestTimesheetConfig();
        String templateName = ruleEngineenFake.getTestTemplateName("ContractorStatusFilterTest");
        List<Integer> workDayIds = Arrays.asList(1, 2, 3, 4, 5);
        List<Map<String, Object>> customRules = buildCustomRulesFromDescription(
                (String) config.get("rulesApplied"), workDayIds,
                (Double) config.get("payRate"), (Double) config.get("billRate"), "Shift");
        Integer templateId = createRuleTemplate(albatrossAuthToken, templateName, workDayIds,
                (String) config.get("regularHours"), customRules, (String) config.get("breakBillable"),
                SHIFTS_LOGGING, (Integer) config.get("breakTimeThreshold"));
        assertThat("Template should be created", templateId, notNullValue());
        return templateId;
    }

    private FilterContractorStatusData createContractorWithTimesheetEnabled(Integer templateId,
                                                                            Map<String, Object> config,
                                                                            String companyName,
                                                                            int expectedStatus,
                                                                            String statusLabel) throws Exception {
        JavaFakerCompany faker = new JavaFakerCompany();
        Company company = new Company(companyName, faker.getCompanyWebsite(), faker.getContactNumber(), faker.getLogoURL());
        company.setCity("Mumbai");
        company.setAddress("Contractor status filter automation");
        company.setAbout_company("Company for contractor status filter automation");

        JsonPath jsonCompany = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company)
                .jsonPath();
        String companySlug = jsonCompany.getString("slug");

        String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, companySlug)
                .jsonPath().getString("slug");
        String jobSlug = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug)
                .jsonPath().getString("slug");

        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String candidateSlug = jsonCandidate.getString("slug");
        Integer realCandidateId = getRealCandidateId(albatrossAuthToken, candidateSlug);
        assertThat("Real candidate ID should be fetched", realCandidateId, notNullValue());

        int userId = function.getUsers(baseURL, apiAuthToken).jsonPath().getInt("[0].id");
        int jobId = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job")
                .jsonPath().getInt("data.job.id");

        assignCandidateToJob(apiAuthToken, candidateSlug, jobSlug);
        Thread.sleep(1500);

        Response timesheetResponse = enableWeeklyTimesheetWithDynamicValues(albatrossAuthToken, jobId,
                realCandidateId, userId, templateId, (String) config.get("dayPattern"),
                (String) config.get("regularHours"), (String) config.get("rulesApplied"),
                (Double) config.get("payRate"), (Double) config.get("billRate"),
                (String) config.get("breakBillable"), (Long) config.get("jobStartDate"),
                (Long) config.get("jobEndDate"), (Integer) config.get("timesheetFrequency"),
                (Integer) config.get("timesheetStartDay"), (Integer) config.get("payCurrencyId"),
                (Integer) config.get("billCurrencyId"), (Integer) config.get("breakTimeThreshold"));
        assertThat("Timesheet settings should succeed", timesheetResponse.getStatusCode(), equalTo(200));
        Thread.sleep(1500);

        return new FilterContractorStatusData(realCandidateId, expectedStatus, statusLabel);
    }
}
