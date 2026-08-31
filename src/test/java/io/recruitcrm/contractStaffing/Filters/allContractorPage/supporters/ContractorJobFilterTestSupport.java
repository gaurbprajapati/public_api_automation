package io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters;

import io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters.model.ContractorJobFilterTestContext;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.JobFilterTestSupport;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.FilterJobData;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.JobFilterTestContext;
import io.restassured.path.json.JsonPath;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.TimesheetFilterTestSupport.parseBracketedIntList;

public abstract class ContractorJobFilterTestSupport extends JobFilterTestSupport {

    protected static ContractorJobFilterTestContext contractorJobFilterContext;

    protected synchronized void ensureContractorJobFilterTestData() {
        if (contractorJobFilterContext != null) {
            return;
        }
        initializeAuthAndFunction();
        long today = System.currentTimeMillis() / 1000L;
        long daySeconds = 86400L;
        Map<String, Object> activeAssignmentConfig = copyTimesheetConfigWithJobDates(
                today - (7 * daySeconds), today + (7 * daySeconds));
        Map<String, Object> availableContractorConfig = copyTimesheetConfigWithJobDates(
                today - (60 * daySeconds), today - (30 * daySeconds));
        contractorJobFilterContext = buildContractorJobFilterTestContext(
                activeAssignmentConfig, availableContractorConfig);
    }

    private ContractorJobFilterTestContext buildContractorJobFilterTestContext(
            Map<String, Object> assignedConfig, Map<String, Object> availableConfig) {
        try {
            long ts = System.currentTimeMillis();
            Integer templateId = createJobFilterRuleTemplate();

            JsonPath sharedCompany = createJobFilterSharedCompany("FilterJobCo_" + ts, "Mumbai");
            String companySlug = sharedCompany.getString("slug");
            String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, companySlug)
                    .jsonPath().getString("slug");

            FilterJobData jobA = createJobFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "FilterJob_Alpha_" + ts, "Mumbai", assignedConfig);
            Thread.sleep(1500);
            FilterJobData jobB = createJobFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "Filter Job Beta " + ts, "Delhi", assignedConfig);
            Thread.sleep(1500);
            FilterJobData jobC = createJobFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "FilterJob_Gamma_" + ts, "Bangalore", assignedConfig);
            Thread.sleep(1500);

            FilterJobData availableJob = createJobFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "FilterJob_Available_" + ts, "Pune", availableConfig);
            Thread.sleep(1500);

            return new ContractorJobFilterTestContext(jobA, jobB, jobC, availableJob.candidateId);
        } catch (Exception e) {
            throw new AssertionError("Error creating contractor job filter test data: " + e.getMessage(), e);
        }
    }

    @Override
    protected JobFilterTestContext activeJobFilterContext() {
        if (contractorJobFilterContext == null) {
            return super.activeJobFilterContext();
        }
        return new JobFilterTestContext(
                contractorJobFilterContext.jobA,
                contractorJobFilterContext.jobB,
                contractorJobFilterContext.jobC,
                0,
                contractorJobFilterContext.availableCandidateId);
    }

    @Override
    protected Map<Integer, String> buildJobIdToNameMap() {
        ContractorJobFilterTestContext ctx = contractorJobFilterContext;
        Map<Integer, String> idToName = new HashMap<>();
        idToName.put(ctx.jobA.id, ctx.jobA.name);
        idToName.put(ctx.jobB.id, ctx.jobB.name);
        idToName.put(ctx.jobC.id, ctx.jobC.name);
        return idToName;
    }

    protected boolean contractorAssignedJobsMatchFilter(JSONObject contractor, String filterType, String filterValue) {
        Set<Integer> assignedJobIds = ContractorFilterTestSupport.resolveAssignedJobIds(contractor);
        List<Integer> expectedJobIds = parseBracketedIntList(filterValue);

        if ("has_any_value".equals(filterType)) {
            return !assignedJobIds.isEmpty();
        }
        if ("is_empty".equals(filterType)) {
            return ContractorFilterTestSupport.resolveContractorStatus(contractor) == 0;
        }

        boolean matchesAnyExpected = expectedJobIds.stream().anyMatch(assignedJobIds::contains);

        switch (filterType) {
            case "is":
            case "contains_at_least_one":
                return matchesAnyExpected;
            case "is_not":
            case "does_not_contain":
                return !matchesAnyExpected;
            default:
                throw new IllegalArgumentException("Unsupported filter type: " + filterType);
        }
    }
}
