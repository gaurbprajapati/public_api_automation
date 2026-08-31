package io.recruitcrm.report.pojo.ScheduleReport;

import java.util.ArrayList;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleReportRequest {
    private ScheduleReportDetails schedule_report;
    private ArrayList<Integer> collaborator_team_ids;
    private ArrayList<Integer> collaborator_user_ids;
    private int reportId;
}
