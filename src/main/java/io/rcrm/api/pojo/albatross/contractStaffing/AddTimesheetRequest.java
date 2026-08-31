package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class AddTimesheetRequest {
    private List<Integer> contractorIds;
    private List<TimesheetDate> timesheetDates;
}