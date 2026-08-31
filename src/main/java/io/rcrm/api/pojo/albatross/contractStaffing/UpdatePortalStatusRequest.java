package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class UpdatePortalStatusRequest {
    private int contractorId;
    private String firstName;
    private String lastName;
    private String contractorEmail;
    private int recruiterUserId;
    private int portalStatus;
    private String recruiterName;
}
