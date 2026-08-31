package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TimesheetSettingsMetaData {
    private int timesheetSettingId;
    private int timesheetId;
    private int workLogType;
    private List<TimelogsMetaData> timelogsMetaData;
    private Approvers approvers;
}
