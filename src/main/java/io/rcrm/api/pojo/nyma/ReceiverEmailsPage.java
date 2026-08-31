package io.rcrm.api.pojo.nyma;

public class ReceiverEmailsPage {

	private String entity_slug;

	private String email;
	private String name;
	private int entity_type;
	private int entity_id;

	public ReceiverEmailsPage() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ReceiverEmailsPage(String email,String name, String entity_slug, int entity_type,int entity_id) {
		super();
		this.email = email;
		this.name = name;
		this.entity_slug = entity_slug;
		this.entity_type = entity_type;
		this.entity_id = entity_id;
	}
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getEntity_slug() {
		return entity_slug;
	}

	public void setEntity_slug(String entity_slug) {
		this.entity_slug = entity_slug;
	}
	public int getEntity_type() {
		return entity_type;
	}

	public void setEntity_type(int entity_type) {
		this.entity_type = entity_type;
	}
	public int getEntity_id() {
		return entity_id;
	}

	public void setEntity_id(int candidateId) {
		this.entity_id = candidateId;
	}
}

