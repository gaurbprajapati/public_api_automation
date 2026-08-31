package io.rcrm.api.pojo;
import com.fasterxml.jackson.annotation.JsonInclude;

public class HiringStage {

	public HiringStage() {
	}
	
	private int status_id;
	private String remark;
	private String stage_date;
	private boolean create_placement;

	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int updated_by;

	public int getStatus_id() {
		return status_id;
	}

	public void setStatus_id(int status_id) {
		this.status_id = status_id;
	}
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public String getStage_date() {
		return stage_date;
	}

	public void setStage_date(String stage_date) {
		this.stage_date = stage_date;
	}

	public int getUpdated_by() {
		return updated_by;
	}

	public void setUpdated_by(int updated_by) {
		this.updated_by = updated_by;
	}

	public boolean getCreate_placement() {
		return create_placement;
	}

	public void setCreate_placement(boolean create_placement) {
		this.create_placement = create_placement;
	}

}
