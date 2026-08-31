package io.rcrm.api.pojo.nyma;

public class DeleteDraftPage {
	
	String id;
	String version;
	String grant_id;
	
	
	public DeleteDraftPage() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public DeleteDraftPage(String id, String version, String grant_id) {
		super();
		this.id = id;
		this.version = version;
		this.grant_id = grant_id;
	}


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getGrant_id() {
		return grant_id;
	}
	public void setGrant_id(String grant_id) {
		this.grant_id = grant_id;
	}
	
	

}
