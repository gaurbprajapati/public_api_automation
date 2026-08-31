package io.rcrm.api.pojo.albatross.contractStaffing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeLog {
    private Integer id;
    private Integer timesheetId;
    private String timesheetPeriod;
    private List<WorkTimeDetail> workTimeDetails;
    private Integer totalTime;
    private Integer overTime;
}