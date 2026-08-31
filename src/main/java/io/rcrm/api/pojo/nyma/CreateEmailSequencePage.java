package io.rcrm.api.pojo.nyma;

public class CreateEmailSequencePage {
	int entity_type;
	String seq_title;
	String seq_settings;
	boolean silent_progress;
	int save_steps;

	public CreateEmailSequencePage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CreateEmailSequencePage(int entity_type, String seq_title, String seq_settings, boolean silent_progress,int save_steps) {
		super();
		this.entity_type = entity_type;
		this.seq_title = seq_title;
		this.seq_settings = seq_settings;
		this.silent_progress = silent_progress;
		this.save_steps = save_steps;
	}

	public int getEntity_type() {
		return entity_type;
	}

	public void setEntity_type(int entity_type) {
		this.entity_type = entity_type;
	}

	public String getSeq_title() {
		return seq_title;
	}

	public void setSeq_title(String seq_title) {
		this.seq_title = seq_title;
	}

	public String getSeq_settings() {
		return seq_settings;
	}

	public void setSeq_settings(String seq_settings) {
		this.seq_settings = seq_settings;
	}

	public boolean isSilent_progress() {
		return silent_progress;
	}

	public void setSilent_progress(boolean silent_progress) {
		this.silent_progress = silent_progress;
	}

	public int getSave_steps() {
		return save_steps;
	}

	public void setSave_steps(int save_steps) {
		this.save_steps = save_steps;
	}
	
	
	

}
