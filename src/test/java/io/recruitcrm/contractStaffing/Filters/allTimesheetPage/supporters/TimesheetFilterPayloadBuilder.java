package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TimesheetFilterPayloadBuilder {

    private static final String GROUP_TYPE = "timesheets";
    private static final String JOIN_OPERATOR = "AND";

    private final List<JSONObject> filters = new ArrayList<>();

    public TimesheetFilterPayloadBuilder addFilter(String filterType,
                                                   String filterValue,
                                                   String dbField,
                                                   String filterName,
                                                   String fieldType) {
        filters.add(new JSONObject()
                .put("groupType", GROUP_TYPE)
                .put("filterName", filterName)
                .put("dbField", dbField)
                .put("filterValue", filterValue)
                .put("filterType", filterType)
                .put("fieldType", fieldType)
                .put("filterBarLabel", resolveFilterBarLabel(filterValue, dbField))
                .put("isCrossEntity", false));
        return this;
    }

    public TimesheetFilterPayloadBuilder addTimesheetStatus(String filterType, String filterValue) {
        return addFilter(filterType, filterValue, "timesheetStatusId", "Timesheet Status", "dropdown");
    }

    public TimesheetFilterPayloadBuilder addCompanyName(String filterType, String filterValue, String filterBarLabel) {
        return addNamedFilter("Company Name", "companyName", filterType, filterValue, "dropdown", filterBarLabel);
    }

    public TimesheetFilterPayloadBuilder addJobName(String filterType, String filterValue, String filterBarLabel) {
        return addNamedFilter("Job Name", "jobName", filterType, filterValue, "dropdown", filterBarLabel);
    }

    public TimesheetFilterPayloadBuilder addDealName(String filterType, String filterValue, String filterBarLabel) {
        return addNamedFilter("Deal Name", "dealName", filterType, filterValue, "multiselect", filterBarLabel);
    }

    public TimesheetFilterPayloadBuilder addTimesheetPeriod(String filterType, Object filterValue,
                                                            String filterBarLabel) {
        filters.add(new JSONObject()
                .put("groupType", GROUP_TYPE)
                .put("filterName", "Timesheet Period")
                .put("dbField", "timesheetPeriod")
                .put("filterValue", filterValue)
                .put("filterType", filterType)
                .put("fieldType", "date")
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
                                        .put("filters", new JSONArray(filters)))));
    }

    public JSONObject buildForTimesheetListPage() {
        return build()
                .put("timesheetIds", JSONObject.NULL)
                .put("isSubmitted", false);
    }

    private TimesheetFilterPayloadBuilder addNamedFilter(String filterName, String dbField, String filterType,
                                                         String filterValue, String fieldType, String filterBarLabel) {
        filters.add(new JSONObject()
                .put("groupType", GROUP_TYPE)
                .put("filterName", filterName)
                .put("dbField", dbField)
                .put("filterValue", filterValue)
                .put("filterType", filterType)
                .put("fieldType", fieldType)
                .put("filterBarLabel", filterBarLabel == null ? "" : filterBarLabel)
                .put("isCrossEntity", false));
        return this;
    }

    private String resolveFilterBarLabel(String filterValue, String dbField) {
        if (filterValue == null || filterValue.isEmpty()) {
            return "";
        }
        if ("timesheetStatusId".equals(dbField)) {
            if (filterValue.contains("1")) {
                return "Open";
            }
            if (filterValue.contains("2")) {
                return "Submitted";
            }
            if (filterValue.contains("3")) {
                return "Rejected";
            }
            if (filterValue.contains("4")) {
                return "Approved";
            }
            return "Multiple";
        }
        return "";
    }
}
