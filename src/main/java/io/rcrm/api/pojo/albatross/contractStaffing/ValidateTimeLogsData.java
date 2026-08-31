package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ValidateTimeLogsData {
    private List<ErrorData> errorData;
    private TimesheetSettingsMetaData timesheetSettingsMetaData;
    private List<TimeLogData> timeLogs;
}
