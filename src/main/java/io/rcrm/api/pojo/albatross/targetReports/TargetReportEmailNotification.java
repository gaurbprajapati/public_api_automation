package io.rcrm.api.pojo.albatross.targetReports;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TargetReportEmailNotification {
    private int target_id;
    private String email_notification_status;
}