package io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters;

import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.DealFilterTestSupport;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import static io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.TimesheetFilterTestSupport.parseBracketedIntList;

public abstract class ContractorDealFilterTestSupport extends DealFilterTestSupport {

    protected boolean contractorDealsMatchFilter(JSONObject contractor, String filterType, String filterValue) {
        Set<Integer> dealIds = ContractorFilterTestSupport.resolveDealIds(contractor);
        List<Integer> expectedDealIds = parseBracketedIntList(filterValue);

        if ("has_any_value".equals(filterType)) {
            return !dealIds.isEmpty();
        }
        if ("is_empty".equals(filterType)) {
            return dealIds.isEmpty();
        }

        boolean matchesAnyExpected = expectedDealIds.stream().anyMatch(dealIds::contains);

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
