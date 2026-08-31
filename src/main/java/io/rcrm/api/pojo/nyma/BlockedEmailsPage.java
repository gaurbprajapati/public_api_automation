package io.rcrm.api.pojo.nyma;

import java.util.ArrayList;

public class BlockedEmailsPage {

	private ArrayList<Object> blacklist_domain_details;
	private ArrayList<Object> blacklist_email_details;

	public BlockedEmailsPage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BlockedEmailsPage(ArrayList<Object> blacklist_domain_details, ArrayList<Object> blacklist_email_details) {
		this.blacklist_domain_details = blacklist_domain_details;
		this.blacklist_email_details = blacklist_email_details;

	}

	public ArrayList<Object> getBlacklist_domain_details() {
		return blacklist_domain_details;
	}

	public void setBlacklist_domain_details(ArrayList<Object> blacklist_domain_details) {
		this.blacklist_domain_details = blacklist_domain_details;
	}

	public ArrayList<Object> getBlacklist_email_details() {
		return blacklist_email_details;
	}

	public void setBlacklist_email_details(ArrayList<Object> blacklist_email_details) {
		this.blacklist_email_details = blacklist_email_details;
	}
}