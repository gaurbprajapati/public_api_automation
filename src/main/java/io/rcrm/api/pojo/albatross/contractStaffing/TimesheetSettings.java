package io.rcrm.api.pojo.albatross.contractStaffing;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TimesheetSettings {
    private long jobStartDate;
    private long jobEndDate;
    private int timesheetFrequency;
    private int timesheetStartDay;
    private Approvers approvers;
    private int payCurrencyId;
    private int payRate;
    private int billCurrencyId;
    private int billRate;
    private List<Integer> workDayIds;
    private int workLogType;
    private boolean calculateBreakTime;
    private List<Integer> workTime;
    private List<Integer> workStartTime;
    private List<Integer> workEndTime;
    private Object updatedOn;
    private Object updatedBy;
    private Integer updatedByUserTypeId;
    private Object enabledOn;
    private Object enabledBy;
    private Integer enabledByUserTypeId;
    private List<CustomRule> customRules;
    private int isPreferencesModified;
    private Integer breakTimeThreshold;
    private Integer isRemarkMandatory;
    private int jobId;
    private List<Integer> contractorIds;
    private int isReimbursementEnabled;
    private int isUnplannedHoursPayEnabled;
    private Integer calculateChargeBy;
    private BigDecimal marginPercentage;
    private BigDecimal markupPercentage;
}