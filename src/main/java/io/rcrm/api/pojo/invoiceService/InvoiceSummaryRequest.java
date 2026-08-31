package io.rcrm.api.pojo.invoiceService;

import java.util.List;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceSummaryRequest {

    private Object advancedSearchContext;
    private DefaultFilterListWrapper defaultFilterList;
    private Object filterSearchList;
    private Object booleanSearchList;
    private List<Object> sortPriorityList;
    private GroupByFields groupByFields;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DefaultFilterListWrapper {
        private DefaultFilterList defaultFilterList;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DefaultFilterList {
        private List<Filter> filters;
        private String subGroupJoinOperator;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Filter {
        private String groupType;
        private String dbField;
        private String filterValue;
        private String filterType;
        private String fieldType;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupByFields {
        private List<String> groupKey;
        private List<String> groupColumns;
    }
}
