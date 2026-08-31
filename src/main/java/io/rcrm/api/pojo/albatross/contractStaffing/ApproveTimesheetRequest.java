package io.rcrm.api.pojo.albatross.contractStaffing;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApproveTimesheetRequest {
    private int approvalStatus;
    private String remark;

    // Manual setters and getters to ensure they work
    public void setApprovalStatus(int approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getApprovalStatus() {
        return this.approvalStatus;
    }

    public String getRemark() {
        return this.remark;
    }
}