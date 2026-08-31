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

public class WorkTimeDetail {
    private Integer id;
    private Integer workStartTime;
    private Integer workEndTime;
    private String rangeBasedRemark;
    private Integer rangeBasedBreakTime;
    private List<BreakInterval> breakIntervals;
}
