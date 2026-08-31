package io.rcrm.api.pojo.albatross.hiringpipeline;

import java.util.ArrayList;

public class CreateHiringPipeline {

	private String name;
	private String is_primary;
	private ArrayList<Object> hiring_stages;

	public CreateHiringPipeline() {

	}

	public CreateHiringPipeline(String name, String is_primary, ArrayList<Object> hiring_stages) {
		super();
		this.name = name;
		this.is_primary = is_primary;
		this.hiring_stages = hiring_stages;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIs_primary() {
		return is_primary;
	}

	public void setIs_primary(String is_primary) {
		this.is_primary = is_primary;
	}

	public ArrayList<Object> getHiring_stages() {
		return hiring_stages;
	}

	public void setHiring_stages(ArrayList<Object> hiring_stages) {
		this.hiring_stages = hiring_stages;
	}

}
