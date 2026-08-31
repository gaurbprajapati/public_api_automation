package io.rcrm.api.pojo.nyma;

public class Scheduled_emailPage {

	ScheduledEmailPage scheduledEmailPage;
	private int linked_email_type;

	public  Scheduled_emailPage() {
		super();
		// TODO Auto-generated constructor stub
		this.linked_email_type = 1;
	}
	public Scheduled_emailPage( ScheduledEmailPage scheduledEmailPage) {
		super();
		this.scheduledEmailPage = scheduledEmailPage;
		this.linked_email_type = 1;
	}

	public Scheduled_emailPage(ScheduledEmailPage scheduledEmailPage, int linked_email_type) {
		super();
		this.scheduledEmailPage = scheduledEmailPage;
		this.linked_email_type = linked_email_type;
	}

	public ScheduledEmailPage getScheduled_email() {
		return scheduledEmailPage;
	}

	public void setScheduled_email(ScheduledEmailPage scheduledEmailPage) {
		this.scheduledEmailPage = scheduledEmailPage;
	}

	public int getLinked_email_type() {
		return linked_email_type;
	}

	public void setLinked_email_type(int linked_email_type) {
		this.linked_email_type = linked_email_type;
	}
}

