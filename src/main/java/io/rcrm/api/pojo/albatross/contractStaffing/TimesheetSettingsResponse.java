package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TimesheetSettingsResponse {
    private int id;
    private long jobStartDate;
    private long jobEndDate;
    private int timesheetFrequency;
    private int timesheetStartDay;
    private List<Integer> workDayIds;
    private Approvers approvers;
    private int payCurrencyId;
    private int billCurrencyId;
    private double billRate;
    private double payRate;
    private int workLogType;
    private boolean calculateBreakTime;
    private List<TemplateWorkDay> templateWorkDays;
    private List<CustomRule> customRules;
    private long updatedOn;
    private int updatedBy;
    private long enabledOn;
    private int enabledBy;
}