package io.rcrm.api.pojo.albatross.hiringpipeline;

import java.util.List;

public class HiringStages {

	private int id;
	private int sequenceno;

	public HiringStages() {

	}

	public HiringStages(int id, int sequenceno) {
		super();
		this.id = id;
		this.sequenceno = sequenceno;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSequenceno() {
		return sequenceno;
	}

	public void setSequenceno(int sequenceno) {
		this.sequenceno = sequenceno;
	}

}
