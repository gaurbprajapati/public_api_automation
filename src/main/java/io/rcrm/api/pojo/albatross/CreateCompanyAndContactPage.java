package io.rcrm.api.pojo.albatross;

public class CreateCompanyAndContactPage {

	ContactPage contact;
	CompanyPage company;
	boolean address_changed;

	public CreateCompanyAndContactPage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CreateCompanyAndContactPage(ContactPage contactPage, CompanyPage companyPage, boolean address_changed) {
		super();
		this.contact = contact;
		this.company = company;
		this.address_changed = address_changed;
	}

	public ContactPage getContact() {
		return contact;
	}

	public void setContact(ContactPage contact) {
		this.contact = contact;
	}

	public CompanyPage getCompany() {
		return company;
	}

	public void setCompany(CompanyPage company) {
		this.company = company;
	}

	public boolean isAddress_changed() {
		return address_changed;
	}

	public void setAddress_changed(boolean address_changed) {
		this.address_changed = address_changed;
	}

}
