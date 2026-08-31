package io.recruitcrm.contractStaffing.Filters.allContractorPage.supporters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContractorFilterPayloadBuilder {

    private static final String GROUP_TYPE = "contractors";
    private static final String JOIN_OPERATOR = "AND";

    private final List<JSONObject> filters = new ArrayList<>();

    public ContractorFilterPayloadBuilder addStatus(String filterValue, String filterBarLabel) {
        filters.add(new JSONObject()
                .put("groupType", GROUP_TYPE)
                .put("filterName", "Status")
                .put("dbField", "status")
                .put("filterValue", filterValue)
                .put("filterType", "is")
                .put("fieldType", "dropdown")
                .put("filterBarLabel", filterBarLabel == null ? "" : filterBarLabel)
                .put("isCrossEntity", false));
        return this;
    }

    public ContractorFilterPayloadBuilder addJobName(String filterType, String filterValue, String filterBarLabel) {
        filters.add(new JSONObject()
                .put("groupType", GROUP_TYPE)
                .put("filterName", "Job Name")
                .put("dbField", "jobName")
                .put("filterValue", filterValue)
                .put("filterType", filterType)
                .put("fieldType", "multiselect")
                .put("filterBarLabel", filterBarLabel == null ? "" : filterBarLabel)
                .put("isCrossEntity", false));
        return this;
    }

    public ContractorFilterPayloadBuilder addDealName(String filterType, String filterValue, String filterBarLabel) {
        filters.add(new JSONObject()
                .put("groupType", GROUP_TYPE)
                .put("filterName", "Deal Name")
                .put("dbField", "dealName")
                .put("filterValue", filterValue)
                .put("filterType", filterType)
                .put("fieldType", "multiselect")
                .put("filterBarLabel", filterBarLabel == null ? "" : filterBarLabel)
                .put("isCrossEntity", false));
        return this;
    }

    public JSONObject build() {
        return new JSONObject()
                .put("advancedSearchContext", JSONObject.NULL)
                .put("defaultFilterList", JSONObject.NULL)
                .put("booleanSearchList", JSONObject.NULL)
                .put("sortPriorityList", new JSONArray())
                .put("filterSearchList", new JSONObject()
                        .put("groupJoinOperator", JOIN_OPERATOR)
                        .put("groupFilterList", new JSONArray()
                                .put(new JSONObject()
                                        .put("groupFilterJoinOperator", JOIN_OPERATOR)
                                        .put("filters", new JSONArray(filters))
                                )
                        )
                );
    }
}
