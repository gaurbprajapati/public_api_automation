package io.rcrm.api.pojo.albatross.contractStaffing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class UpdateReimbursementStatusRequest {
    private int status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String remark;
}
