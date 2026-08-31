package io.recruitcrm.albatross.company;

import io.recruitcrm.albatross.contact.Contact;

public class CompanyJson {

	
	Company company;
	Contact contact;
	private boolean address_changed;
	private String existingContacts;
	
	
	public CompanyJson() {
		super();
	}

	public CompanyJson(boolean address_changed, Company company, Contact contact) {
		super();
		this.address_changed = address_changed;
		this.company = company;
		this.contact = contact;
	}

	public boolean isAddress_changed() {
		return address_changed;
	}

	public void setAddress_changed(boolean address_changed) {
		this.address_changed = address_changed;
	}
	
	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public String getExistingContacts() {
		return existingContacts;
	}

	public void setExistingContacts(String existingContacts) {
		this.existingContacts = existingContacts;
	}
}