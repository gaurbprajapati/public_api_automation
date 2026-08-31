package io.recruitcrm.report.pojo.ScheduleReport;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleReportDetails {
    private String report_name;
    private String selectedIntervalLabel;
    private long date_time;
    private String selectedEndAfterLabel;
    private int selectedEndAfterType;
    private int selectedRepetitions;
    private String end_date;
    private String selectedFileTypeLabel;
    private String subject;
    private String body;
}
