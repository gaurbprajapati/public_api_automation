package io.rcrm.api.pojo.albatross.notification;

public class Context {
	
	private Template template;
	private Cta cta;

	public Context() {
	}
	
	public Context(Template template, Cta cta) {
		this.template = template;
		this.cta = cta;
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public Cta getCta() {
		return cta;
	}

	public void setCta(Cta cta) {
		this.cta = cta;
	}
	
	

}
