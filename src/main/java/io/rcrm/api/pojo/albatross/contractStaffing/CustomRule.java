package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class CustomRule {
    private int id;
    private String ruleName;
    private List<Integer> workDayId;
    private int ruleType;
    private int chargeMethod;
    private int startTime;
    private int endTime;
    private int startDuration;
    private int endDuration;
    private int dailyThreshold;
    private int weeklyThreshold;
    private int payRateMultiplier;
    private int billRateMultiplier;
    private int payRatePerHour;
    private int billRatePerHour;
}