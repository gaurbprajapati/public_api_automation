package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TimeLogData {
    private int id;
    private long date;
    private int dayTypeId;
    private Integer workTime;
    private Integer workStartTime;
    private Integer workEndTime;
    private Integer breakTime;
    private String breakIntervals;
    private Integer overTime;
    private String remark;
    private Integer totalTime;
    private int timesheetId;
    private String timesheetPeriod;
}
