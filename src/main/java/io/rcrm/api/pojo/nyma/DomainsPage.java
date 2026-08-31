package io.rcrm.api.pojo.nyma;

public class DomainsPage {

	private String domain;
	private String name;
	private int entity_type;

	public DomainsPage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DomainsPage(String domain, String name, int entity_type) {
		super();
		this.domain = domain;
		this.name = name;
		this.entity_type = entity_type;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getEntity_type() {
		return entity_type;
	}

	public void setEntity_type(int entity_type) {
		this.entity_type = entity_type;
	}

}