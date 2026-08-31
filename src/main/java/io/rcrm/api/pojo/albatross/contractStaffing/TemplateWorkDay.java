package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TemplateWorkDay {
    private int workDayId;
    private int workTime;
    private int workStartTime;
    private int workEndTime;
}