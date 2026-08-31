package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class RuleEngineTemplate {
    private String templateName;
    private int workLogType;
    private int calculateBreakTime;
    private Integer breakTimeThreshold;
    private List<Integer> workDayIds;
    private List<Integer> workTime;
    private List<Integer> workStartTime;
    private List<Integer> workEndTime;
    private List<CustomRule> customRules;
}