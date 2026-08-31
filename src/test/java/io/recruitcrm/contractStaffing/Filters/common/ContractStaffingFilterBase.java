package io.recruitcrm.contractStaffing.Filters.common;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.ContractStaffing.JavaFakerFilter;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.hoursBasedRuleEngineCalculation.RuleEngineCalculationBase;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class ContractStaffingFilterBase extends RuleEngineCalculationBase {

    protected final JavaFakerFilter filterFaker = new JavaFakerFilter();

    private Integer cachedNonExistentEntityId;
    private Integer cachedNonExistentStatusId;
    private final Map<String, String> nonExistentSearchLabelsByPrefix = new HashMap<>();

    protected String albatrossAuthToken;
    protected String apiAuthToken;
    protected commanFunction function;
    protected AllCrudFunctions allCrudFunctions;

    protected void initializeAuthAndFunction() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
    }

    protected JSONArray getFilteredDataFromResponse(String responseBody) {
        return new JSONObject(responseBody).getJSONArray("data");
    }

    protected JSONArray getFilteredData(Response response) {
        return getFilteredDataFromResponse(response.getBody().asString());
    }

    protected Response postTimesheetSearchGet(JSONObject payload) {
        return RestClient.doPost("JSON", timesheetBaseURL, "/timesheets/search/get?page=1&size=100",
                albatrossAuthToken, null, true, payload);
    }

    protected Response postContractorSearchGet(JSONObject payload) {
        return RestClient.doPost("JSON", timesheetBaseURL, "/contractors/search/get?page=1&size=100",
                albatrossAuthToken, null, true, payload);
    }

    protected Map<String, Object> getFilterTestTimesheetConfig() {
        return FilterTestConfigFactory.createDefaultTimesheetConfig();
    }

    protected Map<String, Object> copyTimesheetConfigWithJobDates(long jobStartDate, long jobEndDate) {
        return FilterTestConfigFactory.createTimesheetConfigWithJobDates(jobStartDate, jobEndDate);
    }

    protected String buildBracketedIdFilterValue(Integer... ids) {
        StringBuilder value = new StringBuilder("[");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                value.append(",");
            }
            value.append(ids[i]);
        }
        value.append("]");
        return value.toString();
    }

    protected String buildDropdownFilterBarLabel(String... labels) {
        if (labels.length == 0) {
            return "";
        }
        if (labels.length == 1) {
            return labels[0];
        }
        return "Multiple";
    }

    protected boolean isExclusionFilterType(String filterType) {
        return "is_not".equals(filterType) || "does_not_contain".equals(filterType);
    }

    protected String firstSearchToken(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return text.trim().split("\\s+")[0];
    }

    protected int getNonExistentEntityId() {
        if (cachedNonExistentEntityId == null) {
            cachedNonExistentEntityId = filterFaker.getNonExistentEntityId();
        }
        return cachedNonExistentEntityId;
    }

    protected int getNonExistentStatusId() {
        if (cachedNonExistentStatusId == null) {
            cachedNonExistentStatusId = filterFaker.getNonExistentStatusId();
        }
        return cachedNonExistentStatusId;
    }

    protected String getNonExistentSearchLabel(String prefix) {
        return nonExistentSearchLabelsByPrefix.computeIfAbsent(prefix,
                p -> filterFaker.getNonExistentSearchLabel(p));
    }
}
