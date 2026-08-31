package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class FreeSlotsRequest {
    private List<Integer> contractorIds;
    private long startDate;
    private long endDate;
    private int timesheetFrequencyId;
    private int timesheetStartDay;

}