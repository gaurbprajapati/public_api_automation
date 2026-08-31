package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class Approvers {
    private List<Integer> agencyIds;
    private List<Integer> clientIds;
}