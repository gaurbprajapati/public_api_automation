package io.rcrm.api.pojo;

public class UpdatePitchStage {
    private int status_id;
    private String stage_date;
    private String remark;

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public String getStage_date() {
        return stage_date;
    }

    public void setStage_date(String stage_date) {
        this.stage_date = stage_date;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
