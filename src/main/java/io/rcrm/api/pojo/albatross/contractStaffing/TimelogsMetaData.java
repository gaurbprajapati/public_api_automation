package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TimelogsMetaData {
    private int timesheetId;
    private boolean calculateBreakTime;
    private Integer breakTimeThreshold;
    private List<TemplateWorkDay> templateWorkDays;
}
