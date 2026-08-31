package io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class ContractorFilterTestSupport {

    private ContractorFilterTestSupport() {
    }

    public static List<Integer> extractContractorIds(JSONArray data) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            ids.add(data.getJSONObject(i).optInt("id", -1));
        }
        return ids;
    }

    public static void assertContractorPresent(JSONArray data, Integer contractorId, String testId) {
        if (contractorId == null) {
            return;
        }
        assertThat(testId + ": Expected contractor " + contractorId + " in filter results",
                extractContractorIds(data), hasItem(contractorId));
    }

    public static void assertContractorAbsent(JSONArray data, Integer contractorId, String testId) {
        if (contractorId == null) {
            return;
        }
        assertThat(testId + ": Contractor " + contractorId + " should be excluded from filter results",
                extractContractorIds(data), not(hasItem(contractorId)));
    }

    public static int resolveContractorStatus(JSONObject contractor) {
        return contractor.optInt("status", -1);
    }

    public static Set<Integer> resolveAssignedJobIds(JSONObject contractor) {
        Set<Integer> jobIds = new HashSet<>();
        JSONArray assignedJobs = contractor.optJSONArray("assignedJobs");
        if (assignedJobs == null) {
            return jobIds;
        }
        for (int i = 0; i < assignedJobs.length(); i++) {
            JSONObject job = assignedJobs.getJSONObject(i);
            if (!job.isNull("id")) {
                jobIds.add(job.optInt("id"));
            } else if (!job.isNull("jobId")) {
                jobIds.add(job.optInt("jobId"));
            }
        }
        return jobIds;
    }

    public static boolean hasEmptyAssignedJobs(JSONObject contractor) {
        JSONArray assignedJobs = contractor.optJSONArray("assignedJobs");
        return assignedJobs == null || assignedJobs.length() == 0;
    }

    public static Set<Integer> resolveDealIds(JSONObject contractor) {
        Set<Integer> dealIds = new HashSet<>();
        JSONArray deals = contractor.optJSONArray("deals");
        if (deals == null) {
            return dealIds;
        }
        for (int i = 0; i < deals.length(); i++) {
            JSONObject deal = deals.getJSONObject(i);
            if (!deal.isNull("dealId")) {
                dealIds.add(deal.optInt("dealId"));
            } else if (!deal.isNull("id")) {
                dealIds.add(deal.optInt("id"));
            }
        }
        return dealIds;
    }

    public static boolean hasEmptyDeals(JSONObject contractor) {
        JSONArray deals = contractor.optJSONArray("deals");
        return deals == null || deals.length() == 0;
    }
}
