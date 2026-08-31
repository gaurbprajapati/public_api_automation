package io.rcrm.api.pojo.albatross.deal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDeal {
    
    private Deal deal;
    private Object[] selectedcandidates;
    private Object[] selectedcompanies;
    private Object[] selectedcontacts;
    private Object[] selectedjobs;
    private CollaboratorData[] collaboratorData;
    private SelectedOwner selectedOwner;
    private SelectedDealType selectedDealType;
    private SelectedDealStage selectedDealStage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Deal {
        private Integer id;
        private String name;
        private Integer dealstage;
        private String dealvalue;
        private Long closedate;
        private String slug;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelectedOwner {
        private Integer id;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelectedDealType {
        private Integer id;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelectedDealStage {
        private Integer id;
        private String percentage;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollaboratorData {
        private Integer id;
        private Integer type;
    }
}
