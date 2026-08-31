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

public class TimeDetails {
    private Integer totalWorkTime;
    private Integer timesheetId;
    private Integer totalOvertime;
    private Integer totalTime;
}