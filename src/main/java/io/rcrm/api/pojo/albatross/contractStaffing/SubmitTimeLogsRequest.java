package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class SubmitTimeLogsRequest {
    private Integer isApproved;
    private Integer save;
    private List<TimeLog> timeLogs;
    private List<TimeDetails> timeDetails;
    private List<Integer> timesheetIdNoLogChanges;
}