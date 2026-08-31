package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class UploadReimbursementDocumentRequest {
    private String fileName;
    private int timesheetId;
}
